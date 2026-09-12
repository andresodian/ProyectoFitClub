# PostgreSQL: base, usuario, schema y tablas (previo al Capítulo 05)

Antes de que Spring pueda guardar nada, las tablas tienen que existir físicamente. Esta guía crea la base de datos, el usuario, el schema y las dos tablas del proyecto de práctica, y explica el error de DataGrip que más tiempo nos costó.

La guía `DataGrip.md` de esta misma carpeta cubre la instalación de DataGrip y la conexión inicial. Aquí damos eso por hecho.

## 0. Base de datos, schema y tabla no son lo mismo

Esta distinción entra en el examen (hay un documento del profesor dedicado solo a esto), así que vale la pena dejarla clara antes de escribir SQL.

- Un **servidor PostgreSQL** puede tener varias **bases de datos**. Están aisladas entre sí: desde una conexión a la base A no puedes consultar tablas de la base B.
- Una **base de datos** puede tener varios **schemas**. Un schema es un espacio de nombres dentro de la base: una carpeta lógica donde viven tablas, vistas y secuencias.
- Un **schema** contiene las **tablas**.

Toda base de datos nueva trae un schema llamado `public`. Si no dices nada, todo se crea ahí.

La consecuencia práctica, y la pregunta típica del examen: **conectarte a la base correcta no basta si apuntas al schema equivocado**. `pruebafitclub.public.socio` y `pruebafitclub.fitclub.socio` son dos tablas distintas, y Hibernate solo va a encontrar la que le digas.

## 1. Por qué creamos un schema propio y no usamos `public`

Podríamos haber dejado todo en `public` y habría funcionado igual. Usamos un schema llamado `fitclub` por tres razones:

1. Es lo que hace el profesor en ParkFlow 360 (`parkflow360` es la base, `parkflow` el schema), así que el proyecto queda alineado con el ejemplo oficial.
2. Deja el proyecto separado de cualquier otra cosa que exista en esa base.
3. Obliga a configurar `default_schema` en Hibernate, que es justamente un concepto evaluable.

El costo es que si te olvidas de `spring.jpa.properties.hibernate.default_schema=fitclub`, Hibernate busca en `public` y el arranque falla con `missing table [fitclub.socio]`. Ver `10-Diagnostico-de-Errores.md`.

## 2. Crear el usuario y la base

Conéctate en DataGrip **como el superusuario `postgres`, a la base `postgres`**. Esto importa: no puedes crear una base desde una conexión a esa misma base, y el usuario de la aplicación todavía no existe.

```sql
CREATE ROLE pruebafitclub_admin WITH LOGIN PASSWORD 'fitclub123' CREATEDB;

CREATE DATABASE pruebafitclub
    WITH OWNER = pruebafitclub_admin
         ENCODING = 'UTF8';
```

Qué significa cada cosa:

| Cláusula | Qué hace |
|---|---|
| `CREATE ROLE` | En PostgreSQL usuarios y grupos son lo mismo: roles. |
| `LOGIN` | Sin esto el rol existe pero no puede conectarse. Es el error más común. |
| `PASSWORD` | La contraseña que irá en `application.properties`. |
| `CREATEDB` | Le permite crear bases. Útil en desarrollo; en producción se evita. |
| `OWNER` | El dueño de la base. El dueño puede crear schemas y tablas dentro sin permisos extra. |

Nota: **no uses `SUPERUSER` para el usuario de la aplicación.** Un superusuario ignora todas las restricciones de permisos; si la aplicación se ve comprometida, el atacante tiene el servidor entero. Esa es una pregunta literal de la guía de DataGrip del profesor.

Nota 2: `CREATE DATABASE` no puede ejecutarse dentro de una transacción. Si DataGrip te da un error sobre eso, cambia el modo de transacción de la consola a *Autocommit* (el selector `Tx:` arriba).

## 3. Conectar DataGrip a la base nueva

Crea un **Data Source nuevo** apuntando a `pruebafitclub` con el usuario `pruebafitclub_admin`, y dale **Test Connection**. Debe decir *Succeeded*.

