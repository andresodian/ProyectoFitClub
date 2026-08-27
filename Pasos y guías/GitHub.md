# GitHub desde cero (para el equipo)

Este es el lado web de todo lo que ya vimos en el documento de Git. Ahí guardaste y organizaste tus cambios en tu compu; aquí es donde esos cambios quedan visibles para todo el equipo.

## 0. ¿Hay que instalar algo para usar GitHub?

No. GitHub es una página web (github.com), no un programa que se instale en tu compu. Lo único que necesitas es un navegador y conexión a internet, como cualquier otra página. Lo que sí "vive" en tu computadora es Git (documento anterior) — el programa que habla con GitHub por internet cuando haces `push` o `pull`.

## 1. Crear tu cuenta

Entras a github.com, das click en "Sign up" y te pide usuario, correo y contraseña. Un par de cosas que sí importan para el curso:

- Usa un usuario que te identifique fácil (nombre o apodo reconocible) — el profesor y tus compañeros van a ver ese nombre en cada commit y cada Pull Request.
- Verifica tu correo apenas te llegue el mail, porque sin verificar no puedes crear repositorios ni comentar en Pull Requests.
- **Una cuenta por persona.** El manual del profesor lo marca explícitamente como algo que NO se debe hacer: que todo el equipo comparta una sola cuenta. Si eso pasa, es imposible saber quién hizo qué, y parte de tu nota depende de que se vea tu aporte individual.

## 2. Que te agreguen al repo del equipo

Como el repo (`ProyectoFitClub`) ya lo creó Andrés, a ti solo te toca que te inviten como colaborador:

1. Andrés (o quien tenga el repo) va a Settings del repositorio → Collaborators → Add people, y busca tu usuario de GitHub.
2. Te va a llegar una invitación por correo o una notificación en GitHub (la campanita arriba a la derecha).
3. La aceptas, y listo — ya puedes clonar el repo (documento de Git) y subir tus ramas.

Si en algún momento intentas hacer `git push` y te sale un error de permisos, lo más probable es que todavía no te hayan agregado como colaborador — no es un problema de tu compu.

## 3. Crear un repositorio (por si te toca crear uno nuevo)

Esto no les toca ahora porque el repo del equipo ya existe, pero por si en otro momento necesitas crear uno:

1. Click en el botón "+" arriba a la derecha → "New repository".
2. Le pones nombre, decides si es Público o Privado.
3. Te ofrece marcar casillas para agregar automáticamente un `README.md` y un `.gitignore` — normalmente conviene marcarlas, así el repo no nace vacío.
4. Ese repo recién creado es el que después conectas con `git remote add origin` o simplemente clonas, como vimos en el otro documento.

## 4. El Pull Request desde la web (lo que pasa después de tu `git push`)

Este es el paso que completa lo que ya hiciste en Git Bash:

1. Después de subir tu rama (`git push -u origin feature/algo`), entras a GitHub y arriba te va a aparecer un cartel amarillo: *"feature/algo had recent pushes — Compare & pull request"*. Click ahí.
2. Te abre un formulario: le pones un título corto y, abajo, una descripción de qué hiciste y por qué (el manual del profesor sugiere: qué implementaste, qué problema resuelve, cómo se prueba).
3. Click en "Create pull request".
4. Ahora cualquiera del equipo puede entrar a esa página, ver línea por línea qué cambiaste (pestaña "Files changed"), y dejar comentarios si algo no cuadra.
5. Cuando esté aprobado (o si tú mismo revisaste que está bien y nadie más lo bloquea), das click en el botón verde "Merge pull request". Con eso tu rama ya se integró a `main`.
6. **Buena práctica, no lo saltes:** justo en esa misma pantalla, después del merge, aparece el botón "Delete branch" — dale click. Esa rama ya cumplió su función y dejarla ahí solo acumula desorden. (Del lado de tu compu, la copia local se borra por separado con `git branch -d nombre-rama` — está en el documento de Git.)

## 5. Cómo te enteras si hay algo nuevo

- La pestaña **"Pull requests"** del repo te muestra todos los que están abiertos esperando revisión — revísala antes de ponerte a trabajar, por si algo tuyo necesita atención.
- La pestaña **"Commits"** (dentro de `<> Code`, arriba a la derecha donde dice el número de commits) te deja ver el historial completo con quién subió qué y cuándo.
- La campanita de notificaciones te avisa si alguien comentó tu PR o si te asignaron como revisor.

Pero ojo: enterarte de que hay algo nuevo en GitHub no significa que ya lo tienes en tu compu. Para eso sigue haciendo falta el `git pull` que vimos en el documento de Git.

## 6. Rápido para no perderte

- Una cuenta de GitHub por persona, nunca compartida.
- Todo cambio importante entra por Pull Request, no subiendo directo a `main`.
- Antes de abrir un PR, revisa tú mismo la pestaña "Files changed" — es más fácil corregir algo ahí que después de que ya se mezcló.
- Después de mezclar un PR, borra la rama — tanto en GitHub (botón "Delete branch") como en tu compu (`git branch -d nombre-rama`).
