# Diagnóstico de errores: síntoma, causa y solución

La rúbrica de la defensa individual dedica un **15%** a *"diagnóstico y razonamiento: interpreta errores, propone causas, pruebas y correcciones razonables **en lugar de adivinar**"*. Y la guía de preparación dice literalmente: *"practique diagnosticar un fallo: no memorice únicamente el camino feliz"*.

Este archivo reúne los errores que realmente aparecieron construyendo el proyecto, con el método que se usó para encontrar cada causa. El método importa más que la solución: en el examen te van a poner un fallo que no está en esta lista.

## 0. El método, antes que el catálogo

Cuando algo falla, en este orden:

1. **Lee el mensaje completo.** No la primera línea: baja hasta el `Caused by:` final, que es donde está la causa real. Y busca la línea que menciona **una clase tuya** — ahí está el punto de entrada al problema.
2. **Ubica en qué capa ocurrió.** ¿Al arrancar (configuración/esquema)? ¿Al recibir la petición (validación/mapeo)? ¿Al guardar (persistencia/constraints)? Cada capa tiene su familia de errores.
3. **Formula una hipótesis concreta**, no un presentimiento. "Hibernate busca la tabla en `public` y está en `fitclub`" es una hipótesis; "algo está mal con la base" no.
4. **Diséñale una prueba barata.** `SELECT current_database()`, un `Ctrl+N` para contar clases, comentar una línea. Una prueba que confirme o descarte en menos de un minuto.
5. **Corrige la causa, no el síntoma.** Si la tabla está en la base equivocada, la solución no es cambiar `ddl-auto` a `update` para que la cree.

Esa secuencia — *leer, ubicar, hipótesis, prueba, corrección* — es lo que la rúbrica llama "razonamiento" y es lo que hay que poder narrar en voz alta.

---

## 1. Arranque: `Schema-validation: missing table [fitclub.socio]`

**Síntoma.** La aplicación no arranca. En el log:
```
org.hibernate.tool.schema.spi.SchemaManagementException:
Schema-validation: missing table [fitclub.socio]
```

**Qué significa.** `ddl-auto=validate` comparó las entidades contra la base y no encontró la tabla. Está haciendo su trabajo.

**Causas posibles, en orden de probabilidad.**

1. Las tablas se crearon en otra base de datos (la trampa de DataGrip, sección 3).
2. Falta `spring.jpa.properties.hibernate.default_schema=fitclub` y Hibernate busca en `public`.
3. El `@Table` de la entidad tiene mal el nombre o el schema.
4. La URL de conexión apunta a otra base.

**Cómo confirmar cuál es.** Busca en el log de arranque esta línea, que Hibernate imprime al conectarse:
```
Default catalog/schema: pruebafitclub/public
```
Si dice `public` cuando debería decir `fitclub`, es la causa 2. Si dice otra base, es la 4.

Y en DataGrip, la prueba definitiva:
```sql
SELECT current_database(), table_schema, table_name
FROM information_schema.tables
WHERE table_name = 'socio';
```

**Solución.** Según la causa: agregar `default_schema`, recrear las tablas en la base correcta, o corregir `@Table`. **Lo que NO se hace** es cambiar `ddl-auto` a `update` para que Hibernate cree la tabla: eso esconde el problema y termina con tablas duplicadas en dos schemas.

---

## 2. Arranque: `wrong column type` / `missing column`

**Síntoma.** Similar al anterior, pero la tabla existe y el problema es una columna.

**Causa.** El nombre o el tipo Java no coincide con la columna real. Casos típicos: `fechaRegistro` sin `@Column(name = "fecha_registro")`; `precio` como `double` cuando la columna es `NUMERIC`; un campo nuevo en la entidad que nunca se agregó a la tabla.

**Cómo confirmar.**
```sql
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'fitclub' AND table_name = 'socio'
ORDER BY ordinal_position;
```
Compara columna por columna contra tu entidad.

**Solución.** Corregir la entidad para que refleje la base. La base es la fuente de verdad; el Java se adapta a ella, no al revés.

---

## 3. LA TRAMPA DE DATAGRIP: el DDL se ejecuta en la base equivocada

**Síntoma.** Creaste el schema y las tablas, DataGrip dijo "completed successfully", y Hibernate insiste en que no existen. Los dos parecen tener razón.

