package view.factory.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.Border;
import view.factory.icons.IconoCampo;
import view.factory.icons.IconoOjo;

/**
 * {@link Border} que da apariencia de esquinas redondeadas a un componente
 * Swing opaco y normal, sin tocar cómo se pinta ese componente.
 *
 * <p><b>Por qué este enfoque y no {@code paintComponent} propio:</b> la forma
 * habitual de redondear en Swing es poner {@code setOpaque(false)} y pintar el
 * fondo a mano con {@code Graphics2D}. Ese camino ya se intentó en este
 * proyecto y produjo conflictos de color y texto invisible (ver "Estabilidad
 * de la interfaz" en {@code CLAUDE.md}), porque al dejar de pintar fondos en
 * cascada los hijos quedaban sin respaldo de color.</p>
 *
 * <p>Este borde invierte el truco y evita ese riesgo: el componente sigue
 * siendo <b>opaco</b> y pinta su fondo rectangular como siempre; el borde solo
 * <em>tapa las cuatro esquinas</em> con el color del contenedor que está
 * detrás y luego traza el contorno redondeado encima. El resultado se ve
 * redondeado sin que ningún componente pierda su fondo, sin
 * {@code setOpaque(false)} en cascada, sin {@code ComponentUI} propio y sin
 * tocar el texto (las esquinas quedan fuera del área de contenido gracias a
 * los {@code Insets}).</p>
 *
 * <p>Es autocontenido igual que {@link IconoOjo} o {@link IconoCampo}: dibuja
 * solo su propia superficie y no conoce ni altera a ningún otro componente.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public class BordeRedondeado implements Border {

    private final Color colorBorde;
    private final Color colorExterior;
    private final int radio;
    private final int grosor;
    private final Insets relleno;

    /**
     * @param colorBorde    color del contorno redondeado
     * @param colorExterior color del contenedor que hay detrás del componente;
     *                      es el que se usa para tapar las esquinas, así que
     *                      debe coincidir con el fondo real del padre o se
     *                      verán "parches" de otro color
     * @param radio         radio de las esquinas en píxeles
     * @param grosor        grosor del contorno en píxeles
     * @param relleno       espacio interno que el borde reserva para el contenido
     */
    public BordeRedondeado(Color colorBorde, Color colorExterior, int radio, int grosor, Insets relleno) {
        this.colorBorde = colorBorde;
        this.colorExterior = colorExterior;
        this.radio = radio;
        this.grosor = grosor;
        this.relleno = relleno;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int ancho, int alto) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RoundRectangle2D forma = new RoundRectangle2D.Float(
                x + grosor / 2f, y + grosor / 2f, ancho - grosor, alto - grosor, radio, radio);

        // Las esquinas del rectángulo que el componente ya pintó se tapan con
        // el color del contenedor: es lo que produce la ilusión de redondeo.
        Area esquinas = new Area(new Rectangle(x, y, ancho, alto));
        esquinas.subtract(new Area(forma));
        g2.setColor(colorExterior);
        g2.fill(esquinas);

        g2.setColor(colorBorde);
        g2.setStroke(new BasicStroke(grosor));
        g2.draw(forma);

        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(relleno.top, relleno.left, relleno.bottom, relleno.right);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}
