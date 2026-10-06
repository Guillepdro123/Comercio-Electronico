package view.auth;

/**
 * Lo que se escribió en la edición de perfil, sin limpiar ni validar.
 *
 * <p>Viaja de la vista al controlador; quien decide si es válido es el caso de
 * uso de cuentas.</p>
 *
 * @param nombres           nombres y apellidos
 * @param correo            correo de acceso
 * @param telefono          teléfono de contacto
 * @param cedula            cédula (solo cuenta si la cuenta no tenía una)
 * @param direccionEnvio    dirección de envío
 * @param nit               NIT (solo vendedor)
 * @param nombreEmpresa     empresa o marca (solo vendedor)
 * @param passwordActual    contraseña vigente
 * @param passwordNueva     contraseña nueva
 * @param passwordConfirmar repetición de la nueva
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 2.0
 */
public record CambiosPerfil(String nombres, String correo, String telefono,
                            String cedula, String direccionEnvio, String nit,
                            String nombreEmpresa, String passwordActual,
                            String passwordNueva, String passwordConfirmar) {
}
