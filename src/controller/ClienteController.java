package controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import aplicacion.compra.CompraService;
import aplicacion.compra.ResultadoCompra;
import aplicacion.cuenta.CuentaService;
import aplicacion.resena.ResenaService;
import model.entity.Carrito;
import model.entity.Categoria;
import model.entity.LineaPedido;
import model.entity.Pedido;
import model.entity.Producto;
import model.entity.Resena;
import model.entity.Usuario;
import model.repository.IProductoRepository;
import service.correo.ResumenPedido;
import view.dashboard.cliente.DatosEnvio;
import view.dashboard.cliente.IClienteDashboardView;
import view.dashboard.cliente.LineaCarrito;
import view.dashboard.cliente.NuevaResena;
import view.dashboard.cliente.Promocion;
import view.dashboard.cliente.ResenaVista;
import view.dashboard.cliente.ResumenCompra;
import view.dashboard.cliente.ResumenResenas;
import view.dashboard.cliente.TarjetaProducto;

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
    /**
     * Probabilidad de ofrecer el pop-up promocional al entrar. Ocasional a
     * propósito: un aviso que sale en cada acceso se aprende a cerrar sin
     * leerlo.
     */
    private static final double PROBABILIDAD_PROMOCION = 0.4;
    /** Más diapositivas que esto y la última tarda demasiado en volver a salir. */
    private static final int PROMOCIONES_EN_BANNER = 4;

    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    /** Promedio y cantidad de un producto sin reseñas. */
    private static final double[] SIN_RESENAS = {0, 0};

    private final IClienteDashboardView vista;
    private final IProductoRepository productos;
    /** Caso de uso de la compra: aquí no queda ninguna regla de negocio. */
    private final CompraService compras;
    private final ResenaService resenas;
    private final CuentaService cuentas;
    private final Usuario usuario;

    /**
     * El carrito es de la sesión, no de esta pantalla: llega de fuera para
     * que sobreviva a cambiar de tema o a ir al panel del proveedor y volver,
     * que reconstruyen la ventana y este controlador.
     */
    private final Carrito carrito;
    private Categoria categoriaActiva;
    private String textoBuscado = "";
    /** Promedio y cantidad de reseñas por producto, de la última recarga. */
    private Map<String, double[]> resumenResenas = Map.of();

    /**
     * Conecta la vista con los casos de uso y deja registradas las acciones.
     *
     * @param vista     panel del Cliente (abstracción)
     * @param productos catálogo, para recorrerlo y armar el carrito
     * @param compras   caso de uso de la compra y del historial
     * @param resenas   caso de uso de las reseñas
     * @param cuentas   caso de uso de la cuenta, para completar los datos de
     *                  envío antes de comprar
     * @param usuario   usuario autenticado (Cliente o Proveedor: todos compran)
     * @param carrito   carrito de la sesión
     */
    public ClienteController(IClienteDashboardView vista, IProductoRepository productos,
                             CompraService compras, ResenaService resenas,
                             CuentaService cuentas, Usuario usuario, Carrito carrito) {
        this.vista = vista;
        this.productos = productos;
        this.compras = compras;
        this.resenas = resenas;
        this.cuentas = cuentas;
        this.usuario = usuario;
        this.carrito = carrito;

        vista.alBuscar(this::buscar);
        vista.alElegirCategoria(this::filtrarPorCategoria);
        vista.alVerDetalle(this::abrirDetalle);
        vista.alPublicarResena(this::publicarResena);
        vista.alAgregarAlCarrito(this::agregarAlCarrito);
        vista.alQuitarDelCarrito(this::quitarDelCarrito);
        vista.alCambiarCantidad(this::cambiarCantidad);
        vista.alConfirmarCompra(this::confirmarCompra);
        vista.alAbrirMisCompras(this::mostrarMisCompras);
        // Cuando el catálogo cambia en otro sitio (el Proveedor publica,
        // edita o elimina), la vista recibe el aviso y pide esta misma
        // recarga: conserva la búsqueda y la categoría que el usuario tenía,
        // y rehace el banner, que también sale del catálogo.
        vista.alRecargarCatalogo(() -> {
            refrescarCatalogo();
            refrescarPromociones();
        });
    }

    /**
     * Carga el catálogo inicial, el filtro de categorías y el banner, y de
     * vez en cuando ofrece una promoción al entrar.
     */
    public void iniciar() {
        iniciar(true);
    }

    /**
     * Igual que {@link #iniciar()}, decidiendo si se sortea el pop-up.
     *
     * @param conPromocion {@code false} cuando la tienda se reconstruye dentro
     *                     de la misma sesión (vuelta desde el panel del
     *                     proveedor): el pop-up es un saludo de
     *                     entrada, no algo que salte cada vez que se repinta
     */
    public void iniciar(boolean conPromocion) {
        List<String> etiquetas = new ArrayList<>();
        etiquetas.add(IClienteDashboardView.CATEGORIA_TODAS);
        for (Categoria categoria : Categoria.values()) {
            etiquetas.add(categoria.getEtiqueta());
        }
        vista.mostrarCategorias(etiquetas);
        refrescarCatalogo();
        refrescarPromociones();
        refrescarCarrito();
        if (conPromocion) {
            sortearPromocion();
        }
    }

    // ---------------------------------------------------------------------
    // Promociones
    // ---------------------------------------------------------------------

    /**
     * @return productos que se pueden promocionar —con descuento y con
     *         existencias, porque anunciar algo agotado solo frustra—, del
     *         mayor descuento al menor
     */
    private List<Producto> candidatosPromocion() {
        List<Producto> candidatos = new ArrayList<>();
        for (Producto producto : productos.buscar("", null)) {
            if (producto.tieneDescuento() && producto.hayExistencias()) {
                candidatos.add(producto);
            }
        }
        candidatos.sort(Comparator.comparingInt(Producto::getPorcentajeDescuento).reversed());
        return candidatos;
    }

    /** Llena el banner con las mejores ofertas del momento. */
    private void refrescarPromociones() {
        List<Promocion> promociones = new ArrayList<>();
        for (Producto producto : candidatosPromocion()) {
            if (promociones.size() == PROMOCIONES_EN_BANNER) {
                break;
            }
            promociones.add(aPromocion(producto));
        }
        vista.mostrarPromociones(promociones);
    }

    /**
     * Decide al azar si esta vez se ofrece una promoción al entrar y, si es
     * así, cuál.
     *
     * <p>Es una regla de la tienda y no de la pantalla, por eso vive aquí y no
     * en la vista: la vista solo recibe "ofrece esto" y decide cómo se ve. Si
     * no hay ningún producto en oferta, no hay nada que ofrecer.</p>
     */
    private void sortearPromocion() {
        List<Producto> candidatos = candidatosPromocion();
        if (candidatos.isEmpty() || Math.random() >= PROBABILIDAD_PROMOCION) {
            return;
        }
        Producto elegido = candidatos.get((int) (Math.random() * candidatos.size()));
        vista.ofrecerPromocion(aPromocion(elegido));
    }

    private Promocion aPromocion(Producto producto) {
        return new Promocion(aTarjeta(producto),
                "-" + producto.getPorcentajeDescuento() + "% en " + producto.getNombre(),
                "Ahora " + ResumenPedido.moneda(producto.getPrecioFinal())
                        + ", antes " + ResumenPedido.moneda(producto.getPrecio()) + ".");
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
        // Una sola consulta para las estrellas de todas las tarjetas.
        resumenResenas = resenas.resumenDelCatalogo();
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
                producto.getImagen(),
                producto.getMarca(),
                resumenResenas.getOrDefault(producto.getId(), SIN_RESENAS)[0],
                (int) resumenResenas.getOrDefault(producto.getId(), SIN_RESENAS)[1]);
    }

    // ---------------------------------------------------------------------
    // Ficha del producto y reseñas
    // ---------------------------------------------------------------------

    /**
     * Abre la ficha de un producto con sus reseñas.
     *
     * <p>Las reseñas y el permiso para opinar (¿lo compró?) se leen de la
     * base, así que van fuera del hilo de eventos.</p>
     */
    private void abrirDetalle(String idProducto) {
        vista.ejecutarEnSegundoPlano("Cargando el producto...", () -> {
            Producto producto = productos.buscarPorId(idProducto);
            return producto == null ? null : new Ficha(aTarjeta(producto), resumenDe(idProducto));
        }, ficha -> {
            if (ficha == null) {
                vista.mostrarAviso("Ese producto ya no está disponible.");
                return;
            }
            vista.mostrarDetalle(ficha.tarjeta(), ficha.resenas());
        });
    }

    /** Publica la reseña y, si salió bien, refresca la ficha y el catálogo. */
    private void publicarResena(NuevaResena nueva) {
        vista.ejecutarEnSegundoPlano("Publicando tu reseña...", () -> {
            String error = resenas.publicar(usuario, nueva.idProducto(),
                    nueva.estrellas(), nueva.comentario());
            return new Publicacion(error, error == null ? resumenDe(nueva.idProducto()) : null);
        }, publicacion -> {
            if (publicacion.error() != null) {
                vista.mostrarErrorResena(publicacion.error());
                return;
            }
            vista.actualizarResenas(nueva.idProducto(), publicacion.resenas());
            // Las tarjetas muestran el promedio: también cambió.
            refrescarCatalogo();
        });
    }

    /**
     * Lo que el trabajo de fondo de la ficha devuelve: un trabajo solo puede
     * devolver un valor, y la ficha necesita dos (mismo motivo que
     * {@code ResultadoCompra}).
     */
    private record Ficha(TarjetaProducto tarjeta, ResumenResenas resenas) {
    }

    /** Cómo salió publicar una reseña: el error, o las reseñas actualizadas. */
    private record Publicacion(String error, ResumenResenas resenas) {
    }

    /**
     * Arma la sección de reseñas de un producto. Corre fuera del hilo de
     * eventos (lee reseñas y el historial de compras).
     */
    private ResumenResenas resumenDe(String idProducto) {
        List<ResenaVista> lista = new ArrayList<>();
        double suma = 0;
        for (Resena resena : resenas.resenasDe(idProducto)) {
            suma += resena.getEstrellas();
            lista.add(new ResenaVista(resena.getNombreAutor(), resena.getEstrellas(),
                    resena.getComentario(), resena.getFecha().format(FECHA_CORTA)));
        }
        String motivo = resenas.motivoParaNoResenar(usuario, idProducto);
        return new ResumenResenas(lista.isEmpty() ? 0 : suma / lista.size(), lista.size(),
                lista, motivo == null, motivo);
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

    /**
     * Cambia cuántas unidades lleva un renglón.
     *
     * <p>Se valida contra el stock real, igual que al agregar: la vista ya
     * topa el "+" con el máximo que conocía, pero entre que se pintó el
     * carrito y se pulsó, otro cliente pudo llevarse unidades.</p>
     */
    private void cambiarCantidad(String idProducto, Integer cantidad) {
        Producto producto = productos.buscarPorId(idProducto);
        if (producto == null) {
            carrito.quitar(idProducto);
            refrescarCarrito();
            vista.mostrarAviso("Ese producto ya no está disponible.");
            return;
        }
        if (cantidad != null && cantidad > producto.getStock()) {
            refrescarCarrito();
            vista.mostrarAviso("Solo quedan " + producto.getStock() + " unidades de "
                    + producto.getNombre() + ".");
            return;
        }
        carrito.establecerCantidad(producto, cantidad == null ? 0 : cantidad);
        refrescarCarrito();
    }

    private void refrescarCarrito() {
        List<LineaCarrito> lineas = new ArrayList<>();
        for (LineaPedido linea : carrito.getLineas()) {
            // La miniatura y el tope se buscan en el catálogo vivo: el renglón
            // del pedido guarda a propósito el precio del momento, no la ficha
            // completa del producto.
            Producto producto = productos.buscarPorId(linea.getIdProducto());
            lineas.add(new LineaCarrito(linea.getIdProducto(), linea.getNombreProducto(),
                    linea.getCantidad(), ResumenPedido.moneda(linea.getPrecioUnitario()),
                    ResumenPedido.moneda(linea.getSubtotal()),
                    producto == null ? linea.getCantidad() : producto.getStock(),
                    producto == null ? "" : producto.getImagen()));
        }
        vista.mostrarCarrito(lineas, ResumenPedido.moneda(carrito.getTotal()));
    }

    // ---------------------------------------------------------------------
    // Compra
    // ---------------------------------------------------------------------

    /**
     * Confirma la compra: asegura el stock de todos los renglones, registra el
     * pedido con la dirección del usuario, vacía el carrito y avisa por correo
     * al comprador y a la tienda.
     *
     * <p>Una compra es todo o nada: o se descuentan todos los renglones y se
     * registra el pedido, o no queda rastro de ella. Ver
     * {@link #procesarCompra()}.</p>
     */
    private void confirmarCompra() {
        if (carrito.estaVacio()) {
            vista.mostrarAviso("Tu carrito está vacío.");
            return;
        }
        // Sin cédula y dirección no se compra: el pedido no se podría
        // despachar ni facturar, y los correos saldrían con esos datos vacíos.
        // Se piden aquí, en un formulario rápido, y al guardarlos la compra
        // sigue sola. (El caso de uso lo vuelve a exigir por su cuenta.)
        if (!usuario.datosDeEnvioCompletos()) {
            vista.pedirDatosEnvio(new DatosEnvio(usuario.getCedula(),
                    usuario.getCedula().isEmpty(), usuario.getDireccionEnvio()),
                    this::guardarDatosEnvio);
            return;
        }
        // Esta es la operación más pesada del programa: descuenta el stock de
        // cada renglón e inserta el pedido. Con MongoDB Atlas detrás son varios
        // viajes por red seguidos, así que va fuera del hilo de eventos; si
        // corriera en él, la ventana se quedaría congelada durante la compra.
        vista.ejecutarEnSegundoPlano("Confirmando tu pedido...",
                this::procesarCompra, this::terminarCompra);
    }

    /**
     * Guarda la cédula y la dirección del formulario rápido y, si son
     * válidas, retoma la compra.
     *
     * <p>Si no lo son, el formulario sigue abierto con lo escrito y el motivo:
     * el usuario no pierde ni el carrito ni lo que ya tecleó.</p>
     */
    private void guardarDatosEnvio(DatosEnvio datos) {
        vista.ejecutarEnSegundoPlano("Guardando tus datos de envío...",
                () -> cuentas.completarDatosEnvio(usuario, datos.cedula(), datos.direccion()),
                resultado -> {
                    if (!resultado.exitoso()) {
                        vista.mostrarErrorDatosEnvio(resultado.error());
                        return;
                    }
                    vista.cerrarDatosEnvio();
                    confirmarCompra();
                });
    }

    /**
     * Pide al servicio de aplicación que haga la compra.
     *
     * <p>Aquí no hay ninguna regla: el stock, la compensación si algo falla y
     * los avisos por correo son de {@link CompraService}. Este método existe
     * solo para pasarle lo que el usuario tiene en el carrito.</p>
     *
     * <p>Corre <b>fuera</b> del hilo de eventos, así que no toca la vista: el
     * resultado lo muestra {@link #terminarCompra(ResultadoCompra)}.</p>
     *
     * @return el pedido confirmado, o el nombre del producto que no alcanzó
     */
    private ResultadoCompra procesarCompra() {
        return compras.confirmar(usuario, carrito.getLineas());
    }

    /**
     * Cuenta cómo salió la compra y deja la pantalla al día.
     *
     * <p>Ya de vuelta en el hilo de eventos. El carrito se vacía aquí y no en
     * el trabajo de fondo: mientras la compra viaja todavía puede fallar, y un
     * carrito vaciado de antemano dejaría al usuario sin nada que reintentar.</p>
     */
    private void terminarCompra(ResultadoCompra resultado) {
        if (resultado.faltanDatosEnvio()) {
            // Solo si la cuenta cambió entre la comprobación y la compra.
            confirmarCompra();
            return;
        }
        if (resultado.faltante() != null) {
            vista.mostrarAviso("No hay unidades suficientes de "
                    + resultado.faltante() + ".");
            return;
        }

        Pedido pedido = resultado.pedido();
        carrito.vaciar();
        refrescarCatalogo();
        // La compra pudo agotar un producto en oferta: deja de anunciarse.
        refrescarPromociones();
        refrescarCarrito();
        vista.mostrarExito("Pedido " + pedido.getId() + " confirmado por "
                + ResumenPedido.moneda(pedido.getTotal())
                + ". Te llegará a: " + pedido.getDireccionEntrega());
    }

    // ---------------------------------------------------------------------
    // Mis compras
    // ---------------------------------------------------------------------

    private void mostrarMisCompras() {
        List<ResumenCompra> resumenes = new ArrayList<>();
        for (Pedido pedido : compras.historial(usuario.getCorreo())) {
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
