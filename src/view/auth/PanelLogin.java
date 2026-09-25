package view.auth;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import view.core.INavegador;
import view.core.MainFrame;
import view.factory.IComponentesFactory;
import view.factory.components.CampoPasswordConToggle;
import view.factory.components.CampoTextoConIcono;
import view.factory.icons.IconoCampo;

/**
 * Tarjeta de {@link MainFrame} con el formulario de inicio de sesión.
 *
 * <p><b>Responsabilidad única:</b> igual que {@link PanelRegistroUsuario},
 * solo compone el formulario delegando en {@link IComponentesFactory}; no
 * valida credenciales ni conoce el repositorio (eso es de
 * {@link controller.LoginController}).</p>
 *
 * <p>No repite el logo: {@link MainFrame} ya lo muestra una sola vez en el
 * sidebar. Aquí solo hay un título y la tarjeta con los campos, centrados en
 * el espacio amplio del panel central.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 2.0
 */
public class PanelLogin extends JPanel implements ILoginView {

    /** Nombre de esta carta dentro del {@link CardLayout} de {@link MainFrame}. */
    public static final String NOMBRE_CARTA = "login";

    private final IComponentesFactory fabrica;
    private final INavegador navegador;

    private CampoTextoConIcono txtCorreo;
    private CampoPasswordConToggle campoPassword;
    private JButton btnIniciarSesion;
    private JLabel lblMensaje;

    /** Contador interno de filas del formulario. */
    private int fila = 0;

    /**
     * @param fabrica   fábrica de la que se toman todos los componentes del formulario
     * @param navegador usado por el enlace "¿No tienes cuenta? Regístrate"
     */
    public PanelLogin(IComponentesFactory fabrica, INavegador navegador) {
        super(new GridBagLayout());
        this.fabrica = fabrica;
        this.navegador = navegador;
        setBackground(fabrica.colorFondo());
        add(construirContenido(), new GridBagConstraints());
    }

    // ---------------------------------------------------------------------
    // Construcción de la interfaz
    // ---------------------------------------------------------------------

    /**
     * Columna con el título y la tarjeta del formulario. Se agrega al panel
     * (que tiene {@link GridBagLayout} sin peso) con restricciones por
     * defecto, lo que la centra automáticamente dentro del espacio amplio
     * que le da {@link MainFrame} en vez de estirarla a todo el ancho.
     */
    private JPanel construirContenido() {
        JPanel columna = new JPanel();
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setOpaque(false);

        JLabel lblTitulo = new JLabel("Iniciar Sesión");
        lblTitulo.setFont(fabrica.fuente(Font.BOLD, 26));
        lblTitulo.setForeground(fabrica.colorTexto());
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = crearTextoAjustado("Ingresa tus credenciales para continuar", 13);
        lblSubtitulo.setBorder(new EmptyBorder(6, 0, 24, 0));

        JPanel tarjeta = construirTarjeta();
        tarjeta.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblAyuda = crearTextoAjustado("¿Problemas? Revisa tu correo y contraseña.", 12);
        lblAyuda.setBorder(new EmptyBorder(18, 0, 0, 0));

        columna.add(lblTitulo);
        columna.add(lblSubtitulo);
        columna.add(tarjeta);
        columna.add(lblAyuda);
        return columna;
    }

