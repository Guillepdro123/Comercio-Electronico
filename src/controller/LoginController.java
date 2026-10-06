package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import aplicacion.acceso.AutenticacionService;
import aplicacion.acceso.ResultadoAcceso;
import view.auth.ILoginView;

/**
 * Controlador de la pantalla de acceso: recoge las credenciales, se las
 * entrega al caso de uso y traduce el resultado a lo que la vista sabe hacer.
 *
 * <p><b>Aquí no hay reglas.</b> Cuántos fallos se toleran, cuánto dura el
 * bloqueo, cómo se compara una contraseña y qué mensaje corresponde a cada
 * caso son de {@link AutenticacionService}. Lo que queda es decidir a qué
 * método de la vista va cada estado.</p>
 *
 * <p><b>El reparto con la vista no cambia:</b> el controlador dice cuánto hay
 * que bloquear y qué hacer al liberarse, y la vista decide cómo se ve la
 * cuenta atrás y lleva su temporizador. Igual con la transición al panel del
 * rol, que la vista ejecuta cuando termina su animación.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 2.0
 */
public class LoginController implements ActionListener {

    private final ILoginView vista;
    private final AutenticacionService autenticacion;
    private final SesionController sesion;

    /**
     * Conecta la vista con el caso de uso y queda a la escucha del botón.
     *
     * @param vista         vista de login (abstracción)
     * @param autenticacion caso de uso del acceso; lleva el contador de
     *                      intentos de esta pantalla
     * @param sesion        quien abre el panel del rol y recuerda la sesión
     */
    public LoginController(ILoginView vista, AutenticacionService autenticacion,
                           SesionController sesion) {
        this.vista = vista;
        this.autenticacion = autenticacion;
        this.sesion = sesion;
        this.vista.getBtnIniciarSesion().addActionListener(this);
    }

    /**
     * Punto de entrada del evento del botón "Iniciar sesión".
     *
     * @param e evento disparado por Swing
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.getBtnIniciarSesion()) {
            iniciarSesion();
        }
    }

    /**
     * Lee el formulario y comprueba las credenciales en segundo plano.
     *
     * <p>Buscar la cuenta y comparar con BCrypt es lo lento de este caso de
     * uso: con MongoDB Atlas detrás viaja por red. Va fuera del hilo de
     * eventos para que la ventana no se congele mientras responde.</p>
     */
    private void iniciarSesion() {
        String correo = vista.getCorreo();
        String password = vista.getPassword();
        vista.ejecutarEnSegundoPlano("Verificando tus credenciales...",
                () -> autenticacion.intentar(correo, password), this::mostrarResultado);
    }

    /** Lleva cada estado a su forma de mostrarse; ya en el hilo de eventos. */
    private void mostrarResultado(ResultadoAcceso resultado) {
        switch (resultado.estado()) {
            case ACEPTADO -> entrar(resultado);
            // La vista decide cómo se ve el bloqueo y lleva la cuenta atrás;
            // al terminar, avisa al servicio para que libere el contador.
            case BLOQUEADO -> vista.bloquearIngreso(resultado.segundos(),
                    autenticacion::liberarBloqueo);
            default -> vista.mostrarError(resultado.mensaje());
        }
    }

    /**
     * Confirma el acceso y salta al panel del rol.
     *
     * <p>El cierre del Login y la apertura del panel viajan como acción
     * diferida: la vista los ejecuta cuando termina su transición.</p>
     */
    private void entrar(ResultadoAcceso resultado) {
        vista.mostrarExito("¡Bienvenido, " + resultado.usuario().getNombres()
                + "! Has ingresado a tu cuenta correctamente.");
        vista.mostrarTransicion(() -> {
            vista.cerrarVentana();
            sesion.abrir(resultado.usuario());
        });
    }
}
