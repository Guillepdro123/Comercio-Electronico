package view.factory.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;

/**
 * "G" de Google en sus cuatro colores, para el botón de acceso con Google.
 *
 * <p>Se dibuja por código, igual que {@link IconoOjo}, en vez de cargar una
 * imagen: es un ícono autocontenido que no pinta fondos ni toca la opacidad
 * de nadie, así que no cae en el patrón prohibido del proyecto, y escala sin
 * perder nitidez en pantallas con escalado de Windows.</p>
 *
 * <p>La letra es un anillo abierto por la derecha, repartido en cuatro arcos
 * (rojo arriba, amarillo a la izquierda, verde abajo y azul a la derecha) más
 * la barra horizontal azul. Los ángulos siguen la convención de
 * {@link Arc2D}: 0° a las tres en punto y sentido antihorario.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class IconoGoogle implements Icon {

    private static final Color ROJO = new Color(0xEA4335);
    private static final Color AMARILLO = new Color(0xFBBC05);
    private static final Color VERDE = new Color(0x34A853);
    private static final Color AZUL = new Color(0x4285F4);

    private final int lado;

    /** @param lado tamaño del ícono en píxeles lógicos */
    public IconoGoogle(int lado) {
        this.lado = lado;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.translate(x, y);

        double grosor = lado * 0.19;
        // El anillo se inscribe dejando medio grosor de margen: el trazo se
        // dibuja centrado sobre la circunferencia.
        double radio = (lado - grosor) / 2.0;
        double centro = lado / 2.0;
        g2.setStroke(new BasicStroke((float) grosor, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        arco(g2, centro, radio, 45, 105, ROJO);
        arco(g2, centro, radio, 150, 70, AMARILLO);
        arco(g2, centro, radio, 220, 100, VERDE);
        arco(g2, centro, radio, 320, 40, AZUL);

        // Barra de la G: del centro al borde exterior derecho, a la altura
        // del centro, con el mismo grosor que el anillo.
        g2.setColor(AZUL);
        g2.fill(new Rectangle2D.Double(centro, centro - grosor / 2.0,
                radio + grosor / 2.0, grosor));
        g2.dispose();
    }

    private void arco(Graphics2D g2, double centro, double radio, double inicio,
                      double extension, Color color) {
        g2.setColor(color);
        g2.draw(new Arc2D.Double(centro - radio, centro - radio, radio * 2, radio * 2,
                inicio, extension, Arc2D.OPEN));
    }

    @Override
    public int getIconWidth() {
        return lado;
    }

    @Override
    public int getIconHeight() {
        return lado;
    }
}
