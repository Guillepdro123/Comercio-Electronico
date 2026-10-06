package app;

import java.awt.EventQueue;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import aplicacion.acceso.AutenticacionService;
import aplicacion.catalogo.ImportadorImagen;
import aplicacion.catalogo.NormalizacionCatalogo;
import aplicacion.cuenta.CuentaService;
import controller.AccesoGoogleController;
import controller.LoginController;
import controller.SesionController;
import controller.UsuarioController;
import model.repository.IImagenRepository;
import model.repository.IPedidoRepository;
import model.repository.IProductoRepository;
import model.repository.IResenaRepository;
import model.repository.IUsuarioRepository;
import model.repository.memoria.ImagenRepositoryArchivo;
import model.repository.memoria.PedidoRepositoryMemoria;
import model.repository.memoria.ProductoRepositoryMemoria;
import model.repository.memoria.ResenaRepositoryMemoria;
import model.repository.memoria.UsuarioRepositoryImpl;
import model.repository.mongo.ImagenRepositoryMongo;
import model.repository.mongo.MongoDBConnection;
import model.repository.mongo.PedidoRepositoryMongo;
import model.repository.mongo.ProductoRepositoryMongo;
import model.repository.mongo.ResenaRepositoryMongo;
import model.repository.mongo.UsuarioRepositoryMongo;
import model.repository.mongo.VigilanteCatalogoMongo;
import observer.CatalogoSubject;
import service.config.Configuracion;
import service.imagen.DescargadorImagenes;
import service.correo.INotificadorCorreo;
import service.correo.NotificadorEnSegundoPlano;
import service.correo.NotificadorRegistroLocal;
import service.correo.NotificadorResend;
import service.google.GoogleAuthService;
import service.google.IAutenticadorExterno;
import service.sesion.PreferenciasUsuario;
import view.auth.PanelLogin;
import view.auth.PanelRegistroUsuario;
import view.core.MainFrame;
import view.core.VentanaSplash;
import view.factory.ComponentesSwingFactory;
import view.factory.IComponentesFactory;
import view.factory.tema.Paleta;

/**
 * Punto de entrada de la plataforma de comercio electrónico.
 *
 * <p>Actúa como ensamblador: crea el repositorio, la fábrica de componentes,
 * la ventana y los controladores, y los conecta entre sí. Es el único lugar
 * donde se mencionan las implementaciones concretas (repositorio, fábrica de
 * componentes); cambiarlas implica modificar una sola línea de este archivo,
 * sin tocar el resto de las capas.</p>
 *
 * <p><b>Por qué tiene paquete propio:</b> el botón "Cerrar sesión" de los
 * dashboards necesita volver a abrir una ventana principal completamente
 * funcional (Login y Registro con sus controladores conectados), no un
 * {@code MainFrame} vacío. Para reutilizar el mismo ensamblado sin
 * duplicarlo, {@link #mostrarVentanaPrincipal(Infraestructura)} tiene que ser
 * invocable desde
 * {@link controller.SesionController}; Java no permite importar clases del
 * paquete por defecto desde un paquete con nombre, así que esta clase vive
 * en {@code app} en vez de en la raíz. Sigue siendo el único lugar que
 * conoce tanto las vistas como los controladores concretos.</p>
 *
 * @author Ingeniería de Sistemas - Segundo Incremento Funcional
 * @version 3.0
 */
public class Main {

    /**
     * Motivo por el que no se pudo usar Atlas estando configurado, o
     * {@code null}. Lo deja {@link #abrirConexion} y se muestra al usuario al
     * abrir la primera ventana.
     */
    private static String motivoSinAtlas;

