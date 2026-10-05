package aplicacion.catalogo;

/**
 * Datos de un producto tal como los escribió el proveedor, todavía sin
 * validar ni convertir.
 *
 * <p><b>Por qué son cadenas.</b> Quien valida es la capa de aplicación, no el
 * formulario. Si la vista entregara un {@code double} en el precio ya habría
 * validado ella, y esa regla acabaría repartida entre la pantalla y el
 * servicio. Aquí llega el texto crudo y {@link CatalogoService} decide si es
 * un producto.</p>
 *
 * <p>Es el equivalente en la capa de aplicación de {@code DatosProducto}, que
 * pertenece a la vista: así el servicio no depende de ningún tipo de Swing y
 * se puede usar desde otro punto de entrada.</p>
 *
 * @param nombre      nombre comercial
 * @param descripcion texto que verá el cliente
 * @param precio      precio en texto
 * @param descuento   porcentaje de descuento en texto
 * @param categoria   etiqueta visible de la categoría
 * @param stock       unidades disponibles en texto
 * @param imagen      ruta o nombre del archivo de imagen; puede venir vacío
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record SolicitudProducto(String nombre, String descripcion, String precio,
                                String descuento, String categoria, String stock,
                                String imagen) {
}
