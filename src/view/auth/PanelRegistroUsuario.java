package view.auth;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import view.core.INavegador;
import view.core.MainFrame;
import view.factory.IComponentesFactory;
import view.factory.components.CampoPasswordConToggle;
import view.factory.components.CampoTextoConIcono;
import view.factory.components.SelectorSegmentado;
import view.factory.icons.IconoCampo;

/**
 * Tarjeta de {@link MainFrame} con el formulario de registro de usuarios.
 *
 * <p><b>Responsabilidad única:</b> esta clase solo compone el formulario,
 * delegando la creación de cada componente a {@link IComponentesFactory}. Ya
 * no instancia ni configura colores o fuentes por su cuenta. La única lógica
 * que conserva es estrictamente de presentación: alternar la etiqueta del
 * campo dinámico según el tipo de cuenta seleccionado.</p>
 *
 * <p>No repite el logo: {@link MainFrame} ya lo muestra una sola vez en el
 * sidebar. Aquí solo hay un título y la tarjeta con los campos, centrados en
 * el espacio amplio del panel central.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 3.0
 */
public class PanelRegistroUsuario extends JPanel implements IRegistroUsuarioView {

    /** Nombre de esta carta dentro del {@link CardLayout} de {@link MainFrame}. */
    public static final String NOMBRE_CARTA = "registro";

    private final IComponentesFactory fabrica;
    private final INavegador navegador;

    private CampoTextoConIcono txtIdentificacion;
    private CampoTextoConIcono txtNombres;
    private CampoTextoConIcono txtCorreo;
    private CampoPasswordConToggle campoPassword;
    private SelectorSegmentado selectorTipoCuenta;
    private JLabel lblCampoDinamico;
    private CampoTextoConIcono txtCampoDinamico;
    private JButton btnRegistrar;
    private JLabel lblMensaje;
    private JLabel lblFortaleza;

    /** Diámetro del punto del semáforo de fortaleza. */
    private static final int DIAMETRO_PUNTO_FORTALEZA = 9;

    /** Contador interno de filas del formulario. */
    private int fila = 0;

