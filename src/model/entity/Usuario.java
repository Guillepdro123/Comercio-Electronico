package model.entity;

/**
 * Clase base abstracta del modelo de dominio.
 *
 * <p><b>Abstracción:</b> representa el concepto genérico de "usuario" de la
 * plataforma. No tiene sentido instanciarla por sí sola: siempre se registra un
 * Cliente o un Proveedor, por eso se declara {@code abstract}.</p>
 *
 * <p><b>Encapsulamiento:</b> todos los atributos son {@code private} y el acceso
 * se realiza exclusivamente mediante getters y setters, lo que permite
 * incorporar validaciones o cifrado en el futuro sin romper a los clientes de
 * la clase.</p>
 *
 * <p><b>Polimorfismo:</b> los métodos abstractos declarados al final del archivo
 * obligan a cada subclase a definir su propio comportamiento. El controlador y
 * el repositorio trabajan contra este tipo y nunca necesitan preguntar con
 * {@code instanceof} de qué clase concreta se trata.</p>
 *
 * @author Ingeniería de Sistemas - Primer Incremento Funcional
 * @version 1.0
 */
public abstract class Usuario {

    // ---------------------------------------------------------------------
    // Atributos encapsulados
    // ---------------------------------------------------------------------

    /** Documento de identidad o identificador único del usuario. */
    private String identificacion;

    /** Nombres y apellidos del usuario. */
    private String nombres;

    /** Correo electrónico de contacto y de acceso. */
    private String correo;

    /** Contraseña de acceso (en un incremento posterior debe almacenarse con hash). */
    private String password;

    /**
     * Teléfono de contacto.
     *
     * <p><b>No va en el constructor a propósito.</b> El registro no lo pide
     * —se mantiene tan corto como estaba— y añadirlo habría obligado a tocar
     * todas las llamadas existentes. Se rellena después, desde la edición de
     * perfil, y hasta entonces vale cadena vacía.</p>
     */
    private String telefono = "";

    /**
     * Cédula o documento de identidad de la persona.
     *
     * <p><b>No es lo mismo que {@link #identificacion}.</b> La identificación
     * es la llave de la cuenta y no cambia nunca; en una cuenta del formulario
     * coincide con la cédula, pero en una cuenta de Google es un identificador
     * técnico ("google-..."). Por eso la cédula vive aparte: se pide al
     * comprar por primera vez y no obliga a cambiar la llave.</p>
     */
    private String cedula = "";

    // ---------------------------------------------------------------------
    // Constructores
    // ---------------------------------------------------------------------

    /**
     * Constructor vacío. Se declara {@code protected} porque solo las subclases
     * deben poder utilizarlo.
     */
    protected Usuario() {
    }

    /**
     * Constructor con los datos comunes a todo usuario de la plataforma.
     *
     * @param identificacion documento o identificador único
     * @param nombres        nombres y apellidos
     * @param correo         correo electrónico
     * @param password       contraseña de acceso
     */
    protected Usuario(String identificacion, String nombres, String correo, String password) {
        this.identificacion = identificacion;
        this.nombres = nombres;
        this.correo = correo;
        this.password = password;
    }

    // ---------------------------------------------------------------------
    // Getters y setters
    // ---------------------------------------------------------------------

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return la cédula registrada; si no hay ninguna pero la identificación
     *         es un número (cuentas del formulario, incluidas las creadas antes
     *         de existir este campo), esa es la cédula. Vacío en una cuenta de
     *         Google que todavía no la ha dado.
     */
    public String getCedula() {
        if (cedula != null && !cedula.isBlank()) {
            return cedula;
        }
        return identificacion != null && identificacion.matches("\\d+") ? identificacion : "";
    }

    public void setCedula(String cedula) {
        this.cedula = cedula == null ? "" : cedula;
    }

