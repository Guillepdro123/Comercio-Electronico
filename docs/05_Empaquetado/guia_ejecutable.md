# Guía: generar `ComercioElectronico.exe`

Resultado: una carpeta con `ComercioElectronico.exe`, **su propio Java** y
todas las librerías, más un acceso directo **"Comercio Electronico"** en el
escritorio con el ícono del proyecto. El equipo de destino **no necesita
tener Java instalado**.

Herramienta elegida: **jpackage** (incluida en el JDK). Se descartó Launch4j
como opción principal porque solo envuelve el JAR y sigue exigiendo un Java
instalado en el equipo (ver §8).

> Probado en este proyecto el 04/10/2026 en una carpeta temporal: el `.exe`
> generado arranca, lleva el ícono y lee el `config.properties` que tiene al
> lado, **incluso lanzado desde otra carpeta de trabajo**. La creación del
> acceso directo del escritorio es lo único que no se ejecutó en esa prueba
> (se omitió para no tocar el escritorio).

### El runtime lleva todos los módulos del JDK

El script pasa a `jpackage` la lista completa de módulos del JDK 26 de NetBeans
(salvo `jdk.jlink` y `jdk.jpackage`), y al terminar comprueba que estén
`java.desktop`, `jdk.naming.dns` y `jdk.localedata`. Por qué:

- **Con los módulos por defecto de `jpackage`, el `.exe` no conectaba a Atlas.**
  Las URI `mongodb+srv://` se resuelven con `jdk.naming.dns`, que `jpackage`
  no incluye. El error era *Failed looking up TXT record*, la aplicación caía
  al almacén en memoria y el acceso con Google pedía el rol a cuentas que ya
  existían en Atlas. Desde NetBeans, con el JDK completo, todo funcionaba.
- **Sin `jdk.localedata` los precios salían como `$ 1,181,000`** en vez de
  `$ 1.181.000`.
- **`--add-modules ALL-DEFAULT` no es fiable:** según el entorno de la consola
  (por ejemplo, con `JAVA_HOME` apuntando a otro JDK) producía un runtime de 6
  módulos sin Swing y el `.exe` no abría.

Además, si `config.properties` tiene una URI de Atlas y la conexión falla, la
aplicación ya no cae en silencio a la memoria: muestra un **aviso** al abrir
con el motivo. Sin configuración sigue abriendo sin avisar, como siempre.

### Dónde busca la aplicación `config.properties`

`service.config.Configuracion` lo busca en este orden y usa el primero que
encuentra:

1. La **carpeta de trabajo** (la raíz del proyecto al ejecutar desde NetBeans).
2. La **carpeta del `.exe`** (el lanzador de jpackage informa su ruta en la
   propiedad `jpackage.app-path`).
3. La **carpeta del JAR** (al ejecutar `java -jar` o un `.exe` de Launch4j).

Por eso basta con dejar el archivo junto al `.exe`: da igual cómo se abra.
También se acepta un archivo guardado con BOM (marca UTF-8 al inicio), como
lo dejan PowerShell 5.1 y el Bloc de notas antiguo: antes, esa marca hacía
que la aplicación ignorara la URI de Atlas sin avisar.

---

## 1. Requisitos

