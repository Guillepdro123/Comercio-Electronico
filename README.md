# Plataforma de Comercio Electrónico

Aplicación de escritorio en Java Swing con arquitectura MVC, desarrollada por incrementos funcionales.

**Estado actual (04/10/2026):** entrega final, funcional y empaquetada como `.exe`. Todo usuario compra (catálogo con búsqueda y filtros, carrito, compra atómica, correos de confirmación, reseñas con estrellas) y el Proveedor además vende (CRUD de su catálogo con su marca e indicadores de venta). Persistencia en MongoDB Atlas, con respaldo en memoria si no hay red. **La documentación técnica completa está en [`docs/`](docs/README.md); si llegas nuevo al proyecto, empieza por la [guía de traspaso](docs/00_Inicio/guia_de_traspaso.md).**

**Incremento 2 — iteración y refinamiento de la interfaz sobre el registro de usuarios del Incremento 1: `MainFrame` amplio con panel lateral (sidebar) de navegación, paleta oscura morada estilo SaaS/Tech con acento violeta, `CardLayout` para Login/Registro, autenticación con redirección por rol, confirmaciones con `JOptionPane` y un ciclo de vida estricto de ventanas (`dispose()` al pasar de una ventana a otra, nunca ventanas ocultas y vivas). La operación de negocio sigue siendo la misma (CREAR usuarios); este incremento no agrega un caso de uso nuevo, refina cómo se presenta y se navega el que ya existía.

---

## Descripción

El sistema registra los dos actores de la plataforma: **Clientes**, que compran y aportan una dirección de envío, y **Proveedores**, que abastecen el catálogo y aportan el NIT de su empresa. Ambos comparten identificación, nombres, correo y contraseña, por lo que se modelan como subclases de una clase abstracta común.

La persistencia es MongoDB Atlas, detrás de interfaces de repositorio: sin `config.properties` (o sin red) la aplicación usa repositorios en memoria sin modificar el resto del sistema.

---

## Requisitos

- **JDK 26** (`javac.source/target=26`). Apache NetBeans reciente ya lo trae
  en `C:\Program Files\Apache NetBeans\jdk`; un JDK anterior no abre las clases.
- Apache NetBeans (proyecto *Java with Ant → Java Application*)

**Dependencias (ocho JAR, ya versionados en `lib/`):**

