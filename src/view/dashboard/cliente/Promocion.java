package view.dashboard.cliente;

/**
 * Una promoción lista para mostrarse en el banner o en el pop-up.
 *
 * <p>Lleva la {@link TarjetaProducto} completa y no solo su id: el botón "Ver
 * oferta" abre la misma ficha de producto que el catálogo, y esa ficha se arma
 * con la tarjeta. Así la vista no tiene que pedirle nada más al controlador.</p>
 *
 * @param producto  producto en oferta, ya con los importes formateados
 * @param titulo    frase principal ("-25% en Audífonos Bluetooth")
 * @param subtitulo línea de apoyo (precio final y precio anterior)
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record Promocion(TarjetaProducto producto, String titulo, String subtitulo) {
}
