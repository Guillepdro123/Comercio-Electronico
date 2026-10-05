package service.correo;

/**
 * Marco común de los correos de la tienda: cabecera con la marca, cuerpo y
 * pie. Cada correo (bienvenida, confirmación de compra, alerta de venta) solo
 * aporta su contenido.
 *
 * <p><b>Por qué el HTML está escrito así.</b> Los clientes de correo no son
 * navegadores: no todos respetan el {@code <style>} del {@code <head>}, y
 * Outlook dibuja con el motor de Word, que no entiende {@code flex} ni
 * {@code grid}. Lo que funciona en todos es lo de hace veinte años, y por eso
 * aquí se usa:
 * maquetación con tablas, estilos en línea en cada elemento, colores repetidos
 * también como atributo {@code bgcolor}, fuentes del sistema y un ancho máximo
 * de 600px, que es el que muestran los clientes de escritorio sin barra de
 * desplazamiento y que en el móvil se encoge solo.</p>
 *
 * <p><b>Colores claros a propósito.</b> La aplicación es oscura, pero un
 * correo oscuro se ve mal cuando el cliente de correo invierte colores en su
 * propio modo oscuro. La identidad morada va en la cabecera y en los
 * acentos.</p>
 *
 * <p>Quien llama es responsable de escapar los datos del usuario que meta en
 * el contenido: para eso está {@link #escapar(String)}.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class PlantillaCorreo {

    static final String FUENTE = "font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;";
    static final String ACENTO = "#7C3AED";
    static final String TEXTO = "#231F3D";
    static final String TEXTO_SUAVE = "#6B6480";
    static final String BORDE = "#E6E1F5";
    static final String FONDO_SUAVE = "#F7F5FD";

    private PlantillaCorreo() {
        // Solo métodos estáticos.
    }

    /**
     * Envuelve un contenido en el marco de la tienda.
     *
     * @param preencabezado texto corto que los clientes de correo muestran junto
     *                      al asunto en la bandeja; no se ve al abrir el correo
     * @param titulo        título de la pestaña o ventana del mensaje
     * @param contenido     HTML del cuerpo, con los datos ya escapados
     * @return documento HTML completo
     */
    public static String envolver(String preencabezado, String titulo, String contenido) {
        return "<!doctype html><html lang=\"es\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + escapar(titulo) + "</title>"
                // Solo para pantallas estrechas: menos margen y sin la columna
                // de precio unitario (el subtotal ya lo dice). Gmail y Apple
                // Mail la aplican; quien no la entienda muestra la versión de
                // escritorio, que sigue siendo correcta.
                + "<style>@media only screen and (max-width:480px){"
                + ".marco{padding:16px 6px!important}"
                + ".cuerpo{padding:24px 18px 20px!important}"
                + ".col-precio{display:none!important}}</style></head>"
                + "<body style=\"margin:0;padding:0;background:#EFECF8;\">"
                // Preencabezado oculto: lo que la bandeja muestra bajo el asunto.
                + "<div style=\"display:none;max-height:0;overflow:hidden;opacity:0;\">"
                + escapar(preencabezado) + "</div>"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\""
                + " border=\"0\" bgcolor=\"#EFECF8\"><tr><td class=\"marco\" align=\"center\""
                + " style=\"padding:32px 12px;\">"
                + "<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\""
                + " bgcolor=\"#FFFFFF\" style=\"width:100%;max-width:600px;background:#FFFFFF;"
                + "border:1px solid " + BORDE + ";border-radius:14px;overflow:hidden;\">"
                // Cabecera con la marca, en los colores de la aplicación.
                + "<tr><td bgcolor=\"#1B1627\" style=\"background:#1B1627;padding:24px 32px;\">"
                + "<span style=\"" + FUENTE + "font-size:21px;font-weight:700;color:#FFFFFF;"
                + "letter-spacing:.2px;\">Comercio <span style=\"color:#A78BFA;\">Electrónico</span>"
                + "</span></td></tr>"
                + "<tr><td bgcolor=\"#8B5CF6\" style=\"background:#8B5CF6;height:4px;line-height:4px;"
                + "font-size:0;\">&nbsp;</td></tr>"
                // Cuerpo.
                + "<tr><td class=\"cuerpo\" style=\"" + FUENTE + "padding:32px 32px 28px;color:" + TEXTO
                + ";font-size:15px;line-height:1.6;\">" + contenido + "</td></tr>"
                // Pie.
                + "<tr><td bgcolor=\"" + FONDO_SUAVE + "\" style=\"" + FUENTE + "padding:18px 32px;"
                + "background:" + FONDO_SUAVE + ";border-top:1px solid " + BORDE + ";font-size:12px;"
                + "line-height:1.5;color:" + TEXTO_SUAVE + ";\">"
                + "Este correo se envió automáticamente desde Comercio Electrónico. "
                + "Si necesitas ayuda, responde a este mensaje y te atenderemos."
                + "</td></tr></table></td></tr></table></body></html>";
    }

    /**
     * @param texto título principal del correo
     * @return encabezado grande, ya escapado
     */
    static String titulo(String texto) {
        return "<h1 style=\"" + FUENTE + "margin:0 0 12px;font-size:23px;line-height:1.3;color:"
                + TEXTO + ";\">" + escapar(texto) + "</h1>";
    }

    /** @return párrafo con el HTML dado (el llamador escapa lo que haga falta) */
    static String parrafo(String html) {
        return "<p style=\"margin:0 0 16px;\">" + html + "</p>";
    }

    /**
     * Recuadro de datos clave (número de pedido, fecha, cuenta...): pares
     * etiqueta-valor, ya escapados aquí.
     *
     * @param pares etiqueta, valor, etiqueta, valor...
     * @return tabla de dos columnas sobre fondo suave
     */
    static String recuadro(String... pares) {
        StringBuilder filas = new StringBuilder();
        for (int i = 0; i + 1 < pares.length; i += 2) {
            filas.append("<tr><td style=\"padding:4px 0;color:").append(TEXTO_SUAVE)
                    .append(";font-size:13px;width:40%;\">").append(escapar(pares[i]))
                    // Un número de pedido son 24 caracteres sin espacios: sin
                    // permitir el corte, en un móvil empujaría el correo de lado.
                    .append("</td><td style=\"padding:4px 0;font-size:14px;font-weight:600;"
                            + "word-break:break-all;color:")
                    .append(TEXTO).append(";\">").append(escapar(pares[i + 1])).append("</td></tr>");
        }
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\""
                + " bgcolor=\"" + FONDO_SUAVE + "\" style=\"background:" + FONDO_SUAVE
                + ";border:1px solid " + BORDE + ";border-radius:10px;margin:0 0 22px;\">"
                + "<tr><td style=\"padding:14px 18px;\"><table role=\"presentation\" width=\"100%\""
                + " cellpadding=\"0\" cellspacing=\"0\">" + filas + "</table></td></tr></table>";
    }

    /**
     * Escapa un texto para meterlo en HTML: evita que un nombre con
     * {@code <} o {@code &} rompa el correo, o que alguien inyecte etiquetas
     * propias (un enlace falso, por ejemplo) a través de su nombre o su
     * dirección.
     *
     * @param texto texto libre, posiblemente escrito por un usuario
     * @return el texto seguro para HTML
     */
    public static String escapar(String texto) {
        return texto == null ? "" : texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
