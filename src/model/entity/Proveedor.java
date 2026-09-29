package model.entity;

/**
 * Usuario que publica y abastece productos en la plataforma.
 *
 * <p><b>Herencia:</b> extiende {@link Usuario} y agrega el NIT, dato tributario
 * que identifica a la empresa proveedora.</p>
 *
 * @author Ingeniería de Sistemas - Primer Incremento Funcional
 * @version 1.0
 */
public class Proveedor extends Usuario {

    /** Número de Identificación Tributaria de la empresa proveedora. */
    private String nitEmpresa;

    /** Constructor vacío requerido para instanciación flexible. */
    public Proveedor() {
        super();
    }

    /**
     * Constructor completo.
     *
     * @param identificacion documento del representante o del proveedor
     * @param nombres        razón social o nombres del contacto
     * @param correo         correo electrónico
     * @param password       contraseña de acceso
     * @param nitEmpresa     NIT de la empresa
     */
    public Proveedor(String identificacion, String nombres, String correo,
                     String password, String nitEmpresa) {
        super(identificacion, nombres, correo, password);
        this.nitEmpresa = nitEmpresa;
    }

    public String getNitEmpresa() {
        return nitEmpresa;
    }

    public void setNitEmpresa(String nitEmpresa) {
        this.nitEmpresa = nitEmpresa;
    }

    // ---------------------------------------------------------------------
    // Implementación del contrato polimórfico
    // ---------------------------------------------------------------------

    @Override
    public String getTipoCuenta() {
        return "Proveedor";
    }

    @Override
    public String getDatoEspecifico() {
        return nitEmpresa;
    }

    @Override
    public String getEtiquetaDatoEspecifico() {
        return "NIT de la empresa";
    }

    @Override
    public String getMensajeDesbloqueo() {
        return "Proveedor registrado correctamente";
    }
}
