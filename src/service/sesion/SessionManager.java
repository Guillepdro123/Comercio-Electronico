package service.sesion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Recuerda en disco quién tenía la sesión abierta, para no pedir la
 * contraseña cada vez que se abre la aplicación.
 *
 * <p>Guarda dos datos en {@code session.properties}: el correo
 * ({@code user.email}) y el rol ({@code user.role}). Nada más: ni la
 * contraseña ni su hash, que no hacen falta para reanudar y que en un archivo
 * de texto serían un regalo.</p>
 *
 * <p><b>Dónde vive el archivo.</b> En la carpeta del usuario del sistema
 * ({@code ~/.comercio-electronico/}), no en la del proyecto. La sesión es de
 * la persona sentada frente a este equipo, no del código: si el archivo
 * quedara junto al proyecto, cualquier copia comprimida para entregar llevaría
 * dentro la cuenta abierta de quien la comprimió.</p>
 *
 * <p><b>El archivo es una pista, no una credencial.</b> Quien arranca con él
 * ({@code controller.SesionController}) vuelve a buscar la cuenta en el
 * repositorio y toma el rol de allí; si la cuenta no existe o el rol no
 * coincide, descarta la sesión y muestra el Login. Así, editar el archivo a
 * mano no convierte a un Cliente en Proveedor.</p>
 *
 * <p>Métodos estáticos y sin estado en memoria: el estado es el archivo, y
 * cada llamada lo lee o lo escribe. No hace falta un Singleton para eso.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class SessionManager {

    /** Nombre del archivo, tal como se pidió. */
    public static final String ARCHIVO = "session.properties";

    private static final String CLAVE_CORREO = "user.email";
    private static final String CLAVE_ROL = "user.role";

    private static final Path RUTA = Path.of(System.getProperty("user.home"),
            ".comercio-electronico", ARCHIVO);

    /**
     * Datos de una sesión recordada.
     *
     * @param correo correo de la cuenta
     * @param rol    rol con el que se guardó ({@code "Cliente"} o
     *               {@code "Proveedor"}); se contrasta con el repositorio
     *               antes de usarlo
     */
    public record SesionGuardada(String correo, String rol) { }

    private SessionManager() {
        // Solo métodos estáticos.
    }

    /**
     * Escribe o reemplaza la sesión recordada.
     *
     * <p>Un fallo al escribir no interrumpe el acceso: el usuario ya entró, y
     * lo único que se pierde es que la próxima vez tendrá que escribir su
     * contraseña. Se avisa por consola y se sigue.</p>
     *
     * @param email correo de la cuenta autenticada
     * @param role  rol de esa cuenta
     */
    public static void saveSession(String email, String role) {
        Properties datos = new Properties();
        datos.setProperty(CLAVE_CORREO, email);
        datos.setProperty(CLAVE_ROL, role);
        try {
            Files.createDirectories(RUTA.getParent());
            try (OutputStream salida = Files.newOutputStream(RUTA)) {
                datos.store(salida, "Sesion recordada de Comercio Electronico");
            }
        } catch (IOException ex) {
            System.err.println("No se pudo recordar la sesión: " + ex.getMessage());
        }
    }

    /**
     * Lee la sesión recordada.
     *
     * @return la sesión, o {@code null} si no hay archivo, no se puede leer o
     *         le falta alguno de los dos datos
     */
    public static SesionGuardada getSession() {
        if (!Files.isRegularFile(RUTA)) {
            return null;
        }
        Properties datos = new Properties();
        try (InputStream entrada = Files.newInputStream(RUTA)) {
            datos.load(entrada);
        } catch (IOException ex) {
            // Ilegible es lo mismo que ausente: se pide la contraseña.
            return null;
        }
        String correo = datos.getProperty(CLAVE_CORREO, "").trim();
        String rol = datos.getProperty(CLAVE_ROL, "").trim();
        if (correo.isEmpty() || rol.isEmpty()) {
            return null;
        }
        return new SesionGuardada(correo, rol);
    }

    /**
     * Olvida la sesión borrando el archivo. No pasa nada si ya no existía.
     */
    public static void clearSession() {
        try {
            Files.deleteIfExists(RUTA);
        } catch (IOException ex) {
            System.err.println("No se pudo borrar la sesión recordada: " + ex.getMessage());
        }
    }
}
