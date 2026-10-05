package service.imagen;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Descarga la imagen de un producto cuya referencia es una URL pública
 * ({@code https://...}).
 *
 * <p><b>Por qué existe.</b> Además de las ilustraciones incluidas y de las
 * imágenes {@code img:<id>} guardadas en Atlas, un producto puede apuntar a una
 * imagen publicada en internet. Igual que esas, la URL es portable: se ve en
 * cualquier equipo con conexión, desde NetBeans o desde el {@code .exe}.</p>
 *
 * <p>Bloquea mientras descarga: no se llama desde el hilo de la interfaz (la
 * fábrica de la vista la usa en segundo plano). No importa Swing.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class DescargadorImagenes {

    /** Tope de tamaño: una imagen de producto razonable pesa unos cientos de KB. */
    private static final int BYTES_MAXIMOS = 8 * 1024 * 1024;
    private static final Duration ESPERA = Duration.ofSeconds(15);

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(ESPERA)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * @param referencia referencia de imagen de un producto
     * @return {@code true} si es una URL web, y no un nombre incluido ni {@code img:<id>}
     */
    public static boolean esUrl(String referencia) {
        return referencia != null
                && (referencia.startsWith("https://") || referencia.startsWith("http://"));
    }

    /**
     * @param url dirección de la imagen
     * @return los bytes de la imagen, o {@code null} si no se pudo obtener una
     *         imagen válida (sin red, enlace roto, no es una imagen o es demasiado
     *         grande): quien la muestra cae en la inicial del producto
     */
    public byte[] descargar(String url) {
        if (!esUrl(url)) {
            return null;
        }
        try {
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(url))
                    .timeout(ESPERA)
                    .header("User-Agent", "ComercioElectronico/1.0")
                    .GET()
                    .build();
            HttpResponse<byte[]> respuesta = cliente.send(peticion,
                    HttpResponse.BodyHandlers.ofByteArray());
            String tipo = respuesta.headers().firstValue("Content-Type").orElse("");
            byte[] datos = respuesta.body();
            if (respuesta.statusCode() != 200 || !tipo.startsWith("image/")
                    || datos.length > BYTES_MAXIMOS) {
                return null;
            }
            return datos;
        } catch (IOException | IllegalArgumentException ex) {
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
