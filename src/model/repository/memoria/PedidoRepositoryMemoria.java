package model.repository.memoria;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import model.entity.Pedido;
import model.repository.IPedidoRepository;

/**
 * Implementación en memoria de {@link IPedidoRepository}.
 *
 * <p>Los pedidos se devuelven del más reciente al más antiguo porque así es
 * como se leen en "Mis compras": lo último que compré es lo que más me
 * interesa ver primero.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class PedidoRepositoryMemoria implements IPedidoRepository {

    private final List<Pedido> pedidos = new ArrayList<>();
    private final AtomicInteger secuencia = new AtomicInteger();

    @Override
    public Pedido registrar(Pedido pedido) {
        pedido.setId("ORD-" + String.format("%04d", secuencia.incrementAndGet()));
        pedidos.add(pedido);
        return pedido;
    }

    @Override
    public List<Pedido> listarPorUsuario(String correoUsuario) {
        List<Pedido> resultado = new ArrayList<>();
        for (Pedido p : pedidos) {
            if (p.getCorreoUsuario().equalsIgnoreCase(correoUsuario)) {
                resultado.add(0, p);
            }
        }
        return resultado;
    }

    @Override
    public List<Pedido> listarTodos() {
        List<Pedido> resultado = new ArrayList<>(pedidos);
        java.util.Collections.reverse(resultado);
        return resultado;
    }
}
