package model.repository;

import java.util.List;
import model.entity.Categoria;
import model.entity.Producto;

/**
 * Contrato de persistencia del catálogo.
 *
 * <p><b>Este es el punto donde entrará MongoDB.</b> Toda la aplicación —
 * catálogo, carrito, checkout, panel del proveedor — habla con esta interfaz y
 * nunca con una implementación concreta. Cambiar el almacén en memoria por una
 * colección de Atlas es escribir otra clase que implemente estos métodos y
 * cambiar una línea en {@code app.Main}; ni las vistas ni los controladores se
 * enteran (Inversión de Dependencias).</p>
 *
 * <p><b>Segregación de Interfaces:</b> solo declara las consultas que algún
 * caso de uso necesita hoy. El filtrado y la búsqueda están aquí, y no
 * resueltos a mano sobre una lista completa, porque una base de datos real los
 * hace mucho mejor que el cliente: así la implementación de Mongo podrá
 * traducirlos a una consulta en vez de traerse el catálogo entero.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public interface IProductoRepository {

    /** @return todos los productos del catálogo */
    List<Producto> listarTodos();

    /**
     * @param categoria categoría a filtrar; {@code null} significa "todas"
     * @return productos de esa categoría
     */
    List<Producto> listarPorCategoria(Categoria categoria);

    /**
     * Busca por coincidencia en nombre o descripción, sin distinguir
     * mayúsculas ni acentos de más.
     *
     * @param texto      texto buscado; vacío devuelve todo
     * @param categoria  categoría a restringir, o {@code null} para todas
     * @return productos que coinciden
     */
    List<Producto> buscar(String texto, Categoria categoria);

    /**
     * @param id identificador del producto
     * @return el producto, o {@code null} si no existe
     */
    Producto buscarPorId(String id);

    /**
     * @param correoProveedor proveedor dueño de los productos
     * @return productos publicados por ese proveedor
     */
    List<Producto> listarPorProveedor(String correoProveedor);

    /**
     * Inserta el producto si no tiene id, o actualiza el existente.
     *
     * @param producto producto a guardar
     * @return el producto ya con su id asignado
     */
    Producto guardar(Producto producto);

    /**
     * @param id identificador del producto a eliminar
     * @return {@code true} si existía y se eliminó
     */
    boolean eliminar(String id);

    /**
     * Descuenta unidades del stock tras una compra.
     *
     * @param id       identificador del producto
     * @param unidades cantidad vendida
     * @return {@code true} si había existencias suficientes y se descontó
     */
    boolean descontarStock(String id, int unidades);
}
