# Índice — Guías del backend FitClub (Capítulos 04 al 08)

Esta serie continúa las guías que ya existen en esta carpeta (`IntelliJ.md`, `DataGrip.md`, `Git.md`, `Clase.md`, `Interfaz.md`…). Aquellas cubren los Capítulos 01 al 03 y las herramientas. Estas cubren del 04 al 08: el backend REST completo, con persistencia real, arquitectura hexagonal y manejo de errores.

## 0. Qué proyecto documentan estas guías

Todo lo que aparece aquí fue construido y probado en el proyecto de práctica `PruebaFitclub`, que vive en:

```
Progra Aplicada\poyecto_proga\Fitclub\PruebaProyec\PruebaFitclub
```

Ese proyecto es deliberadamente pequeño: solo dos entidades, **Socio** y **Membresia**, con una relación 1:N entre ellas. La idea fue aprender el patrón completo en un terreno chico antes de aplicarlo al FitClub real, que tiene diez entidades.

Los datos concretos del proyecto de práctica, para que no tengas que buscarlos:

| Elemento | Valor |
|---|---|
| Proyecto / artifactId | `PruebaFitclub` |
| Package base | `com.fitclub` |
| Clase principal | `PruebaFitclubApplication` |
| Base de datos PostgreSQL | `pruebafitclub` |
| Usuario de base de datos | `pruebafitclub_admin` |
| Schema | `fitclub` (no `public`) |
| Tablas | `fitclub.socio`, `fitclub.membresia` |
| Puerto de la app | 8080 |

## 1. El orden en que conviene leerlas

Las guías están numeradas en el orden en que realmente construimos el proyecto, que no es exactamente el orden de los capítulos del curso. Eso es a propósito: no se puede refactorizar hacia arquitectura hexagonal (Cap. 06) algo que todavía no persiste (Cap. 05), y la relación 1:N (Cap. 07) es más fácil de entender justo después de ver cómo se guarda una sola entidad.

| Guía | Capítulo | Qué construye |
|---|---|---|
| `01-Proyecto-Spring-Boot.md` | Cap. 03–04 | El proyecto, el `pom.xml`, `application.properties` y el primer arranque |
| `02-PostgreSQL-Base-Usuario-Schema.md` | Cap. 05 (previo) | Base de datos, usuario, schema propio y las tablas físicas |
| `03-Cap04-Controller-DTO-Validacion.md` | Cap. 04 | Dominio, Request/Response DTO, validación, Service, Controller y códigos HTTP |
| `04-Postman.md` | Cap. 04 | Descargar, instalar y usar Postman; armar la colección de evidencias |
| `05-Cap05-Persistencia-JPA.md` | Cap. 05 | Entidad JPA, mapper de persistencia, repositorio Spring Data, `ddl-auto=validate` |
| `06-Cap07-Relacion-1aN.md` | Cap. 07 | FK real, `@ManyToOne`, `@JoinColumn`, `LAZY` y validación de existencia del padre |
| `07-Cap06-Hexagonal-Ports-Adapters.md` | Cap. 06 | Port IN, Port OUT, Persistence Adapter, Web Mapper y la regla de dependencias |
| `08-Cap08-Errores-ApiError-Transacciones.md` | Cap. 08 | Excepciones de dominio, `ApiError`, `@RestControllerAdvice`, 400/404/409/500 y `@Transactional` |
| `09-Swagger-OpenAPI.md` | Extra | Documentación automática de la API con springdoc |
| `10-Diagnostico-de-Errores.md` | Todos | Catálogo de errores reales: síntoma, causa, cómo confirmarla y cómo arreglarla |
| `11-Decisiones-y-Alternativas.md` | Todos | Por qué se eligió cada cosa y qué alternativa se descartó |
| `12-Defensa-Individual.md` | Todos | Cómo se califica, guion de defensa y simulacro |
| `13-Actividad-Entrenador-Solucion.md` | — | Las soluciones de la actividad que atraviesa todas las guías |

## 2. La actividad que atraviesa todas las guías

Cada guía termina con una sección **Actividad para practicar**. No es un ejercicio suelto: todas construyen la misma cosa, paso a paso, un módulo nuevo que no existe en el proyecto.

La entidad nueva es **Entrenador**, con **Rutina** como entidad dependiente:

