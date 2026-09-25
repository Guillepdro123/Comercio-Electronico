package view.dashboard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import view.core.MainFrame;
import view.factory.IComponentesFactory;

/**
 * Ventana principal a la que llega un Cliente tras iniciar sesión.
 *
 * <p>Es un {@link JFrame} propio (no una carta de {@link MainFrame}):
 * mientras el Login y el Registro comparten una ventana compacta, el
 * espacio de trabajo del Cliente necesita ser amplio (catálogo, tablas,
 * herramientas), así que se abre más grande, con un tamaño fijo de
 * 1024×680 y sin opción de redimensionar ni maximizar:
 * el contenido está compuesto para ese tamaño y estirarlo lo descompone.</p>
 *
 * <p><b>La vista no conoce el modelo:</b> no recibe un {@code Usuario}, solo
 * el nombre ya extraído por {@link controller.LoginController}, igual que el
 * resto de las vistas del proyecto.</p>
 *
 * <p><b>Ciclo de vida:</b> "Cerrar sesión" hace {@code dispose()} de esta
 * ventana y ejecuta {@code alCerrarSesion}, que el
 * {@link controller.LoginController} deja preparado para volver a abrir una
 * {@link MainFrame} nueva con el mismo repositorio (los usuarios ya
 * registrados no se pierden). Ninguna ventana se oculta y se deja viva.
 * <b>La X de la ventana hace exactamente lo mismo</b> que ese botón: antes
 * solo destruía la ventana, sin volver al Login, y la aplicación quedaba
 * corriendo sin ninguna ventana visible.</p>
 *
 * <p>Pantalla mínima a propósito: no hay todavía caso de uso de compras, así
 * que no tiene controlador propio. Cuando el incremento de catálogo/carrito
 * lo requiera, esta clase crece (o un {@code ClienteController} pasa a
 * dirigirla) sin tocar {@link MainFrame}, el Login ni el Registro
 * (Abierto/Cerrado).</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.1
 */
public class ClientDashboardFrame extends JFrame {

    private static final int TAM_LOGO = 88;
    private static final int ANCHO = 1024;
    private static final int ALTO = 680;

    /**
     * @param fabrica       fábrica de la que se toman los componentes del panel
     * @param nombreUsuario nombre a mostrar en el saludo de bienvenida
     * @param alCerrarSesion acción a ejecutar después de destruir esta ventana
     *                       (reabre la ventana principal de Login/Registro)
     */
    public ClientDashboardFrame(IComponentesFactory fabrica, String nombreUsuario, Runnable alCerrarSesion) {
        setTitle("Plataforma E-Commerce | Panel de Cliente");
        // La X no destruye la ventana por su cuenta: se intercepta para que
        // siga el mismo camino que "Cerrar sesión". Con DISPOSE_ON_CLOSE +
        // windowClosed la vuelta al Login se dispararía dos veces al usar el
        // botón (que también hace dispose()).
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarSesion(alCerrarSesion);
            }
        });

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(fabrica.colorFondo());

        contenido.add(construirBarraSuperior(fabrica, alCerrarSesion), BorderLayout.NORTH);
        contenido.add(construirPanelPrincipal(fabrica, nombreUsuario), BorderLayout.CENTER);
        setContentPane(contenido);

        setSize(ANCHO, ALTO);
        setResizable(false);
    }

    /**
     * Destruye esta ventana y vuelve al Login. Es el único camino de salida,
     * lo use el botón "Cerrar sesión" o la X de la barra de título.
     */
    private void cerrarSesion(Runnable alCerrarSesion) {
        dispose();
        alCerrarSesion.run();
    }

    /** Barra delgada superior, solo con el botón de cerrar sesión a la derecha. */
    private JPanel construirBarraSuperior(IComponentesFactory fabrica, Runnable alCerrarSesion) {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(fabrica.colorPanel());
        barra.setBorder(new EmptyBorder(12, 20, 12, 20));

        JButton btnCerrarSesion = fabrica.crearBotonPrimario("Cerrar sesión");
        btnCerrarSesion.setPreferredSize(new Dimension(160, 36));
        btnCerrarSesion.addActionListener(e -> cerrarSesion(alCerrarSesion));

        barra.add(btnCerrarSesion, BorderLayout.EAST);
        return barra;
    }

    private JPanel construirPanelPrincipal(IComponentesFactory fabrica, String nombreUsuario) {
        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(fabrica.colorFondo());
        principal.add(construirHeader(fabrica, nombreUsuario), BorderLayout.NORTH);
        principal.add(construirCuerpo(fabrica), BorderLayout.CENTER);
        return principal;
    }

    private JPanel construirHeader(IComponentesFactory fabrica, String nombreUsuario) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(fabrica.colorFondo());
        header.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel lblLogo = fabrica.crearLogo(TAM_LOGO);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("Panel de Cliente");
        lblTitulo.setFont(fabrica.fuente(Font.BOLD, 24));
        lblTitulo.setForeground(fabrica.colorTexto());
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setBorder(new EmptyBorder(12, 0, 4, 0));

        JLabel lblBienvenida = new JLabel("Bienvenido, " + nombreUsuario);
        lblBienvenida.setFont(fabrica.fuente(Font.PLAIN, 14));
        lblBienvenida.setForeground(fabrica.colorTextoSuave());
        lblBienvenida.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(lblLogo);
        header.add(lblTitulo);
        header.add(lblBienvenida);
        return header;
    }

    private JPanel construirCuerpo(IComponentesFactory fabrica) {
        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setBackground(fabrica.colorFondo());

        JLabel lblMensaje = new JLabel(
                "Aquí verás tu catálogo de compras en un próximo incremento.",
                SwingConstants.CENTER);
        lblMensaje.setFont(fabrica.fuente(Font.PLAIN, 16));
        lblMensaje.setForeground(fabrica.colorTextoSuave());

        cuerpo.add(lblMensaje, BorderLayout.CENTER);
        return cuerpo;
    }
}
