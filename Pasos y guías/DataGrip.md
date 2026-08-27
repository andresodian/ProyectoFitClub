# DataGrip + PostgreSQL desde cero (para el equipo)

Antes de nada, una aclaración que a veces se presta a confusión: PostgreSQL es el motor de base de datos que corre en tu compu (el servidor real, donde viven los datos). DataGrip es solo el programa con el que te conectas a ese motor y le mandas instrucciones SQL. Si cierras DataGrip, tu base sigue viva; si apagas el servicio de PostgreSQL, ahí sí se cae todo.

## 0. Cómo llegas a tener DataGrip

DataGrip no se descarga suelto — se instala a través de JetBrains Toolbox App, un programa que centraliza todas las herramientas de JetBrains (DataGrip, IntelliJ IDEA, WebStorm, Android Studio, etc.). Vas a jetbrains.com/toolbox-app, descargas Toolbox según tu sistema operativo, lo instalas, y desde ahí, en la lista de herramientas disponibles, buscas DataGrip y le das Install. Es el mismo Toolbox con el que instalaron IntelliJ IDEA, WebStorm y Android Studio.

Además necesitas PostgreSQL instalado por separado — eso es el motor en sí, no viene con Toolbox. Se descarga de postgresql.org, y durante su instalación te pide que definas la contraseña del usuario `postgres`, que vas a necesitar en el siguiente paso.

## 1. Crear tu primer proyecto en DataGrip

Abres DataGrip y creas un proyecto nuevo usando PostgreSQL como base — le pones el nombre de tu proyecto. En este primer paso te conectas con los datos del usuario que ya existe por defecto:

| Campo | Valor |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `postgres` |
| User | `postgres` |
| Password | la que pusiste al instalar PostgreSQL |

Este usuario `postgres` es el que trae el motor por defecto — lo usas solo para crear las cosas iniciales, no es con el que vas a trabajar después.

## 2. Crear tu propio usuario

Con ese proyecto abierto, entras a la consola de comandos y escribes:

```sql
CREATE ROLE fitclub_admin
WITH
    LOGIN
    PASSWORD 'tu_clave'
    SUPERUSER
    CREATEDB
    CREATEROLE
    INHERIT;
```

Qué hace cada palabra:

| Palabra | Qué significa |
|---|---|
| `LOGIN` | que ese rol puede iniciar sesión (sin esto sería solo un "grupo", no un usuario real) |
| `PASSWORD` | la clave con la que se conecta |
| `SUPERUSER` | privilegios administrativos casi ilimitados — cómodo para el proyecto de clase, pero **nunca se usa así en un backend real en producción** |
| `CREATEDB` | que puede crear bases de datos nuevas |
| `CREATEROLE` | que puede crear otros usuarios/roles |
| `INHERIT` | que hereda automáticamente los permisos de los grupos a los que pertenezca |

Nota de seguridad honesta: la contraseña que usó el equipo (`hola`) sirve para que el proyecto corra en tu compu, pero si algún día esto se conecta a algo real, se cambia por algo no adivinable — y jamás se sube tal cual a GitHub (documento de Git, sección de `.gitignore`).

## 3. Crear la base de datos

Justo debajo, en la misma pantalla de comandos, se ejecuta por separado:

```sql
CREATE DATABASE fitclubsc OWNER fitclub_admin;
```

Esta es la versión corta del comando — le dice a Postgres "crea la base `fitclubsc` y que `fitclub_admin` sea el dueño". Con eso basta; no hace falta agregarle `ENCODING` ni `TEMPLATE` a mano, Postgres usa sus valores por defecto.

Un detalle técnico: este comando no puede ir pegado a otros dentro de la misma "transacción" — por eso se corrió por separado, después del `CREATE ROLE`, y no todo junto en una sola ejecución.

## 4. Conectar con tu usuario nuevo

Aquí es donde entra ir a "Data Sources" (o crear una conexión nueva) y poner:

| Campo | Valor |
|---|---|
| Database | `fitclubsc` |
| User | `fitclub_admin` |
| Password | la que definiste en el paso 2 |

Le das "Test Connection" — si sale el mensaje verde de éxito, ya quedaste bien conectado. No hace falta correr ningún comando extra para comprobarlo; el mensaje verde ya es la confirmación.

Después, en la pestaña de esquemas (el "1 of 2"), dejas marcada solo `fitclubsc` y desmarcas las demás bases que aparecen por defecto — así DataGrip no te muestra un montón de esquemas que no te interesan.

## 5. Ejecutar tu script de tablas

Con esa conexión seleccionada, abres tu script SQL (el que crea las tablas de FitClub con sus constraints) y lo ejecutas completo. Si algo falla a la mitad, no sigas creando cosas a mano para "parchar" — identifica la línea exacta del error y corrígela ahí.

## 6. Ver que las tablas y sus relaciones quedaron bien (el diagrama)

Primero refrescas: clic derecho sobre la conexión → Refresh/Synchronize.

Después, para ver el diagrama en vez de solo la lista de tablas: clic derecho sobre el schema `fitclub` (o sobre la carpeta `Tables` completa, seleccionando todas) → **Diagrams → Show Visualization**. DataGrip te dibuja cada tabla como una caja con sus columnas, y líneas conectando las que tienen llave foránea entre sí. Esta es la forma real de comprobar que todo quedó bien — no solo que las tablas existen, sino que están conectadas entre ellas como se supone (por ejemplo, que `reserva_clase` sí quedó enlazada con `membresia` y con `horario_clase`).

Si alguna tabla aparece suelta en el diagrama, sin ninguna línea conectándola a las demás cuando debería tenerla, es señal de que alguna FK no se creó bien en el script.

## 7. Si algo sale mal

| Te sale esto | Casi siempre significa | Cómo revisarlo |
|---|---|---|
| `Connection refused` | el servicio de PostgreSQL no está corriendo, o el puerto está mal | revisa que el servicio esté iniciado |
| `password authentication failed` | usuario o contraseña incorrectos | revisa mayúsculas/minúsculas |
| `permission denied to create database` | el rol con el que estás conectado no tiene `CREATEDB` | conéctate como `postgres` para crear el rol de nuevo |
| `CREATE DATABASE cannot run inside a transaction block` | lo corriste pegado a otro comando | ejecútalo solo, como en el paso 3 |
| No aparecen tablas nuevas | falta refrescar | clic derecho → Refresh/Synchronize |

Si alguna vez tienes dudas de en qué base/usuario estás conectado (por ejemplo, después de tener varias conexiones abiertas), puedes confirmarlo con un comando rápido, aunque para este proyecto no hizo falta usarlo:
```sql
SELECT current_user, current_database();
```

## 8. Para no meter la pata

- `SUPERUSER` está bien para el proyecto de clase, pero no es como se conecta un backend real en producción.
- La contraseña real nunca va en un commit de Git — ni siquiera de ejemplo.
- El `CREATE DATABASE` va solo, nunca pegado con otros comandos en la misma ejecución.
