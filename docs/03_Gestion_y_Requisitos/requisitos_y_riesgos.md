# Requisitos y gestión de riesgos

---

## 1. Alcance

Aplicación de escritorio (Java Swing) para una tienda en línea con dos
perfiles: **comprador** y **vendedor**. Todo usuario puede comprar, y el
Proveedor además administra su propia tienda. Los datos viven en MongoDB
Atlas y se comparten entre varios equipos en tiempo real.

**Fuera del alcance** (comparado con el diagrama conceptual inicial, ver
[arquitectura.md](../01_Arquitectura_y_Diseno/arquitectura.md)): pasarela de
pago real, facturación electrónica, logística de despacho (rutas,
transportadoras, estados de envío), tickets PQR, chat en vivo y devoluciones.

---

## 2. Requisitos funcionales

Prioridad según MoSCoW: **M** = imprescindible, **S** = importante, **C** = deseable.

### 2.1 Cuentas y seguridad

| ID | Requisito | Prioridad | Estado |
|---|---|---|---|
| RF-01 | Registrar una cuenta de Cliente (identificación, nombres, correo, contraseña, dirección) | M | Implementado |
| RF-02 | Registrar una cuenta de Proveedor con **NIT y nombre de empresa obligatorios** | M | Implementado |
| RF-03 | Iniciar sesión con correo y contraseña | M | Implementado |
| RF-04 | Bloquear el acceso 30 s tras **3 intentos fallidos consecutivos**, con cuenta regresiva visible | S | Implementado |
| RF-05 | Exigir contraseña de mínimo 7 caracteres con mayúscula, número y carácter especial, con indicador de fortaleza | M | Implementado |
| RF-06 | Iniciar sesión o registrarse con **Google** (correo verificado), eligiendo el rol la primera vez | S | Implementado (credenciales configuradas; verificado contra un Google simulado, falta la prueba de punta a punta con Google real) |
| RF-07 | Recordar la sesión y reanudarla al abrir la aplicación | C | Implementado |
| RF-08 | Editar el perfil: nombre, teléfono, correo, cédula, dirección, empresa y contraseña (exigiendo la actual) | M | Implementado |
| RF-09 | Avisar en el perfil cuando faltan la cédula o la dirección (típico en cuentas de Google) | M | Implementado |
| RF-10 | Cerrar sesión volviendo a la pantalla de acceso | M | Implementado |

### 2.2 Catálogo (vendedor)

| ID | Requisito | Prioridad | Estado |
|---|---|---|---|
| RF-11 | Publicar, editar y eliminar productos propios (nombre, descripción, precio, descuento, categoría, stock, imagen) | M | Implementado |
| RF-12 | Mostrar la **marca** del vendedor en cada producto ("Vendido por …") | M | Implementado |
| RF-13 | Impedir publicar sin nombre de empresa | M | Implementado |
| RF-14 | Guardar las imágenes en la nube para que se vean en cualquier equipo | M | Implementado |
| RF-15 | Doble rol: el Proveedor entra a la tienda y pasa a su panel con "Gestionar tienda" | M | Implementado |
| RF-16 | Ver indicadores de venta: ingresos, unidades, pedidos, ticket promedio, producto más vendido y gráfico | S | Implementado |

### 2.3 Compra (comprador)

| ID | Requisito | Prioridad | Estado |
|---|---|---|---|
| RF-17 | Navegar el catálogo, buscar por texto (sin importar tildes) y filtrar por categoría | M | Implementado |
| RF-18 | Ver la ficha del producto con imagen, precio, descuento, stock y reseñas | M | Implementado |
| RF-19 | Agregar al carrito y **cambiar cantidades** en el carrito sin superar el stock | M | Implementado |
| RF-20 | **Bloquear el checkout sin cédula ni dirección** y pedirlas en un formulario rápido que retoma la compra | M | Implementado |
| RF-21 | Confirmar la compra de forma atómica (todo o nada) y descontar el stock | M | Implementado |
| RF-22 | Enviar correo de confirmación al comprador y de alerta de venta a la tienda | S | Implementado |
| RF-23 | Consultar el historial "Mis compras" | S | Implementado |
| RF-24 | Calificar con **1–5 estrellas** y comentar (máx. 300 caracteres) **solo productos comprados**, nunca los propios | M | Implementado |
| RF-25 | Banner rotativo con las mejores ofertas y pop-up promocional ocasional | C | Implementado |
| RF-26 | Ver los cambios del catálogo de otros equipos sin recargar | S | Implementado (verificado localmente; falta la prueba entre dos equipos) |
| RF-27 | Cambiar entre tema claro y oscuro sin cerrar la ventana | C | Implementado |

