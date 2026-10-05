package model.repository.mongo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import model.entity.Resena;
import model.repository.IResenaRepository;
import model.repository.mongo.adapter.AdaptadorDocumento;
import model.repository.mongo.adapter.ResenaAdapter;

/**
 * Reseñas en la colección {@code resenas} de MongoDB Atlas.
 *
 * <p>Un índice único sobre producto + autor cierra en la base la regla de
 * "una reseña por persona y producto": guardar es un reemplazo con
 * {@code upsert}, así que una segunda opinión sustituye a la primera en una
 * sola operación, sin la carrera de "buscar y luego insertar".</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class ResenaRepositoryMongo implements IResenaRepository {

    private static final String COLECCION = "resenas";

    private final MongoCollection<Document> resenas;
    private final AdaptadorDocumento<Resena> adaptador = new ResenaAdapter();

    /**
     * @param base base de datos ya conectada
     */
    public ResenaRepositoryMongo(MongoDatabase base) {
        this.resenas = base.getCollection(COLECCION);
        this.resenas.createIndex(Indexes.ascending(
                        ResenaAdapter.CAMPO_ID_PRODUCTO, ResenaAdapter.CAMPO_CORREO_AUTOR),
                new IndexOptions().unique(true));
    }

    @Override
    public void guardar(Resena resena) {
        resenas.replaceOne(Filters.and(
                        Filters.eq(ResenaAdapter.CAMPO_ID_PRODUCTO, resena.getIdProducto()),
                        Filters.eq(ResenaAdapter.CAMPO_CORREO_AUTOR, resena.getCorreoAutor())),
                adaptador.aDocumento(resena), new ReplaceOptions().upsert(true));
    }

    @Override
    public List<Resena> listarPorProducto(String idProducto) {
        return adaptador.aEntidades(resenas.find(
                        Filters.eq(ResenaAdapter.CAMPO_ID_PRODUCTO, idProducto))
                .sort(Sorts.descending(ResenaAdapter.CAMPO_FECHA)));
    }

    /**
     * {@inheritDoc}
     *
     * <p>La agrupación la hace MongoDB ({@code $group} con {@code $avg} y
     * {@code $sum}): viajan unas pocas cifras, no todas las reseñas.</p>
     */
    @Override
    public Map<String, double[]> resumenPorProducto() {
        Map<String, double[]> resumen = new HashMap<>();
        for (Document grupo : resenas.aggregate(List.of(Aggregates.group(
                "$" + ResenaAdapter.CAMPO_ID_PRODUCTO,
                Accumulators.avg("promedio", "$" + ResenaAdapter.CAMPO_ESTRELLAS),
                Accumulators.sum("total", 1))))) {
            resumen.put(grupo.getString("_id"), new double[]{
                    grupo.get("promedio", Number.class).doubleValue(),
                    grupo.get("total", Number.class).doubleValue()});
        }
        return resumen;
    }
}
