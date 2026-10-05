package service.google;

/**
 * Contrato de un acceso por un proveedor de identidad externo.
 *
 * <p><b>Inversión de dependencias:</b> el controlador del acceso con Google
 * depende de esta interfaz, no de {@link GoogleAuthService}. Así se puede
 * probar con un autenticador falso, y añadir otro proveedor sería otra
 * implementación, no un cambio en el controlador.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public interface IAutenticadorExterno {

    /**
     * Lleva al usuario a autenticarse y espera el resultado.
     *
     * <p><b>Bloquea</b> hasta que el usuario termina en el navegador, cancela
     * o se agota el tiempo: nunca debe llamarse desde el hilo de eventos.</p>
     *
     * @return el perfil autenticado, o {@code null} si el usuario canceló
     * @throws IllegalStateException si el acceso falló; el mensaje está
     *         redactado para mostrárselo al usuario tal cual
     */
    PerfilExterno autenticar();

    /**
     * Abandona una autenticación en curso; {@link #autenticar()} vuelve con
     * {@code null}. Se puede llamar desde cualquier hilo.
     */
    void cancelar();
}
