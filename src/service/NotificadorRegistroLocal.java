package service;

import model.entity.Pedido;

/**
 * Implementación de {@link INotificadorPedido} que deja constancia del aviso
 * en la salida estándar en vez de enviarlo.
 *
 * <p><b>Para qué sirve.</b> Es la que usa la aplicación mientras no haya una
 * clave de Resend configurada: permite probar el checkout completo y ver
 * exactamente qué correo se habría enviado, sin depender de una cuenta
 * externa ni de que haya red. Cuando la clave exista, {@code app.Main}
 * instancia {@link NotificadorResend} y nada más cambia.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class NotificadorRegistroLocal implements INotificadorPedido {

    @Override
    public boolean notificarCompra(Pedido pedido, String correoDestino) {
        System.out.println("=== Correo de confirmación (no enviado: sin clave de Resend) ===");
        System.out.println("Para: " + correoDestino);
        System.out.println("Asunto: Confirmación de tu pedido " + pedido.getId());
        System.out.println(ResumenPedido.enTexto(pedido));
        System.out.println("================================================================");
        return true;
    }
}
