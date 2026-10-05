package aplicacion.acceso;

import aplicacion.seguridad.CifradoPassword;
import aplicacion.seguridad.ControlIntentosFallidos;
import model.entity.Usuario;
import model.repository.IUsuarioRepository;

/**
 * Caso de uso "entrar en el sistema": comprobar credenciales, contar los
 * intentos fallidos y reconocer a quien vuelve de Google.
 *
 * <p><b>Qué hace esta clase aquí.</b> Estas reglas vivían en
 * {@code LoginController}: cuántos fallos se toleran, qué mensaje se da en
 * cada caso y cómo se compara una contraseña. Sacarlas permite probarlas sin
 * abrir ventanas, que es justo lo que faltaba en la parte más delicada del
 * sistema.</p>
 *
 * <p><b>Cada instancia lleva su propio contador de intentos.</b> Se crea una
 * por pantalla de acceso, igual que antes: cerrar sesión y volver al Login
 * empieza de cero, y el contador no es compartido entre ventanas.</p>
 *
 * <p><b>Solo cuentan los fallos consecutivos:</b> un acceso correcto reinicia
 * el contador, porque lo que se quiere frenar es la prueba sistemática de
 * contraseñas, no penalizar un error de tecleo aislado.</p>
 *
 * <p><b>Bloquea</b> mientras busca la cuenta y compara con BCrypt: quien lo
 * llame desde una interfaz gráfica debe hacerlo fuera del hilo de eventos.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class AutenticacionService {

    private final IUsuarioRepository repositorio;
    /** Único punto que conoce BCrypt; ver {@link CifradoPassword}. */
    private final CifradoPassword cifrado = new CifradoPassword();
    /** Política de intentos fallidos; ver {@link ControlIntentosFallidos}. */
    private final ControlIntentosFallidos intentos = new ControlIntentosFallidos();

    /**
     * @param repositorio almacén de usuarios (abstracción)
     */
    public AutenticacionService(IUsuarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Comprueba unas credenciales.
     *
     * @param correo   correo escrito en el formulario
     * @param password contraseña escrita en el formulario
     * @return qué ocurrió, con el mensaje ya redactado cuando hay algo que
     *         decirle al usuario
     */
    public ResultadoAcceso intentar(String correo, String password) {
        if (vacio(correo) || vacio(password)) {
            return ResultadoAcceso.rechazado("Ingresa tu correo y tu contraseña.");
        }

        Usuario usuario = repositorio.buscarPorCorreo(correo);

        // Una cuenta creada con Google no tiene contraseña: CifradoPassword ya
        // impide que coincida, pero sin este aviso el usuario vería "correo o
        // contraseña incorrectos" sin entender por qué. No cuenta como intento
        // fallido: aquí no hay contraseña que adivinar.
        if (usuario != null && cifrado.esCuentaGoogle(usuario.getPassword())) {
            return ResultadoAcceso.cuentaDeGoogle(
                    "Esta cuenta entra con Google. Usa el botón de Google.");
        }

        // La comparación pasa por BCrypt, no por equals: lo guardado es un
        // hash. CifradoPassword acepta además contraseñas en claro, para que
        // los usuarios creados antes de activar el cifrado sigan entrando.
        if (usuario == null || !cifrado.coincide(password, usuario.getPassword())) {
            return registrarFallo();
        }

        intentos.reiniciar();
        return ResultadoAcceso.aceptado(usuario);
    }

    /**
     * Reconoce a quien vuelve de Google con un correo ya verificado.
     *
     * @param correo correo que devolvió Google
     * @return la cuenta con ese correo, o {@code null} si es la primera vez
     */
    public Usuario reconocerPorCorreo(String correo) {
        return repositorio.buscarPorCorreo(correo);
    }

    /**
     * Levanta el bloqueo por intentos fallidos.
     *
     * <p>Lo llama la pantalla cuando termina la cuenta atrás que ella misma
     * muestra: el servicio decide <em>cuánto</em> dura el bloqueo, la pantalla
     * decide cómo se ve.</p>
     */
    public void liberarBloqueo() {
        intentos.reiniciar();
    }

    /** Anota el fallo y decide si toca avisar o bloquear. */
    private ResultadoAcceso registrarFallo() {
        intentos.registrarFallo();
        if (intentos.limiteAlcanzado()) {
            return ResultadoAcceso.bloqueado(ControlIntentosFallidos.SEGUNDOS_BLOQUEO);
        }
        int restantes = intentos.intentosRestantes();
        return ResultadoAcceso.rechazado("Correo o contraseña incorrectos. Te "
                + (restantes == 1 ? "queda 1 intento." : "quedan " + restantes + " intentos."));
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
