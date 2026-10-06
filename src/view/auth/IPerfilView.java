package view.auth;

import java.util.function.Consumer;

/**
 * Contrato que {@link controller.PerfilController} necesita de la pantalla de
 * edición de perfil.
 *
 * <p><b>Por qué vive en {@code view.auth}</b> y no en un paquete propio:
 * comparte vocabulario con el Registro —los mismos campos, la misma política
 * de contraseña, los mismos mensajes de validación— y es la misma familia de
 * pantallas, las que gestionan la cuenta. Login y Registro ya están aquí por
 * esa razón.</p>
 *
 * <p><b>Segregación de Interfaces:</b> declara solo lo que el controlador
 * usa. La vista no sabe validar ni guardar; muestra, recoge y avisa.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public interface IPerfilView {

    /**
     * Carga en el formulario los datos actuales de la cuenta.
     *
     * @param datos valores vigentes, ya resueltos por el controlador
     */
    void mostrarDatos(DatosPerfil datos);

    /**
     * Señala que algo de lo escrito no es válido. El formulario **no** se
     * cierra: el usuario tiene que poder corregir sin volver a teclearlo todo.
     *
     * @param mensaje qué está mal, en una frase
     */
    void mostrarError(String mensaje);

    /**
     * Confirma que los cambios quedaron guardados.
     *
     * @param mensaje resumen para el usuario
     */
    void mostrarExito(String mensaje);

    /** Cierra el formulario. Lo pide el controlador cuando ya guardó. */
    void cerrar();

    /** Muestra el formulario y cede el control hasta que se cierre. */
    void abrir();

    /**
     * @param accion qué hacer cuando el usuario pulsa "Guardar cambios"
     */
    void alGuardar(Consumer<CambiosPerfil> accion);
}
