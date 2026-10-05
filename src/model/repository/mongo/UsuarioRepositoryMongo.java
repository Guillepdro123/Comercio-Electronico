package model.repository.mongo;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import model.entity.Usuario;
import model.repository.IUsuarioRepository;
import model.repository.mongo.adapter.AdaptadorDocumento;
import model.repository.mongo.adapter.UsuarioAdapter;

/**
 * Implementación de {@link IUsuarioRepository} sobre la colección
 * {@code usuarios} de MongoDB Atlas.
 *
 * <p><b>Solo consultas.</b> Qué documentos se buscan, con qué filtro y qué
 * operación se lanza es de esta clase; cómo se convierte un documento en un
 * {@code Cliente} o un {@code Proveedor} es de {@link UsuarioAdapter}. Los
 * nombres de campo de los filtros salen de ese mismo adaptador, para que la
 * consulta y el documento no puedan desincronizarse.</p>
 *
 * <p><b>Dos reglas de unicidad, las dos en la base.</b> Ni la identificación
 * ni el correo pueden repetirse, y cada una tiene su índice único. El índice
 * es lo que garantiza la regla aunque dos registros lleguen a la vez desde dos
 * equipos, o alguien inserte por fuera de la aplicación: la comprobación
 * previa de {@link #registrar(Usuario)} da el caso normal, el índice cierra la
 * carrera. La colección no se crea a mano: MongoDB la materializa con el
 * primer documento.</p>
 *
 * <p><b>Aquí no se cifra nada.</b> Las contraseñas llegan ya hasheadas desde
 * el controlador ({@code CifradoPassword}): el repositorio es acceso a datos,
 * no política de seguridad.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.2
 */
public class UsuarioRepositoryMongo implements IUsuarioRepository {

    private static final String COLECCION = "usuarios";

    /**
     * Cómo se comparan los correos: sin distinguir mayúsculas
     * ({@code SECONDARY} ignora mayúsculas pero no acentos).
     *
     * <p><b>La misma para el índice y para las búsquedas, a propósito.</b> Si
     * el índice considerara iguales "Ana@X.com" y "ana@x.com" pero la búsqueda
     * no (o al revés), la aplicación podría decir "ese correo está libre" y la
     * base rechazar la inserción, o dejar entrar un duplicado. Con una sola
     * definición, "existe" significa lo mismo en los dos sitios. Así se
     * conserva el correo tal como lo escribió el usuario, sin tener que
     * guardar una copia en minúsculas.</p>
     */
    private static final Collation COMPARACION_CORREO = Collation.builder()
            .locale("es")
            .collationStrength(CollationStrength.SECONDARY)
            .build();

    private final MongoCollection<Document> usuarios;
    private final AdaptadorDocumento<Usuario> adaptador = new UsuarioAdapter();

    /**
     * @param base base de datos ya conectada
     */
    public UsuarioRepositoryMongo(MongoDatabase base) {
        this.usuarios = base.getCollection(COLECCION);
        this.usuarios.createIndex(Indexes.ascending(UsuarioAdapter.CAMPO_IDENTIFICACION),
                new IndexOptions().unique(true));
        crearIndiceCorreo();
    }

    /**
     * Índice único sobre el correo, con la misma comparación que las
     * búsquedas.
     *
     * <p>Si la colección ya tuviera correos repetidos, MongoDB no puede crear
     * el índice. Eso no detiene el arranque: se avisa por consola y la regla
     * sigue aplicándose en {@link #registrar(Usuario)}, que es lo que la
     * aplicación necesita para no crear duplicados nuevos. Verificado antes de
     * añadirlo: el clúster del proyecto no tenía ninguno.</p>
     */
    private void crearIndiceCorreo() {
        try {
            usuarios.createIndex(Indexes.ascending(UsuarioAdapter.CAMPO_CORREO),
                    new IndexOptions().unique(true).collation(COMPARACION_CORREO));
        } catch (MongoException ex) {
            System.out.println("No se pudo crear el índice único de correos (¿hay correos"
                    + " repetidos en 'usuarios'?): " + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Rechaza una identificación o un correo ya registrados. Si otro equipo
     * registra el mismo correo justo entre la comprobación y la inserción, el
     * índice único rechaza la segunda y aquí se traduce a {@code false}, igual
     * que el caso normal.</p>
     */
    @Override
    public boolean registrar(Usuario usuario) {
        if (usuario == null || usuario.getIdentificacion() == null
                || usuario.getIdentificacion().trim().isEmpty()
                || usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty()) {
            return false;
        }
        if (existeIdentificacion(usuario.getIdentificacion())
                || buscarPorCorreo(usuario.getCorreo()) != null) {
            return false;
        }
        try {
            usuarios.insertOne(adaptador.aDocumento(usuario));
            return true;
        } catch (MongoWriteException ex) {
            if (esDuplicado(ex)) {
                return false;
            }
            throw ex;
        }
    }

    @Override
    public Usuario buscarPorCorreo(String correo) {
        if (correo == null) {
            return null;
        }
        Document encontrado = usuarios.find(correoIgual(correo))
                .collation(COMPARACION_CORREO).first();
        return encontrado == null ? null : adaptador.aEntidad(encontrado);
    }

    @Override
    public boolean actualizar(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        if (correoDeOtraCuenta(usuario)) {
            return false;
        }
        try {
            return usuarios.replaceOne(
                    Filters.eq(UsuarioAdapter.CAMPO_IDENTIFICACION, usuario.getIdentificacion()),
                    adaptador.aDocumento(usuario),
                    new ReplaceOptions().upsert(false)).getMatchedCount() > 0;
        } catch (MongoWriteException ex) {
            // Otro equipo tomó ese correo entre la comprobación y el guardado.
            if (esDuplicado(ex)) {
                return false;
            }
            throw ex;
        }
    }

    @Override
    public boolean cedulaEnUso(String cedula, String identificacionPropia) {
        return usuarios.find(Filters.and(
                        Filters.ne(UsuarioAdapter.CAMPO_IDENTIFICACION, identificacionPropia),
                        Filters.or(
                                Filters.eq(UsuarioAdapter.CAMPO_IDENTIFICACION, cedula),
                                Filters.eq(UsuarioAdapter.CAMPO_CEDULA, cedula))))
                .first() != null;
    }

    private boolean existeIdentificacion(String identificacion) {
        return usuarios.find(Filters.eq(UsuarioAdapter.CAMPO_IDENTIFICACION, identificacion))
                .first() != null;
    }

    /** @return {@code true} si ese correo lo tiene ya otra identificación */
    private boolean correoDeOtraCuenta(Usuario usuario) {
        return usuarios.find(Filters.and(
                        correoIgual(usuario.getCorreo()),
                        Filters.ne(UsuarioAdapter.CAMPO_IDENTIFICACION, usuario.getIdentificacion())))
                .collation(COMPARACION_CORREO)
                .first() != null;
    }

    /**
     * Igualdad exacta de correo. Sin distinguir mayúsculas solo si la consulta
     * lleva {@link #COMPARACION_CORREO}: por eso cada {@code find} que la usa
     * la añade. Antes era una expresión regular con la opción "i"; la igualdad
     * con comparación es la que puede usar el índice y la que coincide con él.
     */
    private Bson correoIgual(String correo) {
        return Filters.eq(UsuarioAdapter.CAMPO_CORREO, correo.trim());
    }

    /** @return {@code true} si la escritura falló por un índice único */
    private boolean esDuplicado(MongoWriteException ex) {
        return ex.getError().getCategory() == ErrorCategory.DUPLICATE_KEY;
    }
}
