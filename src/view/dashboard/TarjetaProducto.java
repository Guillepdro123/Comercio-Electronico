package view.dashboard;

/**
 * Datos de un producto tal como los necesita una tarjeta del catálogo.
 *
 * <p><b>Por qué no se pasa la entidad {@code Producto}.</b> La regla del
 * proyecto es que {@code view} nunca importa {@code model}. El controlador
 * traduce cada producto a este registro —ya con los precios formateados— y la
 * vista se limita a pintarlo. Así el catálogo no sabe que existe un dominio
 * detrás, y el día que los precios vengan de MongoDB la vista no cambia.</p>
 *
 * @param id             identificador, para saber qué se agregó al carrito
 * @param nombre         nombre comercial
 * @param descripcion    texto completo, se muestra en el detalle
 * @param precioOriginal precio de lista ya formateado; vacío si no hay descuento
 * @param precioFinal    precio a cobrar, ya formateado
 * @param descuento      porcentaje de descuento; 0 si no tiene
 * @param categoria      etiqueta legible de la categoría
 * @param stock          unidades disponibles
 * @param imagen         referencia de imagen del producto (recurso o ruta)
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record TarjetaProducto(
        String id,
        String nombre,
        String descripcion,
        String precioOriginal,
        String precioFinal,
        int descuento,
        String categoria,
        int stock,
        String imagen) {

    /** @return {@code true} si hay que mostrar la insignia de descuento */
    public boolean tieneDescuento() {
        return descuento > 0;
    }

    /** @return {@code true} si se puede comprar */
    public boolean hayExistencias() {
        return stock > 0;
    }
}
