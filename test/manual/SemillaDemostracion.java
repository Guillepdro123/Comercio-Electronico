package manual;

import aplicacion.catalogo.CatalogoService;
import aplicacion.catalogo.ImportadorImagen;
import aplicacion.catalogo.ResultadoProducto;
import aplicacion.catalogo.SolicitudProducto;
import aplicacion.cuenta.CuentaService;
import aplicacion.cuenta.ResultadoCuenta;
import aplicacion.cuenta.SolicitudRegistro;
import aplicacion.cuenta.TipoCuenta;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import model.entity.Producto;
import model.entity.Usuario;
import model.repository.mongo.ImagenRepositoryMongo;
import model.repository.mongo.MongoDBConnection;
import model.repository.mongo.ProductoRepositoryMongo;
import model.repository.mongo.UsuarioRepositoryMongo;
import observer.CatalogoSubject;
import service.config.Configuracion;
import service.imagen.DescargadorImagenes;

/**
 * Carga en MongoDB Atlas los datos de demostración de la sustentación: la
 * cuenta del proveedor "Global Tech &amp; Home Store" y su catálogo de 28
 * productos, con imagen en internet (Unsplash, licencia libre).
 *
 * <p>Pasa por los mismos casos de uso que la aplicación ({@link CuentaService},
 * {@link CatalogoService}): la contraseña queda cifrada con BCrypt, cada
 * producto lleva su marca y su campo de búsqueda, y se aplican las mismas
 * validaciones que en el formulario. Nada se escribe a mano en la base.</p>
 *
 * <p><b>Idempotente:</b> si la cuenta ya existe no la toca, y un producto con
 * el mismo nombre en ese catálogo se salta. Ejecutarlo dos veces no duplica.</p>
 *
 * <p>Uso (NetBeans: Run File con argumentos, o línea de comandos):</p>
 * <ul>
 *   <li>{@code verificar}: descarga y decodifica las 28 imágenes; no escribe nada.</li>
 *   <li>{@code cargar <contraseña>}: crea la cuenta (si falta) y publica el
 *       catálogo. La contraseña se pasa al ejecutar y no se guarda en el código.</li>
 * </ul>
 * Necesita {@code config.properties} con la conexión a Atlas.
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class SemillaDemostracion {

    private static final String CORREO = "sandoval.guillermo.privado@gmail.com";
    private static final String NOMBRE = "Guillermo Sandoval";
    private static final String EMPRESA = "Global Tech & Home Store";
    /** Cédula y NIT de demostración: se pueden cambiar desde "Editar perfil". */
    private static final String CEDULA = "1102857463";
    private static final String NIT = "901456789-1";
    private static final String DIRECCION = "Calle 10 # 15-20, Sahagún, Córdoba";

    /** Parámetros que fuerzan JPEG de 800 px: Java no decodifica WebP ni AVIF. */
    private static final String FORMATO = "?w=800&q=80&fm=jpg&fit=crop";

    private record Articulo(String nombre, String descripcion, int precio, int descuento,
                            String categoria, int stock, String foto) {
        String url() {
            return "https://images.unsplash.com/" + foto + FORMATO;
        }
    }

    private static final List<Articulo> CATALOGO = List.of(
            // Tecnología
            new Articulo("Smartwatch Fit Pro 2", "Pantalla AMOLED de 1,9 pulgadas, GPS integrado, ritmo cardiaco y oxígeno en sangre, resistente al agua 5 ATM y hasta 10 días de batería.", 489000, 15, "Tecnología", 40, "photo-1546868871-7041f2a55e12"),
            new Articulo("Audífonos inalámbricos Studio ANC", "Cancelación activa de ruido, Bluetooth 5.3, 40 horas de autonomía y almohadillas de espuma viscoelástica para uso prolongado.", 359000, 20, "Tecnología", 45, "photo-1505740420928-5e560c06d30e"),
            new Articulo("Parlante Bluetooth SoundGo", "Sonido envolvente de 20 W, resistente al agua IPX7, 12 horas de reproducción y modo fiesta para enlazar dos parlantes.", 229000, 10, "Tecnología", 50, "photo-1608043152269-423dbba4e7e1"),
            new Articulo("Audífonos in-ear Buds Air", "Estuche de carga compacto con 30 horas en total, controles táctiles y micrófonos con reducción de ruido para llamadas.", 189000, 0, "Tecnología", 50, "photo-1590658268037-6bf12165a8df"),
            new Articulo("Portátil UltraBook 14", "Procesador de 8 núcleos, 16 GB de RAM, SSD de 512 GB y pantalla Full HD de 14 pulgadas en un chasis de aluminio de 1,3 kg.", 3299000, 12, "Tecnología", 30, "photo-1496181133206-80ce9b88a853"),
            new Articulo("Teclado mecánico RGB TKL", "Switches mecánicos intercambiables, iluminación RGB por tecla, formato sin teclado numérico y cable USB-C desmontable.", 279000, 0, "Tecnología", 40, "photo-1618384887929-16ec33fab9ef"),
            new Articulo("Mouse inalámbrico ergonómico", "Sensor de 4000 DPI, diseño que reduce la tensión de la muñeca y conexión doble: Bluetooth o receptor USB.", 129000, 25, "Tecnología", 50, "photo-1527864550417-7fd91fc51a46"),
            new Articulo("Cámara mirrorless 24 MP", "Sensor APS-C de 24 MP, video 4K, estabilización en el cuerpo y lente 18-55 mm incluido. Ideal para fotografía de producto.", 3899000, 10, "Tecnología", 30, "photo-1516035069371-29a1b244cc32"),
            // Hogar
            new Articulo("Cafetera espresso Barista", "Bomba de 15 bares, vaporizador de leche para capuchino y depósito extraíble de 1,5 litros.", 749000, 18, "Hogar", 35, "photo-1620807773206-49c1f2957417"),
            new Articulo("Licuadora de alta potencia 1200 W", "Vaso de 2 litros, cuchillas de acero inoxidable de 6 puntas y 5 velocidades con función pulso.", 329000, 0, "Hogar", 40, "photo-1695089028114-ce28248f0ab9"),
            new Articulo("Lámpara de escritorio LED", "Brazo articulado, tres temperaturas de color y regulación táctil del brillo, con un consumo de solo 8 W.", 119000, 0, "Hogar", 50, "photo-1519219788971-8d9797e0928e"),
            new Articulo("Olla esmaltada de hierro fundido 5 L", "Apta para todo tipo de estufas y para horno; conserva el calor para guisos, sopas y arroces.", 389000, 20, "Hogar", 30, "photo-1556910148-3adb7f0c665a"),
            new Articulo("Hervidor eléctrico de acero", "Capacidad de 1,7 litros, hierve en 4 minutos, apagado automático y base giratoria.", 149000, 0, "Hogar", 45, "photo-1594213114663-d94db9b17125"),
            new Articulo("Tostadora de 2 ranuras", "Siete niveles de tostado, función para descongelar y bandeja recogemigas extraíble.", 159000, 10, "Hogar", 40, "photo-1613221699807-4940ba9b83f4"),
            new Articulo("Aspiradora inalámbrica", "Motor digital, filtro HEPA, 45 minutos de autonomía y accesorios para tapicería y rincones.", 899000, 15, "Hogar", 30, "photo-1765970101654-337b573142fb"),
            // Calzado
            new Articulo("Zapatillas running Velocity", "Amortiguación de espuma reactiva, malla transpirable y suela de caucho con buen agarre para asfalto.", 389000, 30, "Calzado", 40, "photo-1542291026-7eec264c27ff"),
            new Articulo("Tenis urbanos blancos", "Capellada de cuero sintético, plantilla acolchada y suela de goma vulcanizada. Combinan con todo.", 249000, 0, "Calzado", 50, "photo-1600269452121-4f2416e55c28"),
            new Articulo("Botas de cuero con cordones", "Cuero genuino, forro interior suave y suela antideslizante para el uso diario.", 459000, 0, "Calzado", 30, "photo-1608256246200-53e635b5b65f"),
            new Articulo("Zapatillas de baloncesto High", "Caña alta que sujeta el tobillo, cámara de aire en el talón y suela de tracción en espiga.", 529000, 15, "Calzado", 30, "photo-1605523741177-cd660595c2cf"),
            // Moda y accesorios
            new Articulo("Reloj análogo clásico", "Caja de acero inoxidable de 40 mm, cristal mineral y correa de cuero marrón. Resistente a salpicaduras.", 349000, 0, "Moda", 35, "photo-1523170335258-f5ed11844a49"),
            new Articulo("Gafas de sol polarizadas", "Lentes polarizados con protección UV400 y montura metálica liviana. Incluyen estuche rígido.", 199000, 20, "Moda", 50, "photo-1572635196237-14b3f281503f"),
            new Articulo("Mochila urbana antirrobo", "Compartimento acolchado para portátil de 15,6 pulgadas, puerto USB externo y tela repelente al agua.", 219000, 0, "Moda", 45, "photo-1553062407-98eeb64c6a62"),
            new Articulo("Billetera de cuero", "Cuero genuino cosido a mano, ocho ranuras para tarjetas y bloqueo RFID.", 99000, 0, "Moda", 50, "photo-1628483211662-9bcc692c46dc"),
            new Articulo("Chaqueta de jean clásica", "Denim de algodón resistente, corte regular y botones metálicos. Un básico para cualquier temporada.", 279000, 25, "Moda", 40, "photo-1611312449408-fcece27cdbb7"),
            // Deporte
            new Articulo("Balón de fútbol profesional N.5", "Termosellado de 32 paneles, bote uniforme y apto para césped natural y sintético.", 149000, 0, "Deporte", 50, "photo-1579952363873-27f3bade9f55"),
            new Articulo("Juego de mancuernas ajustables", "De 2 a 24 kg cada una, cambio de peso con selector y base de almacenamiento incluida.", 899000, 10, "Deporte", 30, "photo-1576678927484-cc907957088c"),
            new Articulo("Esterilla de yoga antideslizante", "6 mm de grosor, material TPE ecológico y correa de transporte. Incluye dos bloques.", 129000, 0, "Deporte", 50, "photo-1646239646963-b0b9be56d6b5"),
            new Articulo("Botella térmica deportiva", "Acero inoxidable de doble pared: mantiene el frío 24 horas y el calor 12. Capacidad de 750 ml.", 89000, 15, "Deporte", 50, "photo-1664714628878-9d2aa898b9e3"));

    private SemillaDemostracion() {
    }

    /**
     * @param args {@code verificar} o {@code cargar <contraseña>}
     */
    public static void main(String[] args) {
        if (args.length == 0 || !("verificar".equals(args[0])
                || "cargar".equals(args[0]) && args.length == 2)) {
            System.out.println("Uso: verificar | cargar <contraseña>");
            System.exit(2);
        }
        int imagenesMalas = verificarImagenes();
        if (imagenesMalas > 0) {
            System.out.println(imagenesMalas + " imágenes no sirven: no se carga nada.");
            System.exit(1);
        }
        if ("cargar".equals(args[0])) {
            cargar(args[1]);
        }
        System.exit(0);
    }

    /** Descarga cada imagen por el mismo camino que la aplicación y la decodifica. */
    private static int verificarImagenes() {
        DescargadorImagenes descargador = new DescargadorImagenes();
        int malas = 0;
        for (Articulo articulo : CATALOGO) {
            String estado;
            try {
                byte[] datos = descargador.descargar(articulo.url());
                BufferedImage imagen = datos == null ? null
                        : ImageIO.read(new ByteArrayInputStream(datos));
                estado = imagen == null ? "FALLA" : "OK " + imagen.getWidth() + "x"
                        + imagen.getHeight() + " " + datos.length / 1024 + " KB";
            } catch (java.io.IOException ex) {
                estado = "FALLA " + ex.getMessage();
            }
            if (estado.startsWith("FALLA")) {
                malas++;
            }
            System.out.printf("  %-38s %s%n", articulo.nombre(), estado);
        }
        return malas;
    }

    private static void cargar(String contrasena) {
        MongoDBConnection conexion = new MongoDBConnection(new Configuracion());
        if (!conexion.disponible()) {
            System.out.println("MongoDB Atlas no responde: no se cargó nada.");
            System.exit(1);
        }
        UsuarioRepositoryMongo usuarios = new UsuarioRepositoryMongo(conexion.getBase());
        Usuario proveedor = usuarios.buscarPorCorreo(CORREO);
        if (proveedor == null) {
            // Sin correo de bienvenida: es una carga de datos, no un alta real.
            CuentaService cuentas = new CuentaService(usuarios, usuario -> true);
            ResultadoCuenta alta = cuentas.registrar(new SolicitudRegistro(CEDULA, NOMBRE,
                    CORREO, contrasena, NIT, TipoCuenta.PROVEEDOR, EMPRESA));
            if (!alta.exitoso()) {
                System.out.println("No se pudo crear la cuenta: " + alta.error());
                System.exit(1);
            }
            proveedor = alta.usuario();
            proveedor.setDireccionEnvio(DIRECCION);
            usuarios.actualizar(proveedor);
            System.out.println("Cuenta creada: " + CORREO + " (Proveedor, " + EMPRESA + ")");
        } else {
            System.out.println("La cuenta ya existía; no se modifica.");
        }

        CatalogoService catalogo = new CatalogoService(new ProductoRepositoryMongo(conexion.getBase()),
                new CatalogoSubject(), new ImportadorImagen(new ImagenRepositoryMongo(conexion.getBase())));
        Set<String> yaPublicados = catalogo.catalogoDe(CORREO).stream()
                .map(Producto::getNombre).collect(Collectors.toSet());
        int publicados = 0;
        for (Articulo articulo : CATALOGO) {
            if (yaPublicados.contains(articulo.nombre())) {
                continue;
            }
            ResultadoProducto resultado = catalogo.publicar(new SolicitudProducto(articulo.nombre(),
                    articulo.descripcion(), String.valueOf(articulo.precio()),
                    String.valueOf(articulo.descuento()), articulo.categoria(),
                    String.valueOf(articulo.stock()), articulo.url()), CORREO,
                    proveedor.getNombreEmpresa());
            if (!resultado.exitoso()) {
                System.out.println("  No se publicó " + articulo.nombre() + ": " + resultado.error());
            } else {
                publicados++;
            }
        }
        System.out.println("Productos publicados ahora: " + publicados + " | catálogo de "
                + EMPRESA + ": " + catalogo.catalogoDe(CORREO).size());
        conexion.cerrar();
    }
}
