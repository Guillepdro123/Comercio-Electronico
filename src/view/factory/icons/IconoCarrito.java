package view.factory.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Ícono de carrito de compras, dibujado por código.
 *
 * <p>Mismo principio que {@link IconoCampo} y {@link IconoOjo}: es un glifo
 * autocontenido trazado sobre una rejilla de 16x16 y escalado al tamaño
 * pedido, así que no depende de ningún archivo ni de la resolución de la
 * pantalla. Dibujar <em>íconos</em> con {@code Graphics2D} no es el patrón
 * prohibido del proyecto: este ícono no pinta el fondo de ningún contenedor
 * ni toca la opacidad de nadie.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class IconoCarrito implements Icon {

    private final Color color;
    private final int tamano;

    /**
     * @param color  color del trazo
     * @param tamano lado del ícono en píxeles
     */
    public IconoCarrito(Color color, int tamano) {
        this.color = color;
        this.tamano = tamano;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.translate(x, y);
        g2.scale(tamano / 16.0, tamano / 16.0);

        // Asa, cesta y las dos ruedas.
        g2.drawLine(1, 2, 3, 2);
        g2.drawLine(3, 2, 5, 10);
        g2.drawLine(5, 10, 13, 10);
        g2.drawLine(4, 4, 15, 4);
        g2.drawLine(15, 4, 13, 10);
        g2.drawOval(5, 12, 2, 2);
        g2.drawOval(11, 12, 2, 2);

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
