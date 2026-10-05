package model.repository.memoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.entity.Usuario;
import model.repository.IUsuarioRepository;

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
     * <p>Rechaza valores nulos, identificaciones o correos vacíos, y una
     * identificación o un correo ya registrados. El correo se compara sin
     * distinguir mayúsculas, igual que {@link #buscarPorCorreo(String)}: si
     * no, "Ana@x.com" y "ana@x.com" serían dos cuentas y el Login entraría en
     * cualquiera de las dos.</p>
     */
    @Override
    public boolean registrar(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        if (usuario.getIdentificacion() == null || usuario.getIdentificacion().trim().isEmpty()
                || usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty()) {
            return false;
        }
        if (existeIdentificacion(usuario.getIdentificacion())
                || buscarPorCorreo(usuario.getCorreo()) != null) {
            return false;
        }
        return usuarios.add(usuario);
    }

    /**
     * {@inheritDoc}
     *
     * <p>La lista guarda las mismas instancias que entregó
     * {@code buscarPorCorreo}, así que quien edita un usuario ya modificó el
     * objeto que está aquí dentro. Este método existe igualmente porque el
     * controlador no debe dar por supuesto ese detalle: el día que detrás haya
     * MongoDB, esta llamada será la que escriba el documento.</p>
     */
    @Override
    public boolean actualizar(Usuario usuario) {
        if (usuario == null || !usuarios.contains(usuario)) {
            return false;
        }
        return !correoDeOtraCuenta(usuario);
    }

    /** {@inheritDoc} */
    @Override
    public boolean cedulaEnUso(String cedula, String identificacionPropia) {
        for (Usuario u : usuarios) {
            if (!u.getIdentificacion().equals(identificacionPropia)
                    && (cedula.equals(u.getCedula()) || cedula.equals(u.getIdentificacion()))) {
                return true;
            }
        }
        return false;
    }

    /** @return {@code true} si el correo del usuario ya lo usa otra cuenta */
    private boolean correoDeOtraCuenta(Usuario usuario) {
        for (Usuario u : usuarios) {
            if (u != usuario && u.getCorreo() != null
                    && u.getCorreo().equalsIgnoreCase(usuario.getCorreo())) {
                return true;
            }
        }
        return false;
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
