package model.entity;

/**
 * Usuario que compra en la plataforma.
 *
 * <p><b>Herencia:</b> reutiliza identificación, nombres, correo y password de
 * {@link Usuario} y añade únicamente lo que lo diferencia: la dirección de
 * envío de sus pedidos.</p>
 *
 * @author Ingeniería de Sistemas - Primer Incremento Funcional
 * @version 1.0
 */
public class Cliente extends Usuario {

    /** Dirección física donde se entregan los pedidos del cliente. */
    private String direccionEnvio;

    /** Constructor vacío requerido para instanciación flexible. */
    public Cliente() {
        super();
    }

    /**
     * Constructor completo.
     *
     * @param identificacion documento del cliente
     * @param nombres        nombres y apellidos
     * @param correo         correo electrónico
     * @param password       contraseña de acceso
     * @param direccionEnvio dirección de entrega de los pedidos
     */
    public Cliente(String identificacion, String nombres, String correo,
                   String password, String direccionEnvio) {
        super(identificacion, nombres, correo, password);
        this.direccionEnvio = direccionEnvio;
    }

    public String getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    // ---------------------------------------------------------------------
    // Implementación del contrato polimórfico
    // ---------------------------------------------------------------------

    @Override
    public String getTipoCuenta() {
        return "Cliente";
    }

    @Override
    public String getDatoEspecifico() {
        return direccionEnvio;
    }

    @Override
    public String getEtiquetaDatoEspecifico() {
        return "Dirección de envío";
    }

    @Override
    public String getMensajeDesbloqueo() {
        return "Cliente registrado correctamente";
    }
}
