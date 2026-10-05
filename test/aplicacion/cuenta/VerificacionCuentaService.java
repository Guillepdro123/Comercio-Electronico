package aplicacion.cuenta;

import aplicacion.seguridad.CifradoPassword;
import java.util.ArrayList;
import java.util.List;
import model.entity.Proveedor;
import model.entity.Usuario;
import model.repository.memoria.UsuarioRepositoryImpl;
import service.correo.INotificadorCuenta;

/**
 * Comprobación automática de {@link CuentaService}.
 *
 * <p>Java puro, sin JUnit y sin abrir ninguna ventana: comprueba el registro,
 * la edición del perfil y el alta con Google, que antes estaban repartidos
 * entre tres controladores y solo se podían probar a mano.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class VerificacionCuentaService {

    private static int fallos = 0;

    private VerificacionCuentaService() {
    }

    /** Notificador de prueba: anota a quién se dio la bienvenida. */
    private static final class Bienvenidas implements INotificadorCuenta {
        private final List<String> correos = new ArrayList<>();

        @Override
        public boolean notificarBienvenida(Usuario usuario) {
            correos.add(usuario.getCorreo());
            return true;
        }
    }

    private static void comprobar(String que, boolean ok) {
        System.out.println((ok ? "  OK    " : "  FALLA ") + que);
        if (!ok) {
            fallos++;
        }
    }

    private static SolicitudRegistro registro(String identificacion, String correo,
                                              String password) {
        return new SolicitudRegistro(identificacion, "Ana Pérez", correo, password,
                "Calle 1 # 2-3", TipoCuenta.CLIENTE, "");
    }

    private static CambiosCuenta cambios(String correo, String actual, String nueva,
                                         String confirmar) {
        return new CambiosCuenta("Ana Pérez", correo, "3001234567", "", "Calle 9", "", "", actual,
                nueva, confirmar);
    }

    /**
     * @param args no se usan
     */
    public static void main(String[] args) {
        UsuarioRepositoryImpl repositorio = new UsuarioRepositoryImpl();
        Bienvenidas correos = new Bienvenidas();
        CuentaService servicio = new CuentaService(repositorio, correos);

        System.out.println("Registro por formulario");
        comprobar("rechaza identificación con letras",
                servicio.registrar(registro("12A", "ana@correo.com", "Clave123*")).error()
                        .contains("solo números"));
        comprobar("rechaza un correo mal formado",
                servicio.registrar(registro("12", "ana(arroba)correo", "Clave123*")).error()
                        .contains("formato del correo"));
        comprobar("rechaza una contraseña que no cumple la política",
                !servicio.registrar(registro("12", "ana@correo.com", "corta")).exitoso());
        comprobar("nada de eso creó cuentas ni envió bienvenidas",
                repositorio.contar() == 0 && correos.correos.isEmpty());

        ResultadoCuenta alta = servicio.registrar(registro("12", "ana@correo.com", "Clave123*"));
        comprobar("un registro válido crea la cuenta", alta.exitoso() && repositorio.contar() == 1);
        comprobar("la contraseña se guarda cifrada, nunca en claro",
                alta.usuario().getPassword().startsWith("$2")
                        && !alta.usuario().getPassword().contains("Clave123*"));
        comprobar("envía la bienvenida", correos.correos.equals(List.of("ana@correo.com")));
        comprobar("el correo repetido se rechaza con su mensaje",
                "Este correo ya está registrado en el sistema.".equals(
                        servicio.registrar(registro("99", "ANA@correo.com", "Clave123*")).error()));
        comprobar("la identificación repetida también",
                servicio.registrar(registro("12", "otra@correo.com", "Clave123*")).error()
                        .contains("12"));

        System.out.println("Alta con Google");
        ResultadoCuenta conGoogle = servicio.crearConGoogle("901", "sofia@gmail.com",
                "Sofía Ramírez", TipoCuenta.PROVEEDOR);
        comprobar("crea la cuenta con el rol elegido",
                conGoogle.exitoso() && conGoogle.usuario() instanceof Proveedor);
        comprobar("sin contraseña propia y con identificación google-<id>",
                CifradoPassword.MARCA_CUENTA_GOOGLE.equals(conGoogle.usuario().getPassword())
                        && "google-901".equals(conGoogle.usuario().getIdentificacion()));
        comprobar("repetir la misma cuenta de Google se rechaza",
                !servicio.crearConGoogle("901", "sofia@gmail.com", "Sofía", TipoCuenta.CLIENTE)
                        .exitoso());

        System.out.println("Edición del perfil");
        Usuario ana = alta.usuario();
        comprobar("rechaza dejar el nombre vacío",
                servicio.actualizarPerfil(ana, new CambiosCuenta("  ", "ana@correo.com", "",
                        "", "Calle 9", "", "", "", "", "")).error().contains("nombre"));
        comprobar("rechaza un teléfono de 3 dígitos",
                servicio.actualizarPerfil(ana, new CambiosCuenta("Ana", "ana@correo.com", "123",
                        "", "Calle 9", "", "", "", "", "")).error().contains("teléfono"));
        comprobar("rechaza un correo que ya tiene otra cuenta",
                servicio.actualizarPerfil(ana, cambios("sofia@gmail.com", "", "", ""))
                        .error().contains("otra cuenta"));
        comprobar("acepta conservar el correo propio",
                servicio.actualizarPerfil(ana, cambios("ana@correo.com", "", "", "")).exitoso());

        comprobar("cambiar contraseña exige la actual correcta",
                servicio.actualizarPerfil(ana, cambios("ana@correo.com", "malísima", "Nueva123*",
                        "Nueva123*")).error().contains("actual no es correcta"));
        comprobar("la nueva y su repetición deben coincidir",
                servicio.actualizarPerfil(ana, cambios("ana@correo.com", "Clave123*", "Nueva123*",
                        "Otra123*")).error().contains("no coinciden"));
        comprobar("cambia la contraseña cuando todo está bien",
                servicio.actualizarPerfil(ana, cambios("ana@correo.com", "Clave123*", "Nueva123*",
                        "Nueva123*")).exitoso()
                        && new CifradoPassword().coincide("Nueva123*", ana.getPassword()));
        comprobar("una cuenta de Google no puede ponerse contraseña",
                servicio.actualizarPerfil(conGoogle.usuario(),
                        new CambiosCuenta("Sofía", "sofia@gmail.com", "", "", "", "NIT-1",
                                "Sofi Store", "x", "Nueva123*", "Nueva123*")).error().contains("Google"));

        System.out.println(fallos == 0 ? "\nTODO CORRECTO" : "\n" + fallos + " FALLAS");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
