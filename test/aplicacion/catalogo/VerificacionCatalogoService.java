package aplicacion.catalogo;

import java.util.List;
import model.entity.Categoria;
import model.entity.Cliente;
import model.entity.LineaPedido;
import model.entity.Pedido;
import model.entity.Producto;
import model.repository.memoria.ImagenRepositoryArchivo;
import model.repository.memoria.PedidoRepositoryMemoria;
import model.repository.memoria.ProductoRepositoryMemoria;
import observer.CatalogoSubject;

/**
 * Comprobación automática de {@link CatalogoService} y
 * {@link ReporteVentasService}.
 *
 * <p>Como {@link VerificacionCompraService}: Java puro, sin JUnit y sin abrir
 * ninguna ventana. Comprueba la validación del formulario, el aviso del
 * patrón Observer y el cálculo de las cifras de venta, que antes estaban
 * encerrados en {@code ProveedorController}.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class VerificacionCatalogoService {

    private static int fallos = 0;

    private VerificacionCatalogoService() {
    }

    private static void comprobar(String que, boolean ok) {
        System.out.println((ok ? "  OK    " : "  FALLA ") + que);
        if (!ok) {
            fallos++;
        }
    }

    private static SolicitudProducto solicitud(String nombre, String precio, String descuento,
                                               String stock) {
        return new SolicitudProducto(nombre, "Descripción de prueba", precio, descuento,
                Categoria.HOGAR.getEtiqueta(), stock, "");
    }

    /**
     * @param args no se usan
     */
    public static void main(String[] args) {
        ProductoRepositoryMemoria productos = new ProductoRepositoryMemoria();
        CatalogoSubject catalogo = new CatalogoSubject();
        int[] avisos = {0};
        catalogo.agregarObservador(() -> avisos[0]++);
        CatalogoService servicio = new CatalogoService(productos, catalogo,
                new ImportadorImagen(new ImagenRepositoryArchivo()));
        String proveedor = "tienda@correo.com";

        System.out.println("Validación del formulario");
        comprobar("rechaza el nombre vacío",
                "El producto necesita un nombre.".equals(
                        servicio.publicar(solicitud("  ", "1000", "0", "5"), proveedor, "Supertecno").error()));
        comprobar("rechaza un precio que no es número",
                servicio.publicar(solicitud("Taza", "abc", "0", "5"), proveedor, "Supertecno").error()
                        .contains("precio"));
        comprobar("rechaza un descuento de 120",
                servicio.publicar(solicitud("Taza", "1000", "120", "5"), proveedor, "Supertecno").error()
                        .contains("descuento"));
        comprobar("rechaza stock negativo",
                servicio.publicar(solicitud("Taza", "1000", "0", "-2"), proveedor, "Supertecno").error()
                        .contains("stock"));
        comprobar("nada de eso avisó al catálogo ni guardó",
                avisos[0] == 0 && productos.listarPorProveedor(proveedor).isEmpty());

        System.out.println("Publicar, editar y eliminar");
        ResultadoProducto alta = servicio.publicar(solicitud("Taza", "20000", "10", "5"), proveedor, "Supertecno");
        comprobar("publica y devuelve el producto", alta.exitoso() && alta.producto() != null);
        comprobar("acepta el precio con coma decimal",
                servicio.publicar(solicitud("Plato", "1500,50", "0", "2"), proveedor, "Supertecno").exitoso()
                        && productos.listarPorProveedor(proveedor).size() == 2);
        comprobar("avisó al catálogo una vez por alta", avisos[0] == 2);

        String id = alta.producto().getId();
        ResultadoProducto edicion = servicio.actualizar(id, solicitud("Taza grande", "25000", "0", "7"));
        comprobar("edita y guarda los datos nuevos", edicion.exitoso()
                && "Taza grande".equals(productos.buscarPorId(id).getNombre())
                && productos.buscarPorId(id).getStock() == 7);
        comprobar("editar con datos inválidos no toca lo guardado",
                !servicio.actualizar(id, solicitud("Taza grande", "0", "0", "7")).exitoso()
                        && productos.buscarPorId(id).getPrecio() == 25000);

        comprobar("elimina", servicio.eliminar(id).exitoso() && productos.buscarPorId(id) == null);
        comprobar("eliminar dos veces avisa en vez de fallar",
                "Ese producto ya no existe en el catálogo.".equals(servicio.eliminar(id).error()));
        comprobar("editar algo borrado avisa en vez de fallar",
                servicio.actualizar(id, solicitud("X", "1000", "0", "1")).error()
                        .contains("ya no existe"));

        System.out.println("Cifras de venta");
        Producto plato = productos.listarPorProveedor(proveedor).get(0);
        Producto ajeno = productos.guardar(new Producto(null, "Ajeno", "d", 5000, 0,
                Categoria.MODA, 10, "", "otra@tienda.com"));
        PedidoRepositoryMemoria pedidos = new PedidoRepositoryMemoria();
        pedidos.registrar(new Pedido(null, "ana@correo.com", "Ana", List.of(
                new LineaPedido(plato.getId(), "Plato", 1500.5, 2),
                new LineaPedido(ajeno.getId(), "Ajeno", 5000, 1)), "Calle 1"));
        pedidos.registrar(new Pedido(null, "luis@correo.com", "Luis", List.of(
                new LineaPedido(ajeno.getId(), "Ajeno", 5000, 3)), "Calle 2"));

        ReporteVentas reporte = new ReporteVentasService(productos, pedidos).generar(proveedor);
        comprobar("solo suma los renglones propios, no el total del pedido",
                reporte.ingresos() == 3001.0 && reporte.unidadesVendidas() == 2);
        comprobar("cuenta un pedido con venta, no dos", reporte.pedidosConVenta() == 1);
        comprobar("no incluye productos de otro proveedor",
                reporte.publicados() == 1 && reporte.productos().size() == 1);
        comprobar("detecta que hay ventas", reporte.hayVentas());
        comprobar("ticket promedio = ingresos / pedidos con venta",
                reporte.ticketPromedio() == 3001.0);
        comprobar("el producto estrella es el de mayor ingreso",
                reporte.productoEstrella() != null
                && plato.getId().equals(reporte.productoEstrella().getId()));

        ReporteVentas sinVentas = new ReporteVentasService(productos, new PedidoRepositoryMemoria())
                .generar(proveedor);
        comprobar("sin pedidos, cifras en cero y sin ventas",
                !sinVentas.hayVentas() && sinVentas.ingresos() == 0 && sinVentas.publicados() == 1);
        comprobar("sin pedidos, ticket en cero y sin producto estrella",
                sinVentas.ticketPromedio() == 0 && sinVentas.productoEstrella() == null);

        Cliente cualquiera = new Cliente("9", "X", "x@x.com", "x", "dir");
        comprobar("un proveedor sin catálogo no rompe nada",
                new ReporteVentasService(productos, pedidos).generar(cualquiera.getCorreo())
                        .publicados() == 0);

        System.out.println(fallos == 0 ? "\nTODO CORRECTO" : "\n" + fallos + " FALLAS");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
