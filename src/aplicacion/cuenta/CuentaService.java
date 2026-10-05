package aplicacion.cuenta;

import aplicacion.seguridad.CifradoPassword;
import aplicacion.seguridad.PoliticaPassword;
import model.entity.Cliente;
import model.entity.Proveedor;
import model.entity.Usuario;
import model.repository.IUsuarioRepository;
import service.correo.INotificadorCuenta;

/**
 * Casos de uso de una cuenta: crearla (por formulario o con Google) y editar
 * su perfil.
 *
 * <p><b>Qué hace esta clase aquí.</b> Estas reglas vivían repartidas entre
 * {@code UsuarioController}, {@code PerfilController} y
 * {@code AccesoGoogleController}: qué datos son obligatorios, qué formato debe
 * tener un correo, cuándo se puede cambiar una contraseña y cómo se construye
 * un Cliente o un Proveedor. Tres pantallas distintas aplicando las mismas
 * reglas es exactamente donde esas reglas empiezan a divergir.</p>
 *
 * <p><b>La contraseña se cifra aquí</b>, antes de construir la entidad: el
 * texto en claro no llega nunca al modelo ni, por tanto, al repositorio.</p>
 *
 * <p><b>Bloquea</b> mientras habla con la base (buscar el correo, cifrar con
 * BCrypt, guardar): quien lo llame desde una interfaz gráfica debe hacerlo
 * fuera del hilo de eventos.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class CuentaService {

    /** Formato mínimo aceptable de un correo. */
    private static final String PATRON_CORREO = "^[\\w.+-]+@[\\w-]+\\.[\\w.-]{2,}$";

    /** Longitudes aceptables de un teléfono, contando solo sus dígitos. */
    private static final int TELEFONO_MINIMO = 7;
    private static final int TELEFONO_MAXIMO = 15;

    /** Dígitos aceptables de una cédula. */
    private static final int CEDULA_MINIMA = 5;
    private static final int CEDULA_MAXIMA = 12;
    /** Por debajo de esto una dirección no alcanza para entregar nada. */
    private static final int DIRECCION_MINIMA = 8;
    /** Largo máximo del nombre comercial: aparece en cada tarjeta del catálogo. */
    private static final int EMPRESA_MAXIMA = 40;

    /**
     * Prefijo de la identificación de una cuenta creada con Google. La
     * identificación es única en la base; el identificador de Google también
     * lo es y no cambia nunca, así que con el prefijo no puede chocar con un
     * número de documento real.
     */
    private static final String PREFIJO_GOOGLE = "google-";

    private final IUsuarioRepository repositorio;
    private final INotificadorCuenta notificador;
    private final PoliticaPassword politica = new PoliticaPassword();
    /** Único punto que conoce BCrypt; ver {@link CifradoPassword}. */
    private final CifradoPassword cifrado = new CifradoPassword();

    /**
     * @param repositorio almacén de usuarios (abstracción)
     * @param notificador correo de bienvenida (abstracción)
     */
    public CuentaService(IUsuarioRepository repositorio, INotificadorCuenta notificador) {
        this.repositorio = repositorio;
        this.notificador = notificador;
    }

    // ---------------------------------------------------------------------
    // Alta de cuentas
    // ---------------------------------------------------------------------

    /**
     * Crea una cuenta con los datos del formulario de registro.
     *
     * <p><b>Correo único, en dos barreras.</b> Aquí se comprueba para poder
     * decir exactamente qué falló, y antes de cifrar, porque BCrypt es lento a
     * propósito y no tiene sentido gastarlo en un registro que se va a
     * rechazar. El repositorio vuelve a comprobarlo al registrar (y en MongoDB,
     * además, un índice único), así que esta no es la única barrera: es la que
     * da el mensaje.</p>
     *
     * @param datos lo que se escribió en el formulario, sin validar
     * @return la cuenta creada, o el mensaje de error
     */
    public ResultadoCuenta registrar(SolicitudRegistro datos) {
        String error = validarRegistro(datos);
        if (error != null) {
            return ResultadoCuenta.fallo(error);
        }
        if (repositorio.buscarPorCorreo(datos.correo()) != null) {
            return ResultadoCuenta.fallo("Este correo ya está registrado en el sistema.");
        }

        Usuario usuario = construir(datos.tipoCuenta(), datos.identificacion(), datos.nombres(),
                datos.correo(), cifrado.cifrar(datos.password()), datos.datoAdicional());
        if (usuario.puedeVender()) {
            usuario.setNombreEmpresa(datos.nombreEmpresa().trim());
        }
        if (!repositorio.registrar(usuario)) {
            // El correo ya se comprobó arriba, así que casi siempre el motivo
            // es la identificación. Si otro equipo registró el mismo correo en
            // este mismo instante, el repositorio también devuelve false: por
            // eso el mensaje nombra las dos posibilidades.
            return ResultadoCuenta.fallo("No fue posible registrar: la identificación "
                    + datos.identificacion() + " o el correo ya están registrados.");
        }
        // Encolado, no enviado: el notificador que ensambla app.Main envía en
        // su propio hilo, así que esta llamada vuelve al instante.
        notificador.notificarBienvenida(usuario);
        return ResultadoCuenta.hecho(usuario);
    }

    /**
     * Crea la cuenta de quien entra por primera vez con Google.
     *
     * <p>No tiene contraseña propia: se guarda
     * {@link CifradoPassword#MARCA_CUENTA_GOOGLE}, que {@code CifradoPassword}
     * garantiza que no coincide con nada, de modo que la cuenta no se puede
     * abrir con el formulario de contraseña. La cédula, la dirección, el NIT y
     * la empresa quedan vacíos: la promesa de entrar con Google es no rellenar
     * un formulario. Se exigen cuando hacen falta —la cédula y la dirección al
     * comprar ({@link #completarDatosEnvio}), la empresa al abrir la tienda— y
     * mientras falten, el perfil lo avisa.</p>
     *
     * @param idExterno identificador de la cuenta en Google, que no cambia
     * @param correo    correo ya verificado por Google
     * @param nombre    nombre para mostrar
     * @param rol       {@link TipoCuenta#CLIENTE} o {@link TipoCuenta#PROVEEDOR}
     * @return la cuenta creada, o el mensaje de error
     */
    public ResultadoCuenta crearConGoogle(String idExterno, String correo, String nombre,
                                          String rol) {
        Usuario usuario = construir(rol, PREFIJO_GOOGLE + idExterno, nombre, correo,
                CifradoPassword.MARCA_CUENTA_GOOGLE, "");
        if (!repositorio.registrar(usuario)) {
            return ResultadoCuenta.fallo("Esta cuenta de Google o este correo ya están registrados.");
        }
        notificador.notificarBienvenida(usuario);
        return ResultadoCuenta.hecho(usuario);
    }

    // ---------------------------------------------------------------------
    // Edición del perfil
    // ---------------------------------------------------------------------

    /**
     * Aplica los cambios del perfil de quien tiene la sesión abierta.
     *
     * <p><b>Se valida entero antes de tocar la cuenta.</b> Si se fuera
     * asignando campo por campo y el último fallara, la cuenta quedaría a
     * medio cambiar; el mismo criterio que usa la compra antes de descontar
     * stock.</p>
     *
     * @param usuario cuenta con la sesión abierta, la única editable
     * @param cambios lo que se escribió en el formulario, sin validar
     * @return la cuenta actualizada, o el mensaje de error
     */
    public ResultadoCuenta actualizarPerfil(Usuario usuario, CambiosCuenta cambios) {
        String error = validarDatos(usuario, cambios);
        if (error == null) {
            error = validarPassword(usuario, cambios);
        }
        if (error != null) {
            return ResultadoCuenta.fallo(error);
        }

        usuario.setNombres(cambios.nombres().trim());
        usuario.setCorreo(cambios.correo().trim());
        usuario.setTelefono(cambios.telefono().trim());
        if (usuario.getCedula().isEmpty() && !vacio(cambios.cedula())) {
            usuario.setCedula(cambios.cedula().trim());
        }
        usuario.setDireccionEnvio(texto(cambios.direccionEnvio()));
        if (usuario.puedeVender()) {
            usuario.setDatoEspecifico(texto(cambios.nit()));
            usuario.setNombreEmpresa(texto(cambios.nombreEmpresa()));
        }
        if (!cambios.sinCambioDePassword()) {
            usuario.setPassword(cifrado.cifrar(cambios.passwordNueva()));
        }

        if (!repositorio.actualizar(usuario)) {
            return ResultadoCuenta.fallo("No se pudieron guardar los cambios. Revisa tu correo.");
        }
        return ResultadoCuenta.hecho(usuario);
    }

    /**
     * Completa la cédula y la dirección de una cuenta que va a comprar sin
     * tenerlas (típicamente, una cuenta creada con Google).
     *
     * <p>Si la cuenta ya tenía cédula, la que llegue se ignora: la cédula no
     * se cambia desde la compra, solo se da por primera vez.</p>
     *
     * @param usuario   cuenta con la sesión abierta
     * @param cedula    cédula escrita, sin validar
     * @param direccion dirección escrita, sin validar
     * @return la cuenta actualizada, o el mensaje de error
     */
    public ResultadoCuenta completarDatosEnvio(Usuario usuario, String cedula, String direccion) {
        boolean pideCedula = usuario.getCedula().isEmpty();
        String error = pideCedula ? validarCedula(usuario, cedula) : null;
        if (error == null && (vacio(direccion) || direccion.trim().length() < DIRECCION_MINIMA)) {
            error = "Escribe una dirección de envío completa (calle, número y ciudad).";
        }
        if (error != null) {
            return ResultadoCuenta.fallo(error);
        }
        if (pideCedula) {
            usuario.setCedula(cedula.trim());
        }
        usuario.setDireccionEnvio(direccion.trim());
        if (!repositorio.actualizar(usuario)) {
            return ResultadoCuenta.fallo("No se pudieron guardar tus datos. Inténtalo de nuevo.");
        }
        return ResultadoCuenta.hecho(usuario);
    }

    /**
     * @param usuario cuenta de un proveedor
     * @return qué le falta para poder gestionar su tienda, o {@code null} si
     *         nada
     */
    public String faltanteParaVender(Usuario usuario) {
        if (usuario.getNombreEmpresa().isBlank()) {
            return "Para gestionar tu tienda, escribe el nombre de tu empresa o marca.";
        }
        if (vacio(usuario.getDatoEspecifico())) {
            return "Para gestionar tu tienda, escribe el NIT de tu empresa.";
        }
        return null;
    }

    /**
     * @param usuario cuenta con la sesión abierta
     * @return aviso de lo que le falta al perfil para poder comprar, o
     *         {@code null} si está completo
     */
    public String avisoDePerfil(Usuario usuario) {
        if (usuario.datosDeEnvioCompletos()) {
            return null;
        }
        if (usuario.getCedula().isEmpty()) {
            return "Completa tu cédula y tu dirección: sin ellas no podrás comprar.";
        }
        return "Completa tu dirección de envío: sin ella no podrás comprar.";
    }

    // ---------------------------------------------------------------------
    // Validación
    // ---------------------------------------------------------------------

    /**
     * @return mensaje de error, o {@code null} si el formulario está bien —
     *         mismo patrón que {@link PoliticaPassword#validar(String)}
     */
    private String validarRegistro(SolicitudRegistro datos) {
        if (vacio(datos.identificacion())) {
            return "La identificación es obligatoria.";
        }
        if (!datos.identificacion().matches("\\d+")) {
            return "La identificación debe contener solo números.";
        }
        if (vacio(datos.nombres())) {
            return "Los nombres son obligatorios.";
        }
        if (vacio(datos.correo())) {
            return "El correo electrónico es obligatorio.";
        }
        if (!datos.correo().matches(PATRON_CORREO)) {
            return "El formato del correo no es válido. Ejemplo: usuario@dominio.com";
        }
        String errorPassword = politica.validar(datos.password());
        if (errorPassword != null) {
            return errorPassword;
        }
        boolean esProveedor = TipoCuenta.PROVEEDOR.equals(datos.tipoCuenta());
        if (vacio(datos.datoAdicional())) {
            return esProveedor
                    ? "El NIT de la empresa es obligatorio."
                    : "La dirección de envío es obligatoria.";
        }
        if (esProveedor) {
            return validarEmpresa(datos.nombreEmpresa());
        }
        return null;
    }

    /**
     * @return mensaje de error, o {@code null} si el nombre de empresa sirve
     */
    private String validarEmpresa(String nombreEmpresa) {
        if (vacio(nombreEmpresa)) {
            return "El nombre de la empresa o marca es obligatorio.";
        }
        if (nombreEmpresa.trim().length() > EMPRESA_MAXIMA) {
            return "El nombre de la empresa admite hasta " + EMPRESA_MAXIMA + " caracteres.";
        }
        return null;
    }

    /**
     * Cédula colombiana: solo dígitos, sin puntos, y que no la tenga otra
     * cuenta (dos personas no facturan con el mismo documento).
     *
     * @return mensaje de error, o {@code null} si la cédula sirve
     */
    private String validarCedula(Usuario usuario, String cedula) {
        if (vacio(cedula)) {
            return "La cédula es obligatoria para despachar y facturar tu pedido.";
        }
        String limpia = cedula.trim();
        if (!limpia.matches("\\d{" + CEDULA_MINIMA + "," + CEDULA_MAXIMA + "}")) {
            return "La cédula debe tener entre " + CEDULA_MINIMA + " y " + CEDULA_MAXIMA
                    + " dígitos, sin puntos ni espacios.";
        }
        if (repositorio.cedulaEnUso(limpia, usuario.getIdentificacion())) {
            return "Esa cédula ya está registrada en otra cuenta.";
        }
        return null;
    }

    /** Comprueba los datos de contacto del perfil. */
    private String validarDatos(Usuario usuario, CambiosCuenta cambios) {
        if (vacio(cambios.nombres())) {
            return "El nombre no puede quedar vacío.";
        }
        if (vacio(cambios.correo())) {
            return "El correo no puede quedar vacío.";
        }
        if (!pareceCorreo(cambios.correo())) {
            return "Ese correo no tiene un formato válido.";
        }
        if (correoDeOtraCuenta(usuario, cambios.correo())) {
            return "Ese correo ya está registrado en otra cuenta.";
        }
        if (!vacio(cambios.telefono()) && !pareceTelefono(cambios.telefono())) {
            return "El teléfono debe tener entre " + TELEFONO_MINIMO + " y "
                    + TELEFONO_MAXIMO + " dígitos.";
        }
        if (usuario.getCedula().isEmpty() && !vacio(cambios.cedula())) {
            String errorCedula = validarCedula(usuario, cambios.cedula());
            if (errorCedula != null) {
                return errorCedula;
            }
        }
        // Un Cliente se registró con su dirección y no puede quedarse sin
        // ella; un Proveedor solo la necesita para comprar, y se la pedirá la
        // compra si falta.
        if (!usuario.puedeVender() && vacio(cambios.direccionEnvio())) {
            return "La dirección de envío no puede quedar vacía.";
        }
        if (usuario.puedeVender()) {
            if (vacio(cambios.nit())) {
                return "El NIT de la empresa no puede quedar vacío.";
            }
            return validarEmpresa(cambios.nombreEmpresa());
        }
        return null;
    }

    /**
     * Comprueba el bloque de contraseña.
     *
     * <p>Los tres campos vacíos significan "no la cambio". Si se tocó alguno,
     * se exigen los tres y se pide la contraseña actual: sin eso, cualquiera
     * que se siente frente a una sesión abierta podría cambiarla.</p>
     */
    private String validarPassword(Usuario usuario, CambiosCuenta cambios) {
        if (cambios.sinCambioDePassword()) {
            return null;
        }
        if (cifrado.esCuentaGoogle(usuario.getPassword())) {
            // No hay contraseña actual que comprobar, y ponerle una sin esa
            // comprobación dejaría cambiarla a cualquiera frente a la sesión.
            return "Tu cuenta entra con Google y no usa contraseña.";
        }
        if (vacio(cambios.passwordActual())) {
            return "Escribe tu contraseña actual para poder cambiarla.";
        }
        if (!cifrado.coincide(cambios.passwordActual(), usuario.getPassword())) {
            return "La contraseña actual no es correcta.";
        }
        String errorPolitica = politica.validar(cambios.passwordNueva());
        if (errorPolitica != null) {
            return errorPolitica;
        }
        if (!cambios.passwordNueva().equals(cambios.passwordConfirmar())) {
            return "La nueva contraseña y su repetición no coinciden.";
        }
        if (cambios.passwordNueva().equals(cambios.passwordActual())) {
            return "La nueva contraseña debe ser distinta de la actual.";
        }
        return null;
    }

    /**
     * @return {@code true} si ese correo lo tiene registrado alguien más
     *
     * <p><b>Se compara por identificación, no por referencia.</b> El almacén
     * en memoria devuelve la misma instancia que se está editando, pero un
     * repositorio con base de datos devuelve un objeto nuevo en cada lectura:
     * con {@code !=} el usuario no podría guardar ni conservando su propio
     * correo.</p>
     */
    private boolean correoDeOtraCuenta(Usuario usuario, String correo) {
        Usuario duenno = repositorio.buscarPorCorreo(correo.trim());
        return duenno != null
                && !duenno.getIdentificacion().equals(usuario.getIdentificacion());
    }

    /**
     * Comprobación deliberadamente simple: un nombre, una arroba, un dominio
     * con punto. No se valida contra la norma completa del correo electrónico
     * porque este es un formulario de escritorio, no un registrador de
     * dominios, y una expresión exhaustiva rechaza direcciones legítimas.
     */
    private boolean pareceCorreo(String correo) {
        String limpio = correo.trim();
        int arroba = limpio.indexOf('@');
        int punto = limpio.lastIndexOf('.');
        return arroba > 0 && punto > arroba + 1 && punto < limpio.length() - 1
                && !limpio.contains(" ");
    }

    /** @return {@code true} si solo trae dígitos (y separadores) en cantidad razonable */
    private boolean pareceTelefono(String telefono) {
        String digitos = telefono.replaceAll("[\\s()+-]", "");
        return digitos.chars().allMatch(Character::isDigit)
                && digitos.length() >= TELEFONO_MINIMO
                && digitos.length() <= TELEFONO_MAXIMO;
    }

    /**
     * Fábrica de entidades según el rol.
     *
     * <p>Polimorfismo: la variable es del tipo base y el objeto es de la
     * subclase concreta, comparando texto y sin {@code instanceof}.</p>
     */
    private Usuario construir(String tipoCuenta, String identificacion, String nombres,
                              String correo, String password, String datoAdicional) {
        if (TipoCuenta.PROVEEDOR.equals(tipoCuenta)) {
            return new Proveedor(identificacion, nombres, correo, password, datoAdicional);
        }
        return new Cliente(identificacion, nombres, correo, password, datoAdicional);
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
