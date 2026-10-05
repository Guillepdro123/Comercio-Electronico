package model.repository.mongo.adapter;

import java.text.Normalizer;
import org.bson.Document;
import org.bson.types.ObjectId;
import model.entity.Categoria;
import model.entity.Producto;

/**
 * Adaptador entre la entidad {@link Producto} y los documentos de la colección
 * {@code productos}.
 *
 * <p>Tres diferencias de formato que resuelve y que el resto del programa no
 * tiene por qué conocer:</p>
 * <ul>
 *   <li><b>Identificador.</b> MongoDB usa {@code ObjectId}; la aplicación
 *       trata el id de un producto como un {@code String} opaco. Se convierte
 *       aquí en los dos sentidos.</li>
 *   <li><b>Números.</b> Un precio escrito a mano en Atlas puede llegar como
 *       entero aunque la aplicación lo guarde como decimal. Se lee como
 *       {@link Number} y se convierte, en vez de exigir un tipo exacto.</li>
 *   <li><b>Categoría.</b> Se guarda por el nombre de la constante del
 *       enumerado, no por su etiqueta visible, para que cambiar un texto de
 *       pantalla no deje huérfanos los productos ya guardados.</li>
 * </ul>
 *
 * <p>Además escribe un campo derivado, {@code busqueda}, que no existe en la
 * entidad: nombre y descripción sin acentos ni mayúsculas. El repositorio
 * busca sobre él, y por eso {@link #normalizar(String)} es público: la
 * consulta tiene que normalizar el texto buscado exactamente igual que se
 * normalizó lo guardado.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class ProductoAdapter implements AdaptadorDocumento<Producto> {

    /** Identificador de MongoDB. */
    public static final String CAMPO_ID = "_id";
    /** Filtro por categoría del catálogo. */
    public static final String CAMPO_CATEGORIA = "categoria";
    /** Campo derivado sobre el que se busca por texto. */
    public static final String CAMPO_BUSQUEDA = "busqueda";
    /** Filtro de "mis productos" del Proveedor. */
    public static final String CAMPO_CORREO_PROVEEDOR = "correoProveedor";
    /** Existencias; se descuentan con una actualización atómica. */
    public static final String CAMPO_STOCK = "stock";

    private static final String CAMPO_NOMBRE = "nombre";
    private static final String CAMPO_DESCRIPCION = "descripcion";
    private static final String CAMPO_PRECIO = "precio";
    private static final String CAMPO_DESCUENTO = "porcentajeDescuento";
    /** Referencia de imagen; la normalización de rutas antiguas la busca. */
    public static final String CAMPO_IMAGEN = "imagen";
    /** Empresa del vendedor; se reescribe en bloque cuando el proveedor la cambia. */
    public static final String CAMPO_MARCA = "marca";

    /**
     * Un producto recién creado todavía no tiene id: el documento sale sin
     * {@code _id} y MongoDB le asigna uno al insertarlo.
     */
    @Override
    public Document aDocumento(Producto producto) {
        Document documento = new Document(CAMPO_NOMBRE, producto.getNombre())
                .append(CAMPO_DESCRIPCION, producto.getDescripcion())
                .append(CAMPO_PRECIO, producto.getPrecio())
                .append(CAMPO_DESCUENTO, producto.getPorcentajeDescuento())
                .append(CAMPO_CATEGORIA, producto.getCategoria().name())
                .append(CAMPO_STOCK, producto.getStock())
                .append(CAMPO_IMAGEN, producto.getImagen())
                .append(CAMPO_CORREO_PROVEEDOR, producto.getCorreoProveedor())
                // Guardarlo ya normalizado evita normalizar en cada consulta,
                // que es lo que impediría usar un índice.
                .append(CAMPO_BUSQUEDA,
                        normalizar(producto.getNombre() + " " + producto.getDescripcion()));
        // Campo añadido después: sin marca, el documento es el de siempre.
        if (!producto.getMarca().isEmpty()) {
            documento.append(CAMPO_MARCA, producto.getMarca());
        }
        ObjectId identificador = aObjectId(producto.getId());
        if (identificador != null) {
            documento.append(CAMPO_ID, identificador);
        }
        return documento;
    }

    @Override
    public Producto aEntidad(Document documento) {
        Producto producto = new Producto(
                documento.getObjectId(CAMPO_ID).toHexString(),
                documento.getString(CAMPO_NOMBRE),
                documento.getString(CAMPO_DESCRIPCION),
                documento.get(CAMPO_PRECIO, Number.class).doubleValue(),
                documento.get(CAMPO_DESCUENTO, Number.class).intValue(),
                Categoria.desdeNombre(documento.getString(CAMPO_CATEGORIA)),
                documento.get(CAMPO_STOCK, Number.class).intValue(),
                documento.getString(CAMPO_IMAGEN),
                documento.getString(CAMPO_CORREO_PROVEEDOR));
        producto.setMarca(documento.getString(CAMPO_MARCA));
        return producto;
    }

    /**
     * @param id identificador en texto, tal como lo usa la aplicación
     * @return el {@code ObjectId} equivalente, o {@code null} si está vacío o
     *         no tiene forma de identificador de MongoDB (un producto recién
     *         creado todavía no tiene)
     */
    public static ObjectId aObjectId(String id) {
        if (id == null || id.isBlank() || !ObjectId.isValid(id)) {
            return null;
        }
        return new ObjectId(id);
    }

    /**
     * Quita acentos y mayúsculas, igual que el almacén en memoria.
     *
     * @param texto texto libre
     * @return el texto tal como se guarda en {@value #CAMPO_BUSQUEDA}
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase().trim();
    }
}
