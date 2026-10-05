package model.repository.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import model.entity.Pedido;
import model.repository.IPedidoRepository;
import model.repository.mongo.adapter.AdaptadorDocumento;
import model.repository.mongo.adapter.CompraAdapter;

/**
 * Implementación de {@link IPedidoRepository} sobre la colección
 * {@code compras} de MongoDB Atlas.
 *
 * <p><b>Solo consultas.</b> Cómo se anidan los renglones y cómo se convierte
 * la fecha es de {@link CompraAdapter}; esta clase decide qué se inserta, qué
 * se filtra y en qué orden se devuelve.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.1
 */
public class PedidoRepositoryMongo implements IPedidoRepository {

    private static final String COLECCION = "compras";

    private final MongoCollection<Document> compras;
    private final AdaptadorDocumento<Pedido> adaptador = new CompraAdapter();

    /**
     * @param base base de datos ya conectada
     */
    public PedidoRepositoryMongo(MongoDatabase base) {
        this.compras = base.getCollection(COLECCION);
    }

    @Override
    public Pedido registrar(Pedido pedido) {
        Document documento = adaptador.aDocumento(pedido);
        compras.insertOne(documento);
        // MongoDB escribe el _id que asignó en el mismo documento insertado.
        pedido.setId(documento.getObjectId(CompraAdapter.CAMPO_ID).toHexString());
        return pedido;
    }

    @Override
    public List<Pedido> listarPorUsuario(String correoUsuario) {
        if (correoUsuario == null) {
            return new ArrayList<>();
        }
        return adaptador.aEntidades(compras.find(Filters.regex(CompraAdapter.CAMPO_CORREO_USUARIO,
                        "^" + Pattern.quote(correoUsuario.trim()) + "$", "i"))
                .sort(Sorts.descending(CompraAdapter.CAMPO_FECHA)));
    }

    @Override
    public List<Pedido> listarTodos() {
        return adaptador.aEntidades(compras.find().sort(Sorts.descending(CompraAdapter.CAMPO_FECHA)));
    }
}
