package view.factory.promo;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntConsumer;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import view.factory.IComponentesFactory;

/**
 * Banner superior que rota solo entre varias promociones, al estilo de las
 * portadas de las tiendas en línea.
 *
 * <p><b>Por qué un {@link JLayeredPane}.</b> Las diapositivas se dibujan (son
 * una imagen compuesta: degradado, textos y foto), pero las flechas y el botón
 * "Ver oferta" se pulsan, y lo que se pulsa tiene que ser un botón (regla del
 * proyecto). La capa base es un lienzo <em>sin hijos</em>; los botones van en
 * la capa de encima, igual que el contador del carrito y el botón flotante del
 * catálogo.</p>
 *
 * <p><b>Por qué el degradado no es el patrón prohibido.</b> Lo prohibido es que
 * un contenedor <em>con hijos</em> deje de pintar su fondo o lo pinte a mano:
 * ahí fue donde el texto se volvía invisible. El lienzo no tiene hijos, es
 * opaco, llama primero a {@code super.paintComponent(g)} y encima dibuja la
 * diapositiva, que es el dato en sí, exactamente como {@code GraficoBarras}
 * dibuja sus barras.</p>
 *
 * <p><b>Rotación.</b> Un {@link Timer} de Swing pasa a la siguiente cada pocos
 * segundos con un fundido corto; se detiene mientras el puntero está encima
 * (nadie quiere que la oferta que está leyendo se le escape) y cuando el banner
 * deja de estar en pantalla, para no dejar temporizadores vivos tras cerrar
 * sesión.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class BannerRotativo extends JLayeredPane {

    private static final int ALTO = 200;
    private static final int RADIO = 22;
    private static final int MARGEN = 40;
    private static final int MS_ROTACION = 5000;
    private static final int MS_PASO_FUNDIDO = 25;
    private static final int PASOS_FUNDIDO = 16;
    private static final int TAM_FLECHA = 36;
    private static final int ANCHO_ACCION = 150;
    private static final int ALTO_ACCION = 40;
    private static final int ANCHO_MAXIMO_VITRINA = 300;

    private final IComponentesFactory fabrica;
    private final List<Color[]> degradados;
    private final Function<String, Image> cargador;
    private final Map<String, Image> imagenes = new HashMap<>();

    private final Lienzo lienzo = new Lienzo();
    private final JButton anterior;
    private final JButton siguiente;
    private final JButton accion;

    private final List<Diapositiva> diapositivas = new ArrayList<>();
    private final Diapositiva bienvenida;
    private int actual;
    /** Diapositiva que se está desvaneciendo, o -1 si no hay fundido en curso. */
    private int previa = -1;
    private float opacidad = 1f;

    private final Timer rotacion;
    private final Timer fundido;
    private IntConsumer accionPulsada = indice -> { };

    /**
     * @param fabrica    fábrica de la que salen botones, tipografía y colores
     * @param degradados pares de colores (inicio, fin) que se van alternando
     *                   entre diapositivas
     * @param cargador   cómo convertir una referencia de imagen en una imagen;
     *                   lo aporta la fábrica para no duplicar la búsqueda
     *                   recurso → proyecto → disco
     */
    public BannerRotativo(IComponentesFactory fabrica, List<Color[]> degradados,
                          Function<String, Image> cargador) {
        this.fabrica = fabrica;
        this.degradados = degradados;
        this.cargador = cargador;
        this.bienvenida = new Diapositiva("Bienvenido a la tienda",
                "Explora el catálogo por categorías o busca lo que necesitas.", "");

        anterior = fabrica.crearBotonSegmento("<");
        anterior.setToolTipText("Promoción anterior");
        anterior.addActionListener(e -> irA(actual - 1));
        siguiente = fabrica.crearBotonSegmento(">");
        siguiente.setToolTipText("Promoción siguiente");
        siguiente.addActionListener(e -> irA(actual + 1));
        accion = fabrica.crearBotonDestacado("Ver oferta");
        accion.addActionListener(e -> accionPulsada.accept(actual));

        lienzo.setBackground(fabrica.colorFondo());
        lienzo.setOpaque(true);
        add(lienzo, JLayeredPane.DEFAULT_LAYER);
        add(anterior, JLayeredPane.PALETTE_LAYER);
        add(siguiente, JLayeredPane.PALETTE_LAYER);
        add(accion, JLayeredPane.PALETTE_LAYER);
        setPreferredSize(new Dimension(0, ALTO));

        // El JLayeredPane no reparte espacio: se recoloca a mano, como el FAB.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                colocar();
            }
        });

        rotacion = new Timer(MS_ROTACION, e -> {
            // Con el puntero encima (sobre el lienzo o sobre un botón) no se
            // avanza: el usuario está leyendo esa oferta.
            if (diapositivas.size() > 1 && getMousePosition(true) == null) {
                irA(actual + 1);
            }
        });
        fundido = new Timer(MS_PASO_FUNDIDO, e -> avanzarFundido());

        // Solo rota mientras está en pantalla: al cerrar sesión la ventana se
        // destruye y el temporizador no debe seguir vivo.
        addHierarchyListener(evento -> {
            if ((evento.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == 0) {
                return;
            }
            if (isShowing()) {
                rotacion.restart();
            } else {
                rotacion.stop();
                fundido.stop();
            }
        });
        actualizarControles();
    }

    /**
     * Reemplaza las diapositivas. Con la lista vacía se muestra una de
     * bienvenida, sin flechas ni botón de oferta.
     *
     * @param nuevas diapositivas a rotar, en orden
     */
    public void mostrar(List<Diapositiva> nuevas) {
        diapositivas.clear();
        diapositivas.addAll(nuevas);
        if (actual >= diapositivas.size()) {
            actual = 0;
        }
        previa = -1;
        opacidad = 1f;
        actualizarControles();
        lienzo.repaint();
    }

    /**
     * @param accion qué hacer al pulsar "Ver oferta": recibe el índice de la
     *               diapositiva visible, en el mismo orden de {@link #mostrar(List)}
     */
    public void alPulsarAccion(IntConsumer accion) {
        this.accionPulsada = accion;
    }

    private void irA(int indice) {
        if (diapositivas.size() < 2) {
            return;
        }
        int destino = Math.floorMod(indice, diapositivas.size());
        if (destino == actual) {
            return;
        }
        previa = actual;
        actual = destino;
        opacidad = 0f;
        fundido.restart();
        // Pasar a mano reinicia la cuenta: si no, la siguiente podría saltar
        // un instante después del clic.
        rotacion.restart();
    }

    private void avanzarFundido() {
        opacidad = Math.min(1f, opacidad + 1f / PASOS_FUNDIDO);
        if (opacidad >= 1f) {
            previa = -1;
            fundido.stop();
        }
        lienzo.repaint();
    }

    private void actualizarControles() {
        boolean hayVarias = diapositivas.size() > 1;
        anterior.setVisible(hayVarias);
        siguiente.setVisible(hayVarias);
        accion.setVisible(!diapositivas.isEmpty());
    }

    private void colocar() {
        int ancho = getWidth();
        int alto = getHeight();
        lienzo.setBounds(0, 0, ancho, alto);
        anterior.setBounds(10, (alto - TAM_FLECHA) / 2, TAM_FLECHA, TAM_FLECHA);
        siguiente.setBounds(ancho - TAM_FLECHA - 10, (alto - TAM_FLECHA) / 2,
                TAM_FLECHA, TAM_FLECHA);
        accion.setBounds(MARGEN + 16, alto - ALTO_ACCION - 30, ANCHO_ACCION, ALTO_ACCION);
    }

    private Image imagenDe(Diapositiva diapositiva) {
        String referencia = diapositiva.referenciaImagen();
        if (referencia == null || referencia.isBlank()) {
            return null;
        }
        return imagenes.computeIfAbsent(referencia, cargador);
    }

    /**
     * Capa base: dibuja la diapositiva visible (y la anterior, mientras dura el
     * fundido) y los puntos que indican cuántas hay.
     */
    private final class Lienzo extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            // El fondo lo pinta Swing; aquí solo se dibuja la diapositiva.
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            if (diapositivas.isEmpty()) {
                dibujar(g2, bienvenida, 0, 1f);
            } else {
                if (previa >= 0) {
                    dibujar(g2, diapositivas.get(previa), previa, 1f);
                }
                dibujar(g2, diapositivas.get(actual), actual, opacidad);
                dibujarPuntos(g2);
            }
            g2.dispose();
        }

        private void dibujar(Graphics2D g2, Diapositiva diapositiva, int indice, float alfa) {
            Graphics2D capa = (Graphics2D) g2.create();
            capa.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa));
            int ancho = getWidth();
            int alto = getHeight();

            Color[] par = degradados.get(indice % degradados.size());
            capa.setPaint(new GradientPaint(0, 0, par[0], ancho, alto, par[1]));
            capa.fill(new RoundRectangle2D.Float(0, 0, ancho, alto, RADIO, RADIO));

            // Vitrina con la foto a la derecha: el mismo recuadro oscuro que
            // en las tarjetas, porque las ilustraciones traen ese fondo.
            int anchoVitrina = Math.min(ANCHO_MAXIMO_VITRINA, ancho / 3);
            int xVitrina = ancho - anchoVitrina - MARGEN - 20;
            Image imagen = imagenDe(diapositiva);
            if (imagen != null && imagen.getWidth(null) > 0) {
                Shape marco = new RoundRectangle2D.Float(xVitrina, 22, anchoVitrina, alto - 44, 16, 16);
                Graphics2D foto = (Graphics2D) capa.create();
                foto.clip(marco);
                double escala = Math.min(anchoVitrina / (double) imagen.getWidth(null),
                        (alto - 44) / (double) imagen.getHeight(null));
                int anchoFoto = (int) Math.round(imagen.getWidth(null) * escala);
                int altoFoto = (int) Math.round(imagen.getHeight(null) * escala);
                foto.drawImage(imagen, xVitrina + (anchoVitrina - anchoFoto) / 2,
                        22 + (alto - 44 - altoFoto) / 2, anchoFoto, altoFoto, null);
                foto.dispose();
            } else {
                xVitrina = ancho - MARGEN;
            }

            int anchoTexto = xVitrina - MARGEN - 16 - 24;
            capa.setColor(fabrica.colorTextoSobreAcento());
            capa.setFont(fabrica.fuente(Font.BOLD, 28));
            capa.drawString(recortar(capa.getFontMetrics(), diapositiva.titulo(), anchoTexto),
                    MARGEN + 16, 70);
            capa.setFont(fabrica.fuente(Font.PLAIN, 15));
            capa.drawString(recortar(capa.getFontMetrics(), diapositiva.subtitulo(), anchoTexto),
                    MARGEN + 16, 100);
            capa.dispose();
        }

        /** Un punto por diapositiva; el de la visible, más ancho. */
        private void dibujarPuntos(Graphics2D g2) {
            if (diapositivas.size() < 2) {
                return;
            }
            int diametro = 8;
            int separacion = 8;
            int anchoActivo = 22;
            int total = (diapositivas.size() - 1) * (diametro + separacion) + anchoActivo;
            int x = (getWidth() - total) / 2;
            int y = getHeight() - 18;
            g2.setColor(fabrica.colorTextoSobreAcento());
            for (int i = 0; i < diapositivas.size(); i++) {
                boolean visible = i == actual;
                // Los no visibles, del mismo color pero translúcidos: se leen
                // como "hay más" sin competir con el activo.
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                        visible ? 1f : 0.45f));
                int ancho = visible ? anchoActivo : diametro;
                g2.fillRoundRect(x, y, ancho, diametro, diametro, diametro);
                x += ancho + separacion;
            }
        }

        /** Corta un texto con puntos suspensivos para que no invada la foto. */
        private String recortar(FontMetrics metricas, String texto, int anchoMaximo) {
            if (texto == null) {
                return "";
            }
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
}
