package aplicacion.cuenta;

import aplicacion.compra.CompraService;
import aplicacion.compra.ResultadoCompra;
import java.util.List;
import model.entity.LineaPedido;
import model.entity.Pedido;
import model.entity.Producto;
import model.entity.Usuario;
import model.repository.memoria.PedidoRepositoryMemoria;
import model.repository.memoria.ProductoRepositoryMemoria;
import model.repository.memoria.UsuarioRepositoryImpl;
import service.correo.INotificadorCuenta;
import service.correo.INotificadorPedido;

/**
 * Comprobación automática del doble rol y de los datos de envío.
 *
 * <p>Cubre las reglas que cruzan la cuenta y la compra: el Proveedor necesita
 * empresa para vender, el Cliente no vende, una cuenta de Google nace sin
 * cédula ni dirección y por eso no puede comprar hasta completarlas, y el
 * Proveedor, que también compra, necesita su propia dirección.</p>
 *
 * <p>Java puro, sin JUnit y sin ventanas, con los repositorios en memoria:
 * nunca escribe en MongoDB Atlas.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class VerificacionDobleRol {

    private static int fallos = 0;

    private VerificacionDobleRol() {
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
        CuentaService cuentas = new CuentaService(new UsuarioRepositoryImpl(), correo);
        ProductoRepositoryMemoria productos = new ProductoRepositoryMemoria();
        CompraService compras = new CompraService(productos, new PedidoRepositoryMemoria(), correo);

        System.out.println("Proveedor con empresa");
        ResultadoCuenta sinEmpresa = cuentas.registrar(new SolicitudRegistro("100200", "Luis",
                "luis@x.com", "Clave123*", "900123-1", TipoCuenta.PROVEEDOR, " "));
        comprobar("el registro de proveedor exige la empresa",
                !sinEmpresa.exitoso() && sinEmpresa.error().contains("empresa"));
        ResultadoCuenta luis = cuentas.registrar(new SolicitudRegistro("100200", "Luis",
                "luis@x.com", "Clave123*", "900123-1", TipoCuenta.PROVEEDOR, "Tech"));
        comprobar("con empresa se registra, la guarda y puede vender",
                luis.exitoso() && "Tech".equals(luis.usuario().getNombreEmpresa())
                && luis.usuario().puedeVender());
        ResultadoCuenta ana = cuentas.registrar(new SolicitudRegistro("5551234", "Ana",
                "ana@x.com", "Clave123*", "Calle 1 # 2-3", TipoCuenta.CLIENTE, ""));
        comprobar("el cliente no necesita empresa y no vende",
                ana.exitoso() && !ana.usuario().puedeVender());
        comprobar("la cédula de una cuenta de formulario es su identificación",
                "5551234".equals(ana.usuario().getCedula()));

        System.out.println("Cuenta de Google y checkout");
        Usuario sofia = cuentas.crearConGoogle("777", "sofia@gmail.com", "Sofía",
                TipoCuenta.CLIENTE).usuario();
        comprobar("una cuenta de Google nace sin datos de envío",
                !sofia.datosDeEnvioCompletos() && sofia.getCedula().isEmpty());
        comprobar("el perfil lo avisa", cuentas.avisoDePerfil(sofia) != null);
        Producto audifonos = productos.buscar("Audífonos", null).get(0);
        int stockAntes = audifonos.getStock();
        List<LineaPedido> lineas = List.of(new LineaPedido(audifonos.getId(),
                audifonos.getNombre(), audifonos.getPrecioFinal(), 1));
        ResultadoCompra rechazada = compras.confirmar(sofia, lineas);
        comprobar("la compra se rechaza sin cédula ni dirección y no toca el stock",
                rechazada.faltanDatosEnvio() && audifonos.getStock() == stockAntes);
        comprobar("una cédula con puntos se rechaza",
                cuentas.completarDatosEnvio(sofia, "1.234.567", "Calle 45 # 12-30 Cali")
                        .error().contains("dígitos"));
        comprobar("la cédula de otra cuenta se rechaza",
                cuentas.completarDatosEnvio(sofia, "5551234", "Calle 45 # 12-30 Cali")
                        .error().contains("otra cuenta"));
        comprobar("una dirección demasiado corta se rechaza",
                cuentas.completarDatosEnvio(sofia, "1098765432", "Cl 1").error()
                        .contains("dirección"));
        comprobar("con datos válidos se completan",
                cuentas.completarDatosEnvio(sofia, "1098765432", "Calle 45 # 12-30 Cali")
                        .exitoso() && sofia.datosDeEnvioCompletos());
        ResultadoCompra hecha = compras.confirmar(sofia, lineas);
        comprobar("ya puede comprar y el pedido lleva su cédula y dirección",
                hecha.exitosa() && "1098765432".equals(hecha.pedido().getDocumentoComprador())
                && hecha.pedido().getDireccionEntrega().startsWith("Calle 45"));

        System.out.println("El proveedor también compra");
        Usuario proveedor = luis.usuario();
        comprobar("sin dirección propia no puede comprar",
                !proveedor.datosDeEnvioCompletos()
                && compras.confirmar(proveedor, lineas).faltanDatosEnvio());

        System.out.println(fallos == 0 ? "\nTODO CORRECTO" : "\n" + fallos + " FALLAS");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
