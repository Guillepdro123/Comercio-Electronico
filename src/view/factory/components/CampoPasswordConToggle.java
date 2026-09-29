package view.factory.components;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import view.factory.IComponentesFactory;
import view.factory.icons.IconoCampo;
import view.factory.icons.IconoOjo;
import view.factory.utils.PlaceholderFocusListener;

/**
 * Control compuesto de contraseña: un {@link JPasswordField} y su botón
 * "ojo", presentados como un único componente.
 *
 * <p><b>Encapsulamiento:</b> nadie fuera de esta clase toca el
 * {@link JPasswordField} interno. El resto del código solo conoce
 * {@link #getPassword()} y {@link #limpiar()}.</p>
 *
 * <p>El carácter de enmascarado ({@code echoChar}) depende de dos estados
 * independientes que conviven en el mismo campo: si en este momento se ve el
 * texto fantasma (siempre en texto plano, para que se pueda leer) o si el
 * usuario pidió ver su contraseña real con el botón-ojo. Por eso ambos casos
 * se resuelven en un único método, {@link #actualizarEchoChar()}.</p>
 *
 * <p>El borde y el fondo son un {@code LineBorder} y un color sólido
 * (componentes opacos estándar de Swing) — a propósito, sin
 * {@code paintComponent} propio: es el patrón simple y estable con el que
 * arrancó el proyecto, tras revertir un intento de redondear esquinas que
 * causó conflictos de color y texto invisible.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public class CampoPasswordConToggle extends JPanel {

    private static final char CARACTER_OCULTO = '•';

    private final JPasswordField campo;
    private final JButton botonAlternar;
    private final IComponentesFactory fabrica;
    private boolean passwordVisible = false;

    /**
     * @param fabrica       fábrica de la que se toman colores y tipografía
     * @param textoFantasma guía gris mostrada cuando el campo está vacío
     */
    public CampoPasswordConToggle(IComponentesFactory fabrica, String textoFantasma) {
        super(new BorderLayout());
        this.fabrica = fabrica;

        campo = new JPasswordField();
        campo.setEchoChar(CARACTER_OCULTO);
        campo.setBackground(fabrica.colorCampo());
        campo.setForeground(fabrica.colorTexto());
        campo.setCaretColor(fabrica.colorAcento());
        campo.setFont(fabrica.fuente(Font.PLAIN, 14));
        // El borde lo dibuja este panel, no el campo, para que el ícono y el
        // botón queden dentro del mismo marco redondeado.
        campo.setBorder(null);

        JLabel etiquetaIcono = new JLabel(new IconoCampo(
                IconoCampo.Tipo.CANDADO, fabrica.colorTextoSuave(), fabrica.tamanoIconoCampo()));
        etiquetaIcono.setBorder(new EmptyBorder(0, 0, 0, 8));

        botonAlternar = new JButton();
        botonAlternar.setBackground(fabrica.colorCampo());
        botonAlternar.setOpaque(false);
        botonAlternar.setBorderPainted(false);
        botonAlternar.setFocusPainted(false);
        botonAlternar.setContentAreaFilled(false);
        botonAlternar.setBorder(new EmptyBorder(0, 8, 0, 0));
        botonAlternar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonAlternar.addActionListener(e -> alternarVisibilidad());

        setBackground(fabrica.colorCampo());
        setPreferredSize(new Dimension(fabrica.anchoCampo(), fabrica.altoCampo()));
        add(etiquetaIcono, BorderLayout.WEST);
        add(campo, BorderLayout.CENTER);
        add(botonAlternar, BorderLayout.EAST);
        fabrica.instalarAnilloEnfoque(this, campo);

        new PlaceholderFocusListener(campo, textoFantasma, fabrica.colorTexto(),
                fabrica.colorTextoSuave(), this::actualizarEchoChar).activar();
        actualizarIconoBoton();
    }

    private void alternarVisibilidad() {
        if (PlaceholderFocusListener.esPlaceholderActivo(campo)) {
            // Con el texto fantasma no hay contraseña real que mostrar u ocultar.
            return;
        }
        passwordVisible = !passwordVisible;
        actualizarEchoChar();
        actualizarIconoBoton();
        campo.requestFocusInWindow();
    }

    private void actualizarEchoChar() {
        boolean debeVerseComoTextoPlano = passwordVisible || PlaceholderFocusListener.esPlaceholderActivo(campo);
        campo.setEchoChar(debeVerseComoTextoPlano ? (char) 0 : CARACTER_OCULTO);
    }

    private void actualizarIconoBoton() {
        botonAlternar.setIcon(new IconoOjo(
                passwordVisible ? fabrica.colorAcento() : fabrica.colorTextoSuave(), passwordVisible));
        botonAlternar.setToolTipText(passwordVisible ? "Ocultar contraseña" : "Mostrar contraseña");
    }

    /**
     * Avisa cada vez que cambia lo escrito, para que la vista pueda reaccionar
     * en vivo (lo usa el indicador de fortaleza del registro).
     *
     * @param alEscribir acción a ejecutar tras cada cambio del contenido
     */
    public void alEscribir(Runnable alEscribir) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                alEscribir.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                alEscribir.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                alEscribir.run();
            }
        });
    }

    /** @return contraseña digitada, o cadena vacía si solo se ve el texto fantasma */
    public String getPassword() {
        return PlaceholderFocusListener.esPlaceholderActivo(campo) ? "" : new String(campo.getPassword());
    }

    /**
     * Propaga el estado al campo y al botón-ojo. Sin esto, deshabilitar el
     * componente compuesto dejaría ambos operativos: un {@link JPanel} no
     * traslada {@code setEnabled} a sus hijos.
     */
    @Override
    public void setEnabled(boolean habilitado) {
        super.setEnabled(habilitado);
        botonAlternar.setEnabled(habilitado);
        // Nimbus repinta los campos deshabilitados con un fondo claro que
        // rompe la paleta oscura, y ni setBackground ni setDisabledTextColor
        // lo evitan. Bloquear la escritura con setEditable(false) consigue lo
        // mismo (no se puede teclear) conservando el color del tema; se le
        // quita además el foco para que no sea alcanzable con el tabulador.
        campo.setEditable(habilitado);
        campo.setFocusable(habilitado);
    }

    /** Vacía el campo, vuelve a mostrar el texto fantasma y oculta la contraseña. */
    public void limpiar() {
        PlaceholderFocusListener.limpiarYMostrarPlaceholder(campo);
        passwordVisible = false;
        actualizarEchoChar();
        actualizarIconoBoton();
    }
}
