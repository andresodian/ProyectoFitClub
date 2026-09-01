# IntelliJ IDEA desde cero (para el equipo)

## 0. Cómo llegas a tener IntelliJ IDEA

Se instala a través de JetBrains Toolbox App (documento de DataGrip, sección 0, por si no lo tienes instalado todavía). Dentro de Toolbox, buscas "IntelliJ IDEA" y le das Install — hay versión Community (gratis) y Ultimate (de paga, aunque como estudiantes suelen tener acceso gratis con correo académico). Para lo que hacemos en el curso, Community alcanza perfecto.

## 1. Instalar Java 21 (el JDK, no solo el IDE)

Esto es aparte de IntelliJ — IntelliJ es el programa donde escribes código, pero necesita un JDK (Java Development Kit) real instalado para poder compilar y ejecutar. Hay dos formas de conseguirlo, y cualquiera de las dos está bien:

**Opción A — dejar que IntelliJ lo descargue por ti (la más simple):** cuando llegues al paso de crear el proyecto (siguiente sección) y tengas que elegir el JDK, ahí mismo hay una opción "Download JDK...". Seleccionas versión **21**, un proveedor (por ejemplo Eclipse Temurin o Oracle OpenJDK — cualquiera de los dos funciona igual para este curso), y le das Download. IntelliJ se encarga de todo.

**Opción B — instalarlo tú antes, por separado:** vas a la página de Adoptium (adoptium.net) o a la de Oracle (oracle.com/java), descargas el instalador de la versión 21, y lo corres como cualquier programa. Después, en IntelliJ, ese JDK va a aparecer disponible para seleccionar.

Para comprobar que quedó bien instalado (sea cual sea la opción que usaste), abres una terminal (puede ser Git Bash o la terminal integrada de IntelliJ) y corres:
```bash
java -version
javac -version
```
Ambos comandos te deben mostrar algo con `21` en el número de versión. Si te sale una versión distinta o dice que el comando no existe, hay que revisar la instalación antes de seguir.

## 2. Crear el proyecto (sin Spring Boot todavía)

1. Abres IntelliJ. Si es tu primer proyecto te va a mostrar la pantalla de bienvenida; si ya tienes otros, es File → New → Project.
2. Eliges **Java** como tipo de proyecto en la lista de la izquierda (no Spring Boot, no Maven todavía en este primer paso — eso viene más adelante en el curso).
3. En "Name" escribes el nombre acordado por el equipo (para nosotros, `fitclub`).
4. En "Location" eliges una carpeta de trabajo fija y conocida — no el Escritorio ni una carpeta temporal, algo que sepas que no vas a mover ni borrar por accidente.
5. En "JDK" seleccionas **21** (siguiendo lo que armaste en el paso anterior — ya sea el que IntelliJ descargó o el que instalaste tú).
6. Dejas todo lo demás sin marcar (sin frameworks, sin librerías extra) y le das "Create".
7. IntelliJ va a tardar unos segundos indexando el proyecto — espera a que termine antes de tocar nada, vas a ver una barra de progreso abajo.

## 3. El aviso de "Add file to Git" — acéptalo

Como el proyecto ya está dentro de una carpeta con Git (o en cuanto conectes uno), en el momento en que empieces a crear archivos nuevos (por ejemplo tu primera clase `Main.java`), te va a aparecer una ventanita emergente preguntando algo como *"Add file(s) to Git?"* con dos botones, uno para agregarlo y otro para no.

Dale que sí (**Add**). Si no lo aceptas, ese archivo se queda "invisible" para Git — no vas a poder hacer `commit` de él aunque hagas `git add .` normal desde la terminal, porque IntelliJ a veces lo maneja aparte hasta que confirmes esa ventana. Si en esa misma ventana ves una casilla que dice algo como "Don't ask again" o "Add for all files automatically", puedes marcarla para que no te vuelva a preguntar cada vez que crees un archivo — así IntelliJ agrega todo lo nuevo por defecto sin interrumpirte.

Si en algún momento cierras esa ventana sin querer o le das que no, y luego ves que un archivo tuyo no aparece en `git status`, ese es normalmente el motivo — créalo de nuevo o agrégalo a mano con `git add nombre-del-archivo`.

## 4. Dónde y cómo crear tus packages

Todo lo que crees en Java tiene que ir DENTRO de `src` (o `src/main/java` si el proyecto usa Maven, como el nuestro). Nunca crees clases sueltas fuera de ahí, ni dentro de carpetas como `.idea` o `target` — esas son de configuración interna del proyecto, no de tu código.

Paso a paso para crear un package:
1. En el panel izquierdo (Project), localizas `java` dentro de `src/main/java`.
2. Clic derecho sobre esa carpeta → New → Package.
3. Escribes el nombre completo de una vez, por ejemplo `com.fitclub` — no hace falta crear `com`, después `fitclub` por separado, con escribir el punto ya crea ambos niveles.
4. Para los siguientes niveles (`com.fitclub.socio`, y dentro `domain.model`), repites: clic derecho sobre el package que ya existe → New → Package → escribes el resto de la ruta.

## 5. Las reglas de nombres que usamos (no son gusto personal, son la convención del curso)

