package model.repository.mongo;

import java.util.concurrent.TimeUnit;
import service.config.Configuracion;
import com.mongodb.MongoClientSettings;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Conexión a MongoDB Atlas.
 *
 * <p><b>La credencial nunca vive en el código.</b> La cadena
 * {@code mongodb+srv://...} lleva usuario y contraseña, y escrita en un
 * archivo fuente viajaría en cualquier copia del proyecto. Se lee de
 * {@code config.properties} a través de {@link Configuracion}, el lector único
 * del proyecto; ese archivo está excluido del control de versiones y junto a él
 * se entrega {@code config.properties.ejemplo} como plantilla.</p>
 *
 * <p><b>Que no haya configuración no es un error.</b> {@link #disponible()}
 * devuelve {@code false} y {@code app.Main} arranca con el almacén en memoria
 * de siempre. La aplicación tiene que poder abrirse y defenderse sin conexión
 * a la nube: es una entrega académica, no un servicio.</p>
 *
 * <p>Las colecciones no se crean aquí. MongoDB las materializa sola en el
 * primer documento que se inserta, así que no hace falta ningún script de
 * inicialización.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class MongoDBConnection {

    public static final String CLAVE_URI = "mongodb.uri";
    private static final String CLAVE_BASE = "mongodb.base";
    private static final String BASE_POR_DEFECTO = "comercio_electronico";
    /**
     * Cuánto se espera a que Atlas responda antes de darlo por inalcanzable.
     * El valor de fábrica son 30 s, demasiado para una aplicación de
     * escritorio que debe decidir en el arranque si usa la nube o la memoria.
     */
    private static final int SEGUNDOS_ESPERA = 8;

    /*
     * El driver usa SLF4J si lo encuentra y, si no, avisa por consola. No se
     * añade la librería solo para callar un aviso: se sube el umbral del
     * registro, que deja pasar los errores de verdad y calla lo demás. Lo
     * mismo para el componente BSON, que avisa por su cuenta en cuanto se
     * decodifica el primer evento de un Change Stream.
     *
     * Los dos registros se guardan en campos a propósito: java.util.logging
     * los retiene con referencia débil, y si nadie más los retiene el
     * recolector de basura los descarta y el nivel configurado se pierde. Se
     * comprobó con el aviso de BSON, que salía igual con solo llamar a
     * setLevel(...) sobre un registro que no se guardaba en ningún sitio.
     */
    private static final java.util.logging.Logger REGISTRO_DRIVER =
            silenciar("org.mongodb.driver");
    private static final java.util.logging.Logger REGISTRO_BSON = silenciar("org.bson");

    private static java.util.logging.Logger silenciar(String nombre) {
        java.util.logging.Logger registro = java.util.logging.Logger.getLogger(nombre);
        registro.setLevel(java.util.logging.Level.SEVERE);
        return registro;
    }

    private final MongoClient cliente;
    private final MongoDatabase base;

    /**
     * Abre la conexión leyendo {@code config.properties}.
     *
     * @param configuracion propiedades ya leídas; se inyecta en vez de abrir
     *                      el archivo aquí, para que exista un solo lector
     * @throws IllegalStateException si no hay configuración utilizable; quien
     *         la crea decide qué hacer con eso
     */
    public MongoDBConnection(Configuracion configuracion) {
        String uri = configuracion.valor(CLAVE_URI, "");
        if (uri.isEmpty() || uri.contains("USUARIO:CONTRASENA")) {
            throw new IllegalStateException(
                    "Falta " + Configuracion.ARCHIVO + " o su " + CLAVE_URI
                            + " sigue sin rellenar.");
        }

        MongoClientSettings ajustes = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .applyToClusterSettings(b ->
                        b.serverSelectionTimeout(SEGUNDOS_ESPERA, TimeUnit.SECONDS))
                .build();
        this.cliente = MongoClients.create(ajustes);
        this.base = cliente.getDatabase(configuracion.valor(CLAVE_BASE, BASE_POR_DEFECTO));
    }

    /**
     * Comprueba que el servidor responde de verdad.
     *
     * <p>Crear el cliente no conecta: el driver conecta de forma perezosa, así
     * que sin esta comprobación el primer fallo aparecería en mitad de un
     * registro. Se hace una vez, en el arranque, para poder elegir entre la
     * nube y la memoria antes de mostrar nada.</p>
     *
     * @return {@code true} si la base respondió
     */
    public boolean disponible() {
        try {
            base.runCommand(new org.bson.Document("ping", 1));
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /** @return la base de datos sobre la que trabajan los repositorios */
    public MongoDatabase getBase() {
        return base;
    }

    /** Cierra la conexión. La llama {@code app.Main} al terminar el programa. */
    public void cerrar() {
        cliente.close();
    }
}
