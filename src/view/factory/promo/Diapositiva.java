package view.factory.promo;

/**
 * Una diapositiva del {@link BannerRotativo}.
 *
 * <p>Tipo propio de la fábrica, igual que {@code Barra} lo es del gráfico: el
 * banner es un componente reutilizable y no debe conocer los registros de
 * ningún panel. La vista traduce los suyos a este.</p>
 *
 * @param titulo           frase principal, corta (se dibuja en grande)
 * @param subtitulo        apoyo de una línea
 * @param referenciaImagen imagen de producto a mostrar a la derecha; vacía o
 *                         {@code null} si la diapositiva no lleva imagen
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record Diapositiva(String titulo, String subtitulo, String referenciaImagen) {
}
