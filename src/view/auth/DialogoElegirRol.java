package view.auth;

import java.awt.Component;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import view.factory.IComponentesFactory;

/**
 * Diálogo modal en el que alguien que entra por primera vez con Google elige
 * si será Cliente o Proveedor.
 *
 * <p>Es la única pregunta que Google no puede responder por la aplicación: el
 * correo y el nombre llegan de su perfil, pero el rol es una decisión del
 * negocio. La dirección de envío o el NIT se dejan para "Editar perfil" en vez
 * de pedirlos aquí: la promesa de entrar con Google es no rellenar un
 * formulario.</p>
 *
 * <p>Devuelve las mismas constantes que usa el registro
 * ({@link IRegistroUsuarioView#TIPO_CLIENTE}, {@link IRegistroUsuarioView#TIPO_PROVEEDOR}),
 * así que el controlador decide el rol igual que en el registro normal, y la
 * vista sigue sin importar nada de {@code model}.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class DialogoElegirRol {

    private static final int ANCHO = 300;

    private final IComponentesFactory fabrica;
    private final Component padre;
    private final String nombre;

    private JDialog ventana;
    private String elegido;

    /**
     * @param fabrica fábrica de componentes del tema
     * @param padre   componente sobre el que se centra el diálogo
     * @param nombre  nombre que devolvió Google, para el saludo
     */
    public DialogoElegirRol(IComponentesFactory fabrica, Component padre, String nombre) {
        this.fabrica = fabrica;
        this.padre = padre;
        this.nombre = nombre;
    }

    /**
     * Muestra el diálogo y espera la elección.
     *
     * @return {@link IRegistroUsuarioView#TIPO_CLIENTE},
     *         {@link IRegistroUsuarioView#TIPO_PROVEEDOR}, o {@code null} si
     *         se canceló o se cerró la ventana
     */
    public String elegir() {
        ventana = fabrica.crearDialogoModal(padre, "Completa tu cuenta", construirContenido());
        // Modal: bloquea aquí hasta que se elija o se cierre.
        ventana.setVisible(true);
        return elegido;
    }

    private JPanel construirContenido() {
        JPanel contenido = new JPanel(new MigLayout(
                "wrap 1, insets 24 28 20 28, gapy 0", "[" + ANCHO + "!,fill]"));

        // Solo el primer nombre: los textos no se reparten en dos líneas en
        // este tema (se recortan), y un nombre completo largo no cabría.
        String primerNombre = nombre.trim().split("\\s+")[0];
        JLabel titulo = new JLabel("¡Hola, " + primerNombre + "!");
        titulo.setFont(fabrica.fuente(Font.BOLD, 20));
        titulo.setForeground(fabrica.colorTexto());
        contenido.add(titulo);
        contenido.add(texto("¿Cómo vas a usar la plataforma?", 13), "gaptop 4, gapbottom 18");

        contenido.add(opcion("SOY CLIENTE", IRegistroUsuarioView.TIPO_CLIENTE));
        contenido.add(texto("Compro productos del catálogo.", 12), "gaptop 5, gapbottom 14");
        contenido.add(opcion("SOY PROVEEDOR", IRegistroUsuarioView.TIPO_PROVEEDOR));
        contenido.add(texto("Publico y vendo mis productos.", 12), "gaptop 5, gapbottom 16");

        contenido.add(texto("Dirección o NIT: agrégalos luego en tu perfil.", 12), "gapbottom 14");
        JButton cancelar = fabrica.crearBotonSegmento("Cancelar");
        cancelar.addActionListener(e -> ventana.dispose());
        contenido.add(cancelar, "growx 0, alignx right");
        return contenido;
    }

    private JButton opcion(String texto, String rol) {
        JButton boton = fabrica.crearBotonPrimario(texto);
        boton.addActionListener(e -> {
            elegido = rol;
            ventana.dispose();
        });
        return boton;
    }

    private JLabel texto(String texto, int tamano) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fabrica.fuente(Font.PLAIN, tamano));
        etiqueta.setForeground(fabrica.colorTextoSuave());
        return etiqueta;
    }
}
