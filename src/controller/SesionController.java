package controller;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import app.Infraestructura;
import app.Main;
import aplicacion.catalogo.CatalogoService;
import aplicacion.catalogo.ImportadorImagen;
import aplicacion.catalogo.ReporteVentasService;
import aplicacion.compra.CompraService;
import aplicacion.cuenta.CuentaService;
import aplicacion.resena.ResenaService;
import model.entity.Carrito;
import model.entity.Usuario;
import service.sesion.PreferenciasUsuario;
import service.sesion.SessionManager;
import view.auth.DialogoPerfil;
import view.dashboard.AccionesSesion;
import view.dashboard.cliente.ClientDashboardFrame;
import view.dashboard.proveedor.ProviderDashboardFrame;

/**
 * Controlador de la sesión abierta: abre la tienda, lleva al proveedor a su
 * panel y de vuelta, la recuerda en disco, la reanuda al arrancar, cambia el
 * tema y la cierra.
 *
 * <p><b>Por qué se separó de {@link LoginController}.</b> Abrir la sesión de un
 * usuario ya autenticado ocurre por dos caminos: tras escribir la contraseña y
 * al arrancar con una sesión recordada. En el segundo no existe ninguna
 * pantalla de Login, y {@code LoginController} no puede construirse sin una.
 * Así cada clase tiene un solo motivo para cambiar: {@code LoginController}
 * comprueba credenciales; esta, qué pasa con una sesión ya válida.</p>
 *
 * <p><b>Doble rol.</b> Todo usuario entra a la tienda, porque todo usuario
 * compra. Quien además vende (el Proveedor) recibe en su menú "Gestionar
 * tienda", que lo lleva a su panel; desde el panel, "Ir a la tienda" lo trae
 * de vuelta. Las dos ventanas no conviven: se abre una al cerrar la otra, igual
 * que en el resto de la aplicación. El carrito y el usuario son de la sesión y
 * no de la ventana, así que sobreviven a ese ir y venir. El cambio de tema ni
 * siquiera cierra la ventana: la fábrica la recolorea en el sitio.</p>
 *
 * <p><b>Hereda la excepción documentada de {@code LoginController}.</b> Es el
 * punto que decide qué ventana abrir, así que instancia
 * {@link ClientDashboardFrame}/{@link ProviderDashboardFrame} directamente y
 * conoce {@code app.Main.mostrarVentanaPrincipal(...)} para volver al Login
 * al cerrar sesión. Es la misma excepción, no una nueva.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 2.0
 */
public class SesionController {

    private final Infraestructura infra;

    /** Usuario con la sesión abierta; {@code null} antes de abrirla. */
    private Usuario usuario;
    /** Carrito de la sesión: no se pierde al ir al panel ni al cambiar de tema. */
    private Carrito carrito;
    /** La ventana abierta: la tienda o el panel, nunca las dos. */
    private ClientDashboardFrame tienda;
    private ProviderDashboardFrame panel;
    /** El pop-up promocional se sortea una vez por sesión, al entrar. */
    private boolean promocionYaSorteada;

    /**
     * @param infra piezas compartidas de la ejecución; nunca se crean de nuevo
     *              (ver {@link Infraestructura})
     */
    public SesionController(Infraestructura infra) {
        this.infra = infra;
    }

    /**
     * Intenta reanudar la sesión recordada; si no la hay o ya no vale,
     * ejecuta {@code siNoHaySesion} (normalmente, mostrar el Login).
     *
     * <p><b>El archivo no se cree a ciegas.</b> La cuenta se vuelve a buscar
     * en el repositorio y el rol se toma de ella, no del archivo. Si la cuenta
     * ya no existe (por ejemplo, con el almacén en memoria, que se vacía en
     * cada ejecución) o el rol no coincide con el guardado (el archivo se
     * editó a mano), la sesión se descarta y se pide la contraseña.</p>
     *
     * @param siNoHaySesion qué hacer cuando no se puede reanudar
     */
    public void reanudar(Runnable siNoHaySesion) {
        SessionManager.SesionGuardada guardada = SessionManager.getSession();
        if (guardada == null) {
            siNoHaySesion.run();
            return;
        }

        Usuario encontrado = infra.usuarios().buscarPorCorreo(guardada.correo());
        if (encontrado == null || !encontrado.getTipoCuenta().equals(guardada.rol())) {
            SessionManager.clearSession();
            siNoHaySesion.run();
            return;
        }
        abrir(encontrado);
    }

