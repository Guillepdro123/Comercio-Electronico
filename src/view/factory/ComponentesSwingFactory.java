package view.factory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.text.JTextComponent;
import view.factory.components.CampoPasswordConToggle;
import view.factory.components.CampoTextoConIcono;
import view.factory.components.SelectorSegmentado;
import view.factory.effects.AnimacionConfeti;
import view.factory.effects.IndicadorCarga;
import view.factory.effects.SonidoExito;
import view.factory.icons.IconoCampo;
import view.factory.icons.IconoGifAnimado;
import view.factory.icons.IconoImagenEscalada;
import view.factory.icons.IconoPunto;
import view.factory.utils.BordeRedondeado;

/**
 * Única implementación hoy de {@link IComponentesFactory}: aplica la paleta
 * "SaaS/Tech Morado" (fondo {@code #13111C}, sidebar {@code #1B1627},
 * tarjetas/formularios {@code #231F3D}, acento violeta {@code #8B5CF6}) y
 * una tipografía unificada a cada componente que crea.
 *
 * <p><b>Responsabilidad única:</b> esta es la única clase del proyecto que
 * conoce los valores concretos de colores y fuentes; ninguna vista vuelve a
 * instanciarlos. Un futuro tema (por ejemplo, modo claro) se agregaría como
 * otra clase que implemente {@link IComponentesFactory}, sin modificar las
 * vistas que ya consumen la fábrica (Abierto/Cerrado).</p>
 *
 * <p><b>Estabilidad ante todo:</b> todos los componentes siguen siendo Swing
 * estándar y <b>opacos</b>. El redondeo de esquinas se logra con
 * {@link BordeRedondeado}, un {@code Border} que tapa las esquinas con el
 * color del contenedor — nunca con {@code setOpaque(false)} en cascada ni con
 * un {@code paintComponent} propio, que es lo que en una iteración anterior
 * produjo conflictos de color y texto invisible (ver "Estabilidad de la
 * interfaz" en {@code CLAUDE.md}).</p>
 *
 * <p><b>Elevación por luminosidad:</b> la paleta está ordenada en capas, como
 * pide el diseño en modo oscuro (donde las sombras se ven duras y se
 * sustituyen por variaciones de luz): fondo {@code #13111C} → sidebar
 * {@code #1B1627} → tarjetas {@code #231F3D} → campos {@code #2A2545}. Cada
 * nivel más interactivo se acerca un paso a la luz.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 2.0
 */
public class ComponentesSwingFactory implements IComponentesFactory {

    private static final Color COLOR_FONDO = new Color(0x13111C);
    private static final Color COLOR_PANEL = new Color(0x231F3D);
    private static final Color COLOR_SIDEBAR = new Color(0x1B1627);
    private static final Color COLOR_CAMPO = new Color(0x2A2545);
    private static final Color COLOR_ACENTO = new Color(0x8B5CF6);
    private static final Color COLOR_ACENTO_HOVER = new Color(0xA78BFA);
    private static final Color COLOR_TEXTO = Color.WHITE;
    private static final Color COLOR_TEXTO_SUAVE = new Color(0xF3F4F6);
    private static final Color COLOR_BORDE = new Color(0x362F52);
    private static final Color COLOR_ERROR = new Color(0xF87171);
    private static final Color COLOR_EXITO = new Color(0x4ADE80);
    private static final Color COLOR_ADVERTENCIA = new Color(0xFBBF24);

    private static final String FAMILIA_TIPOGRAFICA = "Segoe UI";
    private static final int ANCHO_CAMPO = 300;
    private static final int ALTO_CAMPO = 38;
    private static final int TAM_ICONO_CAMPO = 16;

    private static final int RADIO_CAMPO = 10;
    private static final int RADIO_BOTON = 12;
    private static final int RADIO_TARJETA = 18;

    /** Ruta relativa del logo dentro del proyecto (la que usa NetBeans al ejecutar desde el IDE). */
    private static final String RUTA_LOGO = "src/resources/images/logo.png";

    /** Carpeta relativa de los íconos (asistente animado, check de éxito, ...). */
    private static final String RUTA_ICONOS = "src/resources/images/icons/";

