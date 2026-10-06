package view.factory;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import view.factory.tema.Paleta;

/**
 * Comprobación automática del conmutador claro/oscuro de
 * {@link ComponentesSwingFactory}.
 *
 * <p>Lo que importa del conmutador es que los colores repartidos por la
 * fábrica son <b>vivos</b>: el mismo objeto {@code Color} que ya tiene un
 * componente pasa a dar el color del tema nuevo. Por eso el tema cambia sin
 * cerrar ninguna ventana. Esta clase no abre ventanas (no hay ninguna que
 * actualizar) ni guarda la preferencia del usuario: eso lo hace la sesión,
 * no la fábrica.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class VerificacionConmutadorTema {

    private static int fallos = 0;

    private VerificacionConmutadorTema() {
    }

    private static void comprobar(String que, boolean ok) {
        System.out.println((ok ? "  OK    " : "  FALLA ") + que);
        if (!ok) {
            fallos++;
        }
    }

    /**
     * @param args no se usan
     * @throws InterruptedException      si se interrumpe la espera del EDT
     * @throws InvocationTargetException si la prueba lanza una excepción
     */
    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        // Como en app.Main: el Look and Feel y la fábrica se crean en el EDT.
        SwingUtilities.invokeAndWait(() -> {
            FlatLightLaf.setup();
            ComponentesSwingFactory fabrica = new ComponentesSwingFactory(Paleta.clara());
            Color fondo = fabrica.colorFondo();
            int claro = Paleta.clara().fondo().getRGB();
            int oscuro = Paleta.oscura().fondo().getRGB();

            System.out.println("Cambio a oscuro");
            comprobar("arranca en claro", !fabrica.esTemaOscuro() && fondo.getRGB() == claro);
            fabrica.alternarTema();
            comprobar("pasa a oscuro", fabrica.esTemaOscuro());
            comprobar("el mismo objeto Color ya entregado da el fondo oscuro",
                    fondo.getRGB() == oscuro);
            comprobar("instala FlatDarkLaf",
                    UIManager.getLookAndFeel().getClass().getSimpleName().equals("FlatDarkLaf"));

            System.out.println("Vuelta a claro");
            fabrica.alternarTema();
            comprobar("vuelve a claro con su fondo",
                    !fabrica.esTemaOscuro() && fondo.getRGB() == claro);
        });

        System.out.println(fallos == 0 ? "\nTODO CORRECTO" : "\n" + fallos + " FALLAS");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