    /**
     * Arranca la aplicación dentro del Event Dispatch Thread de Swing.
     *
     * @param args argumentos de línea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        // El Look and Feel se instala DENTRO del hilo de eventos, antes de
        // construir nada: Swing no es seguro para hilos, y cambiar el L&F
        // desde el hilo principal mientras el EDT ya existe es una carrera.
        // Antes se llamaba fuera; se corrigió al integrar FlatLaf.
        //
        // El tema arranca como se dejó la última vez (el conmutador de las
        // ventanas lo guarda); la primera vez, claro.
        Paleta paleta = PreferenciasUsuario.temaOscuro() ? Paleta.oscura() : Paleta.clara();
        EventQueue.invokeLater(() -> {
            aplicarLookAndFeel(paleta.esOscura());

            // Los repositorios y la fábrica se crean una sola vez, en el
            // arranque, y viajan de aquí en adelante (incluidos los logouts)
            // para que los usuarios ya registrados nunca se pierdan.
            Configuracion configuracion = new Configuracion();
            MongoDBConnection conexion = abrirConexion(configuracion);
            IUsuarioRepository usuarios;
            IProductoRepository productos;
            IPedidoRepository pedidos;
            IResenaRepository resenas;
            IImagenRepository imagenes;
            if (conexion == null) {
                usuarios = new UsuarioRepositoryImpl();
                productos = new ProductoRepositoryMemoria();
                pedidos = new PedidoRepositoryMemoria();
                resenas = new ResenaRepositoryMemoria();
                imagenes = new ImagenRepositoryArchivo();
            } else {
                usuarios = new UsuarioRepositoryMongo(conexion.getBase());
                productos = new ProductoRepositoryMongo(conexion.getBase());
                pedidos = new PedidoRepositoryMongo(conexion.getBase());
                resenas = new ResenaRepositoryMongo(conexion.getBase());
                imagenes = new ImagenRepositoryMongo(conexion.getBase());
                // El cliente de Mongo abre hilos propios: si no se cierra, la
                // aplicación no termina al cerrar la última ventana.
                Runtime.getRuntime().addShutdownHook(new Thread(conexion::cerrar));
            }
            IComponentesFactory fabrica = new ComponentesSwingFactory(paleta);
            // La vista no conoce el repositorio de imágenes ni la red: se le da
            // solo la función que, dada una referencia remota (img:<id> o una
            // URL web), devuelve los bytes.
            DescargadorImagenes descargador = new DescargadorImagenes();
            fabrica.usarFuenteDeImagenes(referencia -> DescargadorImagenes.esUrl(referencia)
                    ? descargador.descargar(referencia) : imagenes.leer(referencia));
            INotificadorCorreo notificador = elegirNotificador(configuracion);

            // Sujeto del patrón Observer: uno solo para toda la ejecución. Le
            // avisa el Proveedor de este programa y, si hay nube, también el
            // vigilante de MongoDB, que trae los cambios hechos desde otros
            // equipos. Sin conexión quedan solo los avisos locales.
            CatalogoSubject catalogo = new CatalogoSubject();
            IAutenticadorExterno google = elegirAccesoGoogle(configuracion);
            if (conexion != null) {
                new VigilanteCatalogoMongo(conexion.getBase(), catalogo).iniciar();
                normalizarCatalogoEnSegundoPlano(productos, usuarios, imagenes, catalogo);
            }

            Infraestructura infra = new Infraestructura(usuarios, productos, pedidos, resenas,
                    imagenes, notificador, catalogo, google, fabrica);

            // La pantalla de bienvenida se muestra solo aquí, en el arranque
            // en frío: al cerrar sesión la aplicación ya está corriendo y
            // repetirla sería una espera sin motivo.
            //
            // Arranque inteligente: si hay una sesión recordada y sigue
            // valiendo, se salta el Login y se abre directamente la tienda; si
            // no, el Login de siempre. Quien decide si "sigue valiendo" es
            // SesionController, contrastando con el repositorio.
            SesionController sesion = new SesionController(infra);
            new VentanaSplash(fabrica).mostrar(() -> {
                sesion.reanudar(() -> mostrarVentanaPrincipal(infra));
                if (motivoSinAtlas != null) {
                    // Después de abrir la ventana, para que el aviso quede encima.
                    EventQueue.invokeLater(() -> fabrica.mostrarDialogoAviso(null,
                            "No se pudo conectar con MongoDB Atlas.\n"
                            + "Se usan datos temporales: lo de esta sesión no se guardará.\n"
                            + "Motivo: " + motivoSinAtlas));
                }
            });
        });
    }

    /**
     * Pone al día, una vez y sin bloquear el arranque, los productos guardados
     * con reglas anteriores: rutas de imagen del disco y productos sin marca.
     * Ver {@link NormalizacionCatalogo}.
     *
     * <p>En su propio hilo porque recorre todo el catálogo de Atlas; si
     * corrigió algo, avisa al catálogo para que la tienda abierta se refresque.
     * Solo con MongoDB: el almacén en memoria nace ya normalizado.</p>
     */
    private static void normalizarCatalogoEnSegundoPlano(IProductoRepository productos,
            IUsuarioRepository usuarios, IImagenRepository imagenes, CatalogoSubject catalogo) {
        Thread hilo = new Thread(() -> {
            try {
                int corregidos = new NormalizacionCatalogo(productos, usuarios,
                        new ImportadorImagen(imagenes)).ejecutar();
                if (corregidos > 0) {
                    System.out.println("Catálogo normalizado: " + corregidos
                            + " producto(s) sin rutas locales ni marca vacía.");
                    catalogo.notificarObservadores();
                }
            } catch (RuntimeException ex) {
                // Un catálogo sin normalizar sigue funcionando: se reintenta
                // en el próximo arranque.
                System.out.println("No se pudo normalizar el catálogo: " + ex.getMessage());
            }
        }, "normalizacion-catalogo");
        hilo.setDaemon(true);
        hilo.start();
    }

