package controller;

import aplicacion.cuenta.CambiosCuenta;
import aplicacion.cuenta.CuentaService;
import aplicacion.cuenta.ResultadoCuenta;
import model.entity.Usuario;
import view.auth.CambiosPerfil;
import view.auth.DatosPerfil;
import view.auth.IPerfilView;

/**
 * Controlador de la pantalla "Editar perfil": carga los datos actuales,
 * entrega los cambios al caso de uso y muestra el resultado.
 *
 * <p><b>Aquí no hay reglas de negocio.</b> Qué correo es válido, si ese correo
 * ya lo tiene otra cuenta, cuándo se puede cambiar la contraseña y el cifrado
 * son de {@link CuentaService}, el mismo servicio que usa el registro: así las
 * dos pantallas no pueden aplicar reglas distintas a los mismos datos.</p>
 *
 * <p><b>La pantalla no se cierra si hay error.</b> El formulario debe seguir
 * abierto con lo ya escrito, y por eso el resultado se muestra sin cerrar
 * salvo que todo haya ido bien.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 2.0
 */
public class PerfilController {

    private final IPerfilView vista;
    private final CuentaService cuentas;
    private final Usuario usuario;

    /**
     * @param vista   pantalla de edición (abstracción)
     * @param cuentas caso de uso de la cuenta
     * @param usuario usuario con la sesión abierta; es el único que se puede
     *                editar desde aquí
     */
    public PerfilController(IPerfilView vista, CuentaService cuentas, Usuario usuario) {
        this.vista = vista;
        this.cuentas = cuentas;
        this.usuario = usuario;
        vista.alGuardar(this::guardar);
    }

    /**
     * Carga los datos actuales y muestra la pantalla. Si al perfil le falta
     * algo para comprar, la pantalla lo avisa.
     */
    public void abrir() {
        abrir(cuentas.avisoDePerfil(usuario));
    }

    /**
     * Igual que {@link #abrir()}, con un aviso concreto: lo usa "Gestionar
     * tienda" cuando a un proveedor le falta el nombre de su empresa.
     *
     * @param aviso qué debe completar, o {@code null} si nada
     */
    public void abrir(String aviso) {
        boolean sinCedula = usuario.getCedula().isEmpty();
        vista.mostrarDatos(new DatosPerfil(
                usuario.getNombres(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getCedula(),
                sinCedula,
                usuario.getDireccionEnvio(),
                usuario.puedeVender(),
                usuario.puedeVender() ? usuario.getDatoEspecifico() : "",
                usuario.getNombreEmpresa(),
                aviso));
        vista.abrir();
    }

    /** Entrega los cambios al caso de uso y cuenta cómo salió. */
    private void guardar(CambiosPerfil cambios) {
        ResultadoCuenta resultado = cuentas.actualizarPerfil(usuario, aCambiosCuenta(cambios));
        if (!resultado.exitoso()) {
            vista.mostrarError(resultado.error());
            return;
        }
        vista.cerrar();
        vista.mostrarExito("Listo, " + resultado.usuario().getNombres()
                + ". Tus datos quedaron actualizados.");
    }

    /** Pasa el registro de la vista al de la aplicación, sin convertir nada. */
    private CambiosCuenta aCambiosCuenta(CambiosPerfil cambios) {
        return new CambiosCuenta(cambios.nombres(), cambios.correo(), cambios.telefono(),
                cambios.cedula(), cambios.direccionEnvio(), cambios.nit(),
                cambios.nombreEmpresa(), cambios.passwordActual(),
                cambios.passwordNueva(), cambios.passwordConfirmar());
    }
}
