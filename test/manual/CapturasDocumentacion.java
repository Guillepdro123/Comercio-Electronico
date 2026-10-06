package manual;

import aplicacion.compra.CompraService;
import aplicacion.cuenta.CuentaService;
import aplicacion.cuenta.SolicitudRegistro;
import aplicacion.cuenta.TipoCuenta;
import aplicacion.resena.ResenaService;
import app.Infraestructura;
import app.Main;
import controller.SesionController;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.MenuElement;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import model.entity.LineaPedido;
import model.entity.Pedido;
import model.entity.Producto;
import model.entity.Usuario;
import model.repository.memoria.ImagenRepositoryArchivo;
import model.repository.memoria.PedidoRepositoryMemoria;
import model.repository.memoria.ProductoRepositoryMemoria;
import model.repository.memoria.ResenaRepositoryMemoria;
import model.repository.memoria.UsuarioRepositoryImpl;
import observer.CatalogoSubject;
import service.correo.MensajeBienvenida;
import service.correo.NotificadorRegistroLocal;
import service.correo.ResumenPedido;
import view.auth.PanelLogin;
import view.auth.PanelRegistroUsuario;
import view.core.MainFrame;
import view.dashboard.cliente.ClientDashboardFrame;
import view.factory.ComponentesSwingFactory;
import view.factory.components.CampoPasswordConToggle;
import view.factory.components.CampoTextoConIcono;
import view.factory.components.SelectorEstrellas;
import view.factory.components.SelectorSegmentado;
import view.factory.promo.BannerRotativo;
import view.factory.tema.Paleta;
import view.factory.utils.PlaceholderFocusListener;

