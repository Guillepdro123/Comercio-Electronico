package controller;

import aplicacion.acceso.AutenticacionService;
import aplicacion.cuenta.CuentaService;
import model.entity.Usuario;
import service.google.IAutenticadorExterno;
import service.google.PerfilExterno;
import view.auth.ILoginView;

/**
 * Controlador del caso de uso "Entrar con Google".
 *
 * <p><b>Por qué es un controlador aparte y no más código en
 * {@link LoginController}.</b> Comparten pantalla, pero no reglas: el Login
 * con contraseña cuenta intentos fallidos y compara con BCrypt; este no tiene
 * contraseña que adivinar, y en cambio puede crear una cuenta. Juntos, cada
 * cambio de uno obligaría a releer el otro.</p>
 *
 * <p><b>Reparto de responsabilidades:</b></p>
 * <ul>
 *   <li>{@link IAutenticadorExterno} habla con Google y devuelve un correo
 *       verificado. No sabe que existen cuentas de la aplicación.</li>
 *   <li>Este controlador decide qué hacer con ese correo: entrar en la cuenta
 *       que ya lo tiene, o crear una nueva con el rol que elija el usuario.</li>
 *   <li>{@link SesionController} abre el panel del rol y recuerda la sesión,
 *       exactamente como tras un Login con contraseña.</li>
 *   <li>La vista muestra la espera, pregunta el rol y enseña los errores.</li>
 * </ul>
 *
 * <p><b>Cuenta existente con contraseña.</b> Si el correo ya tiene una cuenta
 * creada con el formulario, se entra en ella. Es seguro porque Google
 * garantiza que quien entró controla ese correo (el servicio exige
 * {@code email_verified}), que es la misma prueba en la que se apoyaría una
 * recuperación de contraseña por correo.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class AccesoGoogleController {

    private final ILoginView vista;
    private final AutenticacionService autenticacion;
    private final CuentaService cuentas;
    private final SesionController sesion;
    private final IAutenticadorExterno google;

    /**
     * Conecta el botón de Google de la vista con este caso de uso. Si no se
     * construye (no hay credenciales de Google), el botón no aparece.
     *
     * @param vista       pantalla de Login (abstracción)
     * @param autenticacion caso de uso del acceso, para reconocer al que vuelve
     * @param cuentas     caso de uso de alta de cuentas
     * @param sesion      quien abre el panel del rol tras entrar
     * @param google      autenticador externo (abstracción)
     */
    public AccesoGoogleController(ILoginView vista, AutenticacionService autenticacion,
                                  CuentaService cuentas, SesionController sesion,
                                  IAutenticadorExterno google) {
        this.vista = vista;
        this.autenticacion = autenticacion;
        this.cuentas = cuentas;
        this.sesion = sesion;
        this.google = google;
        vista.activarAccesoGoogle(this::acceder);
    }

    /** Lleva al usuario al navegador y espera a que vuelva de Google. */
    private void acceder() {
        vista.ejecutarCancelable("Continúa en tu navegador...",
                google::autenticar, this::alVolverDeGoogle, google::cancelar);
    }

    /**
     * @param perfil identidad verificada por Google, o {@code null} si el
     *               usuario canceló (no es un error: no se muestra nada)
     */
    private void alVolverDeGoogle(PerfilExterno perfil) {
        if (perfil == null) {
            return;
        }
        vista.ejecutarEnSegundoPlano("Buscando tu cuenta...",
                () -> autenticacion.reconocerPorCorreo(perfil.correo()),
                existente -> {
                    if (existente != null) {
                        entrar(existente);
                        return;
                    }
                    crearCuenta(perfil);
                });
    }

    /**
     * Primera vez con este correo: se pregunta el rol y se crea la cuenta.
     *
     * <p>Elegir el rol es de la pantalla; crear la cuenta (identificación,
     * marca de cuenta sin contraseña y correo de bienvenida) es de
     * {@link CuentaService#crearConGoogle}, el mismo servicio que usa el
     * registro por formulario.</p>
     */
    private void crearCuenta(PerfilExterno perfil) {
        String rol = vista.elegirRol(perfil.nombre());
        if (rol == null) {
            return;
        }
        vista.ejecutarEnSegundoPlano("Creando tu cuenta...",
                () -> cuentas.crearConGoogle(perfil.id(), perfil.correo(), perfil.nombre(), rol),
                resultado -> {
                    if (!resultado.exitoso()) {
                        vista.mostrarError(resultado.error());
                        return;
                    }
                    entrar(resultado.usuario());
                });
    }

    /** Mismo cierre que el Login con contraseña: confirmación, transición y panel. */
    private void entrar(Usuario usuario) {
        vista.mostrarExito("¡Bienvenido, " + usuario.getNombres()
                + "! Has ingresado con tu cuenta de Google.");
        vista.mostrarTransicion(() -> {
            vista.cerrarVentana();
            sesion.abrir(usuario);
        });
    }
}
