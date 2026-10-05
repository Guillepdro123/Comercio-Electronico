package service.correo;

import java.time.format.DateTimeFormatter;
import model.entity.LineaPedido;
import model.entity.Pedido;

/**
 * Contenido de los dos correos de una compra: la confirmación para el cliente
 * y la alerta de venta para la tienda, en HTML y en texto plano.
 *
 * <p><b>Por qué está aparte de los notificadores.</b> El contenido es el mismo
 * lo envíe quien lo envíe: si viviera dentro de {@link NotificadorResend}, la
 * versión local tendría que duplicarlo y las dos podrían separarse con el
 * tiempo. Aquí se escribe una vez y ambas lo reutilizan.</p>
 *
 * <p>La tabla de productos es la misma en los dos correos
 * ({@link #tablaProductos(Pedido)}): el cliente y la tienda tienen que ver
 * exactamente el mismo detalle de lo vendido.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 2.0
 */
public final class ResumenPedido {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ResumenPedido() {
    }

    // ---------------------------------------------------------------------
    // Confirmación para el cliente
    // ---------------------------------------------------------------------

    /** @return asunto del correo de confirmación */
    public static String asuntoConfirmacion(Pedido pedido) {
        return "Confirmación de tu pedido " + pedido.getId();
    }

    /**
     * @param pedido pedido confirmado
     * @return correo de confirmación en HTML, con la tabla detallada
     */
    public static String enHtml(Pedido pedido) {
        String contenido = PlantillaCorreo.titulo("¡Gracias por tu compra, "
                        + primerNombre(pedido.getNombreUsuario()) + "!")
                + PlantillaCorreo.parrafo("Tu pedido quedó confirmado y ya lo estamos"
                        + " preparando. Este es el detalle:")
                + PlantillaCorreo.recuadro(
                        "Pedido", pedido.getId(),
                        "Fecha", pedido.getFecha().format(FECHA),
                        "Artículos", String.valueOf(pedido.getTotalUnidades()))
                + tablaProductos(pedido)
                + seccionEntrega(pedido)
                + PlantillaCorreo.parrafo("<span style=\"color:" + PlantillaCorreo.TEXTO_SUAVE
                        + ";font-size:13px;\">Guarda este correo como comprobante.</span>");
        return PlantillaCorreo.envolver(
                "Pedido " + pedido.getId() + " confirmado por " + moneda(pedido.getTotal()),
                asuntoConfirmacion(pedido), contenido);
    }

    /**
     * @param pedido pedido confirmado
     * @return resumen en texto plano, para consola o registro
     */
    public static String enTexto(Pedido pedido) {
        return "Pedido " + pedido.getId() + " - " + pedido.getFecha().format(FECHA) + '\n'
                + "Cliente: " + pedido.getNombreUsuario() + '\n'
                + "Entrega en: " + pedido.getDireccionEntrega() + '\n'
                + documentoEnTexto(pedido)
                + lineasEnTexto(pedido)
                + "Total: " + moneda(pedido.getTotal());
    }

    // ---------------------------------------------------------------------
    // Alerta de venta para la tienda
    // ---------------------------------------------------------------------

    /** @return asunto de la alerta de venta */
    public static String asuntoAlertaVenta(Pedido pedido) {
        return "Nueva venta: " + moneda(pedido.getTotal()) + " - pedido " + pedido.getId();
    }

    /**
     * @param pedido pedido recién confirmado
     * @return alerta de venta en HTML: quién compró, qué y por cuánto
     */
    public static String alertaVentaEnHtml(Pedido pedido) {
        String contenido = PlantillaCorreo.titulo("Nueva venta por " + moneda(pedido.getTotal()))
                + PlantillaCorreo.parrafo("Un cliente acaba de confirmar un pedido. El stock"
                        + " de estos productos ya se descontó.")
                + PlantillaCorreo.recuadro(
                        "Cliente", pedido.getNombreUsuario(),
                        "Correo", pedido.getCorreoUsuario(),
                        "Pedido", pedido.getId(),
                        "Fecha", pedido.getFecha().format(FECHA))
                + tablaProductos(pedido)
                + seccionEntrega(pedido);
        return PlantillaCorreo.envolver(
                pedido.getNombreUsuario() + " compró " + pedido.getTotalUnidades() + " artículo(s)",
                asuntoAlertaVenta(pedido), contenido);
    }

    /** @return alerta de venta en texto plano */
    public static String alertaVentaEnTexto(Pedido pedido) {
        return "Nueva venta - pedido " + pedido.getId() + " - " + pedido.getFecha().format(FECHA) + '\n'
                + "Cliente: " + pedido.getNombreUsuario() + " <" + pedido.getCorreoUsuario() + ">\n"
                + "Entrega en: " + pedido.getDireccionEntrega() + '\n'
                + documentoEnTexto(pedido)
                + lineasEnTexto(pedido)
                + "Total: " + moneda(pedido.getTotal());
    }

