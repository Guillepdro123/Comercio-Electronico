<#
.SYNOPSIS
    Genera el ejecutable de Windows (ComercioElectronico.exe) de la aplicacion
    con jpackage, con su propio runtime de Java, su icono y un acceso directo
    en el escritorio.

.DESCRIPTION
    Requisitos:
      1. En NetBeans: clic derecho sobre el proyecto > "Clean and Build".
         Eso deja dist\Comercio_Electronico.jar y dist\lib\*.jar.
      2. El JDK 26 que trae NetBeans (las clases se compilan para Java 26; el
         jpackage de un JDK mas viejo empaquetaria un runtime que no las abre).

    Lo que hace:
      - Ejecuta jpackage --type app-image: una carpeta con
        ComercioElectronico.exe, el runtime de Java y la aplicacion. No hace
        falta tener Java instalado en el equipo donde se use.
      - Copia config.properties JUNTO al .exe (la aplicacion lo busca en la
        carpeta de trabajo y en la del .exe; sin el, arranca con el almacen en
        memoria y sin MongoDB Atlas).
      - Crea "Comercio Electronico" en el escritorio y en el menu Inicio, con
        "Iniciar en" apuntando a esa carpeta.

    No borra nada: si la carpeta de destino ya existe, se detiene y pide
    moverla a la Papelera a mano.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File .\empaquetado\crear-exe.ps1

.EXAMPLE
    # Version con ventana de consola, para ver los mensajes de conexion
    powershell -ExecutionPolicy Bypass -File .\empaquetado\crear-exe.ps1 -Consola
#>
param(
    # Carpeta con Comercio_Electronico.jar y lib\ (la que deja NetBeans).
    [string]$Entrada = "",
    # Donde queda la carpeta de la aplicacion.
    [string]$Destino = (Join-Path $env:LOCALAPPDATA "Programs"),
    # config.properties a copiar junto al .exe (por defecto, el del proyecto).
    [string]$Configuracion = "",
    [string]$Nombre = "ComercioElectronico",
    [string]$Version = "1.0.0",
    # Abre ademas una consola con los mensajes de la aplicacion.
    [switch]$Consola,
    # No crea los accesos directos (escritorio y menu Inicio).
    [switch]$SinAccesoDirecto
)

$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $PSScriptRoot
if ($Entrada -eq "") { $Entrada = Join-Path $raiz "dist" }
if ($Configuracion -eq "") { $Configuracion = Join-Path $raiz "config.properties" }
$icono = Join-Path $PSScriptRoot "comercio.ico"
# Modulos que el runtime del .exe debe llevar si o si (ver paso 3).
$ModulosObligatorios = @("java.desktop", "jdk.naming.dns", "jdk.localedata")

# --- 1. jpackage de Java 26 o superior -------------------------------------
$candidatos = @(
    "C:\Program Files\Apache NetBeans\jdk\bin\jpackage.exe"
)
if ($env:JAVA_HOME) { $candidatos += (Join-Path $env:JAVA_HOME "bin\jpackage.exe") }
$enRuta = Get-Command jpackage.exe -ErrorAction SilentlyContinue
if ($enRuta) { $candidatos += $enRuta.Source }

$jpackage = $null
foreach ($c in $candidatos) {
    if (Test-Path -LiteralPath $c) {
        $mayor = [int](((& $c --version) -split "\.")[0])
        if ($mayor -ge 26) { $jpackage = $c; break }
        Write-Host "Se ignora $c (Java $mayor): las clases son de Java 26."
    }
}
if (-not $jpackage) {
    throw "No se encontro jpackage de Java 26+. Instala NetBeans (trae su JDK) o un JDK 26 y define JAVA_HOME."
}
Write-Host "jpackage: $jpackage"
# El runtime del .exe lleva TODOS los modulos del JDK que compila el proyecto
# (el mismo con que NetBeans lo ejecuta), salvo las herramientas que arman el
# propio runtime. Por defecto jpackage omite modulos que la aplicacion si usa
# (jdk.naming.dns para las URI mongodb+srv:// de Atlas, jdk.localedata para el
# formato en espanol), y su opcion ALL-DEFAULT daba 6 modulos sin Swing segun
# el entorno de la consola. Con la lista explicita, el .exe se comporta igual
# que NetBeans venga de donde venga.
$javaDelJdk = Join-Path (Split-Path -Parent $jpackage) "java.exe"
$modulosJdk = @(& $javaDelJdk --list-modules | ForEach-Object { ($_ -split "@")[0] } |
        Where-Object { @("jdk.jlink", "jdk.jpackage") -notcontains $_ })