    /**
     * @param fabrica   fábrica de la que se toman todos los componentes del formulario
     * @param navegador usado únicamente por el enlace "¿Ya tienes cuenta? Inicia sesión"
     */
    public PanelRegistroUsuario(IComponentesFactory fabrica, INavegador navegador) {
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
     * Columna con el título y la tarjeta del formulario, centrada dentro del
     * espacio amplio que le da {@link MainFrame} (ver {@link GridBagLayout}
     * sin peso en el constructor, el mismo truco que usa {@link PanelLogin}).
     */
    private JPanel construirContenido() {
        JPanel columna = new JPanel();
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setOpaque(false);

        JLabel lblTitulo = new JLabel("Registro de Usuarios");
        lblTitulo.setFont(fabrica.fuente(Font.BOLD, 26));
        lblTitulo.setForeground(fabrica.colorTexto());
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = crearTextoAjustado("Crea tu cuenta de Cliente o Proveedor", 13);
        lblSubtitulo.setBorder(new EmptyBorder(4, 0, 8, 0));

        JPanel tarjeta = construirTarjeta();
        tarjeta.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblAyuda = crearTextoAjustado("Solo usamos tus datos para gestionar tu cuenta.", 12);
        lblAyuda.setBorder(new EmptyBorder(10, 0, 0, 0));

        columna.add(lblTitulo);
        columna.add(lblSubtitulo);
        columna.add(tarjeta);
        columna.add(lblAyuda);
        return columna;
    }

    /** Tarjeta con los campos del formulario y el botón de registro. */
    private JPanel construirTarjeta() {
        JPanel tarjeta = fabrica.crearTarjeta(new GridBagLayout());
        // La fábrica ya le puso fondo y borde; se combina con el relleno
        // interno en vez de reemplazar el borde con setBorder(...).
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                tarjeta.getBorder(), new EmptyBorder(12, 26, 10, 26)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        txtIdentificacion = fabrica.crearCampoTexto("Ej: 1002345678", IconoCampo.Tipo.IDENTIFICACION);
        txtNombres = fabrica.crearCampoTexto("Ej: Juan Pérez", IconoCampo.Tipo.USUARIO);
        txtCorreo = fabrica.crearCampoTexto("correo@empresa.com", IconoCampo.Tipo.CORREO);
        campoPassword = fabrica.crearCampoPassword("Mínimo 7 caracteres");

        agregarFila(tarjeta, gbc, fabrica.crearEtiqueta("Identificación"), txtIdentificacion);
        agregarFila(tarjeta, gbc, fabrica.crearEtiqueta("Nombres"), txtNombres);
        agregarFila(tarjeta, gbc, fabrica.crearEtiqueta("Correo electrónico"), txtCorreo);
        agregarFila(tarjeta, gbc, construirEncabezadoPassword(), campoPassword);

        selectorTipoCuenta = fabrica.crearSelectorTipoCuenta(
                new String[]{TIPO_CLIENTE, TIPO_PROVEEDOR}, this::actualizarCampoDinamico);
        agregarFila(tarjeta, gbc, fabrica.crearEtiqueta("Tipo de cuenta"), selectorTipoCuenta);

        lblCampoDinamico = fabrica.crearEtiqueta("Dirección de envío");
        txtCampoDinamico = fabrica.crearCampoTexto("Ej: Calle 10 #5-20, Bogotá", IconoCampo.Tipo.UBICACION);
        agregarFila(tarjeta, gbc, lblCampoDinamico, txtCampoDinamico);

        btnRegistrar = fabrica.crearBotonPrimario("REGISTRAR USUARIO");
        gbc.gridy = fila++;
        gbc.insets = new Insets(6, 0, 0, 0);
        tarjeta.add(btnRegistrar, gbc);

        // Mensaje inline de validación: reemplaza a la antigua barra de
        // estado gris. El éxito se confirma aparte con un JOptionPane
        // (ver mostrarExito).
        lblMensaje = fabrica.crearEtiqueta(" ");
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = fila++;
        gbc.insets = new Insets(4, 0, 0, 0);
        tarjeta.add(lblMensaje, gbc);

        JLabel enlaceLogin = fabrica.crearEnlaceSecundario("¿Ya tienes cuenta? Inicia sesión");
        enlaceLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navegador.mostrarCarta(PanelLogin.NOMBRE_CARTA);
            }
        });
        gbc.gridy = fila++;
        gbc.insets = new Insets(8, 0, 0, 0);
        tarjeta.add(enlaceLogin, gbc);

        return tarjeta;
    }

    /**
     * Crea una etiqueta secundaria centrada (subtítulo o texto de ayuda) con
     * el estilo del tema.
     *
     * <p><b>Los textos se mantienen cortos a propósito:</b> este panel vive en
     * una ventana de alto fijo y con la tarjeta ya ocupando casi todo el
     * espacio, así que una frase que no quepa a lo ancho no se reparte en dos
     * líneas: el layout la recorta. Se intentó resolver con ancho en CSS
     * ({@code <html><body style='width:...'>}), pero el motor HTML de Swing no
     * lo respeta de forma fiable. Microcopy breve es, además, mejor guía para
     * el usuario que un párrafo.</p>
     */
    private JLabel crearTextoAjustado(String texto, int tamanoFuente) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fabrica.fuente(Font.PLAIN, tamanoFuente));
        etiqueta.setForeground(fabrica.colorTextoSuave());
        etiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);
        return etiqueta;
    }

    /**
     * Fila de encabezado del campo de contraseña: a la izquierda la etiqueta
     * de siempre y a la derecha el semáforo de fortaleza.
     *
     * <p>El semáforo va <em>en la fila de la etiqueta</em>, no en una fila
     * propia, porque este formulario ya usa casi todo el alto disponible de
     * la ventana (ver la nota del presupuesto de alto en {@code CLAUDE.md}):
     * así no cuesta ni un píxel vertical.</p>
     */
    private JPanel construirEncabezadoPassword() {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(fabrica.crearEtiqueta("Contraseña"), BorderLayout.WEST);

        lblFortaleza = new JLabel();
        lblFortaleza.setFont(fabrica.fuente(Font.BOLD, 11));
        lblFortaleza.setIconTextGap(5);
        fila.add(lblFortaleza, BorderLayout.EAST);
        return fila;
    }

    /** Agrega al formulario una etiqueta y, debajo, su campo correspondiente. */
    private void agregarFila(JPanel panel, GridBagConstraints gbc, Component etiqueta, Component campo) {
        gbc.gridy = fila++;
        gbc.insets = new Insets(0, 0, 3, 0);
        panel.add(etiqueta, gbc);

        gbc.gridy = fila++;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(campo, gbc);
    }

    /**
     * Alterna la etiqueta, el texto fantasma y la ayuda del campo extra según
     * el tipo de cuenta seleccionado. Es presentación pura, por eso vive en
     * la vista.
     */
    private void actualizarCampoDinamico() {
        if (TIPO_PROVEEDOR.equals(getTipoCuentaSeleccionado())) {
            lblCampoDinamico.setText("NIT de la empresa");
            txtCampoDinamico.setToolTipText("Número de Identificación Tributaria del proveedor");
            txtCampoDinamico.cambiarTextoFantasma("Ej: 900123456-7");
            txtCampoDinamico.cambiarIcono(IconoCampo.Tipo.EMPRESA);
        } else {
            lblCampoDinamico.setText("Dirección de envío");
            txtCampoDinamico.setToolTipText("Dirección donde el cliente recibirá sus pedidos");
            txtCampoDinamico.cambiarTextoFantasma("Ej: Calle 10 #5-20, Bogotá");
            txtCampoDinamico.cambiarIcono(IconoCampo.Tipo.UBICACION);
        }
    }

    // ---------------------------------------------------------------------
    // IRegistroUsuarioView
    // ---------------------------------------------------------------------

    @Override
    public String getIdentificacion() {
        return txtIdentificacion.getTexto();
    }

    @Override
    public String getNombres() {
        return txtNombres.getTexto();
    }

    @Override
    public String getCorreo() {
        return txtCorreo.getTexto();
    }

    @Override
    public String getPassword() {
        return campoPassword.getPassword();
    }

    @Override
    public String getTipoCuentaSeleccionado() {
        return selectorTipoCuenta.getSeleccionado();
    }

    @Override
    public void observarPassword(Runnable alEscribir) {
        campoPassword.alEscribir(alEscribir);
    }

    @Override
    public void mostrarFortalezaPassword(int nivel, String descripcion) {
        Color color;
        switch (nivel) {
            case FORTALEZA_FUERTE -> color = fabrica.colorExito();
            case FORTALEZA_MEDIA -> color = fabrica.colorAdvertencia();
            default -> color = fabrica.colorError();
        }
        lblFortaleza.setIcon(fabrica.crearPunto(color, DIAMETRO_PUNTO_FORTALEZA));
        lblFortaleza.setForeground(color);
        lblFortaleza.setText(descripcion);
    }

    @Override
    public void ocultarFortalezaPassword() {
        lblFortaleza.setIcon(null);
        lblFortaleza.setText("");
    }

    @Override
    public String getDatoAdicional() {
        return txtCampoDinamico.getTexto();
    }

    @Override
    public JButton getBtnRegistrar() {
        return btnRegistrar;
    }

    @Override
    public void mostrarError(String mensaje) {
        lblMensaje.setForeground(fabrica.colorError());
        lblMensaje.setText(mensaje);
    }

    @Override
    public void mostrarExito(String mensaje) {
        // El confeti y el sonido se lanzan antes del diálogo: como el diálogo
        // es modal pero Swing sigue despachando eventos mientras está
        // abierto, la celebración se ve correr detrás de él.
        fabrica.celebrar(this);
        fabrica.mostrarDialogoExito(this, mensaje);
    }

    @Override
    public void limpiarFormulario() {
        txtIdentificacion.limpiar();
        txtNombres.limpiar();
        txtCorreo.limpiar();
        campoPassword.limpiar();
        txtCampoDinamico.limpiar();
        lblMensaje.setText(" ");
        txtIdentificacion.enfocar();
    }
}
