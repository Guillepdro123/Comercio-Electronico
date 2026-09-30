package view.dashboard;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Contrato que {@link controller.ClienteController} necesita del panel del
 * Cliente.
 *
 * <p><b>Segregación de Interfaces (I de SOLID):</b> igual que
 * {@code ILoginView} o {@code IRegistroUsuarioView}, declara solo lo que el
 * controlador realmente usa: cosas que mostrar y avisos de que el usuario
 * hizo algo.</p>
 *
 * <p><b>Los eventos se registran como acciones, no como componentes.</b> El
 * formulario de Login le pasa su {@code JButton} al controlador porque hay
 * uno solo; aquí las interacciones son muchas y de distinta forma (escribir
 * en el buscador, elegir categoría, agregar con una cantidad), así que la
 * vista recibe qué ejecutar en cada caso y el controlador nunca toca un
 * componente Swing.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public interface IClienteDashboardView {

    /** Categoría que representa "todas" en el filtro. */
    String CATEGORIA_TODAS = "Todas";

    /**
     * Pinta las tarjetas del catálogo.
     *
     * @param productos productos a mostrar, ya filtrados por el controlador
     */
    void mostrarProductos(List<TarjetaProducto> productos);

    /**
     * Construye el filtro de categorías.
     *
     * @param categorias etiquetas disponibles, incluida {@link #CATEGORIA_TODAS}
     */
    void mostrarCategorias(List<String> categorias);

    /**
     * Refresca el panel del carrito.
     *
     * @param lineas renglones actuales
     * @param total  importe total ya formateado
     */
    void mostrarCarrito(List<LineaCarrito> lineas, String total);

    /**
     * Muestra el historial de compras del usuario.
     *
     * @param compras pedidos anteriores, del más reciente al más antiguo
     */
    void mostrarCompras(List<ResumenCompra> compras);

    /**
     * Informa de algo que impide continuar (sin existencias, carrito vacío).
     *
     * @param mensaje texto a mostrar
     */
    void mostrarAviso(String mensaje);

    /**
     * Confirma una compra completada.
     *
     * @param mensaje resumen para el usuario
     */
    void mostrarExito(String mensaje);

    /** Cierra la ventana del panel (al cerrar sesión). */
    void cerrarVentana();

    /**
     * @param accion qué hacer cuando cambia el texto del buscador
     */
    void alBuscar(Consumer<String> accion);

    /**
     * @param accion qué hacer cuando se elige una categoría
     */
    void alElegirCategoria(Consumer<String> accion);

    /**
     * @param accion qué hacer al agregar al carrito: recibe el id del
     *               producto y la cantidad elegida
     */
    void alAgregarAlCarrito(BiConsumer<String, Integer> accion);

    /**
     * @param accion qué hacer al quitar un renglón del carrito
     */
    void alQuitarDelCarrito(Consumer<String> accion);

    /**
     * @param accion qué hacer al confirmar la compra
     */
    void alConfirmarCompra(Runnable accion);

    /**
     * @param accion qué hacer al abrir "Mis compras"
     */
    void alAbrirMisCompras(Runnable accion);
}
