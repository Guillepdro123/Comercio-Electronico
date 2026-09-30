package view.dashboard;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Contrato que {@link controller.ProveedorController} necesita del panel del
 * Proveedor.
 *
 * <p><b>Segregación de Interfaces (I de SOLID):</b> igual que
 * {@link IClienteDashboardView}, declara solo lo que el controlador usa. Son
 * dos contratos distintos y no una interfaz común de "dashboard" porque los
 * dos roles no comparten ni una sola operación: el Cliente compra, el
 * Proveedor publica.</p>
 *
 * <p><b>Los eventos se registran como acciones, no como componentes:</b> la
 * vista recibe qué ejecutar en cada caso y el controlador nunca toca un
 * componente Swing.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public interface IProveedorDashboardView {

    /**
     * Pinta las tarjetas de indicadores de la parte superior.
     *
     * @param indicadores cifras ya calculadas y formateadas
     */
    void mostrarIndicadores(IndicadorVentas indicadores);

    /**
     * Llena la tabla de gestión con los productos del proveedor.
     *
     * @param productos productos publicados por él
     */
    void mostrarProductos(List<FilaProducto> productos);

    /**
     * Pinta el gráfico de rendimiento.
     *
     * @param titulo       qué representan las barras en esta llamada, porque
     *                     el panel muestra ingresos cuando ya hubo ventas y el
     *                     inventario disponible mientras no las haya: el
     *                     título tiene que decir siempre cuál de las dos es
     * @param barras       datos a dibujar, del mayor al menor
     * @param mensajeVacio qué mostrar si no hay ni una barra
     */
    void mostrarGrafico(String titulo, List<BarraVentas> barras, String mensajeVacio);

    /**
     * Entrega las categorías disponibles para el formulario de producto.
     *
     * @param categorias etiquetas legibles
     */
    void mostrarCategorias(List<String> categorias);

    /**
     * Informa de algo que impide continuar (un dato inválido, un producto que
     * ya no existe).
     *
     * @param mensaje texto a mostrar
     */
    void mostrarAviso(String mensaje);

    /**
     * Confirma una operación completada.
     *
     * @param mensaje resumen para el usuario
     */
    void mostrarExito(String mensaje);

    /** Cierra la ventana del panel (al cerrar sesión). */
    void cerrarVentana();

    /**
     * @param accion qué hacer al guardar un producto nuevo
     */
    void alCrearProducto(Consumer<DatosProducto> accion);

    /**
     * @param accion qué hacer al guardar los cambios de uno existente: recibe
     *               el id del producto y los datos capturados
     */
    void alActualizarProducto(BiConsumer<String, DatosProducto> accion);

    /**
     * @param accion qué hacer al eliminar un producto (la vista ya pidió
     *               confirmación: borrar es irreversible y preguntar es
     *               presentación, no regla de negocio)
     */
    void alEliminarProducto(Consumer<String> accion);
}
