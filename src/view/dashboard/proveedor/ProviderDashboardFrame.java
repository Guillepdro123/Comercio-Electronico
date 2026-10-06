package view.dashboard.proveedor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import view.core.MainFrame;
import view.dashboard.AccionesSesion;
import view.factory.IComponentesFactory;
import view.factory.charts.Barra;
import view.factory.charts.GraficoBarras;
import view.factory.components.CampoTextoConIcono;
import view.factory.components.SelectorSegmentado;
import view.factory.icons.IconoCampo;

/**
 * Ventana de trabajo del Proveedor: indicadores de venta y gestión del
 * catálogo propio.
 *
 * <p><b>Responsabilidad única:</b> igual que {@link view.dashboard.cliente.ClientDashboardFrame},
 * esta clase solo pinta y captura eventos. No sabe qué es un producto del
 * dominio, no suma ventas y no decide si un precio es válido: recibe
 * {@link FilaProducto} e {@link IndicadorVentas} ya resueltos por
 * {@link controller.ProveedorController} y le entrega de vuelta el texto que
 * se capturó ({@link DatosProducto}) para que él lo valide. Por eso no importa
 * nada de {@code model}.</p>
 *
 * <p>Sigue siendo un {@link JFrame} propio y maximizado, no una carta de
 * {@link MainFrame}: el espacio de trabajo del rol necesita ancho, justo lo
 * contrario del Login.</p>
 *
 * <p><b>Editar y Eliminar actúan sobre la fila seleccionada</b> en vez de
 * llevar un botón dentro de cada celda. Poner controles en las celdas obliga a
 * escribir editores y renderizadores propios para la tabla; seleccionar y usar
 * la barra de acciones es el mismo gesto, con componentes estándar.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 2.0
 */
public class ProviderDashboardFrame extends JFrame implements IProveedorDashboardView {

    private static final int TAM_LOGO = 38;
    private static final int TAM_AVATAR = 42;
    private static final int ALTO_INDICADORES = 116;
    private static final int ANCHO_GRAFICO = 470;
    /**
     * Ancho de los campos del formulario de producto. Es mayor que el ancho
     * estándar de la fábrica (pensado para el Login) porque aquí se captura
     * una descripción completa y porque el selector reparte cinco categorías
     * en tres columnas: con 300px, "Tecnología" sale recortada.
     */
    private static final int ANCHO_FORMULARIO = 430;
    private static final int ANCHO_MINIATURA = 96;
    private static final int ALTO_MINIATURA = 64;
    private static final String[] COLUMNAS =
            {"Producto", "Categoría", "Precio final", "Descuento", "Stock"};

    private final IComponentesFactory fabrica;
    private final String nombreUsuario;
    /** Lo que esta ventana pide fuera de ella; la vista solo lo dispara. */
    private final AccionesSesion acciones;

