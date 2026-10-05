package model.repository.mongo.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.bson.Document;
import model.entity.Resena;

/**
 * Adaptador entre la entidad {@link Resena} y los documentos de la colección
 * {@code resenas}.
 *
 * <p>Mismo trato de fechas que {@link CompraAdapter}: BSON guarda instantes y
 * la conversión pasa por la zona horaria del equipo.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class ResenaAdapter implements AdaptadorDocumento<Resena> {

    /** Producto reseñado; filtro de la ficha y clave de agrupación del resumen. */
    public static final String CAMPO_ID_PRODUCTO = "idProducto";
    /** Autor; junto con el producto, identifica la reseña. */
    public static final String CAMPO_CORREO_AUTOR = "correoAutor";
    /** Calificación; el resumen la promedia. */
    public static final String CAMPO_ESTRELLAS = "estrellas";
    /** Orden de la lista, de la más reciente a la más antigua. */
    public static final String CAMPO_FECHA = "fecha";

    private static final String CAMPO_NOMBRE_AUTOR = "nombreAutor";
    private static final String CAMPO_COMENTARIO = "comentario";

    @Override
    public Document aDocumento(Resena resena) {
        return new Document(CAMPO_ID_PRODUCTO, resena.getIdProducto())
                .append(CAMPO_CORREO_AUTOR, resena.getCorreoAutor())
                .append(CAMPO_NOMBRE_AUTOR, resena.getNombreAutor())
                .append(CAMPO_ESTRELLAS, resena.getEstrellas())
                .append(CAMPO_COMENTARIO, resena.getComentario())
                .append(CAMPO_FECHA, Date.from(
                        resena.getFecha().atZone(ZoneId.systemDefault()).toInstant()));
    }

    @Override
    public Resena aEntidad(Document documento) {
        Date fecha = documento.getDate(CAMPO_FECHA);
        return new Resena(
                documento.getString(CAMPO_ID_PRODUCTO),
                documento.getString(CAMPO_CORREO_AUTOR),
                documento.getString(CAMPO_NOMBRE_AUTOR),
                documento.get(CAMPO_ESTRELLAS, Number.class).intValue(),
                documento.getString(CAMPO_COMENTARIO),
                fecha == null ? LocalDateTime.now()
                        : LocalDateTime.ofInstant(fecha.toInstant(), ZoneId.systemDefault()));
    }
}
