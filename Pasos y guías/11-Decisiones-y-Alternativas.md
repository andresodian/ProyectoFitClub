# Decisiones técnicas: por qué se eligió cada cosa y qué se descartó

La rúbrica de la defensa dedica un **15%** (12% de tu nota final) a *"justificación de decisiones: explica por qué se eligió una solución y **reconoce alternativas, límites y consecuencias**"*.

Nota bien lo que pide: no basta con decir *"usamos `validate` porque es lo correcto"*. Hay que poder decir **qué otra cosa se podía haber hecho, por qué se descartó, y qué se pierde con la opción elegida**. Una decisión sin alternativa no es una decisión, es una receta.

Este archivo tiene cada decisión del proyecto en el mismo formato: qué se decidió, qué alternativas había, por qué se eligió esa, y qué se paga por ella.

---

## 1. Schema propio `fitclub` en vez de `public`

**Alternativas:** dejar todo en `public` (lo que pasa si no haces nada) · un schema propio · una base de datos separada por módulo.

**Elegido:** schema propio `fitclub` dentro de la base `pruebafitclub`.

**Por qué.** Es lo que hace el profesor en ParkFlow (base `parkflow360`, schema `parkflow`), separa nuestras tablas de cualquier otra cosa en esa base, y obliga a configurar `default_schema`, que es un concepto evaluable.

**Qué se paga.** Una configuración más que puede olvidarse, y cuando se olvida el error (`missing table [fitclub.socio]`) no dice "te falta `default_schema`" sino que parece que la tabla no existe. Nos costó tiempo.

**Cuándo elegiría distinto.** Para un proyecto de una sola aplicación sin nada más en esa base, `public` habría sido perfectamente defendible. Bases separadas por módulo sería excesivo: perderías la posibilidad de hacer JOIN entre socios y membresías.

---

## 2. `ddl-auto=validate` en vez de `update` o `create`

**Alternativas:** `create` · `create-drop` · `update` · `validate` · `none`.

**Elegido:** `validate`.

**Por qué.** El modelo relacional se diseñó primero, con criterio, en las clases de base de datos. La base es la fuente de verdad y el código Java se adapta a ella. `validate` respeta esa jerarquía y además avisa **al arrancar** si el código y el esquema se desalinean, en vez de dejar que lo descubras en producción.

**Por qué NO las otras.** `create` y `create-drop` borran los datos en cada arranque. `update` es peor que las dos anteriores porque parece inofensivo: modifica el esquema por su cuenta, nunca borra nada, y termina dejando columnas viejas que nadie recuerda para qué eran. Además invierte la autoridad: el código pasa a mandar sobre el diseño de la base.

**Qué se paga.** Cada cambio de modelo requiere dos pasos: el DDL en PostgreSQL y luego la entidad. No puedes "probar rápido" un campo nuevo desde Java.

**El límite honesto.** En un prototipo desechable de un fin de semana, `update` ahorra tiempo real. La decisión depende de si la base es un activo diseñado o un detalle de implementación. Aquí es lo primero.

---

## 3. Dominio y entidad JPA como clases separadas

**Alternativas:** una sola clase con `@Entity` (lo más corto) · dos clases con un mapper.

**Elegido:** dos clases, `Socio` y `SocioJpaEntity`, con `SocioPersistenceMapper` en el medio. Lo exige la guía de Capítulo 05.

**Por qué.** El dominio expresa el negocio; la entidad expresa una tabla. Cambian por razones distintas. Con una sola clase, renombrar una columna te obliga a tocar el negocio, y el dominio deja de poder probarse sin Hibernate. Además JPA impone requisitos (constructor vacío, nada `final`, proxies) que contaminarían un modelo que debería ser Java puro.

**Qué se paga.** Una clase más y un mapper por entidad. Es código aburrido, repetitivo y hay que mantenerlo sincronizado a mano. En un proyecto de dos entidades el costo se siente más que el beneficio.

**Cuándo se justifica de verdad.** Cuando el dominio empieza a tener reglas propias, o cuando la tabla y el concepto dejan de parecerse (una entidad partida en dos tablas, o campos técnicos de auditoría que no son del negocio). En un CRUD puro es discutible — y decirlo así, reconociendo el costo, es mejor defensa que repetir el dogma.

---

## 4. Un Port IN por entidad, no uno por caso de uso

**Alternativas:** `SocioUseCase` con tres métodos · interfaces separadas al estilo del profesor (`RegistrarSocioUseCase`, `ConsultarSocioUseCase`).

**Elegido:** una interfaz por entidad.

