package service;

import java.time.format.DateTimeFormatter;
import model.entity.LineaPedido;
import model.entity.Pedido;

/**
 * Arma el texto del resumen de compra que acompaña al aviso.
 *
 * <p><b>Por qué está aparte de los notificadores.</b> El resumen es el mismo
 * lo envíe quien lo envíe: si viviera dentro de {@link NotificadorResend}, la
 * versión local tendría que duplicarlo y las dos podrían separarse con el
 * tiempo. Aquí se escribe una vez y ambas lo reutilizan.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class ResumenPedido {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ResumenPedido() {
    }

    /**
     * @param pedido pedido confirmado
     * @return resumen en texto plano, para consola o registro
     */
    public static String enTexto(Pedido pedido) {
        StringBuilder texto = new StringBuilder();
        texto.append("Pedido ").append(pedido.getId())
                .append(" - ").append(pedido.getFecha().format(FECHA)).append('\n')
                .append("Cliente: ").append(pedido.getNombreUsuario()).append('\n')
                .append("Entrega en: ").append(pedido.getDireccionEntrega()).append('\n');
        for (LineaPedido linea : pedido.getLineas()) {
            texto.append("  - ").append(linea.getCantidad()).append(" x ")
                    .append(linea.getNombreProducto())
                    .append("  ").append(moneda(linea.getSubtotal())).append('\n');
        }
        texto.append("Total: ").append(moneda(pedido.getTotal()));
        return texto.toString();
    }

    /**
     * @param pedido pedido confirmado
     * @return resumen en HTML, el formato que espera el correo
     */
    public static String enHtml(Pedido pedido) {
        StringBuilder filas = new StringBuilder();
        for (LineaPedido linea : pedido.getLineas()) {
            filas.append("<tr><td>").append(escapar(linea.getNombreProducto()))
                    .append("</td><td align=\"center\">").append(linea.getCantidad())
                    .append("</td><td align=\"right\">").append(moneda(linea.getSubtotal()))
                    .append("</td></tr>");
        }
        return "<div style=\"font-family:Segoe UI,Arial,sans-serif;color:#231F3D\">"
                + "<h2 style=\"color:#8B5CF6\">¡Gracias por tu compra, "
                + escapar(pedido.getNombreUsuario()) + "!</h2>"
                + "<p>Tu pedido <b>" + pedido.getId() + "</b> quedó confirmado el "
                + pedido.getFecha().format(FECHA) + ".</p>"
                + "<table cellpadding=\"6\" style=\"border-collapse:collapse;width:100%\">"
                + "<tr style=\"background:#F3F4F6\"><th align=\"left\">Producto</th>"
                + "<th>Cantidad</th><th align=\"right\">Subtotal</th></tr>"
                + filas
                + "<tr><td colspan=\"2\"><b>Total</b></td><td align=\"right\"><b>"
                + moneda(pedido.getTotal()) + "</b></td></tr></table>"
                + "<p><b>Dirección de entrega:</b><br>"
                + escapar(pedido.getDireccionEntrega()) + "</p></div>";
    }

    /** @return importe con separador de miles, en pesos */
    public static String moneda(double valor) {
        return String.format("$ %,.0f", valor);
    }

    /** Evita que un nombre con &lt; o &amp; rompa el HTML del correo. */
    private static String escapar(String texto) {
        return texto == null ? "" : texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