Si la conexión falla por contraseña y estás seguro de que la escribiste bien, lee la sección correspondiente en `10-Diagnostico-de-Errores.md`: hay una trampa circular ahí que nos costó horas.

## 4. LA TRAMPA DE DATAGRIP (léela dos veces)

Esto nos pasó **dos veces** y las dos veces creímos que era un bug de Hibernate.

Una consola SQL en DataGrip tiene, arriba a la derecha, un **selector de base de datos y schema** (se ve como `postgres.public` o `pruebafitclub.public`). Ese selector es **independiente del Data Source bajo el que abriste la consola**. Puedes tener una pestaña que dice `[pruebafitclub@localhost]` y estar ejecutando el SQL contra la base `postgres`.

Resultado: creas el schema y las tablas, DataGrip dice "completed successfully", y luego Hibernate jura que no existen. Ambos tienen razón — están mirando bases distintas.

**Antes de ejecutar cualquier DDL, corre siempre esto:**

```sql
SELECT current_database(), current_schema();
```

Si la respuesta no dice `pruebafitclub`, cambia el selector de arriba a la derecha antes de seguir. No confíes en el nombre de la pestaña.

## 5. Crear el schema y las tablas

Ya verificado el contexto, ejecuta:

```sql
CREATE SCHEMA fitclub;

CREATE TABLE fitclub.socio (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    telefono        VARCHAR(20)  NOT NULL,
    fecha_registro  DATE         NOT NULL
);

CREATE TABLE fitclub.membresia (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo            VARCHAR(20)    NOT NULL,
    fecha_inicio    DATE           NOT NULL,
    fecha_fin       DATE           NOT NULL,
    precio          NUMERIC(10,2)  NOT NULL,
    socio_id        BIGINT         NOT NULL REFERENCES fitclub.socio(id)
);
```

**El orden importa**: `membresia` referencia a `socio`, así que `socio` tiene que existir antes. Si lo haces al revés, PostgreSQL rechaza la FK porque la tabla referenciada no existe.

Por qué cada decisión de tipos y restricciones:

| Decisión | Justificación |
|---|---|
| `BIGINT GENERATED ALWAYS AS IDENTITY` | PK numérica autogenerada por el motor. `ALWAYS` impide que alguien inserte un id a mano y desincronice la secuencia. |
| `VARCHAR(n)` y no `TEXT` | Un largo máximo declarado documenta la intención y atrapa datos absurdos. `TEXT` no dice nada sobre el dominio. |
| `email ... UNIQUE` | Regla de negocio: dos socios no pueden compartir correo. Es la **última** barrera: la app también debería validarlo, pero la base es la que garantiza. |
| `NOT NULL` en todo | Ninguno de esos datos tiene sentido ausente. Un socio sin nombre no es un socio. |
| `NUMERIC(10,2)` para `precio` | Dinero **nunca** en `FLOAT`/`DOUBLE`: esos tipos son binarios y pierden centavos. `NUMERIC` es exacto. |
| `DATE` para las fechas | Son días del calendario, no instantes. Si necesitaras hora y zona, sería `TIMESTAMPTZ`. |
| `socio_id ... REFERENCES` | La FK. Materializa la relación 1:N e impide que exista una membresía apuntando a un socio inexistente. |

## 6. Verificar que quedó donde debía

```sql
SELECT current_database(), table_schema, table_name
FROM information_schema.tables
WHERE table_name IN ('socio', 'membresia');
```

Debe devolver exactamente dos filas, ambas con `pruebafitclub` y `fitclub`. Si dice `public`, o si sale vacío, volviste a caer en la trampa de la sección 4.

Para ver las columnas y confirmar que coinciden con lo que vas a escribir en las entidades JPA:

```sql
SELECT table_name, column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'fitclub'
ORDER BY table_name, ordinal_position;
```

Esta consulta es importante: en el Capítulo 05 vas a escribir clases Java que deben calzar exactamente con estas columnas, y `ddl-auto=validate` no perdona diferencias.

## 7. Probar que las restricciones realmente funcionan