**Por qué.** Con tres operaciones de CRUD, tres interfaces de un método cada una es más ceremonia que diseño. La regla que la arquitectura exige —que el Controller dependa de una abstracción y no de la clase concreta— se cumple igual.

**Qué se paga.** Se pierde la granularidad del principio de segregación de interfaces: un cliente que solo necesita consultar recibe también la capacidad de registrar.

**Cuándo cambiaría.** Si el módulo creciera a ocho o diez operaciones, o si distintos actores necesitaran subconjuntos distintos (recepción solo consulta, administración registra), separar por caso de uso pasaría a valer la pena. **Si el profesor pregunta por qué no seguiste su ejemplo con dos interfaces, esta es la respuesta honesta** — y reconocer que su forma escala mejor suma, no resta.

---

## 5. `@Component` en el adaptador y `@Service` en el servicio

**Alternativas:** `@Component` · `@Service` · `@Repository`.

**Elegido:** `@Component` para `SocioPersistenceAdapter`, `@Service` para `SocioService`.

**Por qué.** Las tres son funcionalmente idénticas: registran un bean. La diferencia es de intención. `@Service` comunica "capa de aplicación, caso de uso"; usarla también en el adaptador borraría esa señal.

**El argumento a favor de `@Repository`.** Sería defendible para el adaptador, porque agrega traducción automática de excepciones de persistencia a la jerarquía de Spring. No se usó porque `@Repository` sugiere "esto es un repositorio", y el adaptador no lo es: es un traductor que *usa* un repositorio.

**Qué se paga.** Nada funcional. Es una decisión de comunicación.

---

## 6. `getReferenceById` en vez de `findById` en el adaptador de Membresia

**Alternativas:** `findById(...).orElseThrow(...)` · `getReferenceById(...)`.

**Elegido:** `getReferenceById`.

**Por qué.** Para construir la FK solo hace falta la **referencia** al socio, no sus datos. `getReferenceById` devuelve un proxy sin ejecutar ningún SELECT; `findById` haría una consulta completa cuyos datos se descartarían. Y la existencia del socio ya fue validada por el Service antes de llegar aquí.

**Qué se paga.** Si el id no existiera, el fallo aparecería tarde: al hacer *flush*, como violación de FK, con un mensaje peor. Esta decisión **depende** de que la validación previa exista; si se quitara del Service, habría que volver a `findById`.

**Consecuencia a reconocer en la defensa:** es una optimización que crea un acoplamiento entre dos capas — el adaptador asume algo que garantiza la aplicación. Está documentado y es consciente, pero es una dependencia implícita.

---

## 7. Sin `@OneToMany` en `SocioJpaEntity`

**Alternativas:** relación unidireccional (solo `@ManyToOne`) · bidireccional (además `@OneToMany` en el socio).

**Elegido:** unidireccional. La guía pide explícitamente no agregar la colección sin justificarla.

**Por qué.** La relación ya queda completamente representada desde el lado dependiente. Una colección bidireccional hay que sincronizarla a mano en los dos lados (si agregas al `List` pero no seteas el `socio`, la FK queda nula), invita a cargar todas las membresías al tocar un socio, y complica `equals`/`hashCode`.

**Qué se paga.** No se puede escribir `socio.getMembresias()`. Hay que ir por `membresiaRepository.findBySocioId(id)`.

**Cuándo lo agregaría.** Cuando exista un caso de uso real de "dame el socio **con** sus membresías" que se repita lo suficiente. Incluso entonces, una consulta explícita suele ser mejor que una colección mapeada.

---

## 8. `FetchType.LAZY` en vez de `EAGER`

**Alternativas:** `EAGER` (el valor por defecto de `@ManyToOne`) · `LAZY`.

**Elegido:** `LAZY`.

**Por qué.** La mayoría de las operaciones sobre membresías no necesitan los datos del socio. Con `EAGER`, listar cien membresías dispara cien consultas adicionales: el **problema N+1**.

**Qué se paga.** Si accedes al socio fuera de la sesión de persistencia, obtienes `LazyInitializationException`. Obliga a resolver el mapeo dentro del adaptador, mientras la sesión sigue abierta.

**Relación con otra decisión.** `spring.jpa.open-in-view=false` hace que ese error aparezca antes y más claro, en vez de esconderse tras una sesión abierta hasta el final de la petición. Las dos decisiones se sostienen mutuamente: `LAZY` sin `open-in-view=false` funciona, pero por el motivo equivocado.

---

## 9. `open-in-view=false`

**Alternativas:** `true` (el valor por defecto de Spring Boot) · `false`.

**Elegido:** `false`.

