package view.factory.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.Icon;
import view.factory.components.CampoPasswordConToggle;

/**
 * Icono de "ojo" dibujado con {@link Graphics2D}, usado por
 * {@link CampoPasswordConToggle} para alternar la visibilidad de la
 * contraseña.
 *
 * <p>Se dibuja por código en lugar de usar un archivo de imagen o un emoji
 * para que se vea igual en cualquier sistema operativo y no dependa de que la
 * fuente instalada incluya el símbolo.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public class IconoOjo implements Icon {

    private static final int LADO = 20;

    private final Color color;
    /** Si es {@code true} dibuja el ojo tachado (contraseña visible). */
    private final boolean tachado;

    public IconoOjo(Color color, boolean tachado) {
        this.color = color;
        this.tachado = tachado;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Contorno del ojo: dos curvas simétricas.
        Path2D contorno = new Path2D.Double();
        contorno.moveTo(1.5, 10);
        contorno.quadTo(10, 2.5, 18.5, 10);
        contorno.quadTo(10, 17.5, 1.5, 10);
        contorno.closePath();
        g2.draw(contorno);

        // Pupila.
        g2.fillOval(7, 7, 6, 6);

        // Línea diagonal cuando la contraseña está visible.
        if (tachado) {
            g2.drawLine(3, 17, 17, 3);
        }
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return LADO;
    }

    @Override
    public int getIconHeight() {
        return LADO;
    }
}