| Librería | Versión | Para qué |
|---|---|---|
| [FlatLaf](https://www.formdev.com/flatlaf/) | 3.7.2 | Look and Feel base, en variante clara u oscura |
| MigLayout Swing | 11.4.2 | Layout de los formularios |
| MigLayout Core | 11.4.2 | Requerida por la anterior |
| MongoDB Driver Sync | 5.12.0 | Acceso a MongoDB Atlas |
| MongoDB Driver Core | 5.12.0 | Requerida por la anterior |
| BSON | 5.12.0 | Requerida por el driver |
| BSON Record Codec | 5.12.0 | Requerida por el driver en ejecución |
| jBCrypt | 0.4 | Cifrado de contraseñas |

`bson-record-codec` no aparece en la documentación habitual del driver, pero su
POM la declara obligatoria en tiempo de ejecución. `slf4j-api` sí figura, pero
está marcada como **opcional**: no se incluye.

**No hay `pom.xml`: esto es Ant, no Maven.** Los JAR se declaran a mano en
`nbproject/project.properties`, con un `file.reference.<nombre>` por cada uno y
su entrada en `javac.classpath`. Van versionados en el repositorio para que el
proyecto compile sin red. Para agregar otra librería: descargarla de Maven
Central, **verificar el SHA-1 que publica el repositorio**, dejar el JAR en
`lib/` y declararlo ahí.

El proyecto declara además `run.jvmargs=--enable-native-access=ALL-UNNAMED`:
FlatLaf carga una librería nativa en Windows para las decoraciones de ventana,
y desde el JDK 24 eso emite un aviso por consola si no se declara.

---

## Estructura del proyecto

```
ComercioElectronico/
├── README.md                         ← este documento
├── CLAUDE.md                         ← directivas de trabajo sobre el proyecto
├── .claude/skills/java-swing-ui/     ← reglas de la capa visual (stack, pintado, verificación)
├── lib/                              ← FlatLaf y MigLayout (ver Requisitos)
├── build.xml                         ← generado por NetBeans
├── nbproject/                        ← configuración del IDE
└── src/
    ├── app/
    │   ├── Main.java                 ← ensamblador; ver nota de paquete más abajo
    │   └── Infraestructura.java      ← lo que se crea una vez y viaja toda la ejecución
    ├── controller/
    │   ├── UsuarioController.java    ← caso de uso "Registrar usuario"
    │   ├── LoginController.java      ← caso de uso "Iniciar sesión"
    │   ├── AccesoGoogleController.java ← acceso con Google
    │   ├── SesionController.java     ← sesión abierta: tienda ↔ panel, tema, perfil, cierre
    │   ├── ClienteController.java    ← tienda: catálogo, ficha y reseñas, carrito, checkout
    │   ├── ProveedorController.java  ← panel del Proveedor: CRUD del catálogo e indicadores
    │   └── PerfilController.java     ← caso de uso "Editar perfil"
    ├── aplicacion/                   ← casos de uso, sin Swing
    │   ├── acceso/      AutenticacionService
    │   ├── cuenta/      CuentaService (registro, perfil, empresa, cédula y dirección)
    │   ├── catalogo/    CatalogoService, ReporteVentasService, ImportadorImagen,
    │   │                NormalizacionCatalogo (rutas antiguas y marcas vacías)
    │   ├── compra/      CompraService (exige cédula y dirección antes de tocar stock)
    │   ├── resena/      ResenaService (solo opina quien compró)
    │   └── seguridad/   CifradoPassword, PoliticaPassword, ControlIntentosFallidos
    ├── model/
    │   ├── entity/
    │   │   ├── Usuario.java          ← clase abstracta (cédula, dirección, puedeVender)
    │   │   ├── Cliente.java
    │   │   ├── Proveedor.java        ← NIT, empresa y su propia dirección (doble rol)
    │   │   ├── Producto.java         ← artículo del catálogo, con la marca de quien vende
    │   │   ├── Categoria.java        ← enum de categorías
    │   │   ├── Carrito.java          ← carrito de la sesión (no se persiste)
    │   │   ├── LineaPedido.java      ← renglón con el precio del momento
    │   │   ├── Pedido.java           ← compra confirmada, con el documento del comprador
    │   │   └── Resena.java           ← opinión de 1 a 5 estrellas
    │   └── repository/
    │       ├── IUsuarioRepository.java      ← interfaz
    │       ├── IProductoRepository.java     ← interfaz
    │       ├── IPedidoRepository.java       ← interfaz
    │       ├── IResenaRepository.java       ← interfaz
    │       ├── IImagenRepository.java       ← interfaz (imágenes subidas, referencia img:<id>)
    │       ├── memoria/                     ← almacén sin red (por defecto, sin config.properties)
    │       │   ├── UsuarioRepositoryImpl.java
    │       │   ├── ProductoRepositoryMemoria.java
    │       │   ├── PedidoRepositoryMemoria.java
    │       │   ├── ResenaRepositoryMemoria.java
    │       │   └── ImagenRepositoryArchivo.java ← carpeta relativa imagenes/
    │       └── mongo/                       ← MongoDB Atlas (repositorios + adapter/),
    │                                          incluidas las colecciones resenas e imagenes
    ├── service/
    │   ├── config/      Configuracion.java          ← único lector de config.properties
    │   ├── sesion/      SessionManager, PreferenciasUsuario (tema claro/oscuro)
    │   ├── imagen/      OptimizadorImagen           ← reduce a 800 px antes de guardar
    │   ├── google/      GoogleAuthService, IAutenticadorExterno, PerfilExterno
    │   └── correo/      notificadores (Resend, local, en segundo plano), sus
    │                    contratos y las plantillas (PlantillaCorreo, MensajeBienvenida, ResumenPedido)
    ├── resources/
    │   ├── images/
    │   │   ├── logo.png                ← emblema circular de la marca (PNG con transparencia)
    │   │   ├── productos/              ← ilustraciones del catálogo (generadas, ver nota)
    │   │   └── icons/
    │   │       ├── check.png          ← ícono de éxito de los diálogos (reemplaza al del L&F)
    │   │       └── ecommerce_cart.gif ← carrito animado del sidebar (generado a medida)
    │   └── audio/
    │       └── exito.wav              ← tono de confirmación del registro
    └── view/
        ├── core/
        │   ├── MainFrame.java             ← único JFrame Login/Registro: sidebar + CardLayout, implementa INavegador
        │   ├── VentanaSplash.java         ← pantalla de bienvenida del arranque en frío
        │   └── INavegador.java            ← contrato para cambiar de carta
        ├── auth/
        │   ├── IRegistroUsuarioView.java  ← contrato que usa UsuarioController
        │   ├── ILoginView.java            ← contrato que usa LoginController
        │   ├── PanelRegistroUsuario.java  ← carta "registro"
        │   └── PanelLogin.java            ← carta "login" (por defecto al arrancar)
        ├── dashboard/
        │   ├── AccionesSesion.java          ← cerrar sesión, perfil, tema y cambiar de panel
        │   ├── cliente/
        │   │   ├── IClienteDashboardView.java ← contrato que usa ClienteController
        │   │   ├── ClientDashboardFrame.java  ← la tienda: banner, catálogo, ficha con reseñas, carrito y checkout
        │   │   ├── ResenaVista.java, ResumenResenas.java, NuevaResena.java ← reseñas para la vista
        │   │   ├── DatosEnvio.java            ← cédula y dirección del formulario rápido del checkout
        │   │   ├── TarjetaProducto.java       ← datos de un producto para la vista
        │   │   ├── LineaCarrito.java          ← renglón del carrito (precio unitario, tope de cantidad)
        │   │   ├── Promocion.java             ← oferta del banner o del pop-up
        │   │   └── ResumenCompra.java         ← compra pasada para la vista
        │   └── proveedor/
        │       ├── IProveedorDashboardView.java ← contrato que usa ProveedorController
        │       ├── ProviderDashboardFrame.java← tablero de indicadores y gestión del catálogo
        │       ├── BarraVentas.java           ← barra del gráfico para la vista
        │       ├── FilaProducto.java          ← producto en la tabla del Proveedor
        │       ├── IndicadorVentas.java       ← cifras del tablero (con ticket promedio y más vendido)
        │       └── DatosProducto.java         ← lo capturado en el formulario, sin validar
        └── factory/
            ├── IComponentesFactory.java          ← interfaz de la fábrica (la puerta de entrada)
            ├── ComponentesSwingFactory.java      ← construye cada componente con la Paleta que recibe
            ├── tema/
            │   ├── Paleta.java                   ← colores por función; Paleta.clara() y Paleta.oscura()
            │   ├── TemaDinamico.java             ← paleta vigente y un color vivo por rol
            │   └── ColorDeTema.java              ← Color que se resuelve contra la paleta al pintarse
            ├── promo/                            ← piezas promocionales
            │   ├── BannerRotativo.java           ← banner con Timer, fundido y botones superpuestos
            │   ├── Diapositiva.java              ← dato de una diapositiva
            │   └── CabeceraDegradada.java        ← franja con degradado del pop-up
            ├── charts/                           ← visualización de datos
            │   ├── GraficoBarras.java            ← barras horizontales con la paleta
            │   └── Barra.java                    ← dato de una barra
            ├── components/                       ← controles compuestos del formulario
            │   ├── BotonCarrito.java             ← ícono de carrito + contador (badge)
            │   ├── CampoTextoConIcono.java       ← campo de texto + ícono de contexto
            │   ├── CampoPasswordConToggle.java   ← ícono + campo + botón "ojo"
            │   ├── SelectorSegmentado.java       ← control segmentado (reemplaza al JComboBox)
            │   └── SelectorEstrellas.java        ← calificar de 1 a 5 (cinco botones)
            ├── effects/                          ← movimiento y sonido
            │   ├── IndicadorCarga.java           ← indicador circular (giratorio o de avance)
            │   ├── AnimacionConfeti.java         ← celebración sobre el glassPane
            │   ├── SonidoExito.java              ← reproduce exito.wav con AudioSystem/Clip
            │   └── TransicionTema.java           ← fundido del cambio de tema (foto que se desvanece)
            ├── icons/                            ← todo lo que se dibuja o se carga como ícono
            │   ├── IconoCampo.java               ← glifos de línea de cada campo
            │   ├── IconoOjo.java                 ← mostrar/ocultar contraseña
            │   ├── IconoPunto.java               ← punto del semáforo de fortaleza
            │   ├── IconoCarrito.java             ← glifo del carrito de la barra superior
            │   ├── IconoCerrar.java              ← la "X" del pop-up, dibujada (no el carácter)
            │   ├── IconoEstrellas.java           ← estrellas con media estrella, dibujadas
            │   ├── IconoTema.java                ← luna / sol del conmutador de tema
            │   ├── IconoGifAnimado.java          ← GIF animado decodificado con ImageIO
            │   └── IconoImagenEscalada.java      ← reduce una imagen cuidando la nitidez
            └── utils/                            ← piezas de apoyo reutilizables
                ├── BordeRedondeado.java          ← Border que redondea sin pintar fondos a mano
                └── PlaceholderFocusListener.java ← texto fantasma reutilizable
```

Nota de ruta: los íconos nuevos viven en `resources/images/icons/` (la
carpeta de recursos que ya existía en el proyecto), no en
`src/main/resources/img/icons/` como en un layout Maven — este proyecto es
Ant/NetBeans y no usa esa estructura.

**El carrito animado del sidebar se generó a medida, no se descargó.** Dos
razones: un GIF tomado de un banco de imágenes vendría con derechos de autor
que no corresponden a una entrega académica, y generándolo se consiguen
exactamente los hex del tema (trazo `#8B5CF6` latiendo hacia `#A78BFA`, sobre
el fondo `#1B1627` del sidebar) en vez de "algo parecido". Son 30 cuadros de
128×128 a 80 ms — 2,4 s por vuelta: un carrito de líneas que oscila
suavemente mientras un artículo cae dentro. El movimiento es deliberadamente
discreto porque es un adorno en bucle permanente, y lo que en una animación
puntual resulta simpático, repetido cada dos segundos cansa.

`view.factory` también está dividido por función, porque había acumulado
quince clases sueltas. En la raíz quedan solo las dos piezas que forman el
contrato —`IComponentesFactory` y su implementación—, y el detalle se reparte
en cuatro subpaquetes: `components` (controles compuestos), `effects`
(movimiento y sonido), `icons` (todo lo dibujado o cargado como ícono) y
`utils` (piezas de apoyo).

Qué ve cada consumidor desde fuera de la fábrica:

| Clase | Importa de `view.factory` |
|---|---|
| `app.Main`, `controller.LoginController`, `view.core.MainFrame`, ambos dashboards | solo la raíz |
| `view.core.VentanaSplash` | raíz + `effects.IndicadorCarga` |
| `view.auth.PanelLogin`, `view.auth.PanelRegistroUsuario` | raíz + `components` + `icons` |

Los dos formularios son los únicos que bajan al detalle, y no por un descuido:
declaran sus campos con el tipo que la fábrica devuelve
(`CampoTextoConIcono txtCorreo`) y eligen el glifo con `IconoCampo.Tipo`. Es
inherente a una fábrica que entrega componentes: quien recibe algo tiene que
poder nombrarlo. El resto del proyecto solo conoce la interfaz, que es lo que
importa para poder cambiar de tema sin tocar las vistas.

`view` se organiza por contexto funcional, no como un paquete plano:
`view.core` (la ventana y la navegación), `view.auth` (Login y Registro,
que comparten vocabulario e interfaces hermanas), `view.dashboard` (las
ventanas de destino por rol) y `view.factory` (fábrica de componentes,
sin cambios). Cada subpaquete importa de los otros solo lo que
efectivamente usa (por ejemplo, `view.core.MainFrame` importa
`view.auth.PanelLogin` y `view.auth.PanelRegistroUsuario` para sus
constantes `NOMBRE_CARTA`).

El `README.md` va en la raíz del proyecto, al mismo nivel de `src`, no dentro de ella. `build.xml` y `nbproject/` los genera NetBeans automáticamente; no se modifican a mano (con la única excepción de `nbproject/project.properties`, cuyo `main.class` sí se actualizó a mano a `app.Main` al mover el ensamblador de paquete — ver la nota debajo).

`Main.java` vive en el paquete `app` (no en el paquete por defecto, como en el Incremento 1) porque el botón "Cerrar sesión" de los dashboards necesita volver a invocar el mismo ensamblado completo (`app.Main.mostrarVentanaPrincipal(...)`) desde `controller.LoginController`, y Java no permite importar clases del paquete por defecto desde un paquete con nombre.

---

## Instalación en NetBeans

1. *File → Open Project* → la carpeta del proyecto (ya es un proyecto de NetBeans: no hay que crear paquetes ni copiar archivos).
2. Clic derecho sobre el proyecto → **Clean and Build**.
3. **Run** (`F6`): la clase principal es `app.Main`.
4. Para usar MongoDB Atlas: copiar `config.properties.ejemplo` como `config.properties` y rellenarlo. Sin ese archivo arranca con datos en memoria.

Si aparecen caracteres extraños, configurar la codificación del proyecto en UTF-8. El paso a paso completo, incluidas las cuentas externas, está en la [guía de traspaso](docs/00_Inicio/guia_de_traspaso.md); el ejecutable `.exe`, en la [guía del ejecutable](docs/05_Empaquetado/guia_ejecutable.md).

---

## Arquitectura

```
MainFrame (JFrame 950×650, BorderLayout, implementa INavegador)
   ├── WEST: sidebar fijo (logo + "Bienvenido" + botones "Log In"/"Register")
   └── CENTER: CardLayout
         ├── PanelLogin           (carta "login", por defecto al arrancar)
         └── PanelRegistroUsuario (carta "registro")
               ambas construidas con ──► IComponentesFactory (interfaz)
                                                   │
                                                   ▼
                                        ComponentesSwingFactory
                                   (paleta + tipografía, única implementación hoy)

ILoginView (interfaz)  ◄──eventos/lectura──  LoginController
     ▲                                            │
     │ implementa                                 ├──► IUsuarioRepository.buscarPorCorreo(correo)
PanelLogin ──cerrarVentana()──► dispose()          │            │
   (de la MainFrame actual)                        │            ▼
                                                    │  UsuarioRepositoryImpl ──► List<Usuario>
                                                    │            │
                                                    │            ▼
                                                    │     Usuario (abstracta)
                                                    │     ├── Cliente
                                                    │     └── Proveedor
                                                    │
                                                    └──► ClientDashboardFrame / ProviderDashboardFrame
                                                         (JFrame propio, maximizado; según usuario.getTipoCuenta())
                                                              │
                                                    "Cerrar sesión": dispose() de sí mismo +
                                                    app.Main.mostrarVentanaPrincipal(repositorio, fabrica)
                                                    → una MainFrame nueva, con el mismo repositorio

IRegistroUsuarioView (interfaz)  ◄──eventos/lectura──  UsuarioController
        ▲                                                    │
        │ implementa                                         ├──► IUsuarioRepository.registrar(usuario)
PanelRegistroUsuario ──JOptionPane.showMessageDialog──►       │
  (mostrarExito/mostrarError)                                 └──► navegador.mostrarCarta(PanelLogin.NOMBRE_CARTA)
                                                                    (tras cerrar el JOptionPane de éxito)
```

Reglas de dependencia respetadas:

- La vista no importa nada de `model`: desconoce que existen `Cliente` y `Proveedor`. `ClientDashboardFrame`/`ProviderDashboardFrame` no reciben un `Usuario`, solo el nombre ya extraído por `LoginController`.
- El modelo no importa nada de `javax.swing`.
- Los controladores dependen de interfaces (`IRegistroUsuarioView`/`ILoginView`, `IUsuarioRepository`, `IComponentesFactory`, `INavegador`), nunca de una implementación concreta — salvo `LoginController`, que instancia directamente `ClientDashboardFrame`/`ProviderDashboardFrame` (es, literalmente, el caso de uso que decide a cuál ir) y llama a `app.Main.mostrarVentanaPrincipal(...)` para el logout, por la misma razón.
- `UsuarioController` también depende de `INavegador`: tras cerrar el `JOptionPane` de éxito del registro, pide la carta de Login con el mismo `mostrarCarta(...)` genérico que ya usan los enlaces internos de las vistas — no se agregó un método específico a la interfaz para esto.
- Ninguna vista construye sus propios colores, fuentes o logo: todo componente Swing se obtiene de `IComponentesFactory`. El logo aparece una sola vez, en el sidebar de `MainFrame`; ni `PanelLogin` ni `PanelRegistroUsuario` lo repiten.
- `PanelLogin`/`PanelRegistroUsuario` no importan `MainFrame`: reciben `INavegador` para alternar entre ellas. Los dashboards no usan `INavegador` — son ventanas propias, no cartas del mismo `CardLayout`.
- Los controladores no conocen `JOptionPane` ni `dispose()` ni ningún detalle de Swing: `LoginController` llama a `vista.mostrarExito(mensaje)`/`vista.mostrarError(mensaje)`/`vista.cerrarVentana()`, tres métodos de `ILoginView` — nunca a un `JFrame` de Login/Registro directamente (los `JFrame` de destino sí los abre él mismo, ver el punto anterior).
- `LoginController` decide la ventana destino con `usuario.getTipoCuenta()` (polimorfismo), nunca con `instanceof Cliente`/`instanceof Proveedor`.
- **Ciclo de vida estricto:** ninguna ventana que se reemplaza queda oculta y viva (`setVisible(false)` sin `dispose()`). El Login/Registro se destruye al iniciar sesión con éxito; el dashboard se destruye al cerrar sesión.

---

## Novedades del Incremento 2 (iteración y refinamiento de la interfaz)

El caso de uso de negocio sigue siendo el registro del Incremento 1; todo lo
de esta sección es **cómo se presenta y se navega**, no una funcionalidad
nueva:

- **`MainFrame` con sidebar, 950×650.** Antes la ventana era
  `FrmRegistroUsuario extends JFrame`. Ahora hay un único `JFrame`
  (`MainFrame`, `BorderLayout`) con un panel lateral fijo a la izquierda
  (fondo `#1B1627`: logo, título "Bienvenido" y los botones de navegación
  **"Log In"** / **"Register"**) y, al centro, el `CardLayout` de siempre
  (fondo `#13111C`) alternando `PanelLogin` (carta inicial) y
  `PanelRegistroUsuario`. El botón activo del sidebar se resalta en
  violeta. El tamaño de la ventana es fijo (ya no depende de la carta
  visible, a diferencia de una iteración anterior): ambas cartas viven
  dentro del mismo espacio amplio, y cada una centra su contenido con
  `GridBagLayout` en vez de estirarse a todo lo ancho.
- **`INavegador`**, un contrato de un solo método (`mostrarCarta(String)`),
  es lo único que `PanelLogin`/`PanelRegistroUsuario` conocen para alternar
  entre ellas (además de los botones del sidebar, que llaman al método
  directamente porque `MainFrame` es quien lo implementa). Ningún panel
  importa `MainFrame`.
- **Paleta "SaaS/Tech Morado".** Fondo general `#13111C`, sidebar `#1B1627`,
  tarjetas/formularios `#231F3D` (fondos por capas, cada nivel un poco más
  claro que el anterior), acento violeta `#8B5CF6` (botones de acción, con
  texto blanco en negrita) y texto secundario en blanco suave `#F3F4F6`
  para máxima legibilidad. Sigue sin haber `paintComponent` propio en
  ningún componente — Swing estándar (`setBackground`/`setForeground`/
  `BorderFactory`), como ya exige "Estabilidad de la interfaz" en
  `CLAUDE.md`.
- **Fábrica de componentes (`IComponentesFactory` /
  `ComponentesSwingFactory`).** Ninguna vista vuelve a escribir
  `new Color(...)` ni `new Font("Segoe UI", ...)`: se lo pide a la fábrica,
  incluido el logo (`crearLogo(tamanoPx)`, con marcador de posición "EC" si
  el archivo todavía no existe) y, ahora, el propio sidebar de `MainFrame`.
  Un futuro tema (modo claro, por ejemplo) sería otra clase que implemente
  la misma interfaz.
- **Texto fantasma reutilizable (`PlaceholderFocusListener`).** Una sola
  clase gestiona el placeholder gris de todos los campos de texto (Login y
  Registro) y del campo de contraseña; nadie copia esa lógica.
- **ComboBox sin estilo nativo.** `ComboBoxUIPersonalizado` reemplaza el
  botón-flecha cuadrado de Windows por un triángulo plano.
- **Barra de estado reemplazada por mensajes puntuales.** La antigua barra
  de estado gris desapareció; los errores de validación (de registro o de
  login) se muestran en un mensaje discreto bajo el botón, y el éxito se
  confirma con un `JOptionPane.showMessageDialog(...)` — llamado siempre
  desde la vista (`mostrarExito(mensaje)`), nunca desde el controlador.
  En el registro, el mensaje lo aporta la propia entidad
  (`getMensajeDesbloqueo()`): exactamente "Cliente registrado
  correctamente" o "Proveedor registrado correctamente", texto plano y sin
  símbolos.
- **Autenticación, redirección por rol y ciclo de vida estricto de
  ventanas.** `LoginController` valida que los campos no estén vacíos,
  busca el usuario con `IUsuarioRepository.buscarPorCorreo(correo)` y
  compara la contraseña. Si falla, mensaje de error bajo el botón. Si es
  correcta: `vista.mostrarExito("¡Bienvenido, <nombre>! ...")`,
  `vista.cerrarVentana()` (destruye la `MainFrame` actual — nunca queda
  oculta y viva) y, según `usuario.getTipoCuenta()` (sin `instanceof`),
  abre — maximizada — `ClientDashboardFrame` o `ProviderDashboardFrame`.
  Ninguna recibe el `Usuario` completo, solo su nombre. Cada dashboard trae
  un botón **"Cerrar sesión"** que se destruye a sí mismo y vuelve a abrir
  una `MainFrame` nueva (`app.Main.mostrarVentanaPrincipal(...)`, con el
  mismo repositorio: los usuarios ya registrados no se pierden).
- **La operación de Consulta (Read) del CRUD ya está implementada, vía
  autenticación.** No es un pendiente del roadmap: `LoginController` hace
  una consulta activa contra el repositorio
  (`IUsuarioRepository.buscarPorCorreo(correo)`) para validar las
  credenciales ingresadas, y el resultado de esa consulta
  (`usuario.getTipoCuenta()`) determina la redirección dinámica hacia el
  panel de Cliente o de Proveedor. El Incremento 2 cubre de manera
  integral tanto la validación de registros (Create) como las consultas de
  acceso (Read); lo que queda para más adelante es una **interfaz de
  listado** (una tabla que muestre todos los usuarios a la vez), no la
  operación de consulta en sí — ver la Hoja de ruta.
- **Auto-redirección a Login tras un registro exitoso.** Antes, después de
  registrarse el usuario se quedaba en el formulario de Registro. Ahora,
  apenas cierra el `JOptionPane` de éxito (`mostrarExito(...)` es modal: el
  código que sigue solo corre cuando el usuario hace clic en "Aceptar"),
  `UsuarioController` pide `navegador.mostrarCarta(PanelLogin.NOMBRE_CARTA)`
  y la ventana conmuta sola a la carta de Login, lista para iniciar sesión
  con la cuenta recién creada.
- **`view` modularizado por contexto.** El paquete `view`, que hasta ahora
  era plano, se separó en `view.core` (`MainFrame`, `INavegador`: la
  ventana y la navegación), `view.auth` (`PanelLogin`, `ILoginView`,
  `PanelRegistroUsuario`, `IRegistroUsuarioView`: Login y Registro
  comparten vocabulario y se llaman entre sí sin necesitar imports extra,
  al estar en el mismo paquete) y `view.dashboard`
  (`ClientDashboardFrame`, `ProviderDashboardFrame`). `view.factory` no
  cambió. Cada subpaquete importa del resto solo lo que usa de verdad
  (por ejemplo, `view.core.MainFrame` importa `view.auth.PanelLogin` y
  `view.auth.PanelRegistroUsuario` únicamente por sus constantes
  `NOMBRE_CARTA`).
- **Asistente animado y diálogos con ícono propio.** El sidebar muestra,
  en su esquina superior, un GIF animado (`crearAsistenteAnimado(...)`,
  puramente decorativo: si el archivo no existe, no se ve nada, no rompe
  el layout). Los mensajes de éxito de Login y Registro ya no usan el
  ícono por defecto del Look and Feel: `IComponentesFactory.mostrarDialogoExito(...)`
  arma el `JOptionPane` a mano con el ícono de check propio del proyecto y
  recolorea sus componentes internos con la paleta morada. Ambos íconos
  comparten una sola clase, `IconoImagenEscalada` — dibuja la imagen con
  `Graphics.drawImage(...)` en el tamaño pedido en vez de generar una copia
  con `Image.getScaledInstance(...)`, que rompería la animación del GIF.
- **Menos vacío visual en Login y Registro.** Los subtítulos bajo el
  título ahora son guías cortas ("Ingresa tus credenciales para acceder a
  tu cuenta", "Completa tus datos para crear tu cuenta de Cliente o
  Proveedor") y cada tarjeta suma un micro-texto de ayuda debajo. Las
  tarjetas ya tenían un borde sutil propio (`colorBorde()`, un gris-morado
  discreto) que les da peso visual sin pintar nada a mano.

> **Nota de estabilidad:** una versión intermedia de esta iteración
> reemplazó los bordes cuadrados por esquinas redondeadas pintadas a mano
> con `Graphics2D` y agregó una notificación flotante (`JDialog` +
> Observer). Esa capa causó conflictos de color y texto invisible en uso
> real, así que se revirtió por completo: la interfaz volvió a componentes
> Swing estándar y opacos (`BorderFactory.createLineBorder`, sin
> `paintComponent` propio), y la confirmación de éxito pasó a ser el
> `JOptionPane` descrito arriba. Ver `CLAUDE.md` para la directiva que evita
> repetir ese error.

### Refinamiento visual de los formularios

Última pasada de diseño sobre Login y Registro, siguiendo prácticas
habituales de interfaces oscuras tipo SaaS. Todo vive en la capa de vista:
no se tocó el modelo, los controladores ni `IUsuarioRepository`.

- **Elevación por luminosidad, no por sombras.** En modo oscuro las sombras
  se ven duras, así que la profundidad se expresa aclarando cada capa:
  fondo `#13111C` → sidebar `#1B1627` → tarjeta `#231F3D` → campo
  `#2A2545`. Cuanto más interactivo es un elemento, más cerca está de la
  luz.
- **Esquinas redondeadas sin pintar fondos a mano.** `BordeRedondeado` es
  un `Border` que tapa las esquinas del componente con el color del
  contenedor y traza el contorno encima. El componente sigue siendo opaco
  y pinta su fondo como siempre, así que se obtiene el redondeo **sin**
  `setOpaque(false)` en cascada ni `paintComponent` propio — la técnica que
  en su momento dejó texto invisible.
- **Íconos de contexto en cada campo.** `IconoCampo` dibuja por código los
  glifos de identificación, persona, sobre, candado, pin y edificio. El
  campo dinámico del registro cambia de pin a edificio cuando se pasa de
  Cliente a Proveedor, acompañando el cambio de significado.
- **Anillo de foco.** Al escribir en un campo, su borde pasa al violeta de
  acento y engrosa, indicando dónde está el cursor.
- **Selector segmentado en vez de `JComboBox`.** "Cliente / Proveedor" son
  dos opciones: mostrarlas a la vez resuelve la elección en un clic. De
  paso eliminó un problema real de renderizado — Nimbus pinta los
  `JComboBox` con su gradiente claro e ignora la paleta, así que el control
  se veía como una pieza de modo claro incrustada en la interfaz oscura.
- **Colores base del Look and Feel fijados desde la fábrica.** Por la misma
  razón, los diálogos salían con fondo claro. La fábrica ajusta las claves
  base de Nimbus al construirse, y los controles que lo necesitan usan los
  delegados `Basic*` del JDK, que sí respetan `setBackground`.

### Celebración al completar el registro

Cuando el registro termina bien, la vista lanza `fabrica.celebrar(...)`
antes de mostrar el diálogo de confirmación:

- **Confeti** (`AnimacionConfeti`): partículas que salen desde la base de
  la ventana y caen girando, animadas con un `javax.swing.Timer`. Se
  dibujan sobre el `glassPane`, la capa que Swing reserva para superponer
  contenido: no toca ningún componente del formulario y al terminar se
  retira sola, dejando la ventana como estaba.
- **Sonido** (`SonidoExito`): un arpegio corto en `resources/audio/exito.wav`,
  reproducido con `AudioSystem`/`Clip`. Si el equipo no tiene salida de
  audio, el registro termina igual: el fallo se ignora en silencio porque
  el sonido es un adorno, no parte del caso de uso.

El controlador no se enteró de nada de esto: sigue llamando a
`vista.mostrarExito(...)`, y es la vista la que decide cómo se celebra.

### Pantallas de carga

Dos momentos de espera dejaron de ser saltos secos. El criterio para elegir
el indicador viene de la guía de UX de espera: por debajo de un segundo no se
pone nada, entre uno y tres basta un giro continuo, y a partir de ahí
conviene un indicador que muestre **cuánto falta**, porque reduce la espera
percibida y el abandono.

- **Bienvenida al abrir la aplicación** (`view.core.VentanaSplash`): una
  ventana sin barra de título con el logotipo circular, el título "Comercio
  Electrónico", un indicador **de avance** y una línea de estado que va
  cambiando ("Cargando módulos...", "Iniciando sistemas...", "Preparando tu
  espacio...", "Todo listo"). Los mensajes no son decorativos: contar qué
  está pasando convierte una espera pasiva en activa, y esa se percibe más
  corta. Aparece **solo en el arranque en frío**; al cerrar sesión la
  aplicación ya está corriendo y repetirla sería una espera regalada.
- **Transición al iniciar sesión** (`IComponentesFactory.mostrarTransicion`):
  tras validar las credenciales, una ventana flotante con un indicador
  **giratorio** y el texto "Iniciando sesión..." acompaña el salto al panel
  del rol. Al ser una espera corta, gira sin porcentaje.

Ambos indicadores son la misma clase, `view.factory.IndicadorCarga`, en sus
dos modos. El reparto de responsabilidades se mantiene: `LoginController`
decide **qué** pasa después de autenticar y lo entrega como `Runnable`; la
vista decide **cómo** se ve esa espera y ejecuta la acción al terminar. El
controlador sigue sin importar nada de `javax.swing` para esto.

---

## Pilares de la POO aplicados

**Encapsulamiento.** Los atributos de las entidades son `private` y se accede a ellos solo por getters y setters, lo que permitirá cifrar la contraseña más adelante sin tocar el resto del código. En la vista, `CampoPasswordConToggle` esconde su `JPasswordField` interno: el resto del código solo conoce `getPassword()` y `limpiar()`.

**Herencia.** `Cliente` y `Proveedor` extienden `Usuario` y agregan únicamente el atributo que las diferencia.

**Abstracción.** `Usuario` es `abstract` porque en el dominio nunca se registra un usuario genérico. `IUsuarioRepository` oculta el mecanismo de almacenamiento tras un contrato.

**Polimorfismo.** Opera en varios puntos: el repositorio recibe `Usuario` y acepta cualquier subclase; una `List<Usuario>` guarda ambos tipos; `UsuarioController` muestra el mensaje de confirmación con `usuario.getMensajeDesbloqueo()`; y `LoginController` decide qué ventana abrir con `usuario.getTipoCuenta()`. Ninguno de los dos controladores usa `if`/`instanceof` para distinguir Cliente de Proveedor.

---

## Principios SOLID

| Principio | Aplicación |
|---|---|
| **S** | La vista pinta, el controlador coordina, el repositorio persiste. `MainFrame` solo navega entre cartas; `ComponentesSwingFactory` es la única clase que conoce colores, fuentes y el logo; `UsuarioController`/`LoginController` cada uno resuelve un único caso de uso. |
| **O** | Agregar un tipo `Administrador` solo requiere una subclase nueva. Agregar un tema visual nuevo solo requiere otra `Paleta` (`view.factory.tema`), sin tocar la fábrica ni las vistas. Agregar una carta nueva a `MainFrame` solo requiere llamar `mainFrame.agregarCarta(...)` desde `app.Main.mostrarVentanaPrincipal(...)`; agregar un dashboard de rol nuevo es otra clase `JFrame` más, sin tocar `MainFrame` ni `LoginController` fuera de un caso más en su bifurcación. |
| **L** | Cualquier subclase de `Usuario` funciona donde se espera un `Usuario`; cualquier implementación de `IComponentesFactory` funciona donde una vista espera una fábrica; cualquier implementación de `INavegador` funciona donde un panel espera poder cambiar de carta. |
| **I** | `IUsuarioRepository` expone solo lo que los casos de uso actuales necesitan (`registrar`, `buscarPorCorreo`). `IRegistroUsuarioView`/`ILoginView` solo exponen lo que su controlador usa. `INavegador` es un contrato de un único método. |
| **D** | Los controladores dependen de `IRegistroUsuarioView`/`ILoginView`, `IUsuarioRepository` e `IComponentesFactory` (abstracciones); las implementaciones concretas se nombran únicamente en `app.Main` (o, en el caso de las dos ventanas de destino del login y de la reapertura tras cerrar sesión, en el propio `LoginController`, que es el caso de uso que decide cuál abrir). |

---

## Incremento 3 — catálogo, carrito, compra y gestión del proveedor

### Lo que hace el Cliente

Recorrer el catálogo, filtrar por categoría, buscar, ver el detalle de un
producto, elegir cantidad, armar el carrito y confirmar la compra. Al
confirmar, el sistema calcula el total, asocia el pedido a la dirección que el
usuario registró, descuenta el stock y vacía el carrito.

### Lo que hace el Proveedor

Cuatro indicadores en la parte superior —ingresos, unidades vendidas, pedidos
en los que participó y productos publicados, con cuántos quedaron sin
existencias— y debajo una tabla con su catálogo, donde puede **publicar,
editar y eliminar** productos. Lo que guarde aparece de inmediato en el
catálogo del Cliente, porque ambos paneles hablan con el mismo
`IProductoRepository`.

Dos decisiones que importan:

- **Cada proveedor ve y administra solo lo suyo.** La tabla se llena con
  `listarPorProveedor(correo)`, y un proveedor recién registrado empieza con
  el catálogo vacío. El catálogo sembrado de ejemplo pertenece a
  `proveedor@empresa.com`.
- **Los ingresos se suman renglón por renglón, no por el total del pedido.**
  Una misma compra puede mezclar artículos de varios proveedores; tomar el
  total del pedido le atribuiría a cada uno las ventas de los demás.

Editar y Eliminar actúan sobre la fila seleccionada de la tabla en vez de
llevar un botón dentro de cada celda: poner controles dentro de las celdas
obliga a escribir editores y renderizadores propios, y seleccionar y pulsar es
el mismo gesto con componentes estándar. Eliminar pide confirmación, porque es
irreversible.

### Stack visual: FlatLaf y MigLayout

**Look and Feel.** La aplicación arranca con `FlatDarkLaf`, instalado dentro
del hilo de eventos de Swing antes de construir ninguna ventana. Sustituye a
Nimbus, que ignoraba `setBackground` en media docena de controles y obligaba a
sortearlo con delegados `Basic*UI`; FlatLaf respeta los colores que se le piden
y pinta ventanas y diálogos oscuros hasta la barra de título. Si no estuviera
disponible, se cae a Nimbus y la aplicación sigue viéndose con sus colores.

**FlatLaf no sustituye a la fábrica.** El L&F pone la base — barras de
desplazamiento, cursores, sombras, tipografía — y `ComponentesSwingFactory`
sigue poniendo la identidad: la paleta morada, los bordes redondeados, los
íconos y las animaciones son exactamente los de antes.

Los parches heredados de Nimbus **se dejaron puestos**. Se comprobó con
capturas que con FlatLaf la interfaz se ve correcta tal como está; retirarlos
es una tarea aparte, de uno en uno y verificando cada vez, no un efecto
colateral gratuito del cambio de L&F.

**MigLayout está solo en los formularios** (login, registro de usuario y alta
de producto), que es donde paga: son columnas de pares etiqueta/campo, y con
`GridBagConstraints` cada fila costaba tres líneas más un contador de fila que
había que ir pasando entre métodos. Ahora la columna se declara una vez y cada
componente solo dice su separación.

No se llevó a los dashboards: sus layouts están verificados con capturas y
migrarlos sería movimiento sin ganancia. Tampoco al panel raíz de Login y
Registro, que sigue siendo un `GridBagLayout` sin peso porque es justo lo que
centra la tarjeta dentro de la ventana amplia. Tras migrar el formulario de
registro se volvió a medir su alto: **582px de los ~613 disponibles**, con la
alerta visible o sin ella.

### Editar perfil

Desde el menú del avatar, en los dos paneles. Un modal con dos secciones
separadas a propósito: los datos de contacto (nombre, correo, teléfono y el
dato propio del rol) y el cambio de contraseña. Cambiar el teléfono y cambiar
la clave son gestos de riesgo distinto, y mezclarlos en una sola lista de
campos invita a tocar la contraseña sin querer.

Tres decisiones que importan:

- **El formulario no se cierra cuando algo falla.** Si el correo está repetido
  o la contraseña nueva no cumple, el mensaje aparece dentro y lo ya escrito se
  queda. Para eso existe `crearDialogoModal(...)`: el diálogo de confirmación
  de la fábrica se cierra pase lo que pase.
- **Para cambiar la contraseña hay que escribir la actual.** Sin eso,
  cualquiera que se siente frente a una sesión abierta podría cambiarla. Las
  reglas de la nueva son las mismas del registro, reutilizando
  `PoliticaPassword`: si algún día cambia el mínimo, cambia en los dos sitios
  porque solo hay un sitio. Dejar los tres campos vacíos significa "no la
  cambio", y es un caso normal.
- **El rótulo del último campo depende del rol** —"Dirección de envío" para un
  Cliente, "NIT de la empresa" para un Proveedor— y se resuelve con el par
  `getDatoEspecifico()` / `setDatoEspecifico(...)`, sin un solo `instanceof`.

El teléfono es nuevo en el modelo. **No se añadió al constructor** para no
tocar todas las llamadas existentes ni alargar el registro: arranca vacío y se
rellena aquí.

### Imagen de producto: se elige del disco

En el formulario del Proveedor, la ruta ya no se teclea. Hay un botón
"Seleccionar imagen" que abre el selector de archivos filtrado a `png`, `jpg`
y `jpeg`, y al lado una **vista previa** de lo elegido. Una ruta escrita no
dice si el archivo es el correcto ni si el programa puede leerlo; la
miniatura confirma las dos cosas antes de guardar.

Se guarda la ruta absoluta, que el cargador de imágenes ya resolvía como
tercer intento, así que lo que el proveedor elige aparece de inmediato en el
catálogo del Cliente. **Con una salvedad honesta:** una ruta absoluta vale en
ese equipo. Si algún día el catálogo viaja a otra máquina, habrá que copiar el
archivo a `resources/images/productos/` en el momento de elegirlo.

### Cerrar sesión con transición

La opción del menú del avatar ya no salta de golpe al Login: muestra el mismo
indicador que el acceso, con el texto "Cerrando sesión...", y solo después
destruye la ventana y reabre la principal.

### Micro-interacciones

**Los tres estados de un botón los pinta FlatLaf.** Hasta ahora los botones
llevaban un delegado `BasicButtonUI` heredado de los tiempos de Nimbus, y ese
delegado impedía que la librería pintara nada: no había ni hover real ni estado
pulsado. Se retiró, y cada botón declara su propio estilo con
`FlatClientProperties.STYLE` — por componente y no global, porque el botón de
acento y un segmento oscuro no pueden reaccionar igual. Medido en pantalla:
reposo `#8B5CF6`, al pasar el mouse `#A78BFA`, pulsado `#7C3AED`.

**Los botones principales crecen un 3% al pasar el mouse**, y vuelven suave al
salir. La primera versión de ese efecto animaba el *tamaño preferido* del
botón, que es lo que consulta el gestor de disposición, así que el formulario
entero se recolocaba bajo el cursor (medido: la tarjeta del Login pasaba de 266
a 269px de alto). Ahora se animan directamente los límites del componente y
**nunca** se pide recolocar: el botón se pinta más grande sobre el margen de su
propia celda y nada a su alrededor se mueve. Verificado: `306x44` → `315x45`
mientras el vecino de encima y la tarjeta conservan sus medidas al píxel.

**Los campos de captura tienen hover y un anillo de foco que se funde.** El
salto seco de gris a violeta se notaba brusco justo donde el ojo está mirando,
así que el color se interpola con un `Timer`, la misma técnica que el zoom de
los botones.

**El avatar es un botón, no una etiqueta.** Antes se escuchaba con
`mouseClicked`, que no se dispara si el ratón se mueve un píxel entre pulsar y
soltar: el menú fallaba de forma intermitente y parecía un problema de
esquinas. Medido con un arrastre de 1px no abría; quieto sí. Convertido en
botón abre siempre, en cualquier punto del componente, y de paso reacciona al
hover y al pulsado como el resto.

**El buscador solo se enfoca si lo pulsas.** Se quedaba el cursor al abrir la
ventana y también cada vez que la rejilla se reconstruía, porque al
desaparecer el botón enfocado Swing replegaba al primer componente enfocable,
que era él. El panel del Cliente declara ahora una política de recorrido sin
componente por defecto, así que el foco solo llega donde el usuario lo pone.
Y si se pulsa en una zona vacía del catálogo, lo suelta: Swing por sí solo no
mueve el foco al pulsar algo que no es enfocable, de modo que el cursor se
quedaba parpadeando ahí para siempre.

**Un solo botón flotante, y donde no duplica nada:** volver al principio del
catálogo del Cliente. Aparece pasados 160px de desplazamiento. Repetir ahí el
carrito o el buscador, que viven en la barra superior, habría sido ruido.

**Las alertas de validación son una franja, no una línea suelta:** borde y
fondo teñidos del color del mensaje, en rojo para errores y en ámbar para
avisos. La cuenta atrás del bloqueo por intentos fallidos usa el tratamiento de
aviso, porque el usuario no se ha equivocado: está esperando. La franja reserva
su sitio aunque esté vacía, de modo que el formulario no salta cuando aparece un
mensaje.

### Persistencia: MongoDB Atlas

La aplicación guarda en la nube si se lo configuras, y en memoria si no.

**Para conectarla:** copia `config.properties.ejemplo` como `config.properties`
y rellena tu cadena de Atlas.

```properties
mongodb.uri=mongodb+srv://usuario:clave@tu-cluster.mongodb.net/?retryWrites=true&w=majority
mongodb.base=comercio_electronico
resend.api.key=re_tu_clave
resend.remitente=Comercio Electronico <onboarding@resend.dev>
```

Quien lee ese archivo es `service.config.Configuracion`, y solo él: la conexión y el
correo piden su clave, no abren el archivo por su cuenta. Si cada uno lo
abriera, el formato, la codificación y el criterio de "qué pasa si falta" se
decidirían dos veces. Lo busca en la carpeta de trabajo, después junto al
`.exe` y después junto al JAR, así que en el ejecutable basta con dejarlo al
lado de `ComercioElectronico.exe` (ver `docs/05_Empaquetado`).

Ese archivo está en `.gitignore` y **no se entrega**: una contraseña escrita en
un archivo del proyecto termina copiada en cualquier copia del proyecto. Lo que
sí viaja es la plantilla.

**Sin ese archivo la aplicación arranca igual**, con el almacén en memoria de
siempre, y lo dice por consola. Es deliberado: una entrega académica tiene que
poder abrirse y defenderse sin red. El único punto que decide entre una cosa y
otra es `app.Main`, igual que con el notificador de correo — ni las vistas ni
los controladores saben qué hay detrás de las interfaces de repositorio.

**Conversión documento ⇄ entidad (patrón Adapter).** Los repositorios de
`model.repository.mongo` solo deciden qué se consulta; convertir entre un
`org.bson.Document` y una entidad lo hacen los adaptadores de
`model.repository.mongo.adapter`:

| Adaptador | Colección | Entidad | Qué resuelve |
|---|---|---|---|
| `UsuarioAdapter` | `usuarios` | `Cliente` / `Proveedor` | elige la subclase por `tipoCuenta`; cuentas antiguas sin teléfono |
| `ProductoAdapter` | `productos` | `Producto` | `ObjectId` ⇄ texto, números enteros o decimales, categoría por nombre, campo derivado `busqueda` |
| `CompraAdapter` | `compras` | `Pedido` | renglones anidados, fecha UTC ⇄ `LocalDateTime` |

Los tres implementan `AdaptadorDocumento<T>`. Los nombres de campo que usan
las consultas son constantes de su adaptador, así que el filtro y el documento
no pueden dejar de coincidir.

Las tres colecciones (`usuarios`, `productos`, `compras`) **se crean solas** con
el primer documento; no hace falta ningún script. Los renglones de un pedido van
anidados dentro de su documento, porque un pedido se lee siempre entero y sus
renglones no existen sin él.

### La espera no congela la ventana

Swing pinta y atiende los clics en un **único hilo**. Cualquier cosa que tarde
en ese hilo deja la ventana sin repintarse y Windows la marca como "no
responde". Con el almacén en memoria no se notaba; con Atlas detrás, cada
consulta es un viaje por red.

El trabajo lento va en un `SwingWorker` con un indicador delante, y el reparto
es el mismo de siempre: **el controlador dice qué tarda, la vista decide cómo
se ve la espera**. El controlador llama a
`ejecutarEnSegundoPlano(mensaje, tarea, alTerminar)` —un método del contrato de
la vista, no de Swing— y la vista lo resuelve con
`IComponentesFactory.ejecutarConCarga(...)`, que monta un `DialogoCarga`: un
diálogo **modal** sin decoración con el indicador giratorio del proyecto. Modal
a propósito, para que nadie pulse dos veces "comprar" mientras la primera
compra viaja.

Qué corre ya fuera del hilo de eventos:

| Operación | Qué hace | Medido |
|---|---|---|
| Iniciar sesión | busca la cuenta por correo | 1 consulta |
| Registrarse | comprueba el correo, cifra con BCrypt y guarda la cuenta | 2 consultas + BCrypt |
| Confirmar compra | comprueba existencias, registra el pedido y descuenta stock | 2N + 1 consultas |
| Publicar / editar / eliminar un producto | escribe en el catálogo | 1–2 consultas |
| Enviar un correo (compra o bienvenida) | petición HTTP a Resend | en su propio hilo; nadie lo espera |

Qué sigue en el hilo de eventos, a sabiendas: los **refrescos de pantalla**
posteriores (volver a pedir el catálogo, el carrito o los indicadores). Son una
sola lectura, ~100 ms contra el clúster real, y sacarlos también obligaría a
partir cada acción en dos esperas encadenadas.

Detalles que importan del reparto: el carrito se vacía **después** de que la
compra vuelve bien, no antes —mientras viaja todavía puede fallar, y un carrito
vaciado de antemano dejaría al usuario sin nada que reintentar—; y el trabajo de
fondo no toca la vista, así que devuelve su resultado (`ResultadoCompra`, el
nombre del producto, la cuenta encontrada) para que lo cuente quien continúa ya
en el hilo de eventos.

Comprobado en ejecución: la tarea corre en `SwingWorker-pool-1-thread-1`, la
ventana se sigue repintando durante la espera y el diálogo se cierra solo al
terminar.

### Catálogo sincronizado (patrón Observer)

Cuando un Proveedor publica, edita o elimina un producto, el catálogo que un
Cliente tiene abierto se actualiza solo, sin reabrir la ventana.

- `observer.CatalogoObserver` declara el aviso (`actualizarCatalogo()`) y
  `observer.CatalogoSubject` lleva la lista de suscritos
  (`agregarObservador`, `removerObservador`, `notificarObservadores`).
- **Avisan** `ProveedorController`, tras cada escritura con éxito, y
  `VigilanteCatalogoMongo`, que escucha un *Change Stream* de MongoDB sobre la
  colección `productos` y trae los cambios hechos **desde otros equipos**. Esta
  segunda fuente es la que importa en la práctica: en un mismo programa nunca
  están abiertos a la vez el panel del Proveedor y el del Cliente.
- **Observa** el panel del Cliente. Recibe el aviso, pasa al hilo de eventos y
  pide al `ClienteController` que recargue, conservando la búsqueda y la
  categoría activas. Varios avisos seguidos se funden en una sola recarga.
- El panel se suscribe al abrirse y se da de baja al cerrar sesión.

Sin conexión a Atlas solo quedan los avisos locales.

### Acceso con Google

Si `config.properties` trae `google.client.id` y `google.client.secret` (un ID
de cliente OAuth de tipo **App de escritorio**, ver los pasos en
`config.properties.ejemplo`), el Login muestra **Continuar con Google**:

1. La aplicación abre un servidor HTTP efímero en `127.0.0.1` y abre el
   navegador en la página de Google.
2. Google devuelve el navegador a `http://127.0.0.1:PUERTO/callback` con un
   código; el servidor lo recoge y se apaga.
3. La aplicación canjea el código (con PKCE) y lee el correo verificado y el
   nombre.
4. Si el correo ya tiene cuenta, entra. Si no, pregunta si será Cliente o
   Proveedor y crea la cuenta, sin contraseña propia (marca `AUTH_GOOGLE`); la
   dirección o el NIT se completan después en "Editar perfil".

La espera se puede cancelar. Sin credenciales, el botón no aparece y todo lo
demás funciona igual. No añade librerías: `com.sun.net.httpserver` y
`HttpClient` son del JDK.

### Sesión recordada

Al entrar, la aplicación recuerda al usuario en
`~/.comercio-electronico/session.properties` (`user.email` y `user.role`, nunca
la contraseña). En el siguiente arranque, `SesionController.reanudar(...)` busca
esa cuenta en el repositorio y, si existe con el mismo rol, abre directamente su
panel sin pasar por el Login. Si la cuenta ya no existe o el rol del archivo no
coincide con el real, lo descarta y muestra el Login.

- **"Cerrar sesión"** borra el archivo, destruye el panel y vuelve al Login.
- **La X del panel** termina el programa (`EXIT_ON_CLOSE`) y conserva la sesión:
  cerrar la ventana no es cerrar sesión.

### Contraseñas cifradas con BCrypt

El registro guarda el hash, nunca el texto. El login verifica con
`BCrypt.checkpw` y el cambio de contraseña del perfil exige la actual y cifra la
nueva. Una sola clase, `controller.CifradoPassword`, conoce la librería —
exactamente como `PoliticaPassword` es la única que conoce las reglas.

Acepta además contraseñas guardadas en claro. No es una concesión: el almacén en
memoria siembra usuarios así, y sin esa compatibilidad activar el cifrado habría
dejado fuera a toda cuenta creada antes.

### Rediseño de las dos interfaces

Las dos pantallas se rehicieron sobre lo que dicen las guías de usabilidad de
comercio electrónico y de tableros de datos. Estos son los cambios y el motivo
de cada uno.

**Barra de búsqueda.** El campo llevaba un glifo que se leía como una casilla
de verificación; ahora lleva una lupa, es más ancho y se enfoca al pulsar en
cualquier punto del control, ícono incluido. Es el único sitio donde un texto
fantasma hace de etiqueta, que es justo el caso en que las guías lo admiten:
un buscador es un campo único y familiar, así que no hay nada que recordar ni
que revisar antes de enviar. En los formularios la etiqueta sigue yendo fuera
del campo. La ventana además **no le da el foco inicial al buscador**: con el
foco puesto, el texto fantasma se retira y la barra aparecería vacía y muda.

**Carrito: de panel fijo a cajón con contador.** Antes ocupaba
permanentemente la derecha de la ventana, aunque estuviera vacío. Ahora vive
detrás de un ícono en la barra superior con el número de artículos encima
(*badge*), y se despliega como cajón lateral animando su ancho. El catálogo
recupera esa franja. Cada renglón lleva **miniatura del producto**, nombre,
cantidad, subtotal y un botón de quitar: en el carrito es donde la gente
decide de verdad qué compra, y sin imagen hay que leer los nombres uno por uno
para reconocer lo que se lleva. Al agregar algo, el cajón se abre solo —así la
acción se confirma sin ningún aviso— y al terminar la compra se cierra.

**Detalle del producto.** Imagen grande a la izquierda; a la derecha
categoría, título destacado, descripción completa, precio con el anterior
tachado y su insignia de descuento, existencias y el selector de cantidad
"− n +" topado al stock.

**Panel del Proveedor.** Las tarjetas de indicadores ganaron jerarquía con una
franja de acento y una cifra más grande, y el panel incorpora un **gráfico de
barras horizontales** dibujado con Swing en la paleta del tema. Son barras
porque la longitud y la posición son los rasgos con los que mejor se estima
cuánto mayor es una cosa que otra, y horizontales porque las etiquetas son
nombres de productos, que en vertical habría que girar o recortar. El gráfico
muestra **ingresos por producto**; mientras no haya ventas muestra el
inventario disponible, y el título lo dice siempre, porque cambiar de serie
sin anunciarlo sería engañoso y enseñar un recuadro vacío no informa de nada.

### Imágenes de producto

El formulario del Proveedor tiene un campo **Imagen**, y lo que registre ahí
aparece de inmediato en el catálogo del Cliente. La referencia se busca en
tres sitios y en este orden: como recurso del programa
(`resources/images/productos/`), como archivo dentro de esa misma carpeta del
proyecto y, por último, como ruta del sistema, para que un proveedor pueda
usar una imagen suya que no viaja con la entrega. Si no hay imagen o no se
puede leer, el recuadro muestra la inicial del producto: un hueco con algo
dentro se lee mejor que un marco roto.

La imagen se encaja conservando su proporción, no estirándola: la misma
referencia se muestra apaisada en la tarjeta del catálogo y casi cuadrada en
el detalle, y deformarla en una de las dos se nota de inmediato.

**Las ilustraciones del catálogo se generaron, no se descargaron** — misma
razón que el GIF del sidebar: una imagen de banco trae derechos que no encajan
en una entrega académica y nunca coincide con los hex del tema. Son diez
trazos de línea en `#8B5CF6`/`#A78BFA` sobre el fondo del recuadro `#2A2545`,
a 16:9 para que encajen en los dos tamaños sin deformarse.

### Cómo entra MongoDB Atlas

La persistencia se define por **interfaces** (`IProductoRepository`,
`IPedidoRepository`, y el ya existente `IUsuarioRepository`) y hoy corre
sobre implementaciones en memoria con un catálogo de ejemplo. Esto no es un
simulacro provisional: es el diseño que permite que Atlas entre sin tocar
nada más.

Para conectar Atlas hacen falta tres cosas, y el resto del sistema no se
entera de ninguna:

1. Añadir el driver oficial de MongoDB al proyecto (`lib/` + classpath de
   Ant). **Es la única dependencia externa del proyecto**, que hasta ahora es
   JDK puro.
2. Escribir `ProductoRepositoryMongo` y `PedidoRepositoryMongo` implementando
   las mismas interfaces, mapeando cada entidad a un documento.
3. Cambiar la línea de `app.Main` que hoy instancia las versiones en memoria.

Ni las vistas ni los controladores cambian: `ClienteController` y
`ProveedorController` dependen de la interfaz, no de quién la implemente. Las colecciones previstas son
`usuarios`, `productos` (nombre, descripción, precio, descuento, categoría,
stock, imagen, proveedor) y `pedidos` (usuario, renglones, total, dirección
de entrega, fecha).

**La credencial de Atlas no va en el código.** Igual que la clave de Resend,
se lee de `config.properties`; una cadena de conexión con usuario y contraseña
escrita en un archivo fuente termina copiada en cualquier entrega.

### Correos transaccionales (Resend)

La aplicación envía tres correos, con una plantilla común (`PlantillaCorreo`)
que se adapta al móvil:

- **Bienvenida** al registrarse por el formulario.
- **Confirmación de compra** al cliente: tabla con productos, cantidades,
  precio unitario, subtotales y total pagado, más la dirección de entrega.
- **Alerta de venta** a la tienda (`resend.alertas` en `config.properties`):
  quién compró, su correo y el mismo detalle.

Todos pasan por el mismo servicio:

- `INotificadorPedido` (lo usa `ClienteController`) e `INotificadorCuenta` (lo
  usa `UsuarioController`): cada controlador ve solo el aviso que le toca.
- `NotificadorResend` — envío real por HTTP a la API de Resend, con el
  `HttpClient` del JDK: **no añade dependencias**.
- `NotificadorRegistroLocal` — el que corre sin clave: imprime por consola
  exactamente el correo que se habría enviado.
- `NotificadorEnSegundoPlano` — decorador que envuelve a cualquiera de los dos
  y manda los correos **en su propio hilo**: el controlador pide el correo y
  sigue. Así una API lenta no alarga la espera de una compra o de un registro
  que ya se guardaron. Al cerrar la aplicación espera hasta 10 s a que salga
  el correo pendiente.

`app.Main` elige según haya o no `resend.api.key` en `config.properties` (y
opcionalmente `resend.remitente`). Activar el envío real es rellenar esa clave.

El remitente de la tienda es `soporte@misupertiendajava.cyou`, de un dominio
verificado en Resend.

**Ojo con el remitente de prueba.** Con `onboarding@resend.dev`, Resend solo
entrega correos a la dirección de la propia cuenta de Resend. Para escribir a
cualquier cliente hay que verificar un dominio en Resend y poner un remitente
de ese dominio en `resend.remitente`.

Un fallo de correo **nunca** tumba una compra ni un registro: se informa por
consola y la operación sigue hecha.

### Iteración final de la interfaz: tema claro y promociones

- **Tema claro.** Fondo gris muy suave `#F4F5FA`, tarjetas blancas con borde
  `#E2E4EC`, texto `#1F2937`, morado de marca `#7C3AED` (más contraste sobre
  blanco que `#8B5CF6`), **naranja `#EA580C` para las acciones de compra**
  ("Confirmar compra", "Ver oferta") y azul `#2563EB` para datos informativos.
  El sidebar del Login y la barra superior de los paneles siguen en morado
  oscuro `#1B1627`: el GIF del asistente lleva ese fondo horneado.
- **El tema es un dato.** Los colores salieron de `ComponentesSwingFactory` a
  `view.factory.tema.Paleta` (`clara()` y `oscura()`); `app.Main` elige una en
  una línea y, según sea clara u oscura, instala `FlatLightLaf` o `FlatDarkLaf`.
  Antes un tema nuevo exigía duplicar una fábrica de más de mil líneas.
- **Vitrina oscura.** Las ilustraciones de producto traen el fondo `#2A2545`
  horneado, así que su recuadro conserva ese color en los dos temas.
- **Banner rotativo** en la cabecera del catálogo del Cliente: las cuatro
  mejores ofertas, cambio cada 5 s con fundido, pausa con el puntero encima,
  flechas y botón "Ver oferta" que abre la ficha del producto.
- **Pop-up promocional al entrar**, en el 40 % de los accesos
  (`ClienteController.PROBABILIDAD_PROMOCION`, con `Math.random()`): un
  producto con descuento y existencias, imagen, precios, insignia y una X
  dibujada para cerrarlo (también con Escape).
- **Degradados solo donde no hay hijos**: el banner y la cabecera del pop-up se
  dibujan enteros, como `GraficoBarras`; ningún contenedor con hijos pinta su
  fondo a mano.
- **Carrito**: miniatura, precio por unidad, selector `- n +` por renglón
  (topado por el stock y validado otra vez en el controlador) y subtotal.
- **Tablero del Proveedor**: cuatro indicadores con su propio color (ingresos
  con ticket promedio, unidades con el producto más vendido, pedidos y
  catálogo con agotados en rojo).
- **Paquetes**: `service` dividido en `config`, `sesion`, `google` y `correo`;
  `view.dashboard` en `cliente` y `proveedor`; los repositorios en memoria en
  `model.repository.memoria`, simétricos a `model.repository.mongo`.

### Doble rol, empresa, reseñas, datos de envío e imágenes portables

- **Doble rol.** Todo usuario entra a la tienda, porque todo usuario compra.
  El Proveedor ve además **"Gestionar tienda"** en el menú de su avatar, que
  lo lleva a su panel; desde el panel, **"Ir a la tienda"** lo devuelve. Un
  Cliente no ve esa opción. El carrito no se pierde al ir y volver.
- **Empresa o marca.** El registro de Proveedor la pide (NIT y empresa en la
  misma fila) y cada producto queda con ella: en la tarjeta y en la ficha
  aparece **"Vendido por Supertecno"**. Si el proveedor cambia la empresa o el
  correo en su perfil, se actualizan todos sus productos.
- **Reseñas y estrellas.** La ficha del producto muestra el promedio con
  estrellas (admite media estrella), las opiniones y, si el usuario **compró**
  el producto, un formulario para calificarlo de 1 a 5 y comentar. El vendedor
  no puede reseñar lo suyo. Las tarjetas del catálogo muestran el promedio y
  cuántas reseñas tiene.
- **Datos de envío obligatorios.** Una cuenta de Google nace sin cédula ni
  dirección: el perfil lo avisa, y al confirmar una compra aparece un
  formulario rápido que la bloquea hasta completarlos; al guardarlos, la
  compra sigue sola. El pedido y los correos llevan el documento del
  comprador.
- **Imágenes sin rutas locales.** En la base ya no se guarda
  `C:\Users\...`: la imagen elegida se reduce a 800 px y se guarda en Atlas
  (colección `imagenes`), y el producto guarda solo `img:<id>`; sin Atlas, va
  a la carpeta relativa `imagenes/`. Las rutas antiguas se corrigen solas al
  arrancar.
- **Modo claro / oscuro.** Botón de luna/sol en la barra de la tienda, en el
  panel y en el sidebar del Login. Cambia en el sitio, **sin cerrar la
  ventana**: FlatLaf instala la variante clara u oscura,
  `updateComponentTreeUI` actualiza los componentes y una foto de la ventana
  se desvanece encima para que el cambio no sea un salto. Lo escrito, el
  carrito y la posición del catálogo se conservan. Se recuerda para el
  próximo arranque.

### Qué falta del Incremento 3

Nada de lo previsto para este incremento: el envío real de los tres correos
por Resend ya está probado y aceptado. Lo pendiente del proyecto en conjunto
(pruebas manuales sin ejecutar, prueba de Google
real) está en la sección "Pendientes" de la
[guía de traspaso](docs/00_Inicio/guia_de_traspaso.md).

La conexión contra un clúster real de Atlas **ya está probada**: responde al
`ping` y las tres colecciones (`usuarios`, `productos`, `compras`) existen.
Medido en ese clúster: **3,5 s** la primera conexión —el driver la abre de
forma perezosa— y **~100 ms** cada consulta posterior. De ahí la sección
siguiente.

---

## Seguridad del acceso

**Control de intentos fallidos.** Tras **3 fallos consecutivos** de
credenciales, el acceso se bloquea **30 segundos**: los campos dejan de
aceptar escritura, el botón se desactiva y una cuenta regresiva en el propio
formulario indica cuánto falta. Al terminar, todo se reactiva solo y el
contador vuelve a cero. Mientras quedan intentos, el mensaje avisa cuántos
son ("Te quedan 2 intentos"). Solo cuentan los fallos **seguidos**: un acceso
correcto limpia el historial, así que un error aislado de tecleo nunca
bloquea a nadie.

El reparto de responsabilidades es el mismo del resto del proyecto:

- `controller.ControlIntentosFallidos` es la **política**: cuántos intentos
  se toleran y cuánto dura el bloqueo. No conoce Swing; solo cuenta y
  responde. Cambiar la regla (más intentos, bloqueo progresivo) se hace ahí.
- `LoginController` **decide** cuándo aplicarla y qué pasa al liberarse.
- `PanelLogin` decide **cómo se ve**: desactiva los controles y lleva la
  cuenta atrás con un `javax.swing.Timer`, que es trabajo de interfaz.

**Requisitos de la contraseña.** Para registrarse, la contraseña debe tener
al menos **7 caracteres** e incluir **una mayúscula, un número y un carácter
especial**. No hay longitud máxima, y es deliberado: un tope corto empuja a
la gente hacia contraseñas peores sin aportar seguridad. Los mensajes de
error dicen exactamente qué falta ("debe incluir al menos un número"), en vez
de repetir la lista completa de reglas.

**Semáforo de fortaleza en vivo.** Junto a la etiqueta "Contraseña" del
registro hay un punto de color que se actualiza con cada tecla:

| Color | Significado | Cuándo |
|---|---|---|
| Rojo | Insegura | No cumple los requisitos (el registro la rechazará) |
| Ámbar | Medianamente segura | Cumple los requisitos, pero es corta |
| Verde | Segura | Cumple los requisitos y además tiene 12+ caracteres con mayúsculas y minúsculas |

Validación y semáforo salen de la **misma** clase
(`controller.PoliticaPassword`), así que no pueden contradecirse: lo que el
punto pinta en rojo es exactamente lo que el registro va a rechazar. El punto
vive en la fila de la etiqueta, no en una fila propia, para no consumir alto
en un formulario que ya va justo de espacio.

---

## Validaciones

Implementadas en `UsuarioController.validar()`: todos los campos obligatorios, identificación solo numérica, formato de correo válido y contraseña de mínimo 4 caracteres. El repositorio rechaza además identificaciones duplicadas.

Para aceptar identificaciones alfanuméricas, eliminar la condición `identificacion.matches("\\d+")`.

---

## Manual de uso

**Registro.** Diligenciar los campos (cada uno guía con un texto fantasma gris que desaparece al enfocar), seleccionar el tipo de cuenta (la etiqueta del último campo cambia sola entre "Dirección de envío" y "NIT de la empresa") y presionar **REGISTRAR USUARIO**. El ojito junto a la contraseña permite verificar lo digitado.

Si el registro es exitoso aparece un cuadro de diálogo emergente (`JOptionPane`) con el mensaje "Cliente registrado correctamente" o "Proveedor registrado correctamente", según el tipo de cuenta; al cerrarlo, la ventana conmuta sola a la carta de Login para iniciar sesión con la cuenta recién creada. Si hay un error de validación, el mensaje se muestra en rojo bajo el botón y el usuario permanece en el Registro.

**Inicio de sesión.** La aplicación arranca mostrando la ventana amplia con el sidebar y, en el centro, el Login. Con el correo y la contraseña de un usuario ya registrado, **INICIAR SESIÓN** valida las credenciales contra el repositorio; si son correctas aparece "¡Bienvenido, &lt;nombre&gt;! Has ingresado a tu cuenta correctamente" y, al cerrar el mensaje, la ventana de Login/Registro se cierra y se abre, maximizada, la ventana de trabajo del rol correspondiente (Panel de Cliente o Panel de Proveedor) con un botón **"Cerrar sesión"** que la destruye y vuelve a mostrar el Login. Si el correo o la contraseña no coinciden, el mensaje de error aparece en rojo bajo el botón. Dentro de la ventana Login/Registro, tanto los botones del sidebar ("Log In"/"Register") como los enlaces de cada formulario ("¿No tienes cuenta? Regístrate" / "¿Ya tienes cuenta? Inicia sesión") alternan entre las dos cartas sin abrir ventanas nuevas.

---

## Hoja de ruta

| Incremento | Alcance |
|---|---|
| 1 ✅ | Registro de usuarios |
| 2 ✅ | Iteración de interfaz sobre CRUD **Create** (registro de usuarios) y **Read** (consulta de credenciales por correo vía Login, con redirección dinámica por rol): `MainFrame` con sidebar + `CardLayout` (Login/Registro), `view` modularizado por contexto, fábrica de componentes, auto-redirección a Login tras registro y confirmaciones con `JOptionPane` |
| 3 ✅ | Catálogo, carrito y compra (Cliente); CRUD e indicadores (Proveedor); edición de perfil; MongoDB Atlas; BCrypt; acceso con Google; sesión recordada; correos con Resend; catálogo sincronizado (Observer) |
| Final ✅ | Doble rol con empresa/marca, reseñas con estrellas, datos de envío obligatorios, imágenes en la nube, tema claro/oscuro en caliente, banner y pop-up promocional, ejecutable `.exe` y documentación técnica en `docs/` |

La hoja de ruta original preveía después del Incremento 2 un listado tabular de usuarios, su edición y eliminación, persistencia, catálogo y pedidos como incrementos separados; se reorganizó en torno a los dos roles y quedó como arriba. Lo que no se implementó del diagrama conceptual inicial (pasarela de pago, logística, PQR, chat, devoluciones) está explicado en [arquitectura.md](docs/01_Arquitectura_y_Diseno/arquitectura.md).

---

## Limitaciones conocidas

- Sin MongoDB Atlas (sin `config.properties` o sin red) los datos viven en memoria y se pierden al cerrar. Con Atlas persisten.
- Las contraseñas se guardan cifradas con BCrypt; las heredadas en claro se siguen aceptando para no dejar fuera a cuentas antiguas.
- La sesión recordada puede suplantarse escribiendo a mano su archivo (ver `CLAUDE.md`, "Sesión recordada").
- No hay pasarela de pago real ni logística de despacho.
- Los hallazgos de presentación encontrados al probar ya están corregidos: ver [evidencias de pruebas](docs/04_Pruebas_y_Casos/evidencias_de_pruebas.md), §8.

---

## Autoría

**Guillermo Luis Sandoval Ricardo**
**Sebastian Uparela**
**Juan Guillerme Noble**
Ingeniería de Sistemas — Quinto semestre
Corporación Universitaria Remington
Sahagún, Córdoba