**Por qué.** Con `true`, la sesión de persistencia queda abierta hasta que se termina de escribir la respuesta. Eso hace que las cargas diferidas "funcionen" en lugares donde no deberían, y esconde consultas que se disparan durante la serialización del JSON.

**Qué se paga.** Errores más tempranos y explícitos: si el mapeo no se resolvió dentro del caso de uso, revienta. Es más trabajo al principio y menos sorpresas después.

---

## 10. DTOs como clases en vez de `record`

**Alternativas:** `record` (lo que usa el profesor) · clase con constructor, getters y setters.

**Elegido (en el proyecto de práctica):** clases.

**Por qué.** Jackson deserializa una clase con constructor vacío y setters sin ninguna configuración extra. Con `record` también funciona en las versiones actuales, pero históricamente requirió el módulo de parámetros o anotaciones adicionales.

**Por qué el `record` es mejor argumento.** Es inmutable, no tiene setters (un DTO de entrada no debería poder mutar después de llegar), genera `equals`, `hashCode` y `toString` solos, y el código queda en un tercio de las líneas. El profesor le dedica una presentación entera.

**Qué se paga con la clase.** Verbosidad y mutabilidad innecesaria.

**Cómo defenderlo.** Las dos formas cumplen el contrato. Lo que **no** se puede es desconocer el `record`: si te preguntan, di que sabes que es la forma idiomática para DTOs inmutables, por qué encaja, y que tu proyecto usa clases por compatibilidad directa con la deserialización por setters. Eso es reconocer la alternativa, que es exactamente lo que pide la rúbrica.

---

## 11. Excepciones de dominio propias en vez de `NoSuchElementException`

**Alternativas:** `NoSuchElementException` del JDK (lo que había al principio) · `SocioNoEncontradoException` propia · una excepción genérica `RecursoNoEncontradoException`.

**Elegido:** una excepción propia por entidad.

**Por qué.** El nombre comunica el problema del dominio. Y sobre todo: `NoSuchElementException` puede venir de cualquier `Optional.get()` mal usado en cualquier parte del código; mapearla a 404 convertiría un bug interno en un inocente "no encontrado", escondiendo un error real.

**Por qué no una genérica.** `RecursoNoEncontradoException(tipo, id)` evitaría una clase por entidad, pero pierde precisión: no podrías tratar distinto un socio inexistente de una membresía inexistente si algún día hiciera falta.

**Qué se paga.** Una clase por entidad y una línea más en el `@ExceptionHandler`. Barato.

---

## 12. Un solo `@RestControllerAdvice` global

**Alternativas:** `try/catch` en cada Controller · un advice por módulo · un advice global.

**Elegido:** uno global, en `shared`.

**Por qué.** El formato de error es parte del contrato general de la API, no de un módulo. Centralizarlo garantiza que todos los endpoints respondan igual y evita repetir `try/catch` en cada método.

**Qué se paga.** Un punto único que conoce excepciones de todos los módulos: `GlobalExceptionHandler` importa `SocioNoEncontradoException` y `MembresiaNoEncontradaException`. Con veinte módulos esa lista se vuelve incómoda.

**Cómo escalaría.** Una excepción base común (`RecursoNoEncontradoException`) que las demás extiendan, y el advice manejando solo la base. No se hizo con dos módulos porque habría sido complejidad anticipada.

---

## 13. Un catch-all de `Exception` en el advice

**Alternativas:** no tenerlo (dejar que Spring devuelva su error por defecto) · tenerlo.

**Elegido:** tenerlo, como último manejador.

**Por qué.** Sin él, un fallo inesperado llega al cliente como la página de error de Spring, con formato distinto al resto de la API. Con él, incluso lo imprevisto sale con el mismo contrato `ApiError`.

**El matiz importante.** El banco de preguntas marca como mala práctica *"capturar `Exception` de forma genérica en todos los Controllers"*. Esto es distinto: hay **uno solo**, en el borde del sistema, y no se traga nada — el error sigue quedando en el log. Lo malo es esparcir `catch (Exception e)` por el código, donde sí oculta causas.

**Qué se paga.** Hay que ser disciplinado: si aparece un 500, revisar el log siempre, porque el cliente ya no ve la causa.

---

## 14. `@Transactional` en el caso de uso, no en el Controller

**Alternativas:** en el Controller · en el Service · en el adaptador · en ninguna parte.

**Elegido:** en los métodos de escritura del Service.

**Por qué.** La unidad atómica es la **operación de negocio**, y quien la conoce es el caso de uso. En el Controller, la transacción quedaría abierta durante la serialización de la respuesta, y si el caso de uso se invocara desde otro lugar se quedaría sin transacción.

