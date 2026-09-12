# Capítulo 04 — Controller, DTO, validación y códigos HTTP

Aquí aparece el primer endpoint real. Al terminar esta guía vas a poder crear y consultar socios por HTTP, con validación de entrada y códigos de estado correctos. Todavía **sin base de datos**: los datos viven en memoria y se pierden al reiniciar. Eso es a propósito — el capítulo trata de entender el contrato HTTP antes de mezclarlo con persistencia.

## 0. El recorrido que vas a construir

```
Cliente HTTP (Postman)
   │  POST /api/socios  + JSON
   ▼
SocioController          ← @RestController, traduce HTTP
   │  @RequestBody convierte el JSON en SocioRequestDTO
   │  @Valid ejecuta las reglas del DTO
   ▼
SocioService             ← @Service, decide qué hacer
   │
   ▼
Socio                    ← dominio, representa el negocio
   │
   ▼
SocioResponseDTO         ← lo que la API decide exponer
   │
   ▼
ResponseEntity → 201 CREATED + JSON
```

Cada bloque tiene **una** responsabilidad. Si el Controller empieza a decidir reglas, o el dominio empieza a saber de JSON, la arquitectura se deforma. Esa frase es literalmente lo que el profesor pide que puedas defender.

## 1. La estructura de paquetes

Crea esto dentro de `com.fitclub`. En IntelliJ, clic derecho sobre el paquete → `New → Package` y escribe la ruta completa con puntos (`socio.infrastructure.adapter.in.web.dto` crea todos los niveles de una vez).

```
com.fitclub
└── socio
    ├── domain
    │   └── model
    │       └── Socio.java
    ├── application
    │   └── SocioService.java
    └── infrastructure
        └── adapter
            └── in
                └── web
                    ├── SocioController.java
                    └── dto
                        ├── SocioRequestDTO.java
                        └── SocioResponseDTO.java
```

Por qué el Controller está tan profundo: **el Controller es infraestructura**. HTTP es un detalle técnico, una forma concreta de entrar al sistema. Mañana podrías entrar por una cola de mensajes o por línea de comandos y el negocio sería el mismo. Por eso vive en `infrastructure/adapter/in/web` y no al lado del dominio.

**Cuidado con un error clásico de IntelliJ**: si creas un paquete teniendo seleccionada una subcarpeta (por ejemplo `dto`), te lo anida ahí dentro y terminas con `...web.dto.adapter.out.persistence`. Si te pasa, se arregla con `Refactor → Move Class` (F6). Está documentado en `10-Diagnostico-de-Errores.md`.

## 2. El dominio

`Socio.java` en `com.fitclub.socio.domain.model`:

```java
package com.fitclub.socio.domain.model;

import java.time.LocalDate;

public class Socio {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private LocalDate fechaRegistro;

    public Socio() {}

    public Socio(Long id, String nombre, String email, String telefono, LocalDate fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
```

Fíjate en lo que **no** tiene: ni una anotación. Ni `@Entity`, ni `@RestController`, ni nada de Jakarta Validation. El dominio es Java puro. La frase del profesor para repetir: *el dominio no sabe cómo llega la petición ni dónde se guarda el dato*.

## 3. Los DTOs

Un DTO (*Data Transfer Object*) es un objeto que existe solo para transportar datos a través de una frontera. Aquí la frontera es HTTP.

`SocioRequestDTO.java` — lo que el cliente puede enviar:

```java
package com.fitclub.socio.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SocioRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "El teléfono no tiene un formato válido")
    private String telefono;

    public SocioRequestDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
```

`SocioResponseDTO.java` — lo que la API devuelve:

```java
package com.fitclub.socio.infrastructure.adapter.in.web.dto;

import java.time.LocalDate;

public class SocioResponseDTO {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private LocalDate fechaRegistro;

    public SocioResponseDTO() {}

    public SocioResponseDTO(Long id, String nombre, String email, String telefono, LocalDate fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
```

### Tres diferencias entre los dos DTOs que debes poder defender

| | Request | Response |
|---|---|---|
| `id` | **No lo tiene.** Lo genera el sistema; si el cliente lo enviara, podría pisar otro registro. | Sí. Es el dato que el cliente necesita para consultarlo después. |
| `fechaRegistro` | **No la tiene.** La pone el servidor con `LocalDate.now()`; si la enviara el cliente, podría mentir sobre cuándo se inscribió. | Sí. |
| Validaciones | Todas. Es la frontera de entrada. | **Ninguna.** Validar lo que tú mismo generas no tiene sentido: ya es válido por construcción. |

