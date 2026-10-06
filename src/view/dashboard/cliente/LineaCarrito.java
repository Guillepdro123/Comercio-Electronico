package view.dashboard.cliente;

/**
 * Un renglón del carrito, listo para mostrarse.
 *
 * <p>Mismo motivo que {@link TarjetaProducto}: la vista no importa el modelo,
 * así que el controlador le entrega los datos ya formateados.</p>
 *
 * @param idProducto     identificador, para quitarlo o cambiar su cantidad
 * @param nombre         nombre del producto
 * @param cantidad       unidades elegidas
 * @param precioUnitario precio de una unidad, ya formateado: sin él, el
 *                       subtotal de "3 x" no deja ver cuánto cuesta cada una
 * @param subtotal       importe del renglón, ya formateado
 * @param maximo         unidades disponibles; tope del botón "+" del renglón
 * @param imagen         referencia de imagen, para la miniatura del renglón: un
 *                       carrito sin imágenes obliga a leer cada nombre para
 *                       saber qué se lleva
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 2.0
 */
public record LineaCarrito(String idProducto, String nombre, int cantidad,
                           String precioUnitario, String subtotal, int maximo,
                           String imagen) {
}
