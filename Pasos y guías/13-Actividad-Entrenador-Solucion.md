# Soluciones de la actividad: Entrenador 1:N Rutina

Ábrelo **después** de intentarlo. La solución sirve para comparar, no para copiar: en el examen no vas a tener este archivo.

El módulo completo queda en `com.fitclub.entrenador` y `com.fitclub.rutina`, con la misma forma que `socio` y `membresia`.

---

## Guía 02 — DDL

```sql
CREATE TABLE fitclub.entrenador (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL,
    especialidad        VARCHAR(60)  NOT NULL,
    email               VARCHAR(150) NOT NULL UNIQUE,
    fecha_contratacion  DATE         NOT NULL
);

CREATE TABLE fitclub.rutina (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL,
    nivel             VARCHAR(20)  NOT NULL,
    duracion_minutos  INTEGER      NOT NULL,
    entrenador_id     BIGINT       NOT NULL REFERENCES fitclub.entrenador(id),
    CONSTRAINT ck_rutina_nivel CHECK (nivel IN ('BASICO', 'INTERMEDIO', 'AVANZADO')),
    CONSTRAINT ck_rutina_duracion CHECK (duracion_minutos > 0)
);
```

**Respuestas a las preguntas de la actividad:**

1. `duracion_minutos` es `INTEGER`. No es dinero (no necesita decimales exactos como `NUMERIC`) ni texto. Podría ser `SMALLINT`, pero `INTEGER` es el entero por defecto y no hay razón para apretar.
2. El `CHECK` sobre `nivel` obliga a que el valor esté en la lista. Se declara con nombre (`ck_rutina_nivel`) para que el error diga cuál falló.
3. Todas las columnas son `NOT NULL`: ninguna tiene sentido ausente. Una rutina sin nivel o sin duración no es una rutina; un entrenador sin email no puede ser contactado ni identificado de forma única.
4. `entrenador` se crea primero porque `rutina` la referencia. PostgreSQL no puede crear una FK hacia una tabla que no existe.

**Verificación:**
```sql
SELECT table_name, column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'fitclub' AND table_name IN ('entrenador', 'rutina')
ORDER BY table_name, ordinal_position;
```

**Los cuatro errores provocados:**
```sql
INSERT INTO fitclub.entrenador (nombre, especialidad, email, fecha_contratacion)
VALUES (NULL, 'Yoga', 'a@a.com', CURRENT_DATE);                    -- not-null violation

INSERT INTO fitclub.entrenador (nombre, especialidad, email, fecha_contratacion)
VALUES ('Ana', 'Yoga', 'repetido@a.com', CURRENT_DATE);            -- córrela dos veces → unique

INSERT INTO fitclub.rutina (nombre, nivel, duracion_minutos, entrenador_id)
VALUES ('Full body', 'BASICO', 45, 999999);                        -- foreign key violation

INSERT INTO fitclub.rutina (nombre, nivel, duracion_minutos, entrenador_id)
VALUES ('Full body', 'EXPERTO', 45, 1);                            -- check constraint ck_rutina_nivel
```

---

## Guía 03 — Dominio, DTOs, Service en memoria y Controller

### `com.fitclub.entrenador.domain.model.Entrenador`

```java
package com.fitclub.entrenador.domain.model;

import java.time.LocalDate;

public class Entrenador {

    private Long id;
    private String nombre;
    private String especialidad;
    private String email;
    private LocalDate fechaContratacion;

    public Entrenador() {}

    public Entrenador(Long id, String nombre, String especialidad, String email, LocalDate fechaContratacion) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.email = email;
        this.fechaContratacion = fechaContratacion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }
}
```

### `EntrenadorRequestDTO`

```java
package com.fitclub.entrenador.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EntrenadorRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotBlank(message = "La especialidad es obligatoria")
    @Size(max = 60, message = "La especialidad no puede superar los 60 caracteres")
    private String especialidad;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    public EntrenadorRequestDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

**La anotación que había que buscar es `@Size(max = ...)`.**

**El campo que va fuera es `fechaContratacion`**, por la misma razón que `fechaRegistro` en Socio: es un hecho que registra el sistema, no un dato que el cliente deba poder afirmar. Si lo aceptaras del cliente, alguien podría dar de alta un entrenador con fecha del año pasado.

`id` tampoco va: lo genera la base.

### `EntrenadorResponseDTO`

Cinco campos (`id`, `nombre`, `especialidad`, `email`, `fechaContratacion`), constructor completo, getters y setters, **sin una sola anotación de validación**.

### `EntrenadorService` (versión Capítulo 04, en memoria)

```java
@Service
public class EntrenadorService {

