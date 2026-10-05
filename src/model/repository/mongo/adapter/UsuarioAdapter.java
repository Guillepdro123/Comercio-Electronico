package model.repository.mongo.adapter;

import org.bson.Document;
import model.entity.Cliente;
import model.entity.Proveedor;
import model.entity.Usuario;

/**
 * Adaptador entre la entidad {@link Usuario} y los documentos de la colección
 * {@code usuarios}.
 *
 * <p>Un mismo documento puede volver como {@link Cliente} o como
 * {@link Proveedor}. El adaptador decide cuál a partir del campo
 * {@code tipoCuenta}, en vez de guardar el nombre de la clase de Java: el
 * documento sigue siendo legible desde Atlas y no se rompe si una clase cambia
 * de nombre. Es el mismo punto de decisión por texto que usan
 * {@code UsuarioController} y {@code SesionController}, sin {@code instanceof}.</p>
 *
 * <p>Los nombres de los campos que el repositorio usa en sus filtros son
 * públicos: la consulta y el documento tienen que hablar exactamente del mismo
 * campo, y declararlo en un solo sitio impide que se desincronicen.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class UsuarioAdapter implements AdaptadorDocumento<Usuario> {

    /** Campo con índice único: dos cuentas no pueden compartir documento. */
    public static final String CAMPO_IDENTIFICACION = "identificacion";
    /** Campo por el que se busca al iniciar sesión. */
    public static final String CAMPO_CORREO = "correo";

    private static final String CAMPO_NOMBRES = "nombres";
    private static final String CAMPO_PASSWORD = "password";
    private static final String CAMPO_TELEFONO = "telefono";
    private static final String CAMPO_TIPO = "tipoCuenta";
    private static final String CAMPO_DATO = "datoEspecifico";
    /** Cédula, cuando no coincide con la identificación (cuentas de Google). */
    public static final String CAMPO_CEDULA = "cedula";
    private static final String CAMPO_EMPRESA = "nombreEmpresa";
    private static final String CAMPO_DIRECCION = "direccionEnvio";

    private static final String TIPO_PROVEEDOR = "Proveedor";

    /**
     * La contraseña se guarda tal como llega, que ya es un hash: cifrar es
     * política de seguridad y vive en {@code CifradoPassword}, no aquí.
     *
     * <p><b>Los campos añadidos después solo se escriben si aportan algo.</b>
     * Una cuenta sin cédula aparte, o un Cliente (cuya dirección ya es su
     * {@code datoEspecifico}), produce exactamente el documento de antes; así
     * los documentos que ya hay en Atlas no cambian de forma.</p>
     */
    @Override
    public Document aDocumento(Usuario usuario) {
        Document documento = new Document(CAMPO_IDENTIFICACION, usuario.getIdentificacion())
                .append(CAMPO_NOMBRES, usuario.getNombres())
                .append(CAMPO_CORREO, usuario.getCorreo())
                .append(CAMPO_PASSWORD, usuario.getPassword())
                .append(CAMPO_TELEFONO, usuario.getTelefono())
                .append(CAMPO_TIPO, usuario.getTipoCuenta())
                .append(CAMPO_DATO, usuario.getDatoEspecifico());
        if (!usuario.getCedula().isEmpty()
                && !usuario.getCedula().equals(usuario.getIdentificacion())) {
            documento.append(CAMPO_CEDULA, usuario.getCedula());
        }
        if (usuario.puedeVender()) {
            documento.append(CAMPO_EMPRESA, usuario.getNombreEmpresa())
                    .append(CAMPO_DIRECCION, usuario.getDireccionEnvio());
        }
        return documento;
    }

    /**
     * Reconstruye la subclase que corresponde al tipo guardado.
     *
     * <p>Un documento sin teléfono es normal: las cuentas creadas antes de la
     * edición de perfil no lo tienen, y vuelve como texto vacío, que es el
     * valor con el que arranca la entidad.</p>
     */
    @Override
    public Usuario aEntidad(Document documento) {
        String identificacion = documento.getString(CAMPO_IDENTIFICACION);
        String nombres = documento.getString(CAMPO_NOMBRES);
        String correo = documento.getString(CAMPO_CORREO);
        String password = documento.getString(CAMPO_PASSWORD);
        String dato = documento.getString(CAMPO_DATO);

        Usuario usuario;
        if (TIPO_PROVEEDOR.equals(documento.getString(CAMPO_TIPO))) {
            Proveedor proveedor = new Proveedor(identificacion, nombres, correo, password, dato);
            proveedor.setNombreEmpresa(documento.getString(CAMPO_EMPRESA));
            proveedor.setDireccionEnvio(documento.getString(CAMPO_DIRECCION));
            usuario = proveedor;
        } else {
            usuario = new Cliente(identificacion, nombres, correo, password, dato);
        }
        String telefono = documento.getString(CAMPO_TELEFONO);
        usuario.setTelefono(telefono == null ? "" : telefono);
        usuario.setCedula(documento.getString(CAMPO_CEDULA));
        return usuario;
    }
}
