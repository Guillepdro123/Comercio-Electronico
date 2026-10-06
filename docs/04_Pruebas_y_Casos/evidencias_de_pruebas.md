# Evidencias de pruebas

Registro visual de la ejecución de las pruebas funcionales, de los correos que
envía el sistema y de las verificaciones automáticas. Complementa a
[documentacion_pruebas.md](documentacion_pruebas.md), que define los casos de
prueba (CP-xx); aquí se muestra **cómo se comportó la aplicación al
ejecutarlos**.

| Dato | Valor |
|---|---|
| Fecha de ejecución | 04/10/2026 |
| Versión | La del `.exe` 1.0.0 generado ese día |
| Entorno | Windows 11 Pro, JDK 26.0.2 (el de NetBeans), escalado de pantalla 125 % |
| Datos | Repositorios **en memoria** con datos de demostración. Las pruebas nunca escriben en MongoDB Atlas, que es una base compartida |
| Cómo se generaron | Con `test/manual/CapturasDocumentacion.java` (ver §6) |

**Personas de demostración** (datos ficticios):

| Persona | Rol | Para qué aparece |
|---|---|---|
| Ana Gómez | Cliente con perfil completo | Compra, reseña, historial |
| Carlos Ruiz | Cliente | Compras y reseñas previas, para que haya datos |
| Sofía Torres | Cliente creada con Google (sin cédula ni dirección) | Bloqueo del checkout |
| Luis Ortega | Proveedor, empresa "Supertecno" | Panel del vendedor, CRUD |
| María López | Proveedor nueva, empresa "HogarPlus" | Registro |

---

## 1. Acceso y registro

### 1.1 Pantalla de acceso — RF-03

![Login](evidencias/capturas/01_login.png)

Ventana de acceso con el panel lateral de navegación. El botón "Continuar con
Google" no aparece porque este recorrido se ejecuta sin servicios externos
(ni Atlas, ni Google, ni Resend): sin credenciales de Google la aplicación no
muestra el botón, y el acceso con contraseña funciona igual. Con el
`config.properties` del proyecto, el botón aparece bajo "INICIAR SESIÓN".

### 1.2 Contraseña incorrecta — CP-31, RF-04

![Contraseña incorrecta](evidencias/capturas/02_login_contrasena_incorrecta.png)

El sistema no dice cuál de los dos datos falló (no revela si el correo existe)
y avisa de los intentos que quedan.

### 1.3 Bloqueo tras tres intentos — CP-31, RF-04

![Bloqueo](evidencias/capturas/03_login_bloqueado_30s.png)

Al tercer fallo consecutivo el botón se deshabilita y aparece una cuenta
regresiva de 30 s, como **aviso** (naranja) y no como error: el usuario no
se equivocó de nuevo, está esperando.

### 1.4 Validación del registro — CP-32, RF-05

![Registro con errores](evidencias/capturas/04_registro_validacion.png)

Identificación con letras: el registro se rechaza con un mensaje concreto. El
indicador junto a "Contraseña" marca **Insegura** para `abc`: usa la misma
regla (`PoliticaPassword`) que decide si se acepta.

### 1.5 Registro de un Proveedor — RF-02

![Registro de proveedor](evidencias/capturas/05_registro_proveedor.png)

Al elegir *Proveedor*, el formulario pide **NIT y empresa** en la misma fila.
La contraseña `Hogar2026*` se califica como *Medianamente segura*. El aviso
rojo inferior es del intento anterior (1.4): se actualiza al volver a enviar.

### 1.6 Registro exitoso — RF-01, RF-02

![Registro exitoso](evidencias/capturas/06_registro_exitoso.png)

Confirmación con confeti; al cerrarla, la ventana vuelve sola al Login.

---

## 2. Tienda (Cliente)

### 2.1 Pop-up promocional — RF-25

![Pop-up](evidencias/capturas/07_popup_promocional.png)

Aparece en ~40 % de los inicios de sesión (medido: 810 de 2000), solo con
productos que tienen descuento y existencias, y se cierra con la X.

### 2.2 Catálogo con banner — RF-17, RF-12, RF-25

![Tienda](evidencias/capturas/08_tienda_banner.png)

Cada tarjeta muestra la **marca** del vendedor ("Vendido por Supertecno"), la
calificación con estrellas, el precio con descuento y las existencias. El
banner rota las cuatro mejores ofertas.

### 2.3 Búsqueda sin tildes — RF-17

![Búsqueda](evidencias/capturas/09_busqueda_sin_tildes.png)

