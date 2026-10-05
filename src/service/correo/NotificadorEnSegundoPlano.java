package service.correo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import model.entity.Pedido;
import model.entity.Usuario;

/**
 * Envía los correos en un hilo propio, sin que nadie espere por ellos
 * (patrón <em>Decorator</em>).
 *
 * <p><b>Qué resuelve.</b> Una petición a Resend puede tardar un segundo o no
 * responder hasta agotar su plazo. Si el correo se enviara dentro del trabajo
 * de la compra o del registro, el usuario seguiría mirando el indicador de
 * carga por algo que no le afecta: su pedido y su cuenta ya están guardados.
 * Este decorador envuelve al notificador real y devuelve al instante; el envío
 * ocurre después, en su propio hilo.</p>
 *
 * <p><b>Por qué no es un {@code SwingWorker}.</b> Un {@code SwingWorker} sirve
 * para trabajo lento cuyo resultado hay que devolver a la interfaz. El envío de
 * un correo no devuelve nada a la pantalla, y quien lo pide es un controlador,
 * que en este proyecto no importa Swing. Lo que sí hace falta es sacarlo del
 * camino, y para eso basta un {@link ExecutorService} de un hilo.</p>
 *
 * <p><b>Un solo hilo, a propósito.</b> Los correos salen de uno en uno y en
 * orden: no hay volumen que justifique más, y así no se lanzan diez
 * conexiones a la vez contra la API si alguien compra muy seguido.</p>
 *
 * <p><b>Al cerrar la aplicación no se pierde el correo en curso.</b> Un gancho
 * de cierre espera hasta {@value #SEGUNDOS_AL_CERRAR} segundos a que se vacíe
 * la cola. La X de los paneles termina el programa con
 * {@code System.exit}, que ejecuta los ganchos de cierre.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class NotificadorEnSegundoPlano implements INotificadorCorreo {

    private static final int SEGUNDOS_AL_CERRAR = 10;

    private final INotificadorCorreo real;
    private final ExecutorService cola;

    /**
     * @param real notificador que envía de verdad (Resend o la consola)
     */
    public NotificadorEnSegundoPlano(INotificadorCorreo real) {
        this.real = real;
        this.cola = Executors.newSingleThreadExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "envio-correos");
            // Demonio: un correo pendiente no debe impedir que el programa
            // termine; de esperarlo se encarga el gancho de cierre.
            hilo.setDaemon(true);
            return hilo;
        });
        Runtime.getRuntime().addShutdownHook(new Thread(this::vaciarCola, "cierre-correos"));
    }

    /**
     * {@inheritDoc}
     *
     * @return siempre {@code true}: el correo quedó encolado. Si después
     *         falla, lo avisa el notificador real por consola; la compra ya
     *         está hecha y no depende de ello.
     */
    @Override
    public boolean notificarCompra(Pedido pedido, String correoDestino) {
        cola.submit(() -> real.notificarCompra(pedido, correoDestino));
        return true;
    }

    /** {@inheritDoc} Igual que {@link #notificarCompra}: encola y vuelve. */
    @Override
    public boolean notificarVenta(Pedido pedido) {
        cola.submit(() -> real.notificarVenta(pedido));
        return true;
    }

    /** {@inheritDoc} Igual que {@link #notificarCompra}: encola y vuelve. */
    @Override
    public boolean notificarBienvenida(Usuario usuario) {
        cola.submit(() -> real.notificarBienvenida(usuario));
        return true;
    }

    private void vaciarCola() {
        cola.shutdown();
        try {
            if (!cola.awaitTermination(SEGUNDOS_AL_CERRAR, TimeUnit.SECONDS)) {
                System.err.println("Se cerró la aplicación con correos sin enviar.");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
