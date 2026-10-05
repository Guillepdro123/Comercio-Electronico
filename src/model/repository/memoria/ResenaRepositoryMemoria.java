package model.repository.memoria;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.entity.Resena;
import model.repository.IResenaRepository;

/**
 * Reseñas en memoria, para cuando no hay MongoDB configurado.
 *
 * <p>Sus métodos van {@code synchronized} por la misma razón que los de stock
 * de {@link ProductoRepositoryMemoria}: se escriben desde un hilo de fondo
 * mientras el catálogo puede estar leyéndolas.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class ResenaRepositoryMemoria implements IResenaRepository {

    private final List<Resena> resenas = new ArrayList<>();

    @Override
    public synchronized void guardar(Resena resena) {
        resenas.removeIf(r -> r.getIdProducto().equals(resena.getIdProducto())
                && r.getCorreoAutor().equalsIgnoreCase(resena.getCorreoAutor()));
        resenas.add(resena);
    }

    @Override
    public synchronized List<Resena> listarPorProducto(String idProducto) {
        List<Resena> resultado = new ArrayList<>();
        for (Resena r : resenas) {
            if (r.getIdProducto().equals(idProducto)) {
                resultado.add(r);
            }
        }
        resultado.sort(Comparator.comparing(Resena::getFecha).reversed());
        return resultado;
    }

    @Override
    public synchronized Map<String, double[]> resumenPorProducto() {
        Map<String, double[]> sumas = new HashMap<>();
        for (Resena r : resenas) {
            double[] acumulado = sumas.computeIfAbsent(r.getIdProducto(), id -> new double[2]);
            acumulado[0] += r.getEstrellas();
            acumulado[1]++;
        }
        for (double[] acumulado : sumas.values()) {
            acumulado[0] = acumulado[0] / acumulado[1];
        }
        return sumas;
    }
}