    /**
     * Recuerda la sesión y abre la tienda, maximizada.
     *
     * <p>Todos entran a la tienda: el Proveedor también compra. La diferencia
     * de rol no se pregunta con {@code instanceof}: la cuenta dice si
     * {@link Usuario#puedeVender()}, y solo entonces la tienda recibe la acción
     * "Gestionar tienda".</p>
     *
     * @param autenticado usuario ya autenticado o ya reanudado
     */
    public void abrir(Usuario autenticado) {
        SessionManager.saveSession(autenticado.getCorreo(), autenticado.getTipoCuenta());
        this.usuario = autenticado;
        this.carrito = new Carrito();
        this.promocionYaSorteada = false;
        abrirTienda();
    }

    // ---------------------------------------------------------------------
    // Las dos ventanas de la sesión
    // ---------------------------------------------------------------------

    /** Abre la tienda (catálogo, carrito y compra) con su controlador. */
    private void abrirTienda() {
        AccionesSesion acciones = new AccionesSesion(this::cerrarSesion,
                () -> editarPerfil(null), this::cambiarTema,
                usuario.puedeVender() ? this::gestionarTienda : null);
        tienda = new ClientDashboardFrame(infra.fabrica(), usuario.getNombres(), acciones);
        // El controlador recibe los casos de uso ya montados: él solo
        // coordina la pantalla, y las reglas de negocio viven en los servicios.
        new ClienteController(tienda, infra.productos(),
                new CompraService(infra.productos(), infra.pedidos(), infra.notificador()),
                new ResenaService(infra.resenas(), infra.pedidos(), infra.productos()),
                cuentas(), usuario, carrito).iniciar(!promocionYaSorteada);
        promocionYaSorteada = true;
        observarCatalogo(tienda);
        mostrar(tienda);
    }

    /** Abre el panel del proveedor (gestión del catálogo e indicadores). */
    private void abrirPanelProveedor() {
        AccionesSesion acciones = new AccionesSesion(this::cerrarSesion,
                () -> editarPerfil(null), this::cambiarTema, this::volverATienda);
        panel = new ProviderDashboardFrame(infra.fabrica(), usuario.getNombres(), acciones);
        new ProveedorController(panel, catalogo(),
                new ReporteVentasService(infra.productos(), infra.pedidos()), usuario).iniciar();
        mostrar(panel);
    }

    /**
     * "Gestionar tienda": lleva al proveedor de la tienda a su panel.
     *
     * <p>Sin nombre de empresa y NIT no hay tienda que gestionar —los
     * productos se publican con esa marca—, así que primero se piden en el
     * perfil, con el motivo a la vista. Pasa sobre todo con las cuentas de
     * Google, que se crean sin esos datos.</p>
     */
    private void gestionarTienda() {
        String falta = cuentas().faltanteParaVender(usuario);
        if (falta != null) {
            editarPerfil(falta);
            if (cuentas().faltanteParaVender(usuario) != null) {
                return;
            }
        }
        cerrarVentanaActual();
        abrirPanelProveedor();
    }

    /** "Ir a la tienda": del panel del proveedor de vuelta al catálogo. */
    private void volverATienda() {
        cerrarVentanaActual();
        abrirTienda();
    }

