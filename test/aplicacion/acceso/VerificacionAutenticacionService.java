package aplicacion.acceso;

import aplicacion.seguridad.CifradoPassword;
import aplicacion.seguridad.ControlIntentosFallidos;
import model.entity.Cliente;
import model.entity.Proveedor;
import model.repository.memoria.UsuarioRepositoryImpl;

/**
 * Comprobación automática de {@link AutenticacionService}.
 *
 * <p>Java puro, sin JUnit y sin abrir ninguna ventana. Cubre lo que antes solo
 * se podía probar a mano abriendo el Login: el límite de intentos fallidos, el
 * bloqueo temporal, el aviso de cuenta de Google y que las contraseñas se
 * comparan contra el hash.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class VerificacionAutenticacionService {

    private static int fallos = 0;

    private VerificacionAutenticacionService() {
    }

    private static void comprobar(String que, boolean ok) {
        System.out.println((ok ? "  OK    " : "  FALLA ") + que);
        if (!ok) {
            fallos++;
        }
    }

    /**
     * @param args no se usan
     */
    public static void main(String[] args) {
        UsuarioRepositoryImpl repositorio = new UsuarioRepositoryImpl();
        CifradoPassword cifrado = new CifradoPassword();
        repositorio.registrar(new Cliente("1", "Ana", "ana@correo.com",
                cifrado.cifrar("Clave123*"), "Calle 1"));
        repositorio.registrar(new Proveedor("google-901", "Sofía", "sofia@gmail.com",
                CifradoPassword.MARCA_CUENTA_GOOGLE, ""));
        AutenticacionService servicio = new AutenticacionService(repositorio);

        System.out.println("Credenciales");
        comprobar("pide los dos campos si falta alguno",
                servicio.intentar("", "Clave123*").mensaje().contains("Ingresa tu correo"));
        comprobar("acepta la contraseña correcta comparando contra el hash",
                servicio.intentar("ana@correo.com", "Clave123*").aceptado());
        comprobar("reconoce el correo sin distinguir mayúsculas",
                servicio.intentar("ANA@correo.com", "Clave123*").aceptado());
        comprobar("devuelve la cuenta autenticada",
                "Ana".equals(servicio.intentar("ana@correo.com", "Clave123*").usuario().getNombres()));

        System.out.println("Cuenta de Google");
        ResultadoAcceso google = servicio.intentar("sofia@gmail.com", "loquesea");
        comprobar("avisa de que esa cuenta entra con Google",
                google.estado() == ResultadoAcceso.Estado.CUENTA_DE_GOOGLE
                        && google.mensaje().contains("Google"));
        comprobar("escribir la marca como contraseña tampoco entra",
                !servicio.intentar("sofia@gmail.com", CifradoPassword.MARCA_CUENTA_GOOGLE)
                        .aceptado());

        System.out.println("Intentos fallidos y bloqueo");
        AutenticacionService conContador = new AutenticacionService(repositorio);
        ResultadoAcceso primero = conContador.intentar("ana@correo.com", "mala1");
        ResultadoAcceso segundo = conContador.intentar("ana@correo.com", "mala2");
        comprobar("el primer fallo avisa que quedan 2 intentos",
                primero.mensaje().contains("2 intentos"));
        comprobar("el segundo avisa que queda 1 intento",
                segundo.mensaje().contains("queda 1 intento"));
        ResultadoAcceso tercero = conContador.intentar("ana@correo.com", "mala3");
        comprobar("el tercero bloquea, y por " + ControlIntentosFallidos.SEGUNDOS_BLOQUEO + " s",
                tercero.estado() == ResultadoAcceso.Estado.BLOQUEADO
                        && tercero.segundos() == ControlIntentosFallidos.SEGUNDOS_BLOQUEO);

        conContador.liberarBloqueo();
        comprobar("tras liberar el bloqueo se puede reintentar",
                conContador.intentar("ana@correo.com", "otra mala").estado()
                        == ResultadoAcceso.Estado.RECHAZADO);

        System.out.println("Solo cuentan los fallos consecutivos");
        AutenticacionService contadorLimpio = new AutenticacionService(repositorio);
        contadorLimpio.intentar("ana@correo.com", "mala1");
        contadorLimpio.intentar("ana@correo.com", "mala2");
        comprobar("un acceso correcto reinicia el contador",
                contadorLimpio.intentar("ana@correo.com", "Clave123*").aceptado()
                        && contadorLimpio.intentar("ana@correo.com", "mala3").mensaje()
                                .contains("2 intentos"));
        comprobar("cada pantalla de acceso lleva su propio contador",
                new AutenticacionService(repositorio).intentar("ana@correo.com", "mala")
                        .mensaje().contains("2 intentos"));

        System.out.println("Reconocer a quien vuelve de Google");
        comprobar("encuentra la cuenta por su correo",
                servicio.reconocerPorCorreo("sofia@gmail.com") != null
                        && servicio.reconocerPorCorreo("nadie@gmail.com") == null);

        System.out.println(fallos == 0 ? "\nTODO CORRECTO" : "\n" + fallos + " FALLAS");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
