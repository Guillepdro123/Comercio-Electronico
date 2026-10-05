package aplicacion.compra;

import java.util.List;
import java.util.ArrayList;
import model.entity.Categoria;
import model.entity.Cliente;
import model.entity.LineaPedido;
import model.entity.Pedido;
import model.entity.Producto;
import model.entity.Usuario;
import model.repository.IPedidoRepository;
import model.repository.memoria.PedidoRepositoryMemoria;
import model.repository.memoria.ProductoRepositoryMemoria;
import service.correo.INotificadorPedido;

/**
 * Comprobación automática de {@link CompraService}.
 *
 * <p><b>Para qué sirve esta clase.</b> Demuestra lo que se ganó al sacar la
 * compra del controlador: la regla de negocio se puede ejecutar y comprobar
 * <b>sin abrir una sola ventana de Swing</b> y sin conexión a MongoDB, usando
 * los repositorios en memoria. Antes era imposible: la lógica estaba dentro de
 * un controlador que exigía una vista.</p>
 *
 * <p>Está escrita con Java puro, sin JUnit, porque el proyecto no tiene esa
 * dependencia. Se ejecuta con {@code java aplicacion.VerificacionCompraService}
 * y termina con código 0 si todo está bien.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class VerificacionCompraService {

    private static int fallos = 0;

    private VerificacionCompraService() {
    }

    /** Notificador de prueba: anota a quién se avisó, sin enviar nada. */
    private static final class NotificadorDePrueba implements INotificadorPedido {
        private final List<String> compras = new ArrayList<>();
        private final List<String> ventas = new ArrayList<>();

        @Override
        public boolean notificarCompra(Pedido pedido, String correoDestino) {
            compras.add(pedido.getId());
            return true;
        }

        @Override
        public boolean notificarVenta(Pedido pedido) {
            ventas.add(pedido.getId());
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
     */
    public static void main(String[] args) {
        ProductoRepositoryMemoria productos = new ProductoRepositoryMemoria();
        Producto taza = productos.guardar(new Producto(null, "Taza", "Cerámica", 20000, 0,
                Categoria.HOGAR, 5, "", "tienda@correo.com"));
        Producto plato = productos.guardar(new Producto(null, "Plato", "Cerámica", 30000, 0,
                Categoria.HOGAR, 2, "", "tienda@correo.com"));
        PedidoRepositoryMemoria pedidos = new PedidoRepositoryMemoria();
        NotificadorDePrueba correos = new NotificadorDePrueba();
        Usuario ana = new Cliente("1", "Ana", "ana@correo.com", "x", "Calle 1 # 2-3");
        CompraService servicio = new CompraService(productos, pedidos, correos);

        System.out.println("Compra correcta");
        ResultadoCompra ok = servicio.confirmar(ana, List.of(
                new LineaPedido(taza.getId(), "Taza", 20000, 3),
                new LineaPedido(plato.getId(), "Plato", 30000, 2)));
        comprobar("queda registrada", ok.exitosa() && pedidos.listarTodos().size() == 1);
        comprobar("descuenta la cantidad exacta de cada producto",
                productos.buscarPorId(taza.getId()).getStock() == 2
                && productos.buscarPorId(plato.getId()).getStock() == 0);
        comprobar("usa la dirección de la cuenta",
                "Calle 1 # 2-3".equals(ok.pedido().getDireccionEntrega()));
        comprobar("avisa al comprador y a la tienda",
                correos.compras.size() == 1 && correos.ventas.size() == 1);

        System.out.println("Compra sin existencias (todo o nada)");
        ResultadoCompra falta = servicio.confirmar(ana, List.of(
                new LineaPedido(taza.getId(), "Taza", 20000, 1),
                new LineaPedido(plato.getId(), "Plato", 30000, 1)));
        comprobar("dice qué producto faltó", "Plato".equals(falta.faltante()) && !falta.exitosa());
        comprobar("devuelve al stock lo ya descontado",
                productos.buscarPorId(taza.getId()).getStock() == 2);
        comprobar("no registra pedido ni envía correos",
                pedidos.listarTodos().size() == 1 && correos.compras.size() == 1);

        System.out.println("Si falla el registro del pedido");
        IPedidoRepository roto = new PedidoRepositoryMemoria() {
            @Override
            public Pedido registrar(Pedido pedido) {
                throw new IllegalStateException("sin conexión");
            }
        };
        CompraService conFallo = new CompraService(productos, roto, correos);
        String error = "";
        try {
            conFallo.confirmar(ana, List.of(new LineaPedido(taza.getId(), "Taza", 20000, 2)));
        } catch (IllegalStateException ex) {
            error = ex.getMessage();
        }
        comprobar("el fallo llega a quien llamó", "sin conexión".equals(error));
        comprobar("y el stock vuelve entero", productos.buscarPorId(taza.getId()).getStock() == 2);

        System.out.println("Historial del cliente");
        comprobar("devuelve las compras de ese correo",
                servicio.historial("ana@correo.com").size() == 1
                && servicio.historial("otro@correo.com").isEmpty());

        System.out.println(fallos == 0 ? "\nTODO CORRECTO" : "\n" + fallos + " FALLAS");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
