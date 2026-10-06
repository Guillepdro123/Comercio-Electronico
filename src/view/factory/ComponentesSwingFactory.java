package view.factory;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.text.JTextComponent;
import view.factory.charts.GraficoBarras;
import view.factory.components.BotonCarrito;
import view.factory.components.CampoPasswordConToggle;
import view.factory.components.CampoTextoConIcono;
import view.factory.components.SelectorEstrellas;
import view.factory.components.SelectorSegmentado;
import view.factory.effects.AnimacionConfeti;
import view.factory.effects.DialogoCarga;
import view.factory.effects.IndicadorCarga;
import view.factory.effects.SonidoExito;
import view.factory.effects.TransicionTema;
import view.factory.icons.IconoCampo;
import view.factory.icons.IconoCerrar;
import view.factory.icons.IconoEstrellas;
import view.factory.icons.IconoGifAnimado;
import view.factory.icons.IconoGoogle;
import view.factory.icons.IconoImagenEscalada;
import view.factory.icons.IconoPunto;
import view.factory.icons.IconoTema;
import view.factory.promo.BannerRotativo;
import view.factory.promo.CabeceraDegradada;
import view.factory.tema.Paleta;
import view.factory.tema.TemaDinamico;
import view.factory.utils.BordeRedondeado;

/**
 * Única implementación hoy de {@link IComponentesFactory}: construye cada
 * componente con los colores de la {@link Paleta} que recibe y una tipografía
 * unificada.
 *
 * <p><b>Responsabilidad única:</b> esta clase sabe <em>cómo</em> se arma y se
 * estiliza un componente; la paleta dice <em>de qué color</em>. Ninguna vista
 * vuelve a instanciar colores ni fuentes. Un tema nuevo (alto contraste, por
 * ejemplo) es otra {@link Paleta}, sin modificar esta clase ni las vistas que
 * la consumen (Abierto/Cerrado). Antes los colores eran constantes de esta
 * clase y un tema nuevo habría exigido duplicarla entera.</p>
 *
 * <p><b>Estabilidad ante todo:</b> todos los componentes siguen siendo Swing
 * estándar y <b>opacos</b>. El redondeo de esquinas se logra con
 * {@link BordeRedondeado}, un {@code Border} que tapa las esquinas con el
 * color del contenedor — nunca con {@code setOpaque(false)} en cascada ni con
 * un {@code paintComponent} propio, que es lo que en una iteración anterior
 * produjo conflictos de color y texto invisible (ver "Estabilidad de la
 * interfaz" en {@code CLAUDE.md}).</p>
 *
 * <p><b>Profundidad sin pintar fondos:</b> en el tema claro la jerarquía la dan
 * las capas (fondo gris muy suave → tarjetas blancas con borde fino) y no
 * degradados sobre contenedores. Los únicos degradados del proyecto están en
 * piezas que se dibujan enteras y no tienen hijos (el banner rotativo y la
 * cabecera del pop-up promocional, en {@code view.factory.promo}).</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 3.0
 */
public class ComponentesSwingFactory implements IComponentesFactory {

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
    private static final String RUTA_PRODUCTOS = "src/resources/images/productos/";
    /** Formatos que el proyecto sabe decodificar y mostrar. */
    private static final String[] EXTENSIONES_IMAGEN = {"png", "jpg", "jpeg"};

    private static final String ARCHIVO_ASISTENTE = "ecommerce_cart.gif";
    private static final String ARCHIVO_CHECK = "check.png";
    private static final int TAM_ICONO_DIALOGO = 40;

    private static final int ANCHO_CAMPO_BUSQUEDA = 420;
    private static final int PASOS_DESLIZAMIENTO = 12;

    /** Cuánto crece el botón al pasar el mouse, en porcentaje de su tamaño. */
    private static final int PORCENTAJE_EXPANSION = 3;
    private static final int PASOS_EXPANSION = 7;
    private static final int MS_PASO_EXPANSION = 13;
    private static final Insets RELLENO_BOTON = new Insets(8, 16, 8, 16);

    private static final int UNIDAD_DESPLAZAMIENTO = 16;
    private static final int BLOQUE_DESPLAZAMIENTO = 280;

    private static final int ALTO_FILA_TABLA = 34;
    private static final int ALTO_CABECERA_TABLA = 30;

    private static final int TAM_INDICADOR_TRANSICION = 26;
    private static final int MS_TRANSICION = 1400;

    private static final int PASOS_ENTRADA_DIALOGO = 10;
    private static final int PASOS_ENFOQUE = 6;
    private static final int MS_PASO_ENFOQUE = 14;
    /** Cuánto del color de la alerta se mezcla con el fondo del panel. */
    private static final double TINTE_ALERTA = 0.16;
    private static final int DESPLAZAMIENTO_ENTRADA_DIALOGO_PX = 24;

    private static final int ANCHO_PROMOCION = 440;
    private static final int ALTO_CABECERA_PROMOCION = 92;
    private static final int TAM_BOTON_CERRAR = 32;
    /** Prefijo de las imágenes del almacén (el mismo que define el repositorio de imágenes). */
    private static final String PREFIJO_ALMACEN = "img:";

    /**
     * Tema vigente. Todo color que sale de esta fábrica es un color vivo del
     * tema ({@code ColorDeTema}): no guarda un valor fijo sino un rol, así que
     * {@link #alternarTema()} recolorea los componentes ya creados sin
     * reconstruirlos.
     */
    private final TemaDinamico tema;

    /**
     * Propiedad de cliente con lo que hay que volver a aplicar a un componente
     * cuando cambia el tema (su hoja de estilo de FlatLaf, el texto de un
     * botón...). La recorre {@link #alternarTema()} tras actualizar los UI.
     */
    private static final String PROPIEDAD_AL_CAMBIAR_TEMA = "comercio.alCambiarTema";
    /** Cómo recalcular la hoja de estilo de FlatLaf de un componente. */
    private static final String PROPIEDAD_ESTILO = "comercio.estilo";

    /** De dónde leer las imágenes {@code img:<id>}; la pone {@code app.Main}. */
    private Function<String, byte[]> fuenteDeImagenes = referencia -> null;
    /**
     * Imágenes del almacén ya decodificadas. Con Atlas detrás, cada una es un
     * viaje por red: se pide una vez por ejecución y no en cada repintado del
     * catálogo.
     */
    private final Map<String, Image> imagenesLeidas = new ConcurrentHashMap<>();
    /**
     * Lecturas de imágenes remotas en curso o terminadas, una por referencia:
     * diez tarjetas con la misma imagen esperan la misma descarga.
     */
    private final Map<String, CompletableFuture<Image>> lecturasRemotas = new ConcurrentHashMap<>();
    /** Hilos de lectura: viajan por red y nunca deben ocupar el de la interfaz. */
    private final ExecutorService lectorImagenes = Executors.newFixedThreadPool(4, tarea -> {
        Thread hilo = new Thread(tarea, "lector-imagenes");
        hilo.setDaemon(true);
        return hilo;
    });

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
     *
     * @param paleta colores con los que arranca; la elige {@code app.Main}
     */
    public ComponentesSwingFactory(Paleta paleta) {
        this.tema = new TemaDinamico(paleta);
        aplicarClavesBase();
    }

    /**
     * PUENTE TEMPORAL (de la Parte 4 a la Parte 6 del paso al Incremento 4):
     * conserva el constructor sin argumentos que todavía usa el {@code app.Main}
     * del Incremento 3, con la paleta oscura que ese Main instala (FlatDarkLaf).
     * La Parte 6 trae la versión definitiva de esta clase, sin este constructor.
     */
    public ComponentesSwingFactory() {
        this(Paleta.oscura());
    }