| Requisito | Dónde está | Por qué |
|---|---|---|
| **JDK 26** | El que trae NetBeans: `C:\Program Files\Apache NetBeans\jdk` | Las clases se compilan para Java 26. **No sirve el JDK 25 del `PATH`**: empaquetaría un Java que no puede abrir las clases |
| Proyecto compilado | `dist\Comercio_Electronico.jar` + `dist\lib\` | Lo genera NetBeans con *Clean and Build* |
| Ícono | `empaquetado\comercio.ico` (ya incluido, 7 tamaños de 16 a 256 px) | Generado a partir de `src/resources/images/logo.png` |
| Script | `empaquetado\crear-exe.ps1` (ya incluido) | Automatiza los pasos 3 a 5 |
| `config.properties` | Raíz del proyecto (no versionado) | Conexión a MongoDB Atlas, Resend y Google |

**Las 8 dependencias van dentro del `.exe` automáticamente**: NetBeans las
copia a `dist\lib\` y jpackage empaqueta toda la carpeta `dist`.

| Librería | Uso |
|---|---|
| `flatlaf-3.7.2.jar` | Look and Feel moderno |
| `miglayout-core-11.4.2.jar`, `miglayout-swing-11.4.2.jar` | Formularios |
| `mongodb-driver-sync-5.12.0.jar`, `mongodb-driver-core-5.12.0.jar`, `bson-5.12.0.jar`, `bson-record-codec-5.12.0.jar` | MongoDB Atlas |
| `jbcrypt-0.4.jar` | Cifrado de contraseñas |

Las imágenes del programa (logo, íconos, GIF, ilustraciones de productos,
sonido) van **dentro del JAR**; las imágenes que suben los proveedores están
en **MongoDB Atlas**. Ninguna depende de una ruta del disco.

---

## 2. Paso 1 — Compilar en NetBeans

1. Abre el proyecto en NetBeans.
2. Clic derecho sobre el proyecto → **Clean and Build** (o `Shift + F11`).
3. Comprueba que existan:
   - `dist\Comercio_Electronico.jar`
   - `dist\lib\` con los 8 `.jar`

---

## 3. Paso 2 — Preparar `config.properties`

1. Si no lo tienes, copia `config.properties.ejemplo` como `config.properties`
   en la raíz del proyecto.
2. Rellena al menos:
   ```properties
   mongodb.uri=mongodb+srv://USUARIO:CONTRASENA@cluster0.xxxxx.mongodb.net/
   mongodb.base=comercio_electronico
   ```
   (Resend y Google son opcionales: sin ellos la aplicación funciona, los
   correos se imprimen por consola y el botón de Google no aparece).
3. **En MongoDB Atlas → Security → Network Access**, autoriza la IP del equipo
   donde se va a ejecutar el `.exe` (*Add Current IP Address*). Para la
   sustentación, si no se conoce la IP del aula, se puede agregar
   temporalmente `0.0.0.0/0` y **quitarla después**.

> **Seguridad:** este archivo lleva la contraseña de la base. Nunca se sube al
> repositorio ni se entrega en el ZIP del código. Usa en Atlas un usuario con
> permisos solo de lectura y escritura sobre `comercio_electronico`.

---

## 4. Paso 3 — Generar el `.exe` con el script

En la carpeta del proyecto, abre PowerShell y ejecuta:

```powershell
powershell -ExecutionPolicy Bypass -File .\empaquetado\crear-exe.ps1
```

El script hace todo esto:

1. Busca un `jpackage` de **Java 26 o superior** (primero el de NetBeans) e
   ignora, avisando, cualquier versión anterior.
2. Comprueba que existan el JAR, `dist\lib` y el ícono.
3. Ejecuta `jpackage --type app-image` → crea
   `%LOCALAPPDATA%\Programs\ComercioElectronico\` (~170 MB, con Java dentro).
4. Copia `config.properties` **junto al `.exe`**.
5. Crea el acceso directo **"Comercio Electronico"** en el escritorio y en el
   **menú Inicio** ("Todas las aplicaciones"), con *Iniciar en* apuntando a
   esa carpeta. Para tenerlo en la barra de tareas: clic derecho sobre la
   aplicación en el menú Inicio → **Anclar a la barra de tareas**. Windows 11
   no permite que un script lo ancle por el usuario.

Salida esperada (resumida):

```text
jpackage: C:\Program Files\Apache NetBeans\jdk\bin\jpackage.exe
Empaquetando en C:\Users\<usuario>\AppData\Local\Programs\ComercioElectronico ...
config.properties copiado junto al .exe (conexion a MongoDB Atlas).
AVISO: ese archivo lleva credenciales. No compartas la carpeta tal cual.
Acceso directo creado en el escritorio.

