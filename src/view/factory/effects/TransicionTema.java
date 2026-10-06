package view.factory.effects;

import java.awt.AlphaComposite;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.RootPaneContainer;
import javax.swing.Timer;

/**
 * Fundido del cambio de tema: una foto de cada ventana abierta, tomada
 * <em>antes</em> del cambio, que se desvanece encima mientras debajo la
 * ventana ya está pintada con el tema nuevo.
 *
 * <p><b>Por qué propio y no {@code FlatAnimatedLafChange}.</b> Esa clase vive
 * en {@code flatlaf-extras}, una dependencia aparte; el proyecto tiene solo el
 * núcleo de FlatLaf y la regla de no añadir librerías sin que se pidan. Esto
 * hace lo mismo con piezas estándar: la foto se pinta en un componente sin
 * hijos colocado en la capa más alta del {@code JLayeredPane} de la ventana,
 * el mismo recurso que usan el contador del carrito y el botón flotante. No
 * hay {@code setOpaque(false)} en un contenedor con hijos: el componente de la
 * foto es una hoja y desaparece al terminar.</p>
 *
 * <p>Sin esto, el cambio se ve igualmente sin cerrar la ventana, pero de
 * golpe: el fundido solo suaviza el instante del cambio de colores.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class TransicionTema {

    /**
     * Duración del fundido. Se mide en tiempo real y no en pasos: cada paso
     * repinta la ventana entera, que maximizada tarda bastante más que los
     * pocos milisegundos de un paso, y con pasos fijos el fundido se alargaba
     * más de un segundo (medido en captura).
     */
    private static final long MS_FUNDIDO = 300;
    private static final int MS_PASO = 15;
    /** Por encima de todo, incluidos los menús y el arrastre. */
    private static final Integer CAPA_FOTO = JLayeredPane.DRAG_LAYER + 1;

    private final List<Foto> fotos = new ArrayList<>();

    private TransicionTema() {
    }

    /**
     * Fotografía las ventanas visibles y deja la foto encima de cada una.
     *
     * @return la transición, lista para {@link #desvanecer()} tras el cambio
     */
    public static TransicionTema capturar() {
        TransicionTema transicion = new TransicionTema();
        for (Window ventana : Window.getWindows()) {
            if (ventana.isShowing() && ventana instanceof RootPaneContainer contenedor) {
                transicion.fotografiar(contenedor.getRootPane().getLayeredPane());
            }
        }
        return transicion;
    }

    private void fotografiar(JLayeredPane capas) {
        int ancho = capas.getWidth();
        int alto = capas.getHeight();
        if (ancho <= 0 || alto <= 0) {
            return;
        }
        // A resolución física: con escalado de Windows (125%), una foto del
        // tamaño lógico se vería borrosa durante el fundido.
        GraphicsConfiguration configuracion = capas.getGraphicsConfiguration();
        double escala = configuracion == null ? 1 : configuracion.getDefaultTransform().getScaleX();
        BufferedImage imagen = new BufferedImage((int) Math.ceil(ancho * escala),
                (int) Math.ceil(alto * escala), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imagen.createGraphics();
        g2.scale(escala, escala);
        capas.paint(g2);
        g2.dispose();

        Foto foto = new Foto(imagen);
        foto.setBounds(0, 0, ancho, alto);
        capas.add(foto, CAPA_FOTO);
        fotos.add(foto);
    }

    /**
     * Desvanece las fotos y las retira; llamarlo cuando las ventanas ya
     * tienen el tema nuevo.
     */
    public void desvanecer() {
        if (fotos.isEmpty()) {
            return;
        }
        long inicio = System.nanoTime();
        Timer temporizador = new Timer(MS_PASO, null);
        temporizador.setCoalesce(true);
        temporizador.addActionListener(e -> {
            long transcurrido = (System.nanoTime() - inicio) / 1_000_000;
            float opacidad = Math.max(0f, 1f - transcurrido / (float) MS_FUNDIDO);
            for (Foto foto : fotos) {
                foto.opacidad = opacidad;
                foto.repaint();
            }
            if (opacidad <= 0f) {
                temporizador.stop();
                for (Foto foto : fotos) {
                    Container padre = foto.getParent();
                    if (padre != null) {
                        padre.remove(foto);
                        padre.repaint();
                    }
                }
                fotos.clear();
            }
        });
        temporizador.start();
    }

    /** La foto de una ventana, pintada con la opacidad del momento. */
    private static final class Foto extends JComponent {

        private final BufferedImage imagen;
        private float opacidad = 1f;

        Foto(BufferedImage imagen) {
            this.imagen = imagen;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
            g2.drawImage(imagen, 0, 0, getWidth(), getHeight(), null);
            g2.dispose();
        }
    }
}
