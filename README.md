# Plataforma de Comercio Electrónico

Aplicación de escritorio en Java Swing con arquitectura MVC, desarrollada por incrementos funcionales.

**Estado actual:** Incremento 2 — iteración y refinamiento de la interfaz sobre el registro de usuarios del Incremento 1: `MainFrame` amplio con panel lateral (sidebar) de navegación, paleta oscura morada estilo SaaS/Tech con acento violeta, `CardLayout` para Login/Registro, autenticación con redirección por rol, confirmaciones con `JOptionPane` y un ciclo de vida estricto de ventanas (`dispose()` al pasar de una ventana a otra, nunca ventanas ocultas y vivas). La operación de negocio sigue siendo la misma (CREAR usuarios); este incremento no agrega un caso de uso nuevo, refina cómo se presenta y se navega el que ya existía. Tras una revisión de calidad se corrigieron además tres puntos de confiabilidad y estabilidad (ver [Correcciones de calidad y mantenibilidad](#correcciones-de-calidad-y-mantenibilidad)): correo único al registrarse, cierre seguro de los paneles de rol y ventanas de tamaño fijo.

---

## Descripción

El sistema registra los dos actores de la plataforma: **Clientes**, que compran y aportan una dirección de envío, y **Proveedores**, que abastecen el catálogo y aportan el NIT de su empresa. Ambos comparten identificación, nombres, correo y contraseña, por lo que se modelan como subclases de una clase abstracta común.

La persistencia de este incremento es en memoria, diseñada para reemplazarse por una base de datos sin modificar el resto del sistema.

---

## Requisitos

- JDK 25 o superior (el proyecto declara `javac.source=26`; compila también con el JDK 25)
- Apache NetBeans en una versión compatible con ese JDK
- Proyecto tipo *Java with Ant → Java Application*

No requiere librerías externas.

---

## Estructura del proyecto

```
ComercioElectronico/
├── README.md                         ← este documento
├── build.xml                         ← generado por NetBeans
├── nbproject/                        ← configuración del IDE
└── src/
    ├── app/
    │   └── Main.java                 ← ensamblador; ver nota de paquete más abajo
    ├── controller/
    │   ├── UsuarioController.java    ← caso de uso "Registrar usuario"
    │   ├── LoginController.java      ← caso de uso "Iniciar sesión"
    │   ├── PoliticaPassword.java     ← reglas y fortaleza de la contraseña
    │   └── ControlIntentosFallidos.java ← 3 fallos seguidos → bloqueo de 30 s
    ├── model/
    │   ├── entity/
    │   │   ├── Usuario.java          ← clase abstracta
    │   │   ├── Cliente.java
    │   │   └── Proveedor.java
    │   └── repository/
    │       ├── IUsuarioRepository.java   ← interfaz
    │       └── UsuarioRepositoryImpl.java
    ├── resources/
    │   ├── images/
    │   │   ├── logo.png                ← emblema circular de la marca (PNG con transparencia)
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
        │   ├── ClientDashboardFrame.java  ← ventana del Cliente (1024×680 fija), tras login
        │   └── ProviderDashboardFrame.java← ventana del Proveedor (1024×680 fija), tras login
        └── factory/
            ├── IComponentesFactory.java          ← interfaz de la fábrica (la puerta de entrada)
            ├── ComponentesSwingFactory.java      ← implementación "SaaS/Tech Morado"
            ├── components/                       ← controles compuestos del formulario
            │   ├── CampoTextoConIcono.java       ← campo de texto + ícono de contexto
            │   ├── CampoPasswordConToggle.java   ← ícono + campo + botón "ojo"
            │   └── SelectorSegmentado.java       ← control segmentado (reemplaza al JComboBox)
            ├── effects/                          ← movimiento y sonido
            │   ├── IndicadorCarga.java           ← indicador circular (giratorio o de avance)
            │   ├── AnimacionConfeti.java         ← celebración sobre el glassPane
            │   └── SonidoExito.java              ← reproduce exito.wav con AudioSystem/Clip
            ├── icons/                            ← todo lo que se dibuja o se carga como ícono
            │   ├── IconoCampo.java               ← glifos de línea de cada campo
            │   ├── IconoOjo.java                 ← mostrar/ocultar contraseña
            │   ├── IconoPunto.java               ← punto del semáforo de fortaleza
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

1. Crear un proyecto *Java Application* sin clase principal.
2. Sobre **Source Packages**, crear los paquetes escribiendo el nombre completo con puntos: `app`, `model.entity`, `model.repository`, `view.core`, `view.auth`, `view.dashboard`, `view.factory`, `controller` y `resources.images`.
3. Copiar cada archivo en el paquete que indica su primera línea (`Main.java` va en `app`, ya no en la raíz).
4. Copiar el logo en `resources.images`. Si el archivo no es `.png`, ajustar la extensión en la constante `RUTA_LOGO` y en el método `cargarImagenLogo()` de `ComponentesSwingFactory` (único lugar que carga el logo; todas las vistas piden `fabrica.crearLogo(tamano)`).
5. Ejecutar `app.Main` con Shift+F6 (o configurar `app.Main` como clase principal del proyecto).

Si aparecen caracteres extraños, configurar la codificación del proyecto en UTF-8.

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
                                                         (JFrame propio, tamaño fijo; según usuario.getTipoCuenta())
                                                              │
                                                    "Cerrar sesión" o la X: dispose() de sí mismo +
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
  abre, centrada y con tamaño fijo, `ClientDashboardFrame` o
  `ProviderDashboardFrame`. Ninguna recibe el `Usuario` completo, solo su
  nombre. Cada dashboard trae un botón **"Cerrar sesión"** (y su X hace lo
  mismo) que se destruye a sí mismo y vuelve a abrir
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

## Correcciones de calidad y mantenibilidad

Una revisión del Incremento 2 con criterios de ISO/IEC 25010 encontró tres
defectos. Se corrigieron sin agregar clases ni dependencias nuevas y sin
romper la separación de capas: cada arreglo quedó en la capa a la que
pertenece.

| Problema | Consecuencia | Corrección | Característica de calidad |
|---|---|---|---|
| Se podían registrar dos cuentas con el mismo correo | El Login siempre encontraba la primera; la segunda nunca podía entrar | Correo único en el controlador y en el repositorio | Adecuación funcional, fiabilidad |
| La X de los paneles de rol usaba `DISPOSE_ON_CLOSE` | Se cerraba la ventana sin volver al Login y el programa quedaba vivo sin ventanas | La X sigue el mismo camino que "Cerrar sesión" | Fiabilidad, usabilidad |
| Las ventanas se podían estirar o maximizar | El diseño, medido para un tamaño concreto, se descomponía | Tamaño fijo y `setResizable(false)` en las tres ventanas | Usabilidad, estabilidad visual |

### 1. Confiabilidad de los datos: correo único

El correo es la credencial del Login, así que tiene que identificar a una
sola cuenta. La regla se aplica en **dos capas, a propósito**:

- **`UsuarioController`** consulta `buscarPorCorreo(correo)` antes de
  construir la entidad. Es el único que puede dar un mensaje exacto: "Este
  correo ya está registrado en el sistema."
- **`UsuarioRepositoryImpl.registrar(...)`** rechaza de nuevo cualquier
  correo repetido, sin distinguir mayúsculas. La regla quedó escrita en el
  contrato de `IUsuarioRepository`, así que la futura implementación con
  base de datos está obligada a cumplirla aunque el controlador cambie.

No se añadió ningún método nuevo a la interfaz: `buscarPorCorreo(...)` ya
existía para el Login y responde exactamente la pregunta que hacía falta.

### 2. Ciclo de vida de las ventanas: cierre seguro

`ClientDashboardFrame` y `ProviderDashboardFrame` usan ahora
`DO_NOTHING_ON_CLOSE` y un `WindowListener` cuyo `windowClosing` llama al
mismo método privado `cerrarSesion(...)` que el botón. Hay **un solo camino
de salida**: se destruye la ventana con `dispose()` (se liberan sus
recursos) y se vuelve a abrir el Login con el mismo repositorio.

Se eligió `windowClosing` y no `windowClosed` a propósito: el `dispose()`
del botón también dispara `windowClosed`, y la vuelta al Login se habría
ejecutado dos veces. Verificado: tras pulsar la X, la acción de volver al
Login se ejecuta exactamente una vez y la ventana deja de ser
`displayable`.

Para terminar el programa se cierra la ventana de Login/Registro, que
conserva `EXIT_ON_CLOSE`.

### 3. Estabilidad visual: tamaño fijo

| Ventana | Tamaño |
|---|---|
| `MainFrame` (Login/Registro) | 950×650 |
| `ClientDashboardFrame` | 1024×680 |
| `ProviderDashboardFrame` | 1024×680 |

Las tres llaman a `setResizable(false)`. En Windows eso desactiva además el
botón de maximizar: quedan solo minimizar y cerrar. El formulario de
Registro está medido contra el alto de `MainFrame`, y el tamaño mínimo
anterior (720×520) permitía achicar la ventana hasta recortarlo. Los
paneles de rol dejaron de abrirse maximizados: su contenido está compuesto
para un tamaño concreto, y se centran en la pantalla al abrirse.

### Qué gana el código

- **Más robusto:** una regla de negocio crítica ya no depende de que un solo
  punto del código se acuerde de ella, y ninguna ventana puede dejar la
  aplicación colgada en segundo plano.
- **Más fácil de mantener:** el cierre de cada panel está en un único
  método; cualquier cambio futuro en cómo se cierra sesión se hace en un
  sitio.
- **Mismo diseño de capas:** el modelo sigue sin importar Swing, la vista
  sigue sin importar el modelo y no se agregó ninguna dependencia entre
  capas.
- **Documentado donde se lee:** el porqué de cada decisión quedó en el
  Javadoc de las clases tocadas y en `CLAUDE.md`, para que nadie lo revierta
  sin saber qué rompe.

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
| **O** | Agregar un tipo `Administrador` solo requiere una subclase nueva. Agregar un tema visual nuevo solo requiere otra clase que implemente `IComponentesFactory`. Agregar una carta nueva a `MainFrame` solo requiere llamar `mainFrame.agregarCarta(...)` desde `app.Main.mostrarVentanaPrincipal(...)`; agregar un dashboard de rol nuevo es otra clase `JFrame` más, sin tocar `MainFrame` ni `LoginController` fuera de un caso más en su bifurcación. |
| **L** | Cualquier subclase de `Usuario` funciona donde se espera un `Usuario`; cualquier implementación de `IComponentesFactory` funciona donde una vista espera una fábrica; cualquier implementación de `INavegador` funciona donde un panel espera poder cambiar de carta. |
| **I** | `IUsuarioRepository` expone solo lo que los casos de uso actuales necesitan (`registrar`, `buscarPorCorreo`). `IRegistroUsuarioView`/`ILoginView` solo exponen lo que su controlador usa. `INavegador` es un contrato de un único método. |
| **D** | Los controladores dependen de `IRegistroUsuarioView`/`ILoginView`, `IUsuarioRepository` e `IComponentesFactory` (abstracciones); las implementaciones concretas se nombran únicamente en `app.Main` (o, en el caso de las dos ventanas de destino del login y de la reapertura tras cerrar sesión, en el propio `LoginController`, que es el caso de uso que decide cuál abrir). |

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
especial**. Cuenta como especial **cualquier carácter que no sea letra ni
número**, incluidos los del teclado en español (`¿`, `¡`, `°`, `´`, `€`) y el
espacio; la `ñ` y las vocales acentuadas son letras, no símbolos. Antes se
comparaba contra una lista fija de símbolos del teclado inglés, así que
escribir "Clave2026¿" se marcaba como insegura sin explicar por qué. No hay
longitud máxima, y es deliberado: un tope corto empuja a la gente hacia
contraseñas peores sin aportar seguridad. Los mensajes de error dicen
exactamente qué falta ("debe incluir al menos un número"), en vez de repetir
la lista completa de reglas.

**Semáforo de fortaleza en vivo.** Junto a la etiqueta "Contraseña" del
registro hay un punto de color que se actualiza con cada tecla:

| Color | Significado | Cuándo |
|---|---|---|
| Rojo | Insegura | No cumple los requisitos (el registro la rechazará) |
| Ámbar | Medianamente segura | Cumple los requisitos, pero suma menos de 4 puntos de complejidad |
| Verde | Segura | Cumple los requisitos y suma 4 o más puntos |

**El verde se gana por complejidad, no solo por longitud.** Una contraseña
válida suma un punto por cada rasgo que la hace más difícil de adivinar (el
máximo son 5):

| Punto | Se gana cuando |
|---|---|
| Longitud holgada | Tiene 10 caracteres o más |
| Longitud larga | Tiene 14 caracteres o más |
| Mezcla de mayúsculas y minúsculas | Incluye alguna minúscula |
| Variedad real | Usa 10 caracteres distintos o más |
| Más de un símbolo | Incluye dos o más caracteres especiales |

Mayúscula, número y símbolo no suman puntos: son obligatorios, así que toda
contraseña válida los tiene y no distinguen a una de otra. Así,
`Aaaaaaaaaaaa1!` (14 caracteres, pero repetida) se queda en ámbar, mientras
que `Clave.2026#xy` (13, variada y con dos símbolos) llega a verde.

Antes el nivel se decidía con un único corte de longitud (12 caracteres) y el
nivel intermedio casi nunca se veía: al escribir seguido, la contraseña solía
completar los requisitos cuando ya pasaba de esos 12 caracteres, así que el
indicador saltaba de rojo a verde sin pasar por ámbar.

Validación y semáforo salen de la **misma** clase
(`controller.PoliticaPassword`), así que no pueden contradecirse: lo que el
punto pinta en rojo es exactamente lo que el registro va a rechazar. El punto
vive en la fila de la etiqueta, no en una fila propia, para no consumir alto
en un formulario que ya va justo de espacio.

---

## Validaciones

Implementadas en `UsuarioController.validar()`: todos los campos obligatorios, identificación solo numérica, formato de correo válido y contraseña según `PoliticaPassword` (mínimo 7 caracteres, con mayúscula, número y carácter especial). Después, `UsuarioController` rechaza un correo ya registrado ("Este correo ya está registrado en el sistema."), y el repositorio rechaza de nuevo tanto identificaciones como correos duplicados, sin distinguir mayúsculas.

Para aceptar identificaciones alfanuméricas, eliminar la condición `identificacion.matches("\\d+")`.

---

## Manual de uso

**Registro.** Diligenciar los campos (cada uno guía con un texto fantasma gris que desaparece al enfocar), seleccionar el tipo de cuenta (la etiqueta del último campo cambia sola entre "Dirección de envío" y "NIT de la empresa") y presionar **REGISTRAR USUARIO**. El ojito junto a la contraseña permite verificar lo digitado.

Si el registro es exitoso aparece un cuadro de diálogo emergente (`JOptionPane`) con el mensaje "Cliente registrado correctamente" o "Proveedor registrado correctamente", según el tipo de cuenta; al cerrarlo, la ventana conmuta sola a la carta de Login para iniciar sesión con la cuenta recién creada. Si hay un error de validación, el mensaje se muestra en rojo bajo el botón y el usuario permanece en el Registro.

**Inicio de sesión.** La aplicación arranca mostrando la ventana amplia con el sidebar y, en el centro, el Login. Con el correo y la contraseña de un usuario ya registrado, **INICIAR SESIÓN** valida las credenciales contra el repositorio; si son correctas aparece "¡Bienvenido, &lt;nombre&gt;! Has ingresado a tu cuenta correctamente" y, al cerrar el mensaje, la ventana de Login/Registro se cierra y se abre, centrada, la ventana de trabajo del rol correspondiente (Panel de Cliente o Panel de Proveedor) con un botón **"Cerrar sesión"** que la destruye y vuelve a mostrar el Login; la X de esa ventana hace lo mismo. Para salir del programa se cierra la ventana de Login/Registro. Ninguna de las ventanas se puede redimensionar ni maximizar. Si el correo o la contraseña no coinciden, el mensaje de error aparece en rojo bajo el botón. Dentro de la ventana Login/Registro, tanto los botones del sidebar ("Log In"/"Register") como los enlaces de cada formulario ("¿No tienes cuenta? Regístrate" / "¿Ya tienes cuenta? Inicia sesión") alternan entre las dos cartas sin abrir ventanas nuevas.

---

## Hoja de ruta

| Incremento | Alcance |
|---|---|
| 1 ✅ | Registro de usuarios |
| 2 ✅ | Iteración de interfaz sobre CRUD **Create** (registro de usuarios) y **Read** (consulta de credenciales por correo vía Login, con redirección dinámica por rol): `MainFrame` con sidebar + `CardLayout` (Login/Registro), `view` modularizado por contexto, fábrica de componentes, auto-redirección a Login tras registro y confirmaciones con `JOptionPane` |
| 3 | Interfaz de listado tabular (`JTable`) de los usuarios registrados |
| 4 | Edición y eliminación |
| 5 | Persistencia en base de datos |
| 6 | Catálogo de productos |
| 7 | Carrito y pedidos |

El incremento 5 se resuelve creando `UsuarioRepositoryJDBC implements IUsuarioRepository` y cambiando una línea en `app.Main`, sin modificar la vista ni el controlador.

---

## Limitaciones conocidas

Los datos se pierden al cerrar la aplicación y las contraseñas se guardan en texto plano. Ambas cosas corresponden a incrementos posteriores. El contador de intentos fallidos vive en la ventana de Login, no en la cuenta: reiniciar la aplicación lo pone a cero, algo aceptable en una aplicación de escritorio de un solo usuario.

---

## Autoría

**Guillermo Luis Sandoval Ricardo**
Ingeniería de Sistemas — Quinto semestre
Corporación Universitaria Remington
Sahagún, Córdoba