```
Entrenador  1 ──────< N  Rutina
```

| Entidad | Atributos |
|---|---|
| `Entrenador` | `id`, `nombre`, `especialidad`, `email` (único), `fechaContratacion` |
| `Rutina` | `id`, `nombre`, `nivel` (BASICO/INTERMEDIO/AVANZADO), `duracionMinutos`, `entrenadorId` |

La razón de hacerlo con una entidad nueva y no con Socio o Membresia es simple: si la actividad fuera "vuelve a hacer lo mismo que ya hicimos", terminarías copiando y pegando del propio proyecto, y eso no demuestra nada. Con Entrenador tienes que aplicar el patrón de verdad. Además los tipos no son idénticos a propósito: `duracionMinutos` es un entero y no un `BigDecimal` como `precio`, así que no puedes copiar la línea sin pensar.

Si te trabas, la guía `13-Actividad-Entrenador-Solucion.md` tiene las soluciones. Intenta resolverlo antes de abrirla; el examen no te va a dejar mirar.

## 3. Cómo traducir entre ParkFlow y FitClub

Casi todo el material del curso (las 50 preguntas de la ruleta, las presentaciones, los ejemplos del profesor) está escrito con el proyecto del docente: **ParkFlow 360**, con la relación Cliente 1:N Vehículo. Tu proyecto tiene exactamente la misma forma, solo cambian los nombres. Esta tabla te sirve para traducir al vuelo durante el examen:

| ParkFlow 360 (profesor) | FitClub (tuyo) |
|---|---|
| `Cliente` (entidad padre) | `Socio` |
| `Vehiculo` (entidad dependiente) | `Membresia` |
| `cliente_id` (la FK) | `socio_id` |
| `POST /api/clientes` | `POST /api/socios` |
| `POST /api/vehiculos` | `POST /api/membresias` |
| `ClienteJpaEntity` | `SocioJpaEntity` |
| `ClienteRepositoryPort` | `SocioRepositoryPort` |
| `ClientePersistenceAdapter` | `SocioPersistenceAdapter` |
| `ConsultarClienteUseCase` | `SocioUseCase` |
| "la placa no debe estar duplicada" (regla de negocio) | "el email no debe estar duplicado" |
| `PlacaDuplicadaException` | la restricción `UNIQUE` sobre `email` |
| `ClienteNoEncontradoException` | `SocioNoEncontradoException` |
| `ApiErrorResponse` (en `shared/web`) | `ApiError` (en `shared/infrastructure/web`) |
| base `parkflow360`, schema `parkflow` | base `pruebafitclub`, schema `fitclub` |

Cuando en el examen te pregunten algo con nombres de ParkFlow, no respondas con ParkFlow: traduce a tu proyecto y responde señalando tu propio código. Eso es justamente lo que pide la rúbrica.

## 4. Supuestos que se tomaron al escribir estas guías

Hay dos decisiones que se tomaron sin poder consultarte, y conviene que las sepas:

La primera es la que ya expliqué: la actividad usa **Entrenador 1:N Rutina** como entidad nueva.

La segunda es sobre los DTOs. El profesor, en sus presentaciones de Capítulo 04 y en `Presentacion_Record_y_DTO_Java_SpringBoot.pdf`, usa `record` de Java para los DTOs. Tu `PruebaFitclub` los tiene como clases normales con constructor y getters. **Las dos formas son correctas** y ninguna rompe nada, pero como el material del profesor muestra `record`, las guías explican las dos y te dicen cuál es cuál, para que puedas defender la tuya y también leer la del profesor sin confundirte. Eso está desarrollado en `11-Decisiones-y-Alternativas.md`.

## 5. Antes de empezar

- [ ] Tienes IntelliJ IDEA con JDK 21 (ver `IntelliJ.md`).
- [ ] Tienes PostgreSQL instalado y DataGrip conectado (ver `DataGrip.md`).
- [ ] Tienes Git configurado y sabes hacer una rama (ver `Git.md` y `GitHub.md`).
- [ ] Entiendes qué es una clase, una interfaz y un enum en Java (ver `Clase.md`, `Interfaz.md`, `Enum.md`).

Si algo de eso falla, arréglalo primero. Estas guías asumen que el entorno ya funciona.
