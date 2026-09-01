# Git desde cero (para el equipo)

Antes que nada: Git y GitHub no son lo mismo, aunque los mezclamos todo el tiempo al hablar. Git es el programa que corre en tu compu (lo usas desde Git Bash) y lleva el historial de cambios de los archivos. GitHub es la página web donde subimos ese historial para que todos lo veamos. No hay tal cosa como "crear una cuenta en Git Bash" — la cuenta la creas en GitHub (eso lo vemos en el otro documento). En Git Bash lo único que haces es decirle quién eres, para que cuando guardes un cambio, quede a tu nombre.

## 0. Cómo consigues Git Bash

Git no viene instalado en Windows por defecto. Lo descargas gratis desde git-scm.com/downloads, eliges la versión de Windows, y corres el instalador. Durante la instalación te va a preguntar un montón de cosas — para el curso, con dejar las opciones que vienen marcadas por defecto es más que suficiente, no hay que tocarles nada raro. Al terminar, si haces clic derecho dentro de cualquier carpeta, te va a aparecer la opción "Git Bash Here" — esa es la terminal que usas para todo lo de este documento. También la puedes abrir buscando "Git Bash" en el menú de inicio.

## 1. Lo primero que haces en cualquier compu nueva

```bash
git --version
```
Esto solo te dice si Git está instalado. Te va a tirar algo como `git version 2.44.0`. Si en cambio te sale que el comando no existe, hay que instalar Git o revisar que esté bien puesto en el PATH — avísale al equipo si te pasa esto.

```bash
git config --global user.name "Tu Nombre"
git config --global user.email "tu-correo@gmail.com"
```
Con esto le dices a Git quién eres tú, en esta computadora, para siempre (por eso el `--global`). Usa el mismo correo con el que te metiste a GitHub. Si no haces esto, tus commits pueden quedar guardados con un nombre genérico tipo "usuario" y nadie va a saber que fuiste tú quien hizo ese cambio — que es justo lo que no queremos, porque parte de la nota es que se vea qué hizo cada uno.

```bash
git config --global --list
```
Este es solo para chequear que quedó bien guardado. Te va a mostrar algo como:
```
user.name=Tu Nombre
user.email=tu-correo@gmail.com
```
Si ves eso, ya estás listo, no lo tienes que volver a hacer en esa compu.

## 2. Bajar el proyecto del equipo a tu compu (lo normal para ustedes)

```bash
git clone URL_DEL_REPOSITORIO
```
Esto descarga TODO el proyecto — no solo los archivos de ahora, sino toda la historia de cambios — y de paso te deja automáticamente conectado a GitHub (a eso le dicen "origin"). No tienes que hacer nada más para vincularte, `clone` ya lo hace solo.

```bash
cd ProyectoFitClub
git remote -v
```
`cd` es simplemente entrar a la carpeta que se acaba de crear. `git remote -v` es para comprobar que quedaste conectado bien — debería mostrarte algo como:
```
origin  https://github.com/andresodian/ProyectoFitClub.git (fetch)
origin  https://github.com/andresodian/ProyectoFitClub.git (push)
```
Si ves dos líneas con la URL del repo del equipo, estás conectado correctamente.

## 3. Si en vez de clonar, te toca crear un proyecto nuevo desde cero

Esto no es lo que van a hacer con FitClub (ese proyecto ya existe y lo clonas, como en el punto anterior), pero conviene que lo conozcas por si en algún otro momento — un proyecto chiquito, una prueba, otra materia — necesitas empezar de la nada, sin nada que clonar.

```bash
mkdir mi-proyecto
cd mi-proyecto
```
`mkdir` crea la carpeta y `cd` te mete adentro. Hasta aquí es una carpeta normal, todavía no tiene nada de Git.

```bash
git init
```
Esto es lo que convierte esa carpeta normal en un repositorio de Git. Por dentro crea una carpeta oculta `.git` donde Git guarda todo su historial y configuración — no la edites a mano ni la borres pensando que es basura, ahí vive literalmente todo tu historial. Si corres `git status` después de esto, te va a decir que estás en la rama `main` y que no hay nada todavía que guardar.

```bash
echo "# Mi Proyecto" > README.md
git status
```
Creas un archivo cualquiera (aquí un README de ejemplo) y le preguntas a Git qué ve. Te va a decir que `README.md` aparece como "untracked" — existe en la carpeta, pero Git todavía no lo está seleccionando, no forma parte de su historial.

```bash
git add README.md
git commit -m "docs: crear README inicial"
```
Igual que en el flujo normal: lo preparas con `add` y lo guardas de verdad con `commit`.

```bash
git log --oneline
```
Te muestra el historial — en este caso, un solo commit, el que acabas de hacer.

Si después de esto quieres subir este proyecto nuevo a GitHub (en vez de clonar uno que ya existe), primero creas el repositorio vacío desde la web de GitHub (documento de GitHub, sección "Crear un repositorio"), y luego lo conectas:
```bash
git remote add origin URL_DEL_REPOSITORIO
git push -u origin main
```
`remote add origin` es lo mismo que `clone` hace automáticamente, pero a mano — le dices a tu repo local a qué repo de GitHub debe conectarse. Y `push -u origin main` sube por primera vez todo lo que ya tenías guardado.

