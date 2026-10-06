package controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import aplicacion.catalogo.CatalogoService;
import aplicacion.catalogo.ReporteVentas;
import aplicacion.catalogo.ReporteVentasService;
import aplicacion.catalogo.ResultadoProducto;
import aplicacion.catalogo.SolicitudProducto;
import model.entity.Categoria;
import model.entity.Producto;
import model.entity.Usuario;
import service.correo.ResumenPedido;
import view.dashboard.proveedor.BarraVentas;
import view.dashboard.proveedor.DatosProducto;
import view.dashboard.proveedor.FilaProducto;
import view.dashboard.proveedor.IProveedorDashboardView;
import view.dashboard.proveedor.IndicadorVentas;

/**
 * Controlador del panel del Proveedor: recoge lo que se pulsa en la pantalla,
 * llama al caso de uso que corresponde y vuelve a pintar el resultado.
 *
 * <p><b>Aquí no hay reglas de negocio.</b> Validar un producto, guardarlo,
 * avisar al catálogo y calcular las cifras de venta son de
 * {@link CatalogoService} y {@link ReporteVentasService}. Lo que queda en esta
 * clase es traducción: pasar de los registros de la vista
 * ({@link DatosProducto}) a los de la aplicación ({@link SolicitudProducto}) y
 * al revés, dar formato a los importes y decidir qué serie muestra el
 * gráfico.</p>
 *
 * <p><b>La vista no conoce el modelo:</b> recibe {@link FilaProducto},
 * {@link IndicadorVentas} y {@link BarraVentas}, con los importes ya
 * formateados.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 2.0
 */
public class ProveedorController {

    /** Tope de barras del gráfico: más allá deja de leerse de un vistazo. */
    private static final int BARRAS_VISIBLES = 6;

    private final IProveedorDashboardView vista;
    private final CatalogoService catalogo;
    private final ReporteVentasService reportes;
    private final Usuario proveedor;

    /**
     * Conecta la vista con los casos de uso y deja registradas las acciones.
     *
     * @param vista     panel del Proveedor (abstracción)
     * @param catalogo  caso de uso de publicar, editar y eliminar productos
     * @param reportes  caso de uso de las cifras de venta
     * @param proveedor proveedor autenticado; su correo es el dueño de todo lo
     *                  que publique
     */
    public ProveedorController(IProveedorDashboardView vista, CatalogoService catalogo,
                               ReporteVentasService reportes, Usuario proveedor) {
        this.vista = vista;
        this.catalogo = catalogo;
        this.reportes = reportes;
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
    // Acciones del formulario
    // ---------------------------------------------------------------------

    private void crearProducto(DatosProducto datos) {
        // Validar y guardar viajan juntos al servicio: con MongoDB Atlas
        // detrás, guardar es un viaje por red y en el hilo que pinta la
        // ventana la congelaría.
        vista.ejecutarEnSegundoPlano("Publicando tu producto...",
                () -> catalogo.publicar(aSolicitud(datos), proveedor.getCorreo(),
                        proveedor.getNombreEmpresa()),
                resultado -> contar(resultado, "Publicaste %s."
                        + " Ya aparece en el catálogo de los clientes."));
    }

    private void actualizarProducto(String id, DatosProducto datos) {
        vista.ejecutarEnSegundoPlano("Guardando los cambios...",
                () -> catalogo.actualizar(id, aSolicitud(datos)),
                resultado -> contar(resultado, "Actualizaste %s."));
    }

    private void eliminarProducto(String id) {
        vista.ejecutarEnSegundoPlano("Eliminando el producto...",
                () -> catalogo.eliminar(id),
                resultado -> contar(resultado, "Eliminaste %s del catálogo."));
    }

    /**
     * Muestra cómo salió la operación y deja la pantalla al día.
     *
     * <p>Se refresca también cuando hubo error: el motivo más común es que
     * otra sesión borró el producto, y entonces la tabla está enseñando una
     * fila que ya no existe.</p>
     *
     * @param resultado      lo que devolvió el caso de uso
     * @param plantillaExito mensaje de éxito, con un {@code %s} para el nombre
     */
    private void contar(ResultadoProducto resultado, String plantillaExito) {
        refrescar();
        if (!resultado.exitoso()) {
            vista.mostrarAviso(resultado.error());
            return;
        }
        vista.mostrarExito(String.format(plantillaExito, resultado.producto().getNombre()));
    }

    // ---------------------------------------------------------------------
    // Pintado de la tabla, las cifras y el gráfico
    // ---------------------------------------------------------------------

    /** Vuelve a pintar la tabla y las cifras tras cualquier cambio. */
    private void refrescar() {
        ReporteVentas reporte = reportes.generar(proveedor.getCorreo());

        List<FilaProducto> filas = new ArrayList<>();
        for (Producto producto : reporte.productos()) {
            filas.add(aFila(producto));
        }
        vista.mostrarProductos(filas);
        Producto estrella = reporte.productoEstrella();
        vista.mostrarIndicadores(new IndicadorVentas(
                ResumenPedido.moneda(reporte.ingresos()), reporte.unidadesVendidas(),
                reporte.pedidosConVenta(), reporte.publicados(), reporte.agotados(),
                ResumenPedido.moneda(reporte.ticketPromedio()),
                estrella == null ? "" : estrella.getNombre()));
        mostrarRendimiento(reporte);
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
     * <p>Elegir qué serie se muestra es una decisión de pantalla, y por eso se
     * queda aquí: el servicio entrega las cifras de las dos.</p>
     */
    private void mostrarRendimiento(ReporteVentas reporte) {
        List<BarraVentas> barras = new ArrayList<>();
        if (reporte.hayVentas()) {
            for (Producto producto : reporte.productos()) {
                double ingreso = reporte.ingresosPorProducto().getOrDefault(producto.getId(), 0.0);
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

        for (Producto producto : reporte.productos()) {
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

    // ---------------------------------------------------------------------
    // Traducción entre la vista y la capa de aplicación
    // ---------------------------------------------------------------------

    /** Pasa el registro de la vista al de la aplicación, sin convertir nada. */
    private SolicitudProducto aSolicitud(DatosProducto datos) {
        return new SolicitudProducto(datos.nombre(), datos.descripcion(), datos.precio(),
                datos.descuento(), datos.categoria(), datos.stock(), datos.imagen());
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
}