    /**
     * Fija en el {@code UIManager} los colores base del tema. Son colores
     * vivos, así que basta con ponerlos una vez; se repite tras cambiar de
     * Look and Feel por si la instalación los hubiera reemplazado.
     */
    private void aplicarClavesBase() {
        UIManager.put("control", tema.panel());
        UIManager.put("Panel.background", tema.panel());
        UIManager.put("OptionPane.background", tema.panel());
        UIManager.put("OptionPane.messageForeground", tema.texto());
        UIManager.put("nimbusLightBackground", tema.campo());
        // FlatLaf ya lo trae activado, pero se declara por si se cae a
        // Nimbus: es lo que anima el desplazamiento en vez de saltarlo.
        UIManager.put("ScrollPane.smoothScrolling", Boolean.TRUE);
    }

    @Override
    public JLabel crearEtiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(tema.textoSuave());
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
    public SelectorSegmentado crearSelectorOpciones(String[] opciones, Runnable alCambiar) {
        return new SelectorSegmentado(this, opciones, alCambiar);
    }

    @Override
    public JButton crearBotonSegmento(String texto) {
        JButton segmento = new JButton(texto);
        segmento.setFont(fuente(Font.BOLD, 13));
        segmento.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resaltarSegmento(segmento, false);
        return segmento;
    }

    @Override
    public void resaltarSegmento(JButton segmento, boolean activo) {
        segmento.setBackground(activo ? tema.acento() : tema.campo());
        segmento.setForeground(activo ? tema.textoSobreAcento() : tema.texto());
        // El segmento activo se aclara al pasar el mouse; el inactivo sube un
        // escalón hacia el color de panel, que es un realce más discreto y no
        // lo confunde con el que está seleccionado.
        aplicarEstilo(segmento, () -> estiloBoton(
                activo ? tema.acentoHover() : tema.borde(),
                activo ? tema.acentoPresionado() : tema.panel(),
                RADIO_CAMPO, new Insets(6, 10, 6, 10)));
    }