    private final List<Entrenador> entrenadores = new ArrayList<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    public EntrenadorResponseDTO crear(EntrenadorRequestDTO request) {
        Entrenador entrenador = new Entrenador(secuencia.incrementAndGet(),
                request.getNombre(), request.getEspecialidad(), request.getEmail(), LocalDate.now());
        entrenadores.add(entrenador);
        return toResponseDTO(entrenador);
    }

    public List<EntrenadorResponseDTO> listar() {
        return entrenadores.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    public Optional<EntrenadorResponseDTO> buscarPorId(Long id) {
        return entrenadores.stream().filter(e -> e.getId().equals(id)).findFirst().map(this::toResponseDTO);
    }

    private EntrenadorResponseDTO toResponseDTO(Entrenador e) {
        return new EntrenadorResponseDTO(e.getId(), e.getNombre(), e.getEspecialidad(),
                e.getEmail(), e.getFechaContratacion());
    }
}
```

### `EntrenadorController`

Idéntico en forma a `SocioController`, con `@RequestMapping("/api/entrenadores")`.

**Respuestas a las preguntas:**

- Si mandan `especialidad` con 200 caracteres, la API devuelve **400**, y lo genera **Spring**: `@Valid` dispara `@Size`, falla el binding y Spring rechaza antes de entrar al método. Tu código nunca se ejecuta.
- El Controller no tiene `new` porque recibe el servicio por constructor y lo construye Spring. Eso es inyección de dependencias sobre inversión de control.

---

## Guía 05 — Persistencia

### `EntrenadorJpaEntity`

```java
@Entity
@Table(name = "entrenador", schema = "fitclub")
public class EntrenadorJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "especialidad", nullable = false)
    private String especialidad;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "fecha_contratacion", nullable = false)
    private LocalDate fechaContratacion;

    protected EntrenadorJpaEntity() {}

    public EntrenadorJpaEntity(String nombre, String especialidad, String email, LocalDate fechaContratacion) {
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.email = email;
        this.fechaContratacion = fechaContratacion;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEspecialidad() { return especialidad; }
    public String getEmail() { return email; }
    public LocalDate getFechaContratacion() { return fechaContratacion; }
}
```

El `@Column(name = "fecha_contratacion")` es obligatorio: el atributo Java es `fechaContratacion` y la columna usa guiones bajos.

### `SpringDataEntrenadorRepository`

```java
public interface SpringDataEntrenadorRepository extends JpaRepository<EntrenadorJpaEntity, Long> {
    Optional<EntrenadorJpaEntity> findByEmail(String email);
    List<EntrenadorJpaEntity> findByEspecialidad(String especialidad);
}
```

**Por qué uno devuelve `Optional` y el otro `List`:** por la **cardinalidad del resultado**. `email` es `UNIQUE`, así que a lo sumo hay una fila — `Optional` expresa "cero o uno". `especialidad` no es única: puede haber muchos entrenadores de Yoga, así que el resultado natural es una lista, que además puede estar vacía sin que eso sea excepcional.

### Pregunta final de esa guía

Si renombras `especialidad` a `area` en PostgreSQL y no tocas el Java, la aplicación **no arranca**: `ddl-auto=validate` detecta que la entidad espera una columna que ya no existe y lanza `SchemaManagementException`.

Es preferible a que arranque porque el fallo aparece **inmediatamente, en tu máquina, al iniciar**, y no más tarde en producción con la primera petición de un usuario real. Un error temprano y ruidoso es mejor que uno tardío y silencioso.

---

## Guía 06 — La relación 1:N

### `Rutina` (dominio)

```java
public class Rutina {
    private Long id;
    private String nombre;
    private String nivel;
    private Integer duracionMinutos;
    private Long entrenadorId;
    // constructor vacío, constructor completo, getters y setters
}
```

`duracionMinutos` es `Integer` y no `int` para poder distinguir "no vino el dato" (`null`) de "vino cero". Eso permite que `@NotNull` funcione.

### `RutinaRequestDTO`

```java
@NotBlank(message = "El nombre es obligatorio")
@Size(max = 100)
private String nombre;

@NotBlank(message = "El nivel es obligatorio")
@Pattern(regexp = "BASICO|INTERMEDIO|AVANZADO",
         message = "El nivel debe ser BASICO, INTERMEDIO o AVANZADO")
private String nivel;

@NotNull(message = "La duración es obligatoria")
@Positive(message = "La duración debe ser mayor que cero")
private Integer duracionMinutos;

@NotNull(message = "El id del entrenador es obligatorio")
private Long entrenadorId;
```

`@NotBlank` para los `String`, `@NotNull` para los demás tipos.

### `RutinaJpaEntity`

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "entrenador_id", nullable = false)
private EntrenadorJpaEntity entrenador;
```

### `RutinaPersistenceMapper`

