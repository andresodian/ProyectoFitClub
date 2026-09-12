# Capítulo 08 — Errores profesionales, ApiError y transacciones

Hasta aquí la API funciona, pero falla mal. Un `socioId` inexistente devuelve un 500 con un volcado de excepción; un email duplicado, otro 500; un 404 llega con el cuerpo vacío. Este capítulo convierte una API que funciona en una API **predecible**.

## 0. Tres tipos de validación que no son lo mismo

Pregunta del examen. Se ven parecidos pero ocurren en momentos distintos, los produce gente distinta y devuelven códigos distintos:

| Tipo | Qué comprueba | Dónde vive | Ejemplo en FitClub | Código HTTP |
|---|---|---|---|---|
| **Validación de formato** | Que los datos estén bien escritos | El DTO, con Jakarta Validation | `email` sin arroba; `nombre` vacío; `precio` negativo | 400 |
| **Validación de negocio** | Que la operación tenga sentido según las reglas | El caso de uso | "no existe un socio con ese id"; "el email ya está registrado" | 404 / 409 |
| **Integridad física** | Que la base nunca quede en estado inválido | PostgreSQL: `NOT NULL`, `UNIQUE`, `FK`, `CHECK` | `socio_email_key`; `membresia_socio_id_fkey` | 409 |

Las tres coexisten a propósito: es **defensa en profundidad**. La primera rechaza basura antes de gastar recursos, la segunda aplica el criterio del negocio y da buenos mensajes, la tercera garantiza que ni un bug pueda corromper los datos. Quitar cualquiera de las tres deja un hueco.

## 1. Por qué hace falta un contrato de error

Sin un formato común, cada endpoint inventa su propia respuesta de error: uno devuelve texto plano, otro un JSON distinto, otro nada. El cliente web o móvil tiene que programar un caso especial para cada uno.

Un objeto `ApiError` con forma fija resuelve eso:

- El **frontend** escribe una sola función que muestra errores.
- Las **pruebas** pueden verificar la estructura de la respuesta y no solo el código.
- El **diagnóstico** mejora: `timestamp` y `path` te dicen cuándo y dónde ocurrió sin abrir logs.

## 2. Las excepciones de negocio no conocen HTTP

Regla del profesor: una excepción como `SocioNoEncontradoException` **no debe contener `ResponseEntity`, ni un status, ni nada de HTTP**.

El motivo es de responsabilidades. El dominio sabe que "este socio no existe"; **no sabe ni le importa** que quien preguntó lo hizo por HTTP. Si esa misma regla se ejecutara desde una tarea programada o un consumidor de mensajes, el concepto de 404 no tendría ningún sentido.

Traducir el problema de negocio a un código HTTP es trabajo de la capa web, y se hace en un solo lugar.

```java
package com.fitclub.socio.domain.exception;

public class SocioNoEncontradoException extends RuntimeException {
    public SocioNoEncontradoException(Long id) {
        super("No existe un socio con id " + id);
    }
}
```

```java
package com.fitclub.membresia.domain.exception;

public class MembresiaNoEncontradaException extends RuntimeException {
    public MembresiaNoEncontradaException(Long id) {
        super("No existe una membresía con id " + id);
    }
}
```

Extienden `RuntimeException` (no comprobada) porque son condiciones que el llamador normalmente no puede resolver en el momento: obligar a un `try/catch` en cada nivel ensuciaría todo el camino.

Y por qué una excepción propia y no `NoSuchElementException` del JDK: porque el nombre comunica el problema del dominio, y porque permite tratarla distinto del resto. Una `NoSuchElementException` puede venir de un `Optional.get()` mal usado en cualquier parte; mapearla a 404 convertiría un bug interno en "no encontrado".

## 3. El contrato `ApiError`

Vive en un paquete compartido, porque no pertenece a ningún módulo de negocio: es del contrato HTTP general.

```java
package com.fitclub.shared.infrastructure.web;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> fieldErrors
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, List.of());
    }

    public static ApiError of(int status, String error, String message, String path, List<String> fieldErrors) {
        return new ApiError(Instant.now(), status, error, message, path, fieldErrors);
    }
}
```

Qué aporta cada campo: `timestamp` sitúa el error en el tiempo; `status` repite el código en el cuerpo para clientes que solo leen el body; `error` es el nombre estándar del estado; `message` es legible para una persona; `path` dice qué endpoint falló; `fieldErrors` lista los campos que no pasaron validación.

Es un `record` porque un error, una vez creado, no debe cambiar.

En ParkFlow el profesor lo llama `ApiErrorResponse` y lo pone en `shared/web`. Es el mismo concepto con otro nombre y otra ruta.

## 4. El manejador global

