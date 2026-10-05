package service.google;

/**
 * Identidad que devuelve un proveedor de acceso externo (Google) tras
 * autenticar al usuario.
 *
 * <p>Solo lleva lo que la aplicación necesita para buscar o crear la cuenta.
 * No lleva tokens: se usan y se descartan dentro del servicio, porque la
 * aplicación no llama a ninguna API de Google en nombre del usuario después
 * de identificarlo.</p>
 *
 * @param id     identificador estable de la cuenta en el proveedor ({@code sub}
 *               en Google); no cambia aunque el usuario cambie de correo
 * @param correo correo ya verificado por el proveedor
 * @param nombre nombre para mostrar
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record PerfilExterno(String id, String correo, String nombre) { }
