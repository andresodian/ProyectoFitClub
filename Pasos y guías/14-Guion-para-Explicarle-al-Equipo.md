# Guía 14 — Cómo explicarle todo esto al equipo (guion de sesión)

Esta guía es distinta a las trece anteriores. Aquéllas te enseñan a construir. Esta te prepara para **enseñar tú**, incluso si ahora mismo sientes que tienes los términos revueltos. Por eso está partida en dos:

- **Parte A** es solo para ti, antes de reunir a nadie. Es el repaso más corto y más directo posible: si lo lees una vez con calma, las piezas dejan de sentirse sueltas.
- **Parte B** es el guion literal para la sesión con tu equipo: qué decir, en qué orden, y qué mostrar en pantalla en cada momento.

No hace falta que te aprendas nada de memoria. La idea es que tengas esta guía abierta en una pantalla mientras compartes la otra.

---

## PARTE A — Tu repaso, antes de armar la sesión

### A.1 — El dibujo que resuelve la mitad de la confusión

Todo lo que hicimos es **una sola cadena**, siempre en el mismo orden. Una vez que la ves como cadena y no como una lista de nombres sueltos, cada pieza tiene un lugar obvio.

```
Postman  →  Controller  →  Service (UseCase)  →  Port  →  Adapter  →  Repository  →  Hibernate  →  PostgreSQL
 (pide)      (recibe)         (decide)         (contrato)  (cumple)    (ejecuta)      (traduce)      (guarda)
```

La respuesta hace exactamente el camino de regreso, capa por capa, hasta volver a Postman.

Ninguna capa se salta a otra. El Controller nunca le habla directo a PostgreSQL; el Service nunca arma JSON. Cada una solo conoce a la de al lado. Esa es, en una frase, **toda** la arquitectura hexagonal: nadie conoce más de lo que necesita para hacer su parte.

### A.2 — El glosario exacto: cada palabra, una vez, sin vueltas

Esta tabla es el corazón de tu repaso. Para cada término tienes: qué es en una frase, una imagen para no olvidarlo, y el nombre real que tiene en `PruebaFitclub` — así cuando lo digas en la sesión, señalas código de verdad y no hablas en el aire.

