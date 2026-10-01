package controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.entity.Carrito;
import model.entity.Categoria;
import model.entity.LineaPedido;
import model.entity.Pedido;
import model.entity.Producto;
import model.entity.Usuario;
import model.repository.IPedidoRepository;
import model.repository.IProductoRepository;
import service.INotificadorPedido;
import service.ResumenPedido;
import view.dashboard.IClienteDashboardView;
import view.dashboard.LineaCarrito;
import view.dashboard.ResumenCompra;
import view.dashboard.TarjetaProducto;

/**
 * Controlador de los casos de uso del Cliente: recorrer el catálogo, armar el
 * carrito y confirmar la compra.
 *
 * <p>Es el primer controlador de panel del proyecto, y existe porque ahora sí
 * hay acciones reales que coordinar; hasta el Incremento 2 los dashboards eran
 * pantallas de aterrizaje sin lógica y por eso no tenían controlador.</p>
 *
 * <p><b>Inversión de dependencias:</b> depende de {@link IProductoRepository},
 * {@link IPedidoRepository}, {@link INotificadorPedido} y
 * {@link IClienteDashboardView} — todas abstracciones. Por eso el mismo
 * controlador sirve con los repositorios en memoria de hoy y con MongoDB
 * Atlas mañana, sin tocar una línea.</p>
 *
 * <p><b>La vista no conoce el modelo:</b> los productos, el carrito y las
 * compras se le entregan como registros propios de la capa de vista
 * ({@link TarjetaProducto}, {@link LineaCarrito}, {@link ResumenCompra}), ya
 * con los importes formateados.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class ClienteController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IClienteDashboardView vista;
    private final IProductoRepository productos;
    private final IPedidoRepository pedidos;
    private final INotificadorPedido notificador;
    private final Usuario usuario;

    private final Carrito carrito = new Carrito();
    private Categoria categoriaActiva;
    private String textoBuscado = "";

    /**
     * Conecta la vista con los repositorios y deja registradas las acciones.
     *
     * @param vista       panel del Cliente (abstracción)
     * @param productos   catálogo (abstracción)
     * @param pedidos     almacén de órdenes (abstracción)
     * @param notificador aviso de compra confirmada (abstracción)
     * @param usuario     cliente autenticado; de él salen el correo y la
     *                    dirección de entrega que se asocian al pedido
     */
    public ClienteController(IClienteDashboardView vista, IProductoRepository productos,
                             IPedidoRepository pedidos, INotificadorPedido notificador,
                             Usuario usuario) {
        this.vista = vista;
        this.productos = productos;
        this.pedidos = pedidos;
        this.notificador = notificador;
        this.usuario = usuario;

        vista.alBuscar(this::buscar);
        vista.alElegirCategoria(this::filtrarPorCategoria);
        vista.alAgregarAlCarrito(this::agregarAlCarrito);
        vista.alQuitarDelCarrito(this::quitarDelCarrito);
        vista.alConfirmarCompra(this::confirmarCompra);
        vista.alAbrirMisCompras(this::mostrarMisCompras);
    }

    /** Carga el catálogo inicial y el filtro de categorías. */
    public void iniciar() {
        List<String> etiquetas = new ArrayList<>();
        etiquetas.add(IClienteDashboardView.CATEGORIA_TODAS);
        for (Categoria categoria : Categoria.values()) {
            etiquetas.add(categoria.getEtiqueta());
        }
        vista.mostrarCategorias(etiquetas);
        refrescarCatalogo();
        refrescarCarrito();
    }

    // ---------------------------------------------------------------------
    // Catálogo
    // ---------------------------------------------------------------------

    private void buscar(String texto) {
        textoBuscado = texto == null ? "" : texto;
        refrescarCatalogo();
    }

    private void filtrarPorCategoria(String etiqueta) {
        categoriaActiva = null;
        for (Categoria categoria : Categoria.values()) {
            if (categoria.getEtiqueta().equals(etiqueta)) {
                categoriaActiva = categoria;
            }
        }
        refrescarCatalogo();
    }

    /**
     * Vuelve a pedir el catálogo aplicando búsqueda y categoría a la vez.
     *
     * <p>El filtrado lo hace el repositorio, no este controlador: así, cuando
     * detrás haya una base de datos, se traducirá en una consulta en vez de
     * traer todo el catálogo para descartarlo aquí.</p>
     */
    private void refrescarCatalogo() {
        List<TarjetaProducto> tarjetas = new ArrayList<>();
        for (Producto producto : productos.buscar(textoBuscado, categoriaActiva)) {
            tarjetas.add(aTarjeta(producto));
        }
        vista.mostrarProductos(tarjetas);
    }

    private TarjetaProducto aTarjeta(Producto producto) {
        return new TarjetaProducto(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.tieneDescuento() ? ResumenPedido.moneda(producto.getPrecio()) : "",
                ResumenPedido.moneda(producto.getPrecioFinal()),
                producto.getPorcentajeDescuento(),
                producto.getCategoria().getEtiqueta(),
                producto.getStock(),
                producto.getImagen());
    }

    // ---------------------------------------------------------------------
    // Carrito
    // ---------------------------------------------------------------------

    private void agregarAlCarrito(String idProducto, Integer cantidad) {
        Producto producto = productos.buscarPorId(idProducto);
        if (producto == null) {
            vista.mostrarAviso("Ese producto ya no está disponible.");
            return;
        }
        if (cantidad == null || cantidad <= 0) {
            vista.mostrarAviso("Elige al menos una unidad.");
            return;
        }
        // Se compara contra el stock real y no contra lo que mostraba la
        // tarjeta: entre que se pintó el catálogo y se pulsó "agregar", otro
        // cliente pudo llevarse las últimas unidades.
        if (cantidad > producto.getStock()) {
            vista.mostrarAviso("Solo quedan " + producto.getStock() + " unidades de "
                    + producto.getNombre() + ".");
            return;
        }
        carrito.agregar(producto, cantidad);
        refrescarCarrito();
    }

    private void quitarDelCarrito(String idProducto) {
        carrito.quitar(idProducto);
        refrescarCarrito();
    }

    private void refrescarCarrito() {
        List<LineaCarrito> lineas = new ArrayList<>();
        for (LineaPedido linea : carrito.getLineas()) {
            // La miniatura se busca en el catálogo vivo: el renglón del
            // pedido guarda a propósito el precio del momento, no la ficha
            // completa del producto.
            Producto producto = productos.buscarPorId(linea.getIdProducto());
            lineas.add(new LineaCarrito(linea.getIdProducto(), linea.getNombreProducto(),
                    linea.getCantidad(), ResumenPedido.moneda(linea.getSubtotal()),
                    producto == null ? "" : producto.getImagen()));
        }
        vista.mostrarCarrito(lineas, ResumenPedido.moneda(carrito.getTotal()));
    }

    // ---------------------------------------------------------------------
    // Compra
    // ---------------------------------------------------------------------

    /**
     * Confirma la compra: comprueba existencias, registra el pedido con la
     * dirección del usuario, descuenta el stock, vacía el carrito y avisa.
     *
     * <p>El stock se descuenta <em>después</em> de comprobar que alcanza para
     * todos los renglones, no renglón por renglón sobre la marcha: si el
     * último artículo fallara a mitad del proceso, los anteriores ya estarían
     * descontados y la compra quedaría a medias.</p>
     */
    private void confirmarCompra() {
        if (carrito.estaVacio()) {
            vista.mostrarAviso("Tu carrito está vacío.");
            return;
        }
        String faltante = primerProductoSinExistencias();
        if (faltante != null) {
            vista.mostrarAviso("No hay unidades suficientes de " + faltante + ".");
            return;
        }

        Pedido pedido = pedidos.registrar(new Pedido(null,
                usuario.getCorreo(), usuario.getNombres(),
                carrito.getLineas(), direccionDeEntrega()));

        for (LineaPedido linea : pedido.getLineas()) {
            productos.descontarStock(linea.getIdProducto(), linea.getCantidad());
        }
        carrito.vaciar();

        notificador.notificarCompra(pedido, usuario.getCorreo());

        refrescarCatalogo();
        refrescarCarrito();
        vista.mostrarExito("Pedido " + pedido.getId() + " confirmado por "
                + ResumenPedido.moneda(pedido.getTotal())
                + ". Te llegará a: " + pedido.getDireccionEntrega());
    }

    /** @return nombre del primer producto cuyo stock no alcanza, o {@code null} si todo está bien */
    private String primerProductoSinExistencias() {
        for (LineaPedido linea : carrito.getLineas()) {
            Producto producto = productos.buscarPorId(linea.getIdProducto());
            if (producto == null || producto.getStock() < linea.getCantidad()) {
                return linea.getNombreProducto();
            }
        }
        return null;
    }

    /**
     * Dirección a la que se envía el pedido.
     *
     * <p>Sale del dato que el usuario registró al crear su cuenta
     * ({@code getDatoEspecifico()}, polimórfico: es la dirección de envío en
     * un Cliente). El controlador no pregunta de qué subclase se trata.</p>
     */
    private String direccionDeEntrega() {
        String direccion = usuario.getDatoEspecifico();
        return direccion == null || direccion.isBlank()
                ? "Sin dirección registrada" : direccion;
    }

    // ---------------------------------------------------------------------
    // Mis compras
    // ---------------------------------------------------------------------

    private void mostrarMisCompras() {
        List<ResumenCompra> resumenes = new ArrayList<>();
        for (Pedido pedido : pedidos.listarPorUsuario(usuario.getCorreo())) {
            List<String> articulos = new ArrayList<>();
            for (LineaPedido linea : pedido.getLineas()) {
                articulos.add(linea.getCantidad() + " x " + linea.getNombreProducto()
                        + "   " + ResumenPedido.moneda(linea.getSubtotal()));
            }
            resumenes.add(new ResumenCompra(pedido.getId(), pedido.getFecha().format(FECHA),
                    ResumenPedido.moneda(pedido.getTotal()), pedido.getDireccionEntrega(), articulos));
        }
        vista.mostrarCompras(resumenes);
    }
}
