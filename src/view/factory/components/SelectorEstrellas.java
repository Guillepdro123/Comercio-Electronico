package view.factory.components;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import com.formdev.flatlaf.FlatClientProperties;
import view.factory.IComponentesFactory;

/**
 * Control para calificar de 1 a 5 estrellas.
 *
 * <p><b>Cinco botones, no una etiqueta dibujada.</b> Cada estrella se pulsa, y
 * lo que se pulsa es un botón (regla del proyecto: {@code mouseClicked} sobre
 * una etiqueta falla con un píxel de arrastre). De paso, el teclado puede
 * llegar a cada una con el tabulador.</p>
 *
 * <p>Al pasar el puntero se previsualiza la calificación hasta esa estrella;
 * al salir, vuelve a la elegida. Arranca sin ninguna: que el usuario no haya
 * elegido es distinto de que haya elegido una.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class SelectorEstrellas extends JPanel {

    private static final int TAM_ESTRELLA = 22;

    private final IComponentesFactory fabrica;
    private final List<JButton> botones = new ArrayList<>();
    private int seleccion;

    /**
     * @param fabrica fábrica de la que salen los colores
     */
    public SelectorEstrellas(IComponentesFactory fabrica) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.fabrica = fabrica;
        setOpaque(false);
        for (int i = 1; i <= 5; i++) {
            int valor = i;
            JButton estrella = new JButton();
            estrella.setToolTipText(valor == 1 ? "1 estrella" : valor + " estrellas");
            estrella.setCursor(new Cursor(Cursor.HAND_CURSOR));
            // Sin fondo propio: solo se ve la estrella. Lo pinta FlatLaf.
            estrella.putClientProperty(FlatClientProperties.STYLE,
                    "buttonType: toolBarButton; margin: 2,2,2,2; focusWidth: 0");
            estrella.addActionListener(e -> {
                seleccion = valor;
                pintar(seleccion);
            });
            estrella.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    pintar(valor);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    pintar(seleccion);
                }
            });
            botones.add(estrella);
            add(estrella);
        }
        pintar(0);
    }

    /** @return la calificación elegida, o 0 si no se eligió ninguna */
    public int getSeleccion() {
        return seleccion;
    }

    /**
     * @param estrellas calificación a mostrar elegida (por ejemplo, la de una
     *                  reseña anterior del mismo usuario)
     */
    public void setSeleccion(int estrellas) {
        seleccion = estrellas;
        pintar(seleccion);
    }

    private void pintar(int hasta) {
        for (int i = 0; i < botones.size(); i++) {
            botones.get(i).setIcon(fabrica.crearEstrella(i < hasta, TAM_ESTRELLA));
        }
    }
}
