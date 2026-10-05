package service.correo;

/**
 * Todos los correos transaccionales de la aplicación: el de compra y el de
 * bienvenida.
 *
 * <p>Solo lo usan {@code app.Main} y {@code controller.SesionController}, que
 * ensamblan pantallas y reparten el mismo notificador a controladores
 * distintos. Los controladores de negocio no dependen de esta interfaz sino de
 * la parte que les toca ({@link INotificadorPedido} o
 * {@link INotificadorCuenta}).</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public interface INotificadorCorreo extends INotificadorPedido, INotificadorCuenta {
}
