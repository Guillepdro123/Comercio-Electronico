package aplicacion.catalogo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.entity.LineaPedido;
import model.entity.Pedido;
import model.entity.Producto;
import model.repository.IPedidoRepository;
import model.repository.IProductoRepository;

/**
 * Caso de uso "ver cómo se están vendiendo mis productos".
 *
 * <p><b>Qué hace esta clase aquí.</b> El recorrido de todos los pedidos vivía
 * dentro de {@code ProveedorController}, repetido en dos métodos privados: uno
 * para las cifras y otro para el gráfico. Ahora se recorre una sola vez y el
 * resultado sirve para los dos.</p>
 *
 * <p><b>Un pedido puede mezclar artículos de varios proveedores</b>, así que
 * nunca se toma su total: se suman renglón por renglón los que pertenecen a
 * este proveedor. El pedido cuenta como venta suya si aportó al menos uno.</p>
 *
 * <p><b>Aquí irá el filtro por estado del pedido.</b> Hoy se cuentan todos los
 * pedidos registrados porque {@code Pedido} todavía no tiene estado. Cuando
 * exista "cancelar pedido", los cancelados deberán excluirse, y este es el
 * único sitio donde habrá que hacerlo: si la cuenta siguiera repartida por el
 * controlador, un pedido cancelado se seguiría contando como venta sin que
 * nada fallara.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class ReporteVentasService {

    private final IProductoRepository productos;
    private final IPedidoRepository pedidos;

    /**
     * @param productos catálogo (abstracción)
     * @param pedidos   almacén de órdenes, de donde salen las ventas
     */
    public ReporteVentasService(IProductoRepository productos, IPedidoRepository pedidos) {
        this.productos = productos;
        this.pedidos = pedidos;
    }

    /**
     * Calcula las cifras de un proveedor.
     *
     * <p><b>Bloquea</b> mientras lee el catálogo y los pedidos: quien lo llame
     * desde una interfaz gráfica debe hacerlo fuera del hilo de eventos.</p>
     *
     * @param correoProveedor dueño de los productos
     * @return sus productos y sus cifras, sin formatear
     */
    public ReporteVentas generar(String correoProveedor) {
        List<Producto> mios = productos.listarPorProveedor(correoProveedor);

        Map<String, Double> ingresosPorProducto = new LinkedHashMap<>();
        int agotados = 0;
        for (Producto producto : mios) {
            ingresosPorProducto.put(producto.getId(), 0.0);
            if (!producto.hayExistencias()) {
                agotados++;
            }
        }

        double ingresos = 0;
        int unidades = 0;
        int pedidosConVenta = 0;
        for (Pedido pedido : pedidos.listarTodos()) {
            boolean aporta = false;
            for (LineaPedido linea : pedido.getLineas()) {
                if (ingresosPorProducto.containsKey(linea.getIdProducto())) {
                    ingresos += linea.getSubtotal();
                    unidades += linea.getCantidad();
                    ingresosPorProducto.merge(linea.getIdProducto(), linea.getSubtotal(), Double::sum);
                    aporta = true;
                }
            }
            if (aporta) {
                pedidosConVenta++;
            }
        }
        return new ReporteVentas(new ArrayList<>(mios), ingresos, unidades, pedidosConVenta,
                mios.size(), agotados, ingresosPorProducto);
    }
}
