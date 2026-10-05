package model.repository.mongo.adapter;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 * Contrato común de los adaptadores entre documentos BSON y entidades
 * (patrón <em>Adapter</em>).
 *
 * <p><b>Qué se adapta.</b> MongoDB habla en {@link Document}: un mapa de
 * campos con tipos BSON ({@code ObjectId}, {@code Date}, números que pueden
 * llegar como entero o como decimal). El dominio habla en entidades
 * ({@code Usuario}, {@code Producto}, {@code Pedido}) con tipos de Java
 * ({@code String}, {@code LocalDateTime}, enumerados). Son dos interfaces que
 * no encajan, y cada adaptador es la pieza que las conecta en los dos
 * sentidos, para que ni el repositorio ni el modelo tengan que conocer el
 * idioma del otro.</p>
 *
 * <p><b>Responsabilidad única.</b> Un repositorio decide <em>qué</em> se
 * consulta (filtros, orden, operaciones atómicas); el adaptador decide
 * <em>cómo</em> se escribe y se lee cada campo. Si mañana cambia el formato
 * de un documento, se toca solo su adaptador; si cambia una consulta, solo el
 * repositorio.</p>
 *
 * <p>La interfaz existe además para no repetir {@link #aEntidades(Iterable)}:
 * antes cada repositorio tenía su propio bucle de conversión idéntico.</p>
 *
 * @param <T> entidad del dominio que se adapta
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public interface AdaptadorDocumento<T> {

    /**
     * @param entidad entidad del dominio
     * @return el documento listo para guardar en MongoDB
     */
    Document aDocumento(T entidad);

    /**
     * @param documento documento leído de MongoDB
     * @return la entidad reconstruida
     */
    T aEntidad(Document documento);

    /**
     * Convierte el resultado entero de una consulta.
     *
     * @param documentos lo que devolvió {@code find(...)}
     * @return las entidades, en el mismo orden
     */
    default List<T> aEntidades(Iterable<Document> documentos) {
        List<T> resultado = new ArrayList<>();
        for (Document documento : documentos) {
            resultado.add(aEntidad(documento));
        }
        return resultado;
    }
}
