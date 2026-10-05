package model.repository;

/**
 * Contrato del almacén de imágenes de producto subidas por los proveedores.
 *
 * <p><b>Por qué existe.</b> Antes se guardaba en el producto la ruta absoluta
 * del archivo elegido ({@code C:\Users\...}), que solo existe en el equipo
 * donde se eligió: en cualquier otro, o empaquetado, la imagen desaparecía.
 * Ahora la imagen se copia a este almacén y el producto guarda solo una
 * referencia {@code img:<id>}, válida en cualquier equipo que vea la misma
 * base.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public interface IImagenRepository {

    /** Prefijo de las referencias que apuntan a este almacén. */
    String PREFIJO = "img:";

    /**
     * Guarda una imagen.
     *
     * @param datos     bytes de la imagen ya preparada (PNG o JPEG)
     * @param extension {@code "png"} o {@code "jpg"}
     * @return la referencia que debe guardarse en el producto
     *         ({@value #PREFIJO} + identificador)
     */
    String guardar(byte[] datos, String extension);

    /**
     * @param referencia referencia devuelta por {@link #guardar(byte[], String)}
     * @return los bytes de la imagen, o {@code null} si no existe
     */
    byte[] leer(String referencia);
}
