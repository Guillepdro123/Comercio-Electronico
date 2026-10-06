package view.factory.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.function.BooleanSupplier;
import javax.swing.Icon;

/**
 * Ícono del conmutador de tema: una luna (para pasar al modo oscuro) o un sol
 * (para volver al claro), dibujado por código sobre la rejilla de 16x16.
 *
 * <p>Muestra el tema al que se <em>va</em>, no el actual: es la convención de
 * los conmutadores de este tipo, y el botón se lee como "pulsa para tener
 * esto".</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class IconoTema implements Icon {

    /** Se pregunta en cada pintado: el ícono cambia solo al alternar el tema. */
    private final BooleanSupplier luna;
    private final Color color;
    private final int tamano;

    /**
     * @param luna   devuelve {@code true} para la luna (ir al modo oscuro) y
     *               {@code false} para el sol (ir al modo claro)
     * @param color  color del trazo
     * @param tamano lado en píxeles
     */
    public IconoTema(BooleanSupplier luna, Color color, int tamano) {
        this.luna = luna;
        this.color = color;
        this.tamano = tamano;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.translate(x, y);
        g2.scale(tamano / 16.0, tamano / 16.0);
        if (luna.getAsBoolean()) {
            // Media luna: un círculo al que se le resta otro desplazado.
            Area forma = new Area(new Ellipse2D.Double(2, 2, 12, 12));
            forma.subtract(new Area(new Ellipse2D.Double(6, 0, 11, 11)));
            g2.fill(forma);
        } else {
            g2.fill(new Ellipse2D.Double(5, 5, 6, 6));
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int rayo = 0; rayo < 8; rayo++) {
                double angulo = rayo * Math.PI / 4;
                g2.drawLine((int) Math.round(8 + 5 * Math.cos(angulo)),
                        (int) Math.round(8 + 5 * Math.sin(angulo)),
                        (int) Math.round(8 + 7 * Math.cos(angulo)),
                        (int) Math.round(8 + 7 * Math.sin(angulo)));
            }
        }
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return tamano;
    }

    @Override
    public int getIconHeight() {
        return tamano;
    }
}