**El límite que hay que reconocer.** `registrar` hace **una sola** escritura, y una sentencia ya es atómica por sí sola. Hoy la anotación casi no cambia nada. Se pone igual porque marca la frontera correcta y porque en cuanto el método haga dos escrituras (guardar la membresía y su histórico) pasa a ser indispensable sin reestructurar nada.

Decir esto en la defensa —"hoy es casi decorativa, la puse porque marca la frontera correcta"— demuestra más comprensión que afirmar que es imprescindible.

---

## 15. Validar la existencia del socio aunque exista la FK

**Alternativas:** confiar solo en la FK · validar solo en la aplicación · ambas.

**Elegido:** ambas.

**Por qué.** Son barreras de distinto nivel. La aplicación valida **antes** y puede devolver un 404 con un mensaje claro; la FK garantiza que **ningún** camino —ni un bug, ni un INSERT manual— pueda dejar una membresía huérfana. Es **defensa en profundidad**.

**Qué se paga.** Una consulta extra antes de cada inserción. En este volumen es irrelevante.

**Lo que no hay que decir.** Que la validación de la aplicación "reemplaza" a la FK. No la reemplaza: la aplicación mejora el mensaje, la base garantiza la integridad.

---

## 16. `springdoc-openapi` 3.1.1 y no 2.5.0

**Alternativas:** copiar la versión del compañero (2.5.0) · buscar la compatible.

**Elegido:** 3.1.1.

**Por qué.** springdoc tiene versionado propio: la línea 2.x es para Spring Boot 3.x y la 3.x para Spring Boot 4.x. Este proyecto usa Boot 4.1.1.

**La lección general, más valiosa que el dato.** Una dependencia externa no se copia de otro proyecto sin verificar contra qué versión del framework está construida. Fue una decisión de **verificar antes de copiar**, y ese razonamiento es exactamente lo que la rúbrica llama "diagnóstico" aplicado de forma preventiva.

---

## 17. Proyecto de práctica separado en vez de trabajar sobre el FitClub real

**Alternativas:** implementar los Capítulos 04-08 directamente en el proyecto del equipo · un proyecto de práctica aparte con dos entidades.

**Elegido:** el proyecto aparte, `PruebaFitclub`.

**Por qué.** Permite equivocarse sin romper el repositorio del equipo ni ensuciar su historial, y reduce el problema a lo mínimo que permite ver el patrón completo: dos entidades y una relación 1:N.

**Qué se paga, y es importante reconocerlo.** El proyecto real tiene diez entidades y partes construidas por otros integrantes. La rúbrica evalúa la **comprensión del proyecto completo**, incluyendo lo que no implementaste tú. Practicar en un proyecto aparte prepara para explicar *tu* parte y el patrón general, pero no sustituye conocer el código del equipo.

---

## 18. Cómo usar este archivo en la defensa

Si te preguntan por cualquiera de estas decisiones, responde en cuatro tiempos:

1. **Qué se hizo.** "Usamos `ddl-auto=validate`."
2. **Por qué.** "Porque el esquema se diseñó primero y la base es la fuente de verdad; `validate` verifica la correspondencia al arrancar sin modificar nada."
3. **Qué alternativa había y por qué no.** "`update` habría sido más cómodo, pero modifica el esquema por su cuenta, nunca borra, y termina acumulando columnas huérfanas."
4. **Qué se paga.** "El costo es que cada cambio de modelo requiere dos pasos: el DDL y luego la entidad."

Ese cuarto punto es el que separa una respuesta memorizada de una comprendida, y es literalmente lo que la rúbrica llama *"reconoce alternativas, límites y consecuencias"*.

## Actividad para practicar

1. Elige **cinco** decisiones de este archivo, tápalo, y explícalas en voz alta con los cuatro tiempos de la sección 18. Grábate si puedes: vas a notar en qué punto te quedas sin argumentos, y casi siempre es el cuarto.
2. Tu módulo de `Entrenador` te obligó a tomar decisiones propias. Escribe la ficha de cuatro tiempos para cada una de estas:
   - El tipo que elegiste para `duracion_minutos`.
   - Por qué pusiste `CHECK` sobre `nivel` en la base **además** del `@Pattern` en el DTO (o por qué no).
   - Por qué `findByEspecialidad` devuelve `List` y `findByEmail` devuelve `Optional`.
   - Qué campos dejaste fuera del Request DTO y qué pasaría si los aceptaras.
3. Busca en tu propio código **una decisión que no puedas justificar** — algo que hiciste porque la guía lo decía. Investígala hasta poder explicar la alternativa. Ese es el hueco que el docente va a encontrar si no lo encuentras tú primero.
