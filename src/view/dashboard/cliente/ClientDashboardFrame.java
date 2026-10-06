package view.dashboard.cliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;
import observer.CatalogoObserver;
import view.core.MainFrame;
import view.dashboard.AccionesSesion;
import view.factory.IComponentesFactory;
import view.factory.components.BotonCarrito;
import view.factory.components.CampoTextoConIcono;
import view.factory.components.SelectorEstrellas;
import view.factory.icons.IconoCampo;
import view.factory.promo.BannerRotativo;
import view.factory.promo.Diapositiva;

/**
 * Ventana de trabajo del Cliente: catálogo, carrito y compra.
 *
 * <p><b>Responsabilidad única:</b> esta clase solo pinta y captura eventos. No
 * sabe qué es un producto del dominio ni cómo se calcula un total: recibe
 * {@link TarjetaProducto}, {@link LineaCarrito} y {@link ResumenCompra} ya
 * resueltos por {@link controller.ClienteController}, y le avisa de lo que
 * hace el usuario mediante las acciones que registra
 * {@link IClienteDashboardView}. Por eso no importa nada de {@code model}.</p>
 *
 * <p><b>El carrito es un cajón lateral que se abre y se cierra</b>, no un panel
 * fijo. Antes ocupaba permanentemente la derecha de la ventana y le quitaba
 * ancho al catálogo aunque estuviera vacío. Ahora vive detrás del ícono de la
 * barra superior, que lleva encima el número de artículos: el catálogo usa toda
 * la ventana y el carrito aparece solo cuando se le pide. Se abre y se cierra
 * animando su ancho con {@code deslizarPanelLateral}, sin pintar nada a mano.</p>
 *
 * <p>Cada renglón del carrito lleva la miniatura del producto, que es lo que
 * permite reconocer de un vistazo qué se está llevando sin leer los nombres uno
 * por uno, su precio por unidad y un selector para cambiar la cantidad allí
 * mismo.</p>
 *
 * <p><b>Promociones.</b> Encima de la rejilla rota un banner con las mejores
 * ofertas, y al entrar puede aparecer un pop-up con una de ellas. Qué se
 * promociona y si toca mostrar el pop-up lo decide el controlador; esta clase
 * solo decide cómo se ven y cuándo es seguro abrir el pop-up.</p>
 *
 * <p>Sigue siendo un {@link JFrame} propio y maximizado, no una carta de
 * {@link MainFrame}: el espacio de trabajo del rol necesita ancho, justo lo
 * contrario del Login.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 3.0
 */
