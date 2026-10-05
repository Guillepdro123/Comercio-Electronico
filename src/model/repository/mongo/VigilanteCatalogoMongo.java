package model.repository.mongo;

import com.mongodb.client.MongoChangeStreamCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import observer.CatalogoSubject;
import org.bson.Document;

/**
 * Vigila la colección {@code productos} de MongoDB Atlas y avisa al
 * {@link CatalogoSubject} cada vez que cambia.
 *
 * <p><b>Por qué existe.</b> El aviso que da el Proveedor en su propio programa
 * no llega a otro equipo: dos instancias de la aplicación no comparten
 * memoria. Lo que sí comparten es la base de datos. MongoDB ofrece para esto
 * los <em>Change Streams</em>: un cursor que, en vez de terminar, se queda
 * esperando y entrega un evento por cada inserción, modificación o borrado.
 * Esta clase convierte cada evento en un {@code notificarObservadores()}, de
 * modo que el Cliente de otro equipo recibe exactamente el mismo aviso que
 * recibiría del Proveedor local.</p>
 *
 * <p><b>Hilo propio y demonio.</b> El cursor bloquea mientras espera, así que
 * no puede correr en el hilo de eventos (congelaría la ventana). Es un hilo
 * demonio para que no impida al programa terminar al cerrar la última
 * ventana.</p>
 *
 * <p><b>Si falla, se degrada.</b> Un clúster que no admita Change Streams o
 * una caída de red que el driver no pueda reanudar detienen la vigilancia y
 * lo dicen por consola; el programa sigue, y los avisos locales del Proveedor
 * siguen funcionando. Comprobado: el clúster del proyecto sí los admite.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class VigilanteCatalogoMongo {

    private final MongoDatabase base;
    private final CatalogoSubject catalogo;

    /**
     * @param base     base de datos que contiene la colección {@code productos}
     * @param catalogo sujeto al que se reenvía cada cambio
     */
    public VigilanteCatalogoMongo(MongoDatabase base, CatalogoSubject catalogo) {
        this.base = base;
        this.catalogo = catalogo;
    }

    /** Arranca la vigilancia en segundo plano y vuelve de inmediato. */
    public void iniciar() {
        Thread hilo = new Thread(this::vigilar, "vigilante-catalogo");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void vigilar() {
        try (MongoChangeStreamCursor<ChangeStreamDocument<Document>> cursor =
                     base.getCollection(ProductoRepositoryMongo.COLECCION).watch().cursor()) {
            while (true) {
                // Bloquea hasta el siguiente cambio. El contenido del evento no
                // se usa: el aviso solo dice "cambió" y cada observador recarga
                // lo suyo (ver CatalogoObserver).
                cursor.next();
                catalogo.notificarObservadores();
            }
        } catch (RuntimeException ex) {
            System.out.println("Vigilancia del catálogo detenida: " + ex.getMessage());
        }
    }
}
