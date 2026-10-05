package aplicacion.cuenta;

/**
 * Los dos roles que puede tener una cuenta, como texto.
 *
 * <p><b>Por qué aquí y no en la vista.</b> Estas dos palabras son la respuesta
 * de {@code Usuario.getTipoCuenta()} y el criterio con el que se decide qué
 * subclase construir y qué panel abrir: son del negocio, no de la pantalla.
 * El formulario de registro las reutiliza desde aquí, de modo que existen
 * escritas una sola vez en todo el proyecto.</p>
 *
 * <p>Son texto y no un {@code enum} a propósito: es lo que se guarda en
 * MongoDB, y un documento con "Cliente" se lee igual dentro de diez años
 * aunque las clases de Java cambien de nombre.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class TipoCuenta {

    /** Cuenta que compra en el catálogo. */
    public static final String CLIENTE = "Cliente";

    /** Cuenta que publica y vende productos. */
    public static final String PROVEEDOR = "Proveedor";

    private TipoCuenta() {
        // Solo constantes.
    }
}
