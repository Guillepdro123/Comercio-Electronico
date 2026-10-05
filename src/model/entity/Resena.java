package model.entity;

import java.time.LocalDateTime;

/**
 * Entidad del dominio: la opinión de un comprador sobre un producto, con su
 * calificación de 1 a 5 estrellas y un comentario opcional.
 *
 * <p><b>La calificación se valida aquí.</b> Una reseña de 0 o de 7 estrellas
 * no existe en el dominio, sea cual sea la pantalla que la cree; por eso el
 * constructor la rechaza en vez de confiar en que el formulario lo haya
 * impedido.</p>
 *
 * <p>Se identifica por el par producto + autor: una persona tiene como mucho
 * una reseña por producto, y si vuelve a opinar, la nueva reemplaza a la
 * anterior.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class Resena {

    /** Calificaciones posibles. */
    public static final int MINIMO_ESTRELLAS = 1;
    public static final int MAXIMO_ESTRELLAS = 5;

    private final String idProducto;
    private final String correoAutor;
    private final String nombreAutor;
    private final int estrellas;
    private final String comentario;
    private final LocalDateTime fecha;

    /**
     * @param idProducto  producto reseñado
     * @param correoAutor quien opina (con él se reconoce su reseña anterior)
     * @param nombreAutor nombre que se muestra junto al comentario
     * @param estrellas   calificación, de {@value #MINIMO_ESTRELLAS} a
     *                    {@value #MAXIMO_ESTRELLAS}
     * @param comentario  texto libre; puede ir vacío
     * @param fecha       cuándo se escribió
     * @throws IllegalArgumentException si la calificación está fuera de rango
     */
    public Resena(String idProducto, String correoAutor, String nombreAutor,
                  int estrellas, String comentario, LocalDateTime fecha) {
        if (estrellas < MINIMO_ESTRELLAS || estrellas > MAXIMO_ESTRELLAS) {
            throw new IllegalArgumentException("La calificación va de "
                    + MINIMO_ESTRELLAS + " a " + MAXIMO_ESTRELLAS + " estrellas.");
        }
        this.idProducto = idProducto;
        this.correoAutor = correoAutor;
        this.nombreAutor = nombreAutor;
        this.estrellas = estrellas;
        this.comentario = comentario == null ? "" : comentario;
        this.fecha = fecha;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public String getCorreoAutor() {
        return correoAutor;
    }

    public String getNombreAutor() {
        return nombreAutor;
    }

    public int getEstrellas() {
        return estrellas;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