| Término | Qué es, en una frase | Imagen para recordarlo | Su nombre real en FitClub |
|---|---|---|---|
| **DTO** (Data Transfer Object) | La forma exacta del JSON que entra o sale por HTTP. No tiene lógica, solo datos. | El formulario de papel que llenas en la recepción. | `SocioRequestDTO`, `SocioResponseDTO` |
| **Modelo de dominio** | La clase que representa el concepto de negocio "Socio", sin saber nada de HTTP ni de bases de datos. | La idea de "un socio" en la cabeza del dueño del gimnasio, antes de que exista cualquier sistema. | `Socio` (en `domain/model`) |
| **Entidad JPA** | La clase que representa una fila de una tabla. Tiene anotaciones (`@Entity`, `@Column`) que solo le importan a Hibernate. | La ficha física archivada en el gabinete de la base de datos. | `SocioJpaEntity` |
| **Controller** | Recibe la petición HTTP, valida que el JSON tenga la forma correcta, y le pasa el trabajo a otro. No decide nada de negocio. | El recepcionista: te recibe, revisa que llenaste bien el formulario, y pasa tu caso adentro. | `SocioController` |
| **Port IN** | La interfaz que dice "esto es lo que el sistema sabe hacer" (registrar, listar, buscar), sin mencionar HTTP ni JSON. | El menú del gimnasio: la lista de servicios que ofrece, sin decir por qué canal los pides. | `SocioUseCase` |
| **Port OUT** | La interfaz que dice "esto es lo que el sistema necesita del exterior para funcionar" (guardar, recuperar), sin mencionar PostgreSQL ni Spring Data. | La lista de pedidos del gimnasio a sus proveedores, sin decir a cuál proveedor específico se le compra. | `SocioRepositoryPort` |
| **Service / caso de uso** | La clase que aplica las reglas de negocio. Implementa el Port IN y usa el Port OUT. | El encargado que de verdad sabe las reglas del gimnasio y decide qué hacer con tu solicitud. | `SocioService` |
| **Adapter (de persistencia)** | La única clase autorizada a conocer Spring Data JPA. Traduce entre el lenguaje del negocio y el de la base de datos. | El archivista: es el único que entra al cuarto de archivos; a todos los demás solo les entrega o recibe carpetas ya traducidas. | `SocioPersistenceAdapter` |
| **Mapper** | Una clase de solo traducción, sin lógica de negocio, que convierte de una forma a otra. Hay dos, uno por cada frontera. | Un traductor de idiomas: no opina, solo convierte. | `SocioWebMapper` (DTO↔dominio) y `SocioPersistenceMapper` (dominio↔entidad JPA) |
| **Repository de Spring Data** | Una interfaz que tú declaras vacía, y Spring Data le construye la implementación sola en tiempo de ejecución. | Un empleado que ya sabe hacer su trabajo con solo leer el título del puesto — no necesitas escribirle el manual. | `SpringDataSocioRepository` |
| **`ApiError`** | La forma fija que tiene **cualquier** error que devuelve la API, sin importar cuál haya sido. | Un formulario único de reporte de incidentes: siempre los mismos campos, sin importar qué pasó. | `ApiError` (en `shared/infrastructure/web`) |
| **`@RestControllerAdvice` + `@ExceptionHandler`** | Un lugar único que atrapa los errores de **todos** los Controllers y arma el `ApiError`, en vez de repetir ese código en cada endpoint. | La oficina central de reclamos del gimnasio: no importa en qué sucursal ocurrió el problema, todos los reclamos terminan procesándose igual, ahí. | `GlobalExceptionHandler` |
| **`@Transactional`** | Marca que un método debe hacerse todo o nada: si algo falla a la mitad, se deshace todo lo anterior. | Firmar un contrato completo o no firmar nada — no puedes quedar "firmado a la mitad". | Está en los métodos `registrar(...)` de los Services |
| **`ddl-auto=validate`** | La configuración que le dice a Hibernate: "compara tus clases contra la base real, y si no calzan, avísame — pero nunca toques la base tú solo". | Un inspector que revisa que los planos coincidan con el edificio ya construido, pero jamás tiene permiso de tumbar una pared. | En `application.properties` |
| **DIP** (inversión de dependencias) | El **principio**: el código importante no debe depender de detalles técnicos; ambos deben depender de una interfaz en el medio. | La regla general de "no confíes en un proveedor específico, confía en un contrato que cualquiera puede cumplir". | `SocioService` depende de `SocioRepositoryPort`, no de `SpringDataSocioRepository` |
| **IoC** (inversión de control) | El **mecanismo**: quien crea y entrega los objetos no es tu código, es Spring. | Que la oficina de personal te asigne quién va a trabajar contigo, en vez de que tú salgas a contratar. | Ocurre automáticamente al arrancar la aplicación |
| **DI** (inyección de dependencias) | La **técnica concreta** con la que Spring te entrega esos objetos: por constructor. | El repartidor que te deja el paquete en la puerta — la forma exacta de la entrega. | Los constructores de `SocioController`, `SocioService`, etc. |
| **Swagger / OpenAPI** | Una página web generada sola a partir de tu código, donde se ve y se prueba cada endpoint sin abrir Postman. | El menú ilustrado del restaurante, hecho automáticamente a partir de lo que la cocina de verdad puede preparar. | `/swagger-ui.html` |

Si memorizas una sola fila, que sea esta: **DIP es el principio, IoC es el mecanismo, DI es la técnica**. Es la pregunta que más se repite en los bancos de examen, y son tres cosas distintas que se confunden todo el tiempo.

### A.3 — Las tres preguntas que, si las puedes responder tú, ya puedes enseñar el resto

**¿Por qué `Socio` y `SocioJpaEntity` son clases distintas, si representan "lo mismo"?**
Porque cambian por motivos distintos y a ritmos distintos. `Socio` cambia si cambia una regla del gimnasio. `SocioJpaEntity` cambia si cambia cómo está armada la tabla en PostgreSQL. Si fueran la misma clase, un cambio en la base te obligaría a tocar el negocio, y viceversa — y además el negocio quedaría obligado a cumplir reglas de Hibernate (constructor vacío, sin `final`) que no tienen nada que ver con reglas del gimnasio.

**¿Cuál es la diferencia entre el Port IN y el Controller, si los dos "reciben" cosas?**
El Controller es **una forma concreta** de entrar: HTTP, con JSON. El Port IN es **la capacidad abstracta**: "el sistema sabe registrar un socio", sin decir por qué puerta entró la petición. Podrías tener, al mismo tiempo, un Controller REST y —imagina— un comando de consola, y los dos usarían el mismo Port IN sin duplicar ninguna regla de negocio.

