package view.auth;

import java.awt.Component;
import java.awt.Font;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;
import view.factory.IComponentesFactory;
import view.factory.components.CampoPasswordConToggle;
import view.factory.components.CampoTextoConIcono;
import view.factory.icons.IconoCampo;

/**
 * Ventana de edición del perfil: datos de la cuenta, envío, tienda (si la
 * cuenta vende) y cambio de contraseña.
 *
 * <p><b>Responsabilidad única:</b> pinta y recoge. No valida el correo, no
 * comprueba la contraseña actual y no sabe guardar nada; entrega lo escrito en
 * un {@link CambiosPerfil} y {@link controller.PerfilController} decide. Por
 * eso no importa nada de {@code model}.</p>
 *
 * <p><b>Es un modal que no se cierra al fallar.</b> Se arma sobre
 * {@code crearDialogoModal(...)} y no sobre el diálogo de confirmación de la
 * fábrica, porque ese devuelve un sí o un no y se cierra en cualquier caso:
 * aquí, si el correo está repetido o la contraseña no cumple, el formulario
 * tiene que seguir abierto con lo ya escrito.</p>
 *
 * <p><b>Dos columnas.</b> Con la cédula, la dirección y los datos de la tienda,
 * una sola columna pasaba de 800 px de alto y no cabía en una pantalla de
 * portátil. A la izquierda va lo que identifica y entrega (cuenta y envío); a
 * la derecha, la tienda y la contraseña. Cambiar el teléfono y cambiar la
 * clave siguen en secciones separadas: son gestos de riesgo distinto.</p>
 *
 * <p>Si al perfil le falta algo para poder comprar (o vender), arriba aparece
 * un aviso; es la "alerta visual en el perfil" de las cuentas de Google.</p>
 *
 * @author Ingeniería de Sistemas - Tercer Incremento Funcional
 * @version 2.0
 */
public class DialogoPerfil implements IPerfilView {

    private static final int ANCHO_CAMPO = 300;

    private final IComponentesFactory fabrica;
    private final Component padre;

    private final CampoTextoConIcono txtNombres;
    private final CampoTextoConIcono txtCorreo;
    private final CampoTextoConIcono txtTelefono;
    private final CampoTextoConIcono txtCedula;
    private final CampoTextoConIcono txtDireccion;
    private final CampoTextoConIcono txtEmpresa;
    private final CampoTextoConIcono txtNit;
    private final CampoPasswordConToggle txtPasswordActual;
    private final CampoPasswordConToggle txtPasswordNueva;
    private final CampoPasswordConToggle txtPasswordConfirmar;
    private final JLabel lblAviso;
    private final JLabel lblAlerta;

    private boolean esVendedor;
    private JDialog ventana;
    private Consumer<CambiosPerfil> accionGuardar = cambios -> { };

    /**
     * @param fabrica fábrica de la que salen todos los componentes
     * @param padre   componente sobre el que se centra el modal
     */
    public DialogoPerfil(IComponentesFactory fabrica, Component padre) {
        this.fabrica = fabrica;
        this.padre = padre;

        txtNombres = fabrica.crearCampoTexto("Tu nombre completo", IconoCampo.Tipo.USUARIO);
        txtCorreo = fabrica.crearCampoTexto("correo@empresa.com", IconoCampo.Tipo.CORREO);
        txtTelefono = fabrica.crearCampoTexto("Ej: 3001234567", IconoCampo.Tipo.IDENTIFICACION);
        txtCedula = fabrica.crearCampoTexto("Solo números, sin puntos", IconoCampo.Tipo.IDENTIFICACION);
        txtDireccion = fabrica.crearCampoTexto("Ej: Calle 10 #5-20, Bogotá", IconoCampo.Tipo.UBICACION);
        txtEmpresa = fabrica.crearCampoTexto("Ej: Supertecno", IconoCampo.Tipo.ETIQUETA);
        txtNit = fabrica.crearCampoTexto("Ej: 900123456-7", IconoCampo.Tipo.EMPRESA);
        txtPasswordActual = fabrica.crearCampoPassword("Tu contraseña actual");
        txtPasswordNueva = fabrica.crearCampoPassword("Mínimo 7 caracteres");
        txtPasswordConfirmar = fabrica.crearCampoPassword("Repite la nueva contraseña");
        lblAviso = fabrica.crearAlerta();
        lblAlerta = fabrica.crearAlerta();
    }

