package aplicacion.compra;

import java.util.ArrayList;
import java.util.List;
import model.entity.LineaPedido;
import model.entity.Pedido;
import model.entity.Usuario;
import model.repository.IPedidoRepository;
import model.repository.IProductoRepository;
import service.correo.INotificadorPedido;

/**
 * Caso de uso "confirmar una compra" y consulta del historial del cliente.
 *
 * <p><b>Qué hace esta clase aquí.</b> Esta lógica vivía dentro de
 * {@code ClienteController}, mezclada con el refresco de la pantalla. Al
 * sacarla: se puede probar sin abrir ninguna ventana, se puede reutilizar
 * desde otro punto de entrada (un panel de administración, una cancelación de
 * pedido) y el controlador queda solo con lo suyo, que es hablar con la
 * vista.</p>
 *
 * <p><b>No conoce Swing ni la vista.</b> Recibe entidades del dominio y
 * devuelve entidades o un {@link ResultadoCompra}; quién lo muestre y cómo es
 * problema de la capa de presentación.</p>
 *
 * <p><b>Inversión de dependencias:</b> depende de {@link IProductoRepository},
 * {@link IPedidoRepository} e {@link INotificadorPedido}, todas abstracciones,
 * así que sirve igual con los repositorios en memoria y con MongoDB Atlas.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class CompraService {

    private final IProductoRepository productos;
    private final IPedidoRepository pedidos;
    private final INotificadorPedido notificador;

    /**
     * @param productos   catálogo, que es donde vive el stock
     * @param pedidos     almacén de órdenes
     * @param notificador avisos por correo de la compra y de la venta
     */
    public CompraService(IProductoRepository productos, IPedidoRepository pedidos,
                         INotificadorPedido notificador) {
        this.productos = productos;
        this.pedidos = pedidos;
        this.notificador = notificador;
    }

    /**
     * Confirma una compra: asegura el stock de todos los renglones, registra
     * el pedido y avisa por correo al comprador y a la tienda.
     *
     * <p><b>El descuento de stock ES la comprobación.</b> Una versión anterior
     * preguntaba "¿hay existencias?", guardaba el pedido y luego descontaba
     * sin mirar el resultado. Entre la pregunta y el descuento, otro comprador
     * contra el mismo Atlas podía llevarse las últimas unidades: quedaba un
     * pedido guardado y el stock sin descontar.
     * {@link IProductoRepository#descontarStock} comprueba y descuenta en una
     * sola operación atómica, y su respuesta es la única que vale.</p>
     *
     * <p><b>Todo o nada.</b> Si un renglón no alcanza, se devuelven con
     * {@code reponerStock} las unidades de los renglones ya descontados y no
     * queda rastro de la compra; si falla el registro del pedido, se devuelven
     * todas y el fallo sigue su camino. Es una compensación y no una
     * transacción de MongoDB a propósito: funciona igual con el almacén en
     * memoria, y la única ventana observable es que, durante milisegundos,
     * otro comprador vea menos stock del real, que es el error seguro.</p>
     *
     * <p><b>Bloquea</b> mientras habla con la base: quien lo llame desde una
     * interfaz gráfica debe hacerlo fuera del hilo de eventos.</p>
     *
     * @param usuario cliente que compra; de él salen el correo, el nombre y la
     *                dirección de entrega
     * @param lineas  renglones del carrito, ya con su precio del momento
     * @return el pedido confirmado, o qué producto se quedó sin existencias
     * @throws RuntimeException si falla el registro del pedido, después de
     *         haber devuelto el stock
     */
    public ResultadoCompra confirmar(Usuario usuario, List<LineaPedido> lineas) {
        // Sin cédula y dirección el pedido no se puede despachar ni facturar,
        // y los correos saldrían con esos datos vacíos. La pantalla ya los
        // pide antes de llegar aquí, pero la regla es de la compra: aquí se
        // vuelve a exigir, antes de tocar el stock.
        if (!usuario.datosDeEnvioCompletos()) {
            return ResultadoCompra.sinDatosEnvio();
        }
        List<LineaPedido> descontadas = new ArrayList<>();
        for (LineaPedido linea : lineas) {
            if (!productos.descontarStock(linea.getIdProducto(), linea.getCantidad())) {
                reponer(descontadas);
                return ResultadoCompra.sinExistencias(linea.getNombreProducto());
            }
            descontadas.add(linea);
        }

        Pedido pedido;
        try {
            Pedido nuevo = new Pedido(null, usuario.getCorreo(),
                    usuario.getNombres(), lineas, usuario.getDireccionEnvio().trim());
            nuevo.setDocumentoComprador(usuario.getCedula());
            pedido = pedidos.registrar(nuevo);
        } catch (RuntimeException ex) {
            // Sin pedido no hay venta: el stock vuelve a estar disponible.
            reponer(descontadas);
            throw ex;
        }

        // Los avisos se encolan y vuelven al instante (el notificador que
        // ensambla app.Main envía en su propio hilo): la compra ya está hecha
        // y no depende de que el correo salga.
        notificador.notificarCompra(pedido, usuario.getCorreo());
        notificador.notificarVenta(pedido);
        return ResultadoCompra.confirmada(pedido);
    }

    /**
     * @param correoUsuario correo del cliente
     * @return sus pedidos, del más reciente al más antiguo
     */
    public List<Pedido> historial(String correoUsuario) {
        return pedidos.listarPorUsuario(correoUsuario);
    }

    /** Devuelve al stock las unidades de los renglones que se descontaron. */
    private void reponer(List<LineaPedido> descontadas) {
        for (LineaPedido linea : descontadas) {
            productos.reponerStock(linea.getIdProducto(), linea.getCantidad());
        }
    }
}
