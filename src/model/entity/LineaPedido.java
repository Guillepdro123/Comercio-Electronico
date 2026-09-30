package model.entity;

/**
 * Entidad del dominio: un renglón de un pedido — qué producto, cuántas
 * unidades y a qué precio.
 *
 * <p><b>Guarda una foto del precio, no una referencia al producto.</b> Los
 * precios y los descuentos cambian; si un pedido apuntara al {@link Producto}
 * vivo, una rebaja posterior reescribiría el historial de compras de la
 * gente. Por eso se copian aquí el nombre y el precio unitario del momento de
 * la compra: un pedido es un hecho ocurrido, no una consulta al catálogo.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class LineaPedido {

    private final String idProducto;
    private final String nombreProducto;
    private final double precioUnitario;
    private final int cantidad;

    /**
     * @param idProducto     identificador del producto comprado
     * @param nombreProducto nombre en el momento de la compra
     * @param precioUnitario precio ya con descuento, en el momento de la compra
     * @param cantidad       unidades compradas
     */
    public LineaPedido(String idProducto, String nombreProducto, double precioUnitario, int cantidad) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    /** @return importe de este renglón */
    public double getSubtotal() {
        return precioUnitario * cantidad;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }
}
