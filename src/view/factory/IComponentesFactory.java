package view.factory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.LayoutManager;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import view.factory.components.CampoPasswordConToggle;
import view.factory.components.CampoTextoConIcono;
import view.factory.components.SelectorSegmentado;
import view.factory.effects.IndicadorCarga;
import view.factory.icons.IconoCampo;

/**
 * Contrato de una fábrica de componentes Swing con estilo unificado.
 *
 * <p><b>Abierto/Cerrado (O de SOLID):</b> las vistas dependen de esta
 * interfaz, nunca de una implementación concreta. Un tema nuevo (por ejemplo,
 * un modo claro) se incorpora creando otra clase que la implemente; ninguna
 * vista existente necesita modificarse para adoptarlo.</p>
 *
 * <p><b>Inversión de Dependencias (D de SOLID):</b> tanto la paleta de
 * colores como la tipografía se consultan a través de esta abstracción, por
 * lo que ninguna vista vuelve a instanciar un {@link Color} o un
 * {@link Font} por su cuenta.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 1.0
 */
public interface IComponentesFactory {

    /**
     * Crea una etiqueta descriptiva con el estilo del tema.
     *
     * @param texto contenido de la etiqueta
     * @return etiqueta lista para agregarse a un formulario
     */
    JLabel crearEtiqueta(String texto);

    /**
     * Crea un campo de texto con ícono de contexto a la izquierda y texto
     * fantasma (placeholder) que desaparece al recibir el foco.
     *
     * @param textoFantasma guía gris mostrada cuando el campo está vacío
     * @param tipoIcono     glifo que da contexto sobre qué se pide en el campo
     * @return campo compuesto listo para agregarse al formulario
     */
    CampoTextoConIcono crearCampoTexto(String textoFantasma, IconoCampo.Tipo tipoIcono);

    /**
     * Crea el control compuesto de contraseña (ícono + campo + botón para
     * mostrar/ocultar), también con texto fantasma.
     *
     * @param textoFantasma guía gris mostrada cuando el campo está vacío
     * @return control de contraseña listo para agregarse al formulario
     */
    CampoPasswordConToggle crearCampoPassword(String textoFantasma);

    /**
     * Crea un botón de navegación del panel lateral. Vive en la fábrica (y no
     * en {@code MainFrame}) para que el sidebar no estilice componentes a
     * mano: la fábrica sigue siendo la única que conoce colores, tipografía,
     * bordes y efectos de interacción.
     *
     * @param texto texto del botón
     * @return botón estilizado, con el efecto de hover ya instalado
     */
    JButton crearBotonSidebar(String texto);

    /**
     * Marca un botón del sidebar como activo o inactivo (el resaltado de la
     * pestaña visible). La vista decide <em>cuál</em> está activo; la fábrica
     * decide <em>cómo</em> se ve.
     *
     * @param boton  botón creado con {@link #crearBotonSidebar(String)}
     * @param activo {@code true} si corresponde a la carta visible
     */
    void resaltarBotonSidebar(JButton boton, boolean activo);

    /**
     * Aplica a un contenedor de campo el borde redondeado del tema y lo
     * cambia por su versión resaltada mientras el campo interno tiene el
     * foco (el "focus ring" de los formularios modernos, que indica dónde
     * está escribiendo el usuario).
     *
     * @param contenedor componente compuesto que pinta el marco del campo
     * @param campo      campo de texto interno cuyo foco se observa
     */
    void instalarAnilloEnfoque(JComponent contenedor, JTextComponent campo);

    /**
     * Lanza la animación de celebración (confeti) sobre la ventana que
     * contiene al componente dado y reproduce el sonido de éxito. La vista la
     * invoca al confirmar un registro; el controlador no sabe que existe.
     *
     * @param origen componente desde el que se ubica la ventana a decorar
     */
    void celebrar(Component origen);

    /**
     * Crea un indicador circular que gira de forma continua, sin porcentaje.
     * Es el adecuado para esperas cortas (1–3 s), donde un avance numérico
     * cambiaría demasiado rápido para aportar información.
     *
     * @param tamano lado en píxeles
     * @return indicador detenido; hay que llamar a {@code iniciar()}
     */
    IndicadorCarga crearIndicadorGiratorio(int tamano);

    /**
     * Crea un indicador circular que muestra avance real. Es el adecuado para
     * esperas medias, donde ver cuánto falta reduce la espera percibida.
     *
     * @param tamano lado en píxeles
     * @return indicador detenido, en 0; se alimenta con {@code setProgreso(...)}
     */
    IndicadorCarga crearIndicadorProgreso(int tamano);

    /**
     * Muestra una ventana flotante de transición (un indicador giratorio y un
     * texto) durante un instante y, al cerrarse, ejecuta {@code alTerminar}.
     * Sirve para que un cambio de pantalla no ocurra de golpe.
     *
     * <p>No sustituye ni toca la confirmación de éxito con
     * {@link #mostrarDialogoExito(Component, String)}: son cosas distintas —
     * una confirma un resultado y la cierra el usuario, esta solo acompaña
     * una espera y se cierra sola.</p>
     *
     * @param padre       componente sobre el que se centra la ventana
     * @param mensaje     texto que acompaña al indicador
     * @param alTerminar  acción a ejecutar cuando termina la transición
     */
    void mostrarTransicion(Component padre, String mensaje, Runnable alTerminar);

    /**
     * Crea el selector de tipo de cuenta como control segmentado: todas las
     * opciones visibles a la vez y la elección en un solo clic.
     *
     * @param opciones  valores entre los que se elige
     * @param alCambiar acción a ejecutar cuando cambia la selección
     * @return selector estilizado, con la primera opción ya activa
     */
    SelectorSegmentado crearSelectorTipoCuenta(String[] opciones, Runnable alCambiar);

