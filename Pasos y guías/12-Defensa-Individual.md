# La defensa individual: cómo se califica y cómo prepararse

Esta guía no enseña a construir nada. Enseña a **explicar** lo construido, que es donde está la mayor parte de la nota.

## 0. Cómo se calcula tu nota

Según la Guía Formal del Estudiante, sección 10:

```
NOTA DE LA INSTANCIA = (producto y documentación del equipo × 0,20)
                     + (defensa individual × 0,80)
```

El 20% puede ser idéntico para todo el equipo. **El 80% es solo tuyo.** La guía lo dice sin rodeos: dos integrantes del mismo equipo pueden sacar notas muy distintas en la misma instancia.

### Los cinco criterios del 80%

| Criterio | Peso interno | Sobre tu nota final | Qué se evalúa |
|---|---|---|---|
| Comprensión conceptual | 25% | 20% | Defines y **relacionas** los conceptos técnicos |
| Comprensión del proyecto completo | 25% | 20% | Recorres flujo, módulos, datos y decisiones **aunque no los hayas implementado tú** |
| Capacidad práctica | 20% | 16% | Ejecutas, consultas, localizas, modificas o demuestras **sin depender de otro integrante** |
| Justificación de decisiones | 15% | 12% | Explicas por qué, y **reconoces alternativas, límites y consecuencias** |
| Diagnóstico y razonamiento | 15% | 12% | Interpretas errores y propones causas y correcciones **en lugar de adivinar** |

Conviene leer esa tabla dos veces, porque reordena las prioridades de estudio: **escribir código pesa 16%; explicar pesa 40%; justificar y diagnosticar pesan 24%.** Pasar las últimas horas tecleando es peor inversión que pasarlas explicando en voz alta.

### El 20% grupal

| Criterio | Peso |
|---|---|
| Cumplimiento del alcance | 25% |
| Funcionamiento e integración | 20% |
| Calidad técnica | 20% |
| Documentación | 20% |
| Trabajo colaborativo y trazabilidad (commits, PRs, revisiones) | 15% |

## 1. Los doce pasos de preparación que pide el docente

De la sección 11 de la Guía Formal, con lo que significa cada uno para tu proyecto:

1. **Actualiza el repositorio y ejecuta la versión exacta que se evaluará.** Que arranque delante del profesor sin sorpresas.
2. **Revisa el alcance del corte** y marca cada requisito como comprendido, demostrable y explicable.
3. **Dibuja el modelo de datos sin copiarlo** y explica PK, FK, cardinalidades y constraints. Practica dibujar `socio` y `membresia` en papel, con la FK, el `UNIQUE` sobre email y el `NOT NULL`.
4. **Elige dos flujos y síguelos de extremo a extremo.** Usa `POST /api/socios` (éxito) y `POST /api/membresias` con socio inexistente (error).
5. **Explica qué pertenece a dominio, aplicación e infraestructura.** Ten el árbol de paquetes a mano.
6. **Ejecuta consultas SQL y explica qué devuelve cada JOIN o filtro.** El JOIN entre membresía y socio.
7. **Identifica las validaciones y di dónde se aplican y qué error producen.** Las tres capas: DTO, caso de uso, base.
8. **Revisa las pruebas existentes.** Si no hay pruebas automáticas, reconócelo y explica qué probarías primero y por qué — es mejor respuesta que inventar.
9. **En web/móvil, identifica estados de carga, error, vacío y éxito.** Fuera del alcance de lo que construimos; si te preguntan, dilo con claridad.
10. **Revisa Docker, variables de entorno y CI.** Ídem.
11. **Explica al menos una decisión técnica que no tomaste tú.** Implica hablar con tus compañeros antes del examen.
12. **Practica diagnosticar un fallo: no memorices solo el camino feliz.** Ver `10-Diagnostico-de-Errores.md`.

## 2. El guion del flujo feliz

Esta es la pregunta más cara del banco (aparece como la 21 del banco de defensa, la 40 y la 50 de la ruleta). Practícala hasta poder decirla sin leer, en menos de dos minutos.

**`POST /api/socios`, de Postman a PostgreSQL y de vuelta:**

