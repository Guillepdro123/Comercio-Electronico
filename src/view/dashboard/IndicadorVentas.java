package view.dashboard;

/**
 * Cifras del panel del Proveedor, ya calculadas y formateadas por
 * {@link controller.ProveedorController}.
 *
 * <p>Todos los importes llegan como texto: la vista no suma ni formatea, solo
 * pinta la tarjeta correspondiente.</p>
 *
 * @param ingresos         total vendido, formateado
 * @param unidadesVendidas artículos despachados
 * @param pedidos          número de pedidos que incluyeron algún producto suyo
 * @param publicados       productos activos en el catálogo
 * @param agotados         cuántos de ellos se quedaron sin existencias
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record IndicadorVentas(String ingresos, int unidadesVendidas, int pedidos,
                              int publicados, int agotados) {
}
