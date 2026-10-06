package view.dashboard.proveedor;

/**
 * Un producto del proveedor tal como lo necesita su tabla de gestión.
 *
 * <p><b>Por qué no se pasa la entidad {@code Producto}.</b> Misma regla que en
 * {@link view.dashboard.cliente.TarjetaProducto}: {@code view} nunca importa {@code model}. El
 * controlador traduce cada producto a este registro y la vista se limita a
 * pintarlo.</p>
 *
 * <p><b>Por qué el precio viaja dos veces.</b> {@code precio} es el precio de
 * lista en texto plano ({@code "320000"}), que es lo que se carga en el
 * formulario al editar; {@code precioFinal} ya viene formateado con separador
 * de miles y descuento aplicado, y es lo que se muestra en la tabla. Formatear
 * importes no es cosa de la vista, y un campo de captura no puede recibir un
 * texto con símbolo de moneda.</p>
 *
 * @param id          identificador del producto
 * @param nombre      nombre comercial
 * @param descripcion texto que se muestra en el catálogo del Cliente
 * @param categoria   etiqueta legible de la categoría
 * @param precio      precio de lista sin formato, para el formulario
 * @param precioFinal precio ya con descuento y formateado, para la tabla
 * @param descuento   porcentaje de descuento; 0 si no tiene
 * @param stock       unidades disponibles
 * @param imagen      referencia de imagen, para recargarla al editar
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record FilaProducto(
        String id,
        String nombre,
        String descripcion,
        String categoria,
        String precio,
        String precioFinal,
        int descuento,
        int stock,
        String imagen) {
}