    /**
     * Alterna claro/oscuro en caliente y lo recuerda para el próximo arranque.
     *
     * <p>No cierra ni reabre la ventana: la fábrica recolorea las ventanas
     * abiertas en el sitio (ver {@code IComponentesFactory.alternarTema}), así
     * que el catálogo, lo escrito en el buscador y el carrito siguen tal cual.</p>
     */
    private void cambiarTema() {
        infra.fabrica().alternarTema();
        PreferenciasUsuario.guardarTemaOscuro(infra.fabrica().esTemaOscuro());
    }

    /**
     * Cierra la ventana abierta a través de su contrato (nunca con un
     * {@code dispose()} del controlador sobre un {@code JFrame}).
     */
    private void cerrarVentanaActual() {
        if (tienda != null) {
            tienda.cerrarVentana();
            tienda = null;
        }
        if (panel != null) {
            panel.cerrarVentana();
            panel = null;
        }
    }

    /**
     * Suscribe la tienda a los cambios del catálogo y la da de baja cuando se
     * cierra.
     *
     * <p>La suscripción y la baja van juntas en el mismo sitio a propósito:
     * quien suscribe es quien sabe cuándo deja de tener sentido. Si la ventana
     * se cerrara sin darse de baja, el sujeto la mantendría viva en memoria y
     * le seguiría avisando. {@code windowClosed} llega tras el
     * {@code dispose()} de "Cerrar sesión" o del paso al
     * panel; con la X el programa termina y no queda nadie a quien avisar.</p>
     */
    private void observarCatalogo(ClientDashboardFrame ventana) {
        infra.catalogo().agregarObservador(ventana);
        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                infra.catalogo().removerObservador(ventana);
            }
        });
    }

    /**
     * Olvida la sesión y vuelve al Login.
     *
     * <p>La ventana ya se destruyó ella misma ({@code dispose()}) antes de
     * llamar aquí. Se reutiliza la infraestructura de siempre: crear otra
     * perdería las cuentas registradas en esta ejecución.</p>
     */
    private void cerrarSesion() {
        SessionManager.clearSession();
        tienda = null;
        panel = null;
        usuario = null;
        carrito = null;
        Main.mostrarVentanaPrincipal(infra);
    }

    /**
     * Abre la edición de perfil del usuario con la sesión abierta.
     *
     * <p>El diálogo es modal: {@code abrir()} no vuelve hasta que se cierra.
     * Al volver se reescribe la sesión, porque el correo pudo cambiar; y si el
     * usuario vende y cambió su correo o el nombre de su empresa, se lleva a
     * todos sus productos (la marca está guardada en cada uno).</p>
     *
     * @param aviso motivo concreto para abrirlo (por ejemplo, falta la
     *              empresa), o {@code null} para el aviso general del perfil
     */
    private void editarPerfil(String aviso) {
        JFrame padre = tienda != null ? tienda : panel;
        String correoAnterior = usuario.getCorreo();
        String empresaAnterior = usuario.getNombreEmpresa();

        PerfilController perfil = new PerfilController(
                new DialogoPerfil(infra.fabrica(), padre), cuentas(), usuario);
        if (aviso == null) {
            perfil.abrir();
        } else {
            perfil.abrir(aviso);
        }

        SessionManager.saveSession(usuario.getCorreo(), usuario.getTipoCuenta());
        if (usuario.puedeVender()
                && (!correoAnterior.equalsIgnoreCase(usuario.getCorreo())
                    || !empresaAnterior.equals(usuario.getNombreEmpresa()))) {
            catalogo().actualizarVendedor(correoAnterior, usuario.getCorreo(),
                    usuario.getNombreEmpresa());
        }
    }

    private CuentaService cuentas() {
        return new CuentaService(infra.usuarios(), infra.notificador());
    }

    private CatalogoService catalogo() {
        return new CatalogoService(infra.productos(), infra.catalogo(),
                new ImportadorImagen(infra.imagenes()));
    }

    private void mostrar(JFrame ventana) {
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }
}
