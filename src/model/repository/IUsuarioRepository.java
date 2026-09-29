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
     * <p><b>Identificación y correo son únicos.</b> Toda implementación debe
     * rechazar un usuario cuya identificación o cuyo correo (sin distinguir
     * mayúsculas) ya estén registrados: el correo es la credencial del Login,
     * y dos cuentas con el mismo correo dejarían a una de ellas sin acceso.</p>
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
}
