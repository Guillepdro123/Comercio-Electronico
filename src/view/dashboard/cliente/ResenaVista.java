package view.dashboard.cliente;

/**
 * Una reseña lista para mostrarse en la ficha del producto.
 *
 * @param autor      nombre de quien opina
 * @param estrellas  calificación, de 1 a 5
 * @param comentario texto; puede venir vacío
 * @param fecha      fecha ya formateada
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record ResenaVista(String autor, int estrellas, String comentario, String fecha) {
}
