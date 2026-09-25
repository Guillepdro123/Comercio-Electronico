package controller;

/**
 * Política de intentos fallidos del inicio de sesión: cuenta los fallos
 * consecutivos y dice cuándo hay que bloquear el acceso.
 *
 * <p><b>Por qué es una clase aparte.</b> Es una regla de seguridad, no una
 * regla de pantalla: cuántos intentos se toleran y cuánto dura el bloqueo no
 * dependen de que la interfaz sea Swing. Separarla deja a
 * {@link LoginController} ocupándose de coordinar el caso de uso y permite
 * cambiar la política (más intentos, bloqueo progresivo) tocando un solo
 * sitio.</p>
 *
 * <p><b>Sin Swing a propósito.</b> Esta clase no conoce temporizadores ni
 * componentes: solo cuenta y responde. La cuenta regresiva visible es cosa de
 * la vista, que es quien sabe mostrarla (ver
 * {@code ILoginView.bloquearIngreso(...)}).</p>
 *
 * <p><b>Fallos <em>consecutivos</em>:</b> un inicio de sesión correcto
 * reinicia el contador, así que tres errores repartidos a lo largo de una
 * sesión normal nunca bloquean a nadie; solo bloquea la insistencia seguida,
 * que es el patrón que interesa frenar.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public class ControlIntentosFallidos {

    /** Fallos consecutivos tolerados antes de bloquear el acceso. */
    public static final int INTENTOS_PERMITIDOS = 3;

    /** Duración del bloqueo, en segundos. */
    public static final int SEGUNDOS_BLOQUEO = 30;

    private int fallosConsecutivos;

    /** Anota un intento fallido. */
    public void registrarFallo() {
        fallosConsecutivos++;
    }

    /** @return {@code true} si ya se agotaron los intentos permitidos */
    public boolean limiteAlcanzado() {
        return fallosConsecutivos >= INTENTOS_PERMITIDOS;
    }

    /** @return cuántos intentos quedan antes del bloqueo (nunca negativo) */
    public int intentosRestantes() {
        return Math.max(0, INTENTOS_PERMITIDOS - fallosConsecutivos);
    }

    /** Vuelve a cero: tras un acceso correcto o al terminar un bloqueo. */
    public void reiniciar() {
        fallosConsecutivos = 0;
    }
}
