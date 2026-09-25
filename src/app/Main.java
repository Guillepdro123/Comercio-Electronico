package app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import controller.LoginController;
import controller.UsuarioController;
import model.repository.IUsuarioRepository;
import model.repository.UsuarioRepositoryImpl;
import view.auth.PanelLogin;
import view.auth.PanelRegistroUsuario;
import view.core.MainFrame;
import view.core.VentanaSplash;
import view.factory.ComponentesSwingFactory;
import view.factory.IComponentesFactory;

/**
 * Punto de entrada de la plataforma de comercio electrónico.
 *
 * <p>Actúa como ensamblador: crea el repositorio, la fábrica de componentes,
 * la ventana y los controladores, y los conecta entre sí. Es el único lugar
 * donde se mencionan las implementaciones concretas (repositorio, fábrica de
 * componentes); cambiarlas implica modificar una sola línea de este archivo,
 * sin tocar el resto de las capas.</p>
 *
 * <p><b>Por qué tiene paquete propio:</b> el botón "Cerrar sesión" de los
 * dashboards necesita volver a abrir una ventana principal completamente
 * funcional (Login y Registro con sus controladores conectados), no un
 * {@code MainFrame} vacío. Para reutilizar el mismo ensamblado sin
 * duplicarlo, {@link #mostrarVentanaPrincipal(IUsuarioRepository,
 * IComponentesFactory)} tiene que ser invocable desde
 * {@link controller.LoginController}; Java no permite importar clases del
 * paquete por defecto desde un paquete con nombre, así que esta clase vive
 * en {@code app} en vez de en la raíz. Sigue siendo el único lugar que
 * conoce tanto las vistas como los controladores concretos.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 3.0
 */
public class Main {

    /**
     * Arranca la aplicación dentro del Event Dispatch Thread de Swing.
     *
     * @param args argumentos de línea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        aplicarLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            // El repositorio y la fábrica se crean una sola vez, en el
            // arranque, y viajan de aquí en adelante (incluidos los logouts)
            // para que los usuarios ya registrados nunca se pierdan.
            IUsuarioRepository repositorio = new UsuarioRepositoryImpl();
            IComponentesFactory fabrica = new ComponentesSwingFactory();

            // La pantalla de bienvenida se muestra solo aquí, en el arranque
            // en frío: al cerrar sesión la aplicación ya está corriendo y
            // repetirla sería una espera sin motivo.
            new VentanaSplash(fabrica).mostrar(() -> mostrarVentanaPrincipal(repositorio, fabrica));
        });
    }

    /**
     * Construye y muestra una ventana principal (sidebar + Login/Registro)
     * completamente ensamblada: crea {@link MainFrame}, sus dos cartas y sus
     * dos controladores, y la deja visible mostrando el Login.
     *
     * <p>Se usa tanto en el arranque de la aplicación como al cerrar sesión
     * desde un dashboard ({@code ClientDashboardFrame}/
     * {@code ProviderDashboardFrame}), para que ambos caminos abran
     * exactamente la misma ventana, sin duplicar el ensamblado.</p>
     *
     * @param repositorio almacén de usuarios ya existente (nunca se crea uno
     *                    nuevo aquí: se perderían los usuarios registrados)
     * @param fabrica     fábrica de componentes ya existente
     */
    public static void mostrarVentanaPrincipal(IUsuarioRepository repositorio, IComponentesFactory fabrica) {
        MainFrame mainFrame = new MainFrame(fabrica);
        PanelLogin panelLogin = new PanelLogin(fabrica, mainFrame);
        PanelRegistroUsuario panelRegistro = new PanelRegistroUsuario(fabrica, mainFrame);

        mainFrame.agregarCarta(PanelLogin.NOMBRE_CARTA, panelLogin);
        mainFrame.agregarCarta(PanelRegistroUsuario.NOMBRE_CARTA, panelRegistro);

        new UsuarioController(panelRegistro, repositorio, mainFrame);
        new LoginController(panelLogin, repositorio, fabrica);

        mainFrame.mostrarCarta(PanelLogin.NOMBRE_CARTA);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    /**
     * Intenta usar Nimbus para que los componentes se vean más modernos. Si no
     * está disponible, la aplicación continúa con el aspecto por defecto: los
     * colores del tema oscuro se aplican de todas formas desde la fábrica.
     */
    private static void aplicarLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            System.err.println("No se pudo aplicar Nimbus, se usa el Look and Feel por defecto.");
        }
    }
}
