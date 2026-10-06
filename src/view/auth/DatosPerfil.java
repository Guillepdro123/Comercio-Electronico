package view.auth;

/**
 * Datos que la edición de perfil muestra al abrirse, ya extraídos de la cuenta
 * por {@link controller.PerfilController}.
 *
 * <p>La vista no recibe el {@code Usuario}: recibe estos textos y unas pocas
 * banderas que le dicen qué secciones pintar. Así decide cómo se ve sin
 * preguntar de qué clase es la cuenta.</p>
 *
 * @param nombres        nombres y apellidos
 * @param correo         correo de acceso
 * @param telefono       teléfono, puede venir vacío
 * @param cedula         cédula, vacía si la cuenta todavía no la tiene
 * @param cedulaEditable {@code true} si la cuenta aún no tiene cédula y puede
 *                       darla aquí; una vez dada, solo se muestra
 * @param direccionEnvio dirección de envío, puede venir vacía
 * @param esVendedor     {@code true} si la cuenta tiene tienda (Proveedor):
 *                       entonces se muestra la sección de empresa
 * @param nit            NIT de la empresa (solo vendedor)
 * @param nombreEmpresa  empresa o marca (solo vendedor)
 * @param aviso          lo que le falta al perfil, o {@code null} si nada
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 2.0
 */
public record DatosPerfil(String nombres, String correo, String telefono,
                          String cedula, boolean cedulaEditable, String direccionEnvio,
                          boolean esVendedor, String nit, String nombreEmpresa,
                          String aviso) {
}