El profesor pide explícitamente provocar errores de integridad y entenderlos, no solo crear tablas. Ejecuta estas tres y **lee el mensaje antes de corregir**:

```sql
-- 1. Viola NOT NULL
INSERT INTO fitclub.socio (nombre, email, telefono, fecha_registro)
VALUES (NULL, 'x@example.com', '0990000000', CURRENT_DATE);

-- 2. Viola UNIQUE (córrela dos veces)
INSERT INTO fitclub.socio (nombre, email, telefono, fecha_registro)
VALUES ('Prueba', 'duplicado@example.com', '0990000000', CURRENT_DATE);

-- 3. Viola FOREIGN KEY
INSERT INTO fitclub.membresia (tipo, fecha_inicio, fecha_fin, precio, socio_id)
VALUES ('MENSUAL', CURRENT_DATE, CURRENT_DATE, 25.00, 999999);
```

Los mensajes que debes reconocer: `null value in column ... violates not-null constraint`, `duplicate key value violates unique constraint "socio_email_key"` y `insert or update on table ... violates foreign key constraint`. En el Capítulo 08 vas a convertir el segundo en un HTTP 409.

Limpia después: `DELETE FROM fitclub.socio WHERE email = 'duplicado@example.com';`

## 8. Un detalle de PostgreSQL que confunde

Si un `INSERT` falla por UNIQUE, **el id de la secuencia igual se consume**. Por eso en el proyecto de práctica existen los socios con id 1 y 3, pero no el 2: el 2 se "quemó" en un intento fallido. No es un bug ni un dato perdido; es el comportamiento normal de `IDENTITY`, y se hace así porque la secuencia no puede retroceder sin bloquear a todos los demás que estén insertando al mismo tiempo.

## 9. Qué NO debes hacer

- No crees las tablas con el asistente gráfico de DataGrip. El SQL tiene que quedar escrito para poder reproducirlo.
- No uses `ddl-auto=create` "para que Hibernate las haga solo". Ver `11-Decisiones-y-Alternativas.md`.
- No guardes la contraseña de PostgreSQL en el repositorio de un proyecto real.
- No crees tablas que tu modelo todavía no justifica.

## 10. Antes de dar por listo este paso

- [ ] `SELECT current_database(), current_schema();` devuelve `pruebafitclub`.
- [ ] Las dos tablas aparecen en `information_schema.tables` con schema `fitclub`.
- [ ] Probaste los tres errores de integridad y puedes explicar qué constraint se activó en cada uno.
- [ ] Puedes explicar la diferencia entre base de datos, schema y tabla sin leer.
- [ ] Puedes explicar por qué `precio` es `NUMERIC` y no `FLOAT`.

## Actividad para practicar

Crea las tablas de tu módulo nuevo. **En la misma base y el mismo schema `fitclub`**, escribe el DDL de:

- `fitclub.entrenador` con `id`, `nombre`, `especialidad`, `email` (único) y `fecha_contratacion`.
- `fitclub.rutina` con `id`, `nombre`, `nivel`, `duracion_minutos` y `entrenador_id` como FK hacia `entrenador`.

Reglas que debes resolver tú:

1. ¿Qué tipo le corresponde a `duracion_minutos`? No es dinero ni texto.
2. `nivel` solo admite `BASICO`, `INTERMEDIO` o `AVANZADO`. Añade un `CHECK` que lo garantice en la base — es una restricción que no usamos en Socio/Membresia, así que no puedes copiarla.
3. Decide y justifica por escrito qué columnas son `NOT NULL` y cuáles no.
4. Ejecuta primero la tabla padre y después la hija. Explica por qué ese orden.

Después de crearlas:

5. Verifica con `information_schema.columns` que quedaron en el schema correcto.
6. Provoca a propósito los cuatro errores: `NOT NULL`, `UNIQUE`, `FOREIGN KEY` y ahora también `CHECK`. Anota el mensaje exacto de cada uno.
7. Escribe en una frase, para cada restricción que pusiste, qué regla de negocio del gimnasio está protegiendo.

La solución está en `13-Actividad-Entrenador-Solucion.md`, pero intenta las siete primero.
