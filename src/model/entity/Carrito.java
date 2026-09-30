package model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Carrito de compras de la sesión actual.
 *
 * <p><b>No se persiste.</b> Vive mientras el usuario navega y desaparece al
 * confirmar la compra o cerrar sesión; lo que se guarda es el {@link Pedido}
 * que resulta de él. Por eso no tiene repositorio.</p>
 *
 * <p>Usa un {@link LinkedHashMap} por id de producto: así, agregar dos veces
 * el mismo artículo suma cantidades en vez de crear dos renglones, y el orden
 * en que se fueron agregando se conserva al mostrarlo.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class Carrito {

    private final Map<String, LineaPedido> lineas = new LinkedHashMap<>();

    /**
     * Agrega unidades de un producto, sumando si ya estaba en el carrito.
     *
     * @param producto producto elegido
     * @param cantidad unidades a agregar
     */
    public void agregar(Producto producto, int cantidad) {
        if (cantidad <= 0) {
            return;
        }
        LineaPedido existente = lineas.get(producto.getId());
        int total = cantidad + (existente == null ? 0 : existente.getCantidad());
        lineas.put(producto.getId(), new LineaPedido(
                producto.getId(), producto.getNombre(), producto.getPrecioFinal(), total));
    }

    /**
     * Quita por completo un producto del carrito.
     *
     * @param idProducto identificador del producto a quitar
     */
    public void quitar(String idProducto) {
        lineas.remove(idProducto);
    }

    /** Deja el carrito vacío (tras confirmar la compra). */
    public void vaciar() {
        lineas.clear();
    }

    /** @return renglones actuales, en solo lectura */
    public List<LineaPedido> getLineas() {
        return Collections.unmodifiableList(new ArrayList<>(lineas.values()));
    }

    /** @return importe total del carrito */
    public double getTotal() {
        return lineas.values().stream().mapToDouble(LineaPedido::getSubtotal).sum();
    }

    /** @return número de artículos, sumando cantidades */
    public int getTotalUnidades() {
        return lineas.values().stream().mapToInt(LineaPedido::getCantidad).sum();
    }

    /** @return {@code true} si no hay nada que comprar */
    public boolean estaVacio() {
        return lineas.isEmpty();
    }
}
