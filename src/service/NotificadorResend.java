package service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import model.entity.Pedido;

/**
 * Implementación de {@link INotificadorPedido} que envía el correo de
 * confirmación a través de la API de Resend.
 *
 * <p><b>Sin librerías externas.</b> Resend se consume por HTTP con un JSON, y
 * el JDK ya trae {@link HttpClient} desde Java 11; no hace falta añadir
 * dependencias al proyecto, que sigue siendo JDK + Ant puro.</p>
 *
 * <p><b>La clave nunca va en el código.</b> Se recibe por constructor y
 * {@code app.Main} la lee de la variable de entorno {@code RESEND_API_KEY}:
 * una credencial escrita en un archivo fuente termina copiada en cualquier
 * repositorio o entrega.</p>
 *
 * <p><b>Un fallo de correo no tumba la compra.</b> Cualquier problema de red
 * o de la API se traduce en {@code false} y un mensaje por consola; el pedido
 * ya está registrado y el stock descontado, así que deshacerlo por esto sería
 * peor que no avisar.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class NotificadorResend implements INotificadorPedido {

    private static final String ENDPOINT = "https://api.resend.com/emails";
    private static final Duration ESPERA_MAXIMA = Duration.ofSeconds(10);

    private final String claveApi;
    private final String remitente;
    private final HttpClient cliente;

    /**
     * @param claveApi  clave de Resend; nunca se escribe en el código fuente
     * @param remitente dirección verificada en Resend desde la que se envía
     */
    public NotificadorResend(String claveApi, String remitente) {
        this.claveApi = claveApi;
        this.remitente = remitente;
        this.cliente = HttpClient.newBuilder().connectTimeout(ESPERA_MAXIMA).build();
    }

    @Override
    public boolean notificarCompra(Pedido pedido, String correoDestino) {
        try {
            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .timeout(ESPERA_MAXIMA)
                    .header("Authorization", "Bearer " + claveApi)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpoJson(pedido, correoDestino)))
                    .build();

            HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
            boolean exito = respuesta.statusCode() / 100 == 2;
            if (!exito) {
                System.err.println("Resend respondió " + respuesta.statusCode() + ": " + respuesta.body());
            }
            return exito;
        } catch (Exception ex) {
            System.err.println("No se pudo enviar el correo de confirmación: " + ex.getMessage());
            return false;
        }
    }

    /** Arma el JSON que espera Resend. */
    private String cuerpoJson(Pedido pedido, String correoDestino) {
        return "{"
                + "\"from\":\"" + escapar(remitente) + "\","
                + "\"to\":[\"" + escapar(correoDestino) + "\"],"
                + "\"subject\":\"Confirmación de tu pedido " + escapar(pedido.getId()) + "\","
                + "\"html\":\"" + escapar(ResumenPedido.enHtml(pedido)) + "\""
                + "}";
    }

    /** Escapa comillas y saltos para no romper el JSON al insertar el HTML. */
    private String escapar(String texto) {
        return texto == null ? "" : texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