    private final JPanel panelIndicadores = new JPanel(new GridLayout(1, 4, 14, 0));
    private final DefaultTableModel modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            // Los cambios pasan por el formulario, que es donde el controlador
            // puede validarlos; editar la celda a mano los saltaría.
            return false;
        }
    };
    private final JTable tabla;
    private final JLabel lblVacio;
    private final GraficoBarras grafico;
    private final JLabel lblTituloGrafico;

    /** Filas actualmente en la tabla, en el mismo orden; de aquí sale la selección. */
    private final List<FilaProducto> filas = new ArrayList<>();
    private List<String> categorias = new ArrayList<>();

    private Consumer<DatosProducto> accionCrear = datos -> { };
    private BiConsumer<String, DatosProducto> accionActualizar = (id, datos) -> { };
    private Consumer<String> accionEliminar = id -> { };

    /**
     * @param fabrica       fábrica de la que salen todos los componentes
     * @param nombreUsuario nombre del proveedor, para el saludo y las iniciales
     * @param acciones      cerrar sesión, editar perfil, cambiar de tema y
     *                      volver a la tienda; las arma
     *                      {@link controller.SesionController}, que es quien
     *                      tiene el {@code Usuario} — esta ventana nunca lo
     *                      recibe
     */
    public ProviderDashboardFrame(IComponentesFactory fabrica, String nombreUsuario,
                                  AccionesSesion acciones) {
        this.acciones = acciones;
        this.fabrica = fabrica;
        this.nombreUsuario = nombreUsuario;
        this.tabla = fabrica.crearTabla(modeloTabla);
        this.grafico = fabrica.crearGraficoBarras();
        this.lblTituloGrafico = new JLabel("Rendimiento");
        lblTituloGrafico.setFont(fabrica.fuente(Font.BOLD, 16));
        lblTituloGrafico.setForeground(fabrica.colorTexto());

        lblVacio = new JLabel("Todavía no has publicado productos. Empieza con \"Nuevo producto\".");
        lblVacio.setFont(fabrica.fuente(Font.PLAIN, 13));
        lblVacio.setForeground(fabrica.colorTextoSuave());
        lblVacio.setBorder(new EmptyBorder(18, 4, 0, 0));

        setTitle("Plataforma E-Commerce | Panel de Proveedor");
        // La X termina el programa: esta es la ventana principal mientras
        // hay sesión, y cerrarla debe detener también los hilos de fondo
        // (conexión a Mongo, SwingWorker). La sesión recordada NO se borra:
        // cerrar la ventana no es cerrar sesión. "Cerrar sesión" sigue
        // usando dispose(), que no dispara esta operación.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(fabrica.colorFondo());
        contenido.add(construirBarraSuperior(), BorderLayout.NORTH);
        contenido.add(construirCuerpo(), BorderLayout.CENTER);

        setContentPane(contenido);
        setMinimumSize(new Dimension(1100, 640));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    // ---------------------------------------------------------------------
    // Construcción de la interfaz
    // ---------------------------------------------------------------------

    /**
     * Barra superior: marca a la izquierda; a la derecha, volver a la tienda,
     * el tema y el avatar con el menú de usuario.
     *
     * <p>"Ir a la tienda" está a la vista y no solo en el menú: el proveedor
     * también compra (doble rol) y volver al catálogo es tan frecuente como
     * entrar aquí.</p>
     */
    private JPanel construirBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout(20, 0));
        barra.setBackground(fabrica.colorSidebar());
        barra.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel marca = new JPanel(new BorderLayout(10, 0));
        marca.setOpaque(false);
        marca.add(fabrica.crearLogo(TAM_LOGO), BorderLayout.WEST);
        JLabel lblMarca = new JLabel("Comercio Electrónico");
        lblMarca.setFont(fabrica.fuente(Font.BOLD, 16));
        lblMarca.setForeground(fabrica.colorTextoSidebar());
        marca.add(lblMarca, BorderLayout.CENTER);

        JButton btnTienda = fabrica.crearBotonSegmento("Ir a la tienda");
        btnTienda.setToolTipText("Volver al catálogo para comprar");
        btnTienda.addActionListener(e -> acciones.cambiarPanel().run());
        JButton btnTema = fabrica.crearBotonTema(true, false);
        btnTema.addActionListener(e -> acciones.cambiarTema().run());

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        derecha.setOpaque(false);
        derecha.add(btnTienda);
        derecha.add(btnTema);
        derecha.add(construirAvatar());

        barra.add(marca, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    private JButton construirAvatar() {
        JButton avatar = fabrica.crearAvatar(iniciales(nombreUsuario), TAM_AVATAR);
        avatar.setToolTipText(nombreUsuario);

        JPopupMenu menu = fabrica.crearMenuUsuario(nombreUsuario);
        fabrica.agregarOpcionMenu(menu, "Ir a la tienda", acciones.cambiarPanel());
        fabrica.agregarOpcionMenu(menu, "Editar perfil", acciones.editarPerfil());
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

    /** Saludo e indicadores arriba, tabla de gestión abajo. */
    private JPanel construirCuerpo() {
        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setBackground(fabrica.colorFondo());
        cuerpo.setBorder(new EmptyBorder(18, 24, 20, 24));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(fabrica.colorFondo());
        encabezado.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel saludo = new JPanel();
        saludo.setLayout(new BoxLayout(saludo, BoxLayout.Y_AXIS));
        saludo.setOpaque(false);
        saludo.setBorder(new EmptyBorder(0, 0, 14, 0));
        JLabel lblTitulo = new JLabel("Panel de Proveedor");
        lblTitulo.setFont(fabrica.fuente(Font.BOLD, 22));
        lblTitulo.setForeground(fabrica.colorTexto());
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblBienvenida = new JLabel("Hola, " + nombreUsuario);
        lblBienvenida.setFont(fabrica.fuente(Font.PLAIN, 13));
        lblBienvenida.setForeground(fabrica.colorTextoSuave());
        lblBienvenida.setAlignmentX(Component.LEFT_ALIGNMENT);
        saludo.add(lblTitulo);
        saludo.add(lblBienvenida);

        panelIndicadores.setBackground(fabrica.colorFondo());
        panelIndicadores.setPreferredSize(new Dimension(0, ALTO_INDICADORES));

        encabezado.add(saludo, BorderLayout.NORTH);
        encabezado.add(panelIndicadores, BorderLayout.CENTER);

        JPanel trabajo = new JPanel(new BorderLayout(16, 0));
        trabajo.setBackground(fabrica.colorFondo());
        trabajo.add(construirZonaGrafico(), BorderLayout.WEST);
        trabajo.add(construirZonaCatalogo(), BorderLayout.CENTER);

        cuerpo.add(encabezado, BorderLayout.NORTH);
        cuerpo.add(trabajo, BorderLayout.CENTER);
        return cuerpo;
    }

    /** Tarjeta con el gráfico de rendimiento, a la izquierda del catálogo. */
    private JPanel construirZonaGrafico() {
        JPanel zona = fabrica.crearTarjeta(new BorderLayout());
        zona.setBorder(BorderFactory.createCompoundBorder(
                zona.getBorder(), new EmptyBorder(14, 16, 16, 16)));
        zona.setPreferredSize(new Dimension(ANCHO_GRAFICO, 0));

        lblTituloGrafico.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        // El gráfico va al norte para conservar su alto natural (una franja
        // por barra) en vez de estirarse hasta el fondo de la tarjeta.
        contenedor.add(grafico, BorderLayout.NORTH);

        zona.add(lblTituloGrafico, BorderLayout.NORTH);
        zona.add(contenedor, BorderLayout.CENTER);
        return zona;
    }

    /** Barra de acciones y tabla de productos, dentro de una tarjeta. */
    private JPanel construirZonaCatalogo() {
        JPanel zona = fabrica.crearTarjeta(new BorderLayout());
        zona.setBorder(BorderFactory.createCompoundBorder(
                zona.getBorder(), new EmptyBorder(14, 16, 16, 16)));

        JPanel acciones = new JPanel(new BorderLayout());
        acciones.setOpaque(false);
        acciones.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel titulo = new JLabel("Mis productos");
        titulo.setFont(fabrica.fuente(Font.BOLD, 16));
        titulo.setForeground(fabrica.colorTexto());

        JButton btnNuevo = fabrica.crearBotonPrimario("NUEVO PRODUCTO");
        btnNuevo.setPreferredSize(new Dimension(180, 36));
        btnNuevo.addActionListener(e -> abrirFormulario(null));

        JButton btnEditar = fabrica.crearBotonSegmento("Editar");
        btnEditar.addActionListener(e -> conSeleccion(this::abrirFormulario));

        JButton btnEliminar = fabrica.crearBotonSegmento("Eliminar");
        btnEliminar.addActionListener(e -> conSeleccion(this::confirmarEliminacion));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnNuevo);

        acciones.add(titulo, BorderLayout.WEST);
        acciones.add(botones, BorderLayout.EAST);

        JPanel contenedorTabla = new JPanel(new BorderLayout());
        contenedorTabla.setOpaque(false);
        contenedorTabla.add(tabla.getTableHeader(), BorderLayout.NORTH);
        contenedorTabla.add(tabla, BorderLayout.CENTER);
        contenedorTabla.add(lblVacio, BorderLayout.SOUTH);

        JPanel ajuste = new JPanel(new BorderLayout());
        ajuste.setOpaque(false);
        // La tabla va al norte de un contenedor para que conserve su alto
        // natural (una fila por producto) en vez de estirarse hasta el fondo.
        ajuste.add(contenedorTabla, BorderLayout.NORTH);

        // El desplazable vive dentro de una tarjeta, así que su fondo tiene
        // que ser el de la tarjeta y no el de la ventana: de lo contrario, el
        // espacio bajo la última fila se ve como un recuadro negro pegado.
        JScrollPane desplazable = fabrica.crearScroll(ajuste);
        desplazable.getViewport().setBackground(fabrica.colorPanel());

        zona.add(acciones, BorderLayout.NORTH);
        zona.add(desplazable, BorderLayout.CENTER);
        return zona;
    }

    // ---------------------------------------------------------------------
    // IProveedorDashboardView
    // ---------------------------------------------------------------------

    @Override
    public void mostrarIndicadores(IndicadorVentas indicadores) {
        panelIndicadores.removeAll();
        // Cada cifra con su color: de un vistazo se distingue dinero (verde),
        // volumen (azul), pedidos (naranja) y catálogo (marca), sin leer el
        // rótulo. Las notas dan el segundo dato que cada cifra pide.
        panelIndicadores.add(construirIndicador("Ingresos acumulados", indicadores.ingresos(),
                "Ticket promedio " + indicadores.ticketPromedio(),
                fabrica.colorExito(), false));
        panelIndicadores.add(construirIndicador("Unidades vendidas",
                String.valueOf(indicadores.unidadesVendidas()),
                indicadores.productoEstrella().isEmpty()
                        ? "Aún sin ventas"
                        : "Más vendido: " + indicadores.productoEstrella(),
                fabrica.colorInformacion(), false));
        panelIndicadores.add(construirIndicador("Pedidos",
                String.valueOf(indicadores.pedidos()), "Compras con productos tuyos",
                fabrica.colorDestacado(), false));
        panelIndicadores.add(construirIndicador("Catálogo publicado",
                String.valueOf(indicadores.publicados()),
                indicadores.agotados() == 1
                        ? "1 sin existencias"
                        : indicadores.agotados() + " sin existencias",
                fabrica.colorAcento(), indicadores.agotados() > 0));
        panelIndicadores.revalidate();
        panelIndicadores.repaint();
    }

    /**
     * Tarjeta de un indicador: rótulo arriba, cifra grande y nota al pie.
     *
     * <p>La franja de color del borde superior es lo que le da peso visual
     * frente al resto del panel, y se consigue componiendo bordes estándar
     * ({@code MatteBorder}), sin pintar nada a mano.</p>
     *
     * @param color      color propio del indicador (franja y rótulo)
     * @param notaAlerta {@code true} si la nota señala algo que atender, y
     *                   entonces va en el color de error
     */
    private JPanel construirIndicador(String rotulo, String cifra, String nota,
                                      Color color, boolean notaAlerta) {
        JPanel tarjeta = fabrica.crearTarjeta(new BorderLayout());
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                tarjeta.getBorder(),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(4, 0, 0, 0, color),
                        new EmptyBorder(12, 18, 12, 16))));

        JPanel columna = new JPanel();
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setOpaque(false);

        JLabel lblRotulo = new JLabel(rotulo.toUpperCase());
        lblRotulo.setFont(fabrica.fuente(Font.BOLD, 11));
        lblRotulo.setForeground(color);
        lblRotulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCifra = new JLabel(cifra);
        lblCifra.setFont(fabrica.fuente(Font.BOLD, 28));
        lblCifra.setForeground(fabrica.colorTexto());
        lblCifra.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblCifra.setBorder(new EmptyBorder(4, 0, 2, 0));

        JLabel lblNota = new JLabel(nota);
        lblNota.setFont(fabrica.fuente(notaAlerta ? Font.BOLD : Font.PLAIN, 12));
        lblNota.setForeground(notaAlerta ? fabrica.colorError() : fabrica.colorTextoSuave());
        lblNota.setToolTipText(nota);
        lblNota.setAlignmentX(Component.LEFT_ALIGNMENT);

        columna.add(lblRotulo);
        columna.add(lblCifra);
        columna.add(lblNota);
        tarjeta.add(columna, BorderLayout.CENTER);
        return tarjeta;
    }

    @Override
    public void mostrarProductos(List<FilaProducto> productos) {
        filas.clear();
        filas.addAll(productos);

        modeloTabla.setRowCount(0);
        for (FilaProducto fila : productos) {
            modeloTabla.addRow(new Object[]{
                    fila.nombre(),
                    fila.categoria(),
                    fila.precioFinal(),
                    fila.descuento() == 0 ? "Sin descuento" : "-" + fila.descuento() + "%",
                    fila.stock() == 0 ? "Agotado" : String.valueOf(fila.stock())});
        }
        lblVacio.setVisible(productos.isEmpty());
        tabla.revalidate();
        tabla.repaint();
    }

    @Override
    public void mostrarGrafico(String titulo, List<BarraVentas> barras, String mensajeVacio) {
        lblTituloGrafico.setText(titulo);
        // Traducción de un registro a otro: el gráfico es un componente
        // reutilizable con su propio tipo de dato y no debe conocer
        // 'view.dashboard'; el controlador, a su vez, no debe conocer las
        // clases internas de la fábrica. La vista es quien une ambos lados.
        List<Barra> datos = new ArrayList<>();
        for (BarraVentas barra : barras) {
            datos.add(new Barra(barra.etiqueta(), barra.valor(), barra.texto()));
        }
        grafico.mostrar(datos, mensajeVacio);
    }

    @Override
    public void mostrarCategorias(List<String> categorias) {
        this.categorias = new ArrayList<>(categorias);
    }

    @Override
    public void mostrarAviso(String mensaje) {
        fabrica.mostrarDialogoAviso(this, mensaje);
    }

    @Override
    public void mostrarExito(String mensaje) {
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
    public void alCrearProducto(Consumer<DatosProducto> accion) {
        this.accionCrear = accion;
    }

    @Override
    public void alActualizarProducto(BiConsumer<String, DatosProducto> accion) {
        this.accionActualizar = accion;
    }

    @Override
    public void alEliminarProducto(Consumer<String> accion) {
        this.accionEliminar = accion;
    }

    // ---------------------------------------------------------------------
    // Formulario de producto
    // ---------------------------------------------------------------------

    /**
     * Abre el formulario de producto.
     *
     * @param existente producto a editar, o {@code null} para crear uno nuevo
     *                  (un solo formulario para los dos casos: los campos son
     *                  los mismos y duplicarlo solo duplicaría los errores)
     */
    private void abrirFormulario(FilaProducto existente) {
        CampoTextoConIcono txtNombre = fabrica.crearCampoTexto(
                "Nombre del producto", IconoCampo.Tipo.ETIQUETA);
        CampoTextoConIcono txtDescripcion = fabrica.crearCampoTexto(
                "Descripción breve", IconoCampo.Tipo.TEXTO);
        CampoTextoConIcono txtPrecio = fabrica.crearCampoTexto(
                "Precio, por ejemplo 150000", IconoCampo.Tipo.PRECIO);
        CampoTextoConIcono txtDescuento = fabrica.crearCampoTexto(
                "Descuento de 0 a 100", IconoCampo.Tipo.DESCUENTO);
        CampoTextoConIcono txtStock = fabrica.crearCampoTexto(
                "Unidades disponibles", IconoCampo.Tipo.CAJA);

        SelectorSegmentado selectorCategoria = fabrica.crearSelectorOpciones(
                categorias.toArray(new String[0]), () -> { });

        if (existente != null) {
            txtNombre.setTexto(existente.nombre());
            txtDescripcion.setTexto(existente.descripcion());
            txtPrecio.setTexto(existente.precio());
            txtDescuento.setTexto(String.valueOf(existente.descuento()));
            txtStock.setTexto(String.valueOf(existente.stock()));
            selectorCategoria.seleccionar(existente.categoria());
        }

        // MigLayout en una columna de ancho fijo. Con BoxLayout había que
        // alinear cada componente a mano y toparle el máximo a su altura
        // preferida, o los campos se estiraban al repartirse la altura
        // sobrante; aquí basta con declarar la columna una vez.
        JPanel formulario = new JPanel(new MigLayout(
                "wrap 1, insets 0, gapy 0", "[" + ANCHO_FORMULARIO + "!]"));
        formulario.setOpaque(false);
        agregarFila(formulario, "Nombre", txtNombre);
        agregarFila(formulario, "Descripción", txtDescripcion);
        agregarFila(formulario, "Categoría", selectorCategoria);
        agregarFila(formulario, "Precio de lista", txtPrecio);
        agregarFila(formulario, "Descuento (%)", txtDescuento);
        agregarFila(formulario, "Stock", txtStock);
        // La imagen ya no se teclea: se elige del disco y se ve antes de
        // guardar. La ruta viaja en un arreglo de una posición porque el
        // listener del botón tiene que poder cambiarla.
        String[] rutaImagen = {existente == null ? "" : existente.imagen()};
        formulario.add(fabrica.crearEtiqueta("Imagen del producto"), "gaptop 8, gapbottom 4");
        formulario.add(construirSelectorImagen(rutaImagen,
                existente == null ? "" : existente.nombre()), "growx");

        boolean guardar = fabrica.mostrarDialogoConfirmacion(this,
                existente == null ? "Nuevo producto" : "Editar producto",
                formulario, "Guardar");
        if (!guardar) {
            return;
        }

        DatosProducto datos = new DatosProducto(txtNombre.getTexto(), txtDescripcion.getTexto(),
                txtPrecio.getTexto(), txtDescuento.getTexto(),
                selectorCategoria.getSeleccionado(), txtStock.getTexto(),
                rutaImagen[0]);
        if (existente == null) {
            accionCrear.accept(datos);
        } else {
            accionActualizar.accept(existente.id(), datos);
        }
    }

    /**
     * Fila de imagen del formulario: miniatura a la izquierda, botón de
     * elegir y nombre del archivo a la derecha.
     *
     * <p><b>Por qué se ve la miniatura.</b> Una ruta escrita no dice si el
     * archivo es el correcto ni si el programa puede leerlo; con la vista
     * previa, el proveedor confirma las dos cosas antes de guardar. Si la
     * imagen no se puede cargar, el recuadro cae en la inicial del producto,
     * que es justo la señal de que algo no funcionó.</p>
     *
     * @param rutaImagen    celda con la ruta actual; el botón la reescribe
     * @param nombreProducto nombre para el recuadro de respaldo
     * @return la fila lista para agregar al formulario
     */
    private JPanel construirSelectorImagen(String[] rutaImagen, String nombreProducto) {
        JPanel fila = new JPanel(new MigLayout("insets 0, gapx 12", "[]" + "[grow,fill]"));
        fila.setOpaque(false);

        JPanel marco = new JPanel(new BorderLayout());
        marco.setOpaque(false);
        marco.add(fabrica.crearImagenProducto(rutaImagen[0], nombreProducto,
                ANCHO_MINIATURA, ALTO_MINIATURA), BorderLayout.CENTER);

        JLabel lblArchivo = new JLabel(nombreDeArchivo(rutaImagen[0]));
        lblArchivo.setFont(fabrica.fuente(Font.PLAIN, 11));
        lblArchivo.setForeground(fabrica.colorTextoSuave());

        JButton btnElegir = fabrica.crearBotonSegmento("Seleccionar imagen");
        btnElegir.addActionListener(e -> {
            String elegida = fabrica.elegirImagen(this, rutaImagen[0]);
            if (elegida == null) {
                return;
            }
            rutaImagen[0] = elegida;
            lblArchivo.setText(nombreDeArchivo(elegida));
            marco.removeAll();
            marco.add(fabrica.crearImagenProducto(elegida, nombreProducto,
                    ANCHO_MINIATURA, ALTO_MINIATURA), BorderLayout.CENTER);
            marco.revalidate();
            marco.repaint();
        });

        JPanel columna = new JPanel(new MigLayout("wrap 1, insets 0, gapy 6", "[grow,fill]"));
        columna.setOpaque(false);
        columna.add(btnElegir);
        columna.add(lblArchivo);

        fila.add(marco);
        fila.add(columna);
        return fila;
    }

    /** @return solo el nombre del archivo, o un aviso si todavía no hay imagen */
    private String nombreDeArchivo(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return "Sin imagen seleccionada";
        }
        return new java.io.File(ruta).getName();
    }

    /**
     * Agrega al formulario una etiqueta y, debajo, su campo.
     *
     * <p>El ancho lo impone la columna del {@link MigLayout} ({@code !} = ancho
     * exacto), así que aquí ya no hace falta forzar {@code preferredSize} ni
     * {@code maximumSize} en cada control, como sí requería el
     * {@code BoxLayout} anterior para que los campos no se estiraran.</p>
     *
     * @param formulario contenedor del formulario
     * @param etiqueta   rótulo de la fila
     * @param campo      control de captura
     */
    private void agregarFila(JPanel formulario, String etiqueta, JComponent campo) {
        formulario.add(fabrica.crearEtiqueta(etiqueta), "gaptop 8, gapbottom 4");
        formulario.add(campo, "growx");
    }

    // ---------------------------------------------------------------------
    // Selección y borrado
    // ---------------------------------------------------------------------

    /**
     * Ejecuta una acción sobre la fila seleccionada, o avisa si no hay
     * ninguna. Editar y Eliminar necesitan exactamente lo mismo, así que la
     * comprobación vive en un solo sitio.
     */
    private void conSeleccion(Consumer<FilaProducto> accion) {
        int seleccionada = tabla.getSelectedRow();
        if (seleccionada < 0 || seleccionada >= filas.size()) {
            mostrarAviso("Selecciona primero un producto de la tabla.");
            return;
        }
        accion.accept(filas.get(seleccionada));
    }

    /**
     * Pide confirmación antes de borrar. Preguntar es presentación, no regla
     * de negocio: el controlador recibe la orden solo si el usuario aceptó.
     */
    private void confirmarEliminacion(FilaProducto fila) {
        JLabel pregunta = new JLabel("Se quitará " + fila.nombre()
                + " del catálogo. Esta acción no se puede deshacer.");
        pregunta.setFont(fabrica.fuente(Font.PLAIN, 13));
        pregunta.setForeground(fabrica.colorTexto());

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setOpaque(false);
        contenido.add(pregunta, BorderLayout.CENTER);

        if (fabrica.mostrarDialogoConfirmacion(this, "Eliminar producto", contenido, "Eliminar")) {
            accionEliminar.accept(fila.id());
        }
    }

    /** @return una o dos iniciales del nombre, para el avatar */
    private String iniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, 1).toUpperCase();
        }
        return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
    }
}
