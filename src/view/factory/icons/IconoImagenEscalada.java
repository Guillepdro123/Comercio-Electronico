package view.factory.icons;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.Icon;

/**
 * {@link Icon} que dibuja una imagen estática reducida a un tamaño fijo,
 * cuidando que el resultado se vea nítido.
 *
 * <p><b>El problema que resuelve.</b> Las imágenes del proyecto son bastante
 * más grandes que el tamaño al que se muestran (el logo va de 500px a ~112,
 * el check de 512 a 40). Reducir tanto de un solo paso —da igual si con
 * {@code Image.getScaledInstance(...)} o con un {@code drawImage} bicúbico—
 * deja los trazos finos lavados, porque el filtro solo mira unos pocos
 * píxeles de origen por cada píxel de destino.</p>
 *
 * <p><b>Cómo lo resuelve.</b> Dos cosas:</p>
 * <ol>
 *   <li><b>Trabaja en píxeles físicos, no lógicos:</b> el tamaño destino se
 *       calcula multiplicando por la escala del {@code Graphics2D} (1.25 en
 *       una pantalla de Windows al 125%). Así la copia reducida coincide
 *       exactamente con los píxeles reales del monitor y se dibuja sin
 *       volver a estirarse. Si se generara al tamaño lógico, Swing la
 *       ampliaría después y se perdería lo ganado.</li>
 *   <li><b>Reducción bicúbica y máscara de enfoque:</b> el bicúbico conserva
 *       el contraste de los bordes, y el enfoque posterior recupera la
 *       definición que cualquier reducción grande se lleva por delante.</li>
 * </ol>
 *
 * <p><b>Por qué no reduce a la mitad por pasos.</b> Es la receta habitual
 * para reducciones grandes, y aquí se probó y se midió: con este emblema dio
 * un resultado <em>menos</em> definido que el bicúbico de un solo paso
 * (gradiente medio 35,3 frente a 38,3). Tiene sentido: el promediado
 * repetido suaviza, mientras que el bicúbico realza el borde. Con el enfoque
 * aplicado encima, la medida sube a ~62. Si alguien vuelve a plantear el
 * escalado por pasos, conviene medirlo antes de asumir que mejora.</p>
 *
 * <p>El resultado se guarda en caché por tamaño físico: la reducción ocurre
 * una sola vez, no en cada repintado.</p>
 *
 * <p><b>Solo imágenes estáticas.</b> Al cachear una copia, un GIF animado se
 * quedaría congelado en su primer cuadro; para esos está
 * {@link IconoGifAnimado}, que además los decodifica con {@code ImageIO}.</p>
 *
 * <p>Autocontenido, como {@link IconoOjo}: no pinta fondos de contenedores
 * ni depende de {@code setOpaque(false)} en nada ajeno, así que no cae en el
 * patrón de {@code Graphics2D} que causó problemas de contraste en una
 * iteración anterior (ver "Estabilidad de la interfaz" en {@code CLAUDE.md}).</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 2.0
 */
public class IconoImagenEscalada implements Icon {

    /** Intensidad de la máscara de enfoque; medida como el mejor compromiso nitidez/halos. */
    private static final float FUERZA_ENFOQUE = 0.5f;

    private final Image imagen;
    private final int ancho;
    private final int alto;

    private BufferedImage copiaReducida;
    private int anchoCopia = -1;

    /**
     * @param imagen imagen estática ya cargada
     * @param ancho  ancho (lógico) al que se dibuja el ícono
     * @param alto   alto (lógico) al que se dibuja el ícono
     */
    public IconoImagenEscalada(Image imagen, int ancho, int alto) {
        this.imagen = imagen;
        this.ancho = ancho;
        this.alto = alto;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(versionPara(g2.getTransform().getScaleX(), c), x, y, ancho, alto, c);
        g2.dispose();
    }

    /**
     * Devuelve la copia reducida al tamaño físico que corresponde a
     * {@code escala}, generándola la primera vez.
     *
     * @param escala      factor de escala del monitor (1.0 sin escalado)
     * @param observador  componente que observa la carga de la imagen
     * @return la copia reducida, o la imagen original si todavía no se ha cargado
     */
    private Image versionPara(double escala, Component observador) {
        int anchoFisico = Math.max(1, (int) Math.round(ancho * escala));
        int altoFisico = Math.max(1, (int) Math.round(alto * escala));
        if (copiaReducida != null && anchoCopia == anchoFisico) {
            return copiaReducida;
        }
        BufferedImage fuente = aBufferedImage(imagen, observador);
        if (fuente == null) {
            // Aún sin dimensiones conocidas: se dibuja el original y el
            // observador provocará otro repintado cuando termine de cargar.
            return imagen;
        }
        copiaReducida = enfocar(reducir(fuente, anchoFisico, altoFisico));
        anchoCopia = anchoFisico;
        return copiaReducida;
    }

    private static BufferedImage reducir(BufferedImage fuente, int ancho, int alto) {
        BufferedImage destino = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = destino.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(fuente, 0, 0, ancho, alto, null);
        g.dispose();
        return destino;
    }

    /**
     * Máscara de enfoque: realza cada píxel frente a sus vecinos, devolviendo
     * definición a los trazos finos. {@code EDGE_NO_OP} deja el borde de la
     * imagen sin tocar para no ensuciar el contorno transparente.
     */
    private static BufferedImage enfocar(BufferedImage fuente) {
        float centro = 1f + 4f * FUERZA_ENFOQUE;
        float lado = -FUERZA_ENFOQUE;
        float[] nucleo = {0, lado, 0, lado, centro, lado, 0, lado, 0};
        ConvolveOp op = new ConvolveOp(new Kernel(3, 3, nucleo), ConvolveOp.EDGE_NO_OP, null);
        return op.filter(fuente,
                new BufferedImage(fuente.getWidth(), fuente.getHeight(), BufferedImage.TYPE_INT_ARGB));
    }

    private static BufferedImage aBufferedImage(Image imagen, Component observador) {
        if (imagen instanceof BufferedImage buffer) {
            return buffer;
        }
        int ancho = imagen.getWidth(observador);
        int alto = imagen.getHeight(observador);
        if (ancho <= 0 || alto <= 0) {
            return null;
        }
        BufferedImage copia = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copia.createGraphics();
        g.drawImage(imagen, 0, 0, observador);
        g.dispose();
        return copia;
    }

    @Override
    public int getIconWidth() {
        return ancho;
    }

    @Override
    public int getIconHeight() {
        return alto;
    }
}
