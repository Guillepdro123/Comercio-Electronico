package view.dashboard.proveedor;

/**
 * Lo que el proveedor escribió en el formulario de un producto, tal cual,
 * antes de validarlo.
 *
 * <p><b>Los números viajan como texto a propósito.</b> Quien captura es un
 * campo de texto y el usuario puede escribir cualquier cosa; convertir y
 * decidir si "12,5" o "abc" son un precio válido es validación, y la
 * validación es del controlador, no de la vista. Si la vista entregara un
 * {@code double} ya tendría que haber validado ella misma.</p>
 *
 * @param nombre      nombre comercial
 * @param descripcion descripción que verá el Cliente
 * @param precio      precio de lista, sin formato
 * @param descuento   porcentaje de descuento, de 0 a 100
 * @param categoria   etiqueta legible de la categoría elegida
 * @param stock       unidades disponibles
 * @param imagen      nombre de archivo o ruta de la imagen; puede ir vacío
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record DatosProducto(String nombre, String descripcion, String precio,
                            String descuento, String categoria, String stock,
                            String imagen) {
}