# --- 2. Comprobaciones previas ----------------------------------------------
$jar = Join-Path $Entrada "Comercio_Electronico.jar"
if (-not (Test-Path -LiteralPath $jar)) {
    throw "No existe $jar. En NetBeans: clic derecho en el proyecto > Clean and Build."
}
if (-not (Test-Path -LiteralPath (Join-Path $Entrada "lib"))) {
    throw "Falta la carpeta lib\ junto al JAR ($Entrada\lib). Vuelve a hacer Clean and Build."
}
if (-not (Test-Path -LiteralPath $icono)) {
    throw "No se encontro el icono $icono."
}
$carpetaApp = Join-Path $Destino $Nombre
if (Test-Path -LiteralPath $carpetaApp) {
    throw "Ya existe $carpetaApp. Muevela a la Papelera (o cambia -Destino) y vuelve a ejecutar."
}
New-Item -ItemType Directory -Force -Path $Destino | Out-Null

# --- 3. Empaquetado ---------------------------------------------------------
$argumentos = @(
    "--type", "app-image",
    "--name", $Nombre,
    "--app-version", $Version,
    "--vendor", "Ingenieria de Sistemas",
    "--description", "Plataforma de Comercio Electronico",
    "--input", $Entrada,
    "--main-jar", "Comercio_Electronico.jar",
    "--main-class", "app.Main",
    "--icon", $icono,
    "--add-modules", ($modulosJdk -join ","),
    # FlatLaf carga una libreria nativa; sin esto el JDK 24+ avisa en cada arranque.
    "--java-options", "--enable-native-access=ALL-UNNAMED",
    "--dest", $Destino
)
if ($Consola) { $argumentos += "--win-console" }

Write-Host "Empaquetando en $carpetaApp ..."
& $jpackage @argumentos
if ($LASTEXITCODE -ne 0) { throw "jpackage termino con error ($LASTEXITCODE)." }

# Comprobacion: el runtime empaquetado debe llevar esos modulos. Si faltaran,
# el .exe se comportaria distinto que NetBeans sin dar ningun error.
$modulos = (Get-Content -LiteralPath (Join-Path $carpetaApp "runtime\release") |
        Where-Object { $_ -like "MODULES=*" }) -replace '^MODULES="|"$', '' -split ' '
foreach ($m in $ModulosObligatorios) {
    if ($modulos -notcontains $m) { throw "El runtime del .exe no incluye $m." }
}
Write-Host "Runtime verificado: $($modulos.Count) modulos, incluidos jdk.naming.dns y jdk.localedata."

# --- 4. Configuracion junto al .exe -----------------------------------------
if (Test-Path -LiteralPath $Configuracion) {
    Copy-Item -LiteralPath $Configuracion -Destination (Join-Path $carpetaApp "config.properties")
    Write-Host "config.properties copiado junto al .exe (conexion a MongoDB Atlas)."
    Write-Host "AVISO: ese archivo lleva credenciales. No compartas la carpeta tal cual."
} else {
    Write-Host "No hay $Configuracion : la aplicacion arrancara con el almacen en memoria."
}

# --- 5. Accesos directos: escritorio y menu Inicio --------------------------
$exe = Join-Path $carpetaApp "$Nombre.exe"
if (-not $SinAccesoDirecto) {
    $shell = New-Object -ComObject WScript.Shell
    # El menu Inicio del usuario muestra todo acceso directo que haya en su
    # carpeta "Programs": asi la aplicacion aparece en "Todas las aplicaciones"
    # y desde ahi se puede anclar a Inicio o a la barra de tareas.
    $carpetas = @(
        [Environment]::GetFolderPath("Desktop"),
        (Join-Path ([Environment]::GetFolderPath("StartMenu")) "Programs")
    )
    foreach ($carpeta in $carpetas) {
        $acceso = $shell.CreateShortcut((Join-Path $carpeta "Comercio Electronico.lnk"))
        $acceso.TargetPath = $exe
        # "Iniciar en": la carpeta del .exe. La aplicacion ya encuentra ahi
        # config.properties sin esto, pero asi los mensajes y archivos
        # relativos quedan en el mismo sitio.
        $acceso.WorkingDirectory = $carpetaApp
        $acceso.IconLocation = "$exe,0"
        $acceso.Description = "Plataforma de Comercio Electronico"
        $acceso.Save()
        Write-Host "Acceso directo creado en $carpeta"
    }
}

Write-Host ""
Write-Host "Listo: $exe"
