package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import app.Main;
import model.entity.Usuario;
import model.repository.IUsuarioRepository;
import view.auth.ILoginView;
import view.auth.IRegistroUsuarioView;
import view.dashboard.ClientDashboardFrame;
import view.dashboard.ProviderDashboardFrame;
import view.factory.IComponentesFactory;

/**
 * Controlador del caso de uso "Iniciar sesión".
 *
 * <p>Mismo patrón que {@link UsuarioController}: lee los datos de la
 * interfaz, valida, consulta el repositorio y decide qué hacer con el
 * resultado. La vista no sabe cómo se valida una credencial; el repositorio
 * no sabe qué es un {@code JPasswordField}.</p>
 *
 * <p><b>Inversión de dependencias:</b> depende de {@link ILoginView},
 * {@link IUsuarioRepository} e {@link IComponentesFactory} — abstracciones,
 * ninguna implementación concreta salvo las dos ventanas de destino, que se
 * abren directamente porque este es, literalmente, el caso de uso que decide
 * a cuál ir (y, por la misma razón, el único punto que conoce
 * {@code app.Main.mostrarVentanaPrincipal(...)} para reabrirla al cerrar
 * sesión).</p>
 *
 * <p><b>Ciclo de vida estricto de ventanas:</b> tras un login exitoso, este
 * controlador cierra (a través de {@link ILoginView#cerrarVentana()}, nunca
 * llamando {@code dispose()} él mismo) la ventana de Login/Registro antes de
 * abrir el dashboard, para no dejar ventanas huérfanas acumulándose en
 * memoria.</p>
 *
 * <p><b>Bifurcación por rol sin {@code instanceof}:</b> igual que
 * {@code UsuarioController.construirUsuario}, la decisión de qué ventana
 * abrir se apoya en {@link Usuario#getTipoCuenta()} (polimorfismo), no en el
 * tipo concreto de la instancia.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.2
 */
public class LoginController implements ActionListener {

    private final ILoginView vista;
    private final IUsuarioRepository repositorio;
    private final IComponentesFactory fabrica;

    /** Política de intentos fallidos; ver {@link ControlIntentosFallidos}. */
    private final ControlIntentosFallidos intentos = new ControlIntentosFallidos();

    /**
     * Conecta la vista con el repositorio y la fábrica, y queda a la
     * escucha del botón.
     *
     * @param vista       vista de login (abstracción)
     * @param repositorio almacén de usuarios (abstracción)
     * @param fabrica     usada para construir la ventana de destino con el
     *                    mismo tema visual que el resto de la aplicación
     */
    public LoginController(ILoginView vista, IUsuarioRepository repositorio, IComponentesFactory fabrica) {
        this.vista = vista;
        this.repositorio = repositorio;
        this.fabrica = fabrica;
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

    // ---------------------------------------------------------------------
    // Flujo del caso de uso
    // ---------------------------------------------------------------------

    /** Valida las credenciales capturadas y, si son correctas, abre el panel según el rol. */
    private void iniciarSesion() {
        String correo = vista.getCorreo();
        String password = vista.getPassword();

        if (correo.isEmpty() || password.isEmpty()) {
            vista.mostrarError("Ingresa tu correo y tu contraseña.");
            return;
        }

        Usuario usuario = repositorio.buscarPorCorreo(correo);
        if (usuario == null || !usuario.getPassword().equals(password)) {
            registrarCredencialInvalida();
            return;
        }

        // Un acceso correcto limpia el historial: solo interesa frenar los
        // fallos seguidos, no penalizar un error aislado de tecleo.
        intentos.reiniciar();
        vista.mostrarExito("¡Bienvenido, " + usuario.getNombres()
                + "! Has ingresado a tu cuenta correctamente.");
        // El cierre del Login y la apertura del dashboard viajan como acción
        // diferida: la vista los ejecuta cuando termina su transición. Así el
        // controlador sigue decidiendo QUÉ pasa después de autenticar, sin
        // saber nada de cómo se anima ese salto.
        vista.mostrarTransicion(() -> {
            vista.cerrarVentana();
            abrirPanelPrincipal(usuario);
        });
    }

    /**
     * Anota el fallo y decide qué corresponde: avisar cuántos intentos
     * quedan o, si ya se agotaron, pedirle a la vista que bloquee el acceso
     * durante el tiempo que marca la política.
     *
     * <p>El controlador no sabe cómo se ve ese bloqueo ni lleva la cuenta
     * atrás: le pasa la duración y qué hacer al liberarse.</p>
     */
    private void registrarCredencialInvalida() {
        intentos.registrarFallo();

        if (intentos.limiteAlcanzado()) {
            vista.bloquearIngreso(ControlIntentosFallidos.SEGUNDOS_BLOQUEO, intentos::reiniciar);
            return;
        }

        int restantes = intentos.intentosRestantes();
        vista.mostrarError("Correo o contraseña incorrectos. Te "
                + (restantes == 1 ? "queda 1 intento." : "quedan " + restantes + " intentos."));
    }

    /**
     * Abre, centrada, la ventana de trabajo que corresponde al rol del
     * usuario autenticado. La vista de destino no recibe el {@code Usuario}
     * (la vista no conoce el modelo): solo su nombre, ya extraído aquí. Le
     * entrega además la acción de "cerrar sesión": volver a ensamblar una
     * ventana principal nueva, con el mismo repositorio y la misma fábrica
     * (para no perder los usuarios ya registrados).
     *
     * @param usuario usuario ya autenticado
     */
    private void abrirPanelPrincipal(Usuario usuario) {
        String nombre = usuario.getNombres();
        Runnable alCerrarSesion = () -> Main.mostrarVentanaPrincipal(repositorio, fabrica);

        JFrame panelPrincipal = IRegistroUsuarioView.TIPO_PROVEEDOR.equals(usuario.getTipoCuenta())
                ? new ProviderDashboardFrame(fabrica, nombre, alCerrarSesion)
                : new ClientDashboardFrame(fabrica, nombre, alCerrarSesion);
        panelPrincipal.setLocationRelativeTo(null);
        panelPrincipal.setVisible(true);
    }
}
