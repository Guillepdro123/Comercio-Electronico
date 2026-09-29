package view.factory.icons;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.swing.Icon;
import javax.swing.Timer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * {@link Icon} que reproduce un GIF animado decodificando sus cuadros con
 * {@link ImageIO} y alternándolos con un {@link Timer} de Swing.
 *
 * <p><b>Por qué no usa {@code Toolkit}/{@code ImageIcon} (como
 * {@link IconoImagenEscalada}):</b> para el asistente del sidebar,
 * {@code new ImageIcon(url)} reporta {@code MediaTracker.ABORTED} y
 * {@code Graphics.drawImage(...)} nunca llega a pintar nada — el
 * decodificador GIF heredado de AWT ({@code sun.awt.image.GifImageDecoder})
 * no soporta bien este archivo (cuadros con tamaño distinto al lienzo
 * completo, típico de GIFs que solo codifican la región que cambia en cada
 * cuadro). Se comprobó con un diagnóstico aparte que {@code ImageIO}
 * decodifica el mismo archivo sin problemas, así que esta clase decodifica
 * los cuadros una sola vez con {@code ImageIO} y los compone sobre un lienzo
 * propio (los cuadros más pequeños se dibujan en su posición dentro del
 * lienzo, tomada de los metadatos del GIF) para obtener una lista de
 * imágenes estáticas completas, una por cuadro.</p>
 *
 * <p><b>No reintroduce el patrón prohibido de "Estabilidad de la
 * interfaz":</b> el {@code Graphics2D} de esta clase solo compone imágenes
 * fuera de pantalla (en un {@link BufferedImage} propio, antes de pintar) y
 * nunca pinta el fondo de un contenedor Swing en vivo ni toca
 * {@code setOpaque} de otro componente — {@code paintIcon} solo hace
 * {@code Graphics.drawImage(...)} de un cuadro ya resuelto, igual que
 * {@link IconoImagenEscalada}.</p>
 *
 * <p><b>Método de descarte ("disposal method") de cada cuadro:</b> sí se
 * interpreta — un diagnóstico aparte mostró que el asistente del sidebar usaba
 * {@code restoreToBackgroundColor} en dos de sus cuatro cuadros (el primero
 * ocupa el lienzo completo 1080×1080 y debe limpiarse antes de mostrar el
 * segundo, mucho más pequeño). Sin esto, el cuadro anterior queda "fantasma"
 * detrás del siguiente cada vez que este último no lo cubre por completo —
 * eso era el parpadeo reportado, no un problema de doble buffer de Swing
 * (que ya está activo por defecto a través de {@code RepaintManager}).</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public class IconoGifAnimado implements Icon {

    private static final int RETARDO_MINIMO_MS = 20;
    private static final int RETARDO_POR_DEFECTO_MS = 100;
    /** Ajusta la velocidad respecto al archivo (ver {@link #leerRetardoMs}). */
    private static final double FACTOR_RALENTIZACION = 1.0;
    private static final String DISPOSAL_RESTAURAR_FONDO = "restoreToBackgroundColor";
    private static final String DISPOSAL_RESTAURAR_PREVIO = "restoreToPrevious";

    private final List<BufferedImage> cuadros;
    private final int[] retardosMs;
    private final int ancho;
    private final int alto;
    private final Timer temporizador;
    private int indiceActual = 0;
    private Component componenteAPintar;

    /**
     * Decodifica el GIF de inmediato (bloqueante, igual que
     * {@code new ImageIcon(url)}) y, si tiene más de un cuadro, arranca el
     * temporizador de animación.
     *
     * @param recurso ubicación del archivo GIF
     * @param ancho   ancho al que se dibuja el ícono
     * @param alto    alto al que se dibuja el ícono
     */
    public IconoGifAnimado(URL recurso, int ancho, int alto) {
        this.ancho = ancho;
        this.alto = alto;

        List<Integer> retardosDecodificados = new ArrayList<>();
        this.cuadros = decodificarCuadros(recurso, retardosDecodificados);
        this.retardosMs = new int[retardosDecodificados.size()];
        for (int i = 0; i < retardosMs.length; i++) {
            retardosMs[i] = retardosDecodificados.get(i);
        }

        if (cuadros.size() > 1) {
            temporizador = new Timer(retardosMs[0], null);
            temporizador.addActionListener(e -> {
                indiceActual = (indiceActual + 1) % cuadros.size();
                temporizador.setDelay(Math.max(retardosMs[indiceActual], RETARDO_MINIMO_MS));
                if (componenteAPintar != null) {
                    componenteAPintar.repaint();
                }
            });
            temporizador.start();
        } else {
            temporizador = null;
        }
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        componenteAPintar = c;
        if (!cuadros.isEmpty()) {
            g.drawImage(cuadros.get(indiceActual), x, y, ancho, alto, c);
        }
    }

    @Override
    public int getIconWidth() {
        return ancho;
    }

    @Override
    public int getIconHeight() {
        return alto;
    }

    /**
     * Decodifica todos los cuadros del GIF con {@link ImageIO} y los compone
     * sobre un lienzo del tamaño del primer cuadro (que en un GIF ocupa el
     * lienzo lógico completo), aplicando el método de descarte de cada
     * cuadro antes de dibujar el siguiente: {@code restoreToBackgroundColor}
     * limpia (a transparente) el área que ocupaba ese cuadro,
     * {@code restoreToPrevious} devuelve el lienzo al estado que tenía antes
     * de dibujarlo, y cualquier otro valor ({@code doNotDispose}/{@code none})
     * deja el lienzo tal cual para que el siguiente cuadro se dibuje encima.
     *
     * @param recurso          ubicación del archivo GIF
     * @param retardosSalida   lista donde se acumula el retardo (en ms) de cada cuadro devuelto
     * @return cuadros ya compuestos, o una lista vacía si el archivo no se pudo decodificar
     */
    private static List<BufferedImage> decodificarCuadros(URL recurso, List<Integer> retardosSalida) {
        List<BufferedImage> resultado = new ArrayList<>();
        try (ImageInputStream entrada = ImageIO.createImageInputStream(recurso.openStream())) {
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(entrada);
            if (!lectores.hasNext()) {
                return resultado;
            }
            ImageReader lector = lectores.next();
            lector.setInput(entrada);

            int numCuadros = lector.getNumImages(true);
            BufferedImage lienzo = null;
            BufferedImage instantaneaPrevia = null;
            Rectangle rectAnterior = null;
            String disposalAnterior = null;

            for (int i = 0; i < numCuadros; i++) {
                BufferedImage cuadroBruto = lector.read(i);
                IIOMetadata metadatos = lector.getImageMetadata(i);
                int[] posicion = leerPosicion(metadatos);
                Rectangle rect = new Rectangle(posicion[0], posicion[1], cuadroBruto.getWidth(), cuadroBruto.getHeight());
                String disposalActual = leerDisposal(metadatos);

                if (lienzo == null) {
                    lienzo = new BufferedImage(cuadroBruto.getWidth(), cuadroBruto.getHeight(), BufferedImage.TYPE_INT_ARGB);
                } else if (DISPOSAL_RESTAURAR_FONDO.equals(disposalAnterior)) {
                    Graphics2D limpiar = lienzo.createGraphics();
                    limpiar.setComposite(AlphaComposite.Clear);
                    limpiar.fillRect(rectAnterior.x, rectAnterior.y, rectAnterior.width, rectAnterior.height);
                    limpiar.dispose();
                } else if (DISPOSAL_RESTAURAR_PREVIO.equals(disposalAnterior) && instantaneaPrevia != null) {
                    lienzo = copiar(instantaneaPrevia);
                }

                if (DISPOSAL_RESTAURAR_PREVIO.equals(disposalActual)) {
                    instantaneaPrevia = copiar(lienzo);
                }

                Graphics2D g2 = lienzo.createGraphics();
                g2.drawImage(cuadroBruto, rect.x, rect.y, null);
                g2.dispose();

                resultado.add(copiar(lienzo));
                retardosSalida.add(leerRetardoMs(metadatos));

                rectAnterior = rect;
                disposalAnterior = disposalActual;
            }
            lector.dispose();
        } catch (IOException ex) {
            return new ArrayList<>();
        }
        return resultado;
    }

    private static String leerDisposal(IIOMetadata metadatos) {
        if (metadatos == null) {
            return null;
        }
        Element raiz = (Element) metadatos.getAsTree("javax_imageio_gif_image_1.0");
        NodeList controles = raiz.getElementsByTagName("GraphicControlExtension");
        return controles.getLength() > 0 ? ((Element) controles.item(0)).getAttribute("disposalMethod") : null;
    }

    private static BufferedImage copiar(BufferedImage origen) {
        BufferedImage copia = new BufferedImage(origen.getWidth(), origen.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = copia.createGraphics();
        g2.drawImage(origen, 0, 0, null);
        g2.dispose();
        return copia;
    }

    private static int[] leerPosicion(IIOMetadata metadatos) {
        if (metadatos == null) {
            return new int[]{0, 0};
        }
        Element raiz = (Element) metadatos.getAsTree("javax_imageio_gif_image_1.0");
        NodeList descriptores = raiz.getElementsByTagName("ImageDescriptor");
        if (descriptores.getLength() == 0) {
            return new int[]{0, 0};
        }
        Element descriptor = (Element) descriptores.item(0);
        int x = Integer.parseInt(descriptor.getAttribute("imageLeftPosition"));
        int y = Integer.parseInt(descriptor.getAttribute("imageTopPosition"));
        return new int[]{x, y};
    }

    /**
     * GIFs con retardo 0/1 centésimas piden "lo más rápido posible"; se
     * limita a un mínimo razonable. Además, se aplica
     * {@link #FACTOR_RALENTIZACION} sobre el retardo del archivo.
     *
     * <p><b>Hoy ese factor es 1.0, y eso es lo correcto.</b> Existió porque
     * el asset anterior venía codificado demasiado rápido para verse a 48px
     * y hubo que frenarlo desde el código (llegó a 3×). El asset actual se
     * generó con los tiempos ya pensados para ese tamaño —2,4 s por vuelta—,
     * así que multiplicarlo volvería a desajustarlo. El mecanismo se
     * conserva por si algún día entra otro archivo mal temporizado: se
     * escala el conjunto completo, no cuadro por cuadro, para no alterar el
     * ritmo relativo entre cuadros.</p>
     */
    private static int leerRetardoMs(IIOMetadata metadatos) {
        if (metadatos == null) {
            return RETARDO_POR_DEFECTO_MS;
        }
        Element raiz = (Element) metadatos.getAsTree("javax_imageio_gif_image_1.0");
        NodeList controles = raiz.getElementsByTagName("GraphicControlExtension");
        if (controles.getLength() == 0) {
            return RETARDO_POR_DEFECTO_MS;
        }
        Element control = (Element) controles.item(0);
        int centesimas = Integer.parseInt(control.getAttribute("delayTime"));
        int retardoMs = centesimas <= 1 ? RETARDO_POR_DEFECTO_MS : centesimas * 10;
        return (int) Math.round(retardoMs * FACTOR_RALENTIZACION);
    }
}