    @Override
    public void instalarEfectoExpansionHover(JButton boton) {
        // 'normal' guarda el rectángulo que le asignó el gestor de
        // disposición; 'expandido' evita recapturarlo estando ya crecido.
        Rectangle[] normal = new Rectangle[1];
        boolean[] expandido = {false};
        Timer[] enCurso = new Timer[1];

        // Si el layout cambia el tamaño del botón mientras está en reposo (al
        // redimensionar la ventana, por ejemplo), esa pasa a ser su medida.
        boton.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!expandido[0]) {
                    normal[0] = boton.getBounds();
                }
            }
        });

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (expandido[0]) {
                    return;
                }
                normal[0] = boton.getBounds();
                expandido[0] = true;
                animarRectangulo(boton, normal[0], escalar(normal[0]), enCurso, null);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!expandido[0] || normal[0] == null) {
                    return;
                }
                Rectangle crecido = boton.getBounds();
                animarRectangulo(boton, crecido, normal[0], enCurso, crecido);
                expandido[0] = false;
            }
        });
    }

    /**
     * @param origen rectángulo actual del botón
     * @return el mismo rectángulo ampliado un {@value #PORCENTAJE_EXPANSION}%
     *         y recentrado, de modo que crece por igual hacia los cuatro lados
     */
    private Rectangle escalar(Rectangle origen) {
        int ancho = (int) Math.round(origen.width * (1 + PORCENTAJE_EXPANSION / 100.0));
        int alto = (int) Math.round(origen.height * (1 + PORCENTAJE_EXPANSION / 100.0));
        return new Rectangle(
                origen.x - (ancho - origen.width) / 2,
                origen.y - (alto - origen.height) / 2,
                ancho, alto);
    }

    /**
     * Interpola el rectángulo del botón entre dos medidas.
     *
     * <p><b>Por qué esto no mueve nada alrededor.</b> Se llama a
     * {@code setBounds(...)} y <em>nunca</em> a {@code revalidate()}, así que
     * el gestor de disposición no vuelve a ejecutarse: los vecinos conservan
     * exactamente el sitio que ya tenían y el botón simplemente se pinta más
     * grande sobre el margen de su propia celda. La versión antigua de este
     * efecto animaba {@code preferredSize}, que sí obliga a recolocar, y hacía
     * saltar el formulario entero bajo el cursor; también se probó reducir el
     * {@code focusWidth} de FlatLaf y resultó que también relayouta.</p>
     *
     * @param aRepintar zona que hay que refrescar en el padre al encoger, para
     *                  que no queden restos del tamaño anterior; {@code null}
     *                  al crecer, que ahí no se libera espacio
     */
    private void animarRectangulo(JButton boton, Rectangle desde, Rectangle hasta,
                                  Timer[] enCurso, Rectangle aRepintar) {
        if (enCurso[0] != null && enCurso[0].isRunning()) {
            enCurso[0].stop();
        }
        int[] paso = {0};
        Timer temporizador = new Timer(MS_PASO_EXPANSION, null);
        enCurso[0] = temporizador;
        temporizador.addActionListener(e -> {
            paso[0]++;
            double avance = Math.min(1.0, paso[0] / (double) PASOS_EXPANSION);
            boton.setBounds(
                    interpolar(desde.x, hasta.x, avance),
                    interpolar(desde.y, hasta.y, avance),
                    interpolar(desde.width, hasta.width, avance),
                    interpolar(desde.height, hasta.height, avance));
            if (aRepintar != null && boton.getParent() != null) {
                boton.getParent().repaint(aRepintar.x, aRepintar.y,
                        aRepintar.width, aRepintar.height);
            }
            if (paso[0] >= PASOS_EXPANSION) {
                boton.setBounds(hasta);
                temporizador.stop();
            }
        });
        temporizador.start();
    }

    private int interpolar(int desde, int hasta, double avance) {
        return (int) Math.round(desde + (hasta - desde) * avance);
    }

    @Override
    public void instalarLiberacionDeFoco(JComponent campo) {
        AWTEventListener vigia = evento -> {
            if (evento.getID() != MouseEvent.MOUSE_PRESSED || !campo.isShowing()) {
                return;
            }
            Component enfocado = KeyboardFocusManager
                    .getCurrentKeyboardFocusManager().getFocusOwner();
            if (enfocado == null || !SwingUtilities.isDescendingFrom(enfocado, campo)) {
                return;
            }
            // La pulsación cayó fuera del campo teniéndolo él enfocado.
            if (evento.getSource() instanceof Component origen
                    && !SwingUtilities.isDescendingFrom(origen, campo)) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager()
                        .clearGlobalFocusOwner();
            }
        };

        // Se engancha mientras el campo está en pantalla y se suelta cuando
        // deja de estarlo: así un cierre de sesión no deja vigías acumulados.
        campo.addHierarchyListener(evento -> {
            if ((evento.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == 0) {
                return;
            }
            Toolkit herramientas = Toolkit.getDefaultToolkit();
            if (campo.isShowing()) {
                herramientas.addAWTEventListener(vigia, AWTEvent.MOUSE_EVENT_MASK);
            } else {
                herramientas.removeAWTEventListener(vigia);
            }
        });
    }

    @Override
    public void instalarAnilloEnfoque(JComponent contenedor, JTextComponent campo) {
        contenedor.setBorder(bordeCampo(contenedor, tema.borde(), 1));

        // Al pasar el mouse el borde sube un tono: adelanta que el campo es
        // pulsable sin llegar a anunciar que ya está activo. Si el campo tiene
        // el foco no se toca, porque el anillo de acento manda sobre el hover.
        contenedor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!campo.hasFocus()) {
                    contenedor.setBorder(bordeCampo(contenedor, tema.bordeHover(), 1));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!campo.hasFocus()) {
                    contenedor.setBorder(bordeCampo(contenedor, tema.borde(), 1));
                }
            }
        });

        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                animarBordeCampo(contenedor, tema.borde(), tema.acento(), 1, 2);
            }

            @Override
            public void focusLost(FocusEvent e) {
                animarBordeCampo(contenedor, tema.acento(), tema.borde(), 2, 1);
            }
        });
    }

    /**
     * Lleva el borde de un campo de un color a otro en unos cuantos pasos.
     *
     * <p>El salto seco de gris a violeta se notaba brusco justo donde el ojo
     * está mirando. Interpolar el color con un {@code Timer} —la misma técnica
     * que el zoom de los botones y la entrada de los diálogos— lo vuelve un
     * gesto continuo. No pinta nada a mano: solo reemplaza el {@code Border},
     * que sigue siendo {@link BordeRedondeado}.</p>
     *
     * @param contenedor    panel del campo compuesto
     * @param desde         color de partida
     * @param hasta         color de llegada
     * @param grosorDesde   grosor inicial del trazo
     * @param grosorHasta   grosor final
     */
    private void animarBordeCampo(JComponent contenedor, Color desde, Color hasta,
                                  int grosorDesde, int grosorHasta) {
        int[] paso = {0};
        Timer temporizador = new Timer(MS_PASO_ENFOQUE, null);
        temporizador.addActionListener(e -> {
            paso[0]++;
            double avance = Math.min(1.0, paso[0] / (double) PASOS_ENFOQUE);
            if (paso[0] >= PASOS_ENFOQUE) {
                // El último paso deja el color del tema tal cual (vivo), no una
                // mezcla fija: si luego cambia el tema, el anillo lo sigue.
                contenedor.setBorder(bordeCampo(contenedor, hasta, grosorHasta));
                temporizador.stop();
                return;
            }
            contenedor.setBorder(bordeCampo(contenedor, mezclar(desde, hasta, avance),
                    avance < 0.5 ? grosorDesde : grosorHasta));
        });
        temporizador.start();
    }

    /** @return el color intermedio entre dos, con {@code avance} de 0 a 1 */
    private Color mezclar(Color desde, Color hasta, double avance) {
        return new Color(
                (int) Math.round(desde.getRed() + (hasta.getRed() - desde.getRed()) * avance),
                (int) Math.round(desde.getGreen() + (hasta.getGreen() - desde.getGreen()) * avance),
                (int) Math.round(desde.getBlue() + (hasta.getBlue() - desde.getBlue()) * avance));
    }

    /**
     * Borde redondeado de un campo de captura.
     *
     * @param contenedor campo compuesto; si declaró
     *                   {@link BordeRedondeado#PROPIEDAD_COLOR_EXTERIOR}, las
     *                   esquinas se tapan con ese color en vez del de un panel
     * @param color      color del trazo; el acento es el "anillo de foco" que
     *                   señala dónde está escribiendo el usuario
     * @param grosor     grueso del trazo en píxeles
     * @return el borde listo para aplicar
     */
    private BordeRedondeado bordeCampo(JComponent contenedor, Color color, int grosor) {
        Color exterior = contenedor.getClientProperty(BordeRedondeado.PROPIEDAD_COLOR_EXTERIOR)
                instanceof Color propio
                ? propio : tema.panel();
        return new BordeRedondeado(color, exterior, RADIO_CAMPO, grosor,
                new Insets(6, 12, 6, 12));
    }

    @Override
    public JButton crearBotonPrimario(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(fuente(Font.BOLD, 13));
        boton.setBackground(tema.acento());
        boton.setForeground(tema.textoSobreAcento());
        boton.setPreferredSize(new Dimension(ANCHO_CAMPO, 44));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Los tres estados los pinta FlatLaf, no un MouseListener nuestro:
        // así el paso de uno a otro lo interpola la propia librería y se
        // comporta igual en toda la aplicación. Ver estiloBoton(...).
        aplicarEstilo(boton, () -> estiloBoton(tema.acentoHover(), tema.acentoPresionado(),
                RADIO_BOTON, RELLENO_BOTON));
        instalarEfectoExpansionHover(boton);
        return boton;
    }

    @Override
    public JButton crearBotonDestacado(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(fuente(Font.BOLD, 13));
        boton.setBackground(tema.destacado());
        boton.setForeground(tema.textoSobreAcento());
        boton.setPreferredSize(new Dimension(ANCHO_CAMPO, 44));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        aplicarEstilo(boton, () -> estiloBoton(tema.destacadoHover(), tema.destacadoPresionado(),
                RADIO_BOTON, RELLENO_BOTON));
        instalarEfectoExpansionHover(boton);
        return boton;
    }

    @Override
    public JButton crearBotonGoogle(String texto) {
        JButton boton = new JButton(texto, new IconoGoogle(18));
        boton.setIconTextGap(10);
        boton.setFont(fuente(Font.BOLD, 13));
        boton.setBackground(tema.campo());
        boton.setForeground(tema.texto());
        boton.setPreferredSize(new Dimension(ANCHO_CAMPO, 44));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Mismo realce que un segmento inactivo (sube hacia el color de
        // borde), más un contorno fino: sobre la tarjeta, un botón oscuro sin
        // borde se confundiría con un campo de texto.
        aplicarEstilo(boton, () -> estiloBoton(tema.borde(), tema.panel(), RADIO_BOTON, RELLENO_BOTON)
                + ";borderWidth: 1;borderColor: " + hex(tema.borde()));
        instalarEfectoExpansionHover(boton);
        return boton;
    }

    /**
     * Arma la hoja de estilo que FlatLaf aplica a un botón.
     *
     * <p><b>Por qué en texto y no con {@code UIManager}.</b> Las claves de
     * {@code UIManager} son globales: valdrían el mismo hover para el botón de
     * acento y para un segmento oscuro. FlatLaf admite estilo por componente
     * con {@code FlatClientProperties.STYLE}, que es lo que permite que cada
     * tipo de botón tenga su propia reacción sin inventar un delegado.</p>
     *
     * <p><b>El botón no se marca opaco a propósito.</b> Un botón opaco obliga a
     * FlatLaf a rellenar el rectángulo completo y las esquinas redondeadas se
     * pierden; dejándolo no opaco, la librería pinta ella el fondo ya
     * redondeado. Esto no es el patrón prohibido del proyecto: no hay
     * {@code paintComponent} propio ni un contenedor con hijos transparente,
     * es el modo de funcionamiento normal del Look and Feel.</p>
     *
     * @param hover     fondo al pasar el mouse
     * @param presionado fondo mientras se mantiene pulsado
     * @param radio     radio de las esquinas
     * @param relleno   márgenes internos del texto
     * @return la cadena de estilo para {@code FlatClientProperties.STYLE}
     */
    private String estiloBoton(Color hover, Color presionado, int radio, Insets relleno) {
        return "arc: " + radio * 2 + ";"
                + "borderWidth: 0;"
                + "focusWidth: 3;"
                + "innerFocusWidth: 0;"
                + "focusColor: " + hex(tema.acentoHover()) + ";"
                + "hoverBackground: " + hex(hover) + ";"
                + "pressedBackground: " + hex(presionado) + ";"
                + "margin: " + relleno.top + "," + relleno.left + ","
                + relleno.bottom + "," + relleno.right;
    }

    /** Aplica una hoja de estilo de FlatLaf a un componente. */
    private void aplicarEstilo(JComponent componente, Supplier<String> estilo) {
        componente.putClientProperty(FlatClientProperties.STYLE, estilo.get());
        // La hoja de estilo es texto con los hex del momento: FlatLaf no sabe
        // que esos colores son del tema. Se guarda cómo calcularla para que
        // alternarTema() la vuelva a generar con la paleta nueva. Se guarda
        // la última (un segmento cambia de estilo al activarse).
        componente.putClientProperty(PROPIEDAD_ESTILO, estilo);
    }

    /**
     * Registra algo que hay que rehacer en un componente cuando cambia el
     * tema y que no se resuelve con colores vivos (un texto, un tooltip).
     */
    private void alCambiarTema(JComponent componente, Runnable accion) {
        componente.putClientProperty(PROPIEDAD_AL_CAMBIAR_TEMA, accion);
    }

    /** @return el color en la notación {@code #RRGGBB} que entiende FlatLaf */
    private String hex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }


    @Override
    public JLabel crearEnlaceSecundario(String texto) {
        JLabel enlace = new JLabel(texto, SwingConstants.CENTER);
        enlace.setForeground(tema.acento());
        enlace.setFont(fuente(Font.PLAIN, 12));
        enlace.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return enlace;
    }

    @Override
    public JPanel crearTarjeta(LayoutManager layout) {
        JPanel tarjeta = new JPanel(layout);
        tarjeta.setBackground(tema.panel());
        tarjeta.setBorder(new BordeRedondeado(
                tema.borde(), tema.fondo(), RADIO_TARJETA, 1, new Insets(1, 1, 1, 1)));
        return tarjeta;
    }

    @Override
    public JButton crearBotonSidebar(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(fuente(Font.BOLD, 14));
        boton.setForeground(tema.textoSidebar());
        boton.setBackground(tema.sidebar());
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Solo claves de fondo, ninguna de borde (arc, focusWidth...): este
        // botón lleva BordeRedondeado (ver resaltarBotonSidebar), y FlatLaf
        // rechaza con una excepción las claves de borde cuando el borde no es
        // el suyo. Al crearlo no se notaba; al reaplicar el estilo tras cambiar
        // de tema, sí (comprobado).
        aplicarEstilo(boton, () -> "hoverBackground: " + hex(tema.sidebarHover()) + ";"
                + "pressedBackground: " + hex(tema.sidebarPresionado()) + ";"
                + "margin: 12,14,12,14");
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        return boton;
    }

    @Override
    public void resaltarBotonSidebar(JButton boton, boolean activo) {
        boton.setBackground(activo ? tema.acento() : tema.sidebar());
        boton.setForeground(activo ? tema.textoSobreAcento() : tema.textoSidebar());
        // El inactivo conserva un contorno tenue: sin él se leía como texto
        // suelto y no como un botón en el que se puede hacer clic.
        boton.setBorder(new BordeRedondeado(
                activo ? tema.acento() : tema.bordeSidebar(), tema.sidebar(), RADIO_BOTON, 1,
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
            etiqueta.setBackground(tema.sidebar());
            etiqueta.setForeground(tema.acento());
            etiqueta.setFont(fuente(Font.BOLD, tamanoPx / 4));
            etiqueta.setText("EC");
            etiqueta.setBorder(new BordeRedondeado(
                    tema.acento(), tema.sidebar(), RADIO_TARJETA, 2, new Insets(2, 2, 2, 2)));
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
        mostrarDialogoMensaje(padre, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE, icono);
    }

    @Override
    public void mostrarDialogoAviso(Component padre, String mensaje) {
        // El ícono de advertencia lo pone el Look and Feel: con el check verde
        // del éxito, un "el precio debe ser un número" parecía un acierto.
        mostrarDialogoMensaje(padre, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE,
                UIManager.getIcon("OptionPane.warningIcon"));
    }

    private void mostrarDialogoMensaje(Component padre, String mensaje, String titulo,
                                       int tipo, Icon icono) {
        JOptionPane optionPane = new JOptionPane(mensaje, tipo, JOptionPane.DEFAULT_OPTION, icono);
        // El delegado de Nimbus pinta el panel del diálogo con su fondo claro
        // e ignora la tema. BasicOptionPaneUI (estándar del JDK) sí la
        // respeta. Se cambia ANTES de estilizar porque al instalar el
        // delegado se reconstruyen los componentes internos del diálogo.
        optionPane.setUI(new BasicOptionPaneUI());
        optionPane.setBackground(tema.panel());
        estilizarOptionPane(optionPane, null);

        JDialog dialogo = optionPane.createDialog(padre, titulo);
        // El contentPane del diálogo también lo pinta Nimbus; se le aplica el
        // mismo tratamiento que al resto para que no quede un marco claro
        // alrededor del contenido ya estilizado.
        if (dialogo.getContentPane() instanceof JPanel contenido) {
            contenido.setUI(new BasicPanelUI());
            contenido.setOpaque(true);
        }
        dialogo.getContentPane().setBackground(tema.panel());
        dialogo.getRootPane().setBackground(tema.panel());
        dialogo.getRootPane().setOpaque(true);
        dialogo.setBackground(tema.panel());
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
    /**
     * Aplica el tema a las piezas que arma el propio {@link JOptionPane}.
     *
     * @param componente raíz desde la que se desciende
     * @param propio     contenido que entregó quien abre el diálogo, o
     *                   {@code null} si no hay. Ese subárbol se salta entero:
     *                   ya salió de esta misma fábrica y viene con su estilo
     *                   puesto. Sin esta exclusión, el recorrido repintaba
     *                   también sus botones con el color de acento y, por
     *                   ejemplo, las cinco categorías de un selector
     *                   segmentado aparecían las cinco como si estuvieran
     *                   seleccionadas.
     */
    private void estilizarOptionPane(Component componente, Component propio) {
        if (componente == propio) {
            return;
        }
        if (componente instanceof JButton boton) {
            // Igual que el resto de los controles: el delegado Basic del JDK
            // respeta los colores del tema, el de Nimbus los tapa.
            boton.setBackground(tema.acento());
            boton.setForeground(tema.textoSobreAcento());
            boton.setFont(fuente(Font.BOLD, 13));
            boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            aplicarEstilo(boton, () -> estiloBoton(tema.acentoHover(), tema.acentoPresionado(),
                    RADIO_BOTON, new Insets(6, 22, 6, 22)));
        } else if (componente instanceof JLabel) {
            componente.setForeground(tema.texto());
            componente.setFont(fuente(Font.PLAIN, 14));
        } else if (componente instanceof JPanel panel) {
            panel.setUI(new BasicPanelUI());
            panel.setBackground(tema.panel());
            panel.setOpaque(true);
        } else if (componente instanceof JComponent contenedor) {
            contenedor.setBackground(tema.panel());
            contenedor.setOpaque(true);
        }
        if (componente instanceof Container) {
            for (Component hijo : ((Container) componente).getComponents()) {
                estilizarOptionPane(hijo, propio);
            }
        }
    }

    @Override
    public Color colorFondo() {
        return tema.fondo();
    }

    @Override
    public Color colorPanel() {
        return tema.panel();
    }

    @Override
    public Color colorSidebar() {
        return tema.sidebar();
    }

    @Override
    public Color colorCampo() {
        return tema.campo();
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
        return new IndicadorCarga(tema.acento(), tema.borde(), tema.panel(), tamano, false);
    }

    @Override
    public IndicadorCarga crearIndicadorProgreso(int tamano) {
        return new IndicadorCarga(tema.acento(), tema.borde(), tema.fondo(), tamano, true);
    }

    @Override
    public <T> void ejecutarConCarga(Component padre, String mensaje,
                                     java.util.function.Supplier<T> tarea,
                                     java.util.function.Consumer<T> alTerminar,
                                     java.util.function.Consumer<Exception> alFallar) {
        new DialogoCarga(padre, mensaje, crearIndicadorGiratorio(TAM_INDICADOR_TRANSICION),
                tema.panel(), tema.acento(), tema.texto(), fuente(Font.BOLD, 14))
                .ejecutar(tarea, alTerminar, alFallar);
    }

    @Override
    public <T> void ejecutarCancelable(Component padre, String mensaje,
                                       java.util.function.Supplier<T> tarea,
                                       java.util.function.Consumer<T> alTerminar,
                                       java.util.function.Consumer<Exception> alFallar,
                                       Runnable alCancelar) {
        DialogoCarga dialogo = new DialogoCarga(padre, mensaje,
                crearIndicadorGiratorio(TAM_INDICADOR_TRANSICION),
                tema.panel(), tema.acento(), tema.texto(), fuente(Font.BOLD, 14));
        dialogo.permitirCancelar(crearBotonSegmento("Cancelar"), alCancelar);
        dialogo.ejecutar(tarea, alTerminar, alFallar);
    }

    @Override
    public void mostrarTransicion(Component padre, String mensaje, Runnable alTerminar) {
        JPanel contenido = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 20));
        contenido.setBackground(tema.panel());
        contenido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tema.acento(), 1), new EmptyBorder(4, 18, 4, 22)));

        IndicadorCarga indicador = crearIndicadorGiratorio(TAM_INDICADOR_TRANSICION);
        JLabel etiqueta = new JLabel(mensaje);
        etiqueta.setFont(fuente(Font.BOLD, 14));
        etiqueta.setForeground(tema.texto());
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
        AnimacionConfeti.lanzar(origen, tema.acento(), tema.acentoHover(), tema.exito(), tema.texto());
    }

    @Override
    public Color colorAcento() {
        return tema.acento();
    }

    @Override
    public Color colorTexto() {
        return tema.texto();
    }

    @Override
    public Color colorTextoSuave() {
        return tema.textoSuave();
    }

    @Override
    public Color colorBorde() {
        return tema.borde();
    }

    @Override
    public Color colorExito() {
        return tema.exito();
    }

    @Override
    public JButton crearAvatar(String iniciales, int tamano) {
        JButton avatar = new JButton(iniciales);
        avatar.setBackground(tema.acento());
        avatar.setForeground(tema.textoSobreAcento());
        avatar.setFont(fuente(Font.BOLD, tamano / 3));
        avatar.setPreferredSize(new Dimension(tamano, tamano));
        avatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Un arco mayor que el lado lo deja circular, y lo pinta FlatLaf.
        // Al ser botón reacciona al hover y al pulsado como el resto.
        aplicarEstilo(avatar, () -> estiloBoton(tema.acentoHover(), tema.acentoPresionado(),
                tamano, new Insets(0, 0, 0, 0)));
        return avatar;
    }

    @Override
    public JScrollPane crearScroll(JComponent contenido) {
        JScrollPane scroll = new JScrollPane(contenido);
        // Borde vacío y no null: con null, al cambiar de tema el Look and Feel
        // instalaría el suyo (solo respeta los bordes que no son suyos).
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(tema.fondo());
        // Un "diente" de rueda mueve 3 unidades; con 16px por unidad son 48px
        // por muesca, que es un paso corto y parejo. El salto de bloque
        // (rueda con Mayús, tecla AvPág o clic en el canal) se pone a una
        // pantalla menos un solapamiento, para no perder de vista dónde se
        // estaba.
        scroll.getVerticalScrollBar().setUnitIncrement(UNIDAD_DESPLAZAMIENTO);
        scroll.getHorizontalScrollBar().setUnitIncrement(UNIDAD_DESPLAZAMIENTO);
        scroll.getVerticalScrollBar().setBlockIncrement(BLOQUE_DESPLAZAMIENTO);
        return scroll;
    }

    @Override
    public CampoTextoConIcono crearCampoBusqueda(String textoFantasma) {
        // Vive sobre la barra superior, no sobre un panel: las esquinas del
        // borde redondeado se tapan con el color de la barra.
        CampoTextoConIcono campo = new CampoTextoConIcono(this, textoFantasma,
                IconoCampo.Tipo.BUSCAR, tema.sidebar());
        campo.setPreferredSize(new Dimension(ANCHO_CAMPO_BUSQUEDA, ALTO_CAMPO));
        campo.setMaximumSize(campo.getPreferredSize());
        return campo;
    }

    @Override
    public JLabel crearAlerta() {
        JLabel alerta = new JLabel(" ");
        alerta.setFont(fuente(Font.BOLD, 12));
        alerta.setHorizontalAlignment(SwingConstants.CENTER);
        alerta.setOpaque(true);
        alerta.setVisible(false);
        return alerta;
    }

    @Override
    public void pintarAlerta(JLabel alerta, String mensaje, boolean esError) {
        if (mensaje == null || mensaje.isBlank()) {
            alerta.setText(" ");
            alerta.setVisible(false);
            return;
        }
        Color color = esError ? tema.error() : tema.advertencia();
        alerta.setText(mensaje);
        alerta.setForeground(color);
        // Fondo teñido del mismo color pero muy diluido: la franja se lee como
        // un bloque de aviso y no como una línea de texto suelta, sin gritar.
        // Mezcla viva: se recalcula con la paleta vigente en cada pintado.
        alerta.setBackground(tema.derivado(p -> mezclar(p.panel(),
                esError ? p.error() : p.advertencia(), TINTE_ALERTA)));
        alerta.setBorder(new BordeRedondeado(color, tema.panel(), RADIO_CAMPO, 1,
                new Insets(5, 10, 5, 10)));
        alerta.setVisible(true);
    }

    @Override
    public JButton crearBotonFlotante(String glifo, String descripcion, int tamano) {
        JButton boton = new JButton(glifo);
        boton.setFont(fuente(Font.BOLD, tamano / 2));
        boton.setBackground(tema.acento());
        boton.setForeground(tema.textoSobreAcento());
        boton.setToolTipText(descripcion);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setSize(tamano, tamano);
        boton.setPreferredSize(new Dimension(tamano, tamano));
        // Un arco mayor que el lado redondea el botón hasta dejarlo circular,
        // y lo hace FlatLaf al pintar: no hay que dibujar ningún óvalo.
        aplicarEstilo(boton, () -> estiloBoton(tema.acentoHover(), tema.acentoPresionado(),
                tamano, new Insets(0, 0, 0, 0)));
        return boton;
    }

    @Override
    public BotonCarrito crearBotonCarrito() {
        return new BotonCarrito(this);
    }

    @Override
    public JDialog crearDialogoModal(Component padre, String titulo, JComponent contenido) {
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(padre), titulo,
                Dialog.ModalityType.APPLICATION_MODAL);
        contenido.setOpaque(true);
        contenido.setBackground(tema.panel());
        dialogo.setContentPane(contenido);
        // Mismo tratamiento que el resto de diálogos: hay que teñir el
        // contentPane y el rootPane, o queda un marco claro alrededor.
        dialogo.getContentPane().setBackground(tema.panel());
        dialogo.getRootPane().setBackground(tema.panel());
        dialogo.getRootPane().setOpaque(true);
        dialogo.pack();
        dialogo.setResizable(false);
        dialogo.setLocationRelativeTo(padre);
        instalarEntradaSuave(dialogo);
        return dialogo;
    }

    @Override
    public String elegirImagen(Component padre, String rutaActual) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Selecciona la imagen del producto");
        selector.setAcceptAllFileFilterUsed(false);
        selector.setFileFilter(new FileNameExtensionFilter(
                "Imágenes (" + String.join(", ", EXTENSIONES_IMAGEN) + ")", EXTENSIONES_IMAGEN));
        // Si ya había una imagen elegida, se abre donde está: casi siempre la
        // siguiente que busque el proveedor vive en la misma carpeta.
        if (rutaActual != null && !rutaActual.isBlank()) {
            File anterior = new File(rutaActual);
            if (anterior.getParentFile() != null && anterior.getParentFile().isDirectory()) {
                selector.setCurrentDirectory(anterior.getParentFile());
            }
        }
        if (selector.showOpenDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return selector.getSelectedFile().getAbsolutePath();
    }

    @Override
    public JLabel crearImagenProducto(String referencia, String nombre, int ancho, int alto) {
        JLabel recuadro = new JLabel();
        recuadro.setHorizontalAlignment(SwingConstants.CENTER);
        recuadro.setPreferredSize(new Dimension(ancho, alto));
        recuadro.setMaximumSize(new Dimension(ancho, alto));
        recuadro.setOpaque(true);
        recuadro.setBackground(tema.vitrina());

        Image imagen = cargarImagenProducto(referencia);
        if (imagen != null) {
            encajarImagen(recuadro, imagen, ancho, alto);
        } else {
            recuadro.setFont(fuente(Font.BOLD, Math.max(16, alto / 3)));
            // La vitrina es oscura en los dos temas: el acento claro se lee
            // mejor sobre ella que el de marca.
            recuadro.setForeground(tema.acentoHover());
            recuadro.setText(inicial(nombre));
            recuadro.setBorder(new BordeRedondeado(
                    tema.borde(), tema.panel(), RADIO_CAMPO, 1, new Insets(0, 0, 0, 0)));
            if (esRemota(referencia)) {
                // La inicial se ve mientras tanto; al llegar la imagen la sustituye.
                leerEnSegundoPlano(referencia).thenAccept(llegada -> {
                    if (llegada != null) {
                        SwingUtilities.invokeLater(() -> {
                            recuadro.setText(null);
                            recuadro.setBorder(BorderFactory.createEmptyBorder());
                            encajarImagen(recuadro, llegada, ancho, alto);
                        });
                    }
                });
            }
        }
        return recuadro;
    }

    /**
     * Se encaja dentro del recuadro conservando la proporción, no estirando:
     * la misma imagen se muestra en la tarjeta del catálogo (apaisada) y en el
     * detalle (casi cuadrada), y deformarla en una de las dos se nota de
     * inmediato. Lo que sobra queda del color de fondo del recuadro.
     */
    private void encajarImagen(JLabel recuadro, Image imagen, int ancho, int alto) {
        double escala = Math.min(ancho / (double) imagen.getWidth(null),
                alto / (double) imagen.getHeight(null));
        // Mismo escalado que el logo: se dibuja en tiempo de pintado para
        // no perder nitidez en pantallas con escalado de Windows.
        recuadro.setIcon(new IconoImagenEscalada(imagen,
                (int) Math.round(imagen.getWidth(null) * escala),
                (int) Math.round(imagen.getHeight(null) * escala)));
        recuadro.repaint();
    }

    /**
     * Carga la imagen de un producto.
     *
     * <p>Una referencia remota ({@code img:<id>} de Atlas o una URL web) se
     * lee en segundo plano y se devuelve solo si ya llegó. Para las demás, tres
     * intentos, en este orden: recurso del programa, archivo dentro de
     * la carpeta de productos del proyecto y, por último, la referencia tal
     * cual como ruta del sistema (para que un proveedor pueda registrar una
     * imagen suya que no viaja con la entrega). Si ninguno funciona devuelve
     * {@code null} y quien llama pone su respaldo: una imagen ausente nunca
     * debe impedir que se vea el producto.</p>
     *
     * @param referencia nombre de archivo o ruta registrada en el producto
     * @return la imagen, o {@code null} si no hay ninguna utilizable
     */
    private Image cargarImagenProducto(String referencia) {
        if (referencia == null || referencia.isBlank()) {
            return null;
        }
        if (esRemota(referencia)) {
            // Nunca se espera aquí: puede llamarse desde el hilo de la
            // interfaz. Si aún no está, se pide y quien llama vuelve a
            // preguntar (el banner en su siguiente repintado; el recuadro de
            // producto se rellena solo al llegar).
            Image yaLeida = imagenesLeidas.get(referencia);
            if (yaLeida == null) {
                leerEnSegundoPlano(referencia);
            }
            return yaLeida;
        }
        URL recurso = getClass().getResource("/resources/images/productos/" + referencia);
        if (recurso != null) {
            return new ImageIcon(recurso).getImage();
        }
        File enProyecto = new File(RUTA_PRODUCTOS + referencia);
        if (enProyecto.exists()) {
            return new ImageIcon(enProyecto.getAbsolutePath()).getImage();
        }
        File sueltoEnDisco = new File(referencia);
        return sueltoEnDisco.exists() ? new ImageIcon(sueltoEnDisco.getAbsolutePath()).getImage() : null;
    }

    /**
     * Lee una imagen del almacén (Atlas o la carpeta {@code imagenes/}) a
     * través de la función que puso {@code app.Main}, y la recuerda.
     *
     * <p>Se decodifica con {@code ImageIO} y no con {@code ImageIcon}: este
     * último carga de forma asíncrona y la primera vez devolvería una imagen
     * de 0x0 que el cálculo de proporción no podría usar.</p>
     */
    /**
     * Imagen que no viaja con el programa: {@code img:<id>} de Atlas o una URL
     * web. Los bytes los da {@code fuenteDeImagenes}; la vista no sabe de dónde.
     */
    private static boolean esRemota(String referencia) {
        return referencia != null && (referencia.startsWith(PREFIJO_ALMACEN)
                || referencia.startsWith("https://") || referencia.startsWith("http://"));
    }

    private CompletableFuture<Image> leerEnSegundoPlano(String referencia) {
        return lecturasRemotas.computeIfAbsent(referencia,
                r -> CompletableFuture.supplyAsync(() -> leerRemota(r), lectorImagenes));
    }

    private Image leerRemota(String referencia) {
        try {
            byte[] datos = fuenteDeImagenes.apply(referencia);
            if (datos == null) {
                return null;
            }
            Image imagen = ImageIO.read(new ByteArrayInputStream(datos));
            if (imagen != null) {
                imagenesLeidas.put(referencia, imagen);
            }
            return imagen;
        } catch (IOException | RuntimeException ex) {
            // Una imagen que no se puede leer no impide ver el producto: el
            // recuadro cae en su inicial.
            return null;
        }
    }

    /** @return primera letra del nombre en mayúscula, para el recuadro sin imagen */
    private String inicial(String nombre) {
        return nombre == null || nombre.isBlank()
                ? "?" : nombre.trim().substring(0, 1).toUpperCase();
    }

    @Override
    public GraficoBarras crearGraficoBarras() {
        return new GraficoBarras(this);
    }

    @Override
    public void deslizarPanelLateral(JPanel panel, int anchoDestino) {
        int anchoInicial = panel.getPreferredSize().width;
        if (anchoInicial == anchoDestino) {
            return;
        }
        int[] paso = {0};
        Timer temporizador = new Timer(12, null);
        temporizador.addActionListener(e -> {
            paso[0]++;
            double avance = paso[0] / (double) PASOS_DESLIZAMIENTO;
            int ancho = (int) Math.round(anchoInicial + (anchoDestino - anchoInicial) * avance);
            if (paso[0] >= PASOS_DESLIZAMIENTO) {
                ancho = anchoDestino;
                temporizador.stop();
            }
            panel.setPreferredSize(new Dimension(ancho, 0));
            panel.revalidate();
            panel.getParent().repaint();
        });
        temporizador.start();
    }

    @Override
    public JTable crearTabla(TableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setFont(fuente(Font.PLAIN, 13));
        tabla.setRowHeight(ALTO_FILA_TABLA);
        tabla.setBackground(tema.panel());
        tabla.setForeground(tema.texto());
        tabla.setSelectionBackground(tema.acento());
        tabla.setSelectionForeground(tema.textoSobreAcento());
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setShowGrid(false);
        // Un píxel de separación vertical deja ver el fondo entre filas y hace
        // de línea divisoria sin tener que dibujar ninguna.
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setGridColor(tema.borde());
        tabla.setFillsViewportHeight(true);
        tabla.setRowSelectionAllowed(true);

        // Renderizador opaco para las celdas: el que instala Nimbus no lo es,
        // y sin él la tabla se pinta con su fondo claro por debajo. Es una
        // clase estándar del JDK, no un ComponentUI escrito a mano.
        DefaultTableCellRenderer celda = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean seleccionada, boolean enfocada, int fila, int columna) {
                // El renderizador base reimpone en cada pintado el borde que
                // define el Look and Feel, así que el relleno y la línea
                // divisoria se aplican después de llamarlo. Se le pasa
                // 'enfocada' como falso para que Nimbus no dibuje además su
                // recuadro de foco sobre la celda.
                super.getTableCellRendererComponent(t, valor, seleccionada, false, fila, columna);
                // Filas alternas: en el tema claro, blanco sobre blanco hace
                // que la vista se pierda al recorrer una fila larga.
                if (!seleccionada) {
                    setBackground(fila % 2 == 0 ? tema.panel() : tema.fondo());
                }
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, tema.borde()),
                        new EmptyBorder(0, 12, 0, 12)));
                return this;
            }
        };
        celda.setOpaque(true);
        tabla.setDefaultRenderer(Object.class, celda);

        JTableHeader cabecera = tabla.getTableHeader();
        cabecera.setReorderingAllowed(false);
        cabecera.setBackground(tema.fondo());
        cabecera.setForeground(tema.textoSuave());
        cabecera.setBorder(BorderFactory.createEmptyBorder());
        cabecera.setPreferredSize(new Dimension(0, ALTO_CABECERA_TABLA));
        // Colores, fuente y borde se ponen en cada pintado y no una sola vez:
        // al cambiar de tema, updateUI() de un DefaultTableCellRenderer los
        // deja en null y la cabecera perdía su franja (comprobado en captura).
        DefaultTableCellRenderer titulo = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean seleccionada, boolean enfocada, int fila, int columna) {
                super.getTableCellRendererComponent(t, valor, false, false, fila, columna);
                setOpaque(true);
                setBackground(tema.fondo());
                setForeground(tema.textoSuave());
                setFont(fuente(Font.BOLD, 12));
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return this;
            }
        };
        cabecera.setDefaultRenderer(titulo);

        return tabla;
    }

    @Override
    public boolean mostrarDialogoConfirmacion(Component padre, String titulo,
                                              JComponent contenido, String textoOk) {
        return mostrarDialogoConOpciones(padre, titulo, contenido, textoOk, "Cancelar");
    }

    @Override
    public void mostrarDialogoContenido(Component padre, String titulo,
                                        JComponent contenido, String textoCerrar) {
        // Un solo botón: con el de confirmación, "Mis compras" mostraba
        // "Cerrar" y "Cancelar", dos botones que hacían lo mismo.
        mostrarDialogoConOpciones(padre, titulo, contenido, textoCerrar);
    }

    private boolean mostrarDialogoConOpciones(Component padre, String titulo,
                                              JComponent contenido, String textoOk,
                                              String... otras) {
        Object[] opciones = new Object[otras.length + 1];
        opciones[0] = textoOk;
        System.arraycopy(otras, 0, opciones, 1, otras.length);
        JOptionPane optionPane = new JOptionPane(contenido, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, opciones, textoOk);
        // Mismo tratamiento que el diálogo de éxito: sin esto Nimbus lo pinta
        // con su fondo claro y rompe la tema.
        optionPane.setUI(new BasicOptionPaneUI());
        optionPane.setBackground(tema.panel());
        estilizarOptionPane(optionPane, contenido);

        JDialog dialogo = optionPane.createDialog(padre, titulo);
        if (dialogo.getContentPane() instanceof JPanel panel) {
            panel.setUI(new BasicPanelUI());
            panel.setOpaque(true);
        }
        dialogo.getContentPane().setBackground(tema.panel());
        dialogo.getRootPane().setBackground(tema.panel());
        dialogo.getRootPane().setOpaque(true);
        instalarEntradaSuave(dialogo);
        dialogo.setVisible(true);
        dialogo.dispose();

        return textoOk.equals(optionPane.getValue());
    }

    @Override
    public JPopupMenu crearMenuUsuario(String nombreUsuario) {
        JPopupMenu menu = new JPopupMenu();
        // Igual que el resto de superficies: el delegado Basic respeta los
        // colores del tema, el de Nimbus los tapa con su fondo claro.
        menu.setUI(new BasicPopupMenuUI());
        menu.setBackground(tema.panel());
        menu.setBorder(BorderFactory.createLineBorder(tema.borde(), 1));

        JLabel encabezado = new JLabel("  " + nombreUsuario);
        encabezado.setFont(fuente(Font.BOLD, 12));
        encabezado.setForeground(tema.textoSuave());
        encabezado.setBorder(new EmptyBorder(8, 8, 8, 16));
        menu.add(encabezado);
        menu.addSeparator();
        return menu;
    }

    @Override
    public void agregarOpcionMenu(JPopupMenu menu, String texto, Runnable accion) {
        JMenuItem opcion = new JMenuItem(texto);
        opcion.setUI(new BasicMenuItemUI());
        opcion.setBackground(tema.panel());
        opcion.setForeground(tema.texto());
        opcion.setFont(fuente(Font.PLAIN, 13));
        opcion.setBorder(new EmptyBorder(8, 14, 8, 24));
        opcion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        opcion.addActionListener(e -> accion.run());
        menu.add(opcion);
    }

    @Override
    public Color colorAdvertencia() {
        return tema.advertencia();
    }

    @Override
    public Icon crearPunto(Color color, int diametro) {
        return new IconoPunto(color, diametro);
    }

    @Override
    public Color colorError() {
        return tema.error();
    }

    @Override
    public Color colorTextoSidebar() {
        return tema.textoSidebar();
    }

    @Override
    public Color colorTextoSobreAcento() {
        return tema.textoSobreAcento();
    }

    @Override
    public Color colorInformacion() {
        return tema.informacion();
    }

    @Override
    public Color colorDestacado() {
        return tema.destacado();
    }

    @Override
    public BannerRotativo crearBannerRotativo() {
        // Tres pares que alternan marca, compra e información. Salen de la
        // paleta, así que un tema nuevo los cambia sin tocar el banner.
        List<Color[]> degradados = List.of(
                new Color[]{tema.acentoPresionado(), tema.informacion()},
                new Color[]{tema.destacadoPresionado(), tema.acento()},
                new Color[]{tema.informacion(), tema.acentoPresionado()});
        return new BannerRotativo(this, degradados, this::cargarImagenProducto);
    }

    @Override
    public boolean mostrarVentanaPromocional(Component padre, String titulo, String subtitulo,
                                             JComponent cuerpo, String textoAccion) {
        boolean[] aceptada = {false};
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(padre),
                Dialog.ModalityType.APPLICATION_MODAL);
        // Sin barra del sistema: la promoción se cierra con su propia X, más
        // visible que la de la barra de título, como pide un pop-up de tienda.
        dialogo.setUndecorated(true);

        // Cabecera con la X superpuesta. La X no es hija de la cabecera (que
        // se dibuja entera): va en la capa de encima de un JLayeredPane.
        CabeceraDegradada cabecera = new CabeceraDegradada(this, tema.destacadoPresionado(),
                tema.acento(), titulo, subtitulo, ANCHO_PROMOCION, ALTO_CABECERA_PROMOCION);
        JButton cerrar = new JButton(new IconoCerrar(tema.textoSobreAcento(), 16));
        cerrar.setToolTipText("Cerrar");
        cerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // 'toolBarButton': FlatLaf no le pinta fondo en reposo, así que se ve
        // el degradado detrás; al pasar el mouse sí aparece el realce.
        aplicarEstilo(cerrar, () -> "buttonType: toolBarButton;"
                + "arc: 999;"
                + "focusWidth: 0;"
                + "hoverBackground: " + hex(tema.acentoPresionado()) + ";"
                + "pressedBackground: " + hex(tema.acento()));
        cerrar.addActionListener(e -> dialogo.dispose());

        JLayeredPane capaCabecera = new JLayeredPane();
        capaCabecera.setPreferredSize(cabecera.getPreferredSize());
        cabecera.setBounds(0, 0, ANCHO_PROMOCION, ALTO_CABECERA_PROMOCION);
        cerrar.setBounds(ANCHO_PROMOCION - TAM_BOTON_CERRAR - 10, 10,
                TAM_BOTON_CERRAR, TAM_BOTON_CERRAR);
        capaCabecera.add(cabecera, JLayeredPane.DEFAULT_LAYER);
        capaCabecera.add(cerrar, JLayeredPane.PALETTE_LAYER);

        JPanel marcoCuerpo = new JPanel(new BorderLayout());
        marcoCuerpo.setBackground(tema.panel());
        marcoCuerpo.setBorder(new EmptyBorder(18, 24, 8, 24));
        marcoCuerpo.add(cuerpo, BorderLayout.CENTER);

        JButton accion = crearBotonDestacado(textoAccion);
        accion.addActionListener(e -> {
            aceptada[0] = true;
            dialogo.dispose();
        });
        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(tema.panel());
        pie.setBorder(new EmptyBorder(8, 24, 22, 24));
        pie.add(accion, BorderLayout.CENTER);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(tema.panel());
        // Sin decoración, el borde es lo que separa la ventana de lo que hay
        // detrás; en el tema claro, blanco sobre blanco no tendría contorno.
        contenido.setBorder(BorderFactory.createLineBorder(tema.borde(), 1));
        contenido.add(capaCabecera, BorderLayout.NORTH);
        contenido.add(marcoCuerpo, BorderLayout.CENTER);
        contenido.add(pie, BorderLayout.SOUTH);

        dialogo.setContentPane(contenido);
        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.pack();
        dialogo.setLocationRelativeTo(padre);
        instalarEntradaSuave(dialogo);
        dialogo.setVisible(true);
        return aceptada[0];
    }

    @Override
    public Icon crearEstrellas(double calificacion, int tamano) {
        return new IconoEstrellas(calificacion, 5, tamano, tema.advertencia(), estrellaVacia());
    }

    @Override
    public Icon crearEstrella(boolean llena, int tamano) {
        return new IconoEstrellas(llena ? 1 : 0, 1, tamano, tema.advertencia(), estrellaVacia());
    }

    /**
     * Color de la parte vacía de una estrella. En el tema oscuro, el borde de
     * siempre casi no se distingue del panel (comprobado en captura): se usa
     * el escalón siguiente.
     */
    private Color estrellaVacia() {
        // Color vivo: también cambia al alternar el tema con la ventana abierta.
        return tema.derivado(p -> p.esOscura() ? p.bordeHover() : p.borde());
    }

    @Override
    public SelectorEstrellas crearSelectorEstrellas() {
        return new SelectorEstrellas(this);
    }

    @Override
    public JButton crearBotonTema(boolean sobreBarraOscura, boolean conTexto) {
        // El ícono pregunta el tema al pintarse: luna en el claro, sol en el
        // oscuro, sin tener que cambiarlo a mano tras alternar.
        JButton boton = new JButton(new IconoTema(() -> !tema.esOscura(),
                sobreBarraOscura ? tema.textoSidebar() : tema.texto(), 20));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (conTexto) {
            boton.setFont(fuente(Font.PLAIN, 13));
            boton.setForeground(sobreBarraOscura ? tema.textoSidebar() : tema.texto());
            boton.setIconTextGap(10);
        } else {
            boton.setPreferredSize(new Dimension(40, 40));
        }
        Runnable rotular = () -> {
            boton.setToolTipText(tema.esOscura() ? "Cambiar a modo claro" : "Cambiar a modo oscuro");
            if (conTexto) {
                boton.setText(tema.esOscura() ? "Modo claro" : "Modo oscuro");
            }
        };
        rotular.run();
        alCambiarTema(boton, rotular);
        // Sin fondo en reposo, como un ícono de barra; al pasar el mouse sube
        // un escalón del color sobre el que está.
        aplicarEstilo(boton, () -> "buttonType: toolBarButton;"
                + "arc: " + (conTexto ? RADIO_BOTON * 2 : 999) + ";"
                + "focusWidth: 0;"
                + "margin: 6,8,6,12;"
                + "hoverBackground: " + hex(sobreBarraOscura ? tema.sidebarHover() : tema.campo())
                + ";pressedBackground: "
                + hex(sobreBarraOscura ? tema.sidebarPresionado() : tema.borde()));
        return boton;
    }

    @Override
    public boolean esTemaOscuro() {
        return tema.esOscura();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>No cierra ninguna ventana.</b> Los pasos, en orden:</p>
     * <ol>
     *   <li>Foto de las ventanas abiertas ({@link TransicionTema}), que tapa el
     *       instante del cambio.</li>
     *   <li>Cambio de paleta: todos los colores repartidos por esta fábrica
     *       son vivos ({@code ColorDeTema}), así que con esto ya cambian.</li>
     *   <li>Instalación de {@code FlatLightLaf} o {@code FlatDarkLaf} (con la
     *       paleta morada oscura) y {@code SwingUtilities.updateComponentTreeUI}
     *       de cada ventana, para lo que pinta el propio Look and Feel: barras
     *       de desplazamiento, barra de título, selección de texto.</li>
     *   <li>Hojas de estilo de FlatLaf recalculadas (son texto con hex) y
     *       textos que dependen del tema (el del propio conmutador).</li>
     *   <li>{@code revalidate()} y {@code repaint()} de cada ventana, y el
     *       fundido de la foto.</li>
     * </ol>
     * <p>Los controladores, el carrito, la búsqueda escrita y la posición del
     * catálogo siguen intactos: son los mismos objetos.</p>
     */
    @Override
    public void alternarTema() {
        TransicionTema transicion = TransicionTema.capturar();

        tema.cambiar(tema.esOscura() ? Paleta.clara() : Paleta.oscura());
        if (tema.esOscura()) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        aplicarClavesBase();

        for (Window ventana : Window.getWindows()) {
            if (!ventana.isDisplayable()) {
                continue;
            }
            SwingUtilities.updateComponentTreeUI(ventana);
            reaplicarTema(ventana);
            ventana.revalidate();
            ventana.repaint();
        }
        transicion.desvanecer();
    }

    /**
     * Recorre el árbol de una ventana rehaciendo lo que los colores vivos no
     * cubren: la hoja de estilo de FlatLaf de cada componente que la tenga y
     * las acciones registradas con {@link #alCambiarTema(JComponent, Runnable)}.
     */
    private void reaplicarTema(Component componente) {
        if (componente instanceof JComponent j) {
            try {
                if (j.getClientProperty(PROPIEDAD_ESTILO) instanceof Supplier<?> estilo) {
                    j.putClientProperty(FlatClientProperties.STYLE, estilo.get());
                }
                if (j.getClientProperty(PROPIEDAD_AL_CAMBIAR_TEMA) instanceof Runnable accion) {
                    accion.run();
                }
            } catch (RuntimeException ex) {
                // Un estilo que FlatLaf rechace no debe dejar el cambio de tema
                // a medias en el resto de la ventana: se avisa y se sigue.
                System.err.println("No se pudo reaplicar el tema a "
                        + j.getClass().getSimpleName() + ": " + ex.getMessage());
            }
        }
        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                reaplicarTema(hijo);
            }
        }
    }

    @Override
    public void usarFuenteDeImagenes(Function<String, byte[]> fuente) {
        this.fuenteDeImagenes = fuente;
    }

    @Override
    public Font fuente(int estilo, int tamano) {
        return new Font(FAMILIA_TIPOGRAFICA, estilo, tamano);
    }

}