**Causa.** Una consola SQL de DataGrip tiene, arriba a la derecha, un **selector de base y schema** (`postgres.public`, `pruebafitclub.public`…) que es **independiente del Data Source bajo el que abriste la consola**. La pestaña puede decir `[pruebafitclub@localhost]` y el SQL estar ejecutándose contra la base `postgres`.

**Cómo confirmar.** En la misma consola donde corriste el DDL:
```sql
SELECT current_database(), current_schema();
```
Si no dice `pruebafitclub`, ahí está.

**Solución.** Cambiar el selector de arriba a la derecha, verificar otra vez con la consulta, y recrear. Si dejaste basura en la base equivocada, límpiala:
```sql
-- ¡en la consola apuntando a la base EQUIVOCADA!
DROP SCHEMA fitclub CASCADE;
```

**Prevención.** Ejecuta `SELECT current_database(), current_schema();` antes de **cualquier** DDL. Nos pasó dos veces; la segunda dolió más que la primera.

---

## 4. Conexión: `password authentication failed for user`

**Síntoma.** La aplicación no conecta, o DataGrip falla el Test Connection. Estás seguro de que la contraseña es correcta.

**La trampa circular.** El instinto es abrir una consola y correr `ALTER ROLE ... WITH PASSWORD ...`. Pero si esa consola usa la conexión rota, **no puede ejecutarse**: estás intentando autenticarte con las credenciales que quieres arreglar. El comando parece correr y nada cambia.

**Solución.**

1. Crea en DataGrip un **Data Source nuevo** con el superusuario: usuario `postgres`, base `postgres`.
2. Test Connection hasta que diga *Succeeded*.
3. **Desde esa consola** (verifica que el prompt/pestaña diga `postgres`, no la conexión rota):
   ```sql
   ALTER ROLE pruebafitclub_admin WITH PASSWORD 'fitclub123';
   ```
4. Edita el Data Source original con la contraseña nueva y prueba otra vez.
5. Alinea `application.properties`.

**Lección general.** Para arreglar un acceso roto necesitas un camino de acceso que funcione. Vale para contraseñas, permisos y llaves SSH.

---

## 5. IntelliJ: `Package name X does not correspond to the file path Y`

**Síntoma.** Un archivo `.java` marca error rojo en la primera línea diciendo que el paquete declarado no corresponde a la ruta.

**Causa.** El archivo está físicamente en una carpeta distinta de la que declara su `package`. Pasa al crear una clase teniendo seleccionada la carpeta equivocada en el árbol (típicamente `dto`, que es la última donde estuviste).

**Cómo confirmar.** El propio mensaje te dice las dos rutas: la declarada y la real. En el panel Project, localiza el archivo y comprueba dónde está de verdad.

**Solución.** No muevas el archivo arrastrando ni edites la línea `package` a mano. Usa `Refactor → Move Class...` (**F6**), escribe el paquete de destino completo y confirma. IntelliJ mueve el archivo físico y ajusta imports en todo el proyecto.

**Variante del mismo error.** Crear un paquete nuevo con la carpeta equivocada seleccionada produce anidados absurdos como `...adapter.in.web.dto.adapter.out.persistence.entity`. Se arregla borrando el paquete mal creado y rehaciéndolo con clic derecho **sobre el paquete padre correcto**.

---

## 6. IntelliJ: pegar código en la pestaña equivocada

**Síntoma.** Errores raros que no cuadran: `Duplicate class found`, una clase que "desapareció", `cannot find symbol` para un método que juras haber escrito.

**Causa.** Pegaste el contenido de una clase encima de otra. El archivo se llama `A.java` pero ahora contiene la clase `B`.

**Cómo confirmar — este método es el que vale para el examen.** `Ctrl+N` (Navigate → Class) y escribe el nombre de la clase. **Cuenta los resultados:**

| Resultado | Qué significa |
|---|---|
| 1 resultado | Normal. |
| 2 o más con el mismo paquete | La clase está duplicada: la pegaste en otro archivo. |
| 0 resultados | La clase **ya no existe**: algo la sobrescribió. |

En el proyecto nos pasó exactamente así: `Membresia` daba 2 resultados y `PruebaFitclubApplication` daba **0** — se había pegado el código de Membresia encima del archivo de la clase principal.

