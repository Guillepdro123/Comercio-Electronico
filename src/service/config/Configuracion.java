package service.config;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Lector único de {@code config.properties}.
 *
 * <p><b>Por qué centralizarlo.</b> La conexión a Mongo y el envío de correo
 * necesitan credenciales distintas del mismo archivo. Si cada una lo abriera
 * por su cuenta, el formato, la codificación y el criterio de "qué pasa si
 * falta" se decidirían dos veces y acabarían divergiendo. Aquí se lee una vez
 * y cada componente pide solo su clave.</p>
 *
 * <p><b>Falta el archivo no es un error.</b> Devuelve valores vacíos y quien
 * pregunta decide: la conexión cae al almacén en memoria, el correo cae al
 * notificador local. La aplicación tiene que poder abrirse sin credenciales.</p>
 *
 * <p><b>No es un Singleton con estado global.</b> Se instancia en
 * {@code app.Main} y se pasa a quien la necesite, igual que los repositorios:
 * un objeto de configuración alcanzable desde cualquier punto del programa
 * sería justo la dependencia oculta que este proyecto evita.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class Configuracion {

    /** Nombre del archivo; dónde se busca lo decide {@link #ubicar()}. */
    public static final String ARCHIVO = "config.properties";

    /**
     * Propiedad que el lanzador de jpackage define con la ruta del
     * {@code .exe}. Fuera del ejecutable empaquetado no existe.
     */
    private static final String RUTA_EJECUTABLE = "jpackage.app-path";

    /** Archivo encontrado, o {@code null} si no hay ninguno. */
    private final Path ruta;

    private final Properties propiedades;

    /** Lee el archivo una sola vez; si no existe, queda vacía. */
    public Configuracion() {
        this.ruta = ubicar();
        this.propiedades = cargar();
    }

    /**
     * @param clave        nombre de la propiedad
     * @param porDefecto   valor a devolver si no está o viene vacía
     * @return el valor sin espacios sobrantes
     */
    public String valor(String clave, String porDefecto) {
        String encontrado = propiedades.getProperty(clave);
        if (encontrado == null || encontrado.trim().isEmpty()) {
            return porDefecto;
        }
        return encontrado.trim();
    }

    /**
     * @param clave nombre de la propiedad
     * @return {@code true} si está presente y con contenido
     */
    public boolean tiene(String clave) {
        return !valor(clave, "").isEmpty();
    }

    /** @return {@code true} si el archivo de configuración existe */
    public boolean existeArchivo() {
        return ruta != null;
    }

    /**
     * Busca el archivo, en este orden: la carpeta de trabajo, la carpeta del
     * {@code .exe} y la del JAR.
     *
     * <p>Antes solo se miraba la carpeta de trabajo, que es la raíz del
     * proyecto al ejecutar desde NetBeans. Con el {@code .exe} esa carpeta
     * depende de cómo se abra: un acceso directo sin "Iniciar en", o el
     * programa lanzado desde otra consola, arrancaban sin MongoDB aunque el
     * archivo estuviera junto al ejecutable. La carpeta de trabajo sigue
     * siendo la primera para que NetBeans se comporte como siempre.</p>
     *
     * @return el archivo encontrado, o {@code null}
     */
    private static Path ubicar() {
        List<Path> carpetas = new ArrayList<>();
        carpetas.add(Path.of(""));
        String ejecutable = System.getProperty(RUTA_EJECUTABLE);
        if (ejecutable != null) {
            carpetas.add(Path.of(ejecutable).toAbsolutePath().getParent());
        }
        Path jar = ubicacionDelJar();
        if (jar != null) {
            carpetas.add(jar.getParent());
        }
        for (Path carpeta : carpetas) {
            Path candidato = carpeta == null ? null : carpeta.resolve(ARCHIVO);
            if (candidato != null && Files.isRegularFile(candidato)) {
                return candidato;
            }
        }
        return null;
    }

    /**
     * @return el JAR del que se cargó esta clase, o {@code null} si se ejecuta
     *         desde carpetas de clases (NetBeans) o no se puede averiguar
     */
    private static Path ubicacionDelJar() {
        try {
            Path origen = Path.of(Configuracion.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            return Files.isRegularFile(origen) ? origen : null;
        } catch (URISyntaxException | RuntimeException ex) {
            // No saber dónde está el JAR solo quita un sitio donde buscar.
            return null;
        }
    }

    private Properties cargar() {
        Properties leidas = new Properties();
        if (ruta == null) {
            return leidas;
        }
        try {
            String texto = Files.readString(ruta, StandardCharsets.UTF_8);
            // PowerShell 5.1 y el Bloc de notas antiguo guardan el UTF-8 con
            // una marca BOM al inicio; sin quitarla, la primera clave pasaría
            // a ser "﻿mongodb.uri" y la conexión se perdería sin aviso.
            if (texto.startsWith("﻿")) {
                texto = texto.substring(1);
            }
            leidas.load(new StringReader(texto));
        } catch (IOException ex) {
            // Un archivo ilegible se trata como ausente: el arranque sigue con
            // los respaldos en vez de detenerse.
            return new Properties();
        }
        return leidas;
    }
}
