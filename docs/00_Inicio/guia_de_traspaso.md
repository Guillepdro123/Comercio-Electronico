# Guía de traspaso del proyecto

**Lee esto primero.** Es el punto de entrada para quien recibe el proyecto sin
haber participado en él: qué es, en qué estado está, cómo ponerlo a funcionar,
qué cuentas hay que traspasar y dónde está cada cosa. Lo demás de `docs/`
profundiza en cada tema.

| | |
|---|---|
| Proyecto | Plataforma de Comercio Electrónico (aplicación de escritorio) |
| Autor original | Guillermo Luis Sandoval Ricardo — Ingeniería de Sistemas, Corporación Universitaria Remington, Sahagún (Córdoba) |
| Estado al 04/10/2026 | Funcional y empaquetado como `.exe`. Pendientes en §8 |
| Tecnología | Java 26, Swing con FlatLaf, MongoDB Atlas, NetBeans + Ant |

---

## 1. Qué es, en un minuto

Una tienda en línea de escritorio. Cualquier persona se registra y compra:
busca en el catálogo, agrega al carrito, compra, recibe un correo de
confirmación y califica lo que compró. Una cuenta de **Proveedor** además
vende: publica productos con la marca de su empresa y ve sus indicadores de
venta. Los datos viven en **MongoDB Atlas**, así que varios equipos comparten
el mismo catálogo y lo ven cambiar en tiempo real.

| Tienda | Panel del vendedor |
|---|---|
| ![Tienda](../04_Pruebas_y_Casos/evidencias/capturas/08_tienda_banner.png) | ![Panel](../04_Pruebas_y_Casos/evidencias/capturas/21_panel_proveedor.png) |

Todas las pantallas, con lo que demuestra cada una, están en
[evidencias_de_pruebas.md](../04_Pruebas_y_Casos/evidencias_de_pruebas.md).

---

## 2. Mapa de la documentación

| Si necesitas… | Lee |
|---|---|
| Entender cómo está construido y por qué | [01 · Arquitectura y diseño](../01_Arquitectura_y_Diseno/arquitectura.md) |
| Saber qué hay guardado en la base | [02 · Esquema NoSQL](../02_Base_de_Datos/esquema_nosql.md) |
| Saber qué se pidió y qué riesgos hay | [03 · Requisitos y riesgos](../03_Gestion_y_Requisitos/requisitos_y_riesgos.md) |
| Saber qué se probó y cómo | [04 · Casos de uso y pruebas](../04_Pruebas_y_Casos/documentacion_pruebas.md) y [evidencias](../04_Pruebas_y_Casos/evidencias_de_pruebas.md) |
| Generar el ejecutable | [05 · Guía del ejecutable](../05_Empaquetado/guia_ejecutable.md) |
| Conocer **todas las decisiones técnicas y el porqué de cada una** | [`CLAUDE.md`](../../CLAUDE.md) en la raíz (ver §6) |
| Ver la historia por incrementos | [`README.md`](../../README.md) en la raíz |

---

## 3. Poner el proyecto a funcionar (30 minutos)

### 3.1 Instalar

1. **Apache NetBeans** (versión reciente). Trae su propio **JDK 26**, que es
   el que necesita el proyecto: las clases se compilan para Java 26 y un JDK
   anterior no las abre.
2. Nada más: las 8 librerías están dentro del proyecto, en `lib/`. **No hay
   Maven ni `pom.xml`**; el proyecto es Ant.

### 3.2 Abrir y ejecutar

1. NetBeans → *File → Open Project* → la carpeta `Comercio Electronico`.
2. Clic derecho sobre el proyecto → **Clean and Build**.
3. **Run** (`F6`). Arranca con `app.Main`.

**Sin más configuración la aplicación funciona**, con los datos en memoria:
trae 10 productos de ejemplo, las cuentas se crean en el momento y todo se
pierde al cerrar. En la consola de NetBeans verás:

```text
Sin MongoDB (Falta config.properties o su mongodb.uri sigue sin rellenar.); se usa el almacén en memoria.
```

### 3.3 Conectar con la base real

1. Copia `config.properties.ejemplo` como `config.properties` en la raíz.
2. Rellena `mongodb.uri` y `mongodb.base` (y, si los tienes, Resend y Google).
3. En MongoDB Atlas → *Network Access*, autoriza la IP de tu equipo.
4. Ejecuta de nuevo. Debe decir `Conectado a MongoDB Atlas.`

`config.properties` **nunca se sube ni se entrega**: lleva contraseñas. Está en
`.gitignore`.

### 3.4 Comprobar que todo está bien

En *Test Packages*, ejecuta con **Run File** cualquiera de los 11 programas
`Verificacion*.java`: cada uno termina en `TODO CORRECTO`. La salida de
referencia está en `docs/04_Pruebas_y_Casos/evidencias/verificaciones/`.

---