    // ---------------------------------------------------------------------
    // Construcción
    // ---------------------------------------------------------------------

    /** Arma el contenido completo del modal. */
    private JPanel construirContenido() {
        JPanel contenido = new JPanel(new MigLayout(
                "wrap 2, insets 22 26 18 26, gapx 28, gapy 0",
                "[" + ANCHO_CAMPO + "!][" + ANCHO_CAMPO + "!]"));

        JLabel titulo = new JLabel("Editar perfil");
        titulo.setFont(fabrica.fuente(Font.BOLD, 20));
        titulo.setForeground(fabrica.colorTexto());
        JLabel subtitulo = new JLabel(esVendedor
                ? "Tus datos, los de tu tienda y tu contraseña"
                : "Actualiza tus datos de contacto y tu contraseña");
        subtitulo.setFont(fabrica.fuente(Font.PLAIN, 12));
        subtitulo.setForeground(fabrica.colorTextoSuave());

        contenido.add(titulo, "span 2");
        contenido.add(subtitulo, "span 2, gaptop 4, gapbottom 12");
        contenido.add(lblAviso, "span 2, growx, gapbottom 12, hidemode 3");

        contenido.add(construirColumnaIzquierda(), "top");
        contenido.add(construirColumnaDerecha(), "top");

        contenido.add(lblAlerta, "span 2, growx, gaptop 6, hidemode 3");
        contenido.add(construirBotones(), "span 2, growx, gaptop 14");
        return contenido;
    }

    /** Cuenta y envío: lo que identifica a la persona y dónde recibe. */
    private JPanel construirColumnaIzquierda() {
        JPanel columna = columna();
        columna.add(encabezadoSeccion("DATOS DE LA CUENTA"), "gapbottom 10");
        agregarFila(columna, "Nombre completo", txtNombres);
        agregarFila(columna, "Correo electrónico", txtCorreo);
        agregarFila(columna, "Teléfono", txtTelefono);
        agregarFila(columna, "Cédula", txtCedula);
        columna.add(encabezadoSeccion("ENVÍO DE TUS COMPRAS"), "gaptop 6, gapbottom 10");
        agregarFila(columna, "Dirección de envío", txtDireccion);
        return columna;
    }

    /** Tienda (si la cuenta vende) y contraseña. */
    private JPanel construirColumnaDerecha() {
        JPanel columna = columna();
        if (esVendedor) {
            columna.add(encabezadoSeccion("TU TIENDA"), "gapbottom 10");
            agregarFila(columna, "Empresa o marca", txtEmpresa);
            agregarFila(columna, "NIT de la empresa", txtNit);
        }
        columna.add(encabezadoSeccion("CAMBIAR CONTRASEÑA"),
                (esVendedor ? "gaptop 6, " : "") + "gapbottom 6");
        JLabel ayuda = new JLabel("Déjalo vacío si no quieres cambiarla.");
        ayuda.setFont(fabrica.fuente(Font.PLAIN, 11));
        ayuda.setForeground(fabrica.colorTextoSuave());
        columna.add(ayuda, "gapbottom 8");
        agregarFila(columna, "Contraseña actual", txtPasswordActual);
        agregarFila(columna, "Nueva contraseña", txtPasswordNueva);
        agregarFila(columna, "Repite la nueva", txtPasswordConfirmar);
        return columna;
    }

    private JPanel columna() {
        JPanel columna = new JPanel(new MigLayout("wrap 1, insets 0, gapy 0",
                "[" + ANCHO_CAMPO + "!]"));
        columna.setOpaque(false);
        return columna;
    }