```java
public static RutinaJpaEntity toEntity(Rutina rutina, EntrenadorJpaEntity entrenador) {
    return new RutinaJpaEntity(rutina.getNombre(), rutina.getNivel(),
            rutina.getDuracionMinutos(), entrenador);
}

public static Rutina toDomain(RutinaJpaEntity entity) {
    return new Rutina(entity.getId(), entity.getNombre(), entity.getNivel(),
            entity.getDuracionMinutos(), entity.getEntrenador().getId());
}
```

### Verificación en DataGrip

```sql
SELECT r.id, r.nombre, r.nivel, r.duracion_minutos, r.entrenador_id, e.nombre AS nombre_entrenador
FROM fitclub.rutina r
JOIN fitclub.entrenador e ON e.id = r.entrenador_id;
```

**Respuestas a las preguntas:**

- `entrenador_id` está en `rutina`, el lado "muchos". No puede estar en `entrenador` porque un entrenador tiene muchas rutinas y una columna solo guarda un valor.
- Con `EAGER` y 50 rutinas, Hibernate haría **51 consultas**: una para la lista y una por cada rutina para traer su entrenador. Es el problema N+1. (Con un `JOIN FETCH` explícito sería una sola, pero eso se decide por consulta, no globalmente en el mapeo.)
- La diferencia entre el `Long` del dominio y el objeto de la entidad es intencional: el dominio solo necesita **saber a cuál pertenece**, y guardar el objeto completo acoplaría el módulo `rutina` al módulo `entrenador`. La entidad JPA sí necesita el objeto porque así es como JPA expresa una asociación mapeada a una FK.

---

## Guía 07 — Hexagonal

### Puertos de Entrenador

```java
// domain/port/out/EntrenadorRepositoryPort.java
public interface EntrenadorRepositoryPort {
    Entrenador guardar(Entrenador entrenador);
    List<Entrenador> listar();
    Optional<Entrenador> buscarPorId(Long id);
}

// domain/port/in/EntrenadorUseCase.java
public interface EntrenadorUseCase {
    Entrenador registrar(Entrenador entrenador);
    List<Entrenador> listar();
    Optional<Entrenador> buscarPorId(Long id);
}
```

### `RutinaPersistenceAdapter`

```java
@Component
public class RutinaPersistenceAdapter implements RutinaRepositoryPort {

    private final SpringDataRutinaRepository springDataRutinaRepository;
    private final SpringDataEntrenadorRepository springDataEntrenadorRepository;

    public RutinaPersistenceAdapter(SpringDataRutinaRepository springDataRutinaRepository,
                                    SpringDataEntrenadorRepository springDataEntrenadorRepository) {
        this.springDataRutinaRepository = springDataRutinaRepository;
        this.springDataEntrenadorRepository = springDataEntrenadorRepository;
    }

    @Override
    public Rutina guardar(Rutina rutina) {
        EntrenadorJpaEntity ref = springDataEntrenadorRepository.getReferenceById(rutina.getEntrenadorId());
        RutinaJpaEntity entity = RutinaPersistenceMapper.toEntity(rutina, ref);
        return RutinaPersistenceMapper.toDomain(springDataRutinaRepository.save(entity));
    }

    @Override
    public List<Rutina> listar() {
        return springDataRutinaRepository.findAll().stream()
                .map(RutinaPersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Rutina> buscarPorId(Long id) {
        return springDataRutinaRepository.findById(id).map(RutinaPersistenceMapper::toDomain);
    }
}
```

### `RutinaService`

```java
@Service
public class RutinaService implements RutinaUseCase {

    private final RutinaRepositoryPort rutinaRepositoryPort;
    private final EntrenadorUseCase entrenadorUseCase;

    public RutinaService(RutinaRepositoryPort rutinaRepositoryPort, EntrenadorUseCase entrenadorUseCase) {
        this.rutinaRepositoryPort = rutinaRepositoryPort;
        this.entrenadorUseCase = entrenadorUseCase;
    }

    @Override
    @Transactional
    public Rutina registrar(Rutina rutina) {
        entrenadorUseCase.buscarPorId(rutina.getEntrenadorId())
                .orElseThrow(() -> new EntrenadorNoEncontradoException(rutina.getEntrenadorId()));
        return rutinaRepositoryPort.guardar(rutina);
    }

    @Override
    public List<Rutina> listar() { return rutinaRepositoryPort.listar(); }

    @Override
    public Optional<Rutina> buscarPorId(Long id) { return rutinaRepositoryPort.buscarPorId(id); }
}
```

**Los imports correctos de `RutinaService`** son: el dominio `Rutina`, sus dos puertos, `EntrenadorUseCase`, la excepción, `@Service`, `@Transactional`, `List` y `Optional`. Si aparece `RutinaJpaEntity`, `SpringDataRutinaRepository` o cualquier DTO, el refactor está incompleto.