Escribir `cafe` encuentra **Cafetera espresso**: la búsqueda ignora tildes y
mayúsculas.

### 2.4 Ficha de un producto comprado — RF-18, RF-24

![Ficha con reseñas](evidencias/capturas/10_ficha_con_resenas.png)

Ana compró estos audífonos, así que la ficha le ofrece calificarlos.

### 2.5 Reseña publicada — CP-25, RF-24

![Reseña publicada](evidencias/capturas/11_resena_publicada.png)

Con 4 estrellas y un comentario, la sección pasa a **4,5 con 2 reseñas** y la
tarjeta del catálogo de fondo también se actualiza.
La cabecera de la ficha también pasa a 4,5 (2) (hallazgo H-02, corregido).

### 2.6 Ficha de un producto no comprado — CP-24

![Ficha sin compra](evidencias/capturas/12_ficha_sin_compra.png)

Sin compra no hay formulario: *"Podrás calificarlo cuando lo hayas comprado."*

### 2.7 Carrito — CP-17, RF-19

![Carrito](evidencias/capturas/13_carrito.png)

Miniatura, precio unitario, cantidad editable con `-`/`+`, subtotal por
renglón, total y contador sobre el ícono del carrito. Confirmar es naranja
(color reservado a comprar).

### 2.8 Compra confirmada — CP-20, RF-21

![Compra confirmada](evidencias/capturas/14_compra_confirmada.png)

Número de pedido, total y dirección de entrega. En la tarjeta del teclado las
existencias bajaron de 8 a 7.

### 2.9 Mis compras — CP-22, RF-23

![Mis compras](evidencias/capturas/15_mis_compras.png)

Historial con fecha, productos, total y dirección de cada pedido.
Un solo botón, "Cerrar" (hallazgo H-04, corregido).

---

## 3. Cuenta de Google sin datos de envío

### 3.1 Aviso en el perfil — CP-11, RF-09

![Perfil con aviso](evidencias/capturas/16_perfil_con_aviso.png)

Una cuenta creada con Google no tiene cédula ni dirección: el perfil lo avisa
arriba, en naranja.

### 3.2 Checkout bloqueado — CP-12, RF-20

![Checkout pide datos](evidencias/capturas/17_checkout_pide_datos.png)

Al confirmar la compra, en vez de comprar, se abre el formulario de datos de
envío. El stock todavía no se ha tocado.

### 3.3 Cédula inválida — CP-13

![Cédula inválida](evidencias/capturas/18_checkout_cedula_invalida.png)

El formulario sigue abierto con lo escrito y el motivo del rechazo.
El formulario se ensancha para que el mensaje se lea completo (hallazgo H-03, corregido).

### 3.4 La compra continúa sola — CP-15

![Compra completada](evidencias/capturas/19_checkout_compra_completada.png)

Con una cédula válida, el formulario se cierra y la compra se completa sin que
el usuario tenga que volver a pulsar "Confirmar compra".

---

## 4. Proveedor (doble rol)

### 4.1 "Gestionar tienda" — CP-37, RF-15

![Menú del avatar](evidencias/capturas/20_menu_gestionar_tienda.png)

El Proveedor entra a la tienda como cualquier comprador; su menú tiene además
**Gestionar tienda**. Un Cliente no ve esa opción.

### 4.2 Panel del vendedor — RF-16

![Panel del proveedor](evidencias/capturas/21_panel_proveedor.png)

Ingresos, ticket promedio, unidades vendidas, producto más vendido, pedidos,
catálogo y agotados, más el gráfico de ingresos por producto. Las cifras salen
de las compras hechas en las secciones anteriores.

### 4.3 Formulario de producto — RF-11

![Formulario](evidencias/capturas/22_producto_formulario.png)

### 4.4 Precio inválido — CP-02

![Precio inválido](evidencias/capturas/23_producto_precio_invalido.png)

El producto no se guarda y se explica por qué.
El error sale en un diálogo de **Aviso** con ícono de advertencia (hallazgo H-01, corregido).

### 4.5 Producto publicado — CP-01, RF-11, RF-12

![Producto publicado](evidencias/capturas/24_producto_publicado.png)

La lámpara aparece al final de la tabla con su precio final ($ 89.000 − 10 % =
$ 80.100) y el catálogo pasa de 10 a 11 productos.

---

## 5. Tema oscuro — CP-36, RF-27

| Panel del vendedor | Tienda | Ficha |
|---|---|---|
| ![Panel oscuro](evidencias/capturas/25_panel_proveedor_oscuro.png) | ![Tienda oscura](evidencias/capturas/26_tienda_oscura.png) | ![Ficha oscura](evidencias/capturas/27_ficha_oscura.png) |

