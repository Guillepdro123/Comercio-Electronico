package view.dashboard;

/**
 * Un renglón del carrito, listo para mostrarse.
 *
 * <p>Mismo motivo que {@link TarjetaProducto}: la vista no importa el modelo,
 * así que el controlador le entrega los datos ya formateados.</p>
 *
 * @param idProducto identificador, para poder quitarlo del carrito
 * @param nombre     nombre del producto
 * @param cantidad   unidades elegidas
 * @param subtotal   importe del renglón, ya formateado
 * @param imagen     referencia de imagen, para la miniatura del renglón: un
 *                   carrito sin imágenes obliga a leer cada nombre para saber
 *                   qué se lleva
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record LineaCarrito(String idProducto, String nombre, int cantidad,
                           String subtotal, String imagen) {
}
