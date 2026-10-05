package aplicacion.compra;

import model.entity.Pedido;

/**
 * Cómo terminó un intento de compra: el pedido confirmado, el nombre del
 * producto que no tenía existencias suficientes, o que a la cuenta le faltan
 * los datos de envío.
 *
 * <p>Existe porque {@link CompraService#confirmar} tiene varias salidas
 * posibles y un método solo puede devolver un valor. Es un resultado, no una
 * excepción: que un producto se agote entre que se agrega al carrito y se
 * confirma la compra es un caso normal del negocio, no un fallo del
 * programa.</p>
 *
 * @param pedido            pedido registrado, o {@code null} si no llegó a hacerse
 * @param faltante          nombre del producto sin existencias, o {@code null}
 * @param faltanDatosEnvio  {@code true} si la cuenta no tiene cédula o
 *                          dirección y por eso no se intentó la compra
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 2.0
 */
public record ResultadoCompra(Pedido pedido, String faltante, boolean faltanDatosEnvio) {

    /** @return resultado de una compra confirmada */
    public static ResultadoCompra confirmada(Pedido pedido) {
        return new ResultadoCompra(pedido, null, false);
    }

    /** @return resultado de una compra que no se pudo hacer por falta de stock */
    public static ResultadoCompra sinExistencias(String nombreProducto) {
        return new ResultadoCompra(null, nombreProducto, false);
    }

    /** @return resultado de una compra rechazada porque faltan cédula o dirección */
    public static ResultadoCompra sinDatosEnvio() {
        return new ResultadoCompra(null, null, true);
    }

    /** @return {@code true} si la compra quedó registrada */
    public boolean exitosa() {
        return pedido != null;
    }
}
