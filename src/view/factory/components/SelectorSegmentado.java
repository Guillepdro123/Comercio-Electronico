package view.factory.components;

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import view.factory.IComponentesFactory;

/**
 * Selector de una opción entre pocas, presentado como control segmentado: dos
 * o más botones pegados donde el activo queda resaltado.
 *
 * <p><b>Por qué reemplazó al {@code JComboBox}:</b> por diseño, para una
 * elección binaria como "Cliente o Proveedor" un segmentado muestra las dos
 * opciones de una vez y se resuelve en un clic, en vez de obligar a abrir una
 * lista. Y por robustez: Nimbus pinta los {@code JComboBox} (y sus
 * renderers) con sus propios gradientes claros e ignora los colores del tema,
 * así que el combo se veía como un control de modo claro incrustado en la
 * paleta oscura. Este control se arma con los mismos botones que ya produce
 * la fábrica, que sí respetan la paleta.</p>
 *
 * <p><b>Encapsulamiento:</b> hacia afuera solo expone
 * {@link #getSeleccionado()}; quién está resaltado y cómo es asunto interno.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public class SelectorSegmentado extends JPanel {

    private final IComponentesFactory fabrica;
    private final JButton[] segmentos;
    private final String[] opciones;
    private int indiceActivo = 0;

    /**
     * @param fabrica   fábrica de la que se toman los botones y sus estilos
     * @param opciones  textos de cada segmento; el primero queda activo
     * @param alCambiar acción a ejecutar cuando cambia la selección (puede ser {@code null})
     */
    public SelectorSegmentado(IComponentesFactory fabrica, String[] opciones, Runnable alCambiar) {
        super(new GridLayout(1, opciones.length, 8, 0));
        this.fabrica = fabrica;
        this.opciones = opciones.clone();
        this.segmentos = new JButton[opciones.length];
        setOpaque(false);
        setPreferredSize(new Dimension(fabrica.anchoCampo(), fabrica.altoCampo()));

        for (int i = 0; i < opciones.length; i++) {
            final int indice = i;
            JButton segmento = fabrica.crearBotonSegmento(opciones[i]);
            segmento.addActionListener(e -> {
                seleccionar(indice);
                if (alCambiar != null) {
                    alCambiar.run();
                }
            });
            segmentos[i] = segmento;
            add(segmento);
        }
        seleccionar(0);
    }

    /** @return texto de la opción actualmente seleccionada */
    public String getSeleccionado() {
        return opciones[indiceActivo];
    }

    private void seleccionar(int indice) {
        indiceActivo = indice;
        for (int i = 0; i < segmentos.length; i++) {
            fabrica.resaltarSegmento(segmentos[i], i == indice);
        }
    }
}