    /**
     * Rótulo de sección: texto corto en acento con una línea debajo.
     *
     * <p>La línea es un {@code MatteBorder} estándar, no un trazo pintado a
     * mano.</p>
     */
    private JLabel encabezadoSeccion(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fabrica.fuente(Font.BOLD, 11));
        etiqueta.setForeground(fabrica.colorAcento());
        etiqueta.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, fabrica.colorBorde()),
                new EmptyBorder(0, 0, 6, 0)));
        return etiqueta;
    }

    private JPanel construirBotones() {
        JButton btnGuardar = fabrica.crearBotonPrimario("GUARDAR CAMBIOS");
        btnGuardar.addActionListener(e -> accionGuardar.accept(capturar()));

        JButton btnCancelar = fabrica.crearBotonSegmento("Cancelar");
        btnCancelar.addActionListener(e -> cerrar());

        JPanel botones = new JPanel(new MigLayout("insets 0, gapx 10", "[grow,fill][]"));
        botones.setOpaque(false);
        botones.add(btnGuardar);
        botones.add(btnCancelar);
        return botones;
    }

    private void agregarFila(JPanel columna, String etiqueta, Component campo) {
        columna.add(fabrica.crearEtiqueta(etiqueta), "gapbottom 4");
        columna.add(campo, "growx, gapbottom 10");
    }

    /** @return lo escrito ahora mismo, sin limpiar ni validar */
    private CambiosPerfil capturar() {
        return new CambiosPerfil(
                txtNombres.getTexto(),
                txtCorreo.getTexto(),
                txtTelefono.getTexto(),
                txtCedula.getTexto(),
                txtDireccion.getTexto(),
                txtNit.getTexto(),
                txtEmpresa.getTexto(),
                txtPasswordActual.getPassword(),
                txtPasswordNueva.getPassword(),
                txtPasswordConfirmar.getPassword());
    }

    // ---------------------------------------------------------------------
    // IPerfilView
    // ---------------------------------------------------------------------

    @Override
    public void mostrarDatos(DatosPerfil datos) {
        esVendedor = datos.esVendedor();
        txtNombres.setTexto(datos.nombres());
        txtCorreo.setTexto(datos.correo());
        txtTelefono.setTexto(datos.telefono());
        txtCedula.setTexto(datos.cedula());
        // Una cédula ya dada no se cambia desde aquí: se muestra bloqueada
        // (setEnabled del campo compuesto bloquea la escritura sin el fondo
        // claro de un campo deshabilitado; ver CampoTextoConIcono).
        txtCedula.setEnabled(datos.cedulaEditable());
        txtCedula.setToolTipText(datos.cedulaEditable() ? null : "La cédula no se puede cambiar");
        txtDireccion.setTexto(datos.direccionEnvio());
        txtEmpresa.setTexto(datos.nombreEmpresa());
        txtNit.setTexto(datos.nit());
        // El aviso es eso, un aviso: el usuario no se equivocó, le falta algo.
        fabrica.pintarAlerta(lblAviso, datos.aviso(), false);
    }

    @Override
    public void mostrarError(String mensaje) {
        fabrica.pintarAlerta(lblAlerta, mensaje, true);
        if (ventana != null) {
            ventana.pack();
        }
    }

    @Override
    public void mostrarExito(String mensaje) {
        fabrica.mostrarDialogoExito(ventana == null ? padre : ventana, mensaje);
    }

    @Override
    public void cerrar() {
        if (ventana != null) {
            // Ciclo de vida estricto: se destruye, no se esconde.
            ventana.dispose();
            ventana = null;
        }
    }

    @Override
    public void abrir() {
        ventana = fabrica.crearDialogoModal(padre, "Editar perfil", construirContenido());
        // El modal bloquea aquí hasta que alguien llame a cerrar().
        ventana.setVisible(true);
    }

    @Override
    public void alGuardar(Consumer<CambiosPerfil> accion) {
        this.accionGuardar = accion;
    }
}
