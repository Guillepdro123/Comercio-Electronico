package model.repository;

import java.util.List;
import model.entity.Pedido;

/**
 * Contrato de persistencia de los pedidos.
 *
 * <p>Igual que {@link IProductoRepository}, es el punto de entrada de MongoDB:
 * la colección de órdenes se implementará detrás de esta interfaz sin que el
 * checkout ni el panel de compras cambien.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public interface IPedidoRepository {

    /**
     * Guarda un pedido confirmado.
     *
     * @param pedido pedido a registrar
     * @return el pedido ya con su id asignado
     */
    Pedido registrar(Pedido pedido);

    /**
     * @param correoUsuario comprador
     * @return sus pedidos, del más reciente al más antiguo
     */
    List<Pedido> listarPorUsuario(String correoUsuario);

    /** @return todos los pedidos registrados (lo usan las estadísticas del proveedor) */
    List<Pedido> listarTodos();
}
