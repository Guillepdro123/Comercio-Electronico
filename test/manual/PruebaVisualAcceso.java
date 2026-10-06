package manual;

import app.Main;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import model.entity.Cliente;
import model.repository.memoria.PedidoRepositoryMemoria;
import model.repository.memoria.ProductoRepositoryMemoria;
import model.repository.memoria.UsuarioRepositoryImpl;
import observer.CatalogoSubject;
import view.factory.ComponentesSwingFactory;

/**
 * Prueba visual del acceso con la ventana real, para comprobar en pantalla lo
 * que las verificaciones automáticas no pueden ver.
 *
 * <p>Se ejecuta a mano: abre el Login con repositorios en memoria, escribe una
 * contraseña incorrecta, captura el aviso, y después entra con la correcta y
 * captura el panel del Cliente.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class PruebaVisualAcceso {

    private PruebaVisualAcceso() {
    }

    private static void capturar(String archivo) throws Exception {
        Thread.sleep(900);
        Window ventana = null;
        for (Window w : Window.getWindows()) {
            if (w.isVisible() && w instanceof JFrame) {
                ventana = w;
            }
        }
        Rectangle area = ventana == null
                ? new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()) : ventana.getBounds();
        ImageIO.write(new Robot().createScreenCapture(area), "png", new File(archivo));
    }

    private static void recolectar(Container raiz, List<JTextField> campos) {
        for (Component hijo : raiz.getComponents()) {
            if (hijo instanceof JTextField campo && campo.isShowing()) {
                campos.add(campo);
            }
            if (hijo instanceof Container contenedor) {
                recolectar(contenedor, campos);
            }
        }
    }

    private static JButton boton(Container raiz, String texto) {
        for (Component hijo : raiz.getComponents()) {
            if (hijo instanceof JButton b && b.getText() != null
                    && b.getText().toLowerCase().contains(texto.toLowerCase()) && b.isShowing()) {
                return b;
            }
            if (hijo instanceof Container contenedor) {
                JButton encontrado = boton(contenedor, texto);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    private static void escribir(Robot robot, JTextField campo, String texto) throws Exception {
        Point p = campo.getLocationOnScreen();
        robot.mouseMove(p.x + campo.getWidth() / 2, p.y + campo.getHeight() / 2);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(200);
        SwingUtilities.invokeAndWait(() -> campo.setText(texto));
    }

    /**
     * @param args carpeta donde dejar las capturas
     * @throws Exception si falla la automatización de pantalla
     */
    public static void main(String[] args) throws Exception {
        String carpeta = args.length > 0 ? args[0] : ".";
        UsuarioRepositoryImpl usuarios = new UsuarioRepositoryImpl();
        usuarios.registrar(new Cliente("1", "Ana", "ana@correo.com",
                new aplicacion.seguridad.CifradoPassword().cifrar("Clave123*"), "Calle 1 # 2-3"));

        SwingUtilities.invokeAndWait(() -> {
            com.formdev.flatlaf.FlatLightLaf.setup();
            Main.mostrarVentanaPrincipal(new app.Infraestructura(usuarios,
                    new ProductoRepositoryMemoria(), new PedidoRepositoryMemoria(),
                    new model.repository.memoria.ResenaRepositoryMemoria(),
                    new model.repository.memoria.ImagenRepositoryArchivo(),
                    new service.correo.NotificadorRegistroLocal(), new CatalogoSubject(), null,
                    new ComponentesSwingFactory(view.factory.tema.Paleta.clara())));
        });
        Thread.sleep(900);

        JFrame login = (JFrame) java.util.Arrays.stream(Window.getWindows())
                .filter(w -> w.isVisible() && w instanceof JFrame).findFirst().orElseThrow();
        List<JTextField> campos = new ArrayList<>();
        recolectar(login, campos);
        Robot robot = new Robot();

        escribir(robot, campos.get(0), "ana@correo.com");
        escribir(robot, campos.get(1), "incorrecta");
        SwingUtilities.invokeLater(() -> boton(login, "INICIAR SESI").doClick());
        capturar(carpeta + "\\acceso_fallido.png");

        escribir(robot, campos.get(1), "Clave123*");
        SwingUtilities.invokeLater(() -> boton(login, "INICIAR SESI").doClick());
        Thread.sleep(1200);
        SwingUtilities.invokeLater(() -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof javax.swing.JDialog d && d.isVisible()) {
                    JButton aceptar = boton(d, "");
                    if (aceptar != null) {
                        aceptar.doClick();
                    }
                }
            }
        });
        Thread.sleep(3500);
        capturar(carpeta + "\\acceso_correcto.png");

        for (Window w : Window.getWindows()) {
            if (w.isVisible()) {
                System.out.println("ventana visible al final: " + w.getClass().getSimpleName());
            }
        }
        System.exit(0);
    }
}
