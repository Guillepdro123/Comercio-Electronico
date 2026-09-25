package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.entity.Cliente;
import model.entity.Proveedor;
import model.entity.Usuario;
import model.repository.IUsuarioRepository;
import view.auth.IRegistroUsuarioView;
import view.auth.PanelLogin;
import view.core.INavegador;

/**
 * Controlador del caso de uso "Registrar usuario".
 *
 * <p>Es el único punto donde se cruzan la vista y el modelo: lee los datos de
 * la interfaz, los valida, decide qué subclase instanciar y delega la
 * persistencia al repositorio. La vista no sabe qué es un {@code Cliente} y el
 * repositorio no sabe qué es un {@code JTextField}.</p>
 *
 * <p><b>Inversión de dependencias:</b> los atributos se declaran de los
 * tipos {@link IRegistroUsuarioView} e {@link INavegador}, nunca de una
 * implementación concreta de Swing; así el formulario se puede rediseñar
 * por completo sin tocar esta clase.</p>
 *
 * <p><b>Retroalimentación de éxito:</b> el controlador no sabe qué es un
 * {@code JOptionPane}. Se limita a llamar
 * {@link IRegistroUsuarioView#mostrarExito(String)} con el mensaje que la
 * propia entidad registrada le entrega; es la vista quien decide cómo
 * mostrarlo.</p>
 *
 * <p><b>Auto-redirección a Login:</b> como {@code mostrarExito(...)} es un
 * {@code JOptionPane} modal, el código que sigue a esa llamada solo se
 * ejecuta cuando el usuario cierra el diálogo. Justo ahí, este controlador
 * pide a {@link INavegador} que muestre la carta de Login
 * (`PanelLogin.NOMBRE_CARTA`) — reutiliza el mismo {@code mostrarCarta(...)}
 * genérico que ya usan los enlaces internos de las vistas, sin necesidad de
 * un método nuevo en la interfaz.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 2.2
 */
public class UsuarioController implements ActionListener {

    /** Expresión regular básica para validar el formato del correo. */
    private static final String PATRON_CORREO = "^[\\w.+-]+@[\\w-]+\\.[\\w.-]{2,}$";

    /** Reglas de la contraseña: qué se acepta y qué tan fuerte es. */
    private final PoliticaPassword politicaPassword = new PoliticaPassword();

    private final IRegistroUsuarioView vista;
    private final IUsuarioRepository repositorio;
    private final INavegador navegador;

    /**
     * Conecta la vista con el repositorio y el navegador, y queda a la
     * escucha del botón.
     *
     * @param vista       vista de registro (abstracción)
     * @param repositorio almacén de usuarios (abstracción)
     * @param navegador   usado para volver a la carta de Login tras un registro exitoso
     */
    public UsuarioController(IRegistroUsuarioView vista, IUsuarioRepository repositorio, INavegador navegador) {
        this.vista = vista;
        this.repositorio = repositorio;
        this.navegador = navegador;
        this.vista.getBtnRegistrar().addActionListener(this);
        // Evaluar la contraseña mientras se escribe es la misma regla que se
        // aplicará al pulsar "Registrar", así que la decide el controlador:
        // lo que el semáforo pinta en rojo es justo lo que se va a rechazar.
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
    // Flujo del caso de uso
    // ---------------------------------------------------------------------

    /** Valida, construye y persiste el usuario capturado en el formulario. */
    private void registrarUsuario() {
        String identificacion = vista.getIdentificacion();
        String nombres = vista.getNombres();
        String correo = vista.getCorreo();
        String password = vista.getPassword();
        String datoAdicional = vista.getDatoAdicional();
        String tipoCuenta = vista.getTipoCuentaSeleccionado();

        String error = validar(identificacion, nombres, correo, password, datoAdicional, tipoCuenta);
        if (error != null) {
            vista.mostrarError(error);
            return;
        }

        // Se pregunta antes de construir la entidad para poder dar el mensaje
        // exacto; el repositorio vuelve a rechazarlo de todos modos, así que
        // la regla no depende de que este controlador se acuerde de ella.
        if (repositorio.buscarPorCorreo(correo) != null) {
            vista.mostrarError("Este correo ya está registrado en el sistema.");
            return;
        }

        // Polimorfismo: la variable es del tipo base, el objeto es de la
        // subclase concreta que corresponde a la selección del combo.
        Usuario usuario = construirUsuario(tipoCuenta, identificacion, nombres,
                correo, password, datoAdicional);

        boolean registrado = repositorio.registrar(usuario);

        if (registrado) {
            vista.limpiarFormulario();
            // El mensaje lo aporta la propia entidad: el controlador no
            // necesita preguntar si es Cliente o Proveedor.
            vista.mostrarExito(usuario.getMensajeDesbloqueo());
            // mostrarExito(...) es un JOptionPane modal: esta línea solo se
            // ejecuta cuando el usuario cierra el diálogo.
            navegador.mostrarCarta(PanelLogin.NOMBRE_CARTA);
        } else {
            vista.mostrarError("No fue posible registrar: ya existe un usuario con la identificación "
                    + identificacion + ".");
        }
    }

    /**
     * Fábrica de entidades según el tipo de cuenta seleccionado.
     *
     * @param tipoCuenta      "Cliente" o "Proveedor"
     * @param identificacion  documento del usuario
     * @param nombres         nombres y apellidos
     * @param correo          correo electrónico
     * @param password        contraseña
     * @param datoAdicional   dirección de envío o NIT, según el tipo
     * @return instancia concreta referenciada como {@link Usuario}
     */
    private Usuario construirUsuario(String tipoCuenta, String identificacion, String nombres,
                                     String correo, String password, String datoAdicional) {
        if (IRegistroUsuarioView.TIPO_PROVEEDOR.equals(tipoCuenta)) {
            return new Proveedor(identificacion, nombres, correo, password, datoAdicional);
        }
        return new Cliente(identificacion, nombres, correo, password, datoAdicional);
    }

    /**
     * Verifica que los datos capturados sean utilizables.
     *
     * @return mensaje de error listo para mostrar, o {@code null} si todo es válido
     */
    private String validar(String identificacion, String nombres, String correo,
                           String password, String datoAdicional, String tipoCuenta) {
        if (identificacion.isEmpty()) {
            return "La identificación es obligatoria.";
        }
        if (!identificacion.matches("\\d+")) {
            return "La identificación debe contener solo números.";
        }
        if (nombres.isEmpty()) {
            return "Los nombres son obligatorios.";
        }
        if (correo.isEmpty()) {
            return "El correo electrónico es obligatorio.";
        }
        if (!correo.matches(PATRON_CORREO)) {
            return "El formato del correo no es válido. Ejemplo: usuario@dominio.com";
        }
        String errorPassword = politicaPassword.validar(password);
        if (errorPassword != null) {
            return errorPassword;
        }
        if (datoAdicional.isEmpty()) {
            return IRegistroUsuarioView.TIPO_PROVEEDOR.equals(tipoCuenta)
                    ? "El NIT de la empresa es obligatorio."
                    : "La dirección de envío es obligatoria.";
        }
        return null;
    }
}