Ese último punto es un error frecuente: copiar un `@NotNull` al Response DTO. No hace daño, pero demuestra que no se entendió para qué sirve.

### Sobre `record` (importante para el examen)

El profesor, en sus presentaciones, escribe los DTOs como `record`:

```java
public record SocioRequestDTO(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^[0-9+\\- ]{7,20}$") String telefono
) {}
```

Un `record` es una clase inmutable con constructor, getters (sin el prefijo `get`), `equals`, `hashCode` y `toString` generados por el compilador. Para un DTO encaja perfecto: un DTO no debería cambiar después de creado.

El proyecto de práctica los tiene como clases normales, y **también está bien**: funciona igual y Jackson las serializa sin problema. Si en el examen te preguntan por qué usaste clase y no record, la respuesta honesta y correcta es que ambas cumplen el contrato, que el record es más conciso e inmutable, y que la clase con setters es lo que Jackson deserializa por defecto sin configuración adicional. Lo que no puedes hacer es no saber que existe el record. Más sobre esto en `11-Decisiones-y-Alternativas.md`.

## 4. El Service (en memoria, solo por este capítulo)

`SocioService.java` en `com.fitclub.socio.application`:

```java
package com.fitclub.socio.application;

import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.infrastructure.adapter.in.web.dto.SocioRequestDTO;
import com.fitclub.socio.infrastructure.adapter.in.web.dto.SocioResponseDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class SocioService {

    private final List<Socio> socios = new ArrayList<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    public SocioResponseDTO crear(SocioRequestDTO request) {
        Socio socio = new Socio(
                secuencia.incrementAndGet(),
                request.getNombre(),
                request.getEmail(),
                request.getTelefono(),
                LocalDate.now());
        socios.add(socio);
        return toResponseDTO(socio);
    }

    public List<SocioResponseDTO> listar() {
        return socios.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    public Optional<SocioResponseDTO> buscarPorId(Long id) {
        return socios.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .map(this::toResponseDTO);
    }

    private SocioResponseDTO toResponseDTO(Socio socio) {
        return new SocioResponseDTO(socio.getId(), socio.getNombre(), socio.getEmail(),
                socio.getTelefono(), socio.getFechaRegistro());
    }
}
```

La lista en memoria es **temporal y didáctica**. Sirve para aprender HTTP sin mezclar JPA todavía. En el Capítulo 05 desaparece por completo y la reemplaza un repositorio real. Si te preguntan "¿qué pasa con tus datos si reinicias la aplicación en este capítulo?", la respuesta es: se pierden todos, porque solo viven en la memoria del proceso.

`AtomicLong` se usa para generar ids porque un `long` normal podría dar el mismo número a dos peticiones simultáneas. `incrementAndGet()` es atómico.

## 5. El Controller

`SocioController.java` en `com.fitclub.socio.infrastructure.adapter.in.web`:

