package model.entity;

/**
 * Entidad del dominio: un artículo del catálogo, publicado por un proveedor.
 *
 * <p><b>Encapsulamiento:</b> los atributos son privados y el precio con
 * descuento no se guarda, se <em>calcula</em> ({@link #getPrecioFinal()}).
 * Guardar un valor derivado invita a que se desincronice del precio real; el
 * dato que manda es el par precio + descuento.</p>
 *
 * <p><b>El stock se toca solo por sus métodos.</b> {@link #descontarStock(int)}
 * es la única forma de reducirlo y se niega a dejarlo en negativo: la regla de
 * "no se puede vender lo que no hay" pertenece a la entidad, no a la pantalla
 * que muestra el botón de comprar.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class Producto {

    private String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private int porcentajeDescuento;
    private Categoria categoria;
    private int stock;
    private String imagen;
    private String correoProveedor;

    /**
     * @param id                 identificador único (lo asigna el repositorio)
     * @param nombre             nombre comercial
     * @param descripcion        texto que se muestra en la vista de detalle
     * @param precio             precio de lista, antes de descuento
     * @param porcentajeDescuento descuento aplicado, de 0 a 100
     * @param categoria          categoría del catálogo
     * @param stock              unidades disponibles
     * @param imagen             referencia de imagen (nombre de archivo o URL)
     * @param correoProveedor    proveedor dueño del producto, para su panel
     */
    public Producto(String id, String nombre, String descripcion, double precio,
                    int porcentajeDescuento, Categoria categoria, int stock,
                    String imagen, String correoProveedor) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.porcentajeDescuento = porcentajeDescuento;
        this.categoria = categoria;
        this.stock = stock;
        this.imagen = imagen;
        this.correoProveedor = correoProveedor;
    }

    /** @return precio realmente cobrado, ya aplicado el descuento */
    public double getPrecioFinal() {
        return precio * (100 - porcentajeDescuento) / 100.0;
    }

    /** @return {@code true} si el producto tiene descuento vigente */
    public boolean tieneDescuento() {
        return porcentajeDescuento > 0;
    }

    /** @return {@code true} si queda al menos una unidad */
    public boolean hayExistencias() {
        return stock > 0;
    }

    /**
     * Reduce las existencias tras una compra.
     *
     * @param unidades cantidad vendida
     * @return {@code true} si había suficiente y se descontó; {@code false} si
     *         no alcanzaba, en cuyo caso el stock queda intacto
     */
    public boolean descontarStock(int unidades) {
        if (unidades <= 0 || unidades > stock) {
            return false;
        }
        stock -= unidades;
        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(int porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getCorreoProveedor() {
        return correoProveedor;
    }

    public void setCorreoProveedor(String correoProveedor) {
        this.correoProveedor = correoProveedor;
    }
}
