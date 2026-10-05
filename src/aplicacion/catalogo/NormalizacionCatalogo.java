package aplicacion.catalogo;

import model.entity.Producto;
import model.entity.Usuario;
import model.repository.IProductoRepository;
import model.repository.IUsuarioRepository;

/**
 * Pone al día los productos guardados con reglas anteriores. Se ejecuta una
 * vez al arrancar, en segundo plano.
 *
 * <p>Dos arreglos, ambos sin efecto sobre un producto que ya cumple:</p>
 * <ul>
 *   <li><b>Rutas locales de imagen.</b> Versiones anteriores guardaban la ruta
 *       absoluta del archivo elegido. Si el archivo existe en este equipo se
 *       copia al almacén de imágenes; si no, pero su nombre es el de una
 *       ilustración incluida, se guarda solo el nombre; y si tampoco, el
 *       producto se queda sin imagen (muestra su inicial). Ninguna ruta del
 *       disco queda en la base.</li>
 *   <li><b>Productos sin marca.</b> Los publicados antes del nombre de empresa
 *       toman el de su proveedor, si este ya lo tiene.</li>
 * </ul>
 *
 * <p>Es idempotente: una segunda ejecución no encuentra nada que cambiar. Por
 * eso puede correr en cada arranque sin necesidad de recordar si ya corrió.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class NormalizacionCatalogo {

    private final IProductoRepository productos;
    private final IUsuarioRepository usuarios;
    private final ImportadorImagen importador;

    /**
     * @param productos  catálogo a revisar
     * @param usuarios   cuentas, de donde sale la marca de cada proveedor
     * @param importador resuelve las rutas locales
     */
    public NormalizacionCatalogo(IProductoRepository productos, IUsuarioRepository usuarios,
                                 ImportadorImagen importador) {
        this.productos = productos;
        this.usuarios = usuarios;
        this.importador = importador;
    }

    /**
     * Revisa todo el catálogo.
     *
     * @return cuántos productos se corrigieron
     */
    public int ejecutar() {
        int corregidos = 0;
        for (Producto producto : productos.listarTodos()) {
            boolean cambio = false;
            if (importador.esRutaLocal(producto.getImagen())) {
                producto.setImagen(importador.resolverTolerante(producto.getImagen()));
                cambio = true;
            }
            if (producto.getMarca().isEmpty()) {
                Usuario duenno = usuarios.buscarPorCorreo(producto.getCorreoProveedor());
                if (duenno != null && !duenno.getNombreEmpresa().isEmpty()) {
                    producto.setMarca(duenno.getNombreEmpresa());
                    cambio = true;
                }
            }
            if (cambio) {
                productos.guardar(producto);
                corregidos++;
            }
        }
        return corregidos;
    }
}