| Qué es | Cómo se escribe | Ejemplo real de FitClub |
|---|---|---|
| Carpeta del proyecto | kebab-case (minúsculas con guion) | `fitclub-backend-lab` |
| Package | minúsculas, sin guiones ni espacios | `com.fitclub.clase.domain.model` |
| Clases, interfaces, enums | PascalCase (cada palabra con mayúscula) | `Asistencia`, `HorarioClase` |
| Métodos y variables | camelCase (primera palabra en minúscula) | `getReservaClaseId()`, `fechaHoraEntrada` |
| Constantes de enum | MAYÚSCULAS_CON_GUION_BAJO | `PROGRAMADA`, `RECORDATORIO_CLASE` |

Un archivo Java siempre debe llamarse igual que su clase pública — `Asistencia.java` tiene que contener `public class Asistencia`, no otro nombre. Si en algún momento renombras una clase, usa Shift+F6 (Refactor → Rename) en vez de cambiar el nombre a mano — así IntelliJ actualiza también el nombre del archivo y todos los lugares donde se usa esa clase.

## 6. Qué es un package (y qué NO es)

Un package es solo una forma de organizar tus clases dentro del código — le indica a Java "esta clase vive lógicamente aquí". **No es una tabla, no es un schema de PostgreSQL, y no crea ninguna conexión con la base de datos.** Cuando ves arriba de una clase algo como:
```java
package com.fitclub.clase.domain.model;
```
eso es solo la dirección de esa clase dentro del proyecto. La relación real con la base de datos (columnas, FKs, etc.) todavía no existe en este punto — eso llega después, cuando el curso incorpore JPA.

## 7. Así quedó organizada la estructura real de FitClub

```
fitclub/
└── src/main/java/com/fitclub/
    ├── Main.java
    ├── socio/
    │   ├── domain/
    │   │   ├── model/           (Socio, Notificacion, TipoNotificacion, PrioridadNotificacion, CanalNotificacion)
    │   │   ├── exception/       (SocioNoEncontradoException, DocumentoDuplicadoException)
    │   │   └── port/            (SocioRepository — el contrato)
    │   ├── application/         (SocioService)
    │   └── infrastructure/
    │       └── memory/          (SocioRepositoryEnMemoria)
    ├── plan/
    │   ├── domain/model/        (Plan, Membresia, HistorialMembresia, EstadoMembresia, TipoEventoMembresia)
    │   └── application/
    │       └── command/         (RegistrarMembresiaCommand)
    └── clase/domain/model/      (Clase, Instructor, HorarioClase, ReservaClase, Asistencia, Intensidad, EstadoHorarioClase, EstadoReservaClase, EstadoAsistencia)
```
Fíjense que no usamos nombres de ParkFlow (`com.parkflow`, `customer`, `vehicle`) — el profesor deja clarísimo que esos son solo el ejemplo, y que cada equipo debe traducir el patrón a sus propias entidades. Nosotros lo hicimos bien: `com.fitclub`, organizado por los módulos reales del gimnasio.

Las carpetas `domain/exception`, `domain/port`, `application` e `infrastructure/memory` se agregaron después, en el Capítulo 02 — si todavía no llegas a esa parte, tu estructura solo va a tener `domain/model` por ahora, y está bien así. El detalle completo de esas piezas está en `Interfaz.md`.

## 8. Qué NO se debe crear todavía

Ni el profesor lo pide en esta etapa, así que si ven código de otro equipo con esto, no es que vayan atrasados:

- Nada de `@Entity` ni anotaciones de Spring.
- Nada de `JpaRepository`.
- Nada de conexión directa Java↔PostgreSQL.
- Nada de controllers ni `@RestController`.

Por ahora las clases son objetos Java normales, con atributos `private`, constructor, y getters/setters — así están las nuestras ahora mismo, y está bien que sea así.

## 9. Antes de dar por "lista" una clase

- ¿Los atributos son `private` (no `public`)? Un atributo público se puede cambiar desde cualquier parte del programa sin control — eso es justo lo que queremos evitar.
- ¿El archivo se llama igual que la clase pública que contiene?
- ¿El proyecto compila y corre sin errores? (Shift+F10 corre la clase `Main` seleccionada, o el botón verde de "play" arriba a la derecha)
- ¿Puedes explicar en una frase qué representa cada clase, sin mirar el código?

## 10. Subir el avance a Git

Una vez que tus clases compilan y hacen lo que deben, sigues exactamente el mismo método del documento de Git — nada de `commit` directo, va con rama:

```bash
git checkout main
git pull
git checkout -b feature/modelo-dominio
```
Te aseguras de partir de la versión más actualizada de `main`, y creas una rama solo para este avance.

```bash
git status
git add .
git commit -m "feat: crear modelo Java inicial del dominio"
git push -u origin feature/modelo-dominio
```
Puedes correr esto desde la terminal integrada de IntelliJ (Alt+F12) o desde Git Bash, es exactamente lo mismo.

Después entras a GitHub, abres el Pull Request desde esa rama, y lo mezclas a `main` — igual que se explica con detalle en los documentos de Git y GitHub.
