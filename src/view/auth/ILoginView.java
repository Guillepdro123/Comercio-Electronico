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
     * Cierra (destruye, {@code dispose()}) la ventana que aloja esta vista.
     * Se usa tras un login exitoso, justo antes de abrir el panel principal
     * del rol correspondiente, para no acumular ventanas: el controlador la
     * invoca sin saber que existe un {@code JFrame} detrás.
     */
    void cerrarVentana();
}