    /**
     * Crea un botón individual de un control segmentado.
     *
     * @param texto texto del segmento
     * @return botón estilizado en su estado inactivo
     */
    JButton crearBotonSegmento(String texto);

    /**
     * Marca un segmento como activo o inactivo.
     *
     * @param segmento botón creado con {@link #crearBotonSegmento(String)}
     * @param activo   {@code true} si es la opción elegida
     */
    void resaltarSegmento(JButton segmento, boolean activo);

    /**
     * Crea el botón de acción principal del formulario (color de acento y
     * realce al pasar el mouse).
     *
     * @param texto texto del botón
     * @return botón estilizado
     */
    JButton crearBotonPrimario(String texto);

    /**
     * Crea una etiqueta con apariencia de enlace (para acciones secundarias
     * como "¿Ya tienes cuenta? Inicia sesión").
     *
     * @param texto contenido del enlace
     * @return etiqueta con cursor de mano y color de acento
     */
    JLabel crearEnlaceSecundario(String texto);

    /**
     * Crea el contenedor "tarjeta" (fondo y borde del tema) que agrupa un
     * formulario o sección relacionada. Ninguna vista vuelve a instanciar un
     * {@code JPanel} y estilizarlo a mano: se lo pide a la fábrica, igual
     * que cualquier otro componente.
     *
     * @param layout gestor de layout a usar dentro de la tarjeta
     * @return panel vacío, listo para agregarle contenido
     */
    JPanel crearTarjeta(LayoutManager layout);

    /**
     * Crea el logo de la aplicación ya escalado, con un marcador de posición
     * ("EC") si el archivo de imagen todavía no existe. Centraliza la carga
     * (antes duplicada en cada vista con encabezado) en un único lugar.
     *
     * @param tamanoPx lado del logo en píxeles (se muestra cuadrado)
     * @return etiqueta lista para agregarse al encabezado de una vista
     */
    JLabel crearLogo(int tamanoPx);

    /**
     * Crea el ícono decorativo del asistente animado (GIF) para el sidebar,
     * ya escalado al tamaño pedido. Puramente decorativo: si el archivo no
     * existe, entrega una etiqueta vacía en vez de un marcador de posición
     * (a diferencia de {@link #crearLogo(int)}, que sí necesita uno porque
     * su ausencia dejaría la marca sin identidad visual).
     *
     * @param tamanoPx lado del ícono en píxeles
     * @return etiqueta lista para agregarse al sidebar
     */
    JLabel crearAsistenteAnimado(int tamanoPx);

    /**
     * Muestra un cuadro de diálogo de éxito con el ícono de check propio del
     * proyecto (no el ícono por defecto del Look and Feel) y colores
     * coherentes con el tema. Centraliza aquí lo que antes cada vista
     * llamaba directamente como {@code JOptionPane.showMessageDialog(...)},
     * para no duplicar la carga del ícono ni el ajuste de colores.
     *
     * @param padre   componente sobre el que se centra el diálogo
     * @param mensaje texto a mostrar
     */
    void mostrarDialogoExito(Component padre, String mensaje);

    /**
     * Instala un efecto de "zoom" suave en un botón ya construido: crece
     * unos píxeles al pasar el mouse y vuelve a su tamaño original al
     * salir, animado con un {@link javax.swing.Timer}. Centraliza aquí la
     * técnica para que cualquier botón (los que arma la fábrica o los que
     * arma {@code MainFrame} para su sidebar) la reutilice sin duplicar
     * lógica de animación.
     *
     * @param boton botón ya estilizado (con su tamaño final ya asignado)
     */
    void instalarEfectoZoomHover(JButton boton);

    /** @return color de fondo general de la ventana */
    Color colorFondo();

    /** @return color de fondo de tarjetas, formularios y contenedores centrales */
    Color colorPanel();

    /**
     * @return color de relleno de los campos de captura; un paso más claro que
     *         la tarjeta, siguiendo el principio de "elevación por luminosidad"
     *         del diseño en modo oscuro (los elementos interactivos se acercan
     *         a la luz en vez de usar sombras)
     */
    Color colorCampo();

    /** @return lado en píxeles del ícono de contexto de un campo */
    int tamanoIconoCampo();

    /** @return ancho estándar de un campo de captura */
    int anchoCampo();

    /** @return alto estándar de un campo de captura */
    int altoCampo();

    /** @return color de fondo del panel lateral (sidebar) de {@code MainFrame} */
    Color colorSidebar();

    /** @return color de acento (botón principal, enlaces, resaltados) */
    Color colorAcento();

    /** @return color de texto principal */
    Color colorTexto();

    /** @return color de texto secundario (etiquetas, ayudas, placeholders) */
    Color colorTextoSuave();

    /** @return color de bordes sutiles */
    Color colorBorde();

    /** @return color de mensajes de éxito */
    Color colorExito();

    /** @return color de advertencia (estados intermedios, ni error ni éxito) */
    Color colorAdvertencia();

    /**
     * Crea el punto de color que hace de semáforo junto a un campo.
     *
     * @param color    relleno del punto
     * @param diametro tamaño en píxeles
     * @return ícono listo para asignarse a un {@code JLabel}
     */
    Icon crearPunto(Color color, int diametro);

    /** @return color de mensajes de error */
    Color colorError();

    /**
     * Tipografía unificada del tema.
     *
     * @param estilo constante {@link Font#PLAIN}, {@link Font#BOLD}, etc.
     * @param tamano tamaño en puntos
     * @return fuente lista para aplicar a cualquier componente
     */
    Font fuente(int estilo, int tamano);
}
