package view.dashboard.cliente;

/**
 * Lo que el usuario escribió al reseñar, sin validar.
 *
 * @param idProducto producto reseñado
 * @param estrellas  estrellas elegidas; 0 si no eligió ninguna
 * @param comentario texto libre
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record NuevaResena(String idProducto, int estrellas, String comentario) {
}