---

## 3. Requisitos no funcionales

| ID | Categoría | Requisito | Cómo se cumple / verifica |
|---|---|---|---|
| RNF-01 | Seguridad | Las contraseñas nunca se guardan en claro | BCrypt (`CifradoPassword`); prueba "la contraseña se guarda cifrada" |
| RNF-02 | Seguridad | Ningún secreto en el código fuente | URI de Atlas, clave de Resend y secreto de Google en `config.properties` (en `.gitignore`) |
| RNF-03 | Seguridad | El acceso con Google resiste interceptación | Servidor solo en `127.0.0.1`, PKCE S256, `state` comparado en tiempo constante, 3 min de plazo |
| RNF-04 | Seguridad | El texto de otros usuarios no se interpreta como HTML | Escapado y `html.disable` en etiquetas |
| RNF-05 | Integridad | Nunca se vende más de lo que hay | Descuento atómico + compensación; 20 rondas concurrentes sin doble venta (`VerificacionCompraConcurrente`) |
| RNF-06 | Integridad | Un correo, una cuenta | Validación + contrato del repositorio + índice único con colación |
| RNF-07 | Rendimiento / usabilidad | La ventana nunca se congela durante operaciones de red | `SwingWorker` con diálogo de carga (Atlas: 3,5 s la conexión, ~100 ms por consulta) |
| RNF-08 | Disponibilidad | La aplicación abre sin internet | Repositorios en memoria si Atlas no responde en 8 s |
| RNF-09 | Portabilidad | Funciona en cualquier Windows sin instalar Java | `.exe` con runtime propio (jpackage); imágenes en la base, no en rutas locales |
| RNF-10 | Mantenibilidad | Capas desacopladas | MVC + capa de aplicación + repositorios por interfaz; reglas comprobadas con búsquedas de imports |
| RNF-11 | Usabilidad | Interfaz consistente | Todos los componentes salen de una sola fábrica; FlatLaf; tema claro/oscuro |
| RNF-12 | Escalabilidad | Cambiar de base o de interfaz sin reescribir reglas | Implementar las interfaces de repositorio o de vista; solo cambia `app.Main` |
| RNF-13 | Verificabilidad | Reglas de negocio probadas sin interfaz gráfica | 11 programas de verificación, 123 comprobaciones (`test/`) |

---

## 4. Matriz de riesgos

### 4.1 Escalas

| Valor | Probabilidad | Impacto |
|---|---|---|
| 1 — Baja | Improbable en el periodo del proyecto | Molestia menor, sin efecto en la entrega |
| 2 — Media | Puede pasar una vez | Retrasa una funcionalidad o una demostración |
| 3 — Alta | Es probable o ya pasó | Compromete la entrega o la sustentación |

**Exposición = Probabilidad × Impacto** → 1–2 baja · 3–4 media · 6–9 **alta**.

**Estrategias:** **Evitar** (eliminar la causa) · **Mitigar** (reducir
probabilidad o impacto) · **Transferir** (trasladar a un tercero) ·
**Aceptar** (asumir con plan de contingencia).

### 4.2 Matriz