```java
package com.fitclub.shared.infrastructure.web;

import com.fitclub.membresia.domain.exception.MembresiaNoEncontradaException;
import com.fitclub.socio.domain.exception.SocioNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({SocioNoEncontradoException.class, MembresiaNoEncontradaException.class})
    public ResponseEntity<ApiError> handleNoEncontrado(RuntimeException ex, HttpServletRequest request) {
        ApiError error = ApiError.of(HttpStatus.NOT_FOUND.value(), "Not Found",
                ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacion(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        List<String> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError error = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "La solicitud contiene datos inválidos", request.getRequestURI(), errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegridad(DataIntegrityViolationException ex,
                                                     HttpServletRequest request) {
        ApiError error = ApiError.of(HttpStatus.CONFLICT.value(), "Conflict",
                "El registro entra en conflicto con datos existentes (por ejemplo, un valor único duplicado)",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex, HttpServletRequest request) {
        ApiError error = ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "Ocurrió un error inesperado", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### `@RestControllerAdvice`

Es un componente **transversal**: intercepta las excepciones que salen de cualquier Controller de la aplicación. Sin él, cada método tendría que envolver su cuerpo en `try/catch` y construir su propia respuesta — código repetido en decenas de lugares que además se desincroniza apenas alguien olvida actualizar uno.

`@RestControllerAdvice` es `@ControllerAdvice` + `@ResponseBody`: lo que devuelven sus métodos se serializa a JSON.

### `@ExceptionHandler`

Asocia un tipo de excepción con un tratamiento. Spring elige el manejador **más específico** que coincida: si lanzas `SocioNoEncontradoException`, gana el primer método aunque el último acepte `Exception`.

### Un matiz sobre capturar `Exception`

El banco de preguntas marca como mala práctica *capturar `Exception` de forma genérica en todos los Controllers*. Lo que hacemos aquí es distinto y sí es correcto: existe **un solo** catch-all, en el manejador global, como última red de seguridad para que ningún fallo inesperado se escape con un volcado de excepción hacia el cliente. Lo incorrecto es esparcir `catch (Exception e)` por el código, porque se traga errores reales y hace imposible saber qué pasó.

## 5. El significado de cada código

| Código | Qué significa | Cuándo en FitClub |
|---|---|---|
| **400** Bad Request | El cliente mandó algo mal formado. Si lo reintenta igual, vuelve a fallar. | Email sin arroba, campo vacío, `tipo` fuera de la lista, precio negativo |
| **404** Not Found | El recurso pedido no existe. | `GET /api/socios/9999`; crear membresía con `socioId` inexistente |
| **409** Conflict | La petición es válida, pero choca con el estado actual del sistema. | Email ya registrado |
| **500** Internal Server Error | Falló el servidor. **Es culpa nuestra, no del cliente.** | Cualquier cosa no prevista |

La distinción clave: **4xx es culpa del cliente, 5xx es culpa del servidor**. Devolver 500 cuando el cliente mandó datos inválidos es mentirle sobre quién tiene que arreglar algo. Por eso el 500 crudo que teníamos con el `socioId` inexistente estaba mal: el cliente sí podía corregirlo, así que correspondía un 404.

Sobre 404 vs 409: el 404 dice "esto no existe"; el 409 dice "esto existe y por eso no puedo". Crear un socio con un email ya usado no es un dato mal escrito (400) ni algo inexistente (404): es un conflicto con el estado actual.

## 6. Que los Controllers lancen la excepción

El `buscarPorId` del Controller deja de armar el 404 a mano:

```java
@GetMapping("/{id}")
public ResponseEntity<SocioResponseDTO> buscarPorId(@PathVariable Long id) {
    Socio socio = socioUseCase.buscarPorId(id)
            .orElseThrow(() -> new SocioNoEncontradoException(id));
    return ResponseEntity.ok(SocioWebMapper.toResponseDTO(socio));
}
```

Antes devolvía `ResponseEntity.notFound().build()`: un 404 **con el cuerpo vacío**. Ahora lanza la excepción, el manejador global la intercepta y el cliente recibe un `ApiError` completo. Se nota hasta en el tamaño de la respuesta en Postman: pasa de ~130 B a ~330 B.

## 7. `@Transactional`

```java
@Override
@Transactional
public Membresia registrar(Membresia membresia) {
    socioUseCase.buscarPorId(membresia.getSocioId())
            .orElseThrow(() -> new SocioNoEncontradoException(membresia.getSocioId()));
    return membresiaRepositoryPort.guardar(membresia);
}
```

Una **transacción** es un conjunto de operaciones que deben tratarse como una sola: o se confirman todas (*commit*) o no queda ninguna (*rollback*). Las propiedades que se buscan se conocen como **ACID**: atomicidad (todo o nada), consistencia (la base pasa de un estado válido a otro válido), aislamiento (las transacciones concurrentes no se pisan) y durabilidad (lo confirmado sobrevive a una caída).

`@Transactional` abre una transacción al entrar al método y la confirma al salir bien. Si sale una excepción no comprobada, hace rollback.

### Por qué en el caso de uso y no en el Controller

Porque **la unidad atómica es la operación de negocio**, y quien la conoce es el caso de uso. "Registrar una membresía" es una cosa completa; que haya llegado por HTTP es accidental.

Ponerlo en el Controller tiene además dos problemas prácticos: la transacción quedaría abierta durante la serialización de la respuesta, y si mañana ese caso de uso se invocara desde otro lado, se quedaría sin transacción.

Honestidad sobre nuestro caso: `registrar` hace una sola escritura, así que el efecto práctico es mínimo — una sola sentencia ya es atómica por sí sola. Se anota igual porque marca la frontera correcta y porque en cuanto el método haga dos escrituras (por ejemplo, guardar la membresía y registrar un histórico) la anotación se vuelve indispensable sin tener que reestructurar nada.

## 8. El flujo completo de un error

Para la pregunta 50 del banco:

```
1. El cliente envía POST /api/membresias con socioId = 999
2. Spring MVC enruta hacia MembresiaController
3. @RequestBody arma el DTO, @Valid lo valida → pasa (999 es un Long válido)
4. El Controller traduce a dominio y llama al Port IN
5. MembresiaService consulta SocioUseCase.buscarPorId(999) → Optional vacío
6. Lanza SocioNoEncontradoException (que no sabe nada de HTTP)
7. La excepción sube por las capas sin ser capturada
8. GlobalExceptionHandler la intercepta con @ExceptionHandler
9. Construye un ApiError con status 404, message, path y timestamp
10. El cliente recibe 404 + JSON estructurado
```

Y si el fallo viniera de una constraint de PostgreSQL (email duplicado), el camino cambia a partir del paso 5: Hibernate ejecuta el INSERT, PostgreSQL lo rechaza con `duplicate key value violates unique constraint`, Spring traduce esa excepción del driver a `DataIntegrityViolationException`, y el manejador la convierte en 409. El cliente nunca ve el nombre de la constraint ni la traza.

## 9. Qué NO debes hacer

- No pongas `ResponseEntity` ni códigos HTTP dentro de excepciones de dominio.
- No repitas `try/catch` en cada Controller.
- No devuelvas 500 para errores del cliente.
- No expongas trazas de excepción al cliente: filtran rutas de clases y estructura interna.
- No pongas `@Transactional` en el Controller.
- No valides en el Response DTO.

## 10. Antes de dar por listo el capítulo

- [ ] `POST /api/membresias` con `socioId` inexistente devuelve **404** con `ApiError`.
- [ ] `POST /api/socios` con email repetido devuelve **409** con `ApiError`.
- [ ] `GET /api/socios/9999` devuelve **404** con cuerpo, no vacío.
- [ ] Un POST inválido devuelve **400** con la lista de `fieldErrors`.
- [ ] Puedes explicar la diferencia entre 400, 404, 409 y 500 con un ejemplo propio de cada uno.
- [ ] Puedes explicar por qué `@Transactional` va en el caso de uso.
- [ ] Puedes explicar los tres niveles de validación.

## 11. Commit

```bash
git add .
git commit -m "feat: add global exception handling with ApiError contract"
```

## Actividad para practicar

Dale manejo de errores profesional al módulo `Entrenador`/`Rutina`.

1. Crea `EntrenadorNoEncontradoException` y `RutinaNoEncontradaException` en el `domain/exception` de cada módulo.
2. Agrégalas al `@ExceptionHandler` de "no encontrado" del manejador global.
3. Cambia los `buscarPorId` de ambos Controllers para que lancen la excepción.
4. Haz que `RutinaService` lance `EntrenadorNoEncontradoException` cuando el entrenador no exista.
5. Pon `@Transactional` en los métodos de escritura de ambos Services.
6. Prueba en Postman los cuatro casos y **guarda captura de cada uno**: 400, 404, 409 y el caso feliz.

Un ejercicio extra que vale mucho para la defensa: tu tabla `rutina` tiene un `CHECK` sobre `nivel`. Manda un POST con `"nivel": "EXPERTO"`.

- Si tu DTO tiene el `@Pattern` correcto, responde **400** y nunca llega a la base.
- Si quitas temporalmente ese `@Pattern`, la petición llega a PostgreSQL, el `CHECK` la rechaza, y tu manejador de `DataIntegrityViolationException` responde **409**.

Haz las dos pruebas y explica por escrito por qué el mismo dato inválido produce dos códigos distintos según dónde se detecte, y cuál de las dos barreras preferirías que actuara primero y por qué.
