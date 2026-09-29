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

    public void setPassword(String password) {
        this.password = password;
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
     * Etiqueta del dato especializado, útil para construir mensajes o reportes
     * genéricos ("Dirección de envío" / "NIT de la empresa").
     *
     * @return nombre legible del atributo especializado
     */
    public abstract String getEtiquetaDatoEspecifico();

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
