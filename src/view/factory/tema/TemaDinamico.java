package view.factory.tema;

import java.awt.Color;
import java.util.function.Function;

/**
 * El tema vigente de la aplicación: la {@link Paleta} en uso y los colores
 * "vivos" ({@link ColorDeTema}) que la fábrica reparte a los componentes.
 *
 * <p>Cada rol de la paleta tiene un único {@link ColorDeTema}: todos los
 * componentes que usan "el color de texto" comparten el mismo objeto, y al
 * {@link #cambiar(Paleta) cambiar} la paleta todos pasan a pintarse con el
 * valor nuevo sin que nadie los toque. Los métodos repiten los nombres de los
 * roles de {@link Paleta} a propósito: en la fábrica, {@code tema.panel()}
 * se lee igual que antes {@code paleta.panel()}, pero devuelve un color vivo.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class TemaDinamico {

    /** Se lee desde el hilo de pintado y se cambia desde el de eventos. */
    private volatile Paleta paleta;

    private final ColorDeTema fondo = rol(Paleta::fondo);
    private final ColorDeTema panel = rol(Paleta::panel);
    private final ColorDeTema campo = rol(Paleta::campo);
    private final ColorDeTema vitrina = rol(Paleta::vitrina);
    private final ColorDeTema sidebar = rol(Paleta::sidebar);
    private final ColorDeTema sidebarHover = rol(Paleta::sidebarHover);
    private final ColorDeTema sidebarPresionado = rol(Paleta::sidebarPresionado);
    private final ColorDeTema bordeSidebar = rol(Paleta::bordeSidebar);
    private final ColorDeTema textoSidebar = rol(Paleta::textoSidebar);
    private final ColorDeTema acento = rol(Paleta::acento);
    private final ColorDeTema acentoHover = rol(Paleta::acentoHover);
    private final ColorDeTema acentoPresionado = rol(Paleta::acentoPresionado);
    private final ColorDeTema destacado = rol(Paleta::destacado);
    private final ColorDeTema destacadoHover = rol(Paleta::destacadoHover);
    private final ColorDeTema destacadoPresionado = rol(Paleta::destacadoPresionado);
    private final ColorDeTema texto = rol(Paleta::texto);
    private final ColorDeTema textoSuave = rol(Paleta::textoSuave);
    private final ColorDeTema textoSobreAcento = rol(Paleta::textoSobreAcento);
    private final ColorDeTema borde = rol(Paleta::borde);
    private final ColorDeTema bordeHover = rol(Paleta::bordeHover);
    private final ColorDeTema error = rol(Paleta::error);
    private final ColorDeTema exito = rol(Paleta::exito);
    private final ColorDeTema advertencia = rol(Paleta::advertencia);
    private final ColorDeTema informacion = rol(Paleta::informacion);

    /**
     * @param inicial paleta con la que arranca la aplicación
     */
    public TemaDinamico(Paleta inicial) {
        this.paleta = inicial;
    }

    /** @return la paleta vigente */
    public Paleta paleta() {
        return paleta;
    }

    /**
     * Pasa a otra paleta. Los colores vivos ya repartidos toman el valor nuevo
     * en su siguiente pintado.
     *
     * @param nueva paleta a usar desde ahora
     */
    public void cambiar(Paleta nueva) {
        this.paleta = nueva;
    }

    /** @return {@code true} si la paleta vigente es oscura */
    public boolean esOscura() {
        return paleta.esOscura();
    }

    /**
     * Un color vivo calculado a partir de la paleta (una mezcla, o un rol que
     * depende de si el tema es oscuro). No se guarda: cada llamada crea uno.
     *
     * @param calculo cómo obtener el color a partir de la paleta vigente
     * @return color que se recalcula en cada pintado
     */
    public ColorDeTema derivado(Function<Paleta, Color> calculo) {
        return new ColorDeTema(this, calculo);
    }

    private ColorDeTema rol(Function<Paleta, Color> rol) {
        return new ColorDeTema(this, rol);
    }

    public Color fondo() { return fondo; }
    public Color panel() { return panel; }
    public Color campo() { return campo; }
    public Color vitrina() { return vitrina; }
    public Color sidebar() { return sidebar; }
    public Color sidebarHover() { return sidebarHover; }
    public Color sidebarPresionado() { return sidebarPresionado; }
    public Color bordeSidebar() { return bordeSidebar; }
    public Color textoSidebar() { return textoSidebar; }
    public Color acento() { return acento; }
    public Color acentoHover() { return acentoHover; }
    public Color acentoPresionado() { return acentoPresionado; }
    public Color destacado() { return destacado; }
    public Color destacadoHover() { return destacadoHover; }
    public Color destacadoPresionado() { return destacadoPresionado; }
    public Color texto() { return texto; }
    public Color textoSuave() { return textoSuave; }
    public Color textoSobreAcento() { return textoSobreAcento; }
    public Color borde() { return borde; }
    public Color bordeHover() { return bordeHover; }
    public Color error() { return error; }
    public Color exito() { return exito; }
    public Color advertencia() { return advertencia; }
    public Color informacion() { return informacion; }
}
