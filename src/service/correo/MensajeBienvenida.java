package service.correo;

import model.entity.Usuario;

/**
 * Texto del correo de bienvenida, en HTML para Resend y en texto plano para
 * la consola.
 *
 * <p>Mismo papel que {@link ResumenPedido} para los correos de compra: el
 * contenido se decide en un solo sitio, y cada notificador solo decide cómo
 * lo entrega. El marco (cabecera, pie) lo pone {@link PlantillaCorreo}, igual
 * que en los demás correos.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.1
 */
public final class MensajeBienvenida {

    private MensajeBienvenida() {
        // Solo métodos estáticos.
    }

    /** @return asunto del correo */
    public static String asunto(Usuario usuario) {
        return "Bienvenido a Comercio Electrónico, " + usuario.getNombres();
    }

    /**
     * El tipo de cuenta sale de {@link Usuario#getTipoCuenta()}: la misma
     * plantilla sirve para Cliente y Proveedor sin preguntar la clase.
     *
     * @return cuerpo en HTML, con los datos del usuario escapados
     */
    public static String enHtml(Usuario usuario) {
        String contenido = PlantillaCorreo.titulo("¡Te damos la bienvenida, "
                        + usuario.getNombres() + "!")
                + PlantillaCorreo.parrafo("Tu cuenta ya está activa. Estos son los datos con"
                        + " los que entras a la tienda:")
                + PlantillaCorreo.recuadro(
                        "Tipo de cuenta", usuario.getTipoCuenta(),
                        "Correo de acceso", usuario.getCorreo())
                + PlantillaCorreo.parrafo("Puedes completar tus datos en cualquier momento"
                        + " desde <b>Editar perfil</b>, en el menú de tu avatar.")
                + PlantillaCorreo.parrafo("<span style=\"color:" + PlantillaCorreo.TEXTO_SUAVE
                        + ";font-size:13px;\">Si no fuiste tú quien creó esta cuenta, responde a"
                        + " este correo y la revisaremos.</span>");
        return PlantillaCorreo.envolver("Tu cuenta de " + usuario.getTipoCuenta() + " ya está activa",
                asunto(usuario), contenido);
    }

    /** @return cuerpo en texto plano */
    public static String enTexto(Usuario usuario) {
        return "Hola, " + usuario.getNombres() + ".\n"
                + "Tu cuenta de " + usuario.getTipoCuenta()
                + " en Comercio Electrónico ya está activa.\n"
                + "Entras con este correo: " + usuario.getCorreo();
    }
}