**Solución.** Abre por `Ctrl+N` el archivo correcto, **verifica el nombre en la pestaña antes de pegar**, y restaura el contenido que corresponde a cada uno.

**Prevención.** Antes de pegar, mira el nombre de la pestaña. Suena obvio; nos pasó dos veces seguidas.

---

## 7. Compilación: `no suitable constructor found` / `cannot find symbol: method getX()`

**Síntoma.** El build falla diciendo que no encuentra un constructor con esa firma, o un getter que existe.

**Causa.** La clase que se está usando no tiene la forma que crees. Casi siempre: agregaste un campo (por ejemplo `socioId`) y actualizaste el getter pero **no el constructor**, o actualizaste el archivo equivocado (ver sección 6).

**Cómo confirmar.** Abre la clase mencionada en el error y cuenta los parámetros del constructor contra los del error. El mensaje suele mostrar la firma esperada.

**Solución.** Alinear la clase. Si acabas de agregar un campo, revisa los cuatro lugares: el campo, el constructor, el getter y el setter.

---

## 8. Postman: la petición ni siquiera llega

**Síntoma.** Error de conexión, o una respuesta que no viene de tu aplicación.

**Causas frecuentes, en orden.**

| Causa | Cómo se ve | Solución |
|---|---|---|
| `https://` en vez de `http://` | Error de SSL / conexión rechazada | Quita la `s` |
| La aplicación no está corriendo | "Could not send request" | Mira la consola de IntelliJ |
| Puerto equivocado | Lo mismo | Verifica `server.port` |
| Ruta mal escrita (`/api/socio/1` en vez de `/api/socios/1`) | **404** | Ojo: este 404 lo devuelve Spring por ruta inexistente, **no** tu lógica. Un 404 puede ser correcto por el motivo equivocado. |
| Falta `Content-Type: application/json` | **415** Unsupported Media Type | En Body elige *raw → JSON* |

La cuarta merece atención: nos pasó tener un caso de prueba "GET por id inexistente" que daba 404 y parecía correcto, pero la URL tenía `socio` en singular. Estaba probando la página de error por defecto de Spring, no el `buscarPorId`. **Un código correcto por la razón equivocada es una prueba que no prueba nada.**

---

## 9. Validación: un campo obligatorio llega `null`

**Síntoma.** En el log:
```
Field error in object 'membresiaRequestDTO' on field 'socioId': rejected value [null]
```
y el cliente recibe 400.

**Causa.** El JSON enviado no traía ese campo con ese nombre exacto. Jackson distingue mayúsculas: `socioId` ≠ `socioid` ≠ `socio_id`.

**Cómo confirmar.** Mira el JSON en Postman y compáralo carácter por carácter con el nombre del atributo del DTO.

**Solución.** Corregir el nombre en el JSON. Si de verdad quieres aceptar otro nombre en la API, se anota el campo con `@JsonProperty("...")`, pero lo normal es que coincidan.

**Caso hermano:** las anotaciones están puestas pero **no se ejecutan**. Ahí la causa es que falta `@Valid` en el parámetro del Controller. Sin `@Valid`, `@NotBlank` es decoración.

---

## 10. Persistencia: `duplicate key value violates unique constraint`

**Síntoma.**
```
ERROR: duplicate key value violates unique constraint "socio_email_key"
  Detail: Key (email)=(leo@example.com) already exists.
```

**Causa.** Estás insertando un valor que ya existe en una columna `UNIQUE`. **Esto no es un bug**: es la base protegiendo una regla de negocio.

**Cuándo aparece sin que lo busques.** Al re-correr la colección de Postman: las pruebas que crean datos con valores fijos fallan la segunda vez. Correcto y esperable.

**Solución.** Depende de qué quieres:

- Si es una prueba repetible, usa datos variables o limpia antes.
- Si es el flujo real, tu API debe responder **409** — eso es lo que hace el `@ExceptionHandler(DataIntegrityViolationException.class)` del Capítulo 08.

---

## 11. Persistencia: faltan ids en la secuencia

**Síntoma.** Los socios tienen id 1 y 3. El 2 no existe y nadie lo borró.

**Causa.** En PostgreSQL, `GENERATED ALWAYS AS IDENTITY` **consume el número aunque el INSERT falle**. Un intento rechazado por `UNIQUE` se queda con su id.

