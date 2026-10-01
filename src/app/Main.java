package app;

import java.awt.EventQueue;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatDarkLaf;
import controller.LoginController;
import controller.UsuarioController;
import model.repository.IPedidoRepository;
import model.repository.IProductoRepository;
import model.repository.IUsuarioRepository;
import model.repository.PedidoRepositoryMemoria;
import model.repository.ProductoRepositoryMemoria;
import model.repository.UsuarioRepositoryImpl;
import service.INotificadorPedido;
import service.NotificadorRegistroLocal;
import service.NotificadorResend;
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
        // El Look and Feel se instala DENTRO del hilo de eventos, antes de
        // construir nada: Swing no es seguro para hilos, y cambiar el L&F
        // desde el hilo principal mientras el EDT ya existe es una carrera.
        // Antes se llamaba fuera; se corrigió al integrar FlatLaf.
        EventQueue.invokeLater(() -> {
            aplicarLookAndFeel();

            // El repositorio y la fábrica se crean una sola vez, en el
            // arranque, y viajan de aquí en adelante (incluidos los logouts)
            // para que los usuarios ya registrados nunca se pierdan.
            IUsuarioRepository repositorio = new UsuarioRepositoryImpl();
            IProductoRepository productos = new ProductoRepositoryMemoria();
            IPedidoRepository pedidos = new PedidoRepositoryMemoria();
            IComponentesFactory fabrica = new ComponentesSwingFactory();
            INotificadorPedido notificador = elegirNotificador();

            // La pantalla de bienvenida se muestra solo aquí, en el arranque
            // en frío: al cerrar sesión la aplicación ya está corriendo y
            // repetirla sería una espera sin motivo.
            new VentanaSplash(fabrica).mostrar(
                    () -> mostrarVentanaPrincipal(repositorio, productos, pedidos, notificador, fabrica));
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
    public static void mostrarVentanaPrincipal(IUsuarioRepository repositorio,
            IProductoRepository productos, IPedidoRepository pedidos,
            INotificadorPedido notificador, IComponentesFactory fabrica) {
        MainFrame mainFrame = new MainFrame(fabrica);
        PanelLogin panelLogin = new PanelLogin(fabrica, mainFrame);
        PanelRegistroUsuario panelRegistro = new PanelRegistroUsuario(fabrica, mainFrame);

        mainFrame.agregarCarta(PanelLogin.NOMBRE_CARTA, panelLogin);
        mainFrame.agregarCarta(PanelRegistroUsuario.NOMBRE_CARTA, panelRegistro);

        new UsuarioController(panelRegistro, repositorio, mainFrame);
        new LoginController(panelLogin, repositorio, productos, pedidos, notificador, fabrica);

        mainFrame.mostrarCarta(PanelLogin.NOMBRE_CARTA);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    /**
     * Elige cómo se avisa de una compra: por Resend si hay clave configurada
     * en la variable de entorno {@code RESEND_API_KEY}, y si no, dejando
     * constancia local del correo que se habría enviado.
     *
     * <p>La clave se lee del entorno y nunca del código fuente: una
     * credencial escrita en un archivo termina copiada en cualquier entrega.
     * Este es el único punto que decide la implementación concreta, igual que
     * con los repositorios.</p>
     *
     * @return el notificador a usar en esta ejecución
     */
    private static INotificadorPedido elegirNotificador() {
        String clave = System.getenv("RESEND_API_KEY");
        String remitente = System.getenv("RESEND_REMITENTE");
        if (clave == null || clave.isBlank()) {
            return new NotificadorRegistroLocal();
        }
        return new NotificadorResend(clave,
                remitente == null || remitente.isBlank()
                        ? "Comercio Electronico <onboarding@resend.dev>" : remitente);
    }

    /**
     * Instala FlatLaf en su variante oscura como Look and Feel base.
     *
     * <p><b>Por qué FlatLaf y no Nimbus.</b> Nimbus ignora {@code setBackground}
     * en varios controles y los pinta con sus propios <i>painters</i> claros;
     * media fábrica existe para sortearlo a base de delegados {@code Basic*UI}.
     * FlatLaf respeta los colores que se le piden, dibuja mejor en pantallas
     * con escalado y trae un tema oscuro coherente de fábrica.</p>
     *
     * <p><b>Esto no reemplaza a la fábrica.</b> La paleta morada sigue saliendo
     * de {@link view.factory.ComponentesSwingFactory}: el L&amp;F pone la base
     * (barras de desplazamiento, cursores, sombras, tipografía) y la fábrica
     * pone la identidad. Si FlatLaf no estuviera disponible se cae a Nimbus,
     * que es como funcionó hasta ahora, y la aplicación sigue viéndose con sus
     * colores.</p>
     */
    private static void aplicarLookAndFeel() {
        if (FlatDarkLaf.setup()) {
            return;
        }
        System.err.println("No se pudo aplicar FlatLaf; se intenta con Nimbus.");
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            System.err.println("Tampoco se pudo aplicar Nimbus, se usa el Look and Feel por defecto.");
        }
    }
}
