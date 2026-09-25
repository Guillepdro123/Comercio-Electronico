package model.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.entity.Usuario;

/**
 * Implementación en memoria de {@link IUsuarioRepository}.
 *
 * <p>Utiliza una {@code List<Usuario>} como almacén temporal. Por el principio
 * de sustitución de Liskov, la lista declarada del tipo base acepta y conserva
 * objetos {@code Cliente} y {@code Proveedor} sin perder su identidad: cuando se
 * recorren los elementos, cada uno sigue respondiendo con su propia
 * implementación de los métodos abstractos.</p>
 *
 * <p><b>Nota:</b> los datos se pierden al cerrar la aplicación. Esta clase es el
 * punto exacto que se reemplazará por la persistencia real en el siguiente
 * incremento.</p>
 *
 * @author Ingeniería de Sistemas - Primer Incremento Funcional
 * @version 1.0
 */
public class UsuarioRepositoryImpl implements IUsuarioRepository {

    /** Almacén temporal de usuarios registrados. */
    private final List<Usuario> usuarios;

    /** Inicializa el almacén vacío. */
    public UsuarioRepositoryImpl() {
        this.usuarios = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Rechaza valores nulos, identificaciones vacías, identificaciones
     * repetidas y correos ya registrados.</p>
     */
    @Override
    public boolean registrar(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        if (usuario.getIdentificacion() == null || usuario.getIdentificacion().trim().isEmpty()) {
            return false;
        }
        if (existeIdentificacion(usuario.getIdentificacion())) {
            return false;
        }
        // El correo es la llave del Login: si se repitiera, buscarPorCorreo
        // devolvería siempre la primera cuenta y la segunda nunca podría
        // entrar. Por eso la regla vive aquí, en el contrato, y no solo en el
        // controlador: vale para cualquier alta, venga de donde venga.
        if (buscarPorCorreo(usuario.getCorreo()) != null) {
            return false;
        }
        return usuarios.add(usuario);
    }

    /**
     * Verifica si ya hay un usuario con la identificación indicada.
     *
     * @param identificacion documento a buscar
     * @return {@code true} si la identificación ya está registrada
     */
    private boolean existeIdentificacion(String identificacion) {
        for (Usuario u : usuarios) {
            if (u.getIdentificacion().equalsIgnoreCase(identificacion.trim())) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Usuario buscarPorCorreo(String correo) {
        if (correo == null) {
            return null;
        }
        for (Usuario u : usuarios) {
            if (u.getCorreo().equalsIgnoreCase(correo.trim())) {
                return u;
            }
        }
        return null;
    }

    /**
     * Vista de solo lectura del almacén. Se expone en la implementación (no en
     * la interfaz) para depuración y para preparar el incremento de consulta.
     *
     * @return lista inmodificable de usuarios registrados
     */
    public List<Usuario> listar() {
        return Collections.unmodifiableList(usuarios);
    }

    /**
     * Cantidad de usuarios registrados en la sesión actual.
     *
     * @return total de registros
     */
    public int contar() {
        return usuarios.size();
    }
}