1. Postman envía `POST http://localhost:8080/api/socios` con `Content-Type: application/json` y un body JSON.
2. Tomcat recibe la conexión; Spring MVC enruta la petición hacia `SocioController` por `@RestController`, `@RequestMapping("/api/socios")` y `@PostMapping`.
3. `@RequestBody` hace que Jackson deserialice el JSON en un `SocioRequestDTO`.
4. `@Valid` ejecuta Jakarta Validation sobre ese DTO: `@NotBlank`, `@Email`, `@Pattern`. Si algo falla, la petición **no entra al caso de uso**.
5. `SocioWebMapper.toDomain()` traduce el DTO al modelo de dominio `Socio`, poniendo `fechaRegistro = LocalDate.now()`.
6. El Controller invoca `socioUseCase.registrar(socio)` — depende del **Port IN**, no de la clase concreta.
7. `SocioService` implementa ese puerto y ejecuta las reglas de aplicación.
8. El Service llama a `socioRepositoryPort.guardar(socio)` — el **Port OUT**, una interfaz del dominio.
9. `SocioPersistenceAdapter` implementa ese puerto. Es el **Adapter OUT**.
10. `SocioPersistenceMapper.toEntity()` traduce el dominio a `SocioJpaEntity`.
11. `SpringDataSocioRepository.save()` entra en JPA; Hibernate genera el SQL.
12. Hibernate ejecuta por JDBC: `insert into fitclub.socio (email, fecha_registro, nombre, telefono) values (?, ?, ?, ?)`.
13. PostgreSQL valida `NOT NULL`, `UNIQUE` y la PK, inserta la fila y devuelve el `id` generado.
14. El camino se recorre al revés: entidad → `toDomain()` → dominio → `toResponseDTO()` → DTO de salida.
15. `ResponseEntity.status(201).body(...)` devuelve **201 Created** y Jackson serializa el DTO a JSON.

La frase de cierre, si te piden resumirlo en una línea: *entra JSON, Spring MVC lo convierte en DTO, Jakarta Validation valida el contrato, el Controller traduce a dominio y delega en el caso de uso, el caso de uso usa un puerto de salida que implementa un adaptador, Hibernate genera el SQL y PostgreSQL garantiza la integridad; la respuesta vuelve traducida a DTO con un código HTTP coherente.*

## 3. El guion del flujo de error

Igual de importante, y mucha gente solo prepara el anterior.

**`POST /api/membresias` con `socioId: 9999`:**

1. Postman envía el POST con el JSON.
2. Spring MVC enruta hacia `MembresiaController`.
3. `@RequestBody` arma el `MembresiaRequestDTO`; `@Valid` lo valida — **pasa**, porque 9999 es un `Long` perfectamente válido. Esto es clave: la validación de formato no puede detectar este problema.
4. El Controller traduce a dominio y llama al Port IN.
5. `MembresiaService` consulta `socioUseCase.buscarPorId(9999)` — al módulo Socio por su **caso de uso público**, no por su repositorio.
6. Devuelve `Optional.empty()`, y el `orElseThrow` lanza `SocioNoEncontradoException`, que **no sabe nada de HTTP**.
7. La excepción sube por las capas sin que nadie la capture.
8. `GlobalExceptionHandler`, anotado con `@RestControllerAdvice`, la intercepta con su `@ExceptionHandler`.
9. Construye un `ApiError` con `status 404`, `message`, `path` y `timestamp`.
10. El cliente recibe **404** con un JSON estructurado, no un volcado de excepción.

**Variante con constraint de base de datos** (crear un socio con email repetido): los pasos 1-4 son iguales, pero en el 5 la validación de formato pasa y no hay regla de negocio que lo atrape; Hibernate ejecuta el INSERT, PostgreSQL lo rechaza con `duplicate key value violates unique constraint "socio_email_key"`, Spring traduce esa excepción del driver a `DataIntegrityViolationException`, y el manejador la convierte en **409 Conflict**.

Que el mismo tipo de problema (dato inválido) pueda salir como 400, 404 o 409 según **dónde se detecte** es justamente lo que demuestra que entendiste las tres capas de validación.

## 4. Preguntas que te van a hacer y cómo atacarlas

No memorices respuestas; memoriza **de dónde sale cada respuesta**.

| Si te preguntan por… | Abre / señala |
|---|---|
| La diferencia dominio / entidad JPA / DTO | `Socio.java`, `SocioJpaEntity.java`, `SocioRequestDTO.java` lado a lado |
| Dónde vive la FK | La tabla `membresia` en DataGrip, columna `socio_id` |
| Port IN vs Controller | `SocioUseCase.java` y `SocioController.java` |
| Port OUT vs JpaRepository | `SocioRepositoryPort.java` y `SpringDataSocioRepository.java` |
| Qué patrón representa el adaptador | `SocioPersistenceAdapter.java` — patrón Adapter |
| Regla de dependencia | La **lista de imports** de `SocioService.java` |
| Códigos 400/404/409/500 | `GlobalExceptionHandler.java` |
| Por qué `validate` | `application.properties` |
| Validación de formato vs negocio vs integridad | El DTO, el Service y el DDL |
| Una decisión y su alternativa | `11-Decisiones-y-Alternativas.md` |
| Un error y su diagnóstico | `10-Diagnostico-de-Errores.md` |

