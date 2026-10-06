package view.factory.promo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import view.factory.IComponentesFactory;

/**
 * Franja superior del pop-up promocional: degradado de color con un título y
 * un subtítulo encima.
 *
 * <p><b>No tiene hijos, y por eso puede dibujarse entera.</b> La regla del
 * proyecto prohíbe pintar a mano el fondo de un contenedor que tiene hijos (ese
 * fue el origen del texto invisible de una versión anterior). Esta franja es
 * una sola pieza gráfica, como una imagen: llama a
 * {@code super.paintComponent(g)} y encima traza su degradado y sus dos líneas
 * de texto. El botón de cerrar no es hijo suyo: va en una capa superior de un
 * {@code JLayeredPane}, que es como el proyecto superpone componentes.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class CabeceraDegradada extends JPanel {

    private final IComponentesFactory fabrica;
    private final Color desde;
    private final Color hasta;
    private final String titulo;
    private final String subtitulo;

    /**
     * @param fabrica   fábrica de la que salen tipografía y color del texto
     * @param desde     color del extremo izquierdo
     * @param hasta     color del extremo derecho
     * @param titulo    frase principal
     * @param subtitulo línea de apoyo; puede ir vacía
     * @param ancho     ancho de la franja en píxeles
     * @param alto      alto de la franja en píxeles
     */
    public CabeceraDegradada(IComponentesFactory fabrica, Color desde, Color hasta,
                             String titulo, String subtitulo, int ancho, int alto) {
        this.fabrica = fabrica;
        this.desde = desde;
        this.hasta = hasta;
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        setBackground(desde);
        setOpaque(true);
        setPreferredSize(new Dimension(ancho, alto));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, desde, getWidth(), getHeight(), hasta));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(fabrica.colorTextoSobreAcento());
        g2.setFont(fabrica.fuente(Font.BOLD, 22));
        g2.drawString(titulo, 24, 44);
        if (subtitulo != null && !subtitulo.isBlank()) {
            g2.setFont(fabrica.fuente(Font.PLAIN, 13));
            g2.drawString(subtitulo, 24, 68);
        }
        g2.dispose();
    }
}
