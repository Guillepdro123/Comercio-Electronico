package view.dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import view.core.MainFrame;
import view.factory.IComponentesFactory;
import view.factory.components.BotonCarrito;
import view.factory.components.CampoTextoConIcono;

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
 * por uno.</p>
 *
 * <p>Sigue siendo un {@link JFrame} propio y maximizado, no una carta de
 * {@link MainFrame}: el espacio de trabajo del rol necesita ancho, justo lo
 * contrario del Login.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 3.0
 */
public class ClientDashboardFrame extends JFrame implements IClienteDashboardView {

    private static final int TAM_LOGO = 38;
    private static final int TAM_AVATAR = 42;
    private static final int ANCHO_CAJON = 360;
    private static final int COLUMNAS_CATALOGO = 4;
    private static final int ALTO_IMAGEN_TARJETA = 140;
    private static final int TAM_MINIATURA = 46;
    private static final int ANCHO_IMAGEN_DETALLE = 260;
    private static final int ALTO_IMAGEN_DETALLE = 180;
    private static final int ANCHO_DETALLE = 640;
    private static final int LARGO_LINEA_DESCRIPCION = 46;

    private final IComponentesFactory fabrica;
    private final String nombreUsuario;

    private final JPanel panelCategorias = new JPanel();
    private final JPanel panelCatalogo = new JPanel(new GridLayout(0, COLUMNAS_CATALOGO, 16, 16));
    private final JPanel panelLineasCarrito = new JPanel();
    private final JPanel cajonCarrito;
    private final BotonCarrito botonCarrito;
    private final JLabel lblTotal;
    private final JLabel lblResumenCajon;
    private final JButton btnConfirmar;
    private final List<JButton> chipsCategoria = new ArrayList<>();

    private boolean cajonAbierto;

    private Consumer<String> accionBuscar = texto -> { };
    private Consumer<String> accionCategoria = etiqueta -> { };
    private BiConsumer<String, Integer> accionAgregar = (id, cantidad) -> { };
    private Consumer<String> accionQuitar = id -> { };
    private Runnable accionConfirmar = () -> { };
    private Runnable accionMisCompras = () -> { };

    /**
     * @param fabrica        fábrica de la que salen todos los componentes
     * @param nombreUsuario  nombre del cliente, para el saludo y las iniciales
     * @param alCerrarSesion acción que devuelve a la ventana de Login
     */
    public ClientDashboardFrame(IComponentesFactory fabrica, String nombreUsuario,
                                Runnable alCerrarSesion) {
        this.fabrica = fabrica;
        this.nombreUsuario = nombreUsuario;
        this.botonCarrito = fabrica.crearBotonCarrito();

        setTitle("Plataforma E-Commerce | Panel de Cliente");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        lblTotal = new JLabel("$ 0");
        lblTotal.setFont(fabrica.fuente(Font.BOLD, 22));
        lblTotal.setForeground(fabrica.colorTexto());
        lblResumenCajon = new JLabel("Sin artículos");
        lblResumenCajon.setFont(fabrica.fuente(Font.PLAIN, 12));
        lblResumenCajon.setForeground(fabrica.colorTextoSuave());
        btnConfirmar = fabrica.crearBotonPrimario("CONFIRMAR COMPRA");
        btnConfirmar.addActionListener(e -> accionConfirmar.run());
        cajonCarrito = construirCajonCarrito();

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(fabrica.colorFondo());
        contenido.add(construirBarraSuperior(alCerrarSesion), BorderLayout.NORTH);
        contenido.add(construirZonaCatalogo(), BorderLayout.CENTER);
        contenido.add(cajonCarrito, BorderLayout.EAST);

        setContentPane(contenido);
        setMinimumSize(new Dimension(1100, 640));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Sin esto, Swing le da el foco inicial al buscador (el primer
        // componente enfocable de la ventana), su texto fantasma se retira y
        // la barra aparece vacía y sin explicar qué se escribe ahí. El foco
        // arranca en el catálogo, que no dibuja ningún cerco.
        panelCatalogo.setFocusable(true);
        javax.swing.SwingUtilities.invokeLater(panelCatalogo::requestFocusInWindow);
    }

    // ---------------------------------------------------------------------
    // Barra superior
    // ---------------------------------------------------------------------

    /** Marca, buscador centrado y, a la derecha, carrito y avatar. */
    private JPanel construirBarraSuperior(Runnable alCerrarSesion) {
        JPanel barra = new JPanel(new BorderLayout(24, 0));
        barra.setBackground(fabrica.colorSidebar());
        barra.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel marca = new JPanel(new BorderLayout(10, 0));
        marca.setOpaque(false);
        marca.add(fabrica.crearLogo(TAM_LOGO), BorderLayout.WEST);
        JLabel lblMarca = new JLabel("Comercio Electrónico");
        lblMarca.setFont(fabrica.fuente(Font.BOLD, 16));
        lblMarca.setForeground(fabrica.colorTexto());
        marca.add(lblMarca, BorderLayout.CENTER);

        CampoTextoConIcono buscador = fabrica.crearCampoBusqueda("Buscar productos...");
        buscador.alEscribir(() -> accionBuscar.accept(buscador.getTexto()));
        JPanel contenedorBuscador = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        contenedorBuscador.setOpaque(false);
        contenedorBuscador.add(buscador);

        botonCarrito.alPulsar(this::alternarCajon);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        derecha.setOpaque(false);
        derecha.add(botonCarrito);
        derecha.add(construirAvatar(alCerrarSesion));

        barra.add(marca, BorderLayout.WEST);
        barra.add(contenedorBuscador, BorderLayout.CENTER);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    /** Avatar con iniciales que despliega el menú de usuario. */
    private JButton construirAvatar(Runnable alCerrarSesion) {
        JButton avatar = fabrica.crearAvatar(iniciales(nombreUsuario), TAM_AVATAR);
        avatar.setToolTipText(nombreUsuario);

        JPopupMenu menu = fabrica.crearMenuUsuario(nombreUsuario);
        fabrica.agregarOpcionMenu(menu, "Mis compras", () -> accionMisCompras.run());
        fabrica.agregarOpcionMenu(menu, "Editar cuenta", this::mostrarEditarCuenta);
        fabrica.agregarOpcionMenu(menu, "Cerrar sesión", () -> {
            dispose();
            alCerrarSesion.run();
        });

        avatar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                menu.show(avatar, -menu.getPreferredSize().width + avatar.getWidth(),
                        avatar.getHeight() + 6);
            }
        });
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
        // La rejilla va al norte de un contenedor: así las tarjetas conservan
        // su alto natural en vez de estirarse para llenar la ventana.
        JPanel contenedorRejilla = new JPanel(new BorderLayout());
        contenedorRejilla.setBackground(fabrica.colorFondo());
        contenedorRejilla.add(panelCatalogo, BorderLayout.NORTH);

