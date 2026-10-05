package service.correo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import model.entity.Pedido;
import model.entity.Usuario;

/**
 * Envía los correos transaccionales (confirmación de compra, alerta de venta
 * y bienvenida) por la API de Resend: un {@code POST} a
 * {@code https://api.resend.com/emails} con la clave como {@code Bearer}.
 *
 * <p><b>Sin librerías externas.</b> Resend se consume por HTTP con un JSON, y
 * el JDK ya trae {@link HttpClient}; no hace falta añadir dependencias.</p>
 *
 * <p><b>La clave nunca va en el código.</b> Se recibe por constructor y
 * {@code app.Main} la lee de {@code config.properties}
 * ({@code resend.api.key}), el archivo de secretos excluido del control de
 * versiones.</p>
 *
 * <p><b>Un fallo de correo no tumba nada.</b> Cualquier problema de red o de
 * la API se traduce en {@code false} y un mensaje por consola: el pedido o la
 * cuenta ya están guardados, y deshacerlos por no poder avisar sería peor que
 * no avisar.</p>
 *
 * <p><b>Esta clase sí bloquea</b> mientras Resend responde. Nadie la llama
 * directamente: {@code app.Main} la envuelve en
 * {@link NotificadorEnSegundoPlano}, que es quien la saca del camino.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.1
 */
public class NotificadorResend implements INotificadorCorreo {

    private static final String ENDPOINT = "https://api.resend.com/emails";
    private static final Duration ESPERA_MAXIMA = Duration.ofSeconds(10);

    private final String claveApi;
    private final String remitente;
    /** A quién le llega la alerta de cada venta (la tienda). */
    private final String destinoAlertas;
    private final String endpoint;
    private final HttpClient cliente;

    /**
     * @param claveApi       clave de Resend; nunca se escribe en el código fuente
     * @param remitente      dirección de un dominio verificado en Resend desde
     *                       la que se envía
     * @param destinoAlertas dirección de la tienda que recibe la alerta de
     *                       cada venta
     */
    public NotificadorResend(String claveApi, String remitente, String destinoAlertas) {
        this(claveApi, remitente, destinoAlertas, ENDPOINT);
    }

    /**
     * Constructor completo: permite apuntar a otro servidor para probar el
     * envío sin gastar la cuota ni mandar correos reales.
     *
     * @param claveApi       clave de Resend
     * @param remitente      dirección de envío
     * @param destinoAlertas dirección que recibe las alertas de venta
     * @param endpoint       URL de la API
     */
    public NotificadorResend(String claveApi, String remitente, String destinoAlertas,
                             String endpoint) {
        this.claveApi = claveApi;
        this.remitente = remitente;
        this.destinoAlertas = destinoAlertas;
        this.endpoint = endpoint;
        this.cliente = HttpClient.newBuilder().connectTimeout(ESPERA_MAXIMA).build();
    }

    @Override
    public boolean notificarCompra(Pedido pedido, String correoDestino) {
        return enviar(correoDestino, ResumenPedido.asuntoConfirmacion(pedido),
                ResumenPedido.enHtml(pedido));
    }

    @Override
    public boolean notificarVenta(Pedido pedido) {
        return enviar(destinoAlertas, ResumenPedido.asuntoAlertaVenta(pedido),
                ResumenPedido.alertaVentaEnHtml(pedido));
    }

    @Override
    public boolean notificarBienvenida(Usuario usuario) {
        return enviar(usuario.getCorreo(), MensajeBienvenida.asunto(usuario),
                MensajeBienvenida.enHtml(usuario));
    }

    /** Un único punto que habla con Resend: los dos correos solo cambian el contenido. */
    private boolean enviar(String destino, String asunto, String html) {
        try {
            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(ESPERA_MAXIMA)
                    .header("Authorization", "Bearer " + claveApi)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpoJson(destino, asunto, html)))
                    .build();

            HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
            boolean exito = respuesta.statusCode() / 100 == 2;
            if (!exito) {
                System.err.println("Resend respondió " + respuesta.statusCode() + ": " + respuesta.body());
            }
            return exito;
        } catch (Exception ex) {
            System.err.println("No se pudo enviar el correo \"" + asunto + "\": " + ex.getMessage());
            return false;
        }
    }

    /** Arma el JSON que espera Resend. */
    private String cuerpoJson(String destino, String asunto, String html) {
        return "{"
                + "\"from\":\"" + escaparJson(remitente) + "\","
                + "\"to\":[\"" + escaparJson(destino) + "\"],"
                + "\"subject\":\"" + escaparJson(asunto) + "\","
                + "\"html\":\"" + escaparJson(html) + "\""
                + "}";
    }

    /**
     * Escapa un texto para meterlo en una cadena JSON.
     *
     * <p>No basta con comillas y saltos de línea: JSON prohíbe cualquier
     * carácter de control sin escapar, y la versión anterior dejaba pasar,
     * por ejemplo, una tabulación pegada en un nombre, con lo que el cuerpo
     * dejaba de ser JSON válido. Por eso todo lo que está por debajo del
     * espacio sale como {@code \\uXXXX}.</p>
     */
    private String escaparJson(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder salida = new StringBuilder(texto.length() + 16);
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> salida.append("\\\"");
                case '\\' -> salida.append("\\\\");
                case '\n' -> salida.append("\\n");
                case '\r' -> salida.append("\\r");
                case '\t' -> salida.append("\\t");
                default -> {
                    if (c < 0x20) {
                        salida.append(String.format("\\u%04x", (int) c));
                    } else {
                        salida.append(c);
                    }
                }
            }
        }
        return salida.toString();
    }
}