/**
 * Genera las capturas de evidencia de {@code docs/04_Pruebas_y_Casos}.
 *
 * <p>Recorre la aplicación real —las mismas ventanas, controladores y casos
 * de uso que usa el {@code .exe}— como lo haría una persona, y fotografía cada
 * paso con {@link Robot}: acceso, errores, registro, tienda, pop-up, ficha con
 * reseñas, carrito, checkout bloqueado, panel del proveedor y tema oscuro.
 * También escribe el HTML de los tres correos transaccionales.</p>
 *
 * <p><b>No toca nada real.</b> Usa los repositorios en memoria (nunca MongoDB
 * Atlas ni Resend) y, antes de cargar ninguna clase de la sesión, desvía
 * {@code user.home} a una carpeta temporal: así la sesión recordada y la
 * preferencia de tema de quien lo ejecuta no cambian.</p>
 *
 * <p>Se ejecuta a mano (NetBeans: Run File) y tarda unos dos minutos. Durante
 * ese tiempo no hay que usar el equipo: las capturas fotografían la pantalla y
 * saldría lo que estuviera encima. Accede a algunos campos privados de las
 * vistas por reflexión: es una herramienta de documentación, no código de la
 * aplicación, y así las vistas no exponen métodos solo para ella.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class CapturasDocumentacion {

    /** Ancho máximo de cada captura: suficiente para leerla y ligera en el repositorio. */
    private static final int ANCHO_MAXIMO = 1600;

    private static File carpetaCapturas;

    private CapturasDocumentacion() {
    }

    // ------------------------------------------------------------------
    // Utilidades de pantalla
    // ------------------------------------------------------------------

    private static void enEdt(Runnable accion) throws Exception {
        SwingUtilities.invokeAndWait(accion);
    }

    /** Pulsa sin esperar: si abre un diálogo modal, este hilo sigue libre. */
    private static void pulsar(AbstractButton boton) {
        SwingUtilities.invokeLater(boton::doClick);
    }

    private static JFrame ventana() {
        JFrame ultima = null;
        for (Window w : Window.getWindows()) {
            if (w instanceof JFrame f && f.isVisible()) {
                ultima = f;
            }
        }
        return ultima;
    }

    private static JDialog dialogo() {
        JDialog ultimo = null;
        for (Window w : Window.getWindows()) {
            if (w instanceof JDialog d && d.isVisible()) {
                ultimo = d;
            }
        }
        return ultimo;
    }

    private static void cerrarDialogos() throws Exception {
        enEdt(() -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof JDialog && w.isVisible()) {
                    w.dispose();
                }
            }
        });
        Thread.sleep(400);
    }

    /** Fotografía la ventana principal visible (los diálogos caen dentro). */
    private static void capturar(String nombre) throws Exception {
        Thread.sleep(700);
        JFrame activa = ventana();
        detenerBanner(activa);
        // Los límites de la ventana incluyen los bordes invisibles de Windows
        // (y, maximizada, unos píxeles fuera de la pantalla): se recorta al
        // área del contenido, más la barra de título que hay encima.
        Rectangle[] area = new Rectangle[1];
        enEdt(() -> {
            java.awt.Point contenido = activa.getRootPane().getLocationOnScreen();
            int arriba = Math.max(activa.getY(), activa.getGraphicsConfiguration().getBounds().y);
            area[0] = new Rectangle(contenido.x, arriba, activa.getRootPane().getWidth(),
                    contenido.y + activa.getRootPane().getHeight() - arriba);
        });
        // La captura multirresolución trae los píxeles físicos: con el
        // escalado de Windows, la normal sale reducida y borrosa.
        MultiResolutionImage multiple = new Robot().createMultiResolutionScreenCapture(area[0]);
        List<Image> variantes = multiple.getResolutionVariants();
        Image mayor = variantes.get(variantes.size() - 1);
        int ancho = mayor.getWidth(null);
        int alto = mayor.getHeight(null);
        if (ancho > ANCHO_MAXIMO) {
            alto = alto * ANCHO_MAXIMO / ancho;
            ancho = ANCHO_MAXIMO;
        }
        BufferedImage salida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = salida.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(mayor, 0, 0, ancho, alto, null);
        g.dispose();
        ImageIO.write(salida, "png", new File(carpetaCapturas, nombre));
        System.out.println("  captura " + nombre);
    }

    /**
     * Detiene la rotación del banner y espera a que acabe el fundido en curso:
     * fotografiado a mitad de transición, el texto sale superpuesto.
     */
    private static void detenerBanner(JFrame activa) throws Exception {
        BannerRotativo[] banner = new BannerRotativo[1];
        enEdt(() -> banner[0] = buscar(activa.getContentPane(), BannerRotativo.class));
        if (banner[0] == null) {
            return;
        }
        javax.swing.Timer rotacion = campo(banner[0], "rotacion");
        javax.swing.Timer fundido = campo(banner[0], "fundido");
        enEdt(rotacion::stop);
        for (int i = 0; i < 50 && fundido.isRunning(); i++) {
            Thread.sleep(50);
        }
        Thread.sleep(100);
    }

    private static <T extends Component> T buscar(Container raiz, Class<T> tipo) {
        for (Component hijo : raiz.getComponents()) {
            if (tipo.isInstance(hijo) && hijo.isShowing()) {
                return tipo.cast(hijo);
            }
            if (hijo instanceof Container c) {
                T encontrado = buscar(c, tipo);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    private static AbstractButton boton(Container raiz, String texto) {
        for (Component hijo : raiz.getComponents()) {
            if (hijo instanceof AbstractButton b && texto.equals(b.getText()) && b.isShowing()) {
                return b;
            }
            if (hijo instanceof Container c) {
                AbstractButton encontrado = boton(c, texto);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    private static AbstractButton botonPorAyuda(Container raiz, String ayuda) {
        for (Component hijo : raiz.getComponents()) {
            if (hijo instanceof AbstractButton b && ayuda.equals(b.getToolTipText()) && b.isShowing()) {
                return b;
            }
            if (hijo instanceof Container c) {
                AbstractButton encontrado = botonPorAyuda(c, ayuda);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    /** Raíz donde buscar: el diálogo abierto si lo hay, si no la ventana. */
    private static Container raiz() {
        JDialog abierto = dialogo();
        return abierto != null ? abierto.getContentPane() : ventana().getContentPane();
    }

    @SuppressWarnings("unchecked")
    private static <T> T campo(Object dueno, String nombre) {
        try {
            Field campo = dueno.getClass().getDeclaredField(nombre);
            campo.setAccessible(true);
            return (T) campo.get(dueno);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** Escribe como si lo hubiera tecleado el usuario (retira el texto fantasma). */
    private static void escribir(JTextField campo, String texto) {
        PlaceholderFocusListener.escribirTextoReal(campo, texto);
    }

    private static void escribirPassword(CampoPasswordConToggle compuesto, String texto) {
        JPasswordField campo = buscar(compuesto, JPasswordField.class);
        char oculto = campo.getEchoChar();
        escribir(campo, texto);
        // Con el texto fantasma el campo muestra letras; al escribir por
        // detrás hay que volver a ocultarlas, como haría el foco.
        campo.setEchoChar(oculto != 0 ? oculto : '*');
    }

    /** Abre el menú del avatar y elige una opción; opcionalmente fotografía el menú. */
    private static void opcionMenu(String usuario, String opcion, String captura) throws Exception {
        enEdt(() -> botonPorAyuda(ventana().getContentPane(), usuario).doClick());
        Thread.sleep(500);
        if (captura != null) {
            capturar(captura);
        }
        SwingUtilities.invokeLater(() -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof RootPaneContainer r) {
                    JPopupMenu menu = buscar(r.getRootPane().getLayeredPane(), JPopupMenu.class);
                    if (menu == null) {
                        continue;
                    }
                    for (MenuElement e : menu.getSubElements()) {
                        if (e instanceof JMenuItem item && opcion.equals(item.getText())) {
                            menu.setVisible(false);
                            item.doClick();
                            return;
                        }
                    }
                }
            }
        });
        Thread.sleep(1200);
    }

    private static void agregarAlCarrito(String idProducto, int cantidad) throws Exception {
        enEdt(() -> {
            BiConsumer<String, Integer> agregar = campo(ventana(), "accionAgregar");
            agregar.accept(idProducto, cantidad);
        });
        Thread.sleep(400);
    }

    private static void cambiarTema(String ayuda) throws Exception {
        SwingUtilities.invokeLater(() -> botonPorAyuda(ventana().getContentPane(), ayuda).doClick());
        Thread.sleep(1500);
    }

    // ------------------------------------------------------------------
    // Recorrido
    // ------------------------------------------------------------------

    /**
     * @param args [0] carpeta de evidencias (por defecto
     *             {@code docs/04_Pruebas_y_Casos/evidencias})
     * @throws Exception si falla la automatización de pantalla
     */
    public static void main(String[] args) throws Exception {
        try {
            recorrer(args);
        } catch (Exception | Error ex) {
            // Sin esto, un paso fallido dejaría las ventanas abiertas y el
            // programa esperando para siempre.
            ex.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private static void recorrer(String[] args) throws Exception {
        // Primero que nada: la sesión y las preferencias leen user.home al
        // cargarse, y no deben escribir en la carpeta real del usuario.
        Path casaTemporal = Files.createTempDirectory("capturas-comercio");
        System.setProperty("user.home", casaTemporal.toString());

        File evidencias = new File(args.length > 0 ? args[0] : "docs/04_Pruebas_y_Casos/evidencias");
        carpetaCapturas = new File(evidencias, "capturas");
        File carpetaCorreos = new File(evidencias, "correos");
        carpetaCapturas.mkdirs();
        carpetaCorreos.mkdirs();

        // --- Datos de demostración, todo en memoria -----------------------
        UsuarioRepositoryImpl usuarios = new UsuarioRepositoryImpl();
        ProductoRepositoryMemoria productos = new ProductoRepositoryMemoria();
        PedidoRepositoryMemoria pedidos = new PedidoRepositoryMemoria();
        ResenaRepositoryMemoria resenas = new ResenaRepositoryMemoria();
        NotificadorRegistroLocal notificador = new NotificadorRegistroLocal();
        CuentaService cuentas = new CuentaService(usuarios, notificador);
        // El proveedor usa el correo de la tienda de ejemplo: así es dueño
        // del catálogo sembrado y su panel tiene ventas que mostrar.
        Usuario luis = cuentas.registrar(new SolicitudRegistro("79876543", "Luis Ortega",
                "proveedor@empresa.com", "Clave123*", "900123456-7", TipoCuenta.PROVEEDOR,
                "Supertecno")).usuario();
        luis.setDireccionEnvio("Carrera 7 # 80-15, Bogotá");
        Usuario ana = cuentas.registrar(new SolicitudRegistro("1023456789", "Ana Gómez",
                "ana.gomez@correo.com", "Clave123*", "Calle 45 # 12-30, Bogotá",
                TipoCuenta.CLIENTE, "")).usuario();
        Usuario carlos = cuentas.registrar(new SolicitudRegistro("1098765432", "Carlos Ruiz",
                "carlos.ruiz@correo.com", "Clave123*", "Avenida 6N # 23-50, Cali",
                TipoCuenta.CLIENTE, "")).usuario();
        Usuario sofia = cuentas.crearConGoogle("104857392", "sofia.torres@gmail.com",
                "Sofía Torres", TipoCuenta.CLIENTE).usuario();

        List<Producto> catalogo = productos.listarTodos();
        Producto audifonos = catalogo.get(0);
        Producto teclado = catalogo.get(1);
        Producto zapatillas = catalogo.get(3);
        Producto cafetera = catalogo.get(5);
        CompraService compras = new CompraService(productos, pedidos, notificador);
        ResenaService servicioResenas = new ResenaService(resenas, pedidos, productos);
        Pedido pedidoCarlos = compras.confirmar(carlos, List.of(
                linea(audifonos, 1), linea(zapatillas, 2))).pedido();
        compras.confirmar(ana, List.of(linea(audifonos, 1)));
        compras.confirmar(carlos, List.of(linea(cafetera, 1)));
        servicioResenas.publicar(carlos, audifonos.getId(), 5,
                "Excelente sonido y la batería dura todo el día. Llegó antes de lo esperado.");
        servicioResenas.publicar(carlos, zapatillas.getId(), 4, "Cómodas para correr, tallan un poco grande.");

        // --- Correos transaccionales (HTML real que enviaría Resend) ------
        escribirArchivo(new File(carpetaCorreos, "01_bienvenida.html"), MensajeBienvenida.enHtml(ana));
        escribirArchivo(new File(carpetaCorreos, "02_confirmacion_compra.html"),
                ResumenPedido.enHtml(pedidoCarlos));
        escribirArchivo(new File(carpetaCorreos, "03_alerta_venta.html"),
                ResumenPedido.alertaVentaEnHtml(pedidoCarlos));

        // --- Ventana de acceso ---------------------------------------------
        Infraestructura[] infra = new Infraestructura[1];
        enEdt(() -> {
            com.formdev.flatlaf.FlatLightLaf.setup();
            ComponentesSwingFactory fabrica = new ComponentesSwingFactory(Paleta.clara());
            ImagenRepositoryArchivo imagenes = new ImagenRepositoryArchivo();
            fabrica.usarFuenteDeImagenes(imagenes::leer);
            infra[0] = new Infraestructura(usuarios, productos, pedidos, resenas, imagenes,
                    notificador, new CatalogoSubject(), null, fabrica);
            Main.mostrarVentanaPrincipal(infra[0]);
        });
        Thread.sleep(1000);

        System.out.println("Acceso");
        capturar("01_login.png");
        PanelLogin login = buscar(ventana().getContentPane(), PanelLogin.class);
        enEdt(() -> {
            escribir(buscar((CampoTextoConIcono) campo(login, "txtCorreo"), JTextField.class),
                    "ana.gomez@correo.com");
            escribirPassword(campo(login, "campoPassword"), "Incorrecta1*");
        });
        pulsar(boton(login, "INICIAR SESIÓN"));
        Thread.sleep(1500);
        capturar("02_login_contrasena_incorrecta.png");
        for (int intento = 0; intento < 2; intento++) {
            pulsar(boton(login, "INICIAR SESIÓN"));
            Thread.sleep(1500);
        }
        capturar("03_login_bloqueado_30s.png");

        System.out.println("Registro");
        enEdt(() -> ((MainFrame) ventana()).mostrarCarta(PanelRegistroUsuario.NOMBRE_CARTA));
        PanelRegistroUsuario registro = buscar(ventana().getContentPane(), PanelRegistroUsuario.class);
        enEdt(() -> {
            ((CampoTextoConIcono) campo(registro, "txtIdentificacion")).setTexto("10234abc");
            ((CampoTextoConIcono) campo(registro, "txtNombres")).setTexto("María López");
            ((CampoTextoConIcono) campo(registro, "txtCorreo")).setTexto("maria.lopez@correo.com");
            escribirPassword(campo(registro, "campoPassword"), "abc");
            ((CampoTextoConIcono) campo(registro, "txtCampoDinamico")).setTexto("Calle 10 # 5-20, Medellín");
        });
        pulsar(boton(registro, "REGISTRAR USUARIO"));
        Thread.sleep(800);
        capturar("04_registro_validacion.png");
        enEdt(() -> {
            boton((SelectorSegmentado) campo(registro, "selectorTipoCuenta"), TipoCuenta.PROVEEDOR).doClick();
            ((CampoTextoConIcono) campo(registro, "txtIdentificacion")).setTexto("43567890");
            ((CampoTextoConIcono) campo(registro, "txtNombres")).setTexto("María López");
            ((CampoTextoConIcono) campo(registro, "txtCorreo")).setTexto("ventas@hogarplus.co");
            escribirPassword(campo(registro, "campoPassword"), "Hogar2026*");
            ((CampoTextoConIcono) campo(registro, "txtCampoDinamico")).setTexto("901234567-8");
            ((CampoTextoConIcono) campo(registro, "txtEmpresa")).setTexto("HogarPlus");
        });
        capturar("05_registro_proveedor.png");
        pulsar(boton(registro, "REGISTRAR USUARIO"));
        Thread.sleep(2000);
        capturar("06_registro_exitoso.png");
        cerrarDialogos();
        enEdt(() -> ventana().dispose());

        System.out.println("Tienda (Cliente)");
        // El pop-up se sortea al entrar (40 %): se entra hasta que aparece.
        boolean conPopup = false;
        for (int intento = 0; intento < 20 && !conPopup; intento++) {
            enEdt(() -> new SesionController(infra[0]).abrir(ana));
            Thread.sleep(1800);
            conPopup = dialogo() != null;
            if (!conPopup) {
                enEdt(() -> ventana().dispose());
            }
        }
        capturar("07_popup_promocional.png");
        cerrarDialogos();
        capturar("08_tienda_banner.png");
        enEdt(() -> escribir(buscar(ventana().getContentPane(), JTextField.class), "cafe"));
        Thread.sleep(600);
        capturar("09_busqueda_sin_tildes.png");
        enEdt(() -> escribir(buscar(ventana().getContentPane(), JTextField.class), ""));
        Thread.sleep(600);

        pulsar(boton(ventana().getContentPane(), "Ver producto"));
        Thread.sleep(2000);
        capturar("10_ficha_con_resenas.png");
        enEdt(() -> {
            buscar(raiz(), SelectorEstrellas.class).setSeleccion(4);
            buscar(raiz(), CampoTextoConIcono.class)
                    .setTexto("Buen sonido y cómodos; la cancelación de ruido funciona bien.");
        });
        pulsar(boton(raiz(), "Publicar reseña"));
        Thread.sleep(1500);
        capturar("11_resena_publicada.png");
        cerrarDialogos();
        pulsar(botonVerProducto(teclado.getNombre()));
        Thread.sleep(2000);
        capturar("12_ficha_sin_compra.png");
        cerrarDialogos();

        agregarAlCarrito(teclado.getId(), 1);
        agregarAlCarrito(cafetera.getId(), 2);
        pulsar(botonPorAyuda(ventana().getContentPane(), "Ver mi carrito"));
        Thread.sleep(1000);
        capturar("13_carrito.png");
        pulsar(boton(ventana().getContentPane(), "CONFIRMAR COMPRA"));
        Thread.sleep(2000);
        capturar("14_compra_confirmada.png");
        cerrarDialogos();
        opcionMenu("Ana Gómez", "Mis compras", null);
        capturar("15_mis_compras.png");
        cerrarDialogos();
        enEdt(() -> ventana().dispose());

        System.out.println("Cuenta de Google sin datos de envío");
        enEdt(() -> new SesionController(infra[0]).abrir(sofia));
        Thread.sleep(1800);
        cerrarDialogos();
        opcionMenu("Sofía Torres", "Editar perfil", null);
        capturar("16_perfil_con_aviso.png");
        cerrarDialogos();
        agregarAlCarrito(zapatillas.getId(), 1);
        pulsar(boton(ventana().getContentPane(), "CONFIRMAR COMPRA"));
        Thread.sleep(1500);
        capturar("17_checkout_pide_datos.png");
        JDialog formularioEnvio = dialogo();
        enEdt(() -> {
            List<JTextField> campos = new java.util.ArrayList<>();
            recolectar(formularioEnvio.getContentPane(), campos);
            escribir(campos.get(0), "12");
            escribir(campos.get(1), "Carrera 15 # 93-60, Bogotá");
        });
        pulsar(boton(formularioEnvio.getContentPane(), "GUARDAR Y COMPRAR"));
        Thread.sleep(1500);
        capturar("18_checkout_cedula_invalida.png");
        enEdt(() -> {
            List<JTextField> campos = new java.util.ArrayList<>();
            recolectar(formularioEnvio.getContentPane(), campos);
            escribir(campos.get(0), "52789456");
        });
        pulsar(boton(formularioEnvio.getContentPane(), "GUARDAR Y COMPRAR"));
        Thread.sleep(2500);
        capturar("19_checkout_compra_completada.png");
        cerrarDialogos();
        enEdt(() -> ventana().dispose());

        System.out.println("Proveedor");
        enEdt(() -> new SesionController(infra[0]).abrir(luis));
        Thread.sleep(1800);
        cerrarDialogos();
        opcionMenu("Luis Ortega", "Gestionar tienda", "20_menu_gestionar_tienda.png");
        Thread.sleep(800);
        capturar("21_panel_proveedor.png");

        pulsar(boton(ventana().getContentPane(), "NUEVO PRODUCTO"));
        Thread.sleep(1200);
        llenarProducto("Lámpara de escritorio LED", "Luz cálida regulable y brazo flexible.",
                "abc", "10", "8");
        capturar("22_producto_formulario.png");
        pulsar(boton(raiz(), "Guardar"));
        Thread.sleep(1500);
        capturar("23_producto_precio_invalido.png");
        cerrarDialogos();
        pulsar(boton(ventana().getContentPane(), "NUEVO PRODUCTO"));
        Thread.sleep(1200);
        llenarProducto("Lámpara de escritorio LED", "Luz cálida regulable y brazo flexible.",
                "89000", "10", "8");
        pulsar(boton(raiz(), "Guardar"));
        Thread.sleep(2000);
        capturar("24_producto_publicado.png");
        cerrarDialogos();

        System.out.println("Tema oscuro");
        cambiarTema("Cambiar a modo oscuro");
        capturar("25_panel_proveedor_oscuro.png");
        pulsar(boton(ventana().getContentPane(), "Ir a la tienda"));
        Thread.sleep(2000);
        cerrarDialogos();
        capturar("26_tienda_oscura.png");
        pulsar(boton(ventana().getContentPane(), "Ver producto"));
        Thread.sleep(2000);
        capturar("27_ficha_oscura.png");
        cerrarDialogos();

        System.out.println("Listo: " + carpetaCapturas.getAbsolutePath());
    }

    private static LineaPedido linea(Producto producto, int cantidad) {
        return new LineaPedido(producto.getId(), producto.getNombre(), producto.getPrecioFinal(), cantidad);
    }

    private static void escribirArchivo(File archivo, String contenido) throws Exception {
        Files.writeString(archivo.toPath(), contenido, StandardCharsets.UTF_8);
        System.out.println("  correo " + archivo.getName());
    }

    private static void recolectar(Container raiz, List<JTextField> campos) {
        for (Component hijo : raiz.getComponents()) {
            if (hijo instanceof JTextField campo && campo.isShowing()) {
                campos.add(campo);
            }
            if (hijo instanceof Container c) {
                recolectar(c, campos);
            }
        }
    }

    /** El botón "Ver producto" de la tarjeta cuyo título es {@code nombre}. */
    private static AbstractButton botonVerProducto(String nombre) {
        ClientDashboardFrame tienda = (ClientDashboardFrame) ventana();
        return buscarEnTarjeta(tienda.getContentPane(), nombre);
    }

    private static AbstractButton buscarEnTarjeta(Container raiz, String nombre) {
        for (Component hijo : raiz.getComponents()) {
            if (hijo instanceof Container tarjeta && contieneTexto(tarjeta, nombre)) {
                AbstractButton ver = boton(tarjeta, "Ver producto");
                if (ver != null && !contieneOtraTarjeta(tarjeta)) {
                    return ver;
                }
                AbstractButton interno = buscarEnTarjeta(tarjeta, nombre);
                if (interno != null) {
                    return interno;
                }
            }
        }
        return null;
    }

    private static boolean contieneTexto(Container raiz, String texto) {
        for (Component hijo : raiz.getComponents()) {
            if (hijo instanceof javax.swing.JLabel etiqueta && etiqueta.getText() != null
                    && etiqueta.getText().contains(texto)) {
                return true;
            }
            if (hijo instanceof Container c && contieneTexto(c, texto)) {
                return true;
            }
        }
        return false;
    }

    /** Una tarjeta tiene un solo "Ver producto"; si hay más, es la rejilla entera. */
    private static boolean contieneOtraTarjeta(Container raiz) {
        int[] cuenta = {0};
        contarVer(raiz, cuenta);
        return cuenta[0] > 1;
    }

    private static void contarVer(Container raiz, int[] cuenta) {
        for (Component hijo : raiz.getComponents()) {
            if (hijo instanceof AbstractButton b && "Ver producto".equals(b.getText())) {
                cuenta[0]++;
            }
            if (hijo instanceof Container c) {
                contarVer(c, cuenta);
            }
        }
    }

    private static void llenarProducto(String nombre, String descripcion, String precio,
                                       String descuento, String stock) throws Exception {
        enEdt(() -> {
            List<JTextField> campos = new java.util.ArrayList<>();
            recolectar(dialogo().getContentPane(), campos);
            escribir(campos.get(0), nombre);
            escribir(campos.get(1), descripcion);
            escribir(campos.get(2), precio);
            escribir(campos.get(3), descuento);
            escribir(campos.get(4), stock);
            boton(dialogo().getContentPane(), "Hogar").doClick();
        });
        Thread.sleep(400);
    }
}