        zona.add(panelCategorias, BorderLayout.NORTH);
        zona.add(fabrica.crearScroll(contenedorRejilla), BorderLayout.CENTER);
        return zona;
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

        JLabel lblNombre = new JLabel(producto.nombre());
        lblNombre.setFont(fabrica.fuente(Font.BOLD, 15));
        lblNombre.setForeground(fabrica.colorTexto());
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblNombre.setBorder(new EmptyBorder(6, 0, 8, 0));

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
        btnVer.addActionListener(e -> mostrarDetalle(producto));

        columna.add(lblCategoria);
        columna.add(lblNombre);
        columna.add(filaPrecio);
        columna.add(lblStock);
        columna.add(btnVer);

        tarjeta.add(imagen, BorderLayout.NORTH);
        tarjeta.add(columna, BorderLayout.CENTER);
        return tarjeta;
    }

    /** Etiqueta verde de descuento, compartida por la tarjeta y el detalle. */
    private JLabel insignia(String texto) {
        JLabel insignia = new JLabel(" " + texto + " ");
        insignia.setFont(fabrica.fuente(Font.BOLD, 11));
        insignia.setForeground(fabrica.colorFondo());
        insignia.setOpaque(true);
        insignia.setBackground(fabrica.colorExito());
        return insignia;
    }

    // ---------------------------------------------------------------------
    // Detalle del producto
    // ---------------------------------------------------------------------

    /** Ficha del producto: imagen a la izquierda, datos y cantidad a la derecha. */
    private void mostrarDetalle(TarjetaProducto producto) {
        JPanel detalle = new JPanel(new BorderLayout(20, 0));
        detalle.setOpaque(false);
        // Sin un ancho declarado, el diálogo se queda con el mínimo y recorta
        // la descripción por el lado derecho (comprobado en captura).
        detalle.setPreferredSize(new Dimension(ANCHO_DETALLE, 250));

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

        JLabel titulo = new JLabel(producto.nombre());
        titulo.setFont(fabrica.fuente(Font.BOLD, 20));
        titulo.setForeground(fabrica.colorTexto());
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setBorder(new EmptyBorder(6, 0, 10, 0));

        JLabel descripcion = new JLabel(enLineas(producto.descripcion()));
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
        datos.add(descripcion);
        datos.add(filaPrecio);
        datos.add(stock);

        int[] cantidad = {1};
        if (producto.hayExistencias()) {
            datos.add(construirSelectorCantidad(producto, cantidad));
        }

        detalle.add(marcoImagen, BorderLayout.WEST);
        detalle.add(datos, BorderLayout.CENTER);

        boolean confirmado = fabrica.mostrarDialogoConfirmacion(
                this, "Detalle del producto", detalle, "Agregar al carrito");
        if (confirmado && producto.hayExistencias()) {
            accionAgregar.accept(producto.id(), cantidad[0]);
            // Al agregar algo se abre el cajón: confirma que la acción surtió
            // efecto sin necesidad de ningún aviso.
            if (!cajonAbierto) {
                alternarCajon();
            }
        }
    }

    /** Selector "− n +" con el stock como tope. */
    private JPanel construirSelectorCantidad(TarjetaProducto producto, int[] cantidad) {
        JLabel lblCantidad = new JLabel("1", SwingConstants.CENTER);
        lblCantidad.setFont(fabrica.fuente(Font.BOLD, 16));
        lblCantidad.setForeground(fabrica.colorTexto());
        lblCantidad.setOpaque(true);
        lblCantidad.setBackground(fabrica.colorCampo());
        lblCantidad.setPreferredSize(new Dimension(52, 34));

        JButton menos = fabrica.crearBotonSegmento("-");
        JButton mas = fabrica.crearBotonSegmento("+");
        menos.addActionListener(e -> {
            cantidad[0] = Math.max(1, cantidad[0] - 1);
            lblCantidad.setText(String.valueOf(cantidad[0]));
        });
        mas.addActionListener(e -> {
            cantidad[0] = Math.min(Math.max(1, producto.stock()), cantidad[0] + 1);
            lblCantidad.setText(String.valueOf(cantidad[0]));
        });

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        selector.setOpaque(false);
        selector.setAlignmentX(Component.LEFT_ALIGNMENT);
        selector.setBorder(new EmptyBorder(16, 0, 0, 0));
        JLabel etiqueta = new JLabel("Cantidad:");
        etiqueta.setFont(fabrica.fuente(Font.BOLD, 12));
        etiqueta.setForeground(fabrica.colorTextoSuave());
        selector.add(etiqueta);
        selector.add(menos);
        selector.add(lblCantidad);
        selector.add(mas);
        return selector;
    }

    // ---------------------------------------------------------------------
    // Cajón del carrito
    // ---------------------------------------------------------------------

    /** Panel lateral derecho; arranca cerrado (ancho cero). */
    private JPanel construirCajonCarrito() {
        JPanel cajon = new JPanel(new BorderLayout());
        cajon.setBackground(fabrica.colorSidebar());
        cajon.setPreferredSize(new Dimension(0, 0));
        cajon.setBorder(new EmptyBorder(16, 16, 16, 16));

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
        panelLineasCarrito.setBackground(fabrica.colorSidebar());
        JPanel contenedorLineas = new JPanel(new BorderLayout());
        contenedorLineas.setBackground(fabrica.colorSidebar());
        contenedorLineas.add(panelLineasCarrito, BorderLayout.NORTH);
        JScrollPane desplazable = fabrica.crearScroll(contenedorLineas);
        desplazable.getViewport().setBackground(fabrica.colorSidebar());

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

    /** Renglón del carrito: miniatura, nombre, subtotal y botón de quitar. */
    private JPanel construirLineaCarrito(LineaCarrito linea) {
        JPanel fila = fabrica.crearTarjeta(new BorderLayout(10, 0));
        fila.setBorder(BorderFactory.createCompoundBorder(
                fila.getBorder(), new EmptyBorder(8, 10, 8, 10)));
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, TAM_MINIATURA + 24));

        JPanel texto = new JPanel();
        texto.setLayout(new BoxLayout(texto, BoxLayout.Y_AXIS));
        texto.setOpaque(false);
        JLabel nombre = new JLabel(linea.cantidad() + " x " + linea.nombre());
        nombre.setFont(fabrica.fuente(Font.BOLD, 12));
        nombre.setForeground(fabrica.colorTexto());
        nombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtotal = new JLabel(linea.subtotal());
        subtotal.setFont(fabrica.fuente(Font.PLAIN, 12));
        subtotal.setForeground(fabrica.colorTextoSuave());
        subtotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        texto.add(nombre);
        texto.add(subtotal);

        JButton quitar = fabrica.crearBotonSegmento("Quitar");
        quitar.addActionListener(e -> accionQuitar.accept(linea.idProducto()));

        fila.add(fabrica.crearImagenProducto(linea.imagen(), linea.nombre(),
                TAM_MINIATURA, TAM_MINIATURA), BorderLayout.WEST);
        fila.add(texto, BorderLayout.CENTER);
        fila.add(quitar, BorderLayout.EAST);
        return fila;
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
        fabrica.mostrarDialogoConfirmacion(this, "Mis compras", contenedor, "Cerrar");
    }

    private JLabel etiquetaCompra(String texto, int estilo, int tamano, Color color) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fabrica.fuente(estilo, tamano));
        etiqueta.setForeground(color);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return etiqueta;
    }

    private void mostrarEditarCuenta() {
        fabrica.mostrarDialogoExito(this, "La edición de cuenta llega en la siguiente entrega.");
    }

    @Override
    public void mostrarAviso(String mensaje) {
        fabrica.mostrarDialogoExito(this, mensaje);
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
    public void alConfirmarCompra(Runnable accion) {
        this.accionConfirmar = accion;
    }

    @Override
    public void alAbrirMisCompras(Runnable accion) {
        this.accionMisCompras = accion;
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
     */
    private String enLineas(String texto) {
        StringBuilder html = new StringBuilder("<html>");
        int usado = 0;
        for (String palabra : texto.split(" ")) {
            if (usado + palabra.length() > LARGO_LINEA_DESCRIPCION) {
                html.append("<br>");
                usado = 0;
            }
            html.append(palabra).append(' ');
            usado += palabra.length() + 1;
        }
        return html.append("</html>").toString();
    }
}
