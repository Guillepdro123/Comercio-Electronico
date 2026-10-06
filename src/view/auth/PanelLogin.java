package view.auth;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import net.miginfocom.swing.MigLayout;
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
    private JLabel separadorGoogle;
    private JButton btnGoogle;
    private JLabel lblMensaje;

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
        // MigLayout para el interior de la tarjeta, igual que en Registro: dos
        // pares etiqueta/campo en una sola columna. La columna se declara una
        // vez ("[grow,fill]") y cada componente solo dice su separación.
        JPanel tarjeta = fabrica.crearTarjeta(
                new MigLayout("wrap 1, insets 0, gapy 0, fillx", "[grow,fill]"));
        // La fábrica ya le puso fondo y borde; se combina con el relleno
        // interno en vez de reemplazar el borde con setBorder(...).
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                tarjeta.getBorder(), new EmptyBorder(20, 28, 16, 28)));

        txtCorreo = fabrica.crearCampoTexto("correo@empresa.com", IconoCampo.Tipo.CORREO);
        campoPassword = fabrica.crearCampoPassword("Tu contraseña");

        agregarFila(tarjeta, fabrica.crearEtiqueta("Correo electrónico"), txtCorreo);
        agregarFila(tarjeta, fabrica.crearEtiqueta("Contraseña"), campoPassword);

        btnIniciarSesion = fabrica.crearBotonPrimario("INICIAR SESIÓN");
        tarjeta.add(btnIniciarSesion, "gaptop 6");

        lblMensaje = fabrica.crearAlerta();
        tarjeta.add(lblMensaje, "gaptop 6");

        // Acceso con Google: nace oculto y solo aparece si hay credenciales
        // (ver activarAccesoGoogle). "hidemode 3" hace que, oculto, no ocupe
        // sitio: sin él MigLayout reservaría el hueco y la tarjeta tendría un
        // vacío sin explicación.
        separadorGoogle = new JLabel("o", SwingConstants.CENTER);
        separadorGoogle.setFont(fabrica.fuente(Font.PLAIN, 12));
        separadorGoogle.setForeground(fabrica.colorTextoSuave());
        separadorGoogle.setVisible(false);
        tarjeta.add(separadorGoogle, "gaptop 2, hidemode 3");

        btnGoogle = fabrica.crearBotonGoogle("Continuar con Google");
        btnGoogle.setVisible(false);
        tarjeta.add(btnGoogle, "gaptop 6, hidemode 3");

        JLabel enlaceRegistro = fabrica.crearEnlaceSecundario("¿No tienes cuenta? Regístrate");
        // 'mousePressed' y no 'mouseClicked': el segundo no salta si el ratón
        // se desplaza un píxel entre pulsar y soltar, y el enlace se quedaba
        // sin responder de vez en cuando.
        enlaceRegistro.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                navegador.mostrarCarta(PanelRegistroUsuario.NOMBRE_CARTA);
            }
        });
        tarjeta.add(enlaceRegistro, "gaptop 10");

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
    /**
     * Agrega al formulario una etiqueta y, debajo, su campo.
     *
     * <p>Las separaciones son las mismas que tenía con
     * {@code GridBagConstraints} (4px bajo la etiqueta, 10px bajo el campo).</p>
     *
     * @param panel    tarjeta del formulario, con {@link MigLayout} en una columna
     * @param etiqueta rótulo de la fila
     * @param campo    control de captura
     */
    private void agregarFila(JPanel panel, JLabel etiqueta, Component campo) {
        panel.add(etiqueta, "gapbottom 4");
        panel.add(campo, "gapbottom 10");
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
        fabrica.pintarAlerta(lblMensaje, mensaje, true);
    }

    @Override
    public void mostrarExito(String mensaje) {
        fabrica.mostrarDialogoExito(this, mensaje);
    }

    @Override
    public void limpiarFormulario() {
        txtCorreo.limpiar();
        campoPassword.limpiar();
        fabrica.pintarAlerta(lblMensaje, null, true);
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
            fabrica.pintarAlerta(lblMensaje, null, true);
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

    /**
     * Pinta la cuenta atrás del bloqueo.
     *
     * <p>Va como <em>aviso</em> y no como error: el usuario no se ha
     * equivocado ahora, está esperando a que se libere el acceso. El texto y
     * los tiempos los sigue decidiendo el controlador
     * ({@code ControlIntentosFallidos}); esta vista solo los muestra.</p>
     */
    private void mostrarCuentaRegresiva(int segundos) {
        fabrica.pintarAlerta(lblMensaje,
                "Demasiados intentos. Espera " + segundos + " s.", false);
    }

    @Override
    public void mostrarTransicion(Runnable alSiguientePaso) {
        fabrica.mostrarTransicion(this, "Iniciando sesión...", alSiguientePaso);
    }

    @Override
    public <T> void ejecutarEnSegundoPlano(String mensaje,
                                           java.util.function.Supplier<T> tarea,
                                           java.util.function.Consumer<T> alTerminar) {
        fabrica.ejecutarConCarga(this, mensaje, tarea, alTerminar,
                fallo -> mostrarError("No se pudo completar la operación: "
                        + fallo.getMessage()));
    }

    @Override
    public void activarAccesoGoogle(Runnable accion) {
        btnGoogle.addActionListener(e -> accion.run());
        separadorGoogle.setVisible(true);
        btnGoogle.setVisible(true);
        revalidate();
    }

    @Override
    public <T> void ejecutarCancelable(String mensaje,
                                       java.util.function.Supplier<T> tarea,
                                       java.util.function.Consumer<T> alTerminar,
                                       Runnable alCancelar) {
        fabrica.pintarAlerta(lblMensaje, null, true);
        fabrica.ejecutarCancelable(this, mensaje, tarea,
                resultado -> {
                    traerAlFrente();
                    alTerminar.accept(resultado);
                },
                // Los mensajes de esta espera ya vienen redactados para el
                // usuario (ver IAutenticadorExterno): se muestran tal cual.
                fallo -> {
                    traerAlFrente();
                    mostrarError(fallo.getMessage());
                },
                alCancelar);
    }

    /**
     * El usuario viene del navegador: sin esto, la ventana seguiría detrás
     * y parecería que no pasó nada. Windows puede limitarse a hacer
     * parpadear la barra de tareas en vez de traerla, y eso también sirve.
     */
    private void traerAlFrente() {
        Window ventana = SwingUtilities.getWindowAncestor(this);
        if (ventana != null) {
            ventana.toFront();
        }
    }

    @Override
    public String elegirRol(String nombre) {
        return new DialogoElegirRol(fabrica, this, nombre).elegir();
    }

    @Override
    public void cerrarVentana() {
        Window ventana = SwingUtilities.getWindowAncestor(this);
        if (ventana != null) {
            ventana.dispose();
        }
    }
}
