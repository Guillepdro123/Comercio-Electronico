package view.factory.utils;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

/**
 * Gestiona el texto fantasma ("placeholder") de un {@link JTextField}: lo
 * muestra en gris cuando el campo está vacío y sin foco, y lo retira apenas
 * el usuario empieza a escribir.
 *
 * <p><b>Responsabilidad única:</b> esta clase solo sabe de placeholders; no
 * valida datos ni conoce las reglas de negocio del formulario. Se reutiliza
 * en todos los campos de texto en lugar de repetir la lógica en cada uno.</p>
 *
 * <p>El estado y los datos del placeholder (texto, colores) se guardan como
 * propiedades del propio componente ({@code putClientProperty}), de modo que
 * los métodos estáticos puedan consultarlos o refrescarlos desde otra clase
 * (por ejemplo, la vista, al limpiar el formulario) sin necesitar una
 * referencia a esta instancia.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public class PlaceholderFocusListener implements FocusListener {

    private static final String CLAVE_ACTIVO = "rf.placeholder.activo";
    private static final String CLAVE_TEXTO = "rf.placeholder.texto";
    private static final String CLAVE_COLOR_FANTASMA = "rf.placeholder.colorFantasma";
    private static final String CLAVE_COLOR_NORMAL = "rf.placeholder.colorNormal";

    private final JTextField campo;
    /** Se ejecuta cada vez que cambia el estado del placeholder; puede ser {@code null}. */
    private final Runnable alCambiarEstado;

    /**
     * @param campo              campo a vigilar
     * @param textoFantasma      guía gris a mostrar cuando el campo está vacío
     * @param colorTextoNormal   color del texto real digitado por el usuario
     * @param colorTextoFantasma color del texto fantasma
     * @param alCambiarEstado    callback opcional para que quien use el campo
     *                           (por ejemplo, un control de contraseña que
     *                           también maneja el carácter de enmascarado)
     *                           reaccione a cada cambio de estado
     */
    public PlaceholderFocusListener(JTextField campo, String textoFantasma,
            Color colorTextoNormal, Color colorTextoFantasma, Runnable alCambiarEstado) {
        this.campo = campo;
        this.alCambiarEstado = alCambiarEstado;
        campo.putClientProperty(CLAVE_TEXTO, textoFantasma);
        campo.putClientProperty(CLAVE_COLOR_FANTASMA, colorTextoFantasma);
        campo.putClientProperty(CLAVE_COLOR_NORMAL, colorTextoNormal);
    }

    /** Registra el listener y deja el campo en su estado inicial. */
    public void activar() {
        campo.addFocusListener(this);
        actualizarSegunContenido();
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (esPlaceholderActivo(campo)) {
            campo.setText("");
            campo.setForeground((Color) campo.getClientProperty(CLAVE_COLOR_NORMAL));
            campo.putClientProperty(CLAVE_ACTIVO, Boolean.FALSE);
            notificar();
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        actualizarSegunContenido();
    }

    private void actualizarSegunContenido() {
        if (campo.getText().isEmpty()) {
            campo.setText((String) campo.getClientProperty(CLAVE_TEXTO));
            campo.setForeground((Color) campo.getClientProperty(CLAVE_COLOR_FANTASMA));
            campo.putClientProperty(CLAVE_ACTIVO, Boolean.TRUE);
        } else {
            campo.putClientProperty(CLAVE_ACTIVO, Boolean.FALSE);
        }
        notificar();
    }

    private void notificar() {
        if (alCambiarEstado != null) {
            alCambiarEstado.run();
        }
    }

    /**
     * @param campo campo a consultar
     * @return {@code true} si lo que se ve actualmente es el texto fantasma
     */
    public static boolean esPlaceholderActivo(JTextField campo) {
        return Boolean.TRUE.equals(campo.getClientProperty(CLAVE_ACTIVO));
    }

    /**
     * @param campo campo a leer
     * @return texto realmente digitado por el usuario, o cadena vacía si solo
     *         se ve el texto fantasma
     */
    public static String textoReal(JTextField campo) {
        return esPlaceholderActivo(campo) ? "" : campo.getText().trim();
    }

    /**
     * Vacía el campo y vuelve a mostrar su texto fantasma configurado. Se usa
     * al limpiar el formulario tras un registro exitoso.
     *
     * @param campo campo a reiniciar
     */
    public static void limpiarYMostrarPlaceholder(JTextField campo) {
        campo.setText("");
        String texto = (String) campo.getClientProperty(CLAVE_TEXTO);
        Color colorFantasma = (Color) campo.getClientProperty(CLAVE_COLOR_FANTASMA);
        if (texto != null) {
            campo.setText(texto);
            campo.setForeground(colorFantasma);
            campo.putClientProperty(CLAVE_ACTIVO, Boolean.TRUE);
        }
    }

    /**
     * Cambia el texto fantasma configurado para un campo (por ejemplo, cuando
     * el campo dinámico pasa de pedir una dirección a pedir un NIT) y lo
     * refresca de inmediato si en ese momento no tiene contenido real.
     *
     * @param campo       campo a actualizar
     * @param nuevoTexto  nueva guía gris
     */
    public static void cambiarTextoFantasma(JTextField campo, String nuevoTexto) {
        campo.putClientProperty(CLAVE_TEXTO, nuevoTexto);
        limpiarYMostrarPlaceholder(campo);
    }
}
