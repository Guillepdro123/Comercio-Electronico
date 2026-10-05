package aplicacion.cuenta;

/**
 * Datos de una cuenta nueva, tal como se escribieron en el formulario y
 * todavía sin validar.
 *
 * <p>Mismo criterio que {@link SolicitudProducto}: llega texto crudo y es
 * {@link CuentaService} quien decide si eso es una cuenta. La contraseña viaja
 * en claro <b>solo</b> hasta el servicio, que la cifra antes de construir la
 * entidad; el modelo y el repositorio nunca la ven sin cifrar.</p>
 *
 * @param identificacion documento de identidad
 * @param nombres        nombres y apellidos
 * @param correo         correo electrónico, que es con lo que se entra
 * @param password       contraseña en claro
 * @param datoAdicional  dirección de envío (Cliente) o NIT (Proveedor)
 * @param tipoCuenta     {@link TipoCuenta#CLIENTE} o {@link TipoCuenta#PROVEEDOR}
 * @param nombreEmpresa  empresa o marca con la que vende (Proveedor); vacío
 *                       en un Cliente
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 2.0
 */
public record SolicitudRegistro(String identificacion, String nombres, String correo,
                                String password, String datoAdicional, String tipoCuenta,
                                String nombreEmpresa) {
}