    private static final String ARCHIVO_ASISTENTE = "ecommerce_cart.gif";
    private static final String ARCHIVO_CHECK = "check.png";
    private static final int TAM_ICONO_DIALOGO = 40;

    private static final int TAM_INDICADOR_TRANSICION = 26;
    private static final int MS_TRANSICION = 1400;

    private static final int CRECIMIENTO_HOVER_PX = 10;
    private static final int PASOS_ANIMACION_HOVER = 6;
    private static final int PASOS_ENTRADA_DIALOGO = 10;
    private static final int DESPLAZAMIENTO_ENTRADA_DIALOGO_PX = 24;

    /**
     * Al construirse, la fábrica fija los colores base del Look and Feel.
     *
     * <p>Nimbus pinta sus superficies (paneles, diálogos, {@code JOptionPane})
     * con painters propios que ignoran {@code setBackground}, así que un
     * diálogo quedaba con fondo claro en medio de la paleta oscura. Estas
     * claves son la vía documentada para recolorear esa base; se aplican aquí
     * —y no en {@code app.Main}— porque los colores del tema son
     * responsabilidad de la fábrica: otro tema los fijaría a sus propios
     * valores sin tocar el ensamblador.</p>
     */
    public ComponentesSwingFactory() {
        UIManager.put("control", COLOR_PANEL);
        UIManager.put("Panel.background", COLOR_PANEL);
        UIManager.put("OptionPane.background", COLOR_PANEL);
        UIManager.put("OptionPane.messageForeground", COLOR_TEXTO);
        UIManager.put("nimbusLightBackground", COLOR_CAMPO);
    }

