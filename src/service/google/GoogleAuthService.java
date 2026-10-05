package service.google;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bson.Document;
import service.config.Configuracion;

/**
 * Acceso con Google por OAuth 2.0 / OpenID Connect con servidor de
 * redirección local (<em>loopback</em>), el flujo que Google recomienda para
 * aplicaciones de escritorio.
 *
 * <p><b>El flujo, paso a paso:</b></p>
 * <ol>
 *   <li>Levanta un servidor HTTP en {@code 127.0.0.1} en un puerto libre que
 *       elige el sistema, con una sola ruta: {@code /callback}.</li>
 *   <li>Abre el navegador del sistema en la página de consentimiento de
 *       Google, pidiendo solo {@code openid email profile}.</li>
 *   <li>Google redirige el navegador a {@code http://127.0.0.1:PUERTO/callback}
 *       con un código de autorización. El servidor lo recoge, responde una
 *       página de "ya puedes volver a la aplicación" y se apaga.</li>
 *   <li>Canjea el código por un token de acceso con un POST a Google, y con
 *       ese token pide el perfil: identificador, correo y nombre.</li>
 * </ol>
 *
 * <p><b>Protecciones</b> (las de la RFC 8252, la guía de OAuth para
 * aplicaciones nativas):</p>
 * <ul>
 *   <li><b>Solo escucha en {@code 127.0.0.1}</b>, nunca en todas las
 *       interfaces: ningún otro equipo de la red puede hablar con él.</li>
 *   <li><b>PKCE.</b> Se envía a Google el resumen SHA-256 de un secreto
 *       aleatorio, y el secreto solo al canjear el código. Un programa que
 *       interceptara el código no podría canjearlo sin ese secreto. Hace falta
 *       porque en una aplicación de escritorio el "secreto de cliente" viaja
 *       dentro del programa y no protege nada.</li>
 *   <li><b>{@code state}.</b> Un valor aleatorio que Google devuelve tal cual.
 *       Una petición al {@code /callback} que no lo traiga no la inició esta
 *       aplicación y se ignora, sin dar por terminado el acceso.</li>
 *   <li><b>Correo verificado.</b> La cuenta de la aplicación se busca por
 *       correo, así que se exige {@code email_verified}: si no, alguien podría
 *       crear una cuenta de Google con un correo ajeno sin verificar y entrar
 *       en la cuenta de su dueño.</li>
 *   <li><b>Un solo uso y con plazo.</b> El servidor se apaga en cuanto recibe
 *       la respuesta, y si en {@value #MINUTOS_ESPERA} minutos no llega, se
 *       apaga igual.</li>
 * </ul>
 *
 * <p><b>Por qué el perfil sale del endpoint {@code userinfo} y no de validar
 * la firma del {@code id_token}.</b> La especificación de OpenID Connect
 * permite confiar en lo que se recibe directamente de Google por TLS en el
 * mismo intercambio; validar la firma exigiría descargar y cachear las claves
 * públicas de Google y una librería de JWT, sin ganar seguridad en este
 * caso.</p>
 *
 * <p>No añade dependencias: el servidor es {@code com.sun.net.httpserver}, el
 * cliente es {@link HttpClient} del JDK, y el JSON de las respuestas se lee
 * con {@link Document#parse(String)} de la librería BSON que el proyecto ya
 * trae por MongoDB.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class GoogleAuthService implements IAutenticadorExterno {

    private static final String URL_AUTORIZACION = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String URL_TOKEN = "https://oauth2.googleapis.com/token";
    private static final String URL_PERFIL = "https://openidconnect.googleapis.com/v1/userinfo";

    private static final String RUTA_RETORNO = "/callback";
    private static final String ALCANCES = "openid email profile";
    /** Cuánto se espera a que el usuario termine en el navegador. */
    private static final int MINUTOS_ESPERA = 3;
    private static final Duration ESPERA_RED = Duration.ofSeconds(15);

    private final String idCliente;
    private final String secretoCliente;
    private final String urlAutorizacion;
    private final String urlToken;
    private final String urlPerfil;
    private final Consumer<URI> abridorNavegador;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(ESPERA_RED).build();
    private final SecureRandom azar = new SecureRandom();

    /** Espera en curso; {@link #cancelar()} la interrumpe desde otro hilo. */
    private volatile CompletableFuture<String> esperaEnCurso;

    /**
     * @param idCliente      ID de cliente OAuth de tipo "App de escritorio"
     * @param secretoCliente secreto de ese cliente (Google lo exige al canjear
     *                       el código aunque en escritorio no sea secreto)
     */
    public GoogleAuthService(String idCliente, String secretoCliente) {
        this(idCliente, secretoCliente, URL_AUTORIZACION, URL_TOKEN, URL_PERFIL,
                GoogleAuthService::abrirEnNavegador);
    }

    /**
     * Constructor completo. Existe para poder probar el flujo de punta a punta
     * contra un servidor falso, sin credenciales reales ni navegador.
     *
     * @param idCliente        ID de cliente OAuth
     * @param secretoCliente   secreto del cliente
     * @param urlAutorizacion  página de consentimiento
     * @param urlToken         endpoint que canjea el código
     * @param urlPerfil        endpoint del perfil
     * @param abridorNavegador cómo se abre la página de consentimiento
     */
    public GoogleAuthService(String idCliente, String secretoCliente, String urlAutorizacion,
                             String urlToken, String urlPerfil, Consumer<URI> abridorNavegador) {
        this.idCliente = idCliente;
        this.secretoCliente = secretoCliente;
        this.urlAutorizacion = urlAutorizacion;
        this.urlToken = urlToken;
        this.urlPerfil = urlPerfil;
        this.abridorNavegador = abridorNavegador;
    }

    @Override
    public PerfilExterno autenticar() {
        // Google exige el secreto al canjear el código aunque el cliente sea
        // de escritorio, y sin él responde "invalid_request" al final de todo,
        // cuando el usuario ya eligió su cuenta en el navegador. Se comprueba
        // aquí para fallar antes de abrirlo y con un mensaje que diga qué falta.
        if (secretoCliente == null || secretoCliente.isBlank()) {
            throw new IllegalStateException("Falta google.client.secret en "
                    + Configuracion.ARCHIVO + ": Google lo exige para completar el acceso.");
        }
        String estado = aleatorio(32);
        String verificador = aleatorio(64);

        CompletableFuture<String> codigo = new CompletableFuture<>();
        esperaEnCurso = codigo;
        HttpServer servidor = abrirServidor(estado, codigo);
        String redireccion = "http://127.0.0.1:" + servidor.getAddress().getPort() + RUTA_RETORNO;

        String codigoRecibido;
        try {
            abridorNavegador.accept(URI.create(urlAutorizacion + "?" + formulario(Map.of(
                    "client_id", idCliente,
                    "redirect_uri", redireccion,
                    "response_type", "code",
                    "scope", ALCANCES,
                    "state", estado,
                    "code_challenge", desafio(verificador),
                    "code_challenge_method", "S256",
                    // Deja elegir cuenta aunque el navegador ya tenga una
                    // sesión de Google abierta: en un equipo compartido la
                    // sesión del navegador no tiene por qué ser la de quien
                    // está usando la aplicación.
                    "prompt", "select_account"))));
            codigoRecibido = codigo.get(MINUTOS_ESPERA, TimeUnit.MINUTES);
        } catch (CancellationException ex) {
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (TimeoutException ex) {
            throw new IllegalStateException("Pasaron " + MINUTOS_ESPERA
                    + " minutos sin respuesta de Google. Vuelve a intentarlo.");
        } catch (ExecutionException ex) {
            throw new IllegalStateException(ex.getCause().getMessage());
        } finally {
            // Un solo uso: pase lo que pase, el puerto se libera aquí. El
            // manejador ya terminó de responder antes de completar la espera,
            // así que apagar sin demora no corta ninguna respuesta.
            servidor.stop(0);
            esperaEnCurso = null;
        }

        String tokenAcceso = canjearCodigo(codigoRecibido, redireccion, verificador);
        return pedirPerfil(tokenAcceso);
    }

    @Override
    public void cancelar() {
        CompletableFuture<String> espera = esperaEnCurso;
        if (espera != null) {
            espera.cancel(true);
        }
    }

    // ---------------------------------------------------------------------
    // Servidor de retorno
    // ---------------------------------------------------------------------

    private HttpServer abrirServidor(String estado, CompletableFuture<String> codigo) {
        try {
            // Puerto 0: lo elige el sistema entre los libres. Google acepta
            // cualquier puerto en una redirección a 127.0.0.1 de un cliente
            // de escritorio, así que no hace falta reservar uno fijo.
            HttpServer servidor = HttpServer.create(
                    new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0);
            servidor.createContext(RUTA_RETORNO, intercambio -> atender(intercambio, estado, codigo));
            servidor.start();
            return servidor;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo preparar el acceso con Google en este equipo.");
        }
    }

    /**
     * Atiende la redirección de Google.
     *
     * <p>Responde al navegador <em>antes</em> de completar la espera: en
     * cuanto se completa, el hilo que espera apaga el servidor.</p>
     */
    private void atender(HttpExchange intercambio, String estado, CompletableFuture<String> codigo)
            throws IOException {
        if (!"GET".equals(intercambio.getRequestMethod())) {
            responder(intercambio, 405, "Solicitud no admitida", "");
            return;
        }
        Map<String, String> parametros = leerConsulta(intercambio.getRequestURI().getRawQuery());

        // Comparación en tiempo constante: no le da a quien pruebe valores
        // pistas de cuántos caracteres acertó.
        boolean estadoValido = MessageDigest.isEqual(
                estado.getBytes(StandardCharsets.UTF_8),
                parametros.getOrDefault("state", "").getBytes(StandardCharsets.UTF_8));
        if (!estadoValido) {
            // No se completa la espera: esta petición no la inició la
            // aplicación, y la respuesta legítima de Google aún puede llegar.
            responder(intercambio, 400, "Solicitud no reconocida",
                    "Esta página no corresponde a un inicio de sesión en curso.");
            return;
        }
        if (parametros.containsKey("error")) {
            responder(intercambio, 200, "Acceso cancelado",
                    "No se completó el acceso con Google. Puedes cerrar esta pestaña.");
            codigo.completeExceptionally(new IllegalStateException(
                    "No se completó el acceso con Google."));
            return;
        }
        String recibido = parametros.get("code");
        if (recibido == null || recibido.isBlank()) {
            responder(intercambio, 400, "Solicitud incompleta", "Google no envió el código de acceso.");
            codigo.completeExceptionally(new IllegalStateException(
                    "Google no envió el código de acceso."));
            return;
        }
        responder(intercambio, 200, "Listo",
                "Ya iniciaste sesión con Google. Puedes cerrar esta pestaña y volver a la aplicación.");
        codigo.complete(recibido);
    }

    /**
     * Página mínima con la paleta de la aplicación. Nunca incluye nada de lo
     * que llegó en la petición: devolver parámetros de la URL dentro del HTML
     * sería abrirle la puerta a inyectar código en la página.
     */
    private void responder(HttpExchange intercambio, int estado, String titulo, String mensaje)
            throws IOException {
        String html = "<!doctype html><html lang='es'><head><meta charset='utf-8'>"
                + "<title>" + titulo + "</title></head>"
                + "<body style='margin:0;height:100vh;display:flex;align-items:center;"
                + "justify-content:center;background:#13111C;color:#E9E6F5;"
                + "font-family:Segoe UI,Arial,sans-serif'>"
                + "<div style='background:#231F3D;border:1px solid #8B5CF6;border-radius:14px;"
                + "padding:32px 40px;max-width:420px;text-align:center'>"
                + "<h2 style='margin:0 0 10px;color:#A78BFA'>" + titulo + "</h2>"
                + "<p style='margin:0;line-height:1.5'>" + mensaje + "</p></div></body></html>";
        byte[] cuerpo = html.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        intercambio.getResponseHeaders().set("Cache-Control", "no-store");
        intercambio.sendResponseHeaders(estado, cuerpo.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(cuerpo);
        }
    }

    // ---------------------------------------------------------------------
    // Llamadas a Google
    // ---------------------------------------------------------------------

    /** @return el token de acceso con el que se pide el perfil */
    private String canjearCodigo(String codigo, String redireccion, String verificador) {
        Map<String, String> campos = new LinkedHashMap<>();
        campos.put("code", codigo);
        campos.put("client_id", idCliente);
        campos.put("client_secret", secretoCliente);
        campos.put("redirect_uri", redireccion);
        campos.put("grant_type", "authorization_code");
        campos.put("code_verifier", verificador);

        HttpRequest peticion = HttpRequest.newBuilder(URI.create(urlToken))
                .timeout(ESPERA_RED)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formulario(campos)))
                .build();
        Document respuesta = enviar(peticion, "canjear el código de acceso");
        String token = respuesta.getString("access_token");
        if (token == null) {
            throw new IllegalStateException("Google no devolvió un token de acceso.");
        }
        return token;
    }

    private PerfilExterno pedirPerfil(String tokenAcceso) {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(urlPerfil))
                .timeout(ESPERA_RED)
                .header("Authorization", "Bearer " + tokenAcceso)
                .GET()
                .build();
        Document perfil = enviar(peticion, "leer tu perfil");

        String id = perfil.getString("sub");
        String correo = perfil.getString("email");
        if (id == null || correo == null) {
            throw new IllegalStateException("Google no compartió tu correo.");
        }
        if (!Boolean.TRUE.equals(perfil.getBoolean("email_verified"))) {
            throw new IllegalStateException("Tu correo de Google no está verificado.");
        }
        String nombre = perfil.getString("name");
        if (nombre == null || nombre.isBlank()) {
            nombre = correo.substring(0, correo.indexOf('@'));
        }
        return new PerfilExterno(id, correo.trim(), nombre.trim());
    }

    /**
     * Envía una petición y devuelve el JSON de la respuesta.
     *
     * <p>Si Google responde con error, el mensaje incluye su código
     * ({@code invalid_client}, {@code invalid_grant}...): no dice nada
     * sensible y es lo único que permite saber qué está mal configurado.</p>
     */
    private Document enviar(HttpRequest peticion, String paraQue) {
        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo contactar con Google para " + paraQue + ".");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió el acceso con Google.");
        }
        Document cuerpo;
        try {
            cuerpo = Document.parse(respuesta.body());
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Google respondió algo inesperado al " + paraQue + ".");
        }
        if (respuesta.statusCode() != 200) {
            // Se incluye error_description: el código solo ("invalid_request")
            // no dice qué falta, y la descripción de Google sí ("client_secret
            // is missing."). Ninguno de los dos lleva datos sensibles.
            String codigo = cuerpo.get("error", "error " + respuesta.statusCode());
            String detalle = cuerpo.get("error_description", "");
            throw new IllegalStateException("Google rechazó la solicitud al " + paraQue
                    + " (" + codigo + (detalle.isBlank() ? "" : ": " + detalle) + ").");
        }
        return cuerpo;
    }

    // ---------------------------------------------------------------------
    // Apoyo
    // ---------------------------------------------------------------------

    private static void abrirEnNavegador(URI direccion) {
        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IllegalStateException("Este equipo no permite abrir el navegador.");
        }
        try {
            Desktop.getDesktop().browse(direccion);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo abrir el navegador.");
        }
    }

    /** Texto aleatorio seguro para {@code state} y el verificador de PKCE. */
    private String aleatorio(int bytes) {
        byte[] datos = new byte[bytes];
        azar.nextBytes(datos);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(datos);
    }

    /** Desafío PKCE: el SHA-256 del verificador, en Base64 para URL sin relleno. */
    private static String desafio(String verificador) {
        try {
            byte[] resumen = MessageDigest.getInstance("SHA-256")
                    .digest(verificador.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(resumen);
        } catch (NoSuchAlgorithmException ex) {
            // Todo JDK está obligado a traer SHA-256.
            throw new IllegalStateException(ex);
        }
    }

    private static String formulario(Map<String, String> campos) {
        StringBuilder texto = new StringBuilder();
        for (Map.Entry<String, String> campo : campos.entrySet()) {
            if (texto.length() > 0) {
                texto.append('&');
            }
            texto.append(URLEncoder.encode(campo.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(campo.getValue(), StandardCharsets.UTF_8));
        }
        return texto.toString();
    }

    private static Map<String, String> leerConsulta(String consulta) {
        Map<String, String> parametros = new HashMap<>();
        if (consulta == null || consulta.isEmpty()) {
            return parametros;
        }
        for (String par : consulta.split("&")) {
            int igual = par.indexOf('=');
            String clave = igual < 0 ? par : par.substring(0, igual);
            String valor = igual < 0 ? "" : par.substring(igual + 1);
            parametros.put(URLDecoder.decode(clave, StandardCharsets.UTF_8),
                    URLDecoder.decode(valor, StandardCharsets.UTF_8));
        }
        return parametros;
    }
}
