package view.factory.effects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import javax.swing.JPanel;
import javax.swing.Timer;
import view.factory.IComponentesFactory;
import view.factory.icons.IconoOjo;

/**
 * Indicador circular de carga, en dos modos según cuánto se va a esperar.
 *
 * <p><b>Por qué dos modos.</b> La guía de UX de espera es clara: para esperas
 * cortas (1–3 s) basta un giro continuo, porque un porcentaje que sube y baja
 * en un segundo solo aporta ruido; para esperas medias (3–10 s) conviene un
 * indicador <em>determinado</em>, que muestre cuánto falta, porque reduce la
 * sensación de espera y el abandono. De ahí
 * {@link IComponentesFactory#crearIndicadorGiratorio(int)} (splash breve y
 * transiciones) y {@link IComponentesFactory#crearIndicadorProgreso(int)}
 * (arranque de la aplicación, que sí informa avance).</p>
 *
 * <p><b>Estabilidad:</b> extiende {@link JPanel} y llama a
 * {@code super.paintComponent(g)} antes de dibujar, así que el fondo lo pinta
 * Swing como en cualquier componente opaco — aquí no se pinta ningún fondo a
 * mano. El {@code Graphics2D} solo traza el arco sobre la propia superficie
 * del componente, que no tiene hijos: la misma excepción autocontenida de
 * {@link IconoOjo} (ver "Estabilidad de la interfaz" en {@code CLAUDE.md}).</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public class IndicadorCarga extends JPanel {

    private static final int MS_POR_CUADRO = 16;
    private static final double GRADOS_POR_CUADRO = 4.2;
    private static final int BARRIDO_GIRATORIO = 95;
    private static final float GROSOR = 4f;

    private final Color colorArco;
    private final Color colorPista;
    private final boolean determinado;

    private final Timer temporizador;
    private double angulo;
    private double progreso;

    /**
     * @param colorArco   color del arco que avanza o gira
     * @param colorPista  color del anillo de fondo sobre el que corre el arco
     * @param colorFondo  color de la superficie donde se inserta el indicador
     * @param tamano      lado en píxeles (se dibuja cuadrado)
     * @param determinado {@code true} para mostrar avance real con
     *                    {@link #setProgreso(double)}; {@code false} para un
     *                    giro continuo sin porcentaje
     */
    public IndicadorCarga(Color colorArco, Color colorPista, Color colorFondo, int tamano, boolean determinado) {
        this.colorArco = colorArco;
        this.colorPista = colorPista;
        this.determinado = determinado;

        setBackground(colorFondo);
        setOpaque(true);
        setPreferredSize(new Dimension(tamano, tamano));
        setMaximumSize(new Dimension(tamano, tamano));

        temporizador = new Timer(MS_POR_CUADRO, e -> {
            angulo = (angulo + GRADOS_POR_CUADRO) % 360;
            repaint();
        });
    }

    /** Arranca la animación. */
    public void iniciar() {
        temporizador.start();
    }

    /** Detiene la animación; imprescindible antes de descartar la ventana que lo contiene. */
    public void detener() {
        temporizador.stop();
    }

    /**
     * Fija el avance en modo determinado.
     *
     * @param valor avance entre 0 y 1; fuera de ese rango se recorta
     */
    public void setProgreso(double valor) {
        progreso = Math.max(0, Math.min(1, valor));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(GROSOR, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        double margen = GROSOR / 2.0 + 1;
        double lado = Math.min(getWidth(), getHeight()) - margen * 2;
        double x = (getWidth() - lado) / 2.0;
        double y = (getHeight() - lado) / 2.0;

        g2.setColor(colorPista);
        g2.draw(new Arc2D.Double(x, y, lado, lado, 0, 360, Arc2D.OPEN));

        // Ambos modos arrancan arriba (90°) y avanzan en sentido horario.
        double inicio = determinado ? 90 : 90 - angulo;
        double barrido = determinado ? -progreso * 360 : -BARRIDO_GIRATORIO;
        g2.setColor(colorArco);
        g2.draw(new Arc2D.Double(x, y, lado, lado, inicio, barrido, Arc2D.OPEN));

        g2.dispose();
    }
}