## 4. Cómo sabes si estás al día o si hay algo nuevo

```bash
git status
```
Este es el comando que más vas a usar, literal. Te dice en qué rama estás parado y si tienes cambios hechos que todavía no has guardado. Corre esto antes de hacer cualquier cosa, siempre que tengas duda.

```bash
git fetch
```
Este va y se fija en GitHub si hay algo nuevo que tus compañeros subieron, pero **no te lo trae ni te lo mezcla**, solo te avisa que existe. Es como asomarte a ver si llegó correo, sin abrirlo todavía.

```bash
git pull
```
Este sí trae los cambios nuevos de GitHub y los mezcla con lo que tienes en tu compu. Básicamente es un `fetch` + traerlo de una vez. La rutina de todos los días debería ser: antes de ponerte a programar, párate en `main` y corre `git pull`, para arrancar siempre con la versión más reciente que subieron entre todos.

## 5. Subir tus cambios — la forma correcta, con ramas

Esta es la parte donde más nos podemos meter en problemas si la saltamos, así que va paso por paso con lo que realmente pasa en cada uno:

```bash
git checkout main
git pull
```
Te aseguras de arrancar desde la versión más actualizada de `main`, no desde una copia vieja.

```bash
git checkout -b feature/nombre-del-cambio
```
Esto crea una rama nueva SOLO para lo que vas a hacer ahora, y te mueve a trabajar en ella. Piensa en la rama como una copia paralela donde puedes experimentar sin arriesgar lo que ya funciona en `main`. Por ejemplo, si vas a trabajar en el módulo de reservas, podría llamarse `feature/reservas-clase`.

*(...ahora programas normal, guardas tus archivos como siempre...)*

```bash
git status
```
Antes de guardar nada, revisa qué archivos cambiaste. Te los va a mostrar en rojo si aún no están "preparados" para guardarse.

```bash
git add .
```
Esto "prepara" todos los archivos que modificaste para el próximo guardado (a esto Git le llama staging — no es que ya quedó guardado, es más como ponerlo en la bandeja de salida). Si solo quieres preparar un archivo específico, en vez del punto pones la ruta: `git add src/Clase.java`.

```bash
git commit -m "feat: agregar reserva de clase"
```
Aquí sí guardas el cambio de verdad, con un mensaje que explique QUÉ hiciste. Evita mensajes tipo "cambios" o "arreglo" — en dos semanas ni tú te vas a acordar qué significaban. Un mensaje como `fix: corregir bug al validar horario` dice mucho más.

```bash
git push -u origin feature/nombre-del-cambio
```
Esto sube tu rama a GitHub (todavía no a `main`, solo tu rama). El `-u` es para que la próxima vez que subas algo en esa misma rama, puedas escribir solo `git push` sin repetir todo lo demás.

Después de esto, entras a GitHub por el navegador, te va a aparecer un botón para crear un "Pull Request" desde tu rama — ahí es donde el equipo revisa el cambio antes de que entre a `main`. Eso lo cubrimos con más detalle en el documento de GitHub.

## 6. Traer lo que subieron tus compañeros

Una vez que el Pull Request de alguien ya se mezcló en GitHub:
```bash
git checkout main
git pull
```
Con eso ya tienes en tu compu lo último que se integró, y puedes crear tu próxima rama desde ahí.

## 7. Borrar la rama una vez que ya se mezcló

Después de que tu Pull Request ya se integró a `main` en GitHub, esa rama cumplió su función — dejarla ahí solo genera desorden y hace más difícil saber qué sigue activo y qué ya es historia vieja. Borra la copia local:

```bash
git checkout main
git branch -d feature/nombre-del-cambio
```

El `-d` (minúscula) es la versión segura: Git se niega a borrar la rama si detecta cambios que todavía no se mezclaron a ningún lado, así que no hay riesgo de perder trabajo por accidente. La copia remota (la que quedó en GitHub) se borra desde la propia página del Pull Request, justo después de mezclarlo — eso lo ves con más detalle en el documento de GitHub.

## 8. Si alguna vez te pierdes, corre estos 4 y ya sabes dónde estás parado

| Comando | Qué te dice |
|---|---|
| `git status` | qué cambiaste y en qué rama estás |
| `git branch --show-current` | el nombre exacto de tu rama actual |
| `git log --oneline -5` | tus últimos 5 commits, resumidos |
| `git remote -v` | a qué repo de GitHub estás conectado |

## 9. Para no sufrir con esto

- Antes de tocar cualquier cosa: `git status`.
- Antes de empezar una tarea nueva: párate en `main` y haz `pull`.
- Una tarea = una rama. No mezcles tres cosas distintas en una sola rama.
- No uses `git reset --hard` ni `push --force` si no tienes clarísimo qué van a borrar — pueden perder trabajo sin aviso.
- Nunca subas contraseñas, tokens ni el archivo `.env`. Si algo así se llega a subir por accidente, avísale al equipo — borrarlo del repo después no basta, hay que cambiar esa contraseña/token igual.
- Una vez que tu rama ya se mezcló, bórrala (local y remota) — no dejes ramas viejas acumulándose.
