package service.sesion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Preferencias de presentación de este equipo; hoy, solo el tema claro u
 * oscuro.
 *
 * <p>Vive junto a {@link SessionManager}, en la carpeta del usuario
 * ({@code ~/.comercio-electronico/}) y no en la del proyecto, por el mismo
 * motivo: es del equipo de quien usa la aplicación, no del programa que se
 * entrega. Va en un archivo aparte porque cerrar sesión borra la sesión
 * recordada, y el tema no debe borrarse con ella.</p>
 *
 * <p>Si el archivo no existe o no se puede leer, vale el tema claro: una
 * preferencia perdida nunca impide arrancar.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class PreferenciasUsuario {

    private static final String CLAVE_TEMA_OSCURO = "tema.oscuro";
    private static final Path RUTA = Path.of(System.getProperty("user.home"),
            ".comercio-electronico", "preferencias.properties");

    private PreferenciasUsuario() {
        // Solo métodos estáticos, igual que SessionManager.
    }

    /** @return {@code true} si la última vez se eligió el modo oscuro */
    public static boolean temaOscuro() {
        if (!Files.isRegularFile(RUTA)) {
            return false;
        }
        Properties datos = new Properties();
        try (InputStream entrada = Files.newInputStream(RUTA)) {
            datos.load(entrada);
        } catch (IOException ex) {
            return false;
        }
        return Boolean.parseBoolean(datos.getProperty(CLAVE_TEMA_OSCURO, "false"));
    }

    /**
     * @param oscuro {@code true} para recordar el modo oscuro
     */
    public static void guardarTemaOscuro(boolean oscuro) {
        Properties datos = new Properties();
        datos.setProperty(CLAVE_TEMA_OSCURO, String.valueOf(oscuro));
        try {
            Files.createDirectories(RUTA.getParent());
            try (OutputStream salida = Files.newOutputStream(RUTA)) {
                datos.store(salida, "Preferencias de Comercio Electronico");
            }
        } catch (IOException ex) {
            // Un adorno: si no se puede guardar, el tema vale para esta ejecución.
            System.err.println("No se pudo recordar el tema: " + ex.getMessage());
        }
    }
}