**¿Por qué el Adapter sí puede conocer Spring Data JPA, y el Service no?**
Porque el Adapter vive en la capa de **infraestructura** — su trabajo es justamente ese, hablar con la tecnología concreta. El Service vive en el **núcleo** del negocio, y el objetivo entero del patrón es que ese núcleo no se entere de qué tecnología hay detrás. Si mañana cambias PostgreSQL por otra cosa, reescribes el Adapter y nada más — el Service ni se entera.

Si puedes decir estas tres respuestas con tus propias palabras, ya tienes la base sólida para explicarle al equipo. Todo lo demás son detalles que cuelgan de estas tres ideas.

---

## PARTE B — El guion para la sesión con tu equipo

Pensada para 45–60 minutos, con la pantalla compartida. Cada bloque dice qué tener abierto y qué decir. No lo leas literal palabra por palabra — que suene tuyo — pero sigue el orden: está pensado para que nadie se pierda antes de la parte de código.

### B.0 — Antes de empezar: qué dejar abierto

- **IntelliJ**, con `PruebaFitclub` abierto y el archivo `SocioController.java` visible.
- **Postman**, con la colección de FitClub cargada y la petición `POST /api/socios` lista para enviar.
- **DataGrip**, conectado, con una pestaña de consola apuntando al schema `fitclub`.
- El **HTML de defensa** (`fitclub-defensa-cap04-08.html`) abierto en una pestaña — lo vas a usar al final.
- Ten a la mano las guías `05`, `07` y `08` — son las que más código citan en esta sesión.

No expliques nada "en el aire": cada término que digas, señálalo en el código en pantalla en ese mismo momento. Es la diferencia entre que te entiendan y que te escuchen.

### B.1 — Bloque 1: el panorama completo, sin una sola línea de código (10 min)

Todavía no abras IntelliJ. Empieza dibujando en una pizarra o compartiendo una hoja en blanco.

Diles textualmente algo como:

> "Antes de ver código, quiero que tengan clara una sola idea: todo lo que hicimos es una cadena, siempre en el mismo orden. Imaginen que ustedes son un socio nuevo llegando al gimnasio."

Dibuja, mientras hablas, la cadena de la sección A.1 (Postman → Controller → Service → Port → Adapter → Repository → Hibernate → PostgreSQL), pero cuéntala con la analogía del gimnasio:

> "Postman es como si tú llenaras el formulario de inscripción. El Controller es el recepcionista: revisa que llenaste bien el formulario, pero no decide nada — solo lo pasa adentro. El Service es el encargado que sí conoce las reglas del gimnasio: por ejemplo, que no puede haber dos socios con el mismo correo. El Port es como el contrato del gimnasio con el archivo: 'necesito poder guardar y buscar socios', sin decir en qué mueble específico. El Adapter es el archivista, el único que abre ese mueble de verdad. Y hasta el fondo está PostgreSQL, que es el archivero físico."

Cierra el bloque con esto, porque es la idea que sostiene todo lo demás:

> "Cada uno de estos solo le habla al de al lado. El recepcionista nunca entra al cuarto de archivos. El archivista nunca decide si un socio puede inscribirse o no. Eso es toda la arquitectura hexagonal, resumida: nadie sabe más de lo que necesita para hacer su parte."

### B.2 — Bloque 2: ver una petición viajar de verdad (15 min)

Ahora sí, comparte IntelliJ y Postman lado a lado (o alterna entre ventanas).

1. Abre `SocioController.java`. Señala el método `crear`. Di: "esto es el recepcionista — recibe el DTO, y lo único que hace además de recibir es traducirlo a dominio con el mapper y pasárselo al `SocioUseCase`."
2. Abre `SocioService.java`. Señala la lista de imports (la guía 07, sección 6, lo explica). Di textualmente: "miren esta lista de imports — no hay ni un DTO, ni la entidad JPA, ni nada de Spring Data. Eso no es casualidad, es la prueba de que el Service no sabe nada de tecnología, solo de reglas del gimnasio."
3. Abre `SocioPersistenceAdapter.java`. Di: "este es el único archivo de todo el módulo que tiene permiso de mencionar `SpringDataSocioRepository`. Aquí es donde el dominio se traduce a lenguaje de base de datos."
4. Cambia a Postman. Envía el `POST /api/socios` con un socio nuevo.
5. Mientras se ejecuta, señala la consola de IntelliJ: va a aparecer el `insert into fitclub.socio (...)` generado por Hibernate. Di: "esa línea es la prueba de que Hibernate tradujo nuestro objeto Java a SQL de verdad — nosotros nunca escribimos ese INSERT a mano."
6. Cambia a DataGrip, corre `SELECT * FROM fitclub.socio ORDER BY id DESC;` y muestra la fila nueva.

