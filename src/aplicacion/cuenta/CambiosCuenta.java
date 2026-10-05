package aplicacion.cuenta;

/**
 * Lo que el usuario escribió en "Editar perfil", todavía sin validar.
 *
 * <p>Los tres campos de contraseña vacíos significan "no la cambio", que es el
 * caso normal: por eso {@link #sinCambioDePassword()} existe aquí y no en la
 * pantalla, para que cualquiera que use este servicio interprete los campos
 * vacíos igual.</p>
 *
 * @param nombres           nombres y apellidos
 * @param correo            correo de acceso
 * @param telefono          teléfono de contacto; puede quedar vacío
 * @param cedula            cédula; solo se tiene en cuenta si la cuenta
 *                          todavía no tenía una
 * @param direccionEnvio    dirección a la que llegan las compras
 * @param nit               NIT de la empresa (solo Proveedor)
 * @param nombreEmpresa     empresa o marca (solo Proveedor)
 * @param passwordActual    contraseña vigente, para autorizar el cambio
 * @param passwordNueva     contraseña nueva
 * @param passwordConfirmar repetición de la nueva
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 2.0
 */
public record CambiosCuenta(String nombres, String correo, String telefono,
                            String cedula, String direccionEnvio, String nit,
                            String nombreEmpresa, String passwordActual,
                            String passwordNueva, String passwordConfirmar) {

    /** @return {@code true} si el usuario no tocó el bloque de contraseña */
    public boolean sinCambioDePassword() {
        return vacio(passwordActual) && vacio(passwordNueva) && vacio(passwordConfirmar);
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
