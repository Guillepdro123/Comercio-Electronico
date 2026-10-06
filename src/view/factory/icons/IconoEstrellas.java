package view.factory.icons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Fila de cinco estrellas rellenas según una calificación, dibujada por código.
 *
 * <p><b>Por qué no el carácter de estrella.</b> Los símbolos fuera del alfabeto
 * básico salen como cajas vacías con la tipografía del tema (ya comprobado en
 * pantalla). Además, una calificación de 4,5 necesita media estrella, y eso un
 * carácter no lo puede expresar: aquí la última estrella se rellena solo en la
 * fracción que corresponde, recortando el relleno.</p>
 *
 * <p>Con {@code estrellas = 1} sirve también como estrella suelta (los botones
 * del selector de calificación).</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class IconoEstrellas implements Icon {

    private static final int SEPARACION = 2;

    private final double calificacion;
    private final int estrellas;
    private final int tamano;
    private final Color relleno;
    private final Color vacio;

    /**
     * @param calificacion cuántas estrellas van rellenas (admite fracciones)
     * @param estrellas    cuántas estrellas se dibujan (5 en una calificación)
     * @param tamano       lado de cada estrella en píxeles
     * @param relleno      color de la parte rellena
     * @param vacio        color de la parte vacía
     */
    public IconoEstrellas(double calificacion, int estrellas, int tamano,
                          Color relleno, Color vacio) {
        this.calificacion = calificacion;
        this.estrellas = estrellas;
        this.tamano = tamano;
        this.relleno = relleno;
        this.vacio = vacio;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < estrellas; i++) {
            int origen = x + i * (tamano + SEPARACION);
            Polygon estrella = estrella(origen, y);
            g2.setColor(vacio);
            g2.fill(estrella);
            double fraccion = Math.max(0, Math.min(1, calificacion - i));
            if (fraccion > 0) {
                Graphics2D parcial = (Graphics2D) g2.create();
                parcial.clipRect(origen, y, (int) Math.round(tamano * fraccion), tamano);
                parcial.setColor(relleno);
                parcial.fill(estrella);
                parcial.dispose();
            }
        }
        g2.dispose();
    }

    /** Estrella de cinco puntas inscrita en el cuadrado de la posición dada. */
    private Polygon estrella(int x, int y) {
        Polygon poligono = new Polygon();
        double centroX = x + tamano / 2.0;
        double centroY = y + tamano / 2.0 + tamano * 0.04;
        double exterior = tamano / 2.0;
        double interior = exterior * 0.45;
        for (int punta = 0; punta < 10; punta++) {
            double radio = punta % 2 == 0 ? exterior : interior;
            double angulo = -Math.PI / 2 + punta * Math.PI / 5;
            poligono.addPoint((int) Math.round(centroX + radio * Math.cos(angulo)),
                    (int) Math.round(centroY + radio * Math.sin(angulo)));
        }
        return poligono;
    }

    @Override
    public int getIconWidth() {
        return estrellas * tamano + (estrellas - 1) * SEPARACION;
    }

    @Override
    public int getIconHeight() {
        return tamano;
    }
}