    /**
     * Regla de negocio de la compra: sin cédula y sin dirección no se puede
     * despachar ni facturar un pedido.
     *
     * @return {@code true} si la cuenta tiene los dos datos
     */
    public boolean datosDeEnvioCompletos() {
        String direccion = getDireccionEnvio();
        return !getCedula().isBlank() && direccion != null && !direccion.isBlank();
    }

    // ---------------------------------------------------------------------
    // Rol de vendedor
    // ---------------------------------------------------------------------

    /**
     * Si la cuenta puede vender, además de comprar. Todo usuario compra; solo
     * el Proveedor tiene además una tienda (modelo de doble rol).
     *
     * @return {@code false} por defecto; {@link Proveedor} lo redefine
     */
    public boolean puedeVender() {
        return false;
    }

    /**
     * @return nombre de la empresa o marca con la que vende; vacío en quien
     *         solo compra
     */
    public String getNombreEmpresa() {
        return "";
    }

    /**
     * Cambia el nombre de la empresa. Solo tiene sentido en quien
     * {@link #puedeVender()}: llamarlo en una cuenta que no vende es un error
     * de programación, no una situación del dominio, y por eso falla en vez de
     * ignorarse en silencio.
     *
     * @param nombreEmpresa nuevo nombre de la empresa o marca
     */
    public void setNombreEmpresa(String nombreEmpresa) {
        throw new UnsupportedOperationException(getTipoCuenta() + " no tiene empresa.");
    }

    // ---------------------------------------------------------------------
    // Contrato polimórfico: cada subclase responde a su manera
    // ---------------------------------------------------------------------

    /**
     * Nombre legible del tipo de cuenta ("Cliente" o "Proveedor").
     *
     * @return etiqueta del tipo de cuenta
     */
    public abstract String getTipoCuenta();

    /**
     * Dato propio de la subclase: dirección de envío para el Cliente o NIT para
     * el Proveedor. Permite que capas superiores muestren la información
     * específica sin conocer la clase concreta.
     *
     * @return valor del atributo especializado
     */
    public abstract String getDatoEspecifico();

    /**
     * Cambia el dato propio de la subclase.
     *
     * <p>Es el reverso de {@link #getDatoEspecifico()} y existe por el mismo
     * motivo: permite que la edición de perfil modifique la dirección de envío
     * de un Cliente o el NIT de un Proveedor sin preguntar de qué clase se
     * trata. Sin él habría que recurrir a {@code instanceof}, que en este
     * proyecto se evita justamente con este par de métodos.</p>
     *
     * @param valor nuevo valor del atributo especializado
     */
    public abstract void setDatoEspecifico(String valor);

    /**
     * Etiqueta del dato especializado, útil para construir mensajes o reportes
     * genéricos ("Dirección de envío" / "NIT de la empresa").
     *
     * @return nombre legible del atributo especializado
     */
    public abstract String getEtiquetaDatoEspecifico();

    /**
     * Dirección a la que se envían las compras de esta cuenta. Todo usuario
     * compra, también el Proveedor (doble rol), así que todos la tienen; en un
     * Cliente es además su dato propio del registro.
     *
     * @return dirección de envío, o vacío si todavía no la dio
     */
    public abstract String getDireccionEnvio();

    /**
     * @param direccionEnvio nueva dirección de envío
     */
    public abstract void setDireccionEnvio(String direccionEnvio);

    /**
     * Mensaje de dominio que informa qué capacidades habilita el registro de
     * este tipo de usuario dentro de la plataforma.
     *
     * @return mensaje de confirmación del incremento desbloqueado
     */
    public abstract String getMensajeDesbloqueo();

    // ---------------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------------

    /**
     * Representación textual del usuario. Se apoya en {@link #getTipoCuenta()},
     * por lo que su salida cambia según la subclase (polimorfismo).
     */
    @Override
    public String toString() {
        return String.format("[%s] %s - %s <%s>",
                getTipoCuenta(), identificacion, nombres, correo);
    }
}
