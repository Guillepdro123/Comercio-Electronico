package view.factory.components;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;
import view.factory.IComponentesFactory;
import view.factory.icons.IconoCarrito;
import view.factory.utils.BordeRedondeado;

/**
 * Botón de carrito de la barra superior, con su contador de artículos
 * (<i>badge</i>) superpuesto en la esquina.
 *
 * <p><b>Por qué un {@link JLayeredPane} y no un dibujo a mano.</b> El contador
 * tiene que quedar <em>encima</em> del ícono, solapándolo. Pintarlo dentro del
 * {@code paintComponent} del botón sería justo el patrón que este proyecto
 * tiene prohibido; en cambio, un {@code JLayeredPane} es el contenedor que
 * Swing trae precisamente para superponer componentes. El contador es un
 * {@link JLabel} normal, opaco, con el borde redondeado de la fábrica.</p>
 *
 * <p><b>Encapsulamiento:</b> hacia afuera solo expone
 * {@link #setContador(int)} y {@link #alPulsar(Runnable)}; cómo se coloca el
 * badge o de qué color es son asunto interno.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 1.0
 */
public class BotonCarrito extends JLayeredPane {

    private static final int ANCHO = 52;
    private static final int ALTO = 44;
    private static final int TAM_ICONO = 24;
    private static final int TAM_BADGE = 20;
    /** A partir de aquí el contador se resume, o no cabría en el círculo. */
    private static final int MAXIMO_MOSTRADO = 9;

    private final JButton boton;
    private final JLabel badge;

    /**
     * @param fabrica fábrica de la que salen colores, tipografía y bordes
     */
    public BotonCarrito(IComponentesFactory fabrica) {
        boton = new JButton(new IconoCarrito(fabrica.colorTextoSidebar(), TAM_ICONO)) {
            // Al cambiar de tema, updateComponentTreeUI le pondría el UI de
            // FlatLaf; este botón es un ícono plano sobre la barra y conserva
            // el delegado básico.
            @Override
            public void updateUI() {
                setUI(new BasicButtonUI());
            }
        };
        boton.setBackground(fabrica.colorSidebar());
        boton.setOpaque(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setToolTipText("Ver mi carrito");
        boton.setBounds(0, ALTO - 36, 40, 36);

        badge = new JLabel("", SwingConstants.CENTER);
        badge.setFont(fabrica.fuente(Font.BOLD, 10));
        badge.setForeground(fabrica.colorTextoSobreAcento());
        badge.setBackground(fabrica.colorAcento());
        badge.setOpaque(true);
        // El color exterior tiene que ser el fondo real del padre (la barra
        // superior), o las esquinas del círculo se verían de otro color.
        badge.setBorder(new BordeRedondeado(fabrica.colorAcento(), fabrica.colorSidebar(),
                TAM_BADGE / 2, 1, new Insets(0, 0, 0, 0)));
        badge.setBounds(ANCHO - TAM_BADGE - 6, 0, TAM_BADGE, TAM_BADGE);
        badge.setVisible(false);
        // El badge tapa parcialmente al botón; sin este clic delegado, pulsar
        // justo sobre el número no abriría el carrito. Se escucha
        // 'mousePressed' y no 'mouseClicked': el segundo no se dispara si el
        // ratón se mueve un píxel entre pulsar y soltar, que es el mismo
        // fallo intermitente que tenía el avatar.
        badge.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                boton.doClick();
            }
        });

        setPreferredSize(new Dimension(ANCHO, ALTO));
        add(boton, JLayeredPane.DEFAULT_LAYER);
        add(badge, JLayeredPane.PALETTE_LAYER);
    }

    /**
     * Actualiza el número de artículos.
     *
     * @param unidades total de unidades en el carrito; con cero, el contador
     *                 desaparece en vez de mostrar un "0" que no dice nada
     */
    public void setContador(int unidades) {
        badge.setText(unidades > MAXIMO_MOSTRADO ? MAXIMO_MOSTRADO + "+" : String.valueOf(unidades));
        badge.setVisible(unidades > 0);
    }

    /**
     * @param accion qué hacer al pulsar el carrito
     */
    public void alPulsar(Runnable accion) {
        boton.addActionListener(e -> accion.run());
    }
}
