package aplicacion.catalogo;

import model.entity.Producto;

/**
 * Cómo terminó una operación sobre el catálogo: el producto afectado, o el
 * motivo por el que no se pudo hacer.
 *
 * <p>Mismo patrón que {@code ResultadoCompra}: un dato de vuelta en vez de una
 * excepción, porque un formulario mal rellenado o un producto que otra sesión
 * ya borró son situaciones normales del negocio, no fallos del programa. El
 * mensaje viene redactado para mostrárselo al usuario tal cual.</p>
 *
 * @param producto producto publicado, actualizado o eliminado; {@code null} si
 *                 la operación no se hizo
 * @param error    qué salió mal, o {@code null} si todo fue bien
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record ResultadoProducto(Producto producto, String error) {

    /** @return resultado correcto, con el producto afectado */
    public static ResultadoProducto hecho(Producto producto) {
        return new ResultadoProducto(producto, null);
    }

    /** @return resultado fallido, con el mensaje para el usuario */
    public static ResultadoProducto fallo(String error) {
        return new ResultadoProducto(null, error);
    }

    /** @return {@code true} si la operación se completó */
    public boolean exitoso() {
        return error == null;
    }
}