| ID | Riesgo | P | I | Exp. | Estrategia | Acción de tratamiento | Responsable | Evidencia en el proyecto |
|---|---|---|---|---|---|---|---|---|
| **R1** | **Planificación y tiempo:** el alcance del diagrama inicial (5 subsistemas, 15 componentes) no cabe en el semestre; se llega a la sustentación con funcionalidades a medias | 3 | 3 | **9** | **Mitigar** | Entregas por incrementos funcionales cerrados (Incremento 1: registro; 2: acceso e interfaz; 3: catálogo y compra; final: doble rol, reseñas, nube). Priorización MoSCoW: logística, PQR y pasarela pasan a trabajo futuro. Cada incremento se cierra compilando y con capturas | Líder del proyecto | Hoja de ruta del `README.md`; §1 de este documento |
| **R2** | **Pérdida de datos o falla de red con MongoDB Atlas:** el día de la sustentación la red de la universidad bloquea el puerto 27017/DNS SRV, la IP del equipo no está autorizada, o se borran datos del clúster compartido | 3 | 3 | **9** | **Mitigar** + **Transferir** | *Mitigar:* (a) respaldo automático al almacén en memoria si Atlas no responde en 8 s; (b) probar la conexión en el aula días antes; (c) autorizar la IP en *Network Access* o, temporalmente, `0.0.0.0/0`; (d) llevar un punto de acceso móvil como red alternativa; (e) `mongodump` antes de la sustentación; (f) compras con compensación para no dejar stock inconsistente. *Transferir:* la durabilidad y replicación del almacenamiento la asume MongoDB Atlas (clúster con réplica de 3 nodos) | Responsable de base de datos | `app.Main.abrirConexion()`, `ping` de arranque, `CompraService` |
| **R3** | **Cambios inesperados de requisitos:** el docente o el cliente piden nuevas funcionalidades tarde (pasó: doble rol, marca, reseñas, imágenes en la nube, tema oscuro) | 3 | 2 | **6** | **Mitigar** | Arquitectura preparada para crecer sin reescribir: casos de uso en `aplicacion.*`, repositorios por interfaz, fábrica de componentes y paletas. Cada cambio se analiza (impacto en datos de Atlas, en el alto del formulario de registro) antes de codificar, y se registra en `CLAUDE.md` | Arquitecto / todo el equipo | Doble rol añadido con `puedeVender()` sin `instanceof`; tema oscuro = otra `Paleta` |
| **R4** | **Exposición de credenciales:** subir `config.properties` al repositorio o compartir la carpeta del `.exe` con la URI de Atlas y las claves | 2 | 3 | **6** | **Evitar** | `config.properties` en `.gitignore`; plantilla `config.properties.ejemplo` sin secretos; usuario de base con permisos solo sobre `comercio_electronico`; el script del `.exe` avisa que el archivo copiado lleva credenciales. Si se filtra: rotar contraseña de Atlas y claves | Responsable de base de datos | `service.config.Configuracion`, `empaquetado/crear-exe.ps1` |
| **R5** | **Incompatibilidad de versión de Java:** el proyecto compila para Java 26 y el equipo de la sustentación tiene otra versión (pasó: el `PATH` tenía JDK 25) | 2 | 3 | **6** | **Evitar** | Distribuir como `.exe` con su **propio runtime** (jpackage), que no depende del Java instalado. El script exige jpackage 26 o superior | Responsable de empaquetado | [guia_ejecutable.md](../05_Empaquetado/guia_ejecutable.md) |
| **R6** | **Concurrencia en compras:** dos equipos compran la última unidad a la vez | 2 | 2 | 4 | **Evitar** | Descuento atómico condicional (`stock >= n`) en lugar de "consultar y luego descontar"; compensación si falla un renglón o el registro del pedido | Desarrollador de ventas | 20 rondas concurrentes sin doble venta (`VerificacionCompraConcurrente`) |
| **R7** | **Dependencia de servicios externos** (Resend para correos, Google para acceso) caídos o sin credenciales | 2 | 1 | 2 | **Aceptar** | Sin clave de Resend los correos se registran por consola; sin credenciales de Google el botón no aparece. Ninguna compra o registro falla por no poder enviar un correo (envío en segundo plano) | Desarrollador de servicios | `Main.elegirNotificador`, `Main.elegirAccesoGoogle` |

### 4.3 Mapa de calor

| Probabilidad ↓ / Impacto → | 1 — Bajo | 2 — Medio | 3 — Alto |
|---|---|---|---|
| **3 — Alta** | | R3 | **R1, R2** |
| **2 — Media** | R7 | R6 | R4, R5 |
| **1 — Baja** | | | |

### 4.4 Plan de contingencia para la sustentación

| Si ocurre… | Se hace… |
|---|---|
| Atlas no responde | La aplicación arranca sola con el almacén en memoria (trae productos de demostración; las cuentas se registran en vivo durante la demostración); se explica el respaldo como parte del diseño |
| La red bloquea MongoDB | Cambiar a la red móvil y reabrir la aplicación |
| El `.exe` no abre | Ejecutar desde NetBeans (Run) con el JDK incluido |
| Falla una demostración visual | Capturas de respaldo de cada pantalla |

---

## 5. Seguimiento

| Ítem | Frecuencia | Instrumento |
|---|---|---|
| Riesgos R1 y R3 | Cada incremento | Revisión de alcance contra la hoja de ruta |
| Riesgo R2 | Una semana antes y el mismo día de la sustentación | Abrir el `.exe` en el aula y comprobar que el catálogo carga desde Atlas |
| Riesgos R4 y R5 | Antes de cada entrega | Revisar que `config.properties` no esté en la entrega; generar el `.exe` |
| Calidad | Antes de cada entrega | Ejecutar los 11 programas de verificación (123 comprobaciones) |
