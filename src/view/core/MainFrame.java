package view.core;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import view.auth.PanelLogin;
import view.auth.PanelRegistroUsuario;
import view.factory.IComponentesFactory;

/**
 * Único {@link JFrame} de la aplicación para Login y Registro: una ventana
 * amplia con un panel lateral (sidebar) de navegación fija a la izquierda y,
 * a la derecha, un {@link CardLayout} que alterna entre {@link PanelLogin} y
 * {@link PanelRegistroUsuario}.
 *
 * <p><b>Responsabilidad única:</b> esta clase solo organiza el layout y la
 * navegación; la construcción de cada pantalla vive en su propio
 * {@link JPanel}. No valida credenciales ni conoce el repositorio.</p>
 *
 * <p><b>Abierto/Cerrado (O de SOLID):</b> una carta nueva se integra
 * llamando {@link #agregarCarta(String, JPanel)} desde el ensamblador
 * ({@code app.Main}), sin modificar esta clase ni las cartas ya
 * existentes.</p>
 *
 * <p>Implementa {@link INavegador} para que las vistas cambien de carta sin
 * depender de esta clase concreta (Inversión de Dependencias).</p>
 *
 * <p><b>Tamaño fijo, no por carta.</b> A diferencia de una iteración
 * anterior (donde el Login era compacto y el Registro más alto, cada uno
 * con su propio tamaño), ahora ambas viven dentro de una única ventana
 * amplia (950×650) pensada para alojar el sidebar; no tiene sentido
 * redimensionar la ventana según la carta visible, así que
 * {@link #mostrarCarta(String)} solo cambia la carta, no el tamaño. El
 * usuario tampoco puede cambiarlo: la ventana no es redimensionable.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 2.0
 */
public class MainFrame extends JFrame implements INavegador {

    private static final int ANCHO_VENTANA = 950;
    private static final int ALTO_VENTANA = 650;
    private static final int ANCHO_SIDEBAR = 260;
    /**
     * El logo es un emblema circular con el nombre de la marca dentro, así
     * que necesita más tamaño que un ícono suelto para que ese texto se lea.
     */
    private static final int TAM_LOGO_SIDEBAR = 112;
    private static final int TAM_ASISTENTE = 48;

    private final IComponentesFactory fabrica;
    private final CardLayout cardLayout;
    private final JPanel contenedorCartas;
    private final JButton btnIrLogin;
    private final JButton btnIrRegistro;

    /**
     * @param fabrica fábrica de componentes; define los colores del sidebar,
     *                del contenedor de cartas y de sus dos botones de
     *                navegación
     */
    public MainFrame(IComponentesFactory fabrica) {
        this.fabrica = fabrica;

        setTitle("Plataforma E-Commerce");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(ANCHO_VENTANA, ALTO_VENTANA);
        // Tamaño fijo: el sidebar y el formulario de Registro están medidos
        // contra estos 950×650, y achicar la ventana recortaba la tarjeta.
        // Sin redimensionar, Windows desactiva también el botón de maximizar;
        // quedan minimizar y cerrar.
        setResizable(false);

        cardLayout = new CardLayout();
        contenedorCartas = new JPanel(cardLayout);
        contenedorCartas.setBackground(fabrica.colorFondo());

        btnIrLogin = fabrica.crearBotonSidebar("Log In");
        btnIrRegistro = fabrica.crearBotonSidebar("Register");
        btnIrLogin.addActionListener(e -> mostrarCarta(PanelLogin.NOMBRE_CARTA));
        btnIrRegistro.addActionListener(e -> mostrarCarta(PanelRegistroUsuario.NOMBRE_CARTA));

        add(construirSidebar(), BorderLayout.WEST);
        add(contenedorCartas, BorderLayout.CENTER);
    }

    /** Panel lateral fijo: logo, título de bienvenida y los dos accesos de navegación. */
    private JPanel construirSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(fabrica.colorSidebar());
        sidebar.setPreferredSize(new Dimension(ANCHO_SIDEBAR, 0));
        sidebar.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Asistente animado en la esquina superior: fila propia, sin fondo
        // (transparente sobre el sidebar), la mascota queda anclada a la
        // derecha sin alterar el resto de la columna, ya centrada.
        JPanel filaSuperior = new JPanel(new BorderLayout());
        filaSuperior.setOpaque(false);
        filaSuperior.setAlignmentX(Component.CENTER_ALIGNMENT);
        filaSuperior.setMaximumSize(new Dimension(Integer.MAX_VALUE, TAM_ASISTENTE));
        filaSuperior.add(fabrica.crearAsistenteAnimado(TAM_ASISTENTE), BorderLayout.EAST);

        JLabel lblLogo = fabrica.crearLogo(TAM_LOGO_SIDEBAR);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setBorder(new EmptyBorder(16, 0, 0, 0));

        JLabel lblBienvenida = new JLabel("Bienvenido");
        lblBienvenida.setFont(fabrica.fuente(Font.BOLD, 18));
        lblBienvenida.setForeground(fabrica.colorTexto());
        lblBienvenida.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBienvenida.setBorder(new EmptyBorder(14, 0, 36, 0));

        btnIrLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIrRegistro.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(filaSuperior);
        sidebar.add(lblLogo);
        sidebar.add(lblBienvenida);
        sidebar.add(btnIrLogin);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnIrRegistro);
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    /**
     * Registra una carta para poder mostrarla luego con
     * {@link #mostrarCarta(String)}.
     *
     * @param nombre identificador único de la carta
     * @param panel  contenido de la carta
     */
    public void agregarCarta(String nombre, JPanel panel) {
        contenedorCartas.add(panel, nombre);
    }

    /**
     * Cambia la carta visible y resalta el botón del sidebar correspondiente.
     *
     * @param nombre identificador de la carta a mostrar
     */
    @Override
    public void mostrarCarta(String nombre) {
        cardLayout.show(contenedorCartas, nombre);
        actualizarBotonActivo(nombre);
    }

    /** Marca con el color de acento el botón de la carta actualmente visible. */
    private void actualizarBotonActivo(String nombre) {
        boolean esLogin = PanelLogin.NOMBRE_CARTA.equals(nombre);
        fabrica.resaltarBotonSidebar(btnIrLogin, esLogin);
        fabrica.resaltarBotonSidebar(btnIrRegistro, !esLogin);
    }
}
