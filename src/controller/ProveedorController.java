package controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.entity.Categoria;
import model.entity.LineaPedido;
import model.entity.Pedido;
import model.entity.Producto;
import model.entity.Usuario;
import model.repository.IPedidoRepository;
import model.repository.IProductoRepository;
import service.ResumenPedido;
import view.dashboard.BarraVentas;
import view.dashboard.DatosProducto;
import view.dashboard.FilaProducto;
import view.dashboard.IProveedorDashboardView;
import view.dashboard.IndicadorVentas;

/**
 * Controlador de los casos de uso del Proveedor: publicar productos,
 * mantenerlos al día y consultar cómo se están vendiendo.
 *
 * <p>Nace en este incremento por la misma razón que {@link ClienteController}:
 * hasta el Incremento 2 el panel del Proveedor era una pantalla de aterrizaje
 * sin nada que coordinar, y por eso no se le inventó un controlador vacío.</p>
 *
 * <p><b>Inversión de dependencias:</b> depende de {@link IProductoRepository},
 * {@link IPedidoRepository} e {@link IProveedorDashboardView} — abstracciones.
 * El mismo controlador sirve con los repositorios en memoria de hoy y con
 * MongoDB Atlas mañana.</p>
 *
 * <p><b>Aquí vive la validación del formulario</b> (que el precio sea un
 * número, que el descuento esté entre 0 y 100, que el stock no sea negativo).
 * La vista captura texto y este controlador decide si ese texto es un
 * producto, igual que {@link UsuarioController} decide si unos campos son un
 * usuario.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class ProveedorController {

    private static final int DESCUENTO_MAXIMO = 100;
    /** Tope de barras del gráfico: más allá deja de leerse de un vistazo. */
    private static final int BARRAS_VISIBLES = 6;

    private final IProveedorDashboardView vista;
    private final IProductoRepository productos;
    private final IPedidoRepository pedidos;
    private final Usuario proveedor;

    /**
     * Conecta la vista con los repositorios y deja registradas las acciones.
     *
     * @param vista     panel del Proveedor (abstracción)
     * @param productos catálogo (abstracción)
     * @param pedidos   almacén de órdenes, de donde salen las ventas
     * @param proveedor proveedor autenticado; su correo es el dueño de todo lo
     *                  que publique
     */
    public ProveedorController(IProveedorDashboardView vista, IProductoRepository productos,
                               IPedidoRepository pedidos, Usuario proveedor) {
        this.vista = vista;
        this.productos = productos;
        this.pedidos = pedidos;
        this.proveedor = proveedor;

        vista.alCrearProducto(this::crearProducto);
        vista.alActualizarProducto(this::actualizarProducto);
        vista.alEliminarProducto(this::eliminarProducto);
    }

    /** Carga las categorías del formulario, la tabla y los indicadores. */
    public void iniciar() {
        List<String> etiquetas = new ArrayList<>();
        for (Categoria categoria : Categoria.values()) {
            etiquetas.add(categoria.getEtiqueta());
        }
        vista.mostrarCategorias(etiquetas);
        refrescar();
    }

    // ---------------------------------------------------------------------
    // CRUD del catálogo
    // ---------------------------------------------------------------------

    private void crearProducto(DatosProducto datos) {
        String error = validar(datos);
        if (error != null) {
            vista.mostrarAviso(error);
            return;
        }
        Producto producto = new Producto(null, datos.nombre().trim(), datos.descripcion().trim(),
                aNumero(datos.precio()), (int) aNumero(datos.descuento()),
                categoriaDe(datos.categoria()), (int) aNumero(datos.stock()),
                datos.imagen() == null ? "" : datos.imagen().trim(), proveedor.getCorreo());
        productos.guardar(producto);
        refrescar();
        vista.mostrarExito("Publicaste " + producto.getNombre()
                + ". Ya aparece en el catálogo de los clientes.");
    }

    private void actualizarProducto(String id, DatosProducto datos) {
        Producto producto = productos.buscarPorId(id);
        if (producto == null) {
            vista.mostrarAviso("Ese producto ya no existe en el catálogo.");
            refrescar();
            return;
        }
        String error = validar(datos);
        if (error != null) {
            vista.mostrarAviso(error);
            return;
        }
        producto.setNombre(datos.nombre().trim());
        producto.setDescripcion(datos.descripcion().trim());
        producto.setPrecio(aNumero(datos.precio()));
        producto.setPorcentajeDescuento((int) aNumero(datos.descuento()));
        producto.setCategoria(categoriaDe(datos.categoria()));
        producto.setStock((int) aNumero(datos.stock()));
        producto.setImagen(datos.imagen() == null ? "" : datos.imagen().trim());
        productos.guardar(producto);
        refrescar();
        vista.mostrarExito("Actualizaste " + producto.getNombre() + ".");
    }

    private void eliminarProducto(String id) {
        Producto producto = productos.buscarPorId(id);
        if (producto == null || !productos.eliminar(id)) {
            vista.mostrarAviso("Ese producto ya no existe en el catálogo.");
            refrescar();
            return;
        }
        refrescar();
        vista.mostrarExito("Eliminaste " + producto.getNombre() + " del catálogo.");
    }

    // ---------------------------------------------------------------------
    // Validación del formulario
    // ---------------------------------------------------------------------

    /**
     * Comprueba lo que se capturó en el formulario.
     *
     * @param datos texto tal como lo escribió el proveedor
     * @return mensaje de error, o {@code null} si todo está bien — mismo
     *         patrón que {@link PoliticaPassword#validar(String)}
     */
    private String validar(DatosProducto datos) {
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
     *         ninguna de las comprobaciones acepta, así que un texto inválido
     *         cae en el mismo mensaje que un valor fuera de rango
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

    // ---------------------------------------------------------------------
    // Tabla e indicadores
    // ---------------------------------------------------------------------

    /** Vuelve a pintar la tabla y las cifras tras cualquier cambio. */
    private void refrescar() {
        List<Producto> mios = productos.listarPorProveedor(proveedor.getCorreo());

        List<FilaProducto> filas = new ArrayList<>();
        int agotados = 0;
        for (Producto producto : mios) {
            filas.add(aFila(producto));
            if (!producto.hayExistencias()) {
                agotados++;
            }
        }
        vista.mostrarProductos(filas);
        vista.mostrarIndicadores(calcularIndicadores(mios, filas.size(), agotados));
        mostrarRendimiento(mios);
    }

    /**
     * Arma el gráfico del panel.
     *
     * <p><b>Dos series, y el título dice siempre cuál se está viendo.</b> Lo
     * que interesa es el rendimiento de ventas, pero un proveedor que acaba de
     * publicar su catálogo todavía no tiene ninguna: enseñarle un recuadro
     * vacío no le dice nada. Mientras no haya ventas se grafica su inventario,
     * que sí tiene datos desde el primer producto, y el título lo anuncia.
     * Cambiar de serie sin decirlo sería engañoso; decirlo lo vuelve honesto.</p>
     *
     * @param mios productos del proveedor
     */
    private void mostrarRendimiento(List<Producto> mios) {
        Map<String, Double> ingresosPorProducto = ingresosPorProducto(mios);
        boolean hayVentas = ingresosPorProducto.values().stream().anyMatch(valor -> valor > 0);

        List<BarraVentas> barras = new ArrayList<>();
        if (hayVentas) {
            for (Producto producto : mios) {
                double ingreso = ingresosPorProducto.getOrDefault(producto.getId(), 0.0);
                if (ingreso > 0) {
                    barras.add(new BarraVentas(producto.getNombre(), ingreso,
                            ResumenPedido.moneda(ingreso)));
                }
            }
            barras.sort(Comparator.comparingDouble(BarraVentas::valor).reversed());
            vista.mostrarGrafico("Ingresos por producto", recortar(barras),
                    "Todavía no has vendido nada.");
            return;
        }

        for (Producto producto : mios) {
            barras.add(new BarraVentas(producto.getNombre(), producto.getStock(),
                    producto.getStock() + " u."));
        }
        barras.sort(Comparator.comparingDouble(BarraVentas::valor).reversed());
        vista.mostrarGrafico("Inventario disponible", recortar(barras),
                "Publica tu primer producto para ver aquí tu inventario.");
    }

    /** @return las primeras barras; con demasiadas, el gráfico deja de leerse de un vistazo */
    private List<BarraVentas> recortar(List<BarraVentas> barras) {
        return barras.subList(0, Math.min(BARRAS_VISIBLES, barras.size()));
    }

    /** @return cuánto ha facturado cada producto del proveedor, por id */
    private Map<String, Double> ingresosPorProducto(List<Producto> mios) {
        Map<String, Double> ingresos = new LinkedHashMap<>();
        for (Producto producto : mios) {
            ingresos.put(producto.getId(), 0.0);
        }
        for (Pedido pedido : pedidos.listarTodos()) {
            for (LineaPedido linea : pedido.getLineas()) {
                if (ingresos.containsKey(linea.getIdProducto())) {
                    ingresos.merge(linea.getIdProducto(), linea.getSubtotal(), Double::sum);
                }
            }
        }
        return ingresos;
    }

    private FilaProducto aFila(Producto producto) {
        return new FilaProducto(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoria().getEtiqueta(),
                String.format("%.0f", producto.getPrecio()),
                ResumenPedido.moneda(producto.getPrecioFinal()),
                producto.getPorcentajeDescuento(),
                producto.getStock(),
                producto.getImagen());
    }

    /**
     * Recorre los pedidos y suma solo los renglones de productos propios.
     *
     * <p>Un pedido puede mezclar artículos de varios proveedores, así que no
     * se toma su total: se suman renglón por renglón los que le pertenecen a
     * este. El pedido cuenta como venta suya si aportó al menos uno.</p>
     *
     * @param mios       productos publicados por el proveedor
     * @param publicados cuántos son
     * @param agotados   cuántos se quedaron sin existencias
     * @return las cifras ya formateadas para la vista
     */
    private IndicadorVentas calcularIndicadores(List<Producto> mios, int publicados, int agotados) {
        List<String> idsPropios = new ArrayList<>();
        for (Producto producto : mios) {
            idsPropios.add(producto.getId());
        }

        double ingresos = 0;
        int unidades = 0;
        int pedidosConVenta = 0;
        for (Pedido pedido : pedidos.listarTodos()) {
            boolean aporta = false;
            for (LineaPedido linea : pedido.getLineas()) {
                if (idsPropios.contains(linea.getIdProducto())) {
                    ingresos += linea.getSubtotal();
                    unidades += linea.getCantidad();
                    aporta = true;
                }
            }
            if (aporta) {
                pedidosConVenta++;
            }
        }
        return new IndicadorVentas(ResumenPedido.moneda(ingresos), unidades,
                pedidosConVenta, publicados, agotados);
    }
}
