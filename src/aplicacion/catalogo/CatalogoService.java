package aplicacion.catalogo;

import java.util.List;
import model.entity.Categoria;
import model.entity.Producto;
import model.repository.IProductoRepository;
import observer.CatalogoSubject;

/**
 * Casos de uso del catálogo del proveedor: publicar, actualizar y eliminar
 * productos, con su validación.
 *
 * <p><b>Qué hace esta clase aquí.</b> Estas reglas vivían dentro de
 * {@code ProveedorController}, mezcladas con el refresco de la tabla: qué es
 * un precio válido, qué hacer si el producto ya no existe y a quién avisar
 * del cambio. Al sacarlas se pueden probar sin abrir ventanas y las puede
 * reutilizar otro punto de entrada (por ejemplo, una carga masiva de
 * inventario).</p>
 *
 * <p><b>Avisar del cambio es parte del caso de uso.</b> Publicar un producto
 * no termina al guardarlo: el catálogo que están viendo los clientes quedó
 * desactualizado. Por eso el servicio avisa a {@link CatalogoSubject} y no lo
 * hace el controlador; así cualquiera que publique productos dispara el aviso,
 * no solo esta pantalla. Los observadores reciben el aviso en el hilo desde el
 * que se llamó, y cada uno se encarga de saltar al suyo si pinta en pantalla.</p>
 *
 * <p><b>Bloquea</b> mientras habla con la base: quien lo llame desde una
 * interfaz gráfica debe hacerlo fuera del hilo de eventos.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class CatalogoService {

    private static final int DESCUENTO_MAXIMO = 100;

    private final IProductoRepository productos;
    private final CatalogoSubject catalogo;
    private final ImportadorImagen importador;

    /**
     * @param productos  catálogo (abstracción)
     * @param catalogo   sujeto al que se avisa de cada cambio
     * @param importador convierte la imagen elegida en una referencia
     *                   portable; ver {@link ImportadorImagen}
     */
    public CatalogoService(IProductoRepository productos, CatalogoSubject catalogo,
                           ImportadorImagen importador) {
        this.productos = productos;
        this.catalogo = catalogo;
        this.importador = importador;
    }

    /**
     * Publica un producto nuevo a nombre del proveedor y de su marca.
     *
     * <p>Sin marca no se publica: el comprador tiene que ver qué tienda le
     * vende, y un producto sin vendedor visible no inspira confianza.</p>
     *
     * @param datos           lo que escribió el proveedor, sin validar
     * @param correoProveedor dueño del producto
     * @param marca           nombre de la empresa con la que vende
     * @return el producto publicado, o el error de validación
     */
    public ResultadoProducto publicar(SolicitudProducto datos, String correoProveedor,
                                      String marca) {
        String error = validar(datos);
        if (error == null && (marca == null || marca.isBlank())) {
            error = "Antes de publicar, escribe el nombre de tu empresa en \"Editar perfil\".";
        }
        if (error != null) {
            return ResultadoProducto.fallo(error);
        }
        String imagen;
        try {
            imagen = importador.resolver(datos.imagen());
        } catch (IllegalArgumentException ex) {
            return ResultadoProducto.fallo(ex.getMessage());
        }
        Producto producto = new Producto(null, datos.nombre().trim(), datos.descripcion().trim(),
                aNumero(datos.precio()), (int) aNumero(datos.descuento()),
                categoriaDe(datos.categoria()), (int) aNumero(datos.stock()),
                imagen, correoProveedor);
        producto.setMarca(marca.trim());
        productos.guardar(producto);
        catalogo.notificarObservadores();
        return ResultadoProducto.hecho(producto);
    }

    /**
     * Lleva a todos los productos de un proveedor su correo y su marca
     * actuales (tras editar su perfil).
     *
     * @param correoAnterior correo con el que estaban publicados
     * @param correoNuevo    correo actual de la cuenta
     * @param marca          nombre de empresa actual
     */
    public void actualizarVendedor(String correoAnterior, String correoNuevo, String marca) {
        if (productos.actualizarVendedor(correoAnterior, correoNuevo, marca) > 0) {
            catalogo.notificarObservadores();
        }
    }

    /**
     * Vuelca los datos del formulario sobre un producto ya publicado.
     *
     * <p>La lectura y el guardado van juntos a propósito: son dos viajes
     * seguidos al repositorio por una sola acción del usuario.</p>
     *
     * @param id    identificador del producto
     * @param datos lo que escribió el proveedor, sin validar
     * @return el producto actualizado, o el error (validación, o que otra
     *         sesión lo borró mientras se editaba)
     */
    public ResultadoProducto actualizar(String id, SolicitudProducto datos) {
        String error = validar(datos);
        if (error != null) {
            return ResultadoProducto.fallo(error);
        }
        Producto producto = productos.buscarPorId(id);
        if (producto == null) {
            return ResultadoProducto.fallo("Ese producto ya no existe en el catálogo.");
        }
        String imagen;
        try {
            imagen = importador.resolver(datos.imagen());
        } catch (IllegalArgumentException ex) {
            return ResultadoProducto.fallo(ex.getMessage());
        }
        producto.setNombre(datos.nombre().trim());
        producto.setDescripcion(datos.descripcion().trim());
        producto.setPrecio(aNumero(datos.precio()));
        producto.setPorcentajeDescuento((int) aNumero(datos.descuento()));
        producto.setCategoria(categoriaDe(datos.categoria()));
        producto.setStock((int) aNumero(datos.stock()));
        producto.setImagen(imagen);
        productos.guardar(producto);
        catalogo.notificarObservadores();
        return ResultadoProducto.hecho(producto);
    }

    /**
     * @param id identificador del producto
     * @return el producto eliminado, o el error si ya no existía
     */
    public ResultadoProducto eliminar(String id) {
        Producto producto = productos.buscarPorId(id);
        if (producto == null || !productos.eliminar(id)) {
            return ResultadoProducto.fallo("Ese producto ya no existe en el catálogo.");
        }
        catalogo.notificarObservadores();
        return ResultadoProducto.hecho(producto);
    }

    /**
     * @param correoProveedor dueño de los productos
     * @return lo que ese proveedor tiene publicado
     */
    public List<Producto> catalogoDe(String correoProveedor) {
        return productos.listarPorProveedor(correoProveedor);
    }

    // ---------------------------------------------------------------------
    // Validación
    // ---------------------------------------------------------------------

    /**
     * @param datos texto tal como lo escribió el proveedor
     * @return mensaje de error, o {@code null} si todo está bien — mismo
     *         patrón que {@code PoliticaPassword.validar(String)}
     */
    private String validar(SolicitudProducto datos) {
        if (datos.nombre() == null || datos.nombre().isBlank()) {
            return "El producto necesita un nombre.";
        }
        if (datos.descripcion() == null || datos.descripcion().isBlank()) {
            return "Describe el producto para que el cliente sepa qué está comprando.";
        }
        if (aNumero(datos.precio()) <= 0) {
            return "El precio debe ser un número mayor que cero.";
        }
        double descuento = aNumero(datos.descuento());
        if (descuento < 0 || descuento > DESCUENTO_MAXIMO) {
            return "El descuento debe ser un número entre 0 y 100.";
        }
        if (aNumero(datos.stock()) < 0) {
            return "El stock debe ser un número de cero en adelante.";
        }
        return null;
    }

    /**
     * Convierte a número lo que se escribió en un campo.
     *
     * @param texto contenido del campo
     * @return el valor, o {@code -1} si no es un número — un negativo que
     *         ninguna comprobación acepta, así que un texto inválido cae en el
     *         mismo mensaje que un valor fuera de rango
     */
    private double aNumero(String texto) {
        if (texto == null) {
            return -1;
        }
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private Categoria categoriaDe(String etiqueta) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.getEtiqueta().equals(etiqueta)) {
                return categoria;
            }
        }
        return Categoria.values()[0];
    }
}
