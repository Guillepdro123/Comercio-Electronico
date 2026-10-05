package model.repository.mongo.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import model.entity.LineaPedido;
import model.entity.Pedido;

/**
 * Adaptador entre la entidad {@link Pedido} y los documentos de la colección
 * {@code compras}.
 *
 * <p>Se llama como la colección y no como la entidad porque lo que adapta es
 * el formato de ese documento: en el dominio es un {@code Pedido}, en la base
 * es una compra.</p>
 *
 * <p><b>Los renglones van anidados</b> dentro del propio documento, no en otra
 * colección: un pedido se lee siempre entero y sus renglones no existen sin
 * él. Cada renglón guarda el nombre y el precio del momento de la compra; una
 * rebaja posterior no debe reescribir el historial.</p>
 *
 * <p><b>Fechas.</b> BSON guarda instantes en UTC y no conoce
 * {@link LocalDateTime}, así que la conversión pasa por la zona horaria del
 * equipo en los dos sentidos.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class CompraAdapter implements AdaptadorDocumento<Pedido> {

    /** Identificador de MongoDB. */
    public static final String CAMPO_ID = "_id";
    /** Filtro de "mis compras". */
    public static final String CAMPO_CORREO_USUARIO = "correoUsuario";
    /** Orden del historial, de la más reciente a la más antigua. */
    public static final String CAMPO_FECHA = "fecha";

    private static final String CAMPO_NOMBRE_USUARIO = "nombreUsuario";
    private static final String CAMPO_DIRECCION = "direccionEntrega";
    private static final String CAMPO_DOCUMENTO = "documentoComprador";
    private static final String CAMPO_TOTAL = "total";
    private static final String CAMPO_LINEAS = "lineas";
    private static final String CAMPO_ID_PRODUCTO = "idProducto";
    private static final String CAMPO_NOMBRE_PRODUCTO = "nombreProducto";
    private static final String CAMPO_PRECIO_UNITARIO = "precioUnitario";
    private static final String CAMPO_CANTIDAD = "cantidad";

    /**
     * El total se guarda aunque se pueda recalcular: así se puede leer o
     * sumar desde Atlas sin recorrer los renglones.
     */
    @Override
    public Document aDocumento(Pedido pedido) {
        List<Document> lineas = new ArrayList<>();
        for (LineaPedido linea : pedido.getLineas()) {
            lineas.add(new Document(CAMPO_ID_PRODUCTO, linea.getIdProducto())
                    .append(CAMPO_NOMBRE_PRODUCTO, linea.getNombreProducto())
                    .append(CAMPO_PRECIO_UNITARIO, linea.getPrecioUnitario())
                    .append(CAMPO_CANTIDAD, linea.getCantidad()));
        }
        Document documento = new Document(CAMPO_CORREO_USUARIO, pedido.getCorreoUsuario())
                .append(CAMPO_NOMBRE_USUARIO, pedido.getNombreUsuario())
                .append(CAMPO_DIRECCION, pedido.getDireccionEntrega())
                .append(CAMPO_TOTAL, pedido.getTotal())
                .append(CAMPO_FECHA, aFecha(pedido.getFecha()))
                .append(CAMPO_LINEAS, lineas);
        // Campo añadido después: solo se escribe si hay dato, así un pedido
        // sin él sigue produciendo exactamente el documento de antes.
        if (!pedido.getDocumentoComprador().isEmpty()) {
            documento.append(CAMPO_DOCUMENTO, pedido.getDocumentoComprador());
        }
        return documento;
    }

    @Override
    public Pedido aEntidad(Document documento) {
        List<LineaPedido> lineas = new ArrayList<>();
        for (Document linea : documento.getList(CAMPO_LINEAS, Document.class, new ArrayList<>())) {
            lineas.add(new LineaPedido(
                    linea.getString(CAMPO_ID_PRODUCTO),
                    linea.getString(CAMPO_NOMBRE_PRODUCTO),
                    linea.get(CAMPO_PRECIO_UNITARIO, Number.class).doubleValue(),
                    linea.get(CAMPO_CANTIDAD, Number.class).intValue()));
        }

        Pedido pedido = new Pedido(
                documento.getObjectId(CAMPO_ID).toHexString(),
                documento.getString(CAMPO_CORREO_USUARIO),
                documento.getString(CAMPO_NOMBRE_USUARIO),
                lineas,
                documento.getString(CAMPO_DIRECCION));
        // El constructor sella la hora actual porque su caso normal es una
        // compra recién hecha; al releer hay que devolverle la suya.
        pedido.setFecha(aLocalDateTime(documento.getDate(CAMPO_FECHA)));
        pedido.setDocumentoComprador(documento.getString(CAMPO_DOCUMENTO));
        return pedido;
    }

    private Date aFecha(LocalDateTime fecha) {
        return Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime aLocalDateTime(Date fecha) {
        if (fecha == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofInstant(fecha.toInstant(), ZoneId.systemDefault());
    }
}
