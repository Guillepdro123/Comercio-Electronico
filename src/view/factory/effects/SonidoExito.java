package view.factory.effects;

import java.io.File;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Reproduce el tono corto de confirmación que acompaña a un registro exitoso.
 *
 * <p><b>Degradación silenciosa a propósito:</b> el sonido es un adorno, no
 * parte del caso de uso. Si el equipo no tiene tarjeta de audio, el archivo no
 * está o el sistema no expone una línea de salida, el registro debe terminar
 * igual de bien: por eso cualquier fallo se ignora en vez de propagarse como
 * excepción a la vista.</p>
 *
 * <p>El {@link Clip} se carga una sola vez y se reutiliza rebobinándolo: abrir
 * la línea de audio en cada registro es lento y puede fallar si la anterior
 * todavía no se liberó.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public final class SonidoExito {

    private static final String RECURSO_CLASSPATH = "/resources/audio/exito.wav";
    private static final String RUTA_ARCHIVO = "src/resources/audio/exito.wav";

    private static Clip clip;
    private static boolean intentadoCargar = false;

    private SonidoExito() {
    }

    /** Reproduce el tono desde el principio; no hace nada si el audio no está disponible. */
    public static synchronized void reproducir() {
        if (!intentadoCargar) {
            intentadoCargar = true;
            clip = cargar();
        }
        if (clip == null) {
            return;
        }
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    private static Clip cargar() {
        try {
            URL recurso = SonidoExito.class.getResource(RECURSO_CLASSPATH);
            AudioInputStream audio = recurso != null
                    ? AudioSystem.getAudioInputStream(recurso)
                    : AudioSystem.getAudioInputStream(new File(RUTA_ARCHIVO));
            Clip nuevo = AudioSystem.getClip();
            nuevo.open(audio);
            audio.close();
            return nuevo;
        } catch (Exception ex) {
            // Sin audio la aplicación sigue siendo perfectamente usable.
            return null;
        }
    }
}