    @Override
    public JLabel crearEtiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(COLOR_TEXTO_SUAVE);
        etiqueta.setFont(fuente(Font.BOLD, 12));
        return etiqueta;
    }

    @Override
    public CampoTextoConIcono crearCampoTexto(String textoFantasma, IconoCampo.Tipo tipoIcono) {
        return new CampoTextoConIcono(this, textoFantasma, tipoIcono);
    }

    @Override
    public CampoPasswordConToggle crearCampoPassword(String textoFantasma) {
        return new CampoPasswordConToggle(this, textoFantasma);
    }

    @Override
    public SelectorSegmentado crearSelectorTipoCuenta(String[] opciones, Runnable alCambiar) {
        return new SelectorSegmentado(this, opciones, alCambiar);
    }

    @Override
    public JButton crearBotonSegmento(String texto) {
        JButton segmento = new JButton(texto);
        segmento.setUI(new BasicButtonUI());
        segmento.setFont(fuente(Font.BOLD, 13));
        segmento.setOpaque(true);
        segmento.setFocusPainted(false);
        segmento.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resaltarSegmento(segmento, false);
        return segmento;
    }

    @Override
    public void resaltarSegmento(JButton segmento, boolean activo) {
        Color fondo = activo ? COLOR_ACENTO : COLOR_CAMPO;
        segmento.setBackground(fondo);
        segmento.setForeground(activo ? COLOR_TEXTO : COLOR_TEXTO_SUAVE);
        segmento.setBorder(new BordeRedondeado(
                activo ? COLOR_ACENTO : COLOR_BORDE, COLOR_PANEL, RADIO_CAMPO, 1, new Insets(6, 10, 6, 10)));
    }

    @Override
    public void instalarAnilloEnfoque(JComponent contenedor, JTextComponent campo) {
        contenedor.setBorder(bordeCampo(false));
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                contenedor.setBorder(bordeCampo(true));
            }

            @Override
            public void focusLost(FocusEvent e) {
                contenedor.setBorder(bordeCampo(false));
            }
        });
    }

    /**
     * Borde redondeado de un campo de captura. Con {@code enfocado} en
     * {@code true} usa el color de acento y un trazo más grueso: es el
     * "anillo de foco" que señala dónde está escribiendo el usuario.
     */
    private BordeRedondeado bordeCampo(boolean enfocado) {
        return new BordeRedondeado(
                enfocado ? COLOR_ACENTO : COLOR_BORDE,
                COLOR_PANEL,
                RADIO_CAMPO,
                enfocado ? 2 : 1,
                new Insets(6, 12, 6, 12));
    }

    @Override
    public JButton crearBotonPrimario(String texto) {
        JButton boton = new JButton(texto);
        // Igual que en el combo: Nimbus pinta los botones con su propio
        // gradiente y tapa el color del tema. BasicButtonUI (estándar del
        // JDK) sí rellena con setBackground y sí pinta el borde propio.
        boton.setUI(new BasicButtonUI());
        boton.setFont(fuente(Font.BOLD, 13));
        boton.setBackground(COLOR_ACENTO);
        boton.setForeground(COLOR_TEXTO);
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setBorder(new BordeRedondeado(COLOR_ACENTO, COLOR_PANEL, RADIO_BOTON, 1, new Insets(8, 16, 8, 16)));
        boton.setPreferredSize(new Dimension(ANCHO_CAMPO, 44));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(COLOR_ACENTO_HOVER);
                boton.setBorder(new BordeRedondeado(
                        COLOR_ACENTO_HOVER, COLOR_PANEL, RADIO_BOTON, 1, new Insets(8, 16, 8, 16)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(COLOR_ACENTO);
                boton.setBorder(new BordeRedondeado(
                        COLOR_ACENTO, COLOR_PANEL, RADIO_BOTON, 1, new Insets(8, 16, 8, 16)));
            }
        });
        instalarEfectoZoomHover(boton);
        return boton;
    }

    @Override
    public void instalarEfectoZoomHover(JButton boton) {
        Timer[] temporizadorEnCurso = new Timer[1];
        // El tamaño "normal" se captura recién en el primer mouseEntered, no
        // aquí: quien llama a este método (por ejemplo, un dashboard que
        // ajusta setPreferredSize(...) después de pedirle el botón a la
        // fábrica) puede seguir personalizando el tamaño del botón después
        // de instalar el efecto. Si se capturara de una vez, el hover
        // animaría hacia el tamaño de fábrica original en vez del tamaño
        // final realmente usado en pantalla.
        Dimension[] tamanoNormal = new Dimension[1];
        Dimension[] maximoNormal = new Dimension[1];

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (tamanoNormal[0] == null) {
                    tamanoNormal[0] = boton.getPreferredSize();
                    maximoNormal[0] = boton.getMaximumSize();
                }
                Dimension tamanoZoom = new Dimension(
                        tamanoNormal[0].width + CRECIMIENTO_HOVER_PX, tamanoNormal[0].height + CRECIMIENTO_HOVER_PX / 2);
                Dimension maximoZoom = new Dimension(
                        maximoNormal[0].width == Integer.MAX_VALUE
                                ? Integer.MAX_VALUE : maximoNormal[0].width + CRECIMIENTO_HOVER_PX,
                        maximoNormal[0].height + CRECIMIENTO_HOVER_PX / 2);
                animarTamano(boton, tamanoZoom, maximoZoom, temporizadorEnCurso);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (tamanoNormal[0] != null) {
                    animarTamano(boton, tamanoNormal[0], maximoNormal[0], temporizadorEnCurso);
                }
            }
        });
    }

    /**
     * Interpola el tamaño de un botón hacia {@code destinoPreferido} en unos
     * pocos pasos, para que el efecto de zoom del hover se vea como un
     * crecimiento fluido en vez de un salto brusco. Solo usa
     * {@code setPreferredSize}/{@code setMaximumSize} + {@code revalidate()}
     * (componentes Swing estándar), nunca pinta nada a mano.
     */
    private void animarTamano(JButton boton, Dimension destinoPreferido, Dimension destinoMaximo, Timer[] temporizadorEnCurso) {
        if (temporizadorEnCurso[0] != null && temporizadorEnCurso[0].isRunning()) {
            temporizadorEnCurso[0].stop();
        }
        Dimension inicio = boton.getPreferredSize();
        int[] paso = {0};
        Timer temporizador = new Timer(10, null);
        temporizador.addActionListener(e -> {
            paso[0]++;
            float progreso = Math.min(1f, paso[0] / (float) PASOS_ANIMACION_HOVER);
            int ancho = Math.round(inicio.width + (destinoPreferido.width - inicio.width) * progreso);
            int alto = Math.round(inicio.height + (destinoPreferido.height - inicio.height) * progreso);
            boton.setPreferredSize(new Dimension(ancho, alto));
            boton.setMaximumSize(destinoMaximo);
            boton.revalidate();
            if (paso[0] >= PASOS_ANIMACION_HOVER) {
                ((Timer) e.getSource()).stop();
            }
        });
        temporizadorEnCurso[0] = temporizador;
        temporizador.start();
    }

    @Override
    public JLabel crearEnlaceSecundario(String texto) {
        JLabel enlace = new JLabel(texto, SwingConstants.CENTER);
        enlace.setForeground(COLOR_ACENTO);
        enlace.setFont(fuente(Font.PLAIN, 12));
        enlace.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return enlace;
    }

    @Override
    public JPanel crearTarjeta(LayoutManager layout) {
        JPanel tarjeta = new JPanel(layout);
        tarjeta.setBackground(COLOR_PANEL);
        tarjeta.setBorder(new BordeRedondeado(
                COLOR_BORDE, COLOR_FONDO, RADIO_TARJETA, 1, new Insets(1, 1, 1, 1)));
        return tarjeta;
    }

    @Override
    public JButton crearBotonSidebar(String texto) {
        JButton boton = new JButton(texto);
        boton.setUI(new BasicButtonUI());
        boton.setFont(fuente(Font.BOLD, 14));
        boton.setForeground(COLOR_TEXTO_SUAVE);
        boton.setBackground(COLOR_SIDEBAR);
        boton.setOpaque(true);
        boton.setFocusPainted(false);
        boton.setBorder(new BordeRedondeado(
                COLOR_SIDEBAR, COLOR_SIDEBAR, RADIO_BOTON, 1, new Insets(12, 14, 12, 14)));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        instalarEfectoZoomHover(boton);
        return boton;
    }

    @Override
    public void resaltarBotonSidebar(JButton boton, boolean activo) {
        boton.setBackground(activo ? COLOR_ACENTO : COLOR_SIDEBAR);
        boton.setForeground(activo ? COLOR_TEXTO : COLOR_TEXTO_SUAVE);
        // El inactivo conserva un contorno tenue: sin él se leía como texto
        // suelto y no como un botón en el que se puede hacer clic.
        boton.setBorder(new BordeRedondeado(
                activo ? COLOR_ACENTO : COLOR_BORDE, COLOR_SIDEBAR, RADIO_BOTON, 1,
                new Insets(12, 14, 12, 14)));
    }

    @Override
    public JLabel crearLogo(int tamanoPx) {
        JLabel etiqueta = new JLabel();
        etiqueta.setHorizontalAlignment(SwingConstants.CENTER);
        etiqueta.setPreferredSize(new Dimension(tamanoPx, tamanoPx));
        etiqueta.setMaximumSize(new Dimension(tamanoPx, tamanoPx));

        ImageIcon iconoOriginal = cargarImagenLogo();
        if (iconoOriginal != null) {
            // Se escala en tiempo de pintado (IconoImagenEscalada) y no con
            // getScaledInstance: este último "hornea" un mapa de bits del
            // tamaño lógico, y en una pantalla con escalado de Windows
            // (125% en el equipo de referencia) Swing luego lo estira a los
            // píxeles físicos, así que el logo se veía suave. Dibujando la
            // imagen original en cada pintado, el escalado lo hace el
            // Graphics2D ya con la resolución real del monitor.
            etiqueta.setIcon(new IconoImagenEscalada(iconoOriginal.getImage(), tamanoPx, tamanoPx));
        } else {
            // Marcador de posición: evita que la ventana se vea rota si el
            // archivo del logo todavía no existe.
            etiqueta.setOpaque(true);
            etiqueta.setBackground(COLOR_FONDO);
            etiqueta.setForeground(COLOR_ACENTO);
            etiqueta.setFont(fuente(Font.BOLD, tamanoPx / 4));
            etiqueta.setText("EC");
            etiqueta.setBorder(new BordeRedondeado(
                    COLOR_ACENTO, COLOR_SIDEBAR, RADIO_TARJETA, 2, new Insets(2, 2, 2, 2)));
        }
        return etiqueta;
    }

    /**
     * Busca el logo primero en el classpath y luego en la ruta relativa del
     * proyecto.
     *
     * @return icono cargado o {@code null} si no se encontró el archivo
     */
    private ImageIcon cargarImagenLogo() {
        // El nombre debe coincidir con el archivo real, o la búsqueda por
        // classpath nunca acierta y todo queda dependiendo del directorio de
        // trabajo (ya pasó cuando aquí decía ".png" y el asset era ".jpg").
        URL recurso = getClass().getResource("/resources/images/logo.png");
        if (recurso != null) {
            return new ImageIcon(recurso);
        }
        File archivo = new File(RUTA_LOGO);
        if (archivo.exists()) {
            return new ImageIcon(archivo.getAbsolutePath());
        }
        return null;
    }

    @Override
    public JLabel crearAsistenteAnimado(int tamanoPx) {
        JLabel etiqueta = new JLabel();
        URL recurso = buscarRecursoIcono(ARCHIVO_ASISTENTE);
        // GIF animado: se decodifica con ImageIO (ver IconoGifAnimado), no
        // con Toolkit/ImageIcon como el resto de los íconos, porque este
        // archivo hace abortar al decodificador GIF heredado de AWT.
        if (recurso != null) {
            etiqueta.setIcon(new IconoGifAnimado(recurso, tamanoPx, tamanoPx));
        }
        // Sin marcador de posición: es puramente decorativo, no una marca
        // que necesite identidad visual como el logo.
        return etiqueta;
    }

    @Override
    public void mostrarDialogoExito(Component padre, String mensaje) {
        Image imagenCheck = cargarImagenIcono(ARCHIVO_CHECK);
        Icon icono = imagenCheck != null
                ? new IconoImagenEscalada(imagenCheck, TAM_ICONO_DIALOGO, TAM_ICONO_DIALOGO)
                : null;

        JOptionPane optionPane = new JOptionPane(mensaje, JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION, icono);
        // El delegado de Nimbus pinta el panel del diálogo con su fondo claro
        // e ignora la paleta. BasicOptionPaneUI (estándar del JDK) sí la
        // respeta. Se cambia ANTES de estilizar porque al instalar el
        // delegado se reconstruyen los componentes internos del diálogo.
        optionPane.setUI(new BasicOptionPaneUI());
        optionPane.setBackground(COLOR_PANEL);
        estilizarOptionPane(optionPane);

        JDialog dialogo = optionPane.createDialog(padre, "Éxito");
        // El contentPane del diálogo también lo pinta Nimbus; se le aplica el
        // mismo tratamiento que al resto para que no quede un marco claro
        // alrededor del contenido ya estilizado.
        if (dialogo.getContentPane() instanceof JPanel contenido) {
            contenido.setUI(new BasicPanelUI());
            contenido.setOpaque(true);
        }
        dialogo.getContentPane().setBackground(COLOR_PANEL);
        dialogo.getRootPane().setBackground(COLOR_PANEL);
        dialogo.getRootPane().setOpaque(true);
        dialogo.setBackground(COLOR_PANEL);
        instalarEntradaSuave(dialogo);
        dialogo.setVisible(true);
    }

    /**
     * Anima la aparición del diálogo deslizándolo unos píxeles hacia su
     * posición final en vez de aparecer de golpe.
     *
     * <p><b>Por qué no usa {@link Window#setOpacity(float)}:</b> se probó
     * primero un fundido de opacidad, pero {@code Dialog.setOpacity(x)} con
     * {@code x < 1} exige que la ventana sea {@code undecorated} — en una
     * decorada (la que entrega {@code JOptionPane.createDialog(...)}, con su
     * barra de título nativa) lanza {@code IllegalComponentStateException}
     * (comprobado). Quitarle la decoración para poder usar opacidad
     * significaría volver a dibujar el marco/título a mano, justo el tipo de
     * riesgo que "Estabilidad de la interfaz" pide evitar. Animar la
     * posición con {@code setBounds(...)} no tiene esa restricción, no
     * reordena el contenido interno del diálogo (el tamaño no cambia, solo
     * la posición) y sigue siendo el mismo {@code JDialog} decorado de
     * siempre.</p>
     *
     * @param dialogo diálogo ya creado (tamaño y posición final ya
     *                calculados por {@code createDialog}), todavía no visible
     */
    private void instalarEntradaSuave(JDialog dialogo) {
        Point destino = dialogo.getLocation();
        Point inicio = new Point(destino.x, destino.y - DESPLAZAMIENTO_ENTRADA_DIALOGO_PX);
        dialogo.setLocation(inicio);

        dialogo.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                int[] paso = {0};
                Timer temporizador = new Timer(12, null);
                temporizador.addActionListener(ev -> {
                    paso[0]++;
                    float progreso = Math.min(1f, paso[0] / (float) PASOS_ENTRADA_DIALOGO);
                    int y = Math.round(inicio.y + (destino.y - inicio.y) * progreso);
                    dialogo.setLocation(destino.x, y);
                    if (paso[0] >= PASOS_ENTRADA_DIALOGO) {
                        ((Timer) ev.getSource()).stop();
                    }
                });
                temporizador.start();
            }
        });
    }

    /**
     * Carga un ícono estático (no animado) vía {@link ImageIcon}, buscándolo
     * con {@link #buscarRecursoIcono(String)}.
     *
     * @param nombreArchivo nombre del archivo dentro de {@code resources/images/icons/}
     * @return imagen cargada, o {@code null} si el archivo no existe
     */
    private Image cargarImagenIcono(String nombreArchivo) {
        URL recurso = buscarRecursoIcono(nombreArchivo);
        return recurso != null ? new ImageIcon(recurso).getImage() : null;
    }

    /**
     * Busca un ícono primero en el classpath y luego en la ruta relativa del
     * proyecto, devolviendo su ubicación sin decodificarlo (para que cada
     * quien lo cargue con el decodificador que necesite: {@link ImageIcon}
     * para íconos estáticos, {@link ImageIO} para el GIF animado).
     *
     * @param nombreArchivo nombre del archivo dentro de {@code resources/images/icons/}
     * @return ubicación del recurso, o {@code null} si no se encontró
     */
    private URL buscarRecursoIcono(String nombreArchivo) {
        URL recurso = getClass().getResource("/resources/images/icons/" + nombreArchivo);
        if (recurso != null) {
            return recurso;
        }
        File archivo = new File(RUTA_ICONOS + nombreArchivo);
        if (archivo.exists()) {
            try {
                return archivo.toURI().toURL();
            } catch (MalformedURLException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Recorre el árbol de componentes de un {@link JOptionPane} y aplica la
     * paleta del tema (fondo de tarjeta, texto claro, botón de acento) en
     * vez de los colores por defecto del Look and Feel. Solo usa
     * {@code setBackground}/{@code setForeground}, igual que el resto de la
     * fábrica — no pinta nada a mano.
     *
     * @param componente raíz del árbol a estilizar (el propio {@code JOptionPane})
     */
    private void estilizarOptionPane(Component componente) {
        if (componente instanceof JButton boton) {
            // Igual que el resto de los controles: el delegado Basic del JDK
            // respeta los colores del tema, el de Nimbus los tapa.
            boton.setUI(new BasicButtonUI());
            boton.setBackground(COLOR_ACENTO);
            boton.setForeground(COLOR_TEXTO);
            boton.setFont(fuente(Font.BOLD, 13));
            boton.setFocusPainted(false);
            boton.setOpaque(true);
            boton.setBorder(new BordeRedondeado(
                    COLOR_ACENTO, COLOR_PANEL, RADIO_BOTON, 1, new Insets(6, 22, 6, 22)));
            boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else if (componente instanceof JLabel) {
            componente.setForeground(COLOR_TEXTO);
            componente.setFont(fuente(Font.PLAIN, 14));
        } else if (componente instanceof JPanel panel) {
            panel.setUI(new BasicPanelUI());
            panel.setBackground(COLOR_PANEL);
            panel.setOpaque(true);
        } else if (componente instanceof JComponent contenedor) {
            contenedor.setBackground(COLOR_PANEL);
            contenedor.setOpaque(true);
        }
        if (componente instanceof Container) {
            for (Component hijo : ((Container) componente).getComponents()) {
                estilizarOptionPane(hijo);
            }
        }
    }

    @Override
    public Color colorFondo() {
        return COLOR_FONDO;
    }

    @Override
    public Color colorPanel() {
        return COLOR_PANEL;
    }

    @Override
    public Color colorSidebar() {
        return COLOR_SIDEBAR;
    }

    @Override
    public Color colorCampo() {
        return COLOR_CAMPO;
    }

    @Override
    public int tamanoIconoCampo() {
        return TAM_ICONO_CAMPO;
    }

    @Override
    public int anchoCampo() {
        return ANCHO_CAMPO;
    }

    @Override
    public int altoCampo() {
        return ALTO_CAMPO;
    }

    @Override
    public IndicadorCarga crearIndicadorGiratorio(int tamano) {
        return new IndicadorCarga(COLOR_ACENTO, COLOR_BORDE, COLOR_PANEL, tamano, false);
    }

    @Override
    public IndicadorCarga crearIndicadorProgreso(int tamano) {
        return new IndicadorCarga(COLOR_ACENTO, COLOR_BORDE, COLOR_FONDO, tamano, true);
    }

    @Override
    public void mostrarTransicion(Component padre, String mensaje, Runnable alTerminar) {
        JPanel contenido = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 20));
        contenido.setBackground(COLOR_PANEL);
        contenido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACENTO, 1), new EmptyBorder(4, 18, 4, 22)));

        IndicadorCarga indicador = crearIndicadorGiratorio(TAM_INDICADOR_TRANSICION);
        JLabel etiqueta = new JLabel(mensaje);
        etiqueta.setFont(fuente(Font.BOLD, 14));
        etiqueta.setForeground(COLOR_TEXTO);
        contenido.add(indicador);
        contenido.add(etiqueta);

        JDialog ventana = new JDialog(SwingUtilities.getWindowAncestor(padre));
        // Sin decoración: es un aviso efímero, no una ventana con la que se
        // interactúe, así que una barra de título con botones sobraría.
        ventana.setUndecorated(true);
        ventana.setContentPane(contenido);
        ventana.pack();
        ventana.setLocationRelativeTo(padre);

        Timer cierre = new Timer(MS_TRANSICION, null);
        cierre.setRepeats(false);
        cierre.addActionListener(e -> {
            indicador.detener();
            ventana.dispose();
            alTerminar.run();
        });

        indicador.iniciar();
        cierre.start();
        // No modal a propósito: el temporizador debe poder cerrarla, y una
        // ventana modal bloquearía aquí hasta que alguien la cierre a mano.
        ventana.setVisible(true);
    }

    @Override
    public void celebrar(Component origen) {
        SonidoExito.reproducir();
        AnimacionConfeti.lanzar(origen, COLOR_ACENTO, COLOR_ACENTO_HOVER, COLOR_EXITO, COLOR_TEXTO);
    }

    @Override
    public Color colorAcento() {
        return COLOR_ACENTO;
    }

    @Override
    public Color colorTexto() {
        return COLOR_TEXTO;
    }

    @Override
    public Color colorTextoSuave() {
        return COLOR_TEXTO_SUAVE;
    }

    @Override
    public Color colorBorde() {
        return COLOR_BORDE;
    }

    @Override
    public Color colorExito() {
        return COLOR_EXITO;
    }

    @Override
    public Color colorAdvertencia() {
        return COLOR_ADVERTENCIA;
    }

    @Override
    public Icon crearPunto(Color color, int diametro) {
        return new IconoPunto(color, diametro);
    }

    @Override
    public Color colorError() {
        return COLOR_ERROR;
    }

    @Override
    public Font fuente(int estilo, int tamano) {
        return new Font(FAMILIA_TIPOGRAFICA, estilo, tamano);
    }

}
