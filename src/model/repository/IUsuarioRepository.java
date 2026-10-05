package model.repository;

import model.entity.Usuario;

/**
 * Contrato de persistencia para la entidad raíz {@link Usuario}.
 *
 * <p><b>Abstracción + Inversión de Dependencias (D de SOLID):</b> el controlador
 * depende de esta interfaz y no de una implementación concreta. Cuando el
 * siguiente incremento reemplace la lista en memoria por MySQL o por un archivo,
 * bastará con crear una nueva clase que implemente esta interfaz sin modificar
 * ni una línea del controlador ni de la vista.</p>
 *
 * <p><b>Segregación de Interfaces (I de SOLID):</b> solo se expone lo que
 * los casos de uso actuales necesitan: registrar (Incremento 1) y buscar por
 * correo, para el Login (Incremento 3). Actualización y eliminación se
 * incorporarán cuando un caso de uso lo requiera.</p>
 *
 * @author Ingeniería de Sistemas - Primer Incremento Funcional
 * @version 1.1
 */
public interface IUsuarioRepository {

    /**
     * Persiste un usuario en el almacén de datos.
     *
     * <p>Gracias al polimorfismo, el parámetro declarado como {@code Usuario}
     * acepta indistintamente instancias de {@code Cliente} o de
     * {@code Proveedor}.</p>
     *
     * <p><b>Toda implementación debe rechazar una identificación o un correo
     * ya registrados</b> (el correo, sin distinguir mayúsculas). Esta es la
     * última palabra sobre la unicidad y vale para cualquier forma de dar de
     * alta una cuenta, sea el formulario o el acceso con Google. Quien quiera
     * explicar al usuario el motivo exacto debe comprobarlo antes con
     * {@link #buscarPorCorreo(String)}: aquí solo se devuelve si se pudo.</p>
     *
     * @param usuario usuario a registrar; no debe ser {@code null}
     * @return {@code true} si el registro fue exitoso, {@code false} si el
     *         usuario es inválido o ya existía uno con la misma identificación
     *         o el mismo correo
     */
    boolean registrar(Usuario usuario);

    /**
     * Busca un usuario registrado por su correo electrónico. Es una consulta
     * pura: no valida contraseña ni ninguna otra regla de negocio, eso es
     * responsabilidad de quien la use (por ejemplo, {@code LoginController}).
     *
     * @param correo correo a buscar, no sensible a mayúsculas/minúsculas
     * @return el usuario encontrado, o {@code null} si no existe ninguno con ese correo
     */
    Usuario buscarPorCorreo(String correo);

    /**
     * Guarda los cambios de un usuario ya registrado.
     *
     * <p>Se incorpora al aparecer el caso de uso "editar perfil", siguiendo la
     * regla del proyecto de no declarar operaciones antes de que alguien las
     * necesite. Rechaza el cambio si el correo nuevo ya lo usa otra cuenta: el
     * correo es con lo que se inicia sesión, y dos cuentas con el mismo correo
     * dejarían el acceso ambiguo.</p>
     *
     * @param usuario usuario con los datos ya modificados
     * @return {@code true} si se actualizó; {@code false} si no estaba
     *         registrado o el correo pertenece a otra cuenta
     */
    boolean actualizar(Usuario usuario);

    /**
     * Dice si una cédula ya pertenece a otra cuenta, como cédula o como
     * identificación.
     *
     * <p>Aparece con la cédula de las cuentas de Google: dos personas no
     * pueden facturar con el mismo documento.</p>
     *
     * @param cedula               documento a comprobar
     * @param identificacionPropia cuenta que lo quiere usar (no cuenta como
     *                             "otra")
     * @return {@code true} si lo tiene otra cuenta
     */
    boolean cedulaEnUso(String cedula, String identificacionPropia);
}
