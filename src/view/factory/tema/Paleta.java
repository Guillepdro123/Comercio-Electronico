package view.factory.tema;

import java.awt.Color;

/**
 * Colores de un tema visual, por función y no por valor.
 *
 * <p><b>Por qué la paleta es un dato y no una clase de fábrica.</b> La regla
 * del proyecto era que un tema nuevo se añadiera como otra implementación de
 * {@code IComponentesFactory}. Con una fábrica de más de mil líneas eso
 * obligaba a duplicarla entera para cambiar unos cuantos colores, y cada
 * corrección habría que hacerla dos veces. Separando los colores en este
 * registro, {@code ComponentesSwingFactory} construye los componentes y la
 * paleta decide cómo se ven: un tema nuevo es otro valor de {@code Paleta},
 * sin tocar ni la fábrica ni las vistas (Abierto/Cerrado).</p>
 *
 * <p>Esta es, junto con la fábrica, la única clase del proyecto que conoce
 * colores concretos.</p>
 *
 * @param fondo               fondo general de las ventanas
 * @param panel               tarjetas, formularios y diálogos
 * @param campo               relleno de los campos de captura
 * @param vitrina             recuadro de las imágenes de producto; coincide con
 *                            el fondo horneado en las ilustraciones del catálogo
 * @param sidebar             sidebar del Login y barra superior de los paneles
 * @param sidebarHover        botón del sidebar bajo el puntero
 * @param sidebarPresionado   botón del sidebar pulsado
 * @param bordeSidebar        contorno de los botones inactivos del sidebar
 * @param textoSidebar        texto sobre el sidebar y la barra superior
 * @param acento              color de marca: botón principal, selección, enlaces
 * @param acentoHover         acento bajo el puntero
 * @param acentoPresionado    acento pulsado (se hunde, no se aclara)
 * @param destacado           llamadas a la acción de compra (naranja)
 * @param destacadoHover      destacado bajo el puntero
 * @param destacadoPresionado destacado pulsado
 * @param texto               texto principal sobre fondo y paneles
 * @param textoSuave          etiquetas, ayudas y texto fantasma
 * @param textoSobreAcento    texto sobre el acento y el destacado
 * @param borde               bordes sutiles
 * @param bordeHover          borde de un campo bajo el puntero
 * @param error               mensajes de error
 * @param exito               mensajes de éxito e insignias de descuento
 * @param advertencia         avisos intermedios
 * @param informacion         datos informativos (azul)
 * @param esOscura            {@code true} si el tema es oscuro; decide qué
 *                            variante de FlatLaf se instala
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record Paleta(Color fondo, Color panel, Color campo, Color vitrina,
                     Color sidebar, Color sidebarHover, Color sidebarPresionado,
                     Color bordeSidebar, Color textoSidebar,
                     Color acento, Color acentoHover, Color acentoPresionado,
                     Color destacado, Color destacadoHover, Color destacadoPresionado,
                     Color texto, Color textoSuave, Color textoSobreAcento,
                     Color borde, Color bordeHover,
                     Color error, Color exito, Color advertencia, Color informacion,
                     boolean esOscura) {

    /** Fondo horneado en las ilustraciones de producto: no depende del tema. */
    private static final Color VITRINA = new Color(0x2A2545);

    /*
     * El sidebar es el mismo en los dos temas: el GIF del asistente lleva su
     * fondo #1B1627 horneado en cada cuadro (la transparencia binaria del GIF
     * deja bordes dentados), así que sobre otro color se vería el recuadro.
     */
    private static final Color SIDEBAR = new Color(0x1B1627);
    private static final Color SIDEBAR_HOVER = new Color(0x231F3D);
    private static final Color SIDEBAR_PRESIONADO = new Color(0x2A2545);
    private static final Color BORDE_SIDEBAR = new Color(0x362F52);

    /** El naranja de las llamadas a la acción, igual en los dos temas. */
    private static final Color DESTACADO = new Color(0xEA580C);
    private static final Color DESTACADO_HOVER = new Color(0xF97316);
    private static final Color DESTACADO_PRESIONADO = new Color(0xC2410C);

    /**
     * Tema claro: fondos blancos y grises muy suaves, el morado como color de
     * marca y el naranja para las acciones de compra.
     *
     * <p>El acento baja de {@code #8B5CF6} a {@code #7C3AED} porque sobre
     * blanco el primero apenas llega a 4,2:1 de contraste con texto blanco
     * encima; el segundo pasa de 5,7:1.</p>
     *
     * @return la paleta clara, que es la que usa hoy la aplicación
     */
    public static Paleta clara() {
        return new Paleta(
                new Color(0xF4F5FA), Color.WHITE, new Color(0xF3F4F8), VITRINA,
                SIDEBAR, SIDEBAR_HOVER, SIDEBAR_PRESIONADO, BORDE_SIDEBAR, Color.WHITE,
                new Color(0x7C3AED), new Color(0x8B5CF6), new Color(0x6D28D9),
                DESTACADO, DESTACADO_HOVER, DESTACADO_PRESIONADO,
                new Color(0x1F2937), new Color(0x6B7280), Color.WHITE,
                new Color(0xE2E4EC), new Color(0xC4B5FD),
                new Color(0xDC2626), new Color(0x16A34A), new Color(0xD97706),
                new Color(0x2563EB),
                false);
    }

    /**
     * Tema oscuro "SaaS/Tech Morado" de los incrementos anteriores, con los
     * mismos valores que tenía la fábrica, ordenados por elevación: fondo
     * {@code #13111C} → sidebar {@code #1B1627} → tarjetas {@code #231F3D} →
     * campos {@code #2A2545}.
     *
     * @return la paleta oscura
     */
    public static Paleta oscura() {
        return new Paleta(
                new Color(0x13111C), new Color(0x231F3D), new Color(0x2A2545), VITRINA,
                SIDEBAR, SIDEBAR_HOVER, SIDEBAR_PRESIONADO, BORDE_SIDEBAR, Color.WHITE,
                new Color(0x8B5CF6), new Color(0xA78BFA), new Color(0x7C3AED),
                DESTACADO, DESTACADO_HOVER, DESTACADO_PRESIONADO,
                Color.WHITE, new Color(0xF3F4F6), Color.WHITE,
                new Color(0x362F52), new Color(0x4C4370),
                new Color(0xF87171), new Color(0x4ADE80), new Color(0xFBBF24),
                new Color(0x60A5FA),
                true);
    }
}
