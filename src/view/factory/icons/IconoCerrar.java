package view.factory.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Ícono de cerrar (una "X"), dibujado por código.
 *
 * <p><b>Por qué no es el carácter {@code ✕}.</b> Los símbolos fuera del
 * alfabeto básico salen como cajas vacías con la tipografía del tema (ya se
 * comprobó en pantalla con los mensajes de validación). Dos trazos sobre la
 * rejilla de 16x16 —mismo principio que {@link IconoCarrito}— se ven igual en
 * cualquier equipo y a cualquier escala.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class IconoCerrar implements Icon {

    private final Color color;
    private final int tamano;

    /**
     * @param color  color del trazo
     * @param tamano lado del ícono en píxeles
     */
    public IconoCerrar(Color color, int tamano) {
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
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2.drawLine(3, 3, 13, 13);
        g2.drawLine(13, 3, 3, 13);

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
