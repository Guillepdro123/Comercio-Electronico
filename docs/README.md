# Documentación técnica — Plataforma de Comercio Electrónico

Aplicación de escritorio en Java 26 + Swing (FlatLaf), arquitectura MVC con
capa de aplicación y persistencia en MongoDB Atlas.

> **¿Llegas nuevo al proyecto?** Empieza por la
> **[Guía de traspaso](00_Inicio/guia_de_traspaso.md)**: qué es, cómo ponerlo
> a funcionar, qué cuentas hay que traspasar y dónde está cada cosa.

| # | Documento | Contenido |
|---|---|---|
| 00 | [Guía de traspaso](00_Inicio/guia_de_traspaso.md) | Punto de entrada: resumen, puesta en marcha, cuentas y credenciales, mapa del código, reglas, pendientes y glosario |
| 01 | [Arquitectura y diseño](01_Arquitectura_y_Diseno/arquitectura.md) | Evolución del diagrama conceptual inicial frente al diseño final, arquitectura en capas, patrones (MVC, Factory, Observer, Repository, Adapter, Decorator), SOLID y programación orientada a interfaces |
| 02 | [Esquema NoSQL](02_Base_de_Datos/esquema_nosql.md) | Colecciones de MongoDB Atlas (`usuarios`, `productos`, `compras`, `resenas`, `imagenes`), campos, documentos de ejemplo, índices y reglas de evolución |
| 03 | [Requisitos y riesgos](03_Gestion_y_Requisitos/requisitos_y_riesgos.md) | 27 requisitos funcionales, 13 no funcionales y matriz formal de 7 riesgos con estrategia y plan de tratamiento |
| 04 | [Casos de uso y pruebas](04_Pruebas_y_Casos/documentacion_pruebas.md) | Diagrama de casos de uso, especificaciones, diagramas de secuencia, 123 comprobaciones automatizadas en 11 programas y 38 casos de prueba funcionales con su resultado |
| 04 | [Evidencias de pruebas](04_Pruebas_y_Casos/evidencias_de_pruebas.md) | 27 capturas de la aplicación en ejecución, los 3 correos que envía, la salida de las verificaciones y los hallazgos encontrados |
| 05 | [Guía del ejecutable](05_Empaquetado/guia_ejecutable.md) | Paso a paso para generar `ComercioElectronico.exe` con jpackage, ícono propio, menú Inicio y conexión a Atlas |
| 06 | [Métricas del proyecto](06_Metricas/metricas_del_proyecto.md) | Avance (tareas de Trello, requisitos y pruebas) y medición con GQM, ISO/IEC 15939, PSM, ISO/IEC 25010, CMMI e IEEE: siete objetivos con sus métricas, evolución de los cuatro incrementos, hallazgos, defectos detectados y relación con los riesgos |

```text
docs/
├── README.md                          ← este índice
├── 00_Inicio/
│   └── guia_de_traspaso.md
├── 01_Arquitectura_y_Diseno/
│   ├── arquitectura.md
│   └── img/diagrama_conceptual_inicial.png
├── 02_Base_de_Datos/
│   └── esquema_nosql.md
├── 03_Gestion_y_Requisitos/
│   └── requisitos_y_riesgos.md
├── 04_Pruebas_y_Casos/
│   ├── documentacion_pruebas.md
│   ├── evidencias_de_pruebas.md
│   └── evidencias/
│       ├── capturas/                  ← 27 PNG de la aplicación
│       ├── correos/                   ← 3 correos (HTML y PNG)
│       └── verificaciones/            ← salida de los 11 programas de test/
├── 05_Empaquetado/
│   └── guia_ejecutable.md
└── 06_Metricas/
    └── metricas_del_proyecto.md
```

Fuera de `docs/`, en la raíz del proyecto:

| Archivo | Para qué |
|---|---|
| `README.md` | Descripción general e historia por incrementos |
| `CLAUDE.md` | Bitácora de decisiones técnicas: cada regla del proyecto con su motivo |
| `empaquetado/crear-exe.ps1`, `empaquetado/comercio.ico` | Generación del ejecutable |
| `test/manual/CapturasDocumentacion.java` | Regenera las evidencias de `04_Pruebas_y_Casos/evidencias/` |

Los diagramas están en Mermaid: se ven como gráficos en GitHub, en VS Code
(extensión *Markdown Preview Mermaid Support*) o pegándolos en
<https://mermaid.live> para exportarlos como imagen.
