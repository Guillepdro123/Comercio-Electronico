package view.factory.icons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Punto relleno de un color, usado como semáforo junto al campo de
 * contraseña: rojo si es insegura, ámbar si es aceptable y verde si es
 * fuerte.
 *
 * <p><b>Por qué un ícono dibujado y no el carácter "●".</b> En este proyecto
 * ya se comprobó que los símbolos fuera del alfabeto básico se renderizan
 * como cajas vacías con la tipografía del tema. Un círculo trazado con
 * {@code Graphics2D} se ve siempre igual, en cualquier fuente y a cualquier
 * tamaño.</p>
 *
 * <p>Autocontenido, como {@link IconoOjo} o {@link IconoCampo}: solo pinta su
 * propia superficie (ver "Estabilidad de la interfaz" en {@code CLAUDE.md}).</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public class IconoPunto implements Icon {

    private final Color color;
    private final int diametro;

    /**
     * @param color    relleno del punto
     * @param diametro tamaño en píxeles
     */
    public IconoPunto(Color color, int diametro) {
        this.color = color;
        this.diametro = diametro;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(x, y, diametro, diametro);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return diametro;
    }

    @Override
    public int getIconHeight() {
        return diametro;
    }
}
