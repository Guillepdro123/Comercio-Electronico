package service.imagen;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Prepara una imagen elegida del disco para guardarla en el almacén: la lee,
 * la reduce si es grande y la codifica.
 *
 * <p><b>Por qué reducirla.</b> Una foto de móvil pesa varios megas y mide
 * 4000 px de lado; en la aplicación se muestra a 300 px como mucho. Guardarla
 * entera llenaría la base y haría lento cada catálogo. Con
 * {@value #LADO_MAXIMO} px de lado sobra para el detalle del producto.</p>
 *
 * <p>Se codifica en PNG si tiene transparencia y en JPEG si no: el JPEG pesa
 * una fracción en fotos, pero no guarda transparencia.</p>
 *
 * <p>No importa Swing: trabaja con {@code java.awt.image} e {@code ImageIO},
 * que procesan imágenes sin abrir ninguna ventana.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class OptimizadorImagen {

    /** Lado mayor máximo de la imagen guardada, en píxeles. */
    public static final int LADO_MAXIMO = 800;

    private OptimizadorImagen() {
    }

    /**
     * Resultado de preparar una imagen.
     *
     * @param datos     bytes codificados
     * @param extension {@code "png"} o {@code "jpg"}
     */
    public record ImagenPreparada(byte[] datos, String extension) {
    }

    /**
     * @param archivo imagen del disco ({@code png}, {@code jpg} o {@code jpeg})
     * @return la imagen reducida y codificada
     * @throws IOException si el archivo no existe o no es una imagen legible
     */
    public static ImagenPreparada preparar(File archivo) throws IOException {
        BufferedImage original = ImageIO.read(archivo);
        if (original == null) {
            throw new IOException("El archivo no es una imagen que el programa sepa leer.");
        }
        boolean transparente = original.getColorModel().hasAlpha();
        BufferedImage reducida = reducir(original, transparente);

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        String extension = transparente ? "png" : "jpg";
        ImageIO.write(reducida, extension, salida);
        return new ImagenPreparada(salida.toByteArray(), extension);
    }

    /** Escala a {@value #LADO_MAXIMO} px de lado mayor, conservando la proporción. */
    private static BufferedImage reducir(BufferedImage original, boolean transparente) {
        int ancho = original.getWidth();
        int alto = original.getHeight();
        double escala = Math.min(1.0, LADO_MAXIMO / (double) Math.max(ancho, alto));
        int nuevoAncho = Math.max(1, (int) Math.round(ancho * escala));
        int nuevoAlto = Math.max(1, (int) Math.round(alto * escala));

        // Siempre se redibuja, aunque no haga falta reducir: así la imagen
        // queda en un tipo de color que el codificador JPEG acepta (uno con
        // canal alfa o indexado lo haría fallar).
        BufferedImage destino = new BufferedImage(nuevoAncho, nuevoAlto,
                transparente ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = destino.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(original, 0, 0, nuevoAncho, nuevoAlto, null);
        g2.dispose();
        return destino;
    }
}
