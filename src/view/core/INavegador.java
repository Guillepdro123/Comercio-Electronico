package view.core;

import java.awt.CardLayout;
import view.auth.PanelLogin;
import view.auth.PanelRegistroUsuario;

/**
 * Contrato mínimo de navegación entre cartas del {@link CardLayout} de
 * {@link MainFrame}.
 *
 * <p><b>Inversión de Dependencias (D de SOLID):</b> las vistas (por ejemplo,
 * el enlace "¿Ya tienes cuenta? Inicia sesión" de
 * {@link PanelRegistroUsuario}, o "¿No tienes cuenta? Regístrate" de
 * {@link PanelLogin}) dependen de esta abstracción, nunca de
 * {@link MainFrame} directamente. Así un panel se puede probar o reutilizar
 * sin arrastrar toda la ventana.</p>
 *
 * <p><b>Abierto/Cerrado (O de SOLID):</b> agregar una carta nueva no
 * requiere tocar esta interfaz ni a {@link MainFrame}: solo llamar
 * {@code agregarCarta(...)} desde el ensamblador y usar el nombre de esa
 * carta en algún {@code mostrarCarta(...)}.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.1
 */
public interface INavegador {

    /**
     * Cambia la carta visible.
     *
     * @param nombreCarta identificador de la carta a mostrar (ver las
     *                    constantes {@code NOMBRE_CARTA} de cada panel)
     */
    void mostrarCarta(String nombreCarta);
}
