# Postman: instalar, usar y armar la colección de evidencias

Postman es un **cliente HTTP**. No es parte de tu backend ni una librería del proyecto: es un programa aparte que arma peticiones HTTP y te muestra la respuesta. Lo mismo que hace un navegador cuando entras a una página, pero pudiendo elegir el método, los headers y el body.

Esa aclaración importa porque es una pregunta del banco: *"¿Postman es parte del backend?"*. No. Es una herramienta cliente de prueba, y el backend no sabe si la petición vino de Postman, de React o de una app móvil.

## 0. Descargar e instalar

1. Entra a `https://www.postman.com/downloads/`.
2. Descarga la versión para **Windows 64-bit**. El archivo pesa unos 200 MB.
3. Ejecuta el instalador. No pregunta casi nada; se instala en tu usuario y se abre solo.
4. Al abrirlo te pide crear cuenta o iniciar sesión. **Puedes saltarlo**: abajo del formulario hay un enlace pequeño que dice *"Continue without an account"* o *"Skip and go to the app"*. Sin cuenta funciona todo lo que necesitas; lo único que pierdes es la sincronización en la nube entre computadoras.
5. Si prefieres tener tus colecciones sincronizadas (útil si trabajas en dos máquinas), crea la cuenta con tu correo institucional.

Alternativa si Postman te pesa: IntelliJ trae un **HTTP Client** integrado (archivos `.http`). El profesor lo acepta igual y la guía de Capítulo 04 muestra su sintaxis. La sección 8 de esta guía explica cómo usarlo.

## 1. Las tres piezas que tienes que entender

| Pieza | Qué es |
|---|---|
| **Request** | Una petición: método + URL + headers + body. Es lo mínimo. |
| **Collection** | Una carpeta de requests guardados. Es lo que entregas como evidencia. |
| **Environment** | Un conjunto de variables (por ejemplo `{{baseUrl}}`). Opcional, pero evita repetir `http://localhost:8080` en veinte lugares. |

## 2. Tu primera petición

Con la aplicación corriendo (`Started PruebaFitclubApplication` en la consola de IntelliJ):

1. Clic en **New → HTTP Request** (o el botón `+` de la pestaña).
2. En el selector de método (dice `GET` por defecto) elige **POST**.
3. En la barra de URL escribe:
   ```
   http://localhost:8080/api/socios
   ```
   **Con `http://`, no `https://`.** Tu aplicación local no tiene certificado TLS; si pones `https` la conexión falla antes de llegar al backend. Es un error que cuesta diez minutos la primera vez.
4. Ve a la pestaña **Body**, marca la opción **raw**, y en el desplegable de la derecha (que dice *Text*) elige **JSON**.
5. Escribe el cuerpo:
   ```json
   {
       "nombre": "Leonardo Aguilera",
       "email": "leo@example.com",
       "telefono": "0991234567"
   }
   ```
6. **Send**.

Debes recibir `201 Created` y un JSON con el `id` y la `fechaRegistro` que puso el servidor.

Al elegir *raw → JSON*, Postman agrega solo el header `Content-Type: application/json`. Ese header es el que le dice a Spring cómo interpretar el cuerpo; sin él, `@RequestBody` no sabe qué hacer y responde 415. Puedes comprobarlo en la pestaña **Headers**.

## 3. Leer la respuesta

Abajo, Postman te muestra tres datos que debes saber interpretar:

- **El código de estado** (`201 Created`, en verde). Es lo primero que hay que mirar.
- **El tiempo** (`58 ms`). Útil para notar si algo va mal.
- **El tamaño** (`267 B`). Sirve para detectar respuestas vacías: un 404 sin cuerpo pesa ~130 B; con un `ApiError` completo pesa ~330 B. En el Capítulo 08 vas a usar justo eso para confirmar que tu manejador global está funcionando.

## 4. Las otras peticiones del recurso

| Nombre | Método | URL | Body |
|---|---|---|---|
| Listar socios | GET | `http://localhost:8080/api/socios` | — |
| Obtener por id | GET | `http://localhost:8080/api/socios/1` | — |
| Obtener inexistente | GET | `http://localhost:8080/api/socios/9999` | — |
| Validación falla | POST | `http://localhost:8080/api/socios` | `{"nombre":"","email":"no-es-email","telefono":""}` |

Los GET no llevan body. Si le pones body a un GET, Postman te deja, pero es incorrecto semánticamente y algunos servidores lo ignoran.

## 5. Guardar y organizar la colección

Una petición que no guardas se pierde al cerrar Postman, y la colección es parte de la evidencia que pide el profesor.

1. Con la petición abierta, **Save** (o `Ctrl+S`). Te pide nombre y colección; crea una nueva llamada **`FitClub API`**.
2. Dentro de la colección, clic derecho → **Add Folder**. Crea dos: `Socio` y `Membresia`.
3. Arrastra cada petición a su carpeta.

