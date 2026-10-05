package aplicacion.acceso;

import model.entity.Usuario;

/**
 * Cómo terminó un intento de acceso.
 *
 * <p><b>Por qué un estado y no solo un mensaje.</b> Las cuatro salidas no se
 * muestran igual: entrar abre el panel, un bloqueo enciende una cuenta atrás
 * en la pantalla, y los otros dos casos son un aviso normal. Si el servicio
 * devolviera solo texto, la pantalla tendría que adivinar por el mensaje qué
 * hacer con él.</p>
 *
 * @param estado   qué ocurrió
 * @param usuario  cuenta autenticada, solo cuando el estado es
 *                 {@link Estado#ACEPTADO}
 * @param mensaje  texto listo para mostrar, o {@code null} si no hay nada que
 *                 decir
 * @param segundos duración del bloqueo, solo cuando el estado es
 *                 {@link Estado#BLOQUEADO}
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record ResultadoAcceso(Estado estado, Usuario usuario, String mensaje, int segundos) {

    /** Las cuatro formas en que puede terminar un intento de acceso. */
    public enum Estado {
        /** Credenciales correctas: se puede abrir la sesión. */
        ACEPTADO,
        /** Faltan datos, o el correo y la contraseña no coinciden. */
        RECHAZADO,
        /** Se agotaron los intentos: hay que esperar antes de reintentar. */
        BLOQUEADO,
        /** La cuenta existe pero entra con Google, no con contraseña. */
        CUENTA_DE_GOOGLE
    }

    /** @return acceso correcto */
    public static ResultadoAcceso aceptado(Usuario usuario) {
        return new ResultadoAcceso(Estado.ACEPTADO, usuario, null, 0);
    }

    /** @return acceso rechazado, con el motivo para mostrar */
    public static ResultadoAcceso rechazado(String mensaje) {
        return new ResultadoAcceso(Estado.RECHAZADO, null, mensaje, 0);
    }

    /** @return acceso bloqueado durante {@code segundos} */
    public static ResultadoAcceso bloqueado(int segundos) {
        return new ResultadoAcceso(Estado.BLOQUEADO, null, null, segundos);
    }

    /** @return la cuenta existe, pero se entra por Google */
    public static ResultadoAcceso cuentaDeGoogle(String mensaje) {
        return new ResultadoAcceso(Estado.CUENTA_DE_GOOGLE, null, mensaje, 0);
    }

    /** @return {@code true} si se puede abrir la sesión */
    public boolean aceptado() {
        return estado == Estado.ACEPTADO;
    }
}
