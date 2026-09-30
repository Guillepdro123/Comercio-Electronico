package model.entity;

/**
 * Categorías del catálogo.
 *
 * <p><b>Por qué un enum y no un {@code String}:</b> el conjunto de categorías
 * es cerrado y lo conocen tanto el filtro del catálogo como el formulario del
 * proveedor. Con un enum, añadir una categoría es tocar un solo sitio y el
 * compilador obliga a cubrirla en todos los usos; con texto libre, cualquier
 * falta de ortografía crearía una categoría fantasma que el filtro nunca
 * encontraría.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public enum Categoria {

    TECNOLOGIA("Tecnología"),
    CALZADO("Calzado"),
    HOGAR("Hogar"),
    MODA("Moda"),
    DEPORTE("Deporte");

    private final String etiqueta;

    Categoria(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** @return nombre legible, el que se muestra en la interfaz */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Recupera una categoría desde el texto guardado en la base de datos.
     *
     * @param nombre nombre de la constante ({@code TECNOLOGIA}, ...)
     * @return la categoría correspondiente, o {@link #TECNOLOGIA} si no se reconoce
     */
    public static Categoria desdeNombre(String nombre) {
        for (Categoria categoria : values()) {
            if (categoria.name().equalsIgnoreCase(nombre)) {
                return categoria;
            }
        }
        return TECNOLOGIA;
    }
}