Nombres recomendados — descriptivos, diciendo qué caso prueban, no "Prueba1":

```
FitClub API
├── Socio
│   ├── Crear socio - OK
│   ├── Crear socio - validación falla
│   ├── Crear socio - email duplicado
│   ├── Listar socios
│   ├── Obtener socio por id - OK
│   └── Obtener socio por id - no existe
└── Membresia
    ├── Crear membresía - OK
    ├── Crear membresía - validación falla
    ├── Crear membresía - socioId inexistente
    ├── Listar membresías
    ├── Obtener membresía por id - OK
    └── Obtener membresía por id - no existe
```

Cuando lo tengas así, borra las peticiones `Get data` y `Post data` que Postman crea de ejemplo.

## 6. Correr toda la colección de una vez

Clic derecho sobre la colección → **Run collection** → **Run**. Postman ejecuta todas las peticiones en orden y te muestra una lista con el código de cada una. Es la forma más rápida de comprobar que nada se rompió después de un refactor: si todos los códigos son los mismos de antes, el cambio fue transparente.

### Un detalle que confunde al re-correr la colección

Las peticiones que **crean** datos con valores fijos fallan la segunda vez, y eso es correcto. `Crear socio - OK` con el email `leo@example.com` da 201 la primera vez y 409 (o 500, si todavía no hiciste el Capítulo 08) la segunda, porque el email ya existe y viola el `UNIQUE`. No es un bug de tu API: es tu API funcionando.

Lo mismo al revés: si tienes una prueba `Obtener por id - no existe` apuntando a `/api/membresias/2` y antes corres `Crear membresía - OK`, esa creación puede ocupar justo el id 2 y tu prueba de 404 empieza a dar 200. **Usa ids altos** (`9999`) para las pruebas de "no existe", así nunca chocan con datos reales.

## 7. Evidencias que pide el profesor

El Capítulo 04 pide entregar, como mínimo:

- [ ] Captura de un POST válido con 201.
- [ ] Captura de un POST inválido con 400.
- [ ] Captura de un GET por id existente con 200.
- [ ] Captura de un GET inexistente con 404.
- [ ] La tabla de contrato HTTP completada (verbo, ruta, entrada, salida, status).

Para capturar: `Win + Shift + S` recorta la pantalla en Windows. Conviene que en la captura se vea **la URL, el método y el código de estado** juntos.

## 8. Alternativa: el HTTP Client de IntelliJ

Si no quieres depender de Postman, crea un archivo `peticiones.http` en la raíz del proyecto:

```http
### Crear socio válido
POST http://localhost:8080/api/socios
Content-Type: application/json

{
  "nombre": "Leonardo Aguilera",
  "email": "leo@example.com",
  "telefono": "0991234567"
}

### Listar socios
GET http://localhost:8080/api/socios

### Buscar por id
GET http://localhost:8080/api/socios/1

### Buscar inexistente
GET http://localhost:8080/api/socios/9999
```

Los `###` separan peticiones. A la izquierda de cada una aparece una flecha verde para ejecutarla. La ventaja sobre Postman: el archivo vive **dentro del repositorio**, así que queda versionado en Git junto al código y cualquier compañero lo tiene al hacer `pull`.

## 9. Qué NO debes hacer

- No uses `https://` contra `localhost`.
- No dejes las peticiones sin guardar ni con nombres como "Prueba1".
- No pruebes solo el camino feliz: la mitad de la evidencia son los errores.
- No confundas Postman con parte del backend.

## 10. Antes de dar por listo este paso

- [ ] La colección `FitClub API` existe, con carpetas y nombres descriptivos.
- [ ] Tienes las cuatro capturas de evidencia.
- [ ] Sabes explicar de dónde sale el header `Content-Type` y para qué sirve.
- [ ] Sabes por qué re-correr `Crear socio - OK` da conflicto la segunda vez.

## Actividad para practicar

1. Crea en tu colección una carpeta nueva llamada **`Entrenador`**.
2. Agrega las seis peticiones del módulo que construiste en la guía 03, con los mismos nombres descriptivos.
3. Corre la colección completa y anota el código de estado de cada una.
4. Provoca a propósito estos tres casos y explica **qué componente generó cada respuesta** (¿Spring por el `@Valid`? ¿tu Controller? ¿PostgreSQL?):
   - `especialidad` con 200 caracteres.
   - `email` sin arroba.
   - `GET /api/entrenadores/9999`.
5. Exporta la colección (clic derecho → Export → Collection v2.1) y guarda el archivo `.json` dentro del repositorio, en una carpeta `docs/`. Así queda versionada como evidencia reproducible.
6. Escribe la tabla de contrato HTTP de tu recurso `entrenadores`, con las cinco columnas: verbo, ruta, entrada, salida y status posibles.
