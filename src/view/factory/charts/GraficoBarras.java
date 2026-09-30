package view.factory.charts;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import view.factory.IComponentesFactory;

/**
 * Gráfico de barras horizontales con la paleta del tema.
 *
 * <p><b>Por qué barras y por qué horizontales.</b> De todos los rasgos que el
 * ojo procesa sin esfuerzo, la longitud y la posición son con los que mejor se
 * estima <em>cuánto</em> mayor es una cosa que otra; por eso una barra compara
 * mejor que un color o un área. Se trazan horizontales porque las etiquetas
 * son nombres de productos: en vertical habría que girarlas o recortarlas.</p>
 *
 * <p><b>Esto no es el patrón de pintado prohibido en el proyecto.</b> La clase
 * llama primero a {@code super.paintComponent(g)} —el fondo lo pinta Swing,
 * como en cualquier {@link JPanel} opaco— y solo después traza las barras, que
 * son el dato en sí y no existen como componente. Es exactamente lo que ya
 * hace {@code IndicadorCarga}. No hay {@code setOpaque(false)} en cascada, ni
 * un {@code ComponentUI} propio, ni se dibuja el fondo de ningún contenedor.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class GraficoBarras extends JPanel {

    private static final int ALTO_BARRA = 20;
    private static final int SEPARACION = 14;
    private static final int ANCHO_ETIQUETA = 124;
    private static final int ANCHO_VALOR = 78;
    private static final int MARGEN = 14;
    private static final int RADIO = 6;
    /** Longitud mínima visible: una barra de valor casi cero debe verse igual. */
    private static final int LARGO_MINIMO = 6;

    private final IComponentesFactory fabrica;
    private final List<Barra> barras = new ArrayList<>();
    private String mensajeVacio = "Todavía no hay datos que mostrar.";

    /**
     * @param fabrica fábrica de la que salen colores y tipografía
     */
    public GraficoBarras(IComponentesFactory fabrica) {
        this.fabrica = fabrica;
        setBackground(fabrica.colorPanel());
        setOpaque(true);
    }

    /**
     * Reemplaza los datos del gráfico.
     *
     * @param nuevas       barras a dibujar, en el orden en que deben aparecer
     * @param mensajeVacio qué decir cuando la lista viene vacía, en lugar de
     *                     dejar un recuadro en blanco sin explicación
     */
    public void mostrar(List<Barra> nuevas, String mensajeVacio) {
        barras.clear();
        barras.addAll(nuevas);
        this.mensajeVacio = mensajeVacio;
        setPreferredSize(new Dimension(0, MARGEN * 2
                + Math.max(1, barras.size()) * (ALTO_BARRA + SEPARACION)));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // El fondo lo pinta Swing; aquí solo se traza el dato.
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (barras.isEmpty()) {
            dibujarMensajeVacio(g2);
            g2.dispose();
            return;
        }

        double maximo = barras.stream().mapToDouble(Barra::valor).max().orElse(1);
        if (maximo <= 0) {
            maximo = 1;
        }
        int largoDisponible = getWidth() - ANCHO_ETIQUETA - ANCHO_VALOR - MARGEN * 2;
        int y = MARGEN;

        for (Barra barra : barras) {
            g2.setFont(fabrica.fuente(java.awt.Font.PLAIN, 12));
            g2.setColor(fabrica.colorTextoSuave());
            g2.drawString(recortar(g2.getFontMetrics(), barra.etiqueta(), ANCHO_ETIQUETA - 10),
                    MARGEN, y + ALTO_BARRA - 5);

            int largo = Math.max(LARGO_MINIMO,
                    (int) Math.round(barra.valor() / maximo * largoDisponible));
            int x = MARGEN + ANCHO_ETIQUETA;

            // Canal de fondo: deja ver de un vistazo cuánto falta para el
            // máximo, que es lo que convierte la barra en una comparación.
            g2.setColor(fabrica.colorCampo());
            g2.fillRoundRect(x, y, largoDisponible, ALTO_BARRA, RADIO, RADIO);

            g2.setColor(fabrica.colorAcento());
            g2.fillRoundRect(x, y, largo, ALTO_BARRA, RADIO, RADIO);

            g2.setFont(fabrica.fuente(java.awt.Font.BOLD, 12));
            g2.setColor(fabrica.colorTexto());
            g2.drawString(barra.texto(), x + largoDisponible + 10, y + ALTO_BARRA - 5);

            y += ALTO_BARRA + SEPARACION;
        }
        g2.dispose();
    }

    private void dibujarMensajeVacio(Graphics2D g2) {
        g2.setFont(fabrica.fuente(java.awt.Font.PLAIN, 13));
        g2.setColor(fabrica.colorTextoSuave());
        FontMetrics metricas = g2.getFontMetrics();
        int x = Math.max(MARGEN, (getWidth() - metricas.stringWidth(mensajeVacio)) / 2);
        g2.drawString(mensajeVacio, x, getHeight() / 2);
    }

    /** Corta una etiqueta larga con puntos suspensivos para que no invada la barra. */
    private String recortar(FontMetrics metricas, String texto, int anchoMaximo) {
        if (metricas.stringWidth(texto) <= anchoMaximo) {
            return texto;
        }
        String recortado = texto;
        while (recortado.length() > 1
                && metricas.stringWidth(recortado + "...") > anchoMaximo) {
            recortado = recortado.substring(0, recortado.length() - 1);
        }
        return recortado + "...";
    }
}
