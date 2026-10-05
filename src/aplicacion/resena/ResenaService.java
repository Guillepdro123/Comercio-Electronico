package aplicacion.resena;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import model.entity.Pedido;
import model.entity.Producto;
import model.entity.Resena;
import model.entity.Usuario;
import model.repository.IPedidoRepository;
import model.repository.IProductoRepository;
import model.repository.IResenaRepository;

/**
 * Casos de uso de las reseñas: leerlas, saber si alguien puede opinar y
 * publicar una opinión.
 *
 * <p><b>Solo opina quien compró.</b> Una reseña vale por venir de alguien que
 * tuvo el producto en las manos; se comprueba contra su historial de pedidos,
 * no contra lo que diga la pantalla. Y el vendedor no reseña lo suyo: sería
 * una calificación interesada.</p>
 *
 * <p><b>Bloquea</b> mientras habla con la base: quien lo llame desde una
 * interfaz gráfica debe hacerlo fuera del hilo de eventos.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class ResenaService {

    /** Largo máximo de un comentario: una opinión, no un ensayo. */
    public static final int LARGO_MAXIMO_COMENTARIO = 300;

    private final IResenaRepository resenas;
    private final IPedidoRepository pedidos;
    private final IProductoRepository productos;

    /**
     * @param resenas   almacén de reseñas
     * @param pedidos   historial de compras, para acreditar al comprador
     * @param productos catálogo, para reconocer al vendedor
     */
    public ResenaService(IResenaRepository resenas, IPedidoRepository pedidos,
                         IProductoRepository productos) {
        this.resenas = resenas;
        this.pedidos = pedidos;
        this.productos = productos;
    }

    /**
     * @param idProducto producto
     * @return sus reseñas, de la más reciente a la más antigua
     */
    public List<Resena> resenasDe(String idProducto) {
        return resenas.listarPorProducto(idProducto);
    }

    /**
     * @return promedio y cantidad de reseñas por producto, para el catálogo
     */
    public Map<String, double[]> resumenDelCatalogo() {
        return resenas.resumenPorProducto();
    }

    /**
     * @param usuario    quien quiere opinar
     * @param idProducto producto
     * @return por qué no puede reseñarlo, o {@code null} si puede
     */
    public String motivoParaNoResenar(Usuario usuario, String idProducto) {
        Producto producto = productos.buscarPorId(idProducto);
        if (producto == null) {
            return "Ese producto ya no está en el catálogo.";
        }
        if (producto.getCorreoProveedor() != null
                && producto.getCorreoProveedor().equalsIgnoreCase(usuario.getCorreo())) {
            return "Es un producto de tu tienda: las reseñas son de quienes lo compran.";
        }
        for (Pedido pedido : pedidos.listarPorUsuario(usuario.getCorreo())) {
            if (pedido.incluyeProducto(idProducto)) {
                return null;
            }
        }
        return "Podrás calificarlo cuando lo hayas comprado.";
    }

    /**
     * Publica (o reemplaza) la reseña del usuario sobre un producto.
     *
     * @param usuario    autor
     * @param idProducto producto
     * @param estrellas  calificación elegida; 0 si no eligió ninguna
     * @param comentario texto libre, opcional
     * @return mensaje de error, o {@code null} si se publicó — mismo patrón
     *         que {@code PoliticaPassword.validar(String)}
     */
    public String publicar(Usuario usuario, String idProducto, int estrellas, String comentario) {
        String motivo = motivoParaNoResenar(usuario, idProducto);
        if (motivo != null) {
            return motivo;
        }
        if (estrellas < Resena.MINIMO_ESTRELLAS || estrellas > Resena.MAXIMO_ESTRELLAS) {
            return "Elige de 1 a 5 estrellas.";
        }
        String texto = comentario == null ? "" : comentario.trim();
        if (texto.length() > LARGO_MAXIMO_COMENTARIO) {
            return "El comentario admite hasta " + LARGO_MAXIMO_COMENTARIO + " caracteres.";
        }
        resenas.guardar(new Resena(idProducto, usuario.getCorreo(), usuario.getNombres(),
                estrellas, texto, LocalDateTime.now()));
        return null;
    }
}