El cambio se hizo con la ventana abierta, sin cerrarla. En la ficha de su
propio producto, el Proveedor lee *"Es un producto de tu tienda: las reseñas
son de quienes lo compran"* (CP-29).

---

## 6. Correos transaccionales — RF-22

HTML real que genera el sistema y que Resend entrega. Los archivos `.html`
están en [`evidencias/correos/`](evidencias/correos/) y se pueden abrir en
cualquier navegador.

| Bienvenida | Confirmación de compra | Alerta de venta (tienda) |
|---|---|---|
| ![Bienvenida](evidencias/correos/01_bienvenida.png) | ![Confirmación](evidencias/correos/02_confirmacion_compra.png) | ![Alerta](evidencias/correos/03_alerta_venta.png) |

---

## 7. Verificaciones automáticas

Salida completa de cada programa en
[`evidencias/verificaciones/`](evidencias/verificaciones/).

| Programa | OK | Fallas |
|---|---|---|
| VerificacionAutenticacionService | 13 | 0 |
| VerificacionCatalogoService | 22 | 0 |
| VerificacionImagenesPortables | 9 | 0 |
| VerificacionCompraService | 10 | 0 |
| VerificacionCompraConcurrente | 1 (20 rondas) | 0 |
| VerificacionCuentaService | 20 | 0 |
| VerificacionDobleRol | 13 | 0 |
| VerificacionResenaService | 9 | 0 |
| VerificacionClienteController | 17 | 0 |
| VerificacionUsuarioAdapter | 4 | 0 |
| VerificacionConmutadorTema | 5 | 0 |
| **Total** | **123** | **0** |

---

## 8. Hallazgos

Defectos encontrados **al revisar estas capturas**, todos corregidos el 04/10/2026 y vueltos a fotografiar. Ninguno impedía usar el
sistema ni afectaba datos; eran de presentación.

| ID | Severidad | Descripción | Evidencia | Dónde está | Estado |
|---|---|---|---|---|---|
| H-01 | Media | Los avisos de error del panel del vendedor y de la tienda se muestran en el diálogo de **éxito** (título "Éxito" y check verde). Ej.: "El precio debe ser un número mayor que cero." | 4.4 | `mostrarAviso` de ambas ventanas; ahora usan el nuevo `mostrarDialogoAviso` de la fábrica | **Corregido** el 04/10/2026 |
| H-02 | Baja | Tras publicar una reseña, la cabecera de la ficha conserva el promedio anterior; la sección de opiniones y la tarjeta sí se actualizan | 2.5 | `ClientDashboardFrame.actualizarResenas`; ahora repinta también la cabecera | **Corregido** el 04/10/2026 |
| H-03 | Baja | En el formulario de datos de envío, el mensaje de cédula inválida es más ancho que el formulario y se corta | 3.3 | Columna de ancho fijo en `pedirDatosEnvio`; ahora es un ancho mínimo | **Corregido** el 04/10/2026 |
| H-04 | Baja | "Mis compras" muestra dos botones que hacen lo mismo: "Cerrar" y "Cancelar" | 2.9 | Usaba `mostrarDialogoConfirmacion`; ahora `mostrarDialogoContenido`, de un botón | **Corregido** el 04/10/2026 |
| H-05 | Baja | Al vaciar un campo con texto fantasma, el texto fantasma se notificaba como si el usuario lo hubiera escrito (el buscador lo tomaba como búsqueda y vaciaba el catálogo) | — | `PlaceholderFocusListener.limpiarYMostrarPlaceholder` | **Corregido** el 04/10/2026 |

---

## 9. Cómo regenerar estas evidencias

1. En NetBeans, abrir `test/manual/CapturasDocumentacion.java` (carpeta *Test
   Packages*).
2. Clic derecho → **Run File**. Tarda unos dos minutos.
3. **No usar el equipo mientras corre**: fotografía la pantalla, y saldría lo
   que hubiera encima de la aplicación.
4. Las capturas y los correos se reescriben en `docs/04_Pruebas_y_Casos/evidencias/`.
   Las imágenes de los correos se obtienen aparte, abriendo cada `.html` en el
   navegador (o con Edge sin ventana: `msedge --headless --screenshot`).

El programa usa datos en memoria y desvía la carpeta del usuario a una
temporal, así que no cambia la sesión recordada ni el tema elegido de quien lo
ejecuta.
