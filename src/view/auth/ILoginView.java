package view.auth;

import javax.swing.JButton;

/**
 * Contrato mínimo que {@link controller.LoginController} necesita de la
 * vista de inicio de sesión.
 *
 * <p><b>Segregación de Interfaces (I de SOLID):</b> mismo rol que
 * {@link IRegistroUsuarioView} para {@code UsuarioController}: solo declara
 * lo que el controlador realmente usa.</p>
 *
 * <p><b>Inversión de Dependencias (D de SOLID):</b> el controlador depende
 * de esta abstracción, nunca de {@link PanelLogin} directamente.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.1
 */
public interface ILoginView {

    /** @return correo digitado, sin espacios sobrantes */
    String getCorreo();

    /** @return contraseña digitada */
    String getPassword();

    /** @return botón principal, para que el controlador le registre su listener */
    JButton getBtnIniciarSesion();

    /**
     * Muestra un mensaje de error (credenciales inválidas o campos vacíos).
     *
     * @param mensaje texto a publicar
     */
    void mostrarError(String mensaje);

    /**
     * Confirma al usuario que sus credenciales fueron aceptadas.
     *
     * @param mensaje mensaje de confirmación, ya resuelto por el controlador
     */
    void mostrarExito(String mensaje);

    /** Vacía los campos del formulario. */
    void limpiarFormulario();

    /**
     * Bloquea la entrada de credenciales durante un tiempo y muestra la
     * cuenta regresiva; al terminar, reactiva los componentes y ejecuta
     * {@code alDesbloquear}.
     *
     * <p>El controlador decide <em>cuándo</em> y <em>por cuánto</em> hay que
     * bloquear (es una regla de seguridad); la vista decide <em>cómo</em> se
     * ve el bloqueo y lleva la cuenta atrás, que es trabajo de interfaz. El
     * método no bloquea el hilo: devuelve el control de inmediato y avisa por
     * el {@code Runnable}.</p>
     *
     * @param segundos      duración del bloqueo
     * @param alDesbloquear acción a ejecutar al liberarse (reiniciar el conteo)
     */
    void bloquearIngreso(int segundos, Runnable alDesbloquear);

    /**
     * Acompaña con una transición visible el salto del Login al panel
     * principal y, al terminar, ejecuta {@code alSiguientePaso}.
     *
     * <p>El controlador entrega <em>qué</em> hacer después (cerrar esta
     * ventana y abrir el dashboard) sin saber <em>cómo</em> se ve la espera:
     * eso lo decide la vista, igual que decide cómo se ve un error o una
     * confirmación. El método no bloquea; la acción llega por el
     * {@code Runnable} cuando la transición acaba.</p>
     *
     * @param alSiguientePaso acción a ejecutar al cerrarse la transición
     */
    void mostrarTransicion(Runnable alSiguientePaso);

    /**
     * Ejecuta un trabajo lento fuera del hilo de eventos y continúa después.
     *
     * <p>Mismo reparto que {@link #mostrarTransicion(Runnable)}: el controlador
     * dice <em>qué</em> es lento y qué hacer al terminar; la vista decide
     * <em>cómo</em> se ve la espera. Sin esto, una consulta a MongoDB Atlas
     * correría en el hilo que pinta la ventana y la dejaría congelada mientras
     * viaja por la red.</p>
     *
     * @param <T>        lo que el trabajo devuelve al terminar
     * @param mensaje    qué se está haciendo, para el indicador
     * @param tarea      trabajo lento; corre FUERA del hilo de eventos, así que
     *                   no debe tocar componentes Swing
     * @param alTerminar qué hacer con el resultado, ya de vuelta en el hilo de
     *                   eventos, que es el único desde el que se puede pintar
     */
    <T> void ejecutarEnSegundoPlano(String mensaje,
                                    java.util.function.Supplier<T> tarea,
                                    java.util.function.Consumer<T> alTerminar);

    /**
     * Muestra el botón "Continuar con Google" y le conecta su acción.
     *
     * <p>El botón nace oculto: solo aparece si alguien llama a este método,
     * y eso solo ocurre cuando {@code config.properties} trae las credenciales
     * de Google. Sin ellas, un botón que siempre fallara sería peor que no
     * tenerlo.</p>
     *
     * @param accion qué hacer al pulsarlo
     */
    void activarAccesoGoogle(Runnable accion);

    /**
     * Espera, fuera del hilo de eventos, algo que depende del usuario (como
     * terminar el acceso en el navegador), con un botón para abandonarla.
     *
     * <p>Mismo reparto que {@link #ejecutarEnSegundoPlano}: el controlador
     * dice qué se espera y qué hacer después; la vista, cómo se ve. Si la
     * tarea falla, su mensaje se muestra en la alerta del formulario.</p>
     *
     * @param <T>        lo que devuelve la tarea
     * @param mensaje    qué se está esperando
     * @param tarea      espera bloqueante; corre FUERA del hilo de eventos
     * @param alTerminar qué hacer con el resultado, ya en el hilo de eventos
     * @param alCancelar cómo pedirle a la tarea que termine si el usuario
     *                   pulsa "Cancelar"
     */
    <T> void ejecutarCancelable(String mensaje,
                                java.util.function.Supplier<T> tarea,
                                java.util.function.Consumer<T> alTerminar,
                                Runnable alCancelar);

    /**
     * Pregunta a quien entra por primera vez con Google qué rol tendrá.
     *
     * @param nombre nombre de la persona, para el saludo
     * @return {@link IRegistroUsuarioView#TIPO_CLIENTE},
     *         {@link IRegistroUsuarioView#TIPO_PROVEEDOR}, o {@code null} si
     *         no eligió ninguno
     */
    String elegirRol(String nombre);

    /**
     * Cierra (destruye, {@code dispose()}) la ventana que aloja esta vista.
     * Se usa tras un login exitoso, justo antes de abrir el panel principal
     * del rol correspondiente, para no acumular ventanas: el controlador la
     * invoca sin saber que existe un {@code JFrame} detrás.
     */
    void cerrarVentana();
}
