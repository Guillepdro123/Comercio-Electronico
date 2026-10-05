package aplicacion.resena;

import aplicacion.compra.CompraService;
import java.util.List;
import model.entity.Categoria;
import model.entity.Cliente;
import model.entity.LineaPedido;
import model.entity.Pedido;
import model.entity.Producto;
import model.entity.Usuario;
import model.repository.memoria.PedidoRepositoryMemoria;
import model.repository.memoria.ProductoRepositoryMemoria;
import model.repository.memoria.ResenaRepositoryMemoria;
import service.correo.INotificadorPedido;

/**
 * Comprobación automática de {@link ResenaService}.
 *
 * <p>Las reglas de las reseñas se comprueban contra el historial real de
 * pedidos, no contra lo que diga la pantalla: solo opina quien compró, el
 * vendedor nunca califica lo suyo, de 1 a 5 estrellas, comentario de hasta
 * 300 caracteres y una sola reseña por persona y producto.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class VerificacionResenaService {

    private static int fallos = 0;

    private VerificacionResenaService() {
    }

    /** Notificador de prueba: no envía nada. */
    private static final class SinCorreo implements INotificadorPedido {
        @Override
        public boolean notificarCompra(Pedido pedido, String correoDestino) {
            return true;
        }

        @Override
        public boolean notificarVenta(Pedido pedido) {
            return true;
        }
    }

    private static void comprobar(String que, boolean ok) {
        System.out.println((ok ? "  OK    " : "  FALLA ") + que);
        if (!ok) {
            fallos++;
        }
    }

    /**
     * @param args no se usan
     */
    public static void main(String[] args) {
        ProductoRepositoryMemoria productos = new ProductoRepositoryMemoria();
        PedidoRepositoryMemoria pedidos = new PedidoRepositoryMemoria();
        ResenaService resenas = new ResenaService(new ResenaRepositoryMemoria(), pedidos, productos);
        Usuario ana = new Cliente("5551234", "Ana", "ana@x.com", "x", "Calle 1 # 2-3");
        Producto audifonos = productos.buscar("Audífonos", null).get(0);
        Producto teclado = productos.buscar("Teclado", null).get(0);
        new CompraService(productos, pedidos, new SinCorreo()).confirmar(ana, List.of(
                new LineaPedido(audifonos.getId(), audifonos.getNombre(),
                        audifonos.getPrecioFinal(), 1)));

        System.out.println("Quién puede opinar");
        comprobar("quien no lo compró no puede reseñar",
                resenas.motivoParaNoResenar(ana, teclado.getId()) != null
                && resenas.publicar(ana, teclado.getId(), 5, "") != null);
        comprobar("quien lo compró sí", resenas.motivoParaNoResenar(ana, audifonos.getId()) == null);
        Producto deLuis = productos.guardar(new Producto(null, "Mouse", "Inalámbrico", 50000, 0,
                Categoria.TECNOLOGIA, 5, "", "luis@x.com"));
        Usuario luis = new Cliente("100200", "Luis", "luis@x.com", "x", "Cra 7 # 1-1");
        comprobar("el vendedor no reseña lo suyo",
                resenas.motivoParaNoResenar(luis, deLuis.getId()).contains("tu tienda"));

        System.out.println("Estrellas y comentario");
        comprobar("sin estrellas no se publica",
                resenas.publicar(ana, audifonos.getId(), 0, "x") != null);
        comprobar("seis estrellas tampoco",
                resenas.publicar(ana, audifonos.getId(), 6, "x") != null);
        comprobar("un comentario de 301 caracteres se rechaza",
                resenas.publicar(ana, audifonos.getId(), 4, "a".repeat(301)) != null);
        comprobar("publica con 4 estrellas y 300 caracteres",
                resenas.publicar(ana, audifonos.getId(), 4, "a".repeat(300)) == null);

        System.out.println("Una por persona");
        comprobar("una segunda reseña reemplaza a la primera",
                resenas.publicar(ana, audifonos.getId(), 2, "Se dañó") == null
                && resenas.resenasDe(audifonos.getId()).size() == 1
                && resenas.resenasDe(audifonos.getId()).get(0).getEstrellas() == 2);
        double[] resumen = resenas.resumenDelCatalogo().get(audifonos.getId());
        comprobar("el resumen del catálogo da promedio y cantidad",
                resumen[0] == 2.0 && resumen[1] == 1.0);

        System.out.println(fallos == 0 ? "\nTODO CORRECTO" : "\n" + fallos + " FALLAS");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