```java
package com.fitclub.socio.infrastructure.adapter.in.web;

import com.fitclub.socio.application.SocioService;
import com.fitclub.socio.infrastructure.adapter.in.web.dto.SocioRequestDTO;
import com.fitclub.socio.infrastructure.adapter.in.web.dto.SocioResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/socios")
public class SocioController {

    private final SocioService socioService;

    public SocioController(SocioService socioService) {
        this.socioService = socioService;
    }

    @PostMapping
    public ResponseEntity<SocioResponseDTO> crear(@Valid @RequestBody SocioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(socioService.crear(request));
    }

    @GetMapping
    public List<SocioResponseDTO> listar() {
        return socioService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SocioResponseDTO> buscarPorId(@PathVariable Long id) {
        return socioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

### Las anotaciones, una por una

| Anotación | Qué hace |
|---|---|
| `@RestController` | Marca la clase como bean de Spring **y** hace que lo que devuelvan los métodos se serialice a JSON en el body (en vez de interpretarse como nombre de una vista HTML). |
| `@RequestMapping("/api/socios")` | La ruta base del recurso. Todos los métodos cuelgan de aquí. |
| `@PostMapping` | Asocia el método al verbo POST sobre la ruta base. |
| `@GetMapping` / `@GetMapping("/{id}")` | Igual para GET, con y sin segmento adicional. |
| `@RequestBody` | Le dice a Spring que lea el **cuerpo** de la petición y lo convierta (con Jackson) en el objeto Java. |
| `@Valid` | Dispara las anotaciones de Jakarta Validation del DTO. **Sin `@Valid`, `@NotBlank` y compañía están escritas pero no se ejecutan.** |
| `@PathVariable` | Toma un trozo de la **ruta** (`/api/socios/7` → `id = 7`). Se usa para identificar un recurso. |
| `@RequestParam` | Toma un parámetro de **consulta** (`/api/socios?ciudad=LaPaz`). Se usa para filtrar u ordenar, no para identificar. |

La diferencia `@PathVariable` vs `@RequestParam` es pregunta fija del banco. La regla corta: **la ruta identifica, el query filtra**.

### La inyección por constructor

Fíjate que el Controller no hace `new SocioService()`. Recibe el servicio ya construido en su constructor, y Spring se lo entrega. Eso es **inyección de dependencias**, y el contenedor que la hace posible es la **inversión de control** (IoC): tú no controlas la creación de los objetos, el framework la controla por ti.

Se hace por constructor (y no con `@Autowired` sobre el campo) por tres razones concretas: el campo puede ser `final`, el objeto nunca existe a medio construir, y en una prueba puedes pasarle un doble sin levantar Spring.

### Los códigos HTTP

| Código | Cuándo |
|---|---|
| `201 Created` | POST que creó un recurso nuevo. Devuelve la representación creada, con su id. |
| `200 OK` | GET exitoso. |
| `400 Bad Request` | La entrada no cumple el contrato. Lo genera Spring solo, gracias a `@Valid`. |
| `404 Not Found` | El recurso pedido no existe. |

Devolver `200` para todo "porque funciona" es exactamente lo que la guía del profesor marca como error: pierde la semántica y obliga al cliente a adivinar leyendo el body.

## 6. Probarlo

Arranca la aplicación y usa Postman (guía `04-Postman.md`). Las seis pruebas obligatorias del capítulo:

| Prueba | Esperado |
|---|---|
| POST válido | 201 + JSON con `id` |
| POST con `nombre` vacío | 400 |
| POST con email mal formado | 400 |
| GET lista | 200 + arreglo |
| GET `/api/socios/1` | 200 |
| GET `/api/socios/999` | 404 |

## 7. Qué NO debes hacer

- No devuelvas la clase de dominio directamente como respuesta de la API.
- No pongas reglas de negocio complejas dentro del Controller.
- No uses `@RequestBody` sin `@Valid` si el DTO tiene validaciones: no se van a ejecutar.
- No uses `200` para todo.
- No conectes JPA todavía.
- No pongas validaciones en el Response DTO.

## 8. Antes de dar por listo el capítulo

- [ ] Los seis casos de prueba dan el código esperado.
- [ ] Puedes explicar el recorrido completo sin mirar el código.
- [ ] Puedes decir qué hace `@RequestBody` y qué hace `@Valid`, y por qué son cosas distintas.
- [ ] Puedes justificar por qué tu Request y tu Response no tienen los mismos campos.
- [ ] Puedes explicar dónde está el adaptador de entrada de tu módulo (`infrastructure/adapter/in/web`).

## 9. Commit

```bash
git add .
git commit -m "feat: add REST API and validation for socio"
```

## Actividad para practicar

Construye el módulo `entrenador` completo de Capítulo 04, **sin copiar y pegar el de socio**.

1. Crea la estructura de paquetes de `com.fitclub.entrenador` siguiendo el mismo patrón.
2. Escribe `Entrenador` en `domain/model`: `id`, `nombre`, `especialidad`, `email`, `fechaContratacion`. Sin anotaciones.
3. Escribe `EntrenadorRequestDTO`. Decide tú qué campos lleva y cuáles no (piensa: ¿quién debería fijar `fechaContratacion`, el cliente o el servidor?). Ponle validaciones a todo lo obligatorio; `especialidad` no debe superar 60 caracteres — busca qué anotación de Jakarta Validation sirve para eso, porque no la usamos en Socio.
4. Escribe `EntrenadorResponseDTO`. Recuerda: sin validaciones.
5. Escribe `EntrenadorService` con lista en memoria, `AtomicLong` y los tres métodos.
6. Escribe `EntrenadorController` en `/api/entrenadores` con POST, GET lista y GET por id.
7. Pruébalo en Postman con los seis casos y guarda las evidencias.

Preguntas para responder por escrito cuando termines:

- ¿Qué campo decidiste dejar fuera del Request y por qué?
- Si alguien manda `especialidad` con 200 caracteres, ¿qué código HTTP devuelve tu API y quién lo generó?
- ¿Por qué tu Controller no tiene la palabra `new` por ningún lado?

La solución está en `13-Actividad-Entrenador-Solucion.md`.
