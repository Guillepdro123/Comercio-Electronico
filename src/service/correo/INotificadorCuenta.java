package service.correo;

import model.entity.Usuario;

/**
 * Contrato del aviso de cuenta nueva (correo de bienvenida).
 *
 * <p><b>Segregación de interfaces:</b> va separado de
 * {@link INotificadorPedido} porque lo usan controladores distintos. El
 * registro no tiene por qué conocer los pedidos, ni el carrito las cuentas.
 * Las mismas clases implementan los dos contratos; cada controlador ve solo
 * el suyo.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public interface INotificadorCuenta {

    /**
     * Da la bienvenida a quien acaba de registrarse.
     *
     * @param usuario cuenta recién creada
     * @return {@code true} si el aviso salió (o quedó encolado); {@code false}
     *         si no se pudo
     */
    boolean notificarBienvenida(Usuario usuario);
}
