package view.dashboard.cliente;

/**
 * Cédula y dirección de envío: lo que el formulario rápido del checkout pide
 * cuando a la cuenta le faltan. Viaja en los dos sentidos: con lo que ya hay
 * al abrirse, y con lo escrito, sin validar, al enviarse.
 *
 * @param cedula     cédula
 * @param pideCedula {@code true} si la cuenta aún no tiene cédula; si ya la
 *                   tiene, el campo no se muestra
 * @param direccion  dirección de envío
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record DatosEnvio(String cedula, boolean pideCedula, String direccion) {
}
