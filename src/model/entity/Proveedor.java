package model.entity;

/**
 * Usuario que, además de comprar, publica y abastece productos en la
 * plataforma (doble rol).
 *
 * <p><b>Herencia:</b> extiende {@link Usuario} y agrega lo que lo hace
 * vendedor: el NIT y el nombre de la empresa o marca con la que aparecen sus
 * productos. Como también compra, tiene su propia dirección de envío.</p>
 *
 * @author Ingeniería de Sistemas - Primer Incremento Funcional
 * @version 2.0
 */
public class Proveedor extends Usuario {

    /** Número de Identificación Tributaria de la empresa proveedora. */
    private String nitEmpresa;

    /**
     * Nombre comercial con el que vende ("Supertecno"). Es lo que el comprador
     * ve en cada producto suyo. No va en el constructor por la misma razón que
     * el teléfono: las cuentas creadas antes no lo tenían.
     */
    private String nombreEmpresa = "";

    /** Dirección a la que llegan sus propias compras. */
    private String direccionEnvio = "";

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

    @Override
    public boolean puedeVender() {
        return true;
    }

    @Override
    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    @Override
    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa == null ? "" : nombreEmpresa;
    }

    @Override
    public String getDireccionEnvio() {
        return direccionEnvio;
    }

    @Override
    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio == null ? "" : direccionEnvio;
    }

    // ---------------------------------------------------------------------
    // Implementación del contrato polimórfico
    // ---------------------------------------------------------------------

    @Override
    public String getTipoCuenta() {
        return "Proveedor";
    }

    @Override
    public void setDatoEspecifico(String valor) {
        setNitEmpresa(valor);
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
