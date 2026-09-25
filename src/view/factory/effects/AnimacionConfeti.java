package view.factory.effects;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.util.Random;
import javax.swing.JComponent;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Animación de celebración: una ráfaga de confeti que sube desde la base de la
 * ventana y cae girando, como festejo de un registro exitoso.
 *
 * <p><b>Dónde se dibuja:</b> sobre el {@code glassPane} de la ventana, la capa
 * que Swing reserva justo para superponer cosas encima de toda la interfaz.
 * Así la animación no toca ni un solo componente del formulario: no cambia
 * layouts, no reordena nada y al terminar se retira sin dejar rastro.</p>
 *
 * <p><b>Por qué esto no reintroduce el patrón prohibido:</b> el
 * {@code Graphics2D} de esta clase pinta únicamente partículas sobre una capa
 * propia y vacía (sin hijos, sin fondo). No pinta el fondo de ningún
 * contenedor con hijos ni pone {@code setOpaque(false)} en cascada, que es lo
 * que en su momento dejó texto invisible (ver "Estabilidad de la interfaz" en
 * {@code CLAUDE.md}). Si algo fallara aquí, el peor caso es que no se vea el
 * confeti: el formulario de abajo nunca cambia.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public final class AnimacionConfeti {

    private static final int CANTIDAD_PARTICULAS = 90;
    private static final int MS_POR_CUADRO = 16;
    private static final int DURACION_MS = 2600;
    private static final double GRAVEDAD = 0.16;

    private AnimacionConfeti() {
    }

    /**
     * Lanza el confeti sobre la ventana que contiene a {@code origen}.
     *
     * @param origen  componente usado para localizar la ventana a decorar
     * @param colores paleta de la que se toma el color de cada partícula
     */
    public static void lanzar(Component origen, Color... colores) {
        Window ventana = SwingUtilities.getWindowAncestor(origen);
        if (!(ventana instanceof RootPaneContainer contenedor)) {
            return;
        }
        Component glassOriginal = contenedor.getGlassPane();
        CapaConfeti capa = new CapaConfeti(colores);
        contenedor.setGlassPane(capa);
        capa.setVisible(true);
        // Al terminar se devuelve el glassPane original: la ventana queda
        // exactamente como estaba antes de la celebración.
        capa.animar(() -> {
            capa.setVisible(false);
            contenedor.setGlassPane(glassOriginal);
        });
    }

    /** Capa transparente que dibuja y mueve las partículas. */
    private static class CapaConfeti extends JComponent {

        private final Particula[] particulas = new Particula[CANTIDAD_PARTICULAS];
        private final Random azar = new Random();
        private boolean iniciado = false;

        CapaConfeti(Color[] colores) {
            // Sin fondo propio: solo superpone partículas sobre la interfaz.
            setOpaque(false);
            for (int i = 0; i < particulas.length; i++) {
                particulas[i] = new Particula(colores[azar.nextInt(colores.length)]);
            }
        }

        void animar(Runnable alTerminar) {
            long inicio = System.currentTimeMillis();
            Timer temporizador = new Timer(MS_POR_CUADRO, null);
            temporizador.addActionListener(e -> {
                if (!iniciado) {
                    iniciado = true;
                    sembrar();
                }
                for (Particula p : particulas) {
                    p.avanzar();
                }
                repaint();
                if (System.currentTimeMillis() - inicio > DURACION_MS) {
                    ((Timer) e.getSource()).stop();
                    alTerminar.run();
                }
            });
            temporizador.start();
        }

        /** Coloca las partículas recién cuando la capa ya tiene tamaño real. */
        private void sembrar() {
            int ancho = Math.max(getWidth(), 1);
            int alto = Math.max(getHeight(), 1);
            for (Particula p : particulas) {
                p.x = ancho * (0.15 + azar.nextDouble() * 0.7);
                p.y = alto + azar.nextDouble() * 40;
                p.velocidadX = (azar.nextDouble() - 0.5) * 5.5;
                p.velocidadY = -(8.5 + azar.nextDouble() * 7.5);
                p.angulo = azar.nextDouble() * Math.PI;
                p.giro = (azar.nextDouble() - 0.5) * 0.38;
                p.lado = 5 + azar.nextInt(6);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (Particula p : particulas) {
                p.dibujar(g2);
            }
            g2.dispose();
        }
    }

    /** Un trozo de papel: posición, velocidad, giro y color propios. */
    private static class Particula {

        private final Color color;
        private double x;
        private double y;
        private double velocidadX;
        private double velocidadY;
        private double angulo;
        private double giro;
        private int lado = 6;

        Particula(Color color) {
            this.color = color;
        }

        void avanzar() {
            x += velocidadX;
            y += velocidadY;
            velocidadY += GRAVEDAD;
            velocidadX *= 0.995; // rozamiento del aire: la caída se va enderezando
            angulo += giro;
        }

        void dibujar(Graphics2D g2) {
            AffineTransform original = g2.getTransform();
            g2.translate(x, y);
            g2.rotate(angulo);
            g2.setColor(color);
            // Rectángulo aplastado: al girar parece una serpentina de papel.
            g2.fillRect(-lado / 2, -lado / 4, lado, Math.max(2, lado / 2));
            g2.setTransform(original);
        }
    }
}