    // ---------------------------------------------------------------------
    // Piezas comunes
    // ---------------------------------------------------------------------

    /**
     * Tabla de productos: nombre, cantidad, precio unitario y subtotal, con
     * el total al pie. Los importes van alineados a la derecha para que se
     * lean en columna.
     */
    private static String tablaProductos(Pedido pedido) {
        String celda = "padding:11px 10px;border-bottom:1px solid " + PlantillaCorreo.BORDE
                + ";font-size:14px;color:" + PlantillaCorreo.TEXTO + ";";
        String cabecera = "padding:10px;font-size:12px;font-weight:600;text-transform:uppercase;"
                + "letter-spacing:.4px;color:" + PlantillaCorreo.TEXTO_SUAVE + ";border-bottom:2px solid "
                + PlantillaCorreo.BORDE + ";";

        StringBuilder filas = new StringBuilder();
        for (LineaPedido linea : pedido.getLineas()) {
            filas.append("<tr>")
                    .append("<td style=\"").append(celda).append("\">")
                    .append(PlantillaCorreo.escapar(linea.getNombreProducto())).append("</td>")
                    .append("<td align=\"center\" style=\"").append(celda).append("\">")
                    .append(linea.getCantidad()).append("</td>")
                    .append("<td class=\"col-precio\" align=\"right\" style=\"").append(celda)
                    .append("white-space:nowrap;\">")
                    .append(moneda(linea.getPrecioUnitario())).append("</td>")
                    .append("<td align=\"right\" style=\"").append(celda)
                    .append("white-space:nowrap;font-weight:600;\">")
                    .append(moneda(linea.getSubtotal())).append("</td>")
                    .append("</tr>");
        }
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\""
                + " style=\"border-collapse:collapse;\">"
                + "<tr>"
                + "<th align=\"left\" style=\"" + cabecera + "\">Producto</th>"
                + "<th align=\"center\" style=\"" + cabecera + "\">Cant.</th>"
                + "<th class=\"col-precio\" align=\"right\" style=\"" + cabecera + "\">Precio</th>"
                + "<th align=\"right\" style=\"" + cabecera + "\">Subtotal</th>"
                + "</tr>"
                + filas
                + "</table>"
                // El total va en su propia tabla y no como última fila: con
                // un colspan, la columna de precio oculta en el móvil seguía
                // contando en la rejilla y la tabla dejaba de ocupar el ancho.
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\""
                + " style=\"margin:0 0 22px;\"><tr>"
                + "<td align=\"right\" style=\"padding:14px 10px 4px;font-size:15px;font-weight:700;"
                + "color:" + PlantillaCorreo.TEXTO + ";\">Total pagado</td>"
                + "<td align=\"right\" width=\"1%\" style=\"padding:14px 10px 4px;font-size:18px;"
                + "font-weight:700;white-space:nowrap;color:" + PlantillaCorreo.ACENTO + ";\">"
                + moneda(pedido.getTotal()) + "</td></tr></table>";
    }

    private static String seccionEntrega(Pedido pedido) {
        String rotulo = "<p style=\"margin:0 0 4px;font-size:12px;font-weight:600;text-transform:uppercase;"
                + "letter-spacing:.4px;color:" + PlantillaCorreo.TEXTO_SUAVE + ";\">";
        String seccion = rotulo + "Dirección de entrega</p>"
                + PlantillaCorreo.parrafo(PlantillaCorreo.escapar(pedido.getDireccionEntrega()));
        // Los pedidos anteriores a la cédula obligatoria no la tienen.
        if (!pedido.getDocumentoComprador().isEmpty()) {
            seccion += rotulo + "Documento del comprador</p>"
                    + PlantillaCorreo.parrafo(PlantillaCorreo.escapar(pedido.getDocumentoComprador()));
        }
        return seccion;
    }

    private static String documentoEnTexto(Pedido pedido) {
        return pedido.getDocumentoComprador().isEmpty()
                ? "" : "Documento: " + pedido.getDocumentoComprador() + '\n';
    }

    private static String lineasEnTexto(Pedido pedido) {
        StringBuilder texto = new StringBuilder();
        for (LineaPedido linea : pedido.getLineas()) {
            texto.append("  - ").append(linea.getCantidad()).append(" x ")
                    .append(linea.getNombreProducto())
                    .append(" (").append(moneda(linea.getPrecioUnitario())).append(" c/u)  ")
                    .append(moneda(linea.getSubtotal())).append('\n');
        }
        return texto.toString();
    }

    private static String primerNombre(String nombre) {
        return nombre == null || nombre.isBlank() ? "" : nombre.trim().split("\\s+")[0];
    }

    /** @return importe con separador de miles, en pesos */
    public static String moneda(double valor) {
        return String.format("$ %,.0f", valor);
    }
}
