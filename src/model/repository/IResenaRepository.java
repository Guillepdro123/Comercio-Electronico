package model.repository;

import java.util.List;
import java.util.Map;
import model.entity.Resena;

/**
 * Contrato de persistencia de las reseñas de productos.
 *
 * <p>Mismo esquema que el resto de repositorios: el caso de uso depende de
 * esta interfaz y {@code app.Main} decide si detrás hay memoria o MongoDB.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public interface IResenaRepository {

    /**
     * Guarda la reseña de un autor sobre un producto; si ya tenía una, la
     * reemplaza (una reseña por persona y producto).
     *
     * @param resena reseña ya validada
     */
    void guardar(Resena resena);

    /**
     * @param idProducto producto
     * @return sus reseñas, de la más reciente a la más antigua
     */
    List<Resena> listarPorProducto(String idProducto);

    /**
     * Promedio y cantidad de reseñas de cada producto que tenga alguna, en una
     * sola consulta: el catálogo los muestra en todas sus tarjetas, y pedirlos
     * de uno en uno serían diez viajes a la base por cada recarga.
     *
     * @return por identificador de producto: {@code [promedio, cantidad]}
     */
    Map<String, double[]> resumenPorProducto();
}