**Por qué funciona así.** La secuencia está fuera de la transacción a propósito: si retrocediera, dos transacciones concurrentes podrían recibir el mismo número. Se prefiere perder ids a arriesgar colisiones.

**Solución.** Ninguna: no es un error. Pero conviene saberlo, porque puede hacerte creer que un dato se perdió. En el proyecto de práctica, el 500 de "crear membresía con `socioId: 2`" tenía exactamente esta explicación: el socio 2 nunca existió.

---

## 12. Arranque: `Parameter 0 of constructor ... required a bean of type ... that could not be found`

**Síntoma.** Spring no arranca porque no encuentra qué inyectar.

**Causas.**

1. La clase que debía ser el bean no tiene `@Service`, `@Component`, `@Repository` o `@RestController`.
2. Está fuera del paquete raíz (`com.fitclub`), así que el escaneo no la ve.
3. Declaraste una interfaz (por ejemplo `SocioRepositoryPort`) y **nadie la implementa**, o el adaptador que la implementa no está anotado.

**Cómo confirmar.** El mensaje dice exactamente qué tipo falta. Búscalo con `Ctrl+N`, comprueba su paquete y su anotación.

**Solución.** Anotar el bean, o moverlo bajo `com.fitclub` con `Refactor → Move`.

**Caso inverso:** `expected single matching bean but found 2`. Hay dos implementaciones del mismo puerto. Se resuelve con `@Primary` en la que debe ganar, o `@Qualifier("nombre")` en el punto de inyección.

---

## 13. Tabla resumen para repasar rápido

| Síntoma | Primera sospecha | Prueba de un minuto |
|---|---|---|
| `missing table [fitclub.x]` | Base o schema equivocado | `SELECT current_database(), current_schema();` |
| `wrong column type` | Entidad desalineada con la tabla | Consulta a `information_schema.columns` |
| DataGrip dice OK pero Hibernate no ve nada | Selector de base de la consola | `SELECT current_database();` |
| `password authentication failed` | Intento de arreglo circular | Conectar como `postgres` y hacer el `ALTER ROLE` desde ahí |
| `Package name does not correspond` | Archivo en carpeta equivocada | Mirar el árbol; arreglar con F6 |
| `Duplicate class` / clase desaparecida | Pegado en la pestaña equivocada | `Ctrl+N` y contar resultados |
| `no suitable constructor` | Campo agregado sin actualizar el constructor | Contar parámetros |
| 415 en Postman | Falta `Content-Type` | Body → raw → JSON |
| 404 "correcto" pero sospechoso | Ruta mal escrita | Leer la URL entera, letra por letra |
| `rejected value [null]` | Nombre del campo en el JSON | Comparar JSON contra el DTO |
| `duplicate key` | Regla `UNIQUE` haciendo su trabajo | Buscar el valor en la tabla |
| Faltan ids | Secuencia consumida por un INSERT fallido | No es error |
| `required a bean of type` | Falta anotación o implementación | `Ctrl+N` sobre el tipo faltante |

## Actividad para practicar

Esta actividad es distinta: se trata de **romper el proyecto a propósito** y diagnosticar. Hazlo sobre tu módulo de `Entrenador` y, después de cada una, **revierte el cambio**.

Para cada caso anota cuatro cosas: **qué mensaje salió**, **en qué capa ocurrió**, **qué hipótesis formulaste** y **con qué prueba la confirmaste**.

1. Quita `@Column(name = "fecha_contratacion")` de la entidad y arranca.
2. Cambia `@Table(schema = "fitclub")` por `schema = "public"` y arranca.
3. Quita `@Valid` del Controller y manda un POST con el email vacío. ¿Qué código devuelve ahora y por qué es peor que antes?
4. Quita `@Component` del `EntrenadorPersistenceAdapter` y arranca.
5. Manda un POST sin el header `Content-Type`. Anota el código exacto.
6. Manda dos veces el mismo email. Compara el mensaje de PostgreSQL en el log con lo que recibió el cliente.
7. Cambia la URL de una prueba de Postman a `/api/entrenador/9999` (singular). Da 404 — explica por qué ese 404 **no** demuestra que tu `buscarPorId` funcione.

Cuando termines, escribe un párrafo corto respondiendo: *de los siete fallos, ¿cuáles se detectaron al arrancar y cuáles solo al recibir una petición? ¿Por qué es preferible que un error aparezca en el arranque?*
