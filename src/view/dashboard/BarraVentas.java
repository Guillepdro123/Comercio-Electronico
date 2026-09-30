package view.dashboard;

/**
 * Una barra del gráfico del panel del Proveedor.
 *
 * <p><b>Por qué existe habiendo ya un tipo igual en la fábrica.</b> El gráfico
 * tiene su propio registro ({@code view.factory.charts.Barra}) porque es un
 * componente reutilizable y no puede depender de quién lo use. El controlador,
 * por su parte, no debe conocer las clases internas de la fábrica: habla con la
 * vista a través de registros de {@code view.dashboard}, igual que con
 * {@link TarjetaProducto} o {@link FilaProducto}. La vista traduce de uno a
 * otro en una línea, y así ninguna de las dos capas invade a la otra.</p>
 *
 * @param etiqueta nombre de la barra
 * @param valor    magnitud, para el largo relativo
 * @param texto    el valor ya formateado, para escribirlo al lado
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record BarraVentas(String etiqueta, double valor, String texto) {
}
