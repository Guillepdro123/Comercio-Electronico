package view.factory.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import view.factory.IComponentesFactory;
import view.factory.icons.IconoCampo;
import view.factory.utils.PlaceholderFocusListener;

/**
 * Campo de texto con un ícono de contexto a la izquierda, presentados como un
 * único componente.
 *
 * <p><b>Encapsulamiento:</b> nadie fuera de esta clase toca el
 * {@link JTextField} interno; el resto del código solo conoce
 * {@link #getTexto()} y {@link #limpiar()}. Es el mismo patrón de componente
 * compuesto que ya usa {@link CampoPasswordConToggle} (panel con
 * {@link BorderLayout}, borde en el panel y no en el campo, para que ícono y
 * campo compartan un único marco visual) — se reutiliza esa estructura
 * probada en vez de inventar otra.</p>
 *
 * <p>El ícono es puramente informativo: da contexto de un vistazo sobre qué
 * se pide en el campo, siguiendo el patrón de "leading icon" de los
 * formularios modernos.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public class CampoTextoConIcono extends JPanel {

    private final IComponentesFactory fabrica;
    private final JLabel etiquetaIcono;
    private final JTextField campo;

    /**
     * @param fabrica       fábrica de la que se toman colores, tipografía y el borde
     * @param textoFantasma guía gris mostrada cuando el campo está vacío
     * @param tipoIcono     glifo que acompaña al campo
     */
    public CampoTextoConIcono(IComponentesFactory fabrica, String textoFantasma, IconoCampo.Tipo tipoIcono) {
        super(new BorderLayout());
        this.fabrica = fabrica;

        etiquetaIcono = new JLabel();
        etiquetaIcono.setBorder(new EmptyBorder(0, 0, 0, 8));
        cambiarIcono(tipoIcono);

        campo = new JTextField();
        campo.setBackground(fabrica.colorCampo());
        campo.setForeground(fabrica.colorTexto());
        campo.setCaretColor(fabrica.colorAcento());
        campo.setFont(fabrica.fuente(Font.PLAIN, 14));
        // El borde lo pinta este panel, no el campo, para que el ícono quede
        // dentro del mismo marco redondeado.
        campo.setBorder(null);

        setBackground(fabrica.colorCampo());
        setPreferredSize(new Dimension(fabrica.anchoCampo(), fabrica.altoCampo()));
        add(etiquetaIcono, BorderLayout.WEST);
        add(campo, BorderLayout.CENTER);

        fabrica.instalarAnilloEnfoque(this, campo);
        new PlaceholderFocusListener(campo, textoFantasma, fabrica.colorTexto(),
                fabrica.colorTextoSuave(), null).activar();
    }

    /** @return texto real digitado, o cadena vacía si solo se ve el texto fantasma */
    public String getTexto() {
        return PlaceholderFocusListener.textoReal(campo);
    }

    /** Vacía el campo y vuelve a mostrar el texto fantasma. */
    public void limpiar() {
        PlaceholderFocusListener.limpiarYMostrarPlaceholder(campo);
    }

    /** Cambia el texto fantasma (lo usa el campo dinámico del registro). */
    public void cambiarTextoFantasma(String nuevoTexto) {
        PlaceholderFocusListener.cambiarTextoFantasma(campo, nuevoTexto);
    }

    /**
     * Cambia el glifo del campo. Lo usa el campo dinámico del registro, que
     * pasa de pedir una dirección a pedir un NIT según el tipo de cuenta: el
     * ícono acompaña ese cambio de significado.
     *
     * @param tipoIcono nuevo glifo
     */
    public final void cambiarIcono(IconoCampo.Tipo tipoIcono) {
        etiquetaIcono.setIcon(new IconoCampo(
                tipoIcono, fabrica.colorTextoSuave(), fabrica.tamanoIconoCampo()));
    }

    /** Lleva el cursor a este campo. */
    public void enfocar() {
        campo.requestFocusInWindow();
    }

    /**
     * Propaga el estado al campo interno. Sin esto, deshabilitar el
     * componente compuesto dejaría el {@code JTextField} de dentro
     * perfectamente escribible: un {@link JPanel} no traslada
     * {@code setEnabled} a sus hijos.
     */
    @Override
    public void setEnabled(boolean habilitado) {
        super.setEnabled(habilitado);
        // Nimbus repinta los campos deshabilitados con un fondo claro que
        // rompe la paleta oscura, y ni setBackground ni setDisabledTextColor
        // lo evitan. Bloquear la escritura con setEditable(false) consigue lo
        // mismo (no se puede teclear) conservando el color del tema; se le
        // quita además el foco para que no sea alcanzable con el tabulador.
        campo.setEditable(habilitado);
        campo.setFocusable(habilitado);
    }
}
