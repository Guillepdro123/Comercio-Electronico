package view.dashboard.cliente;

import java.util.List;

/**
 * Sección de reseñas de la ficha de un producto, ya calculada por el
 * controlador.
 *
 * <p>Si el usuario puede opinar o no lo decide el caso de uso (solo quien lo
 * compró, y nunca el vendedor); la vista solo muestra el formulario o el
 * motivo.</p>
 *
 * @param promedio     calificación media; 0 si no hay reseñas
 * @param total        cuántas reseñas hay
 * @param resenas      las reseñas, de la más reciente a la más antigua
 * @param puedeResenar {@code true} si se muestra el formulario
 * @param motivo       por qué no puede opinar, si {@code puedeResenar} es falso
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record ResumenResenas(double promedio, int total, List<ResenaVista> resenas,
                             boolean puedeResenar, String motivo) {
}
