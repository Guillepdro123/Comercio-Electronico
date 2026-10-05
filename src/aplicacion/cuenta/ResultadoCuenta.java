package aplicacion.cuenta;

import model.entity.Usuario;

/**
 * Cómo terminó una operación sobre una cuenta: la cuenta afectada, o el
 * motivo por el que no se pudo hacer.
 *
 * <p>Mismo patrón que los demás resultados de la capa de aplicación: un dato
 * de vuelta en vez de una excepción, porque un formulario incompleto o un
 * correo ya registrado son casos normales del negocio. El mensaje viene
 * redactado para mostrárselo al usuario tal cual.</p>
 *
 * @param usuario cuenta creada o actualizada; {@code null} si no se hizo nada
 * @param error   qué salió mal, o {@code null} si todo fue bien
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record ResultadoCuenta(Usuario usuario, String error) {

    /** @return resultado correcto, con la cuenta afectada */
    public static ResultadoCuenta hecho(Usuario usuario) {
        return new ResultadoCuenta(usuario, null);
    }

    /** @return resultado fallido, con el mensaje para el usuario */
    public static ResultadoCuenta fallo(String error) {
        return new ResultadoCuenta(null, error);
    }

    /** @return {@code true} si la operación se completó */
    public boolean exitoso() {
        return error == null;
    }
}
