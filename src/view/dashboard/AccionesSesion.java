package view.dashboard;

/**
 * Lo que una ventana de la sesión (tienda o panel del proveedor) puede pedir
 * que pase fuera de ella, todo resuelto por {@code controller.SesionController}.
 *
 * <p><b>Por qué un registro y no cuatro parámetros sueltos.</b> Las dos
 * ventanas de la sesión reciben exactamente las mismas acciones; con el doble
 * rol y el cambio de tema pasaron de dos a cuatro, y cuatro {@code Runnable}
 * seguidos en un constructor son fáciles de cruzar sin que el compilador lo
 * note. Con nombre, cada uno dice qué es.</p>
 *
 * <p>Las ventanas solo disparan estas acciones; no saben qué hacen ni tienen
 * el {@code Usuario}.</p>
 *
 * @param cerrarSesion vuelve al Login olvidando la sesión
 * @param editarPerfil abre la edición de perfil
 * @param cambiarTema  alterna modo claro/oscuro y reconstruye la ventana
 * @param cambiarPanel en la tienda, abre el panel del proveedor; en el panel,
 *                     vuelve a la tienda. {@code null} en la tienda de quien no
 *                     vende: entonces la opción no aparece
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record AccionesSesion(Runnable cerrarSesion, Runnable editarPerfil,
                             Runnable cambiarTema, Runnable cambiarPanel) {
}
