package model.repository.memoria;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import model.repository.IImagenRepository;

/**
 * Almacén de imágenes en una carpeta <b>relativa</b> al programa
 * ({@code imagenes/}), para cuando no hay MongoDB.
 *
 * <p>Se guarda solo el nombre del archivo dentro de la referencia
 * ({@code img:<nombre>}), nunca la ruta completa: la carpeta puede viajar con
 * el programa a otro equipo o a un instalador y las referencias siguen
 * valiendo. Con Atlas configurado se usa
 * {@code model.repository.mongo.ImagenRepositoryMongo}, que además las
 * comparte entre equipos.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class ImagenRepositoryArchivo implements IImagenRepository {

    /** Carpeta relativa al directorio desde el que se ejecuta el programa. */
    private static final Path CARPETA = Paths.get("imagenes");

    @Override
    public String guardar(byte[] datos, String extension) {
        try {
            Files.createDirectories(CARPETA);
            String nombre = UUID.randomUUID() + "." + extension;
            Files.write(CARPETA.resolve(nombre), datos);
            return PREFIJO + nombre;
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo guardar la imagen", ex);
        }
    }

    @Override
    public byte[] leer(String referencia) {
        if (referencia == null || !referencia.startsWith(PREFIJO)) {
            return null;
        }
        // Solo el nombre: una referencia manipulada con "../" no sale de la carpeta.
        Path archivo = CARPETA.resolve(Paths.get(referencia.substring(PREFIJO.length()))
                .getFileName().toString());
        try {
            return Files.exists(archivo) ? Files.readAllBytes(archivo) : null;
        } catch (IOException ex) {
            return null;
        }
    }
}
