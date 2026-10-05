package observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Sujeto observable del catálogo (rol <em>Subject</em> del patrón Observer).
 *
 * <p><b>Qué resuelve.</b> Quien cambia el catálogo (el Proveedor) y quien lo
 * muestra (el Cliente) no se conocen entre sí. El que cambia solo llama a
 * {@link #notificarObservadores()}; el que muestra se suscribió antes con
 * {@link #agregarObservador(CatalogoObserver)}. Así se puede añadir otra
 * pantalla que dependa del catálogo sin tocar a quien lo modifica: basta con
 * suscribirla.</p>
 *
 * <p><b>Dos fuentes de aviso, un solo sujeto.</b> Avisa
 * {@code controller.ProveedorController} tras publicar, editar o eliminar en
 * este mismo programa, y avisa
 * {@code model.repository.mongo.VigilanteCatalogoMongo} cuando el cambio llega
 * de otro equipo conectado al mismo clúster. Hace falta la segunda porque en
 * un mismo programa el panel del Proveedor y el del Cliente nunca están
 * abiertos a la vez: la sincronización que de verdad ocurre es entre equipos.
 * Los observadores no distinguen de dónde vino el aviso.</p>
 *
 * <p><b>Por qué {@link CopyOnWriteArrayList}.</b> Las suscripciones ocurren en
 * el hilo de eventos (al abrir y cerrar ventanas) y los avisos pueden llegar
 * desde el hilo que vigila MongoDB. Esta lista permite recorrerla mientras
 * otro hilo la modifica sin {@code ConcurrentModificationException} ni
 * candados: cada recorrido trabaja sobre una copia. Copiar en cada alta o
 * baja es caro en general, pero aquí hay uno o dos observadores y cambian muy
 * rara vez; es el caso para el que existe.</p>
 *
 * <p>No es un Singleton: se crea una vez en {@code app.Main} y se pasa a
 * quien lo necesite, como los repositorios.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class CatalogoSubject {

    private final List<CatalogoObserver> observadores = new CopyOnWriteArrayList<>();

    /**
     * Suscribe un observador. Suscribirlo dos veces no duplica los avisos.
     *
     * @param observador quien quiere enterarse de los cambios
     */
    public void agregarObservador(CatalogoObserver observador) {
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    /**
     * Da de baja un observador. Hay que llamarlo al cerrar la ventana que
     * observa: si no, la lista la mantendría viva en memoria y le seguiría
     * avisando a una pantalla que ya no existe.
     *
     * @param observador quien deja de observar
     */
    public void removerObservador(CatalogoObserver observador) {
        observadores.remove(observador);
    }

    /** Avisa a todos los observadores suscritos de que el catálogo cambió. */
    public void notificarObservadores() {
        for (CatalogoObserver observador : observadores) {
            observador.actualizarCatalogo();
        }
    }

    /** @return cuántos observadores hay suscritos ahora mismo */
    public int cantidadObservadores() {
        return observadores.size();
    }
}
