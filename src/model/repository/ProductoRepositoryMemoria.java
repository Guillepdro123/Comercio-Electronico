package model.repository;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import model.entity.Categoria;
import model.entity.Producto;

/**
 * Implementación en memoria de {@link IProductoRepository}.
 *
 * <p><b>Qué es y qué no es.</b> Es la implementación con la que la aplicación
 * funciona hoy, con un catálogo de ejemplo para poder usarla de punta a punta;
 * no es un simulacro de pruebas. Cuando entre MongoDB Atlas, esta clase se
 * queda como está y {@code app.Main} instancia la otra: por eso los dos
 * repositorios implementan la misma interfaz.</p>
 *
 * <p>La búsqueda normaliza acentos y mayúsculas para que "camara" encuentre
 * "Cámara": es lo mínimo que un usuario espera de una barra de búsqueda.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class ProductoRepositoryMemoria implements IProductoRepository {

    private final List<Producto> productos = new ArrayList<>();
    private final AtomicInteger secuencia = new AtomicInteger();

    /** Crea el repositorio con un catálogo de ejemplo para poder navegar la aplicación. */
    public ProductoRepositoryMemoria() {
        sembrarCatalogo();
    }

    @Override
    public List<Producto> listarTodos() {
        return new ArrayList<>(productos);
    }

    @Override
    public List<Producto> listarPorCategoria(Categoria categoria) {
        if (categoria == null) {
            return listarTodos();
        }
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (p.getCategoria() == categoria) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    @Override
    public List<Producto> buscar(String texto, Categoria categoria) {
        String aguja = normalizar(texto);
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : listarPorCategoria(categoria)) {
            if (aguja.isEmpty()
                    || normalizar(p.getNombre()).contains(aguja)
                    || normalizar(p.getDescripcion()).contains(aguja)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    @Override
    public Producto buscarPorId(String id) {
        for (Producto p : productos) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public List<Producto> listarPorProveedor(String correoProveedor) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (p.getCorreoProveedor().equalsIgnoreCase(correoProveedor)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    @Override
    public Producto guardar(Producto producto) {
        if (producto.getId() == null || producto.getId().isEmpty()) {
            producto.setId("P" + secuencia.incrementAndGet());
            productos.add(producto);
            return producto;
        }
        Producto existente = buscarPorId(producto.getId());
        if (existente == null) {
            productos.add(producto);
            return producto;
        }
        productos.set(productos.indexOf(existente), producto);
        return producto;
    }

    @Override
    public boolean eliminar(String id) {
        Producto existente = buscarPorId(id);
        return existente != null && productos.remove(existente);
    }

    @Override
    public boolean descontarStock(String id, int unidades) {
        Producto producto = buscarPorId(id);
        return producto != null && producto.descontarStock(unidades);
    }

    /** Quita acentos y mayúsculas para comparar como espera un buscador. */
    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinAcentos.toLowerCase().trim();
    }

    private void sembrarCatalogo() {
        String proveedor = "proveedor@empresa.com";
        guardar(new Producto(null, "Audífonos inalámbricos",
                "Bluetooth 5.3, cancelación activa de ruido y 30 horas de batería.",
                320000, 20, Categoria.TECNOLOGIA, 14, "audifonos.png", proveedor));
        guardar(new Producto(null, "Teclado mecánico compacto",
                "Formato 65%, switches silenciosos e iluminación configurable.",
                245000, 0, Categoria.TECNOLOGIA, 8, "teclado.png", proveedor));
        guardar(new Producto(null, "Cámara de seguridad Wi-Fi",
                "Visión nocturna, detección de movimiento y grabación en la nube.",
                180000, 15, Categoria.TECNOLOGIA, 20, "camara.png", proveedor));
        guardar(new Producto(null, "Zapatillas para correr",
                "Amortiguación ligera, ideales para entrenamiento diario en asfalto.",
                289000, 30, Categoria.CALZADO, 11, "zapatillas.png", proveedor));
        guardar(new Producto(null, "Botas de cuero",
                "Cuero legítimo, suela antideslizante y forro interior térmico.",
                410000, 0, Categoria.CALZADO, 5, "botas.png", proveedor));
        guardar(new Producto(null, "Cafetera espresso",
                "15 bares de presión, vaporizador de leche y depósito de 1,5 L.",
                520000, 10, Categoria.HOGAR, 7, "cafetera.png", proveedor));
        guardar(new Producto(null, "Juego de sábanas",
                "Algodón 300 hilos, tamaño queen, incluye dos fundas de almohada.",
                150000, 0, Categoria.HOGAR, 25, "sabanas.png", proveedor));
        guardar(new Producto(null, "Chaqueta impermeable",
                "Cortavientos con capucha plegable y costuras selladas.",
                230000, 25, Categoria.MODA, 9, "chaqueta.png", proveedor));
        guardar(new Producto(null, "Reloj deportivo GPS",
                "Monitor de ritmo cardiaco, GPS integrado y 7 días de autonomía.",
                650000, 12, Categoria.DEPORTE, 6, "reloj.png", proveedor));
        guardar(new Producto(null, "Balón de fútbol profesional",
                "Cosido a máquina, tamaño 5, aprobado para competencia.",
                95000, 0, Categoria.DEPORTE, 30, "balon.png", proveedor));
    }
}
