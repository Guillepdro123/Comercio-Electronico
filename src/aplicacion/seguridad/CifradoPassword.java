package aplicacion.seguridad;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Cifrado y verificación de contraseñas con BCrypt.
 *
 * <p><b>Es el único punto del proyecto que conoce la librería.</b> Mismo
 * criterio que {@link PoliticaPassword} con las reglas: si mañana se cambia de
 * algoritmo, se cambia aquí y nada más. Los controladores piden "cifra" o
 * "coincide", sin saber con qué.</p>
 *
 * <p><b>Por qué vive en {@code controller} y no en el modelo.</b> Cifrar no es
 * persistir ni es una regla de negocio del dominio: es una decisión de
 * seguridad del caso de uso, igual que cuántos intentos se toleran. Además el
 * modelo no debe arrastrar dependencias de librerías.</p>
 *
 * <p><b>Compatible con lo que ya había.</b> {@link #coincide(String, String)}
 * acepta también contraseñas guardadas en claro —las del almacén en memoria,
 * que se siembran así— comparándolas directamente cuando no tienen forma de
 * hash. Sin eso, activar MongoDB habría dejado fuera a todo usuario creado
 * antes, y la aplicación tiene que seguir funcionando sin la nube.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class CifradoPassword {

    /**
     * Coste del algoritmo. Cada punto duplica el trabajo; 10 es el valor por
     * omisión de BCrypt y tarda unas decenas de milisegundos, imperceptible en
     * un formulario y caro de fuerza bruta.
     */
    private static final int COSTE = 10;

    /** Prefijos con los que empieza todo hash de BCrypt. */
    private static final String[] PREFIJOS_BCRYPT = {"$2a$", "$2b$", "$2y$"};

    /**
     * Lo que se guarda como contraseña en una cuenta creada con Google: esa
     * cuenta no tiene contraseña propia, entra solo por Google.
     *
     * <p><b>Nunca debe poder coincidir con nada.</b> Como este método acepta
     * contraseñas heredadas en claro, sin la comprobación explícita de
     * {@link #coincide(String, String)} bastaría con escribir
     * {@code AUTH_GOOGLE} en el Login para entrar en cualquier cuenta de
     * Google. La marca se trata aquí, y no en cada controlador, porque este es
     * el único sitio donde se comparan contraseñas: así lo cubren a la vez el
     * Login y el cambio de contraseña del perfil.</p>
     */
    public static final String MARCA_CUENTA_GOOGLE = "AUTH_GOOGLE";

    /**
     * @param password contraseña en claro, tal como la escribió el usuario
     * @return su hash, con la sal incluida dentro
     */
    public String cifrar(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(COSTE));
    }

    /**
     * Comprueba una contraseña contra lo guardado.
     *
     * @param password  lo que acaba de escribir el usuario
     * @param guardado  lo que hay en el repositorio: un hash, o texto en claro
     *                  si el usuario se creó antes de activar el cifrado
     * @return {@code true} si coinciden
     */
    public boolean coincide(String password, String guardado) {
        if (password == null || guardado == null || esCuentaGoogle(guardado)) {
            return false;
        }
        if (!esHash(guardado)) {
            return password.equals(guardado);
        }
        try {
            return BCrypt.checkpw(password, guardado);
        } catch (IllegalArgumentException ex) {
            // Un hash corrupto no autentica a nadie, pero tampoco debe tumbar
            // el inicio de sesión.
            return false;
        }
    }

    /**
     * @param guardado contraseña guardada de una cuenta
     * @return {@code true} si la cuenta entra con Google y no tiene contraseña
     */
    public boolean esCuentaGoogle(String guardado) {
        return MARCA_CUENTA_GOOGLE.equals(guardado);
    }

    /**
     * @param texto valor guardado
     * @return {@code true} si tiene forma de hash de BCrypt
     */
    public boolean esHash(String texto) {
        if (texto == null) {
            return false;
        }
        for (String prefijo : PREFIJOS_BCRYPT) {
            if (texto.startsWith(prefijo)) {
                return true;
            }
        }
        return false;
    }
}
