package observer;

/**
 * Observador del catálogo de productos (rol <em>Observer</em> del patrón).
 *
 * <p>Lo implementa quien necesita enterarse de que el catálogo cambió para
 * volver a pintarlo: hoy, el panel del Cliente. El aviso no dice <em>qué</em>
 * cambió, solo <em>que</em> cambió. Es a propósito: quien observa ya sabe
 * cómo recargar lo que muestra (con su búsqueda y su categoría activas), y un
 * aviso sin datos no obliga al sujeto a conocer el formato de nadie.</p>
 *
 * <p><b>Puede llegar desde cualquier hilo.</b> El sujeto avisa desde donde se
 * produjo el cambio: el hilo de eventos si fue una acción del Proveedor en
 * este mismo programa, o el hilo que vigila MongoDB si el cambio vino de otro
 * equipo. Quien implemente esta interfaz y toque componentes Swing tiene que
 * pasar al hilo de eventos por su cuenta. Este paquete no importa Swing, igual
 * que el modelo.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public interface CatalogoObserver {

    /** El catálogo cambió: un producto se publicó, se editó o se eliminó. */
    void actualizarCatalogo();
}
