package view.factory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.LayoutManager;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import view.factory.charts.GraficoBarras;
import view.factory.components.BotonCarrito;
import view.factory.components.CampoPasswordConToggle;
import view.factory.components.CampoTextoConIcono;
import view.factory.components.SelectorEstrellas;
import view.factory.components.SelectorSegmentado;
import view.factory.effects.IndicadorCarga;
import view.factory.icons.IconoCampo;
import view.factory.promo.BannerRotativo;

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
     * Crea un control segmentado: todas las opciones visibles a la vez y la
     * elección en un solo clic.
     *
     * <p>Nació para el tipo de cuenta del Registro y hoy lo usa también el
     * formulario del Proveedor para elegir la categoría de un producto; por
     * eso el nombre es genérico y no menciona ningún caso de uso.</p>
     *
     * @param opciones  valores entre los que se elige
     * @param alCambiar acción a ejecutar cuando cambia la selección
     * @return selector estilizado, con la primera opción ya activa
     */
    SelectorSegmentado crearSelectorOpciones(String[] opciones, Runnable alCambiar);

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
     * Crea el botón de las llamadas a la acción de compra ("Confirmar compra",
     * "Ver oferta"): mismo tamaño y comportamiento que el primario, pero en el
     * color destacado (naranja).
     *
     * <p>Se separa del primario a propósito: el morado es la marca y sirve
     * para los formularios; el naranja contrasta con él y señala dónde se
     * compra, que es la acción que una tienda quiere que no pase desapercibida.</p>
     *
     * @param texto texto del botón
     * @return botón estilizado, con el realce de hover ya instalado
     */
    JButton crearBotonDestacado(String texto);

    /**
     * Crea el botón "Continuar con Google": del mismo tamaño que el botón
     * principal, pero oscuro y con borde, con la "G" en sus colores.
     *
     * <p>Es secundario a propósito. El botón de acento sigue siendo el de la
     * contraseña; dos botones igual de llamativos en la misma tarjeta harían
     * dudar de cuál es el principal.</p>
     *
     * @param texto texto del botón
     * @return botón estilizado
     */
    JButton crearBotonGoogle(String texto);

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
     * Igual que {@link #mostrarDialogoExito(Component, String)}, pero para un
     * aviso o un error: título "Aviso" e ícono de advertencia.
     *
     * @param padre   componente sobre el que se centra el diálogo
     * @param mensaje texto a mostrar
     */
    void mostrarDialogoAviso(Component padre, String mensaje);

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

    /**
     * Crea el avatar circular con las iniciales del usuario.
     *
     * <p><b>Devuelve un {@link JButton} y no una etiqueta.</b> Es un control
     * que se pulsa para abrir un menú, así que debe comportarse como tal: como
     * {@code JLabel} había que escucharlo con {@code mouseClicked}, que
     * <em>no se dispara si el ratón se mueve un píxel entre pulsar y soltar</em>
     * —comprobado: con un arrastre de 1px el menú no abría—. Siendo botón,
     * cualquier pulsación en cualquier punto del componente dispara su acción,
     * y de paso gana los estados de hover y pulsado de FlatLaf.</p>
     *
     * @param iniciales una o dos letras del nombre
     * @param tamano    diámetro en píxeles
     * @return botón circular listo para recibir su {@code ActionListener}
     */
    JButton crearAvatar(String iniciales, int tamano);

    /**
     * Instala el realce de expansión al pasar el mouse: el botón crece unos
     * píxeles hacia fuera y vuelve suavemente al salir.
     *
     * <p><b>No toca el tamaño del componente.</b> Lo que se anima es el margen
     * que FlatLaf reserva alrededor del relleno ({@code focusWidth}), de modo
     * que crece el área pintada y no el rectángulo que ocupa: el gestor de
     * disposición sigue reservándole lo mismo y nada se mueve alrededor. Una
     * versión anterior de este efecto animaba {@code preferredSize} y hacía
     * saltar el formulario entero bajo el cursor; no vuelvas a ese camino.</p>
     *
     * <p>Mientras el botón tiene el foco no se expande: ese hueco es el anillo
     * de foco y cederlo dejaría al usuario de teclado sin referencia.</p>
     *
     * @param boton botón al que instalar el realce
     */
    void instalarEfectoExpansionHover(JButton boton);

    /**
     * Hace que un campo suelte el foco cuando se pulsa fuera de él.
     *
     * <p>Swing solo mueve el foco al pulsar sobre algo <em>enfocable</em>. Si
     * el usuario pulsa en una zona vacía —el fondo del catálogo, por ejemplo—
     * el campo se queda con el cursor parpadeando indefinidamente. Esto vigila
     * las pulsaciones y, cuando una cae fuera del campo teniéndolo él
     * enfocado, se lo retira.</p>
     *
     * <p>La vigilancia se conecta y se desconecta sola según el campo esté o
     * no en pantalla, así que cerrar la ventana no deja nada colgando.</p>
     *
     * @param campo campo que debe soltar el foco al pulsar fuera
     */
    void instalarLiberacionDeFoco(JComponent campo);

    /**
     * Envuelve un contenido en un área desplazable con el estilo del tema.
     *
     * @param contenido componente a desplazar
     * @return panel desplazable, sin borde y con el fondo del tema
     */
    JScrollPane crearScroll(JComponent contenido);

    /**
     * Muestra contenido propio en un diálogo con un único botón para
     * cerrarlo, para lo que solo se consulta (como "Mis compras").
     *
     * @param padre       componente sobre el que se centra
     * @param titulo      título de la ventana
     * @param contenido   panel a mostrar dentro del diálogo
     * @param textoCerrar texto del botón
     */
    void mostrarDialogoContenido(Component padre, String titulo, JComponent contenido,
                                 String textoCerrar);

    /**
     * Muestra un diálogo de aceptar/cancelar con contenido propio, usando el
     * mismo tratamiento de tema que {@link #mostrarDialogoExito(Component, String)}
     * (que Nimbus, si no, pinta con su fondo claro).
     *
     * @param padre     componente sobre el que se centra
     * @param titulo    título de la ventana
     * @param contenido panel a mostrar dentro del diálogo
     * @param textoOk   texto del botón de confirmación
     * @return {@code true} si el usuario confirmó
     */
    boolean mostrarDialogoConfirmacion(Component padre, String titulo,
                                       JComponent contenido, String textoOk);

    /**
     * Crea el menú flotante del avatar de usuario, con su encabezado.
     *
     * @param nombreUsuario nombre que encabeza el menú
     * @return menú vacío, listo para recibir opciones
     */
    JPopupMenu crearMenuUsuario(String nombreUsuario);

    /**
     * Agrega una opción al menú de usuario.
     *
     * @param menu   menú creado con {@link #crearMenuUsuario(String)}
     * @param texto  texto de la opción
     * @param accion qué ejecutar al elegirla
     */
    void agregarOpcionMenu(JPopupMenu menu, String texto, Runnable accion);

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

    /** @return color del texto sobre el sidebar y la barra superior oscura */
    Color colorTextoSidebar();

    /** @return color del texto sobre el acento o el destacado (insignias, botones) */
    Color colorTextoSobreAcento();

    /** @return color informativo (azul), para datos que no son ni éxito ni alerta */
    Color colorInformacion();

    /** @return color de las llamadas a la acción de compra (naranja) */
    Color colorDestacado();

    /**
     * Crea el banner rotativo de la parte superior del catálogo.
     *
     * @return banner sin diapositivas (muestra una de bienvenida) hasta que se
     *         le pasen con {@code mostrar(...)}
     */
    BannerRotativo crearBannerRotativo();

    /**
     * Muestra una ventana emergente promocional: cabecera con degradado y
     * botón de cerrar, el contenido que entrega la vista y un botón de acción.
     *
     * <p>Es modal, sin barra de título del sistema, y se cierra con la X, con
     * Escape o con la acción. A diferencia de
     * {@link #mostrarDialogoConfirmacion(Component, String, JComponent, String)},
     * no ofrece "Cancelar": una promoción se descarta cerrándola, no
     * rechazándola.</p>
     *
     * @param padre       componente sobre el que se centra
     * @param titulo      frase de la cabecera
     * @param subtitulo   línea de apoyo de la cabecera
     * @param cuerpo      contenido (imagen, precio...), armado por la vista
     * @param textoAccion texto del botón de acción
     * @return {@code true} si se pulsó la acción; {@code false} si se cerró
     */
    boolean mostrarVentanaPromocional(Component padre, String titulo, String subtitulo,
                                      JComponent cuerpo, String textoAccion);

    /**
     * Crea la fila de cinco estrellas de una calificación (admite medias
     * estrellas).
     *
     * @param calificacion promedio de 0 a 5
     * @param tamano       lado de cada estrella en píxeles
     * @return ícono listo para un {@code JLabel}
     */
    Icon crearEstrellas(double calificacion, int tamano);

    /**
     * @param llena  {@code true} para una estrella rellena
     * @param tamano lado en píxeles
     * @return una estrella suelta (los botones del selector de calificación)
     */
    Icon crearEstrella(boolean llena, int tamano);

    /**
     * @return control para elegir de 1 a 5 estrellas, sin ninguna elegida
     */
    SelectorEstrellas crearSelectorEstrellas();

    /**
     * Crea el botón que alterna entre modo claro y oscuro. Muestra el tema al
     * que lleva (luna en el claro, sol en el oscuro) y lo actualiza solo tras
     * cada cambio, igual que su ayuda y su texto.
     *
     * @param sobreBarraOscura {@code true} si va en la barra superior o el
     *                         sidebar, que son oscuros en los dos temas
     * @param conTexto         {@code true} para acompañar el ícono con
     *                         "Modo oscuro" / "Modo claro"
     * @return botón sin acción; quien lo coloca decide qué hace
     */
    JButton crearBotonTema(boolean sobreBarraOscura, boolean conTexto);

    /** @return {@code true} si el tema actual es el oscuro */
    boolean esTemaOscuro();

    /**
     * Cambia al otro tema (claro ↔ oscuro) en caliente, en todas las ventanas
     * abiertas, sin cerrar ninguna.
     *
     * <p>Instala la variante de FlatLaf que corresponde, actualiza los UI con
     * {@code SwingUtilities.updateComponentTreeUI} y repinta, con un fundido
     * para que el cambio no sea un salto. Los colores que reparte la fábrica
     * son vivos (siguen a la paleta vigente), así que los componentes ya
     * creados cambian también. La sesión, el carrito y lo escrito quedan
     * intactos: no se reconstruye nada.</p>
     */
    void alternarTema();

    /**
     * Le da a la fábrica de dónde leer las imágenes guardadas en el almacén
     * ({@code img:<id>}).
     *
     * <p>La vista no puede importar el modelo, así que no conoce el
     * repositorio de imágenes: {@code app.Main} le pasa una función que, dada
     * la referencia, devuelve los bytes. Las imágenes ya leídas se guardan en
     * memoria para no volver a pedirlas.</p>
     *
     * @param fuente referencia → bytes de la imagen, o {@code null} si no existe
     */
    void usarFuenteDeImagenes(java.util.function.Function<String, byte[]> fuente);

    /**
     * Crea el campo de la barra de búsqueda: más ancho que un campo de
     * formulario y con una lupa como ícono de contexto.
     *
     * <p>Es el único sitio del proyecto donde un texto fantasma hace de
     * etiqueta, y es el caso en que las guías de usabilidad lo admiten: un
     * buscador es un campo único y familiar, así que no hay nada que recordar
     * ni que verificar antes de enviar. En los formularios, la etiqueta sigue
     * yendo fuera del campo.</p>
     *
     * @param textoFantasma guía gris mostrada mientras está vacío
     * @return campo de búsqueda estilizado
     */
    CampoTextoConIcono crearCampoBusqueda(String textoFantasma);

    /**
     * Crea la etiqueta de alerta de un formulario: la franja donde aparecen
     * los errores de validación.
     *
     * <p>Arranca vacía e invisible. Se llena con
     * {@link #pintarAlerta(JLabel, String, boolean)}, que además decide si es
     * un error o un aviso; así la vista no elige colores.</p>
     *
     * @return etiqueta lista para colocar en el formulario
     */
    JLabel crearAlerta();

    /**
     * Llena (o vacía) una alerta creada con {@link #crearAlerta()}.
     *
     * @param alerta   etiqueta a actualizar
     * @param mensaje  texto a mostrar; vacío o {@code null} la oculta
     * @param esError  {@code true} para el tratamiento de error, {@code false}
     *                 para un aviso informativo
     */
    void pintarAlerta(JLabel alerta, String mensaje, boolean esError);

    /**
     * Crea un botón de acción flotante (FAB): circular, en color de acento y
     * pensado para superponerse al contenido.
     *
     * <p>Se reserva para acciones que <em>no</em> están ya visibles en la
     * pantalla. Duplicar con un FAB un botón que el usuario ya tiene delante
     * añade ruido, no alcance.</p>
     *
     * @param glifo       texto corto del botón (una flecha, un signo)
     * @param descripcion ayuda emergente que explica qué hace
     * @param tamano      diámetro en píxeles
     * @return el botón, listo para colocar en una capa superpuesta
     */
    JButton crearBotonFlotante(String glifo, String descripcion, int tamano);

    /**
     * Crea el botón de carrito de la barra superior, con su contador.
     *
     * @return botón con badge, listo para recibir su acción
     */
    BotonCarrito crearBotonCarrito();

    /**
     * Ejecuta una tarea lenta fuera del hilo de eventos, mostrando un
     * indicador mientras dura.
     *
     * <p>Swing pinta en un solo hilo: una consulta a la base o una petición de
     * red hechas ahí dejan la ventana congelada. Esto la mueve a un
     * {@code SwingWorker} y devuelve el resultado ya en el hilo de eventos,
     * que es el único desde el que se puede tocar la interfaz.</p>
     *
     * @param <T>        tipo del resultado
     * @param padre      componente sobre el que se centra el indicador
     * @param mensaje    qué se está haciendo ("Guardando...", "Conectando...")
     * @param tarea      trabajo lento; **no** debe tocar componentes Swing
     * @param alTerminar qué hacer con el resultado, ya en el hilo de eventos
     * @param alFallar   qué hacer si la tarea falló, también en el hilo de
     *                   eventos; recibe la excepción para poder explicarla
     */
    <T> void ejecutarConCarga(Component padre, String mensaje,
                              java.util.function.Supplier<T> tarea,
                              java.util.function.Consumer<T> alTerminar,
                              java.util.function.Consumer<Exception> alFallar);

    /**
     * Igual que {@link #ejecutarConCarga}, pero con un botón "Cancelar".
     *
     * <p>Para esperas que dependen del usuario y no de la red, como terminar
     * el acceso con Google en el navegador. Pulsar "Cancelar" ejecuta
     * {@code alCancelar}, que debe hacer que la tarea vuelva cuanto antes; el
     * indicador se cierra cuando la tarea vuelve, no antes.</p>
     *
     * @param <T>        tipo del resultado
     * @param padre      componente sobre el que se centra el indicador
     * @param mensaje    qué se está esperando
     * @param tarea      trabajo lento; **no** debe tocar componentes Swing
     * @param alTerminar qué hacer con el resultado, ya en el hilo de eventos
     * @param alFallar   qué hacer si la tarea falló, en el hilo de eventos
     * @param alCancelar cómo pedirle a la tarea que termine
     */
    <T> void ejecutarCancelable(Component padre, String mensaje,
                                java.util.function.Supplier<T> tarea,
                                java.util.function.Consumer<T> alTerminar,
                                java.util.function.Consumer<Exception> alFallar,
                                Runnable alCancelar);

    /**
     * Crea un diálogo modal vacío con el tema aplicado, para pantallas que
     * necesitan más que un "aceptar / cancelar".
     *
     * <p>{@link #mostrarDialogoConfirmacion(Component, String, JComponent,
     * String)} sirve cuando la respuesta es un sí o un no y el diálogo se
     * cierra en cualquier caso. No sirve cuando hay que validar lo escrito y
     * <em>seguir abierto</em> si algo está mal, que es lo que necesita la
     * edición de perfil: aquí se devuelve el {@link JDialog} y quien lo pide
     * decide cuándo cerrarlo.</p>
     *
     * @param padre     componente sobre el que se centra
     * @param titulo    título de la ventana
     * @param contenido panel con los campos y los botones
     * @return el diálogo, todavía sin mostrar
     */
    JDialog crearDialogoModal(Component padre, String titulo, JComponent contenido);

    /**
     * Abre el selector de archivos para elegir la imagen de un producto.
     *
     * <p>Filtrado a los formatos que el proyecto sabe leer
     * ({@code .png}, {@code .jpg}, {@code .jpeg}). Vive en la fábrica y no en
     * la vista por la misma razón que el resto: es un componente de Swing con
     * estilo, y ninguna pantalla debe montar el suyo a mano.</p>
     *
     * @param padre    componente sobre el que se centra el diálogo
     * @param rutaActual ruta ya seleccionada, para abrir en esa carpeta; puede
     *                   ir vacía
     * @return ruta absoluta del archivo elegido, o {@code null} si se canceló
     */
    String elegirImagen(Component padre, String rutaActual);

    /**
     * Crea el recuadro de imagen de un producto.
     *
     * <p>Busca la referencia primero como recurso del propio programa
     * ({@code /resources/images/productos/...}) y después como archivo en
     * disco, de modo que un proveedor pueda registrar tanto una imagen
     * incluida en la entrega como una ruta suya. Si no hay imagen, o no se
     * puede leer, devuelve un recuadro con la inicial del producto: un hueco
     * con algo dentro se lee mejor que un marco roto.</p>
     *
     * @param referencia ruta o nombre de archivo guardado en el producto
     * @param nombre     nombre del producto, para la inicial de respaldo
     * @param ancho      ancho del recuadro en píxeles
     * @param alto       alto del recuadro en píxeles
     * @return etiqueta con la imagen o con su respaldo
     */
    JLabel crearImagenProducto(String referencia, String nombre, int ancho, int alto);

    /**
     * Crea un gráfico de barras vacío, listo para recibir sus datos.
     *
     * @return gráfico con los colores del tema
     */
    GraficoBarras crearGraficoBarras();

    /**
     * Abre o cierra un panel lateral animando su ancho.
     *
     * <p>Mismo mecanismo que el resto de animaciones de la fábrica:
     * interpola {@code setPreferredSize} + {@code revalidate()} con un
     * {@code Timer}. No pinta nada a mano y el contenido del panel no se
     * reacomoda, solo el espacio que ocupa.</p>
     *
     * @param panel        panel lateral a mover
     * @param anchoDestino ancho final en píxeles; cero lo cierra
     */
    void deslizarPanelLateral(JPanel panel, int anchoDestino);

    /**
     * Crea una tabla de datos con el tema oscuro ya aplicado (celdas,
     * cabecera, selección y separadores).
     *
     * <p>Existe por la misma razón que el resto de la fábrica: Nimbus pinta
     * las celdas y la cabecera con sus propios <i>painters</i> claros e
     * ignora {@code setBackground}. El remedio es instalar renderizadores
     * opacos estándar del JDK ({@code DefaultTableCellRenderer}), no escribir
     * un {@code ComponentUI} propio.</p>
     *
     * @param modelo datos y columnas de la tabla
     * @return tabla estilizada, de una sola fila seleccionable a la vez
     */
    JTable crearTabla(TableModel modelo);

    /**
     * Tipografía unificada del tema.
     *
     * @param estilo constante {@link Font#PLAIN}, {@link Font#BOLD}, etc.
     * @param tamano tamaño en puntos
     * @return fuente lista para aplicar a cualquier componente
     */
    Font fuente(int estilo, int tamano);
}