## 5. Los cinco errores que arruinan una defensa

Salen de las propias guías del profesor:

1. **Decir "lo generó la IA" o "esa parte la hizo mi compañero".** La guía dice literalmente que *"lo generó la IA" no constituye una explicación técnica*, y que desconocer una parte hecha por otro **no es justificación válida**.
2. **Confundir tecnologías.** Decir que JPA e Hibernate son lo mismo, o Spring y Spring Boot, o que `@ManyToOne` "crea" la FK.
3. **Solo el camino feliz.** Que te pregunten qué pasa si el dato está mal y no tengas respuesta.
4. **Recitar sin relacionar.** El criterio del docente pide *definir el concepto, explicar su función, relacionarlo con otros y vincularlo al capítulo*. Una definición suelta suma poco.
5. **Responder con nombres de ParkFlow.** Las preguntas vienen con Cliente y Vehículo; tú respondes con Socio y Membresia. Usa la tabla traductora de `00-Indice-Cap-04-08.md`.

## 6. Qué hacer si no sabes algo

Va a pasar. La peor salida es inventar; el criterio de "diagnóstico y razonamiento" premia el razonamiento incluso sin la respuesta exacta.

La forma correcta tiene tres partes: reconocer el límite, mostrar el razonamiento, proponer cómo lo averiguarías.

> *"No recuerdo el nombre exacto de esa anotación, pero sé que tiene que estar en la entidad JPA porque es donde se describe el mapeo hacia la tabla, y lo confirmaría abriendo `SocioJpaEntity` y comparándola con `information_schema.columns`."*

Eso vale mucho más que una respuesta inventada con seguridad.

## 7. Checklist personal antes del examen

Adaptado de la sección 18 de la Guía Formal:

- [ ] Conozco el alcance exacto que será evaluado.
- [ ] Puedo levantar el proyecto sin depender de nadie.
- [ ] Puedo dibujar el modelo de datos de memoria, con PK, FK y constraints.
- [ ] Puedo ejecutar y explicar las consultas SQL importantes.
- [ ] Puedo ubicar dominio, aplicación e infraestructura en el código.
- [ ] Puedo seguir al menos dos flujos end-to-end (uno feliz y uno de error).
- [ ] Conozco las validaciones y qué error produce cada una.
- [ ] Puedo justificar decisiones que no tomé personalmente.
- [ ] Conozco los principales problemas técnicos que tuvo el equipo y cómo se resolvieron.
- [ ] Revisé código hecho por compañeros.
- [ ] La documentación coincide con el software real.
- [ ] No hay secretos ni credenciales versionadas.

## Actividad para practicar: simulacro

Hazlo en voz alta y **cronometrado**. Si puedes, que alguien te escuche; si no, grábate.

**Bloque 1 — Conceptual (10 min).** Responde sin mirar código:

1. ¿Cuál es la diferencia entre JPA, Hibernate y Spring Data JPA?
2. ¿Por qué `SocioRequestDTO`, `Socio` y `SocioJpaEntity` no deberían ser la misma clase?
3. ¿Qué diferencia hay entre un Port IN y un Controller?
4. ¿Qué diferencia hay entre 400, 404, 409 y 500? Da un ejemplo tuyo de cada uno.
5. ¿Dónde vive físicamente `socio_id` y por qué de ese lado?

**Bloque 2 — Recorrido (5 min).** Explica el flujo completo de `POST /api/membresias` desde Postman hasta PostgreSQL y de vuelta, nombrando **todas** tus clases reales en orden.

**Bloque 3 — Práctico (15 min).** Con el proyecto abierto:

1. Levanta la aplicación y demuestra que responde.
2. Crea un socio por Postman y demuestra en DataGrip que la fila existe.
3. Localiza en el código dónde se valida que el email tenga formato correcto.
4. Modifica algo en caliente: agrega el campo `ciudad` al `SocioResponseDTO` y haz que se devuelva. Cronométrate — deberías tardar menos de diez minutos.
5. Muestra un error real: manda un email duplicado y explica el recorrido completo de esa excepción.

**Bloque 4 — Justificación (10 min).** Elige tres decisiones de `11-Decisiones-y-Alternativas.md` y explícalas con los cuatro tiempos: qué, por qué, qué alternativa, qué se paga.

**Bloque 5 — Diagnóstico (10 min).** Pídele a alguien que rompa algo en tu proyecto sin decirte qué (quitar una anotación, cambiar un nombre de columna, borrar un `@Valid`). Diagnostícalo narrando en voz alta: qué dice el error, en qué capa está, qué hipótesis tienes, qué prueba harías.

Ese bloque 5 es el que más se parece a lo que el docente va a hacer, y el que casi nadie practica.
