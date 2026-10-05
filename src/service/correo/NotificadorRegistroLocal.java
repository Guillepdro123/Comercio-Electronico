package service.correo;

import model.entity.Pedido;
import model.entity.Usuario;

/**
 * Notificador por defecto: imprime por consola los correos que se habrían
 * enviado.
 *
 * <p>Es el que se usa cuando {@code config.properties} no trae clave de
 * Resend. Deja constancia exacta de cada aviso (a quién, con qué asunto y qué
 * contenido) sin necesitar red, así que el flujo completo se puede probar y
 * defender sin credenciales.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.1
 */
public class NotificadorRegistroLocal implements INotificadorCorreo {

    @Override
    public boolean notificarCompra(Pedido pedido, String correoDestino) {
        imprimir(correoDestino, ResumenPedido.asuntoConfirmacion(pedido),
                ResumenPedido.enTexto(pedido));
        return true;
    }

    @Override
    public boolean notificarVenta(Pedido pedido) {
        imprimir("(la tienda)", ResumenPedido.asuntoAlertaVenta(pedido),
                ResumenPedido.alertaVentaEnTexto(pedido));
        return true;
    }

    @Override
    public boolean notificarBienvenida(Usuario usuario) {
        imprimir(usuario.getCorreo(), MensajeBienvenida.asunto(usuario),
                MensajeBienvenida.enTexto(usuario));
        return true;
    }

    private void imprimir(String destino, String asunto, String cuerpo) {
        System.out.println("=== Correo (no enviado: sin clave de Resend) ===");
        System.out.println("Para: " + destino);
        System.out.println("Asunto: " + asunto);
        System.out.println(cuerpo);
        System.out.println("================================================");
    }
}