Cierra el bloque:

> "Vimos la misma petición pasar por cuatro pantallas distintas: Postman, el código, la consola y DataGrip. Las cuatro tienen que coincidir. Si alguna vez una no coincide con las otras, ahí está el error — y eso es literalmente lo que enseña la guía de diagnóstico."

### B.3 — Bloque 3: la parte que más cuesta — puertos y adaptadores (10 min)

Este es el bloque donde más se pierde la gente, así que ve más despacio aquí que en cualquier otro.

Abre `SocioRepositoryPort.java` (una interfaz corta, tres métodos). Di:

> "Esto es solo una lista de promesas: 'guardar', 'listar', 'buscarPorId'. No dice cómo se cumplen. Es literalmente un contrato en blanco."

Abre `SocioPersistenceAdapter.java` de nuevo. Di:

> "Y esta clase es la que firma ese contrato y lo cumple, usando Spring Data JPA por dentro. Si mañana cambiáramos PostgreSQL por otra base de datos, o incluso por archivos de texto, solo reescribiríamos esta clase. El Service de arriba jamás se enteraría, porque él solo conoce el contrato, no a quién lo cumple."

Si alguien pregunta "¿y para qué tanta vuelta si al final hace lo mismo?", responde con la frase de la guía 07:

> "Tienen razón en que hoy, con dos entidades, se siente exagerado. La ganancia se nota cuando el proyecto crece: sin esto, cambiar de tecnología de base de datos significaría reescribir el Service completo. Con esto, se reescribe solo el Adapter."

### B.4 — Bloque 4: qué pasa cuando algo sale mal (10 min)

Vuelve a Postman. Manda, en orden, estas tres peticiones y muestra la respuesta de cada una:

1. Un `POST /api/socios` con un email repetido → **409**, con el `ApiError` en el cuerpo.
2. Un `POST /api/socios` con el email sin arroba → **400**, con `fieldErrors` listando el campo malo.
3. Un `GET /api/socios/9999` (un id que no existe) → **404**, con el `ApiError` diciendo que no existe.

Para cada una, señala el `ApiError` en la respuesta y di:

> "Fíjense que los tres tienen exactamente la misma forma: `timestamp`, `status`, `error`, `message`, `path`. No importa cuál haya sido el problema, el formato del error nunca cambia. Eso es a propósito: así el frontend puede mostrar cualquier error con una sola función, sin casos especiales."

Explica en una frase por qué son códigos distintos:

> "400 es 'lo que mandaste está mal escrito'. 404 es 'lo que buscas no existe'. 409 es 'lo que mandaste está bien escrito, pero choca con algo que ya existe'. La regla corta: 4xx siempre es 'culpa de quien pide'; 500 sería 'culpa nuestra'."

### B.5 — Bloque 5: que ellos lo digan de vuelta, y un mini-ejercicio en vivo (10–15 min)

Esta parte es la más importante de toda la sesión, aunque parezca la más simple.

Elige a un compañero y pídele, sin ayuda: "explícame tú ahora, con tus palabras, qué pasa desde que se envía el POST hasta que se guarda en la base." Dale el espacio para que se trabe — ahí es exactamente donde está el hueco real que hay que reforzar, no antes.

Después, hagan juntos un ejercicio corto en código, con otra persona del equipo escribiendo (no tú):

> "Vamos a agregar una validación nueva: que `nombre` en `SocioRequestDTO` no pueda tener menos de 3 caracteres. ¿Quién le pone las manos al teclado?"

Guíalos a que agreguen `@Size(min = 3)` sobre el campo, reinicien la app, y prueben en Postman con un nombre de dos letras — deben ver un 400. Este ejercicio dura cinco minutos y deja clarísimo, de forma práctica, cómo una anotación en el DTO se convierte en una respuesta HTTP real.

### B.6 — Cierre (5 min)

Diles exactamente esto, porque es el dato que más les va a importar:

