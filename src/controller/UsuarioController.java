package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import aplicacion.cuenta.CuentaService;
import aplicacion.cuenta.ResultadoCuenta;
import aplicacion.cuenta.SolicitudRegistro;
import aplicacion.seguridad.PoliticaPassword;
import view.auth.IRegistroUsuarioView;
import view.auth.PanelLogin;
import view.core.INavegador;

/**
 * Controlador de la pantalla de registro: recoge lo que se escribió, se lo
 * entrega al caso de uso y muestra el resultado.
 *
 * <p><b>Aquí no hay reglas de negocio.</b> Qué datos son obligatorios, qué
 * formato debe tener el correo, el cifrado de la contraseña, la unicidad del
 * correo y el aviso de bienvenida son de {@link CuentaService}. Lo que queda
 * es leer la vista, pedir el caso de uso y contar cómo salió.</p>
 *
 * <p><b>El semáforo de fortaleza sí se queda.</b> Es la misma regla que
 * aplicará el registro ({@link PoliticaPassword}), pero traducida a las
 * constantes que entiende la vista: lo que el semáforo pinta en rojo es justo
 * lo que el servicio va a rechazar.</p>
 *
 * <p><b>Auto-redirección a Login:</b> como {@code mostrarExito(...)} es un
 * {@code JOptionPane} modal, el código que sigue a esa llamada solo se ejecuta
 * cuando el usuario cierra el diálogo. Justo ahí se pide a {@link INavegador}
 * la carta de Login.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 3.0
 */
public class UsuarioController implements ActionListener {

    /** Reglas de la contraseña: aquí solo se usan para pintar el semáforo. */
    private final PoliticaPassword politicaPassword = new PoliticaPassword();

    private final IRegistroUsuarioView vista;
    private final CuentaService cuentas;
    private final INavegador navegador;

    /**
     * Conecta la vista con el caso de uso y queda a la escucha del botón.
     *
     * @param vista     vista de registro (abstracción)
     * @param cuentas   caso de uso de alta de cuentas
     * @param navegador usado para volver a la carta de Login tras un registro
     *                  exitoso
     */
    public UsuarioController(IRegistroUsuarioView vista, CuentaService cuentas,
                             INavegador navegador) {
        this.vista = vista;
        this.cuentas = cuentas;
        this.navegador = navegador;
        this.vista.getBtnRegistrar().addActionListener(this);
        this.vista.observarPassword(this::actualizarFortalezaPassword);
    }

    /** Traduce la contraseña actual al semáforo que muestra la vista. */
    private void actualizarFortalezaPassword() {
        String password = vista.getPassword();
        if (password.isEmpty()) {
            vista.ocultarFortalezaPassword();
            return;
        }
        PoliticaPassword.Nivel nivel = politicaPassword.evaluar(password);
        vista.mostrarFortalezaPassword(aNivelDeVista(nivel), nivel.getDescripcion());
    }

    /** Pasa del enum de la política a la constante que entiende la vista. */
    private int aNivelDeVista(PoliticaPassword.Nivel nivel) {
        switch (nivel) {
            case FUERTE:
                return IRegistroUsuarioView.FORTALEZA_FUERTE;
            case MEDIA:
                return IRegistroUsuarioView.FORTALEZA_MEDIA;
            default:
                return IRegistroUsuarioView.FORTALEZA_DEBIL;
        }
    }

    /**
     * Punto de entrada del evento del botón "Registrar usuario".
     *
     * @param e evento disparado por Swing
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.getBtnRegistrar()) {
            registrarUsuario();
        }
    }

    // ---------------------------------------------------------------------
    // Flujo de la pantalla
    // ---------------------------------------------------------------------

    /**
     * Lee el formulario y lanza el registro en segundo plano.
     *
     * <p>Todo lo que se lee de la vista se lee aquí, en el hilo de eventos:
     * el trabajo de fondo no puede tocar componentes Swing.</p>
     */
    private void registrarUsuario() {
        SolicitudRegistro solicitud = new SolicitudRegistro(
                vista.getIdentificacion(),
                vista.getNombres(),
                vista.getCorreo(),
                vista.getPassword(),
                vista.getDatoAdicional(),
                vista.getTipoCuentaSeleccionado(),
                vista.getNombreEmpresa());

        // Validar, comprobar el correo, cifrar y guardar viajan juntos al
        // servicio: con MongoDB Atlas detrás son viajes por red, y BCrypt es
        // lento a propósito; en el hilo que pinta la ventana la congelarían.
        vista.ejecutarEnSegundoPlano("Creando tu cuenta...",
                () -> cuentas.registrar(solicitud), this::mostrarResultado);
    }

    /** Cuenta cómo salió el registro; ya en el hilo de eventos. */
    private void mostrarResultado(ResultadoCuenta resultado) {
        if (!resultado.exitoso()) {
            vista.mostrarError(resultado.error());
            return;
        }
        vista.limpiarFormulario();
        // El mensaje lo aporta la propia entidad: el controlador no necesita
        // preguntar si es Cliente o Proveedor.
        vista.mostrarExito(resultado.usuario().getMensajeDesbloqueo());
        navegador.mostrarCarta(PanelLogin.NOMBRE_CARTA);
    }
}
