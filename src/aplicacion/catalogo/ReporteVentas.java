package aplicacion.catalogo;

import java.util.List;
import java.util.Map;
import model.entity.Producto;

/**
 * Cifras de venta de un proveedor, en crudo y sin formatear.
 *
 * <p>Los importes van como {@code double} y las cantidades como {@code int} a
 * propósito: darles formato de moneda es cosa de quien los muestre. Así el
 * mismo reporte sirve para la pantalla, para un correo o para una exportación
 * sin que ninguna de las tres herede el formato de las otras.</p>
 *
 * @param productos           lo que el proveedor tiene publicado
 * @param ingresos            total facturado por sus productos
 * @param unidadesVendidas    cuántas unidades suyas se han vendido
 * @param pedidosConVenta     en cuántos pedidos aportó al menos un renglón
 * @param publicados          cuántos productos tiene publicados
 * @param agotados            cuántos se quedaron sin existencias
 * @param ingresosPorProducto cuánto facturó cada producto, por identificador
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record ReporteVentas(List<Producto> productos, double ingresos, int unidadesVendidas,
                            int pedidosConVenta, int publicados, int agotados,
                            Map<String, Double> ingresosPorProducto) {

    /** @return {@code true} si hay al menos una venta registrada */
    public boolean hayVentas() {
        return ingresosPorProducto.values().stream().anyMatch(valor -> valor > 0);
    }

    /**
     * @return lo que deja en promedio cada pedido con productos del proveedor;
     *         cero si todavía no hay pedidos (no hay nada que promediar)
     */
    public double ticketPromedio() {
        return pedidosConVenta == 0 ? 0 : ingresos / pedidosConVenta;
    }

    /**
     * @return el producto que más ha facturado, o {@code null} si no se ha
     *         vendido nada todavía
     */
    public Producto productoEstrella() {
        Producto estrella = null;
        double mayor = 0;
        for (Producto producto : productos) {
            double ingreso = ingresosPorProducto.getOrDefault(producto.getId(), 0.0);
            if (ingreso > mayor) {
                mayor = ingreso;
                estrella = producto;
            }
        }
        return estrella;
    }
}
