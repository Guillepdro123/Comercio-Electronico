package view.factory.tema;

import java.awt.Color;
import java.util.function.Function;

/**
 * Un color que no tiene valor fijo: lo toma de la paleta vigente cada vez que
 * alguien lo pinta.
 *
 * <p><b>Por qué existe.</b> Los componentes de este proyecto reciben sus
 * colores de la fábrica con {@code setBackground(...)}, bordes y estilos, no
 * del Look and Feel. {@code SwingUtilities.updateComponentTreeUI(...)} solo
 * reemplaza los colores que puso el propio Look and Feel, así que al cambiar
 * de tema todos esos componentes se quedaban con la paleta anterior, y la
 * única salida era cerrar la ventana y reconstruirla (con su parpadeo). Con
 * este color, el componente guarda "el color de panel" en vez de "#FFFFFF":
 * al cambiar la paleta basta con repintar.</p>
 *
 * <p><b>Cómo funciona.</b> Java2D y Swing obtienen el valor de un
 * {@link Color} a través de {@link #getRGB()} (y {@code getRed()},
 * {@code getAlpha()}... se calculan a partir de él), así que basta con
 * redefinir ese método. {@link #hashCode()} se redefine a juego para que
 * {@code equals}/{@code hashCode} sigan siendo coherentes.</p>
 *
 * <p>Solo la crea {@link TemaDinamico}; ninguna vista instancia colores.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class ColorDeTema extends Color {

    private static final long serialVersionUID = 1L;

    private final transient TemaDinamico tema;
    private final transient Function<Paleta, Color> rol;

    /**
     * @param tema tema del que se lee la paleta vigente
     * @param rol  qué color de la paleta representa
     */
    ColorDeTema(TemaDinamico tema, Function<Paleta, Color> rol) {
        // El valor que guarda Color no se usa nunca: getRGB() lo calcula.
        super(0, true);
        this.tema = tema;
        this.rol = rol;
    }

    @Override
    public int getRGB() {
        return rol.apply(tema.paleta()).getRGB();
    }

    @Override
    public int hashCode() {
        return getRGB();
    }

    @Override
    public boolean equals(Object otro) {
        return super.equals(otro);
    }
}
