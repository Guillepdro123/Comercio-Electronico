package model.repository.mongo;

import java.util.Date;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import model.repository.IImagenRepository;

/**
 * Imágenes de producto guardadas dentro de MongoDB Atlas, en la colección
 * {@code imagenes}.
 *
 * <p><b>Por qué en la base y no en una carpeta.</b> Varios equipos comparten
 * el mismo Atlas: una imagen copiada a una carpeta del equipo que la subió no
 * existiría en los demás. Guardada aquí, cualquier equipo conectado la ve. Se
 * guarda como binario en un documento (no con GridFS) porque llega ya reducida
 * a unos cientos de KB, muy por debajo del límite de 16 MB por documento, y un
 * documento simple es más fácil de inspeccionar desde Atlas.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class ImagenRepositoryMongo implements IImagenRepository {

    private static final String COLECCION = "imagenes";
    private static final String CAMPO_ID = "_id";
    private static final String CAMPO_DATOS = "datos";
    private static final String CAMPO_EXTENSION = "extension";
    private static final String CAMPO_FECHA = "fecha";

    private final MongoCollection<Document> imagenes;

    /**
     * @param base base de datos ya conectada
     */
    public ImagenRepositoryMongo(MongoDatabase base) {
        this.imagenes = base.getCollection(COLECCION);
    }

    @Override
    public String guardar(byte[] datos, String extension) {
        Document documento = new Document(CAMPO_DATOS, new Binary(datos))
                .append(CAMPO_EXTENSION, extension)
                .append(CAMPO_FECHA, new Date());
        imagenes.insertOne(documento);
        return PREFIJO + documento.getObjectId(CAMPO_ID).toHexString();
    }

    @Override
    public byte[] leer(String referencia) {
        if (referencia == null || !referencia.startsWith(PREFIJO)) {
            return null;
        }
        String id = referencia.substring(PREFIJO.length());
        if (!ObjectId.isValid(id)) {
            return null;
        }
        Document encontrado = imagenes.find(Filters.eq(CAMPO_ID, new ObjectId(id))).first();
        if (encontrado == null) {
            return null;
        }
        Binary datos = encontrado.get(CAMPO_DATOS, Binary.class);
        return datos == null ? null : datos.getData();
    }
}
