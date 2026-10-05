package model.repository.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import model.entity.Categoria;
import model.entity.Producto;
import model.repository.IProductoRepository;
import model.repository.mongo.adapter.AdaptadorDocumento;
import model.repository.mongo.adapter.ProductoAdapter;

/**
 * Implementación de {@link IProductoRepository} sobre la colección
 * {@code productos} de MongoDB Atlas.
 *
 * <p><b>Solo consultas.</b> La conversión entre documento y {@link Producto}
 * (el {@code ObjectId}, los números, la categoría, el campo derivado de
 * búsqueda) es de {@link ProductoAdapter}; esta clase decide qué se consulta y
 * qué operación se lanza.</p>
 *
 * <p><b>El filtrado se hace en la base, no en Java.</b> Era la razón por la
 * que {@code buscar(texto, categoria)} se declaró en la interfaz en lugar de
 * traerse el catálogo entero y descartarlo en el cliente: aquí esa llamada se
 * traduce a una consulta.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.1
 */
public class ProductoRepositoryMongo implements IProductoRepository {

    /** Visible en el paquete: {@link VigilanteCatalogoMongo} vigila esta misma colección. */
    static final String COLECCION = "productos";

    private final MongoCollection<Document> productos;
    private final AdaptadorDocumento<Producto> adaptador = new ProductoAdapter();

    /**
     * @param base base de datos ya conectada
     */
    public ProductoRepositoryMongo(MongoDatabase base) {
        this.productos = base.getCollection(COLECCION);
    }

    @Override
    public List<Producto> listarTodos() {
        return adaptador.aEntidades(productos.find());
    }

    @Override
    public List<Producto> listarPorCategoria(Categoria categoria) {
        if (categoria == null) {
            return listarTodos();
        }
        return adaptador.aEntidades(productos.find(
                Filters.eq(ProductoAdapter.CAMPO_CATEGORIA, categoria.name())));
    }

    @Override
    public List<Producto> buscar(String texto, Categoria categoria) {
        List<Bson> condiciones = new ArrayList<>();
        if (categoria != null) {
            condiciones.add(Filters.eq(ProductoAdapter.CAMPO_CATEGORIA, categoria.name()));
        }
        if (texto != null && !texto.isBlank()) {
            // El texto buscado se normaliza con la misma función con que el
            // adaptador guardó el campo de búsqueda: si difirieran, "cafe" no
            // encontraría "Café".
            String patron = Pattern.quote(ProductoAdapter.normalizar(texto));
            condiciones.add(Filters.regex(ProductoAdapter.CAMPO_BUSQUEDA, patron, "i"));
        }
        if (condiciones.isEmpty()) {
            return listarTodos();
        }
        return adaptador.aEntidades(productos.find(Filters.and(condiciones)));
    }

    @Override
    public Producto buscarPorId(String id) {
        ObjectId identificador = ProductoAdapter.aObjectId(id);
        if (identificador == null) {
            return null;
        }
        Document encontrado = productos.find(porId(identificador)).first();
        return encontrado == null ? null : adaptador.aEntidad(encontrado);
    }

    @Override
    public List<Producto> listarPorProveedor(String correoProveedor) {
        if (correoProveedor == null) {
            return new ArrayList<>();
        }
        return adaptador.aEntidades(productos.find(Filters.regex(
                ProductoAdapter.CAMPO_CORREO_PROVEEDOR,
                "^" + Pattern.quote(correoProveedor.trim()) + "$", "i")));
    }

    @Override
    public Producto guardar(Producto producto) {
        Document documento = adaptador.aDocumento(producto);
        ObjectId identificador = ProductoAdapter.aObjectId(producto.getId());
        if (identificador == null) {
            productos.insertOne(documento);
            // MongoDB escribe el _id que asignó en el mismo documento insertado.
            producto.setId(documento.getObjectId(ProductoAdapter.CAMPO_ID).toHexString());
            return producto;
        }
        productos.replaceOne(porId(identificador), documento);
        return producto;
    }

    @Override
    public boolean eliminar(String id) {
        ObjectId identificador = ProductoAdapter.aObjectId(id);
        return identificador != null
                && productos.deleteOne(porId(identificador)).getDeletedCount() > 0;
    }

    @Override
    public boolean descontarStock(String id, int unidades) {
        ObjectId identificador = ProductoAdapter.aObjectId(id);
        if (identificador == null || unidades <= 0) {
            return false;
        }
        // La condición de existencias va dentro del filtro, no en un 'if'
        // previo: así la comprobación y el descuento son una sola operación y
        // no puede colarse una venta entre una cosa y la otra.
        return productos.updateOne(
                Filters.and(porId(identificador),
                        Filters.gte(ProductoAdapter.CAMPO_STOCK, unidades)),
                Updates.inc(ProductoAdapter.CAMPO_STOCK, -unidades)).getModifiedCount() > 0;
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@code $inc} positivo, también atómico: si mientras tanto otro
     * comprador descontó unidades de este producto, las dos operaciones se
     * suman correctamente en vez de pisarse, que es lo que pasaría leyendo el
     * stock, sumando en Java y escribiendo el resultado.</p>
     */
    @Override
    public void reponerStock(String id, int unidades) {
        ObjectId identificador = ProductoAdapter.aObjectId(id);
        if (identificador == null || unidades <= 0) {
            return;
        }
        productos.updateOne(porId(identificador), Updates.inc(ProductoAdapter.CAMPO_STOCK, unidades));
    }

    @Override
    public int actualizarVendedor(String correoAnterior, String correoNuevo, String marca) {
        if (correoAnterior == null || correoNuevo == null) {
            return 0;
        }
        // Una sola operación sobre todos sus productos: el mismo filtro sin
        // distinguir mayúsculas que usa listarPorProveedor.
        return (int) productos.updateMany(
                Filters.regex(ProductoAdapter.CAMPO_CORREO_PROVEEDOR,
                        "^" + Pattern.quote(correoAnterior.trim()) + "$", "i"),
                Updates.combine(
                        Updates.set(ProductoAdapter.CAMPO_CORREO_PROVEEDOR, correoNuevo.trim()),
                        Updates.set(ProductoAdapter.CAMPO_MARCA, marca)))
                .getModifiedCount();
    }

    private Bson porId(ObjectId identificador) {
        return Filters.eq(ProductoAdapter.CAMPO_ID, identificador);
    }
}
