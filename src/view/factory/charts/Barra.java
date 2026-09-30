package view.factory.charts;

/**
 * Una barra de {@link GraficoBarras}.
 *
 * <p><b>Por qué el valor viaja dos veces.</b> {@code valor} es el número con
 * el que se calcula el largo de la barra; {@code texto} es cómo se escribe ese
 * mismo número al lado ("$ 512.000", "12 unidades"). El gráfico no sabe si
 * está pintando dinero o cantidades, y formatear importes no es cosa suya:
 * quien tiene los datos ya los trae escritos.</p>
 *
 * @param etiqueta nombre de la barra, a la izquierda
 * @param valor    magnitud, para la longitud relativa
 * @param texto    el valor ya formateado, al final de la barra
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record Barra(String etiqueta, double valor, String texto) {
}