    /** Tarjeta con los campos del formulario y el botón de acceso. */
    private JPanel construirTarjeta() {
        JPanel tarjeta = fabrica.crearTarjeta(new GridBagLayout());
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                tarjeta.getBorder(), new EmptyBorder(28, 32, 28, 32)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        txtCorreo = fabrica.crearCampoTexto("correo@empresa.com", IconoCampo.Tipo.CORREO);
        campoPassword = fabrica.crearCampoPassword("Tu contraseña");

        agregarFila(tarjeta, gbc, fabrica.crearEtiqueta("Correo electrónico"), txtCorreo);
        agregarFila(tarjeta, gbc, fabrica.crearEtiqueta("Contraseña"), campoPassword);

        btnIniciarSesion = fabrica.crearBotonPrimario("INICIAR SESIÓN");
        gbc.gridy = fila++;
        gbc.insets = new Insets(6, 0, 0, 0);
        tarjeta.add(btnIniciarSesion, gbc);

        lblMensaje = fabrica.crearEtiqueta(" ");
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = fila++;
        gbc.insets = new Insets(6, 0, 0, 0);
        tarjeta.add(lblMensaje, gbc);

        JLabel enlaceRegistro = fabrica.crearEnlaceSecundario("¿No tienes cuenta? Regístrate");
        enlaceRegistro.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navegador.mostrarCarta(PanelRegistroUsuario.NOMBRE_CARTA);
            }
        });
        gbc.gridy = fila++;
        gbc.insets = new Insets(10, 0, 0, 0);
        tarjeta.add(enlaceRegistro, gbc);

        return tarjeta;
    }

    /**
     * Crea una etiqueta secundaria centrada (subtítulo o texto de ayuda) con
     * el estilo del tema. Los textos se mantienen cortos a propósito: si una
     * frase no cabe a lo ancho, el layout la recorta en vez de repartirla en
     * dos líneas (ver la nota equivalente en {@link PanelRegistroUsuario}).
     */
    private JLabel crearTextoAjustado(String texto, int tamanoFuente) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fabrica.fuente(Font.PLAIN, tamanoFuente));
        etiqueta.setForeground(fabrica.colorTextoSuave());
        etiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);
        return etiqueta;
    }

    /** Agrega al formulario una etiqueta y, debajo, su campo correspondiente. */
    private void agregarFila(JPanel panel, GridBagConstraints gbc, JLabel etiqueta, Component campo) {
        gbc.gridy = fila++;
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(etiqueta, gbc);

        gbc.gridy = fila++;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(campo, gbc);
    }

    // ---------------------------------------------------------------------
    // ILoginView
    // ---------------------------------------------------------------------

    @Override
    public String getCorreo() {
        return txtCorreo.getTexto();
    }

    @Override
    public String getPassword() {
        return campoPassword.getPassword();
    }

    @Override
    public JButton getBtnIniciarSesion() {
        return btnIniciarSesion;
    }

    @Override
    public void mostrarError(String mensaje) {
        lblMensaje.setForeground(fabrica.colorError());
        lblMensaje.setText(mensaje);
    }

    @Override
    public void mostrarExito(String mensaje) {
        fabrica.mostrarDialogoExito(this, mensaje);
    }

    @Override
    public void limpiarFormulario() {
        txtCorreo.limpiar();
        campoPassword.limpiar();
        lblMensaje.setText(" ");
        txtCorreo.enfocar();
    }

    @Override
    public void bloquearIngreso(int segundos, Runnable alDesbloquear) {
        habilitarEntradas(false);
        mostrarCuentaRegresiva(segundos);

        // El Timer late una vez por segundo porque lo que se muestra son
        // segundos: refrescar más seguido solo gastaría repintados.
        int[] restante = {segundos};
        Timer cuentaAtras = new Timer(1000, null);
        cuentaAtras.addActionListener(e -> {
            restante[0]--;
            if (restante[0] > 0) {
                mostrarCuentaRegresiva(restante[0]);
                return;
            }
            ((Timer) e.getSource()).stop();
            habilitarEntradas(true);
            lblMensaje.setText(" ");
            txtCorreo.enfocar();
            alDesbloquear.run();
        });
        cuentaAtras.start();
    }

    /** Activa o desactiva todo lo que permite intentar un acceso. */
    private void habilitarEntradas(boolean habilitado) {
        txtCorreo.setEnabled(habilitado);
        campoPassword.setEnabled(habilitado);
        btnIniciarSesion.setEnabled(habilitado);
    }

    private void mostrarCuentaRegresiva(int segundos) {
        lblMensaje.setForeground(fabrica.colorError());
        lblMensaje.setText("Demasiados intentos. Espera " + segundos + " s.");
    }

    @Override
    public void mostrarTransicion(Runnable alSiguientePaso) {
        fabrica.mostrarTransicion(this, "Iniciando sesión...", alSiguientePaso);
    }

    @Override
    public void cerrarVentana() {
        Window ventana = SwingUtilities.getWindowAncestor(this);
        if (ventana != null) {
            ventana.dispose();
        }
    }
}