> "El examen pesa 80% en la defensa individual: cada quien explica solo, sin que el compañero pueda ayudar. Lo que acabamos de ver aquí no reemplaza que cada uno repase por su cuenta — usen las guías de la carpeta `Pasos y guías` y el HTML de preguntas para practicar solos, tantas veces como haga falta, hasta que puedan explicarlo sin mirar el código."

Y si alguien todavía se ve perdido, dile que no pasa nada — para eso está la Parte D de esta misma guía.

---

## PARTE C — Preguntas que probablemente te van a hacer, con la respuesta exacta

**"¿No es exagerado tener tantas clases para hacer algo tan simple?"**
Con dos entidades, sí se siente exagerado — y es una observación válida, no hay que evadirla. La razón de hacerlo así de todos modos es que es una práctica deliberada para un proyecto que sí va a crecer (el FitClub real, con diez entidades): el patrón hay que aprenderlo en algo chico antes de necesitarlo en algo grande.

**"¿Por qué no ponemos toda la lógica en el Controller y ya?"**
Porque el Controller debería poder cambiar (por ejemplo, agregar un endpoint nuevo, o cambiar de REST a otra forma de entrada) sin tocar ni una regla de negocio. Si la lógica vive ahí, cada cambio de forma de entrada obliga a tocar reglas del gimnasio, y cada cambio de regla obliga a tocar cómo entra la información. Separarlas evita que un cambio arrastre al otro.

**"¿Qué pasa si se me olvida en qué capa va algo?"**
Hay una pregunta de una sola frase que casi siempre resuelve la duda: *¿esto sabe algo de HTTP o de PostgreSQL?* Si la respuesta es sí, va en infraestructura (Controller, DTO, Adapter, entidad JPA). Si la respuesta es no, va en el núcleo (dominio, Service, Ports).

**"¿Esto es lo mismo que en el proyecto del profesor, ParkFlow?"**
Es exactamente el mismo patrón, con otros nombres: `Cliente` es `Socio`, `Vehiculo` es `Membresia`. La guía `00-Indice-Cap-04-08.md`, sección 3, tiene la tabla completa de traducción — sirve para cuando una pregunta de examen viene redactada con los nombres del profesor.

**"¿Por qué el DTO del profesor es un `record` y el nuestro es una clase normal?"**
Las dos formas son correctas — no es que una esté mal. `record` es más corto porque genera constructor, getters y `equals`/`hashCode` automáticamente, pero es inmutable. Nosotros usamos clases normales, con setters, lo cual también es válido en Java. La guía `11-Decisiones-y-Alternativas.md` compara las dos a fondo, por si alguien quiere profundizar.

---

## PARTE D — Si alguien se sigue perdiendo, qué hacer distinto

Si notas que después de todo esto alguien sigue sin ubicarse, no repitas la misma explicación más fuerte o más rápido — cambia de estrategia:

**Reduce la cadena a la mitad.** En vez de las ocho capas completas, quédate solo con tres: "algo recibe la petición, algo decide qué hacer, algo la guarda". Una vez que esas tres estén claras, vuelve a partirlas en las ocho reales.

**Sigue un solo endpoint del principio al fin, sin saltar a comparar con otros.** Mezclar `POST /api/socios` con `POST /api/membresias` en la misma explicación duplica la carga mental. Termina uno completo antes de mencionar el segundo.

**Déjalos escribir ellos, aunque sea despacio.** Ver a alguien más escribir código rara vez deja huella; escribirlo uno mismo, aunque sea copiando con calma y explicando cada línea en voz alta, sí. Prioriza esto sobre seguir explicando de palabra.

**Usa siempre el nombre real de FitClub, nunca el de ParkFlow, al explicar.** Traducir mientras se explica agrega una carga extra innecesaria — la tabla de traducción es para el momento del examen, no para aprender.

## Actividad para practicar

No hay código nuevo que escribir en esta guía — la actividad es la sesión misma. Cuando la hagas, evalúala así:

1. Elige a la persona del equipo que menos segura se sienta con el tema.
2. Al final de la sesión, pídele que te explique con sus propias palabras las tres preguntas de la sección A.3, sin mirar ninguna guía.
3. Anota cuál de las tres le costó más. Esa es la que necesita una segunda vuelta — corta, de cinco minutos, señalando otra vez el código real en pantalla — antes del examen.
4. Repite el mismo chequeo con cada integrante del equipo. El objetivo no es que la sesión haya "sonado bien", sino que cada quien pueda responder solo, porque así es como se evalúa en la defensa individual.
