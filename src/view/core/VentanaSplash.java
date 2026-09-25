package view.core;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import view.factory.IComponentesFactory;
import view.factory.effects.IndicadorCarga;

/**
 * Pantalla de bienvenida que se muestra al arrancar la aplicación, mientras
 * se prepara la ventana de Login.
 *
 * <p><b>Por qué existe.</b> Abrir directamente sobre un lienzo en blanco
 * aumenta la sensación de lentitud; una pantalla de marca ocupa ese momento
 * mostrando el logotipo y, sobre todo, <em>diciendo qué está pasando</em>.
 * Los mensajes que se van sucediendo convierten una espera pasiva en una
 * activa, que se percibe más corta, y el indicador de avance da la respuesta
 * a la única pregunta que importa mientras se espera: cuánto falta.</p>
 *
 * <p><b>Solo en el arranque en frío.</b> {@code app.Main} la muestra desde
 * {@code main(...)}, no desde
 * {@link app.Main#mostrarVentanaPrincipal(model.repository.IUsuarioRepository,
 * IComponentesFactory)}: al cerrar sesión la aplicación ya está en marcha y
 * repetir el splash sería una espera gratuita.</p>
 *
 * <p><b>Responsabilidad única:</b> esta clase solo compone y anima la
 * bienvenida. No sabe qué viene después: recibe esa acción como
 * {@code Runnable} en {@link #mostrar(Runnable)}, así que podría precederse a
 * cualquier pantalla sin tocar su código (Abierto/Cerrado). Todos sus
 * componentes salen de {@link IComponentesFactory}, como el resto de las
 * vistas.</p>
 *
 * <p>Es un {@link JWindow} (ventana sin barra de título) porque una pantalla
 * de carga no se minimiza, no se cierra a mano y no se redimensiona: ofrecer
 * esos controles sería ruido.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public class VentanaSplash extends JWindow {

    private static final int ANCHO = 460;
    private static final int ALTO = 340;
    private static final int TAM_LOGO = 120;
    private static final int TAM_INDICADOR = 42;

    private static final int MS_POR_PASO = 30;
    private static final int PASOS_TOTALES = 90;

    /** Mensajes que se van turnando; el último queda fijo al completarse. */
    private static final String[] MENSAJES = {
        "Cargando módulos...",
        "Iniciando sistemas...",
        "Preparando tu espacio...",
        "Todo listo"
    };

    private final IndicadorCarga indicador;
    private final JLabel lblEstado;
    private int paso;

    /**
     * @param fabrica fábrica de la que salen el logotipo, los colores y la
     *                tipografía; la pantalla no define estilo propio
     */
    public VentanaSplash(IComponentesFactory fabrica) {
        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(fabrica.colorFondo());
        contenido.setBorder(BorderFactory.createLineBorder(fabrica.colorAcento(), 1));

        JLabel lblLogo = fabrica.crearLogo(TAM_LOGO);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("Comercio Electrónico");
        lblTitulo.setFont(fabrica.fuente(Font.BOLD, 22));
        lblTitulo.setForeground(fabrica.colorTexto());
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setBorder(new EmptyBorder(14, 0, 0, 0));

        indicador = fabrica.crearIndicadorProgreso(TAM_INDICADOR);
        indicador.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblEstado = new JLabel(MENSAJES[0]);
        lblEstado.setFont(fabrica.fuente(Font.PLAIN, 13));
        lblEstado.setForeground(fabrica.colorTextoSuave());
        lblEstado.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblEstado.setBorder(new EmptyBorder(14, 0, 0, 0));

        JPanel columna = new JPanel();
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setBackground(fabrica.colorFondo());
        columna.setBorder(new EmptyBorder(24, 30, 24, 30));
        // El relleno elástico a ambos lados centra el bloque verticalmente,
        // en vez de dejarlo pegado arriba con un hueco muerto debajo.
        columna.add(Box.createVerticalGlue());
        columna.add(lblLogo);
        columna.add(lblTitulo);
        columna.add(Box.createRigidArea(new Dimension(0, 28)));
        columna.add(indicador);
        columna.add(lblEstado);
        columna.add(Box.createVerticalGlue());

        contenido.add(columna, BorderLayout.CENTER);
        setContentPane(contenido);
        setSize(ANCHO, ALTO);
        setLocationRelativeTo(null);
    }

    /**
     * Muestra la pantalla, anima el avance y, al terminar, la destruye y
     * ejecuta la acción siguiente.
     *
     * @param alTerminar qué abrir cuando la bienvenida acabe (normalmente, la
     *                   ventana de Login)
     */
    public void mostrar(Runnable alTerminar) {
        setVisible(true);
        indicador.iniciar();

        Timer temporizador = new Timer(MS_POR_PASO, null);
        temporizador.addActionListener(e -> {
            paso++;
            indicador.setProgreso(paso / (double) PASOS_TOTALES);
            lblEstado.setText(mensajeParaElPaso());

            if (paso >= PASOS_TOTALES) {
                ((Timer) e.getSource()).stop();
                indicador.detener();
                dispose();
                alTerminar.run();
            }
        });
        temporizador.start();
    }

    /** Reparte los mensajes a lo largo del avance, en orden. */
    private String mensajeParaElPaso() {
        int indice = Math.min(MENSAJES.length - 1, paso * MENSAJES.length / PASOS_TOTALES);
        return MENSAJES[indice];
    }
}
