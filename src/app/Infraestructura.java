package app;

import model.repository.IImagenRepository;
import model.repository.IPedidoRepository;
import model.repository.IProductoRepository;
import model.repository.IResenaRepository;
import model.repository.IUsuarioRepository;
import observer.CatalogoSubject;
import service.correo.INotificadorCorreo;
import service.google.IAutenticadorExterno;
import view.factory.IComponentesFactory;

/**
 * Las piezas compartidas que {@link Main} crea una sola vez al arrancar y que
 * viajan intactas durante toda la ejecución, incluidos los cierres de sesión.
 *
 * <p><b>Por qué un registro.</b> Con las reseñas y las imágenes,
 * {@code Main.mostrarVentanaPrincipal} y {@code SesionController} pasaban a
 * recibir nueve parámetros, todos los mismos y en el mismo orden. Agruparlos
 * no los oculta —siguen siendo todos interfaces— pero evita cruzarlos al
 * pasarlos, y añadir uno ya no obliga a tocar cada firma.</p>
 *
 * <p><b>Nunca se crean de nuevo.</b> Un repositorio nuevo en un cierre de
 * sesión perdería las cuentas registradas en memoria, y un sujeto nuevo
 * dejaría sin destino los avisos del vigilante de MongoDB.</p>
 *
 * @param usuarios    cuentas
 * @param productos   catálogo
 * @param pedidos     compras
 * @param resenas     reseñas de productos
 * @param imagenes    imágenes subidas por los proveedores
 * @param notificador correos transaccionales
 * @param catalogo    sujeto del patrón Observer
 * @param google      acceso con Google, o {@code null} si no está configurado
 * @param fabrica     fábrica de componentes (con el tema actual)
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public record Infraestructura(IUsuarioRepository usuarios, IProductoRepository productos,
                              IPedidoRepository pedidos, IResenaRepository resenas,
                              IImagenRepository imagenes, INotificadorCorreo notificador,
                              CatalogoSubject catalogo, IAutenticadorExterno google,
                              IComponentesFactory fabrica) {
}
