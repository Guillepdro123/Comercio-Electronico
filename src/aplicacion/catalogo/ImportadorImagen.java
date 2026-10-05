package aplicacion.catalogo;

import java.io.File;
import java.io.IOException;
import model.repository.IImagenRepository;
import service.imagen.DescargadorImagenes;
import service.imagen.OptimizadorImagen;

/**
 * Convierte la imagen que eligió un proveedor en una referencia portable.
 *
 * <p><b>Regla: en la base nunca se guarda una ruta del disco.</b> Una ruta
 * como {@code C:\Users\ana\fotos\cafetera.png} solo existe en el equipo de
 * Ana. Hay tres tipos de referencia válidos y solo esos llegan al producto:</p>
 * <ul>
 *   <li>vacía: el producto no tiene imagen y se muestra su inicial;</li>
 *   <li>el nombre de una ilustración incluida en el programa
 *       ({@code cafetera.png}), que viaja dentro del propio programa;</li>
 *   <li>{@code img:<id>}: una imagen copiada al {@link IImagenRepository}.</li>
 * </ul>
 * <p>Cualquier otra cosa es una ruta local: si el archivo existe en este equipo
 * se copia al almacén, y si no, se intenta reconocer como ilustración
 * incluida por su nombre.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class ImportadorImagen {

    /** Carpeta de recursos donde viajan las ilustraciones de ejemplo. */
    private static final String RECURSOS = "/resources/images/productos/";

    private final IImagenRepository imagenes;

    /**
     * @param imagenes almacén al que se copian las imágenes elegidas del disco
     */
    public ImportadorImagen(IImagenRepository imagenes) {
        this.imagenes = imagenes;
    }

    /**
     * Resuelve la referencia que llega del formulario.
     *
     * @param referencia ruta elegida, nombre de ilustración o referencia ya
     *                   portable
     * @return la referencia portable a guardar en el producto
     * @throws IllegalArgumentException si es una ruta que no existe ni
     *         corresponde a una ilustración incluida, o no es una imagen
     */
    public String resolver(String referencia) {
        String limpia = referencia == null ? "" : referencia.trim();
        if (limpia.isEmpty() || esPortable(limpia)) {
            return limpia;
        }
        File archivo = new File(limpia);
        if (archivo.isFile()) {
            try {
                OptimizadorImagen.ImagenPreparada preparada = OptimizadorImagen.preparar(archivo);
                return imagenes.guardar(preparada.datos(), preparada.extension());
            } catch (IOException ex) {
                throw new IllegalArgumentException("No se pudo leer la imagen: " + ex.getMessage());
            }
        }
        String incluida = archivo.getName();
        if (esIlustracionIncluida(incluida)) {
            return incluida;
        }
        throw new IllegalArgumentException("No se encontró la imagen " + incluida
                + " en este equipo. Vuelve a elegirla.");
    }

    /**
     * Igual que {@link #resolver(String)}, pero para datos ya guardados: si la
     * ruta no se puede recuperar, el producto se queda sin imagen en vez de
     * fallar (muestra su inicial, que es la señal de que falta).
     *
     * @param referencia referencia guardada
     * @return referencia portable, o vacía si no se pudo recuperar
     */
    public String resolverTolerante(String referencia) {
        try {
            return resolver(referencia);
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    /**
     * @param referencia referencia guardada en un producto
     * @return {@code true} si es una ruta del disco que hay que migrar
     */
    public boolean esRutaLocal(String referencia) {
        return referencia != null && !referencia.isBlank() && !esPortable(referencia.trim());
    }

    private boolean esPortable(String referencia) {
        if (referencia.startsWith(IImagenRepository.PREFIJO)) {
            return true;
        }
        // Una URL web ya es portable: se ve en cualquier equipo con conexión.
        // Sin esto contaba como ruta del disco (lleva ":" y "/") y la
        // normalización de arranque la borraba de todos los productos.
        if (DescargadorImagenes.esUrl(referencia)) {
            return true;
        }
        // Un nombre suelto, sin carpetas, es una ilustración incluida.
        return !referencia.contains("/") && !referencia.contains("\\") && !referencia.contains(":");
    }

    private boolean esIlustracionIncluida(String nombre) {
        return getClass().getResource(RECURSOS + nombre) != null
                || new File("src" + RECURSOS + nombre).isFile();
    }
}
