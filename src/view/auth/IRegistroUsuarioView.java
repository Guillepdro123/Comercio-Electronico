package view.auth;

import javax.swing.JButton;

/**
 * Contrato mínimo que {@link controller.UsuarioController} necesita de la
 * vista de registro.
 *
 * <p><b>Segregación de Interfaces (I de SOLID):</b> solo declara lo que el
 * controlador realmente usa (leer datos, pedir el botón para escuchar su
 * clic, mostrar un error, limpiar el formulario); no expone layouts, colores
 * ni componentes internos que no le conciernen.</p>
 *
 * <p><b>Inversión de Dependencias (D de SOLID):</b> el controlador depende de
 * esta abstracción, nunca de {@link PanelRegistroUsuario} directamente, así
 * que la vista se puede rediseñar por completo sin tocar una sola línea del
 * controlador.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public interface IRegistroUsuarioView {

    /** Opciones del selector de tipo de cuenta. */
    String TIPO_CLIENTE = "Cliente";
    String TIPO_PROVEEDOR = "Proveedor";

    /** @return identificación digitada, sin espacios sobrantes */
    String getIdentificacion();

    /** @return nombres digitados, sin espacios sobrantes */
    String getNombres();

    /** @return correo digitado, sin espacios sobrantes */
    String getCorreo();

    /** Nivel de fortaleza: no cumple los requisitos (se mostrará en rojo). */
    int FORTALEZA_DEBIL = 0;

    /** Nivel de fortaleza: cumple los requisitos pero es corta (ámbar). */
    int FORTALEZA_MEDIA = 1;

    /** Nivel de fortaleza: cumple los requisitos y es larga y variada (verde). */
    int FORTALEZA_FUERTE = 2;

    /**
     * Registra una acción que la vista ejecutará cada vez que se escriba en el
     * campo de contraseña, para poder evaluar su fortaleza en vivo.
     *
     * <p>Es el mismo trato que con el botón: la vista avisa, el controlador
     * decide. Aquí el controlador no puede usar un {@code ActionListener}
     * porque le interesa cada tecla, no un clic.</p>
     *
     * @param alEscribir acción a ejecutar tras cada cambio
     */
    void observarPassword(Runnable alEscribir);

    /**
     * Muestra el semáforo de fortaleza junto al campo de contraseña.
     *
     * <p>El controlador manda el nivel y el texto ya resueltos; la vista solo
     * decide de qué color pinta cada nivel, que es cosa del tema.</p>
     *
     * @param nivel       una de las constantes {@code FORTALEZA_*}
     * @param descripcion texto a mostrar junto al punto
     */
    void mostrarFortalezaPassword(int nivel, String descripcion);

    /** Oculta el semáforo (cuando todavía no se ha escrito nada). */
    void ocultarFortalezaPassword();

    /** @return contraseña digitada */
    String getPassword();

    /** @return opción seleccionada en el combo ("Cliente" o "Proveedor") */
    String getTipoCuentaSeleccionado();

    /** @return valor del campo dinámico (dirección o NIT, según el combo) */
    String getDatoAdicional();

    /** @return botón principal, para que el controlador le registre su listener */
    JButton getBtnRegistrar();

    /**
     * Muestra un mensaje de error de validación o de negocio.
     *
     * @param mensaje texto a publicar
     */
    void mostrarError(String mensaje);

    /**
     * Confirma al usuario que el registro fue exitoso (por ejemplo, con un
     * cuadro de diálogo). El controlador solo entrega el texto; cómo se
     * muestra es decisión exclusiva de la vista.
     *
     * @param mensaje mensaje de confirmación, ya resuelto por la entidad registrada
     */
    void mostrarExito(String mensaje);

    /** Vacía todos los campos y devuelve el foco al primero. */
    void limpiarFormulario();
}
