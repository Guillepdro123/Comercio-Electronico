package aplicacion.catalogo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import model.entity.Categoria;
import model.entity.Producto;
import model.entity.Proveedor;
import model.repository.IImagenRepository;
import model.repository.memoria.ProductoRepositoryMemoria;
import model.repository.memoria.UsuarioRepositoryImpl;

/**
 * Comprobación automática de la portabilidad de imágenes y de la marca del
 * vendedor ({@link ImportadorImagen}, {@link NormalizacionCatalogo} y
 * {@code IProductoRepository.actualizarVendedor}).
 *
 * <p>La regla que defiende: en la base nunca queda una ruta del disco, porque
 * {@code C:\Users\...} no existe en otro equipo ni dentro del {@code .exe}.</p>
 *
 * <p>El almacén de imágenes es uno de prueba en memoria: el de archivos
 * escribiría una carpeta {@code imagenes/} en el proyecto.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public final class VerificacionImagenesPortables {

    private static int fallos = 0;

    private VerificacionImagenesPortables() {
    }

    /** Almacén de imágenes de prueba: guarda los bytes en un mapa. */
    private static final class ImagenesEnMemoria implements IImagenRepository {
        private final Map<String, byte[]> datos = new HashMap<>();

        @Override
        public String guardar(byte[] contenido, String extension) {
            String referencia = PREFIJO + (datos.size() + 1);
            datos.put(referencia, contenido);
            return referencia;
        }

        @Override
        public byte[] leer(String referencia) {
            return datos.get(referencia);
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
     * @throws IOException si no se puede crear la imagen temporal de prueba
     */
    public static void main(String[] args) throws IOException {
        ImagenesEnMemoria almacen = new ImagenesEnMemoria();
        ImportadorImagen importador = new ImportadorImagen(almacen);
        ProductoRepositoryMemoria productos = new ProductoRepositoryMemoria();
        UsuarioRepositoryImpl usuarios = new UsuarioRepositoryImpl();
        Proveedor luis = new Proveedor("100200", "Luis", "luis@x.com", "x", "900123-1");
        luis.setNombreEmpresa("Tech");
        usuarios.registrar(luis);

        File foto = File.createTempFile("foto", ".png");
        foto.deleteOnExit();
        ImageIO.write(new BufferedImage(1600, 1200, BufferedImage.TYPE_INT_RGB), "png", foto);

        System.out.println("Importar al publicar");
        String referencia = importador.resolver(foto.getAbsolutePath());
        comprobar("una ruta absoluta se convierte en img:<id>",
                referencia.startsWith(IImagenRepository.PREFIJO)
                && !referencia.contains(":\\") && !referencia.contains("/"));
        BufferedImage guardada = ImageIO.read(new ByteArrayInputStream(almacen.leer(referencia)));
        comprobar("la imagen guardada se redujo a 800 px de lado",
                guardada.getWidth() == 800 && guardada.getHeight() == 600);
        comprobar("una ilustración incluida se deja tal cual",
                "camara.png".equals(importador.resolver("camara.png")));
        comprobar("una ruta de otro equipo con nombre de ilustración queda en el nombre",
                "camara.png".equals(importador.resolver("C:\\Users\\otro\\camara.png")));
        boolean rechazada = false;
        try {
            importador.resolver("C:\\Users\\otro\\no-existe.png");
        } catch (IllegalArgumentException ex) {
            rechazada = true;
        }
        comprobar("una ruta inexistente se rechaza al publicar", rechazada);

        System.out.println("Normalizar el catálogo al arrancar");
        Producto deLuis = productos.guardar(new Producto(null, "Mouse", "Inalámbrico", 50000, 0,
                Categoria.TECNOLOGIA, 5, "", "luis@x.com"));
        Producto perdida = productos.buscar("Cafetera", null).get(0);
        perdida.setImagen("D:\\fotos\\desaparecida.jpg");
        Producto local = productos.buscar("Balón", null).get(0);
        local.setImagen(foto.getAbsolutePath());
        int corregidos = new NormalizacionCatalogo(productos, usuarios, importador).ejecutar();
        comprobar("corrige las 2 rutas y pone la marca al producto que no la tenía",
                corregidos == 3 && "Tech".equals(deLuis.getMarca())
                && perdida.getImagen().isEmpty() && local.getImagen().startsWith("img:"));
        comprobar("no queda ninguna ruta local en el catálogo",
                productos.listarTodos().stream().noneMatch(p -> importador.esRutaLocal(p.getImagen())));
        comprobar("una segunda normalización no cambia nada",
                new NormalizacionCatalogo(productos, usuarios, importador).ejecutar() == 0);

        System.out.println("Marca del vendedor");
        int movidos = productos.actualizarVendedor("luis@x.com", "luis.nuevo@x.com", "Tech Pro");
        comprobar("cambiar correo y empresa mueve sus productos",
                movidos == 1 && "Tech Pro".equals(deLuis.getMarca())
                && "luis.nuevo@x.com".equals(deLuis.getCorreoProveedor()));

        System.out.println(fallos == 0 ? "\nTODO CORRECTO" : "\n" + fallos + " FALLAS");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
