package controller;

import aplicacion.compra.CompraService;
import aplicacion.cuenta.CuentaService;
import aplicacion.cuenta.TipoCuenta;
import aplicacion.resena.ResenaService;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import model.entity.Carrito;
import model.entity.Cliente;
import model.entity.Pedido;
import model.entity.Producto;
import model.entity.Usuario;
import model.repository.memoria.PedidoRepositoryMemoria;
import model.repository.memoria.ProductoRepositoryMemoria;
import model.repository.memoria.ResenaRepositoryMemoria;
import model.repository.memoria.UsuarioRepositoryImpl;
import service.correo.INotificadorCuenta;
import service.correo.INotificadorPedido;
import view.dashboard.cliente.DatosEnvio;
import view.dashboard.cliente.IClienteDashboardView;
import view.dashboard.cliente.LineaCarrito;
import view.dashboard.cliente.NuevaResena;
import view.dashboard.cliente.Promocion;
import view.dashboard.cliente.ResumenCompra;
import view.dashboard.cliente.ResumenResenas;
import view.dashboard.cliente.TarjetaProducto;

/**
 * Comprobación automática de {@link ClienteController} con una vista falsa.
 *
 * <p>Es la prueba de que el contrato {@link IClienteDashboardView} sirve para
 * algo más que para separar capas: el controlador de la tienda se ejecuta
 * completo sin abrir ninguna ventana. La vista falsa anota lo que el
 * controlador le pide mostrar y ejecuta en el acto el trabajo "en segundo
 * plano".</p>
 *
 * <p>Cubre el checkout bloqueado de una cuenta de Google, la reseña desde la
 * ficha, el pop-up promocional (≈40 % de los accesos y una vez por sesión),
 * el banner y las cantidades del carrito.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class VerificacionClienteController {

    private static int fallos = 0;

    private VerificacionClienteController() {
    }

    /** Notificador de prueba: no envía nada. */
    private static final class SinCorreo implements INotificadorCuenta, INotificadorPedido {
        @Override
        public boolean notificarBienvenida(Usuario usuario) {
            return true;
        }

        @Override
        public boolean notificarCompra(Pedido pedido, String correoDestino) {
            return true;
        }

        @Override
        public boolean notificarVenta(Pedido pedido) {
            return true;
        }
    }

    /** Vista falsa: guarda lo último que se le pidió y las acciones conectadas. */
    private static final class VistaFalsa implements IClienteDashboardView {
        private List<TarjetaProducto> catalogo = new ArrayList<>();
        private List<Promocion> banner = new ArrayList<>();
        private List<LineaCarrito> carrito = new ArrayList<>();
        private Promocion ofrecida;
        private DatosEnvio datosPedidos;
        private Consumer<DatosEnvio> enviarDatos;
        private String errorDatos;
        private boolean datosCerrados;
        private String exito;
        private String aviso;
        private TarjetaProducto fichaAbierta;
        private ResumenResenas resenasFicha;
        private ResumenResenas resenasActualizadas;
        private String errorResena;
        private BiConsumer<String, Integer> agregar;
        private BiConsumer<String, Integer> cambiar;
        private Runnable confirmar;
        private Consumer<String> verDetalle;
        private Consumer<NuevaResena> publicar;

        @Override public void mostrarProductos(List<TarjetaProducto> p) { catalogo = p; }
        @Override public void mostrarCategorias(List<String> c) { }
        @Override public void mostrarCarrito(List<LineaCarrito> l, String t) { carrito = l; }
        @Override public void mostrarPromociones(List<Promocion> p) { banner = p; }
        @Override public void ofrecerPromocion(Promocion p) { ofrecida = p; }
        @Override public void mostrarDetalle(TarjetaProducto t, ResumenResenas r) {
            fichaAbierta = t;
            resenasFicha = r;
        }
        @Override public void actualizarResenas(String id, ResumenResenas r) { resenasActualizadas = r; }
        @Override public void mostrarErrorResena(String m) { errorResena = m; }
        @Override public void pedirDatosEnvio(DatosEnvio a, Consumer<DatosEnvio> e) {
            datosPedidos = a;
            enviarDatos = e;
        }
        @Override public void mostrarErrorDatosEnvio(String m) { errorDatos = m; }
        @Override public void cerrarDatosEnvio() { datosCerrados = true; }
        @Override public void mostrarCompras(List<ResumenCompra> c) { }
        @Override public void mostrarAviso(String m) { aviso = m; }
        @Override public void mostrarExito(String m) { exito = m; }
        @Override public <T> void ejecutarEnSegundoPlano(String m, Supplier<T> t, Consumer<T> f) {
            f.accept(t.get());
        }
        @Override public void cerrarVentana() { }
        @Override public void alBuscar(Consumer<String> a) { }
        @Override public void alElegirCategoria(Consumer<String> a) { }
        @Override public void alVerDetalle(Consumer<String> a) { verDetalle = a; }
        @Override public void alPublicarResena(Consumer<NuevaResena> a) { publicar = a; }
        @Override public void alAgregarAlCarrito(BiConsumer<String, Integer> a) { agregar = a; }
        @Override public void alQuitarDelCarrito(Consumer<String> a) { }
        @Override public void alCambiarCantidad(BiConsumer<String, Integer> a) { cambiar = a; }
        @Override public void alConfirmarCompra(Runnable a) { confirmar = a; }
        @Override public void alAbrirMisCompras(Runnable a) { }
        @Override public void alRecargarCatalogo(Runnable a) { }
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
        SinCorreo correo = new SinCorreo();
        ProductoRepositoryMemoria productos = new ProductoRepositoryMemoria();
        PedidoRepositoryMemoria pedidos = new PedidoRepositoryMemoria();
        CuentaService cuentas = new CuentaService(new UsuarioRepositoryImpl(), correo);
        CompraService compras = new CompraService(productos, pedidos, correo);
        ResenaService resenas = new ResenaService(new ResenaRepositoryMemoria(), pedidos, productos);
        Producto audifonos = productos.buscar("Audífonos", null).get(0);

        System.out.println("Checkout de una cuenta de Google sin datos");
        Usuario sofia = cuentas.crearConGoogle("1", "sofia@gmail.com", "Sofía",
                TipoCuenta.CLIENTE).usuario();
        Carrito carritoSofia = new Carrito();
        VistaFalsa vista = new VistaFalsa();
        new ClienteController(vista, productos, compras, resenas, cuentas, sofia, carritoSofia)
                .iniciar(false);
        vista.agregar.accept(audifonos.getId(), 1);
        vista.confirmar.run();
        comprobar("confirmar pide el formulario de datos y no compra",
                vista.enviarDatos != null && vista.datosPedidos.pideCedula() && vista.exito == null);
        vista.enviarDatos.accept(new DatosEnvio("12", true, "Calle 45 # 12-30 Cali"));
        comprobar("una cédula inválida deja el formulario abierto con el error",
                vista.errorDatos != null && !vista.datosCerrados && vista.exito == null);
        vista.enviarDatos.accept(new DatosEnvio("1098765432", true, "Calle 45 # 12-30 Cali"));
        comprobar("con datos válidos se cierra el formulario y la compra sigue sola",
                vista.datosCerrados && vista.exito != null && carritoSofia.estaVacio());

        System.out.println("Reseña desde la ficha");
        vista.verDetalle.accept(audifonos.getId());
        comprobar("la ficha llega con el permiso para opinar (lo compró)",
                vista.fichaAbierta != null && vista.resenasFicha.puedeResenar());
        vista.publicar.accept(new NuevaResena(audifonos.getId(), 0, "sin estrellas"));
        comprobar("sin estrellas muestra el error en la ficha", vista.errorResena != null);
        vista.publicar.accept(new NuevaResena(audifonos.getId(), 5, "Genial"));
        comprobar("publicada, la sección se actualiza con 1 reseña de 5",
                vista.resenasActualizadas != null && vista.resenasActualizadas.total() == 1
                && vista.resenasActualizadas.promedio() == 5.0);
        TarjetaProducto tarjeta = vista.catalogo.stream()
                .filter(t -> t.id().equals(audifonos.getId())).findFirst().orElseThrow();
        comprobar("la tarjeta del catálogo ya muestra la calificación y la marca",
                tarjeta.totalResenas() == 1 && tarjeta.calificacion() == 5.0
                && !tarjeta.marca().isEmpty());
        Producto teclado = productos.buscar("Teclado", null).get(0);
        vista.verDetalle.accept(teclado.getId());
        comprobar("en un producto que no compró, la ficha da el motivo",
                !vista.resenasFicha.puedeResenar() && vista.resenasFicha.motivo() != null);

        System.out.println("Pop-up promocional");
        Usuario ana = new Cliente("5551234", "Ana", "ana@x.com", "x", "Calle 1 # 2-3");
        int ofrecidas = 0;
        int invalidas = 0;
        for (int i = 0; i < 2000; i++) {
            VistaFalsa otra = new VistaFalsa();
            new ClienteController(otra, productos, compras, resenas, cuentas, ana, new Carrito())
                    .iniciar();
            if (otra.ofrecida != null) {
                ofrecidas++;
                TarjetaProducto oferta = otra.ofrecida.producto();
                if (!oferta.tieneDescuento() || !oferta.hayExistencias()) {
                    invalidas++;
                }
            }
        }
        System.out.println("        " + ofrecidas + " de 2000 accesos ofrecieron promoción");
        comprobar("solo se ofrecen productos con descuento y existencias", invalidas == 0);
        comprobar("sale cerca del 40 % de las veces", ofrecidas > 700 && ofrecidas < 900);
        int alReconstruir = 0;
        for (int i = 0; i < 300; i++) {
            VistaFalsa otra = new VistaFalsa();
            new ClienteController(otra, productos, compras, resenas, cuentas, ana, new Carrito())
                    .iniciar(false);
            if (otra.ofrecida != null) {
                alReconstruir++;
            }
        }
        comprobar("al reconstruir la tienda en la misma sesión nunca sale", alReconstruir == 0);

        System.out.println("Banner");
        VistaFalsa tienda = new VistaFalsa();
        new ClienteController(tienda, productos, compras, resenas, cuentas, ana, new Carrito())
                .iniciar(false);
        boolean ordenado = true;
        for (int i = 1; i < tienda.banner.size(); i++) {
            ordenado &= tienda.banner.get(i - 1).producto().descuento()
                    >= tienda.banner.get(i).producto().descuento();
        }
        comprobar("lleva las 4 mejores ofertas, de mayor a menor descuento",
                tienda.banner.size() == 4 && ordenado);
        ProductoRepositoryMemoria sinOfertas = new ProductoRepositoryMemoria();
        for (Producto p : sinOfertas.listarTodos()) {
            p.setPorcentajeDescuento(0);
        }
        int conSinOfertas = 0;
        VistaFalsa vacia = null;
        for (int i = 0; i < 300; i++) {
            vacia = new VistaFalsa();
            new ClienteController(vacia, sinOfertas, compras, resenas, cuentas, ana, new Carrito())
                    .iniciar();
            if (vacia.ofrecida != null) {
                conSinOfertas++;
            }
        }
        comprobar("sin descuentos no hay pop-up ni banner",
                conSinOfertas == 0 && vacia.banner.isEmpty());

        System.out.println("Cantidades del carrito");
        Producto teclas = productos.buscar("Teclado", null).get(0);
        tienda.agregar.accept(teclas.getId(), 2);
        comprobar("agregar 2 unidades", tienda.carrito.get(0).cantidad() == 2);
        tienda.cambiar.accept(teclas.getId(), 3);
        comprobar("cambiar a 3, con el stock como tope",
                tienda.carrito.get(0).cantidad() == 3
                && tienda.carrito.get(0).maximo() == teclas.getStock());
        tienda.aviso = null;
        tienda.cambiar.accept(teclas.getId(), teclas.getStock() + 1);
        comprobar("pasar del stock avisa y no cambia",
                tienda.aviso != null && tienda.carrito.get(0).cantidad() == 3);
        tienda.cambiar.accept(teclas.getId(), 0);
        comprobar("cantidad 0 quita el renglón", tienda.carrito.isEmpty());

        System.out.println(fallos == 0 ? "\nTODO CORRECTO" : "\n" + fallos + " FALLAS");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