Listo: C:\Users\<usuario>\AppData\Local\Programs\ComercioElectronico\ComercioElectronico.exe
```

**Opciones del script:**

| Opción | Efecto |
|---|---|
| `-Consola` | El `.exe` abre además una consola con los mensajes ("Conectado a MongoDB Atlas.", etc.). Útil para diagnosticar |
| `-Destino "C:\Ruta"` | Otra carpeta de destino (por ejemplo, el escritorio: `-Destino ([Environment]::GetFolderPath('Desktop'))`) |
| `-Version "1.1.0"` | Versión que muestra Windows en las propiedades del `.exe` |
| `-SinAccesoDirecto` | No crea el acceso directo |

> **Si la carpeta de destino ya existe**, el script se detiene sin borrar
> nada. Mueve la carpeta anterior a la Papelera y vuelve a ejecutarlo.

---

## 5. Paso 4 — Comprobar

1. Doble clic en **"Comercio Electronico"** del escritorio.
2. Debe aparecer la pantalla de bienvenida y luego el Login, con el ícono del
   proyecto en la barra de tareas.
3. Inicia sesión y comprueba que el catálogo trae los productos **y sus
   imágenes** desde Atlas.
4. Si algo falla, genera la versión con consola (`-Consola`, con otro
   `-Destino`) y lee el mensaje:

| Mensaje en consola | Significado | Solución |
|---|---|---|
| `Conectado a MongoDB Atlas.` | Todo bien | — |
| `Sin MongoDB (Falta config.properties ...)` | El `.exe` no encontró el archivo, o le falta `mongodb.uri` | Debe estar **junto al `.exe`** (no dentro de `app\`) y tener la clave `mongodb.uri` rellena |
| `MongoDB Atlas no responde; se usa el almacén en memoria.` | Sin red, IP no autorizada o red que bloquea MongoDB | Autoriza la IP en *Network Access*; prueba con datos móviles; revisa usuario/contraseña de la URI |
| `Sin MongoDB (Failed looking up TXT record ...)` | Al runtime le falta `jdk.naming.dns` (`.exe` generado sin el script actual) | Volver a generar el `.exe` con `crear-exe.ps1` |
| `Sin clave de Resend válida ...` | Correos desactivados | Opcional: completa `resend.api.key` |

---

## 6. Qué contiene la carpeta generada

```text
ComercioElectronico\
├── ComercioElectronico.exe      ← lanzador nativo con el ícono
├── config.properties            ← copiado por el script (secretos)
├── app\
│   ├── Comercio_Electronico.jar
│   ├── lib\                     ← las 8 dependencias
│   └── ComercioElectronico.cfg  ← clase principal y opciones de Java
└── runtime\                     ← Java 26 propio
```

El `.exe` **no funciona separado de su carpeta**: si se quiere tener "el
`.exe` en el escritorio", se usa el acceso directo (lo crea el script) o se
genera la carpeta completa en el escritorio con `-Destino`.

Para llevarlo a otro equipo: comprime la carpeta **sin** `config.properties`,
y en el otro equipo coloca su propio `config.properties` junto al `.exe`.

---

## 7. Alternativa manual (sin el script)

Desde la raíz del proyecto, en PowerShell:

```powershell
& "C:\Program Files\Apache NetBeans\jdk\bin\jpackage.exe" `
  --type app-image `
  --name ComercioElectronico `
  --app-version 1.0.0 `
  --vendor "Ingenieria de Sistemas" `
  --description "Plataforma de Comercio Electronico" `
  --input dist `
  --main-jar Comercio_Electronico.jar `
  --main-class app.Main `
  --icon empaquetado\comercio.ico `
  --java-options "--enable-native-access=ALL-UNNAMED" `
  --dest "$env:LOCALAPPDATA\Programs"
```

Luego copia `config.properties` a
`%LOCALAPPDATA%\Programs\ComercioElectronico\` y crea un acceso directo al
`.exe` (clic derecho → *Enviar a → Escritorio*).

| Parámetro | Por qué |
|---|---|
| `--type app-image` | Carpeta lista para usar, sin instalador. No requiere WiX |
| `--input dist` | Empaqueta el JAR **y** `dist\lib` |
| `--main-class app.Main` | Punto de entrada |
| `--java-options --enable-native-access=ALL-UNNAMED` | FlatLaf carga una librería nativa; sin esto Java 24+ muestra un aviso en cada arranque |
| `--icon` | Ícono del `.exe`, la ventana y la barra de tareas |

### Instalador `.exe` (opcional)

Si además se quiere un **instalador** (asistente "Siguiente → Instalar", entrada
en el menú Inicio y desinstalación desde Configuración), se necesita
[WiX Toolset](https://wixtoolset.org) instalado y se cambia el tipo:

```powershell
& "C:\Program Files\Apache NetBeans\jdk\bin\jpackage.exe" `
  --type exe --name ComercioElectronico --app-version 1.0.0 `
  --input dist --main-jar Comercio_Electronico.jar --main-class app.Main `
  --icon empaquetado\comercio.ico `
  --java-options "--enable-native-access=ALL-UNNAMED" `
  --win-shortcut --win-menu --win-dir-chooser `
  --dest instalador