public class ClientDashboardFrame extends JFrame
        implements IClienteDashboardView, CatalogoObserver {

    private static final int TAM_LOGO = 38;
    private static final int TAM_AVATAR = 42;
    /** Ancho del cajón: cabe miniatura, nombre, precio unitario, cantidad y subtotal. */
    private static final int ANCHO_CAJON = 400;
    private static final int COLUMNAS_CATALOGO = 4;
    private static final int ALTO_IMAGEN_TARJETA = 140;
    private static final int TAM_MINIATURA = 56;
    private static final int ANCHO_IMAGEN_PROMOCION = 392;
    private static final int ALTO_IMAGEN_PROMOCION = 190;
    /** Por debajo de esto el pop-up avisa de que quedan pocas unidades. */
    private static final int STOCK_BAJO = 5;
    /**
     * Espera antes de mostrar el pop-up: el controlador lo pide mientras la
     * ventana aún se está construyendo, y un modal sin su ventana detrás
     * aparecería flotando sobre el escritorio.
     */
    private static final int MS_ESPERA_PROMOCION = 700;
    private static final int ANCHO_IMAGEN_DETALLE = 260;
    private static final int ALTO_IMAGEN_DETALLE = 180;
    private static final int ANCHO_DETALLE = 640;
    private static final int LARGO_LINEA_DESCRIPCION = 46;
    private static final int LARGO_LINEA_COMENTARIO = 95;
    /** Alto de la parte superior de la ficha (imagen y datos, con vendedor y estrellas). */
    private static final int ALTO_DETALLE_SUPERIOR = 285;
    private static final int ALTO_LISTA_RESENAS = 120;
    private static final int ANCHO_COMENTARIO = 440;
    private static final int TAM_ESTRELLA_TARJETA = 13;
    private static final int TAM_ESTRELLA_FICHA = 18;
    private static final int ANCHO_FORMULARIO_ENVIO = 340;
    private static final int TAM_FAB = 48;
    private static final int MARGEN_FAB = 20;
    /** Píxeles desplazados a partir de los cuales aparece el botón de volver arriba. */
    private static final int DESPLAZAMIENTO_PARA_FAB = 160;

    private final IComponentesFactory fabrica;
    private final String nombreUsuario;
    /** Lo que esta ventana pide fuera de ella; la vista solo lo dispara. */
    private final AccionesSesion acciones;

    private final JPanel panelCategorias = new JPanel();
    private final JPanel panelCatalogo = new JPanel(new GridLayout(0, COLUMNAS_CATALOGO, 16, 16));
    private final JPanel panelLineasCarrito = new JPanel();
    private final JPanel cajonCarrito;
    private final BotonCarrito botonCarrito;
    private final BannerRotativo banner;
    /** Promociones del banner, en el mismo orden que sus diapositivas. */
    private final List<Promocion> promociones = new ArrayList<>();
    private final JLabel lblTotal;
    private final JLabel lblResumenCajon;
    private final JButton btnConfirmar;
    private final List<JButton> chipsCategoria = new ArrayList<>();

    private boolean cajonAbierto;

    private Consumer<String> accionBuscar = texto -> { };
    private Consumer<String> accionCategoria = etiqueta -> { };
    private BiConsumer<String, Integer> accionAgregar = (id, cantidad) -> { };
    private Consumer<String> accionQuitar = id -> { };
    private BiConsumer<String, Integer> accionCambiarCantidad = (id, cantidad) -> { };
    private Runnable accionConfirmar = () -> { };
    private Runnable accionMisCompras = () -> { };
    private Runnable accionRecargarCatalogo = () -> { };
    private Consumer<String> accionVerDetalle = id -> { };
    private Consumer<NuevaResena> accionPublicarResena = resena -> { };

    /** Sección de reseñas de la ficha abierta; {@code null} si no hay ficha abierta. */
    private JPanel seccionResenas;
    /** Calificación de la cabecera de la ficha abierta: se repinta al publicar una reseña. */
    private JLabel calificacionFicha;
    /** Producto de la ficha abierta, para saber si una reseña recién publicada es suya. */
    private String idFichaAbierta;
    private JLabel alertaResena;
    private JDialog dialogoDatosEnvio;
    private JLabel alertaDatosEnvio;
    /**
     * Hay ya una recarga encargada al hilo de eventos y todavía no corrió.
     * Una compra que descuenta el stock de cinco productos llega como cinco
     * avisos seguidos; sin esta marca serían cinco recargas del catálogo.
     */
    private final AtomicBoolean recargaPendiente = new AtomicBoolean();

    /**
     * @param fabrica       fábrica de la que salen todos los componentes
     * @param nombreUsuario nombre del usuario, para el saludo y las iniciales
     * @param acciones      cerrar sesión, editar perfil, cambiar de tema y, si
     *                      la cuenta vende, ir a su tienda; las arma
     *                      {@link controller.SesionController}, que es quien
     *                      tiene el {@code Usuario} — esta ventana nunca lo
     *                      recibe
     */
    public ClientDashboardFrame(IComponentesFactory fabrica, String nombreUsuario,
                                AccionesSesion acciones) {
        this.acciones = acciones;
        this.fabrica = fabrica;
        this.nombreUsuario = nombreUsuario;
        this.botonCarrito = fabrica.crearBotonCarrito();
        this.banner = fabrica.crearBannerRotativo();
        banner.alPulsarAccion(indice -> {
            if (indice >= 0 && indice < promociones.size()) {
                accionVerDetalle.accept(promociones.get(indice).producto().id());
            }
        });

        setTitle("Plataforma E-Commerce | Tienda");
        // La X termina el programa: esta es la ventana principal mientras
        // hay sesión, y cerrarla debe detener también los hilos de fondo
        // (conexión a Mongo, SwingWorker). La sesión recordada NO se borra:
        // cerrar la ventana no es cerrar sesión. "Cerrar sesión" sigue
        // usando dispose(), que no dispara esta operación.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        lblTotal = new JLabel("$ 0");
        lblTotal.setFont(fabrica.fuente(Font.BOLD, 22));
        lblTotal.setForeground(fabrica.colorTexto());
        lblResumenCajon = new JLabel("Sin artículos");
        lblResumenCajon.setFont(fabrica.fuente(Font.PLAIN, 12));
        lblResumenCajon.setForeground(fabrica.colorTextoSuave());
        // Naranja y no morado: es la acción de compra, la que no debe pasar
        // desapercibida (ver crearBotonDestacado).
        btnConfirmar = fabrica.crearBotonDestacado("CONFIRMAR COMPRA");
        btnConfirmar.addActionListener(e -> accionConfirmar.run());
        cajonCarrito = construirCajonCarrito();

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(fabrica.colorFondo());
        contenido.add(construirBarraSuperior(), BorderLayout.NORTH);
        contenido.add(construirZonaCatalogo(), BorderLayout.CENTER);
        contenido.add(cajonCarrito, BorderLayout.EAST);

        setContentPane(contenido);
        setMinimumSize(new Dimension(1100, 640));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // El buscador no debe quedarse el foco sin que nadie se lo pida.
        // Ver la nota de instalarPoliticaDeFoco().
        instalarPoliticaDeFoco();
    }

    /**
     * Impide que el buscador se quede el foco sin que el usuario lo pida.
     *
     * <p><b>El problema no era solo el arranque.</b> Swing entrega el foco
     * inicial al primer componente enfocable de la ventana, que es el
     * buscador; pero además, <em>cada vez que se reconstruye el catálogo</em>
     * (al buscar, al filtrar por categoría o al comprar) el botón que tuviera
     * el foco desaparece, y el gestor de foco vuelve a caer en ese mismo
     * primer componente. Medido: tras enfocar un "Ver producto" y refrescar la
     * rejilla, el dueño del foco pasaba a ser el {@code JTextField} de la
     * búsqueda. Por eso no bastaba con mover el foco al abrir: había que
     * quitarle al buscador la condición de destino por defecto.</p>
     *
     * <p>Se resuelve con la política de recorrido y no con
     * {@code requestFocus(...)}: pedir el foco para otro componente es
     * volver a forzarlo, solo que a otro sitio. Aquí simplemente no hay
     * candidato por defecto, así que al abrir no escribe nadie y tras
     * reconstruir la rejilla el cursor no reaparece solo. El recorrido con
     * el tabulador sigue intacto, porque {@code getComponentAfter} no se
     * toca: el buscador se enfoca con un clic o llegando a él con el
     * tabulador, que son las dos formas en que el usuario lo pide.</p>
     */
    private void instalarPoliticaDeFoco() {
        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
            @Override
            public Component getDefaultComponent(Container contenedor) {
                return null;
            }

            @Override
            public Component getInitialComponent(Window ventana) {
                return null;
            }
        });
    }

    // ---------------------------------------------------------------------
    // Barra superior
    // ---------------------------------------------------------------------

    /** Marca, buscador centrado y, a la derecha, tema, carrito y avatar. */
    private JPanel construirBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout(24, 0));
        barra.setBackground(fabrica.colorSidebar());
        barra.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel marca = new JPanel(new BorderLayout(10, 0));
        marca.setOpaque(false);
        marca.add(fabrica.crearLogo(TAM_LOGO), BorderLayout.WEST);
        JLabel lblMarca = new JLabel("Comercio Electrónico");
        lblMarca.setFont(fabrica.fuente(Font.BOLD, 16));
        lblMarca.setForeground(fabrica.colorTextoSidebar());
        marca.add(lblMarca, BorderLayout.CENTER);

        CampoTextoConIcono buscador = fabrica.crearCampoBusqueda("Buscar productos...");
        buscador.alEscribir(() -> accionBuscar.accept(buscador.getTexto()));
        // Sin esto el cursor se queda parpadeando en el buscador aunque se
        // pulse en el catálogo: Swing solo mueve el foco cuando se pulsa algo
        // enfocable, y las tarjetas y el fondo no lo son.
        fabrica.instalarLiberacionDeFoco(buscador);
        JPanel contenedorBuscador = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        contenedorBuscador.setOpaque(false);
        contenedorBuscador.add(buscador);

        botonCarrito.alPulsar(this::alternarCajon);

        JButton botonTema = fabrica.crearBotonTema(true, false);
        botonTema.addActionListener(e -> acciones.cambiarTema().run());

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        derecha.setOpaque(false);
        derecha.add(botonTema);
        derecha.add(botonCarrito);
        derecha.add(construirAvatar());

        barra.add(marca, BorderLayout.WEST);
        barra.add(contenedorBuscador, BorderLayout.CENTER);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    /** Avatar con iniciales que despliega el menú de usuario. */
    private JButton construirAvatar() {
        JButton avatar = fabrica.crearAvatar(iniciales(nombreUsuario), TAM_AVATAR);
        avatar.setToolTipText(nombreUsuario);

        JPopupMenu menu = fabrica.crearMenuUsuario(nombreUsuario);
        fabrica.agregarOpcionMenu(menu, "Mis compras", () -> accionMisCompras.run());
        fabrica.agregarOpcionMenu(menu, "Editar perfil", acciones.editarPerfil());
        // Doble rol: quien vende ve aquí la entrada a su tienda; quien solo
        // compra, no. La vista no decide quién vende: si la acción no llegó,
        // la opción no existe.
        if (acciones.cambiarPanel() != null) {
            fabrica.agregarOpcionMenu(menu, "Gestionar tienda", acciones.cambiarPanel());
        }
        Runnable alCerrarSesion = acciones.cerrarSesion();
        // Cerrar sesión no salta de golpe al Login: se acompaña con la misma
        // transición que el acceso ("Iniciando sesión..."), para que el cambio
        // de ventana no parezca un parpadeo. El cierre real va como acción
        // diferida, igual que en el acceso.
        fabrica.agregarOpcionMenu(menu, "Cerrar sesión", () ->
                fabrica.mostrarTransicion(this, "Cerrando sesión...", () -> {
                    dispose();
                    alCerrarSesion.run();
                }));

        // Un ActionListener y no un MouseListener: 'mouseClicked' no se
        // dispara si el ratón se mueve un píxel entre pulsar y soltar, y el
        // menú se quedaba sin abrir. La acción de un botón sí salta con
        // cualquier pulsación sobre cualquier punto del componente.
        avatar.addActionListener(e -> menu.show(avatar,
                -menu.getPreferredSize().width + avatar.getWidth(),
                avatar.getHeight() + 6));
        return avatar;
    }

    // ---------------------------------------------------------------------
    // Catálogo
    // ---------------------------------------------------------------------

    /** Filtros de categoría arriba y rejilla de tarjetas debajo. */
    private JPanel construirZonaCatalogo() {
        JPanel zona = new JPanel(new BorderLayout());
        zona.setBackground(fabrica.colorFondo());
        zona.setBorder(new EmptyBorder(16, 22, 16, 16));

        panelCategorias.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelCategorias.setBackground(fabrica.colorFondo());
        panelCategorias.setBorder(new EmptyBorder(0, 0, 14, 0));

        panelCatalogo.setBackground(fabrica.colorFondo());
        // El banner va dentro del área desplazable, encima de la rejilla: se
        // ve al entrar y se aparta al bajar por el catálogo, en vez de robarle
        // alto de forma permanente. Los chips de categoría sí quedan fijos.
        JPanel columna = new JPanel(new BorderLayout(0, 18));
        columna.setBackground(fabrica.colorFondo());
        columna.add(banner, BorderLayout.NORTH);
        columna.add(panelCatalogo, BorderLayout.CENTER);
        // La columna va al norte de un contenedor: así las tarjetas conservan
        // su alto natural en vez de estirarse para llenar la ventana.
        JPanel contenedorRejilla = new JPanel(new BorderLayout());
        contenedorRejilla.setBackground(fabrica.colorFondo());
        contenedorRejilla.add(columna, BorderLayout.NORTH);

        JScrollPane desplazable = fabrica.crearScroll(contenedorRejilla);

        zona.add(panelCategorias, BorderLayout.NORTH);
        zona.add(construirCapaConFab(desplazable), BorderLayout.CENTER);
        return zona;
    }

    /**
     * Superpone al catálogo un botón flotante de "volver arriba".
     *
     * <p><b>Por qué este FAB y no otro.</b> Un botón flotante solo aporta si
     * ofrece algo que no esté ya a la vista; repetir el carrito o el buscador,
     * que viven en la barra superior, sería ruido. Con diez o más productos el
     * catálogo se desplaza varias pantallas y volver al principio obliga a
     * arrastrar la barra: eso sí no está resuelto en ninguna otra parte.</p>
     *
     * <p>Aparece solo cuando hay algo de dónde volver, y se esconde arriba del
     * todo. La superposición es un {@link JLayeredPane}, que es el contenedor
     * que Swing trae para esto —mismo recurso que el contador del carrito—, no
     * un dibujo sobre el contenido.</p>
     */
    private JLayeredPane construirCapaConFab(JScrollPane desplazable) {
        JButton fab = fabrica.crearBotonFlotante("↑", "Volver al inicio del catálogo", TAM_FAB);
        fab.setVisible(false);
        fab.addActionListener(e ->
                desplazable.getVerticalScrollBar().setValue(0));

        JLayeredPane capa = new JLayeredPane();
        capa.setLayout(null);
        capa.add(desplazable, JLayeredPane.DEFAULT_LAYER);
        capa.add(fab, JLayeredPane.PALETTE_LAYER);

        // El JLayeredPane no reparte espacio solo: hay que recolocar a mano al
        // cambiar de tamaño. El desplazable ocupa todo; el FAB se ancla a la
        // esquina inferior derecha.
        capa.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                desplazable.setBounds(0, 0, capa.getWidth(), capa.getHeight());
                fab.setBounds(capa.getWidth() - TAM_FAB - MARGEN_FAB,
                        capa.getHeight() - TAM_FAB - MARGEN_FAB, TAM_FAB, TAM_FAB);
            }
        });

        desplazable.getVerticalScrollBar().addAdjustmentListener(e ->
                fab.setVisible(e.getValue() > DESPLAZAMIENTO_PARA_FAB));
        return capa;
    }

    @Override
    public void mostrarCategorias(List<String> categorias) {
        panelCategorias.removeAll();
        chipsCategoria.clear();
        for (String etiqueta : categorias) {
            JButton chip = fabrica.crearBotonSegmento(etiqueta);
            chip.addActionListener(e -> {
                resaltarChip(chip);
                accionCategoria.accept(etiqueta);
            });
            chipsCategoria.add(chip);
            panelCategorias.add(chip);
        }
        if (!chipsCategoria.isEmpty()) {
            resaltarChip(chipsCategoria.get(0));
        }
        panelCategorias.revalidate();
        panelCategorias.repaint();
    }

    private void resaltarChip(JButton activo) {
        for (JButton chip : chipsCategoria) {
            fabrica.resaltarSegmento(chip, chip == activo);
        }
    }

    @Override
    public void mostrarProductos(List<TarjetaProducto> productos) {
        panelCatalogo.removeAll();
        if (productos.isEmpty()) {
            JLabel vacio = new JLabel("No encontramos productos con esa búsqueda.");
            vacio.setFont(fabrica.fuente(Font.PLAIN, 14));
            vacio.setForeground(fabrica.colorTextoSuave());
            panelCatalogo.add(vacio);
        }
        for (TarjetaProducto producto : productos) {
            panelCatalogo.add(construirTarjeta(producto));
        }
        panelCatalogo.revalidate();
        panelCatalogo.repaint();
    }

    /** Tarjeta de catálogo: imagen, nombre, precio, descuento, stock y acción. */
    private JPanel construirTarjeta(TarjetaProducto producto) {
        JPanel tarjeta = fabrica.crearTarjeta(new BorderLayout());
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                tarjeta.getBorder(), new EmptyBorder(12, 14, 14, 14)));

        JLabel imagen = fabrica.crearImagenProducto(producto.imagen(), producto.nombre(),
                ANCHO_CAJON, ALTO_IMAGEN_TARJETA);
        // El ancho real lo reparte la rejilla; aquí solo se fija el alto, para
        // que todas las tarjetas de una fila queden alineadas.
        imagen.setPreferredSize(new Dimension(0, ALTO_IMAGEN_TARJETA));

        JPanel columna = new JPanel();
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setOpaque(false);
        columna.setBorder(new EmptyBorder(12, 0, 0, 0));

        JLabel lblCategoria = new JLabel(producto.categoria().toUpperCase());
        lblCategoria.setFont(fabrica.fuente(Font.BOLD, 10));
        lblCategoria.setForeground(fabrica.colorAcento());
        lblCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNombre = textoPlano(producto.nombre());
        lblNombre.setFont(fabrica.fuente(Font.BOLD, 15));
        lblNombre.setForeground(fabrica.colorTexto());
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblNombre.setBorder(new EmptyBorder(6, 0, 2, 0));

        JLabel lblVendedor = etiquetaVendedor(producto, 11);
        lblVendedor.setBorder(new EmptyBorder(0, 0, 4, 0));
        JLabel lblEstrellas = etiquetaCalificacion(producto.calificacion(),
                producto.totalResenas(), TAM_ESTRELLA_TARJETA);
        lblEstrellas.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel filaPrecio = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaPrecio.setOpaque(false);
        filaPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblPrecio = new JLabel(producto.precioFinal());
        lblPrecio.setFont(fabrica.fuente(Font.BOLD, 17));
        lblPrecio.setForeground(fabrica.colorTexto());
        filaPrecio.add(lblPrecio);
        if (producto.tieneDescuento()) {
            JLabel lblAntes = new JLabel("<html><s>" + producto.precioOriginal() + "</s></html>");
            lblAntes.setFont(fabrica.fuente(Font.PLAIN, 12));
            lblAntes.setForeground(fabrica.colorTextoSuave());
            filaPrecio.add(lblAntes);
            filaPrecio.add(insignia("-" + producto.descuento() + "%"));
        }

        JLabel lblStock = new JLabel(producto.hayExistencias()
                ? producto.stock() + " disponibles" : "Sin existencias");
        lblStock.setFont(fabrica.fuente(Font.PLAIN, 11));
        lblStock.setForeground(producto.hayExistencias()
                ? fabrica.colorTextoSuave() : fabrica.colorError());
        lblStock.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblStock.setBorder(new EmptyBorder(8, 0, 10, 0));

        JButton btnVer = fabrica.crearBotonSegmento("Ver producto");
        btnVer.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnVer.addActionListener(e -> accionVerDetalle.accept(producto.id()));

        columna.add(lblCategoria);
        columna.add(lblNombre);
        columna.add(lblVendedor);
        columna.add(lblEstrellas);
        columna.add(filaPrecio);
        columna.add(lblStock);
        columna.add(btnVer);

        tarjeta.add(imagen, BorderLayout.NORTH);
        tarjeta.add(columna, BorderLayout.CENTER);
        return tarjeta;
    }

    /**
     * "Vendido por Supertecno": la tienda que vende, en la tarjeta y en la
     * ficha. Los productos publicados antes de existir el nombre de empresa
     * no tienen marca; para ellos la línea queda en blanco y no desplaza el
     * resto de la tarjeta.
     */
    private JLabel etiquetaVendedor(TarjetaProducto producto, int tamano) {
        JLabel vendedor = textoPlano(producto.marca().isBlank()
                ? " " : "Vendido por " + producto.marca());
        vendedor.setFont(fabrica.fuente(Font.PLAIN, tamano));
        vendedor.setForeground(fabrica.colorTextoSuave());
        vendedor.setAlignmentX(Component.LEFT_ALIGNMENT);
        return vendedor;
    }

    /**
     * Estrellas del promedio con la cantidad de reseñas al lado, o un texto
     * neutro si aún no hay ninguna (cero estrellas se leería como "es malo").
     */
    private JLabel etiquetaCalificacion(double promedio, int total, int tamanoEstrella) {
        JLabel etiqueta = new JLabel();
        etiqueta.setFont(fabrica.fuente(Font.PLAIN, 11));
        etiqueta.setForeground(fabrica.colorTextoSuave());
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        pintarCalificacion(etiqueta, promedio, total, tamanoEstrella);
        return etiqueta;
    }

    private void pintarCalificacion(JLabel etiqueta, double promedio, int total, int tamanoEstrella) {
        if (total == 0) {
            etiqueta.setIcon(null);
            etiqueta.setText("Sin reseñas todavía");
        } else {
            etiqueta.setIcon(fabrica.crearEstrellas(promedio, tamanoEstrella));
            etiqueta.setIconTextGap(6);
            etiqueta.setText(String.format("%.1f  (%d)", promedio, total));
        }
    }

    /** Etiqueta verde de descuento, compartida por la tarjeta y el detalle. */
    private JLabel insignia(String texto) {
        JLabel insignia = new JLabel(" " + texto + " ");
        insignia.setFont(fabrica.fuente(Font.BOLD, 11));
        insignia.setForeground(fabrica.colorTextoSobreAcento());
        insignia.setOpaque(true);
        insignia.setBackground(fabrica.colorExito());
        return insignia;
    }

    // ---------------------------------------------------------------------
    // Detalle del producto
    // ---------------------------------------------------------------------

    /**
     * Ficha del producto: arriba, imagen a la izquierda y datos y cantidad a la
     * derecha; debajo, las reseñas.
     *
     * <p>La abre el controlador (no la tarjeta directamente) porque las
     * reseñas y el permiso para opinar se leen de la base. Mientras está
     * abierta se recuerda qué producto muestra, para que una reseña recién
     * publicada refresque esta misma sección sin cerrarla.</p>
     */
    @Override
    public void mostrarDetalle(TarjetaProducto producto, ResumenResenas resenas) {
        JPanel detalle = new JPanel(new BorderLayout(20, 0));
        detalle.setOpaque(false);
        // Sin un ancho declarado, el diálogo se queda con el mínimo y recorta
        // la descripción por el lado derecho (comprobado en captura).
        detalle.setPreferredSize(new Dimension(ANCHO_DETALLE, ALTO_DETALLE_SUPERIOR));

        JPanel marcoImagen = new JPanel(new BorderLayout());
        marcoImagen.setOpaque(false);
        marcoImagen.add(fabrica.crearImagenProducto(producto.imagen(), producto.nombre(),
                ANCHO_IMAGEN_DETALLE, ALTO_IMAGEN_DETALLE), BorderLayout.NORTH);

        JPanel datos = new JPanel();
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
        datos.setOpaque(false);

        JLabel categoria = new JLabel(producto.categoria().toUpperCase());
        categoria.setFont(fabrica.fuente(Font.BOLD, 10));
        categoria.setForeground(fabrica.colorAcento());
        categoria.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titulo = textoPlano(producto.nombre());
        titulo.setFont(fabrica.fuente(Font.BOLD, 20));
        titulo.setForeground(fabrica.colorTexto());
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setBorder(new EmptyBorder(6, 0, 2, 0));

        JLabel vendedor = etiquetaVendedor(producto, 12);
        vendedor.setBorder(new EmptyBorder(0, 0, 4, 0));
        JLabel calificacion = etiquetaCalificacion(producto.calificacion(),
                producto.totalResenas(), TAM_ESTRELLA_TARJETA);
        calificacionFicha = calificacion;
        calificacion.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel descripcion = new JLabel(enLineas(producto.descripcion(), LARGO_LINEA_DESCRIPCION));
        descripcion.setFont(fabrica.fuente(Font.PLAIN, 13));
        descripcion.setForeground(fabrica.colorTextoSuave());
        descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        descripcion.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel filaPrecio = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaPrecio.setOpaque(false);
        filaPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel precio = new JLabel(producto.precioFinal());
        precio.setFont(fabrica.fuente(Font.BOLD, 24));
        precio.setForeground(fabrica.colorTexto());
        filaPrecio.add(precio);
        if (producto.tieneDescuento()) {
            JLabel antes = new JLabel("<html><s>" + producto.precioOriginal() + "</s></html>");
            antes.setFont(fabrica.fuente(Font.PLAIN, 13));
            antes.setForeground(fabrica.colorTextoSuave());
            filaPrecio.add(antes);
            filaPrecio.add(insignia("-" + producto.descuento() + "%"));
        }

        JLabel stock = new JLabel(producto.hayExistencias()
                ? producto.stock() + " unidades disponibles" : "Este producto está agotado.");
        stock.setFont(fabrica.fuente(Font.PLAIN, 12));
        stock.setForeground(producto.hayExistencias()
                ? fabrica.colorTextoSuave() : fabrica.colorError());
        stock.setAlignmentX(Component.LEFT_ALIGNMENT);
        stock.setBorder(new EmptyBorder(10, 0, 0, 0));

        datos.add(categoria);
        datos.add(titulo);
        datos.add(vendedor);
        datos.add(calificacion);
        datos.add(descripcion);
        datos.add(filaPrecio);
        datos.add(stock);

        int[] cantidad = {1};
        if (producto.hayExistencias()) {
            JPanel filaCantidad = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            filaCantidad.setOpaque(false);
            filaCantidad.setAlignmentX(Component.LEFT_ALIGNMENT);
            filaCantidad.setBorder(new EmptyBorder(16, 0, 0, 0));
            JLabel etiqueta = new JLabel("Cantidad:");
            etiqueta.setFont(fabrica.fuente(Font.BOLD, 12));
            etiqueta.setForeground(fabrica.colorTextoSuave());
            filaCantidad.add(etiqueta);
            filaCantidad.add(construirSelectorCantidad(1, producto.stock(),
                    nueva -> cantidad[0] = nueva));
            datos.add(filaCantidad);
        }

        detalle.add(marcoImagen, BorderLayout.WEST);
        detalle.add(datos, BorderLayout.CENTER);

        // Las reseñas van debajo, en una sección que se puede rehacer sola
        // cuando se publica una (ver actualizarResenas).
        seccionResenas = new JPanel(new BorderLayout(0, 10));
        seccionResenas.setOpaque(false);
        seccionResenas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, fabrica.colorBorde()),
                new EmptyBorder(14, 0, 0, 0)));
        idFichaAbierta = producto.id();
        pintarResenas(resenas);

        JPanel ficha = new JPanel(new BorderLayout(0, 16));
        ficha.setOpaque(false);
        ficha.add(detalle, BorderLayout.NORTH);
        ficha.add(seccionResenas, BorderLayout.CENTER);

        boolean confirmado = fabrica.mostrarDialogoConfirmacion(
                this, "Detalle del producto", ficha, "Agregar al carrito");
        idFichaAbierta = null;
        seccionResenas = null;
        calificacionFicha = null;
        if (confirmado && producto.hayExistencias()) {
            accionAgregar.accept(producto.id(), cantidad[0]);
            // Al agregar algo se abre el cajón: confirma que la acción surtió
            // efecto sin necesidad de ningún aviso.
            if (!cajonAbierto) {
                alternarCajon();
            }
        }
    }

    // ---------------------------------------------------------------------
    // Reseñas de la ficha
    // ---------------------------------------------------------------------

    /**
     * Rehace la sección de reseñas: promedio con estrellas, la lista de
     * opiniones y, si el usuario puede opinar, el formulario; si no, el motivo.
     */
    private void pintarResenas(ResumenResenas resenas) {
        seccionResenas.removeAll();

        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cabecera.setOpaque(false);
        JLabel titulo = new JLabel("Opiniones del producto");
        titulo.setFont(fabrica.fuente(Font.BOLD, 15));
        titulo.setForeground(fabrica.colorTexto());
        cabecera.add(titulo);
        if (resenas.total() > 0) {
            JLabel promedio = new JLabel(String.format("%.1f", resenas.promedio()));
            promedio.setFont(fabrica.fuente(Font.BOLD, 22));
            promedio.setForeground(fabrica.colorTexto());
            JLabel estrellas = new JLabel(resenas.total() == 1
                    ? "1 reseña" : resenas.total() + " reseñas",
                    fabrica.crearEstrellas(resenas.promedio(), TAM_ESTRELLA_FICHA),
                    SwingConstants.LEFT);
            estrellas.setIconTextGap(8);
            estrellas.setFont(fabrica.fuente(Font.PLAIN, 12));
            estrellas.setForeground(fabrica.colorTextoSuave());
            cabecera.add(promedio);
            cabecera.add(estrellas);
        }

        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setOpaque(false);
        if (resenas.resenas().isEmpty()) {
            JLabel vacia = new JLabel("Todavía nadie ha opinado sobre este producto.");
            vacia.setFont(fabrica.fuente(Font.PLAIN, 12));
            vacia.setForeground(fabrica.colorTextoSuave());
            lista.add(vacia);
        }
        for (ResenaVista resena : resenas.resenas()) {
            lista.add(construirFilaResena(resena));
        }
        JPanel contenedorLista = new JPanel(new BorderLayout());
        contenedorLista.setOpaque(false);
        contenedorLista.add(lista, BorderLayout.NORTH);
        JScrollPane desplazable = fabrica.crearScroll(contenedorLista);
        // Va dentro de un diálogo: el fondo es el del panel, no el de la ventana.
        desplazable.getViewport().setBackground(fabrica.colorPanel());
        desplazable.setPreferredSize(new Dimension(ANCHO_DETALLE, ALTO_LISTA_RESENAS));

        seccionResenas.add(cabecera, BorderLayout.NORTH);
        seccionResenas.add(desplazable, BorderLayout.CENTER);
        seccionResenas.add(resenas.puedeResenar()
                ? construirFormularioResena()
                : construirMotivoSinResena(resenas.motivo()), BorderLayout.SOUTH);
        seccionResenas.revalidate();
        seccionResenas.repaint();
    }

    /** Una opinión: autor, estrellas y fecha en una línea; el comentario debajo. */
    private JPanel construirFilaResena(ResenaVista resena) {
        JPanel fila = new JPanel(new BorderLayout(0, 2));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setBorder(new EmptyBorder(6, 0, 8, 0));

        JPanel encabezado = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        encabezado.setOpaque(false);
        JLabel autor = textoPlano(resena.autor());
        autor.setFont(fabrica.fuente(Font.BOLD, 12));
        autor.setForeground(fabrica.colorTexto());
        JLabel estrellas = new JLabel(fabrica.crearEstrellas(resena.estrellas(), 12));
        JLabel fecha = new JLabel(resena.fecha());
        fecha.setFont(fabrica.fuente(Font.PLAIN, 11));
        fecha.setForeground(fabrica.colorTextoSuave());
        encabezado.add(autor);
        encabezado.add(estrellas);
        encabezado.add(fecha);
        fila.add(encabezado, BorderLayout.NORTH);

        if (!resena.comentario().isBlank()) {
            JLabel comentario = new JLabel(enLineas(resena.comentario(), LARGO_LINEA_COMENTARIO));
            comentario.setFont(fabrica.fuente(Font.PLAIN, 12));
            comentario.setForeground(fabrica.colorTextoSuave());
            comentario.setBorder(new EmptyBorder(0, 8, 0, 0));
            fila.add(comentario, BorderLayout.CENTER);
        }
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
        return fila;
    }

    /** Estrellas, comentario y botón para publicar; con su alerta de error. */
    private JPanel construirFormularioResena() {
        SelectorEstrellas selector = fabrica.crearSelectorEstrellas();
        CampoTextoConIcono comentario = fabrica.crearCampoTexto(
                "Cuéntale a otros qué te pareció (opcional)", IconoCampo.Tipo.TEXTO);
        comentario.setPreferredSize(new Dimension(ANCHO_COMENTARIO, fabrica.altoCampo()));
        JButton publicar = fabrica.crearBotonPrimario("Publicar reseña");
        publicar.setPreferredSize(new Dimension(160, fabrica.altoCampo()));
        alertaResena = fabrica.crearAlerta();
        String idProducto = idFichaAbierta;
        publicar.addActionListener(e -> accionPublicarResena.accept(
                new NuevaResena(idProducto, selector.getSeleccion(), comentario.getTexto())));

        JLabel etiqueta = new JLabel("Tu calificación:");
        etiqueta.setFont(fabrica.fuente(Font.BOLD, 12));
        etiqueta.setForeground(fabrica.colorTextoSuave());

        JPanel filaEstrellas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaEstrellas.setOpaque(false);
        filaEstrellas.add(etiqueta);
        filaEstrellas.add(selector);

        JPanel filaComentario = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaComentario.setOpaque(false);
        filaComentario.add(comentario);
        filaComentario.add(publicar);

        JPanel formulario = new JPanel(new BorderLayout(0, 6));
        formulario.setOpaque(false);
        formulario.add(filaEstrellas, BorderLayout.NORTH);
        formulario.add(filaComentario, BorderLayout.CENTER);
        formulario.add(alertaResena, BorderLayout.SOUTH);
        return formulario;
    }

    /** Por qué no puede opinar (no lo compró, o es su propio producto). */
    private JLabel construirMotivoSinResena(String motivo) {
        alertaResena = null;
        JLabel etiqueta = new JLabel(motivo == null ? " " : motivo);
        etiqueta.setFont(fabrica.fuente(Font.PLAIN, 12));
        etiqueta.setForeground(fabrica.colorTextoSuave());
        return etiqueta;
    }

    @Override
    public void actualizarResenas(String idProducto, ResumenResenas resenas) {
        if (seccionResenas != null && idProducto.equals(idFichaAbierta)) {
            pintarResenas(resenas);
            // La cabecera también: si no, decía "5,0 (1)" sobre una sección
            // que ya mostraba 4,5 con dos reseñas.
            if (calificacionFicha != null) {
                pintarCalificacion(calificacionFicha, resenas.promedio(), resenas.total(),
                        TAM_ESTRELLA_TARJETA);
            }
        }
    }

    @Override
    public void mostrarErrorResena(String mensaje) {
        if (alertaResena != null) {
            fabrica.pintarAlerta(alertaResena, mensaje, true);
        } else {
            mostrarAviso(mensaje);
        }
    }

    // ---------------------------------------------------------------------
    // Datos de envío antes de comprar
    // ---------------------------------------------------------------------

    /**
     * Formulario rápido de cédula y dirección, sobre la tienda.
     *
     * <p>Se arma sobre {@code crearDialogoModal} porque, igual que la edición
     * de perfil, tiene que seguir abierto si lo escrito no vale. Quien decide
     * si vale es el controlador; aquí solo se pinta y se entrega.</p>
     */
    @Override
    public void pedirDatosEnvio(DatosEnvio actuales, Consumer<DatosEnvio> alEnviar) {
        CampoTextoConIcono txtCedula = fabrica.crearCampoTexto("Solo números, sin puntos",
                IconoCampo.Tipo.IDENTIFICACION);
        CampoTextoConIcono txtDireccion = fabrica.crearCampoTexto("Ej: Calle 10 #5-20, Bogotá",
                IconoCampo.Tipo.UBICACION);
        txtCedula.setTexto(actuales.cedula());
        txtDireccion.setTexto(actuales.direccion());
        alertaDatosEnvio = fabrica.crearAlerta();

        // Ancho mínimo, no fijo: con "!" un mensaje de error más largo que el
        // formulario se cortaba; así el pack() de mostrarErrorDatosEnvio lo ensancha.
        JPanel formulario = new JPanel(new MigLayout("wrap 1, insets 22 26 18 26, gapy 0",
                "[" + ANCHO_FORMULARIO_ENVIO + "::]"));
        JLabel titulo = new JLabel("Completa tus datos de envío");
        titulo.setFont(fabrica.fuente(Font.BOLD, 19));
        titulo.setForeground(fabrica.colorTexto());
        JLabel subtitulo = new JLabel("Los necesitamos para despachar y facturar tu pedido.");
        subtitulo.setFont(fabrica.fuente(Font.PLAIN, 12));
        subtitulo.setForeground(fabrica.colorTextoSuave());
        formulario.add(titulo);
        formulario.add(subtitulo, "gaptop 4, gapbottom 14");
        if (actuales.pideCedula()) {
            formulario.add(fabrica.crearEtiqueta("Cédula"), "gapbottom 4");
            formulario.add(txtCedula, "growx, gapbottom 10");
        }
        formulario.add(fabrica.crearEtiqueta("Dirección de envío"), "gapbottom 4");
        formulario.add(txtDireccion, "growx, gapbottom 6");
        formulario.add(alertaDatosEnvio, "growx, gaptop 4, hidemode 3");

        JButton guardar = fabrica.crearBotonDestacado("GUARDAR Y COMPRAR");
        guardar.addActionListener(e -> alEnviar.accept(new DatosEnvio(
                txtCedula.getTexto(), actuales.pideCedula(), txtDireccion.getTexto())));
        JButton cancelar = fabrica.crearBotonSegmento("Ahora no");
        cancelar.addActionListener(e -> cerrarDatosEnvio());
        JPanel botones = new JPanel(new MigLayout("insets 0, gapx 10", "[grow,fill][]"));
        botones.setOpaque(false);
        botones.add(guardar);
        botones.add(cancelar);
        formulario.add(botones, "growx, gaptop 14");

        dialogoDatosEnvio = fabrica.crearDialogoModal(this, "Datos de envío", formulario);
        // Modal: bloquea aquí hasta que se cierra (al aceptarse los datos o
        // al pulsar "Ahora no").
        dialogoDatosEnvio.setVisible(true);
    }

    @Override
    public void mostrarErrorDatosEnvio(String mensaje) {
        if (alertaDatosEnvio != null) {
            fabrica.pintarAlerta(alertaDatosEnvio, mensaje, true);
            if (dialogoDatosEnvio != null) {
                dialogoDatosEnvio.pack();
            }
        }
    }

    @Override
    public void cerrarDatosEnvio() {
        if (dialogoDatosEnvio != null) {
            // Ciclo de vida estricto: se destruye, no se esconde.
            dialogoDatosEnvio.dispose();
            dialogoDatosEnvio = null;
            alertaDatosEnvio = null;
        }
    }

    /**
     * Selector "- n +" entre 1 y el máximo. Lo usan la ficha del producto y
     * cada renglón del carrito: es el mismo gesto, así que es el mismo control.
     *
     * <p>Los botones se apagan en los extremos en vez de ignorar el clic: un
     * "+" que no hace nada parece roto, uno apagado dice que ya no hay más.</p>
     *
     * @param inicial   cantidad de partida
     * @param maximo    tope (las existencias)
     * @param alCambiar qué hacer con cada cantidad nueva
     * @return el selector, sin márgenes propios
     */
    private JPanel construirSelectorCantidad(int inicial, int maximo, IntConsumer alCambiar) {
        int tope = Math.max(1, maximo);
        int[] cantidad = {Math.min(Math.max(1, inicial), tope)};

        JLabel lblCantidad = new JLabel(String.valueOf(cantidad[0]), SwingConstants.CENTER);
        lblCantidad.setFont(fabrica.fuente(Font.BOLD, 15));
        lblCantidad.setForeground(fabrica.colorTexto());
        lblCantidad.setOpaque(true);
        lblCantidad.setBackground(fabrica.colorCampo());
        lblCantidad.setPreferredSize(new Dimension(44, 30));

        JButton menos = fabrica.crearBotonSegmento("-");
        JButton mas = fabrica.crearBotonSegmento("+");
        Runnable actualizar = () -> {
            lblCantidad.setText(String.valueOf(cantidad[0]));
            menos.setEnabled(cantidad[0] > 1);
            mas.setEnabled(cantidad[0] < tope);
        };
        menos.addActionListener(e -> {
            cantidad[0] = Math.max(1, cantidad[0] - 1);
            actualizar.run();
            alCambiar.accept(cantidad[0]);
        });
        mas.addActionListener(e -> {
            cantidad[0] = Math.min(tope, cantidad[0] + 1);
            actualizar.run();
            alCambiar.accept(cantidad[0]);
        });
        actualizar.run();

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        selector.setOpaque(false);
        selector.setAlignmentX(Component.LEFT_ALIGNMENT);
        selector.add(menos);
        selector.add(lblCantidad);
        selector.add(mas);
        return selector;
    }

    // ---------------------------------------------------------------------
    // Cajón del carrito
    // ---------------------------------------------------------------------

    /**
     * Panel lateral derecho; arranca cerrado (ancho cero).
     *
     * <p>Su fondo es el de la ventana y no el de la barra superior: los
     * renglones son tarjetas, y el borde redondeado de una tarjeta tapa sus
     * esquinas con ese color. Una línea a la izquierda lo separa del
     * catálogo.</p>
     */
    private JPanel construirCajonCarrito() {
        JPanel cajon = new JPanel(new BorderLayout());
        cajon.setBackground(fabrica.colorFondo());
        cajon.setPreferredSize(new Dimension(0, 0));
        cajon.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, fabrica.colorBorde()),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel titulo = new JLabel("Tu carrito");
        titulo.setFont(fabrica.fuente(Font.BOLD, 18));
        titulo.setForeground(fabrica.colorTexto());

        JButton cerrar = fabrica.crearBotonSegmento("Cerrar");
        cerrar.addActionListener(e -> alternarCajon());

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.setBorder(new EmptyBorder(0, 0, 12, 0));
        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(cerrar, BorderLayout.EAST);

        panelLineasCarrito.setLayout(new BoxLayout(panelLineasCarrito, BoxLayout.Y_AXIS));
        panelLineasCarrito.setBackground(fabrica.colorFondo());
        JPanel contenedorLineas = new JPanel(new BorderLayout());
        contenedorLineas.setBackground(fabrica.colorFondo());
        contenedorLineas.add(panelLineasCarrito, BorderLayout.NORTH);
        JScrollPane desplazable = fabrica.crearScroll(contenedorLineas);

        JPanel etiquetas = new JPanel();
        etiquetas.setLayout(new BoxLayout(etiquetas, BoxLayout.Y_AXIS));
        etiquetas.setOpaque(false);
        JLabel etiquetaTotal = new JLabel("Total a pagar");
        etiquetaTotal.setFont(fabrica.fuente(Font.BOLD, 13));
        etiquetaTotal.setForeground(fabrica.colorTextoSuave());
        etiquetaTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblResumenCajon.setAlignmentX(Component.LEFT_ALIGNMENT);
        etiquetas.add(etiquetaTotal);
        etiquetas.add(lblResumenCajon);

        JPanel filaTotal = new JPanel(new BorderLayout());
        filaTotal.setOpaque(false);
        filaTotal.add(etiquetas, BorderLayout.WEST);
        filaTotal.add(lblTotal, BorderLayout.EAST);

        JPanel pie = new JPanel(new BorderLayout(0, 12));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, fabrica.colorBorde()),
                new EmptyBorder(14, 0, 0, 0)));
        pie.add(filaTotal, BorderLayout.NORTH);
        pie.add(btnConfirmar, BorderLayout.CENTER);

        cajon.add(cabecera, BorderLayout.NORTH);
        cajon.add(desplazable, BorderLayout.CENTER);
        cajon.add(pie, BorderLayout.SOUTH);
        return cajon;
    }

    /** Abre el cajón si está cerrado y lo cierra si está abierto. */
    private void alternarCajon() {
        cajonAbierto = !cajonAbierto;
        fabrica.deslizarPanelLateral(cajonCarrito, cajonAbierto ? ANCHO_CAJON : 0);
    }

    @Override
    public void mostrarCarrito(List<LineaCarrito> lineas, String total) {
        panelLineasCarrito.removeAll();
        if (lineas.isEmpty()) {
            JLabel vacio = new JLabel("Todavía no has agregado nada.");
            vacio.setFont(fabrica.fuente(Font.PLAIN, 12));
            vacio.setForeground(fabrica.colorTextoSuave());
            vacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelLineasCarrito.add(vacio);
        }
        int unidades = 0;
        for (LineaCarrito linea : lineas) {
            unidades += linea.cantidad();
            panelLineasCarrito.add(construirLineaCarrito(linea));
            panelLineasCarrito.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        lblTotal.setText(total);
        lblResumenCajon.setText(resumenUnidades(lineas.size(), unidades));
        botonCarrito.setContador(unidades);
        btnConfirmar.setEnabled(!lineas.isEmpty());
        panelLineasCarrito.revalidate();
        panelLineasCarrito.repaint();
    }

    private String resumenUnidades(int renglones, int unidades) {
        if (unidades == 0) {
            return "Sin artículos";
        }
        return (unidades == 1 ? "1 artículo" : unidades + " artículos")
                + " en " + (renglones == 1 ? "1 producto" : renglones + " productos");
    }

    /**
     * Renglón del carrito: miniatura | nombre, precio por unidad y selector de
     * cantidad | subtotal y "Quitar".
     *
     * <p>El precio unitario va a la vista junto al subtotal porque con "3 x"
     * el subtotal solo no deja ver cuánto cuesta cada una. El selector cambia
     * la cantidad en el sitio: antes había que quitar el renglón y volver a
     * agregarlo desde la ficha.</p>
     */
    private JPanel construirLineaCarrito(LineaCarrito linea) {
        JPanel fila = fabrica.crearTarjeta(new BorderLayout(12, 0));
        fila.setBorder(BorderFactory.createCompoundBorder(
                fila.getBorder(), new EmptyBorder(10, 10, 10, 12)));
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel marcoImagen = new JPanel(new BorderLayout());
        marcoImagen.setOpaque(false);
        marcoImagen.add(fabrica.crearImagenProducto(linea.imagen(), linea.nombre(),
                TAM_MINIATURA, TAM_MINIATURA), BorderLayout.NORTH);

        JPanel texto = new JPanel();
        texto.setLayout(new BoxLayout(texto, BoxLayout.Y_AXIS));
        texto.setOpaque(false);
        JLabel nombre = new JLabel(linea.nombre());
        nombre.setFont(fabrica.fuente(Font.BOLD, 13));
        nombre.setForeground(fabrica.colorTexto());
        nombre.setToolTipText(linea.nombre());
        nombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel unitario = new JLabel(linea.precioUnitario() + " c/u");
        unitario.setFont(fabrica.fuente(Font.PLAIN, 11));
        unitario.setForeground(fabrica.colorTextoSuave());
        unitario.setAlignmentX(Component.LEFT_ALIGNMENT);
        unitario.setBorder(new EmptyBorder(2, 0, 6, 0));
        texto.add(nombre);
        texto.add(unitario);
        texto.add(construirSelectorCantidad(linea.cantidad(), linea.maximo(),
                nueva -> accionCambiarCantidad.accept(linea.idProducto(), nueva)));

        JLabel subtotal = new JLabel(linea.subtotal(), SwingConstants.RIGHT);
        subtotal.setFont(fabrica.fuente(Font.BOLD, 14));
        subtotal.setForeground(fabrica.colorTexto());
        JButton quitar = fabrica.crearBotonSegmento("Quitar");
        quitar.addActionListener(e -> accionQuitar.accept(linea.idProducto()));
        JPanel derecha = new JPanel(new BorderLayout(0, 8));
        derecha.setOpaque(false);
        derecha.add(subtotal, BorderLayout.NORTH);
        derecha.add(quitar, BorderLayout.SOUTH);

        fila.add(marcoImagen, BorderLayout.WEST);
        fila.add(texto, BorderLayout.CENTER);
        fila.add(derecha, BorderLayout.EAST);
        // BoxLayout estira en alto lo que no tiene tope: el renglón se queda
        // con su alto natural.
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
        return fila;
    }

    // ---------------------------------------------------------------------
    // Promociones: banner y pop-up
    // ---------------------------------------------------------------------

    @Override
    public void mostrarPromociones(List<Promocion> nuevas) {
        promociones.clear();
        promociones.addAll(nuevas);
        // Traducción de un registro a otro, igual que con el gráfico del
        // Proveedor: el banner es de la fábrica y no conoce 'Promocion'.
        List<Diapositiva> diapositivas = new ArrayList<>();
        for (Promocion promocion : nuevas) {
            diapositivas.add(new Diapositiva(promocion.titulo(), promocion.subtitulo(),
                    promocion.producto().imagen()));
        }
        banner.mostrar(diapositivas);
    }

    /**
     * Muestra el pop-up promocional en cuanto la ventana está en pantalla.
     *
     * <p>El controlador lo pide al iniciarse, cuando esta ventana todavía no
     * es visible; por eso se espera un instante con un {@link Timer} de un
     * solo disparo en vez de abrirlo en el acto. Si para entonces la ventana
     * ya no está (se cerró sesión muy rápido), no se muestra.</p>
     */
    @Override
    public void ofrecerPromocion(Promocion promocion) {
        Timer espera = new Timer(MS_ESPERA_PROMOCION, e -> {
            if (!isShowing()) {
                return;
            }
            boolean verOferta = fabrica.mostrarVentanaPromocional(this,
                    "Oferta especial para ti", "Solo por iniciar sesión hoy.",
                    construirCuerpoPromocion(promocion.producto()), "Ver oferta");
            if (verOferta) {
                accionVerDetalle.accept(promocion.producto().id());
            }
        });
        espera.setRepeats(false);
        espera.start();
    }

    /** Imagen grande, nombre, precios, descuento y aviso de pocas unidades. */
    private JPanel construirCuerpoPromocion(TarjetaProducto producto) {
        JPanel cuerpo = new JPanel();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setOpaque(false);

        JLabel imagen = fabrica.crearImagenProducto(producto.imagen(), producto.nombre(),
                ANCHO_IMAGEN_PROMOCION, ALTO_IMAGEN_PROMOCION);
        imagen.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nombre = new JLabel(producto.nombre());
        nombre.setFont(fabrica.fuente(Font.BOLD, 18));
        nombre.setForeground(fabrica.colorTexto());
        nombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        nombre.setBorder(new EmptyBorder(14, 0, 6, 0));

        JPanel filaPrecio = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaPrecio.setOpaque(false);
        filaPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel precio = new JLabel(producto.precioFinal());
        precio.setFont(fabrica.fuente(Font.BOLD, 26));
        precio.setForeground(fabrica.colorTexto());
        filaPrecio.add(precio);
        if (producto.tieneDescuento()) {
            JLabel antes = new JLabel("<html><s>" + producto.precioOriginal() + "</s></html>");
            antes.setFont(fabrica.fuente(Font.PLAIN, 14));
            antes.setForeground(fabrica.colorTextoSuave());
            filaPrecio.add(antes);
            filaPrecio.add(insignia("-" + producto.descuento() + "%"));
        }

        cuerpo.add(imagen);
        cuerpo.add(nombre);
        cuerpo.add(filaPrecio);
        if (producto.stock() <= STOCK_BAJO) {
            JLabel urgencia = new JLabel(producto.stock() == 1
                    ? "Queda 1 unidad." : "Quedan solo " + producto.stock() + " unidades.");
            urgencia.setFont(fabrica.fuente(Font.BOLD, 12));
            urgencia.setForeground(fabrica.colorDestacado());
            urgencia.setAlignmentX(Component.LEFT_ALIGNMENT);
            urgencia.setBorder(new EmptyBorder(8, 0, 0, 0));
            cuerpo.add(urgencia);
        }
        return cuerpo;
    }

    // ---------------------------------------------------------------------
    // Mis compras y avisos
    // ---------------------------------------------------------------------

    @Override
    public void mostrarCompras(List<ResumenCompra> compras) {
        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setOpaque(false);

        if (compras.isEmpty()) {
            JLabel vacio = new JLabel("Todavía no tienes compras registradas.");
            vacio.setFont(fabrica.fuente(Font.PLAIN, 13));
            vacio.setForeground(fabrica.colorTextoSuave());
            vacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            lista.add(vacio);
        }
        for (ResumenCompra compra : compras) {
            JPanel tarjeta = fabrica.crearTarjeta(new BorderLayout());
            tarjeta.setBorder(BorderFactory.createCompoundBorder(
                    tarjeta.getBorder(), new EmptyBorder(10, 12, 10, 12)));
            tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel columna = new JPanel();
            columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
            columna.setOpaque(false);
            columna.add(etiquetaCompra(compra.id() + "   " + compra.total(),
                    Font.BOLD, 14, fabrica.colorTexto()));
            columna.add(etiquetaCompra(compra.fecha(), Font.PLAIN, 11, fabrica.colorTextoSuave()));
            for (String articulo : compra.articulos()) {
                columna.add(etiquetaCompra(articulo, Font.PLAIN, 12, fabrica.colorTextoSuave()));
            }
            columna.add(etiquetaCompra("Entrega: " + compra.direccion(),
                    Font.PLAIN, 11, fabrica.colorAcento()));
            tarjeta.add(columna, BorderLayout.CENTER);

            lista.add(tarjeta);
            lista.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.setPreferredSize(new Dimension(430, Math.min(430, 80 + compras.size() * 120)));
        contenedor.add(fabrica.crearScroll(lista), BorderLayout.CENTER);
        fabrica.mostrarDialogoContenido(this, "Mis compras", contenedor, "Cerrar");
    }

    private JLabel etiquetaCompra(String texto, int estilo, int tamano, Color color) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fabrica.fuente(estilo, tamano));
        etiqueta.setForeground(color);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return etiqueta;
    }

    @Override
    public void mostrarAviso(String mensaje) {
        fabrica.mostrarDialogoAviso(this, mensaje);
    }

    @Override
    public void mostrarExito(String mensaje) {
        // Tras comprar, el cajón queda vacío: se cierra solo y le devuelve el
        // ancho al catálogo.
        if (cajonAbierto) {
            alternarCajon();
        }
        fabrica.celebrar(this);
        fabrica.mostrarDialogoExito(this, mensaje);
    }

    @Override
    public <T> void ejecutarEnSegundoPlano(String mensaje,
                                           java.util.function.Supplier<T> tarea,
                                           java.util.function.Consumer<T> alTerminar) {
        fabrica.ejecutarConCarga(this, mensaje, tarea, alTerminar,
                fallo -> mostrarAviso("No se pudo completar la operación: "
                        + fallo.getMessage()));
    }

    @Override
    public void cerrarVentana() {
        dispose();
    }

    @Override
    public void alBuscar(Consumer<String> accion) {
        this.accionBuscar = accion;
    }

    @Override
    public void alElegirCategoria(Consumer<String> accion) {
        this.accionCategoria = accion;
    }

    @Override
    public void alAgregarAlCarrito(BiConsumer<String, Integer> accion) {
        this.accionAgregar = accion;
    }

    @Override
    public void alQuitarDelCarrito(Consumer<String> accion) {
        this.accionQuitar = accion;
    }

    @Override
    public void alCambiarCantidad(BiConsumer<String, Integer> accion) {
        this.accionCambiarCantidad = accion;
    }

    @Override
    public void alVerDetalle(Consumer<String> accion) {
        this.accionVerDetalle = accion;
    }

    @Override
    public void alPublicarResena(Consumer<NuevaResena> accion) {
        this.accionPublicarResena = accion;
    }

    @Override
    public void alConfirmarCompra(Runnable accion) {
        this.accionConfirmar = accion;
    }

    @Override
    public void alAbrirMisCompras(Runnable accion) {
        this.accionMisCompras = accion;
    }

    @Override
    public void alRecargarCatalogo(Runnable accion) {
        this.accionRecargarCatalogo = accion;
    }

    /**
     * Aviso del patrón Observer: el catálogo cambió en algún sitio.
     *
     * <p>Puede llegar desde el hilo que vigila MongoDB, y Swing solo se toca
     * desde el hilo de eventos: por eso la recarga se encarga con
     * {@code invokeLater} en vez de ejecutarse aquí. Si ya hay una encargada
     * que no ha corrido, este aviso se funde con ella: recargar una vez ya
     * recoge todos los cambios que llegaron mientras tanto.</p>
     */
    @Override
    public void actualizarCatalogo() {
        if (recargaPendiente.compareAndSet(false, true)) {
            SwingUtilities.invokeLater(() -> {
                recargaPendiente.set(false);
                accionRecargarCatalogo.run();
            });
        }
    }

    // ---------------------------------------------------------------------
    // Utilidades de presentación
    // ---------------------------------------------------------------------

    /** @return una o dos iniciales del nombre, para el avatar */
    private String iniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, 1).toUpperCase();
        }
        return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
    }

    /**
     * Reparte un texto largo en varias líneas con {@code <br>} explícitos.
     *
     * <p>Se corta a mano y no con ancho en CSS porque el motor HTML de Swing
     * no respeta ese ancho de forma fiable: ya se comprobó que una frase
     * larga acaba recortada en una sola línea.</p>
     *
     * <p><b>El texto se escapa.</b> Descripciones y comentarios los escriben
     * proveedores y compradores, y un {@code JLabel} con {@code <html>}
     * interpreta las etiquetas: sin escapar, un comentario con
     * {@code <img src=...>} haría que la aplicación descargara lo que dijera
     * otra persona.</p>
     *
     * @param texto texto libre
     * @param largo caracteres por línea, aproximados
     */
    private String enLineas(String texto, int largo) {
        StringBuilder html = new StringBuilder("<html>");
        int usado = 0;
        for (String palabra : texto.split(" ")) {
            if (usado + palabra.length() > largo) {
                html.append("<br>");
                usado = 0;
            }
            html.append(escaparHtml(palabra)).append(' ');
            usado += palabra.length() + 1;
        }
        return html.append("</html>").toString();
    }

    /**
     * Etiqueta para texto que escribió otra persona (nombre de un producto,
     * de una tienda o de quien reseña). Un {@code JLabel} interpreta HTML si
     * el texto empieza por {@code <html>}; con {@code html.disable} se muestra
     * tal cual, sin importar lo que traiga.
     */
    private JLabel textoPlano(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.putClientProperty("html.disable", Boolean.TRUE);
        return etiqueta;
    }

    private String escaparHtml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