**Respuestas a las preguntas:**

- `RutinaRepositoryPort` evita que la capa de aplicación dependa de Spring Data JPA y, a través de ella, de Hibernate y del modelo de tablas.
- Cambiando PostgreSQL por archivos en disco reescribirías: la entidad JPA, el mapper de persistencia, el repositorio Spring Data y el adaptador. **No tocarías**: el dominio, los puertos, el Service, el Web Mapper, los DTOs ni el Controller. Esa lista es la mejor demostración de para qué sirve la arquitectura.
- `RutinaService` no puede inyectar `SpringDataEntrenadorRepository` porque haría que la aplicación dependa de la persistencia interna de otro módulo. Compilaría, pero rompe la regla de dependencia y acopla los módulos por su implementación.

---

## Guía 08 — Errores

```java
package com.fitclub.entrenador.domain.exception;

public class EntrenadorNoEncontradoException extends RuntimeException {
    public EntrenadorNoEncontradoException(Long id) {
        super("No existe un entrenador con id " + id);
    }
}
```

Y en el manejador global, se agregan a la lista existente:

```java
@ExceptionHandler({SocioNoEncontradoException.class, MembresiaNoEncontradaException.class,
                   EntrenadorNoEncontradoException.class, RutinaNoEncontradaException.class})
public ResponseEntity<ApiError> handleNoEncontrado(RuntimeException ex, HttpServletRequest request) { ... }
```

### El ejercicio del `nivel` inválido

Mandando `"nivel": "EXPERTO"`:

- **Con `@Pattern` en el DTO:** `@Valid` lo rechaza, responde **400** con `fieldErrors`, y la petición nunca toca la base.
- **Sin `@Pattern`:** el dato llega a PostgreSQL, el `CHECK ck_rutina_nivel` lo rechaza, Spring traduce a `DataIntegrityViolationException` y el manejador responde **409**.

**Por qué el mismo dato produce dos códigos:** porque el código HTTP describe **qué tipo de problema** es, y eso depende de quién lo detecta. Detectado en la frontera de entrada, es un dato mal formado según el contrato de la API → 400. Detectado en la base, es un conflicto con las reglas de integridad del almacenamiento → 409.

**Cuál debería actuar primero:** la validación del DTO. Es más barata (no abre transacción ni toca la base), da un mensaje mucho mejor (dice qué campo y qué valores se aceptan) y devuelve el código semánticamente correcto. Pero el `CHECK` debe quedarse igual: es la garantía de que ningún otro camino —otra aplicación, un script, un bug futuro— pueda meter un nivel inválido. Defensa en profundidad.

---

## Guía 10 — Los siete fallos provocados

| # | Qué se rompe | Mensaje esperado | Capa | Cuándo aparece |
|---|---|---|---|---|
| 1 | Sin `@Column(name="fecha_contratacion")` | `missing column [fechaContratacion]` | Persistencia/esquema | Arranque |
| 2 | `schema = "public"` | `missing table [public.entrenador]` | Persistencia/esquema | Arranque |
| 3 | Sin `@Valid` | **201** con email vacío: el dato inválido entra y se guarda | Web | Petición |
| 4 | Sin `@Component` | `required a bean of type ...EntrenadorRepositoryPort` | Configuración | Arranque |
| 5 | Sin `Content-Type` | **415** Unsupported Media Type | Web | Petición |
| 6 | Email repetido | `duplicate key ... "entrenador_email_key"` en el log; **409** al cliente | Persistencia | Petición |
| 7 | `/api/entrenador/9999` | **404** de Spring por ruta inexistente | Web | Petición |

El caso 3 es el más instructivo: **no da error**. Devuelve 201 y guarda un entrenador con email vacío. Un fallo silencioso que corrompe datos es peor que uno ruidoso.

El caso 7 es el más engañoso: da exactamente el código que esperabas, pero por una razón distinta. Es la página de error por defecto de Spring respondiendo a una ruta que no existe, no tu `buscarPorId` diciendo que el recurso no está. Una prueba que pasa por el motivo equivocado no prueba nada.

**Párrafo de cierre.** De los siete, cuatro (1, 2, 4 y parcialmente el 6) se detectan al arrancar o por configuración, y tres solo al recibir una petición. Es preferible que un error aparezca en el arranque porque falla en tu máquina, antes de que nadie lo use, con un mensaje que apunta a la causa; un error que solo aparece con cierta petición puede quedar dormido meses y despertarse con un usuario real. Por eso `ddl-auto=validate`, la inyección por constructor y los métodos derivados de Spring Data —que también fallan al arrancar si el nombre está mal— son decisiones que **adelantan** el momento del fallo a propósito.