## 4. Cuentas y credenciales que hay que traspasar

**Esto es lo que se pierde si quien se va no lo entrega.** El código no sirve
de nada sin acceso a la base de datos.

| Servicio | Para qué | Qué hay que traspasar | Si no se traspasa |
|---|---|---|---|
| **MongoDB Atlas** | Base de datos compartida (usuarios, productos, pedidos, reseñas, imágenes) | Invitar al nuevo responsable como *Project Owner* de la organización/proyecto en Atlas. Crear para él su propio usuario de base (no compartir el existente) | Se pierden todos los datos reales. La aplicación seguiría abriendo, pero solo en memoria |
| **Resend** | Envío de correos (bienvenida, confirmación, alerta de venta) | Acceso a la cuenta y al dominio verificado `misupertiendajava.cyou`; generar una clave nueva (`re_...`) | Los correos dejan de enviarse: el sistema los imprime en consola y todo lo demás sigue igual |
| **Dominio `misupertiendajava.cyou`** | Remitente verificado de los correos | Acceso al registrador del dominio y su renovación | Si el dominio vence, Resend deja de aceptar el remitente |
| **Google Cloud** | Botón "Continuar con Google" | Acceso al proyecto de Google Cloud que tiene el cliente OAuth "App de escritorio" (su ID y secreto ya están en el `config.properties` del equipo original). Mientras la pantalla de consentimiento esté en modo *Prueba*, cada usuario nuevo debe agregarse como *usuario de prueba* | El botón no aparece; el acceso con contraseña funciona igual |

**Lista de comprobación del traspaso:**

- [ ] El nuevo responsable entra a Atlas con su propia cuenta y ve el clúster.
- [ ] Tiene su propio `config.properties` y la aplicación le dice `Conectado a MongoDB Atlas.`
- [ ] Se rotaron las contraseñas/claves que conocía quien se fue (usuario de base, clave de Resend).
- [ ] Tiene acceso a Resend y al dominio, con la fecha de renovación anotada.
- [ ] Tiene acceso al proyecto de Google Cloud (si se va a mantener el acceso con Google).
- [ ] Tiene una copia del proyecto completo (ver §7: no hay control de versiones).

---

## 5. Cómo está organizado el código

Arquitectura MVC con una capa de casos de uso en medio. La regla de oro: **la
vista nunca importa el modelo**, y **el modelo y los casos de uso nunca
importan Swing**.

| Paquete | Qué contiene | Toca aquí si… |
|---|---|---|
| `app` | `Main` (arranque y ensamblado) e `Infraestructura` | Cambias qué base, notificador o tema se usa al arrancar |
| `view.auth`, `view.core` | Login, Registro, Perfil, ventana principal | Cambias esas pantallas |
| `view.dashboard.cliente` / `.proveedor` | Tienda y panel del vendedor, con sus contratos (`I...View`) | Cambias esas pantallas |
| `view.factory` | **Toda** la apariencia: colores (`tema.Paleta`), botones, campos, diálogos, íconos, banner | Cambias cómo se ve algo. Ninguna vista crea sus colores |
| `controller` | Un controlador por pantalla | Cambias qué pasa al pulsar algo |
| `aplicacion.*` | Reglas de negocio: cuenta, acceso, seguridad, catálogo, compra, reseña | Cambias una regla (qué es un precio válido, quién reseña…) |
| `model.entity` | `Usuario`, `Cliente`, `Proveedor`, `Producto`, `Pedido`, `Resena`, `Carrito` | Cambias el dominio |
| `model.repository` | Contratos de persistencia + implementaciones `memoria` y `mongo` | Cambias cómo se guarda algo |
| `model.repository.mongo.adapter` | Conversión objeto ↔ documento de MongoDB | Cambias un campo guardado (**cuidado**: ver §6) |
| `service` | Configuración, sesión recordada, Google, correo, imágenes | Cambias integraciones externas |
| `observer` | Aviso de cambios del catálogo | — |
| `test/` | 11 verificaciones automáticas y 2 programas manuales de capturas | Agregas o corres pruebas |
| `empaquetado/` | Script del `.exe` e ícono | Generas el ejecutable |

**Recorrido de una compra**, para orientarse: `ClientDashboardFrame` (botón)
→ `ClienteController.confirmarCompra` → `CompraService.confirmar` (reglas) →
`IProductoRepository.descontarStock` + `IPedidoRepository.registrar` →
`ProductoRepositoryMongo` / `PedidoRepositoryMongo` → Atlas. El correo sale
en paralelo por `NotificadorEnSegundoPlano`.

---

## 6. Reglas que no hay que romper

El archivo [`CLAUDE.md`](../../CLAUDE.md) de la raíz es la **bitácora de
decisiones** del proyecto: cada regla con su motivo, y muchas con la medición o
el error que la originó. Se llama así porque también lo usa el asistente de IA
con el que se desarrolló (Claude Code), pero está escrito para personas. Antes
de "arreglar" algo que parece raro, búscalo ahí: casi siempre hay una razón.
Las más importantes:

1. **Cambiar un adaptador es cambiar los datos de Atlas.** Si renombras un
   campo, los documentos ya guardados dejan de leerse.
2. **Nada que viaje por red se ejecuta en el hilo de la interfaz.** Se usa
   `vista.ejecutarEnSegundoPlano(...)`; si no, la ventana se congela.
3. **La compra es todo o nada.** El descuento de stock *es* la comprobación;
   no se pregunta antes "¿hay existencias?" (eso permitía vender dos veces la
   última unidad).
4. **Ninguna credencial en el código.** Todo secreto va en `config.properties`.
5. **Ninguna vista crea colores ni botones a mano**: se piden a
   `IComponentesFactory`. Un tema nuevo es otra `Paleta`.
6. **No se usa `instanceof` para distinguir roles**: se pregunta al objeto
   (`usuario.puedeVender()`).
7. **Los cambios de interfaz se verifican mirándolos** (capturas), no solo
   compilando.
8. **Sin Atlas la aplicación debe seguir abriendo** (almacén en memoria).

---

## 7. Advertencias del estado actual

- **El proyecto no está en un sistema de control de versiones** (no hay Git).
  Vive en una carpeta de OneDrive. Lo primero recomendable es crear un
  repositorio privado (Git + GitHub/GitLab); el `.gitignore` ya existe y
  excluye `config.properties`, `build/` y `dist/`.
- **Requiere Java 26.** El JDK que haya en el `PATH` del equipo puede ser otro
  (en el equipo original había un JDK 25): ejecuta siempre con el de NetBeans,
  o usa el `.exe`, que lleva su propio Java.
- **La sesión recordada se puede suplantar** si alguien escribe en
  `~/.comercio-electronico/session.properties` el correo de otra cuenta.
  Cerrarlo exige un token aleatorio (descrito en `CLAUDE.md`, sección
  "Sesión recordada").

---

## 7b. Datos de demostración

Atlas tiene cargada la cuenta de proveedor **sandoval.guillermo.privado@gmail.com**
(empresa "Global Tech & Home Store", con cédula, NIT y dirección completos) y
su catálogo de **28 productos** en las cinco categorías, con imágenes por URL
(Unsplash, licencia libre). Se cargaron con
`test/manual/SemillaDemostracion.java`, que pasa por los casos de uso de la
aplicación y es idempotente:

- `verificar`: descarga y decodifica las 28 imágenes, sin escribir nada.
- `cargar <contraseña>`: crea la cuenta si falta y publica los productos que
  falten. La contraseña se pasa al ejecutarlo y no está escrita en el código.

**Importante:** una versión de la aplicación anterior al 04/10/2026 (en
NetBeans o en un `.exe` viejo) trata las URL como rutas del disco y, al
arrancar, **las borra** de los productos. Todos los equipos que usen esta base
deben tener el código actual.

## 8. Pendientes

| Pendiente | Tipo | Dónde se describe |
|---|---|---|
| 7 casos de prueba sin ejecutar (sincronización entre dos equipos, sesión recordada, cierre de sesión, carrito vacío, HTML en comentarios, proveedor sin empresa) | Pruebas | [Pruebas §6](../04_Pruebas_y_Casos/documentacion_pruebas.md) |
| Probar el acceso con Google de punta a punta contra Google real (el flujo se verificó contra un Google simulado; las credenciales ya están configuradas) | Prueba | `CLAUDE.md`, sección "Acceso con Google" |
| Pasarela de pago, logística de despacho, PQR, chat, devoluciones | Fuera del alcance actual | [Arquitectura §1.2](../01_Arquitectura_y_Diseno/arquitectura.md) |
| Token para la sesión recordada | Seguridad | `CLAUDE.md` |

---

## 9. Glosario

| Término | Significado aquí |
|---|---|
| Atlas | MongoDB Atlas, la base de datos en la nube |
| Almacén en memoria | Repositorios que guardan en listas; se usan sin Atlas y en las pruebas |
| Caso de uso / servicio | Clase de `aplicacion.*` con una regla de negocio (`CompraService`…) |
| Contrato de vista | Interfaz `I...View` que el controlador usa sin conocer Swing |
| Fábrica | `IComponentesFactory`: crea todos los componentes con estilo |
| Paleta | Conjunto de colores de un tema (claro u oscuro) |
| Doble rol | El Proveedor también compra |
| Texto fantasma | Texto gris de ayuda dentro de un campo vacío |
| Observer | Mecanismo que avisa a las tiendas abiertas cuando cambia el catálogo |
| Change Stream | Función de MongoDB que avisa de cambios hechos desde otro equipo |
| Resend | Servicio externo que envía los correos |
| `.exe` / app-image | Ejecutable de Windows con su propio Java, generado con `jpackage` |
