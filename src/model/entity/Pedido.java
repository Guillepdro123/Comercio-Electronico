package model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidad del dominio: una compra confirmada.
 *
 * <p>Asocia al usuario que compró, los renglones comprados, el total y la
 * dirección a la que se entrega. La dirección también se copia aquí en vez de
 * leerse del usuario al mostrar el pedido: si alguien se muda, sus compras
 * anteriores deben seguir diciendo a dónde se enviaron realmente.</p>
 *
 * <p>La lista de renglones se entrega como copia inmodificable
 * ({@link #getLineas()}): un pedido ya registrado no se edita.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class Pedido {

    private String id;
    private final String correoUsuario;
    private final String nombreUsuario;
    private final List<LineaPedido> lineas;
    private final double total;
    private final String direccionEntrega;
    private LocalDateTime fecha;
    /**
     * Cédula del comprador, para la factura. Fuera del constructor por la
     * misma razón que la fecha: los pedidos guardados antes no la tienen.
     */
    private String documentoComprador = "";

    /**
     * @param id               identificador único (lo asigna el repositorio)
     * @param correoUsuario    correo del comprador
     * @param nombreUsuario    nombre del comprador, para el resumen y el correo
     * @param lineas           renglones comprados
     * @param direccionEntrega dirección registrada por el usuario al comprar
     */
    public Pedido(String id, String correoUsuario, String nombreUsuario,
                  List<LineaPedido> lineas, String direccionEntrega) {
        this.id = id;
        this.correoUsuario = correoUsuario;
        this.nombreUsuario = nombreUsuario;
        this.lineas = new ArrayList<>(lineas);
        this.direccionEntrega = direccionEntrega;
        this.fecha = LocalDateTime.now();
        this.total = this.lineas.stream().mapToDouble(LineaPedido::getSubtotal).sum();
    }

    /** @return número de artículos comprados, sumando las cantidades de cada renglón */
    public int getTotalUnidades() {
        return lineas.stream().mapToInt(LineaPedido::getCantidad).sum();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    /** @return renglones del pedido, en solo lectura */
    public List<LineaPedido> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    public double getTotal() {
        return total;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getDocumentoComprador() {
        return documentoComprador;
    }

    public void setDocumentoComprador(String documentoComprador) {
        this.documentoComprador = documentoComprador == null ? "" : documentoComprador;
    }

    /**
     * @param idProducto producto a buscar
     * @return {@code true} si el pedido incluye ese producto; es lo que
     *         acredita a alguien como comprador al dejar una reseña
     */
    public boolean incluyeProducto(String idProducto) {
        return lineas.stream().anyMatch(linea -> linea.getIdProducto().equals(idProducto));
    }

    /**
     * Restituye la fecha original al reconstruir un pedido guardado.
     *
     * <p>Existe por el mismo motivo que {@link #setId(String)}: al releer un
     * pedido de la base de datos hay que devolverle <em>cuándo ocurrió</em>, y
     * el constructor sella la hora actual porque su caso normal es una compra
     * que acaba de suceder. No la uses para nada más: un pedido registrado no
     * cambia de fecha.</p>
     *
     * @param fecha momento en que se registró la compra
     */
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
