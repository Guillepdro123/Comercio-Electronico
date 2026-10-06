package view.dashboard.cliente;

import java.util.List;

/**
 * Una compra pasada, tal como se muestra en "Mis compras".
 *
 * @param id        número de pedido
 * @param fecha     fecha formateada
 * @param total     importe total, ya formateado
 * @param direccion dirección a la que se entregó
 * @param articulos descripción de cada renglón ("2 x Audífonos")
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record ResumenCompra(String id, String fecha, String total,
                            String direccion, List<String> articulos) {
}