```

Con el instalador, `config.properties` se copia a mano a la carpeta de
instalación elegida, junto al `.exe`.

---

## 8. Alternativa con Launch4j

Launch4j genera un `.exe` pequeño que **envuelve** el JAR, pero **no trae
Java**: hay que indicarle uno.

1. Descarga Launch4j desde <https://launch4j.sourceforge.net> e instálalo.
2. Pestaña **Basic**: *Output file* `ComercioElectronico.exe`; *Jar*
   `dist\Comercio_Electronico.jar`; *Icon* `empaquetado\comercio.ico`.
   (No hace falta *Change dir*: la aplicación busca `config.properties`
   también junto al JAR, que en Launch4j es el propio `.exe`. Esta variante
   no se probó; si no lo encontrara, marca *Change dir* con `.`.)
3. Pestaña **Classpath**: *Main class* `app.Main`; agrega `lib\*.jar`.
4. Pestaña **JRE**: *Min JRE version* `26`; en *Bundled JRE path* indica una
   carpeta `runtime` con un Java 26 (puede reutilizarse la carpeta `runtime\`
   que genera jpackage). *JVM options*: `--enable-native-access=ALL-UNNAMED`.
5. *Build wrapper*. Coloca junto al `.exe`: la carpeta `lib\`, la carpeta
   `runtime\` y `config.properties`.

Por eso se recomienda jpackage: hace lo mismo en un solo paso y sin
herramientas externas.

---

## 9. Problemas frecuentes

| Problema | Causa | Solución |
|---|---|---|
| `No se encontro jpackage de Java 26+` | NetBeans instalado en otra ruta | Define `JAVA_HOME` con un JDK 26 o edita la primera ruta del script |
| `Se ignora ... (Java 25)` | El JDK del `PATH` es más viejo | Es un aviso: el script sigue buscando el de NetBeans |
| `No existe ...\dist\Comercio_Electronico.jar` | No se compiló | *Clean and Build* en NetBeans |
| `Ya existe ...\ComercioElectronico` | Ya se generó antes | Mover esa carpeta a la Papelera y repetir |
| El script no se ejecuta ("ejecución de scripts deshabilitada") | Política de PowerShell | Usar exactamente `powershell -ExecutionPolicy Bypass -File ...` (solo afecta a esa ejecución) |
| El programa abre pero sin productos de Atlas | `config.properties` no está junto al `.exe`, o Atlas no responde | Copiarlo junto al `.exe`; si ya está, generar la versión `-Consola` y leer el mensaje (§5) |
| Atlas funciona en casa pero no en la universidad | IP no autorizada o red que bloquea el puerto 27017 | *Network Access* en Atlas; datos móviles como alternativa |
| Las imágenes subidas no se ven | Producto con una ruta antigua del disco | Al arrancar con Atlas, la normalización del catálogo las corrige; si la imagen original no existe, se muestra la inicial del producto |

---

## 10. Lista de comprobación antes de la sustentación

- [ ] *Clean and Build* sin errores.
- [ ] Los 11 programas `Verificacion*` de `test/` dan `TODO CORRECTO`.
- [ ] `.exe` generado y abierto con el acceso directo del escritorio.
- [ ] IP del aula autorizada en Atlas (o red móvil disponible).
- [ ] Respaldo de la base (`mongodump`) hecho.
- [ ] `config.properties` **no** está en el ZIP de entrega.
- [ ] Probado en el equipo de la sustentación: login, compra e imágenes.