    /**
     * Construye y muestra una ventana principal (sidebar + Login/Registro)
     * completamente ensamblada: crea {@link MainFrame}, sus dos cartas y sus
     * dos controladores, y la deja visible mostrando el Login.
     *
     * <p>Se usa en el arranque y al cerrar sesión, para que los dos caminos
     * abran exactamente la misma ventana, sin duplicar el ensamblado.</p>
     *
     * @param infra piezas compartidas de la ejecución; nunca se crean de nuevo
     *              aquí (se perderían los usuarios registrados en memoria y los
     *              avisos del vigilante de MongoDB)
     */
    public static void mostrarVentanaPrincipal(Infraestructura infra) {
        IComponentesFactory fabrica = infra.fabrica();
        MainFrame mainFrame = new MainFrame(fabrica);
        PanelLogin panelLogin = new PanelLogin(fabrica, mainFrame);
        PanelRegistroUsuario panelRegistro = new PanelRegistroUsuario(fabrica, mainFrame);

        mainFrame.agregarCarta(PanelLogin.NOMBRE_CARTA, panelLogin);
        mainFrame.agregarCarta(PanelRegistroUsuario.NOMBRE_CARTA, panelRegistro);
        // El conmutador de tema del sidebar: la fábrica recolorea la ventana
        // en el sitio (sin cerrarla, conservando lo escrito) y aquí solo se
        // recuerda la elección para el próximo arranque.
        mainFrame.alCambiarTema(() -> {
            fabrica.alternarTema();
            PreferenciasUsuario.guardarTemaOscuro(fabrica.esTemaOscuro());
        });

        // Un solo caso de uso de cuentas para las tres pantallas que crean o
        // editan cuentas: registro, perfil y acceso con Google.
        CuentaService cuentas = new CuentaService(infra.usuarios(), infra.notificador());

        // Una instancia de autenticación por pantalla de acceso: el contador
        // de intentos fallidos es suyo, y empieza de cero en cada Login.
        AutenticacionService autenticacion = new AutenticacionService(infra.usuarios());

        new UsuarioController(panelRegistro, cuentas, mainFrame);
        SesionController sesion = new SesionController(infra);
        new LoginController(panelLogin, autenticacion, sesion);
        if (infra.google() != null) {
            new AccesoGoogleController(panelLogin, autenticacion, cuentas, sesion, infra.google());
        }

        mainFrame.mostrarCarta(PanelLogin.NOMBRE_CARTA);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    /**
     * Abre la conexión con MongoDB Atlas, si está configurada y responde.
     *
     * <p><b>Que no haya nube no es un error.</b> Devuelve {@code null} y el
     * arranque sigue con el almacén en memoria de siempre: la aplicación tiene
     * que poder abrirse, mostrarse y defenderse sin conexión. Este es el único
     * punto que decide entre una implementación y otra, igual que con el
     * notificador — el resto del programa solo conoce las interfaces.</p>
     *
     * <p>La comprobación se hace aquí, en el arranque, y no al primer guardado:
     * el driver conecta de forma perezosa, así que sin un {@code ping} previo
     * el primer fallo aparecería en mitad de un registro.</p>
     *
     * @return la conexión lista, o {@code null} si hay que usar memoria
     */
    private static MongoDBConnection abrirConexion(Configuracion configuracion) {
        try {
            MongoDBConnection conexion = new MongoDBConnection(configuracion);
            if (conexion.disponible()) {
                System.out.println("Conectado a MongoDB Atlas.");
                return conexion;
            }
            conexion.cerrar();
            System.out.println("MongoDB Atlas no responde; se usa el almacén en memoria.");
            avisarSiConfigurado(configuracion, "no responde");
        } catch (RuntimeException ex) {
            System.out.println("Sin MongoDB (" + ex.getMessage()
                    + "); se usa el almacén en memoria.");
            avisarSiConfigurado(configuracion, ex.getMessage());
        }
        return null;
    }

    /**
     * Sin archivo de configuración, trabajar en memoria es lo esperado y no se
     * avisa. Con una URI configurada que falla, sí: el {@code .exe} no tiene
     * consola, y en silencio el usuario creía estar en Atlas mientras sus
     * cuentas iban a una memoria vacía (por eso Google pedía el rol de nuevo).
     */
    private static void avisarSiConfigurado(Configuracion configuracion, String motivo) {
        if (configuracion.tiene(MongoDBConnection.CLAVE_URI)) {
            motivoSinAtlas = motivo;
        }
    }

    /**
     * Elige cómo se envían los correos: por Resend si hay una clave válida en
     * {@code config.properties}, y si no, dejando constancia local del correo
     * que se habría enviado.
     *
     * <p><b>La clave se reconoce por su forma</b>: las de Resend empiezan por
     * {@code re_}. Antes solo se descartaban la vacía y el marcador de la
     * plantilla ({@code re_TU_...}), así que cualquier otro marcador (por
     * ejemplo {@code [PEGA_AQUI_TU_CLAVE_DE_RESEND]}) se tomaba por clave y
     * todos los correos fallaban contra la API en vez de caer a la consola.</p>
     *
     * <p>La clave sale del mismo archivo que la cadena de Atlas y **nunca**
     * del código fuente. Ese archivo está excluido del control de versiones;
     * una credencial escrita en un {@code .java} viaja en cualquier copia del
     * proyecto. Este es el único punto que decide la implementación concreta,
     * igual que con los repositorios.</p>
     *
     * @param configuracion propiedades ya leídas
     * @return el notificador a usar en esta ejecución
     */
    private static INotificadorCorreo elegirNotificador(Configuracion configuracion) {
        String clave = configuracion.valor("resend.api.key", "");
        boolean claveValida = clave.startsWith("re_") && !clave.startsWith("re_TU_");
        if (!claveValida) {
            System.out.println("Sin clave de Resend válida en " + Configuracion.ARCHIVO
                    + ": los correos se imprimirán por consola.");
        }
        String remitente = configuracion.valor("resend.remitente",
                "Comercio Electronico <onboarding@resend.dev>");
        // Sin dirección de alertas propia, las ventas se avisan al propio
        // remitente: es la dirección de la tienda.
        String alertas = configuracion.valor("resend.alertas", remitente);
        INotificadorCorreo real = claveValida
                ? new NotificadorResend(clave, remitente, alertas)
                : new NotificadorRegistroLocal();
        // Decorador: los controladores piden el correo y siguen; el envío
        // ocurre en su propio hilo. Así una API de correo lenta no alarga la
        // espera de una compra o de un registro que ya se guardaron.
        return new NotificadorEnSegundoPlano(real);
    }

    /**
     * Decide si se ofrece "Continuar con Google".
     *
     * <p>Solo si {@code config.properties} trae un ID de cliente de Google
     * rellenado. Igual que con Atlas y Resend, las credenciales nunca van en el
     * código, y que falten no es un error: la aplicación arranca sin el botón.</p>
     *
     * @param configuracion propiedades ya leídas
     * @return el autenticador, o {@code null} si no está configurado
     */
    private static IAutenticadorExterno elegirAccesoGoogle(Configuracion configuracion) {
        String idCliente = configuracion.valor("google.client.id", "");
        if (idCliente.isEmpty() || idCliente.startsWith("TU_")) {
            return null;
        }
        String secreto = configuracion.valor("google.client.secret", "");
        if (secreto.isEmpty() || secreto.startsWith("TU_")) {
            // El botón se muestra igual (el ID ya está), pero se avisa desde el
            // arranque: si no, el fallo solo aparecería tras pasar por Google.
            System.out.println("Falta google.client.secret en " + Configuracion.ARCHIVO
                    + ": el acceso con Google fallará hasta que lo añadas.");
        }
        return new GoogleAuthService(idCliente, secreto);
    }

    /**
     * Instala FlatLaf como Look and Feel base, en la variante que corresponde
     * al tema (clara u oscura): las barras de desplazamiento, los menús y la
     * barra de título de los diálogos tienen que acompañar a la paleta.
     *
     * <p><b>Por qué FlatLaf y no Nimbus.</b> Nimbus ignora {@code setBackground}
     * en varios controles y los pinta con sus propios <i>painters</i> claros;
     * media fábrica existe para sortearlo a base de delegados {@code Basic*UI}.
     * FlatLaf respeta los colores que se le piden, dibuja mejor en pantallas
     * con escalado y trae un tema oscuro coherente de fábrica.</p>
     *
     * <p><b>Esto no reemplaza a la fábrica.</b> Los colores siguen saliendo de
     * la {@link Paleta} a través de {@link view.factory.ComponentesSwingFactory}:
     * el L&amp;F pone la base (barras de desplazamiento, cursores, sombras,
     * tipografía) y la fábrica pone la identidad. Si FlatLaf no estuviera
     * disponible se cae a Nimbus, y la aplicación sigue viéndose con sus
     * colores.</p>
     *
     * @param oscuro {@code true} para la variante oscura de FlatLaf
     */
    private static void aplicarLookAndFeel(boolean oscuro) {
        if (oscuro ? FlatDarkLaf.setup() : FlatLightLaf.setup()) {
            return;
        }
        System.err.println("No se pudo aplicar FlatLaf; se intenta con Nimbus.");
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            System.err.println("Tampoco se pudo aplicar Nimbus, se usa el Look and Feel por defecto.");
        }
    }
}
