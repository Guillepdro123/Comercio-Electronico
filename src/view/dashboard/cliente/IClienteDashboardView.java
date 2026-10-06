package view.dashboard.cliente;

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
     * Llena el banner rotativo de la parte superior del catálogo.
     *
     * @param promociones ofertas a rotar; vacía si no hay ninguna (la vista
     *                    decide qué enseñar entonces)
     */
    void mostrarPromociones(List<Promocion> promociones);

    /**
     * Ofrece una promoción en una ventana emergente al entrar.
     *
     * <p>El controlador decide <em>si</em> se ofrece y <em>cuál</em> (es una
     * regla de la tienda); la vista decide cómo y cuándo aparece en pantalla,
     * por ejemplo esperando a que la ventana ya esté visible.</p>
     *
     * @param promocion oferta a destacar
     */
    void ofrecerPromocion(Promocion promocion);

    /**
     * Abre la ficha de un producto con su sección de reseñas.
     *
     * @param producto producto, ya formateado
     * @param resenas  reseñas y si el usuario puede opinar
     */
    void mostrarDetalle(TarjetaProducto producto, ResumenResenas resenas);

    /**
     * Refresca la sección de reseñas de la ficha abierta, tras publicar una.
     *
     * @param idProducto producto de la ficha; si ya no es la abierta, no se hace nada
     * @param resenas    reseñas actualizadas
     */
    void actualizarResenas(String idProducto, ResumenResenas resenas);

    /**
     * Muestra por qué no se pudo publicar una reseña, sin cerrar la ficha.
     *
     * @param mensaje motivo
     */
    void mostrarErrorResena(String mensaje);

    /**
     * Pide la cédula y la dirección antes de comprar, en un formulario rápido
     * que no se cierra hasta que los datos son válidos o el usuario desiste.
     *
     * @param actuales lo que la cuenta ya tiene
     * @param alEnviar qué hacer con lo escrito, sin validar
     */
    void pedirDatosEnvio(DatosEnvio actuales, Consumer<DatosEnvio> alEnviar);

    /**
     * @param mensaje por qué no se aceptaron los datos de envío
     */
    void mostrarErrorDatosEnvio(String mensaje);

    /** Cierra el formulario de datos de envío (cuando se aceptaron). */
    void cerrarDatosEnvio();

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

    /**
     * Ejecuta un trabajo lento fuera del hilo de eventos y continúa después.
     *
     * <p>Mismo reparto que {@code mostrarTransicion}: el controlador dice
     * <em>qué</em> es lento y qué hacer al terminar; la vista decide <em>cómo</em>
     * se ve la espera. Sin esto, una consulta a MongoDB Atlas se ejecutaría en
     * el hilo que pinta la ventana y la dejaría congelada.</p>
     *
     * @param <T>        lo que el trabajo devuelve al terminar
     * @param mensaje    qué se está haciendo, para el indicador
     * @param tarea      trabajo lento; corre FUERA del hilo de eventos, así que
     *                   no debe tocar componentes Swing
     * @param alTerminar qué hacer con el resultado, ya de vuelta en el hilo de
     *                   eventos, que es el único desde el que se puede pintar
     */
    <T> void ejecutarEnSegundoPlano(String mensaje,
                                    java.util.function.Supplier<T> tarea,
                                    java.util.function.Consumer<T> alTerminar);

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
     * @param accion qué hacer al abrir la ficha de un producto: recibe su id.
     *               La ficha pasa por el controlador porque necesita las
     *               reseñas, que se leen de la base
     */
    void alVerDetalle(Consumer<String> accion);

    /**
     * @param accion qué hacer al publicar una reseña desde la ficha
     */
    void alPublicarResena(Consumer<NuevaResena> accion);

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
     * @param accion qué hacer al cambiar la cantidad de un renglón del
     *               carrito: recibe el id del producto y la cantidad nueva
     *               (sin validar; eso lo hace el controlador contra el stock)
     */
    void alCambiarCantidad(BiConsumer<String, Integer> accion);

    /**
     * @param accion qué hacer al confirmar la compra
     */
    void alConfirmarCompra(Runnable accion);

    /**
     * @param accion qué hacer al abrir "Mis compras"
     */
    void alAbrirMisCompras(Runnable accion);

    /**
     * Registra cómo se recarga el catálogo cuando llega un aviso de cambio.
     *
     * <p>La vista recibe el aviso (es la que observa el catálogo) pero no sabe
     * leer productos: eso lo hace el controlador, con la búsqueda y la
     * categoría que el usuario tenga activas. Por eso el aviso termina aquí.</p>
     *
     * @param accion qué hacer para volver a pedir el catálogo
     */
    void alRecargarCatalogo(Runnable accion);
}
