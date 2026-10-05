package aplicacion.compra;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import model.entity.Categoria;
import model.entity.Cliente;
import model.entity.LineaPedido;
import model.entity.Pedido;
import model.entity.Producto;
import model.entity.Usuario;
import model.repository.memoria.PedidoRepositoryMemoria;
import model.repository.memoria.ProductoRepositoryMemoria;
import service.correo.INotificadorPedido;

/**
 * Comprobación automática de que nunca se vende dos veces la última unidad.
 *
 * <p>Simula a dos compradores en dos equipos que confirman a la vez la compra
 * de un producto con una sola unidad. La protección está en que el descuento
 * de stock <b>es</b> la comprobación (en MongoDB, {@code $inc} con filtro
 * {@code stock >= n}; en memoria, métodos {@code synchronized}): si se
 * preguntara primero "¿hay?" y se descontara después, los dos verían una
 * unidad y los dos comprarían.</p>
 *
 * <p>Usa el almacén en memoria, que reproduce la misma garantía sin escribir
 * en la base compartida.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class VerificacionCompraConcurrente {

    private static final int RONDAS = 20;

    private static int fallos = 0;

    private VerificacionCompraConcurrente() {
    }

    /** Notificador de prueba: no envía nada. */
    private static final class SinCorreo implements INotificadorPedido {
        @Override
        public boolean notificarCompra(Pedido pedido, String correoDestino) {
            return true;
        }

        @Override
        public boolean notificarVenta(Pedido pedido) {
            return true;
        }
    }

    private static void comprobar(String que, boolean ok) {
        System.out.println((ok ? "  OK    " : "  FALLA ") + que);
        if (!ok) {
            fallos++;
        }
    }

    /**
     * @param args no se usan
     * @throws InterruptedException si se interrumpe la espera de los hilos
     */
    public static void main(String[] args) throws InterruptedException {
        ProductoRepositoryMemoria productos = new ProductoRepositoryMemoria();
        PedidoRepositoryMemoria pedidos = new PedidoRepositoryMemoria();
        CompraService servicio = new CompraService(productos, pedidos, new SinCorreo());
        Usuario ana = new Cliente("1", "Ana", "ana@x.com", "x", "Calle 1 # 2-3");
        Usuario luis = new Cliente("2", "Luis", "luis@x.com", "x", "Calle 2 # 3-4");

        System.out.println("Dos compradores por la última unidad, " + RONDAS + " rondas");
        int rondasCorrectas = 0;
        for (int ronda = 0; ronda < RONDAS; ronda++) {
            Producto ultima = productos.guardar(new Producto(null, "Última " + ronda, "d", 10000, 0,
                    Categoria.HOGAR, 1, "", "tienda@x.com"));
            List<LineaPedido> lineas = List.of(
                    new LineaPedido(ultima.getId(), ultima.getNombre(), 10000, 1));
            int pedidosAntes = pedidos.listarTodos().size();
            CountDownLatch salida = new CountDownLatch(1);
            AtomicInteger exitosas = new AtomicInteger();
            Thread[] compradores = new Thread[2];
            Usuario[] quienes = {ana, luis};
            for (int i = 0; i < 2; i++) {
                Usuario quien = quienes[i];
                compradores[i] = new Thread(() -> {
                    try {
                        salida.await();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    if (servicio.confirmar(quien, lineas).exitosa()) {
                        exitosas.incrementAndGet();
                    }
                });
                compradores[i].start();
            }
            salida.countDown();
            for (Thread comprador : compradores) {
                comprador.join();
            }
            if (exitosas.get() == 1
                    && productos.buscarPorId(ultima.getId()).getStock() == 0
                    && pedidos.listarTodos().size() == pedidosAntes + 1) {
                rondasCorrectas++;
            }
        }
        comprobar("en cada ronda compra exactamente uno, el stock queda en 0 y hay un solo pedido",
                rondasCorrectas == RONDAS);

        System.out.println(fallos == 0 ? "\nTODO CORRECTO" : "\n" + fallos + " FALLAS");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
