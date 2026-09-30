package service;

import model.entity.Pedido;

/**
 * Contrato del aviso de compra confirmada.
 *
 * <p><b>Por qué existe esta interfaz.</b> El checkout tiene que poder avisar
 * al comprador sin saber <em>cómo</em> se avisa. Hoy hay dos formas de
 * cumplirlo —dejar constancia local o enviar el correo real por Resend— y
 * elegir una u otra es cosa del ensamblador ({@code app.Main}), no del
 * controlador de compra (Inversión de Dependencias).</p>
 *
 * <p><b>Nunca debe tumbar una compra.</b> Una venta ya cobrada y con stock
 * descontado no puede deshacerse porque el servidor de correo esté caído, así
 * que las implementaciones informan del fallo con el valor de retorno en vez
 * de lanzar excepciones hacia el caso de uso.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public interface INotificadorPedido {

    /**
     * Avisa al comprador de que su pedido quedó confirmado.
     *
     * @param pedido        pedido ya registrado, con su total y dirección
     * @param correoDestino dirección del comprador
     * @return {@code true} si el aviso salió; {@code false} si no se pudo
     */
    boolean notificarCompra(Pedido pedido, String correoDestino);
}
