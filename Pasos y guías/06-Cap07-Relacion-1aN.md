# Capítulo 07 — La relación 1:N entre Socio y Membresia

Un socio puede tener muchas membresías a lo largo del tiempo; cada membresía pertenece a un solo socio. Eso es una relación **uno a muchos**: `Socio` es la entidad **padre**, `Membresia` es la **dependiente**.

## 0. La misma relación existe en tres niveles distintos

Esta es la idea central del capítulo y una pregunta directa del examen. La relación se representa de tres formas diferentes según la capa, y **no son equivalentes**:

| Nivel | Cómo se representa | Código |
|---|---|---|
| **Dominio** | Un `Long socioId`. Solo el identificador. | `private Long socioId;` |
| **Persistencia JPA** | Una referencia al objeto entidad del padre. | `@ManyToOne ... private SocioJpaEntity socio;` |
| **PostgreSQL** | Una columna con restricción de clave foránea. | `socio_id BIGINT NOT NULL REFERENCES fitclub.socio(id)` |

Por qué el dominio guarda solo el `Long` y no el objeto `Socio` completo: porque el dominio de `Membresia` **no necesita** conocer los datos del socio para existir ni para validar sus propias reglas. Necesita saber a cuál pertenece, nada más. Si guardara el objeto entero, el módulo `membresia` quedaría acoplado al módulo `socio`, y cargar una membresía obligaría a cargar un socio. El principio que protege esa decisión es el de **módulos con fronteras claras**: cada uno conoce del otro lo mínimo indispensable.

Y una precisión que conviene decir bien en la defensa: **`@ManyToOne` no es la clave foránea**. `@ManyToOne` es una anotación de Java que le dice a Hibernate cómo mapear una columna hacia un objeto. La clave foránea es una **restricción física de PostgreSQL** que existe aunque tu aplicación no exista. Si borras toda la aplicación Java, la FK sigue ahí impidiendo datos huérfanos.

## 1. Dónde vive la FK y por qué de ese lado

La columna `socio_id` va en la tabla **`membresia`**, la del lado "muchos". Siempre es así en una relación 1:N.

La razón es que cada fila de `membresia` apunta a **un solo** socio, así que una sola columna basta. Al revés no funcionaría: un socio puede tener muchas membresías, y no puedes guardar "muchos" valores en una columna sin romper la primera forma normal.

## 2. El dominio

`Membresia.java`:

```java
package com.fitclub.membresia.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Membresia {

    private Long id;
    private String tipo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal precio;
    private Long socioId;

    public Membresia() {}

    public Membresia(Long id, String tipo, LocalDate fechaInicio, LocalDate fechaFin,
                     BigDecimal precio, Long socioId) {
        this.id = id;
        this.tipo = tipo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.precio = precio;
        this.socioId = socioId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Long getSocioId() { return socioId; }
    public void setSocioId(Long socioId) { this.socioId = socioId; }
}
```

`precio` es `BigDecimal` y no `double`, por el mismo motivo por el que la columna es `NUMERIC` y no `FLOAT`: los tipos de punto flotante binario no representan exactamente valores decimales y acumulan error. Con dinero eso es inaceptable.

## 3. Los DTOs

El Request lleva `socioId` como campo obligatorio: sin saber a quién pertenece, la membresía no puede crearse.

```java
@NotBlank(message = "El tipo es obligatorio")
@Pattern(regexp = "MENSUAL|TRIMESTRAL|ANUAL", message = "El tipo debe ser MENSUAL, TRIMESTRAL o ANUAL")
private String tipo;

@NotNull(message = "La fecha de inicio es obligatoria")
private LocalDate fechaInicio;

@NotNull(message = "La fecha de fin es obligatoria")
private LocalDate fechaFin;

@NotNull(message = "El precio es obligatorio")
@Positive(message = "El precio debe ser mayor que cero")
private BigDecimal precio;

@NotNull(message = "El id del socio es obligatorio")
private Long socioId;
```

Detalle a notar: para `String` se usa `@NotBlank` (rechaza null, vacío y solo-espacios); para los demás tipos se usa `@NotNull`, porque `@NotBlank` solo aplica a texto.

El Response devuelve `socioId`, no el objeto socio completo. Si el cliente necesita los datos del socio, hace `GET /api/socios/{id}`. Así cada recurso expone lo suyo.

## 4. La entidad JPA: aquí sí aparece el objeto

```java
package com.fitclub.membresia.infrastructure.adapter.out.persistence.entity;

import com.fitclub.socio.infrastructure.adapter.out.persistence.entity.SocioJpaEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "membresia", schema = "fitclub")
public class MembresiaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "precio", nullable = false)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "socio_id", nullable = false)
    private SocioJpaEntity socio;

    protected MembresiaJpaEntity() {}

    public MembresiaJpaEntity(String tipo, LocalDate fechaInicio, LocalDate fechaFin,
                              BigDecimal precio, SocioJpaEntity socio) {
        this.tipo = tipo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.precio = precio;
        this.socio = socio;
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public BigDecimal getPrecio() { return precio; }
    public SocioJpaEntity getSocio() { return socio; }
}
```

### `@ManyToOne`

Se lee desde la clase donde está escrita: *muchas membresías hacia un socio*. Va **siempre en el lado dependiente**, el que tiene la columna FK.

`optional = false` significa que la asociación es obligatoria: no puede existir una membresía sin socio. Es el equivalente en JPA del `NOT NULL` de la columna.

### `@JoinColumn(name = "socio_id")`

Le dice a Hibernate **con qué columna física** se materializa esa asociación. Sin esta anotación, Hibernate inventaría un nombre por convención (`socio_id` en este caso coincidiría, pero no siempre) — es mejor ser explícito.

### `FetchType.LAZY`

Define **cuándo** se carga el socio asociado.

- `EAGER`: cada vez que traes una membresía, Hibernate trae también el socio, con una consulta extra o un JOIN.
- `LAZY`: Hibernate deja un objeto proxy vacío y solo consulta el socio si alguien llama a un método suyo distinto de `getId()`.

`LAZY` es la decisión correcta aquí porque la mayoría de las operaciones sobre membresías **no necesitan los datos del socio**. Listar cien membresías con `EAGER` dispararía cien consultas adicionales — ese es exactamente el **problema N+1**: una consulta para la lista más N consultas, una por cada elemento. Con `LAZY` se hace una sola.

El precio de `LAZY` es que si intentas leer el socio fuera de la sesión de persistencia obtienes `LazyInitializationException`. Por eso importa que el mapper lea `entity.getSocio().getId()` **dentro** del adaptador, mientras la sesión sigue abierta, y que `spring.jpa.open-in-view=false` nos obligue a resolver eso bien en vez de esconderlo.

### Por qué NO hay `@OneToMany` en `SocioJpaEntity`

Podríamos haber puesto una `List<MembresiaJpaEntity>` en el socio. **No lo hicimos**, y la guía del profesor pide explícitamente no agregarlo sin justificación.

Los motivos: una colección bidireccional hay que mantenerla sincronizada a mano en los dos lados; invita a cargar todas las membresías cada vez que tocas un socio; y la relación ya está completamente representada por el lado dependiente. Se agrega solo cuando existe un caso de uso real que diga "dame un socio **con** todas sus membresías", y ese caso hoy se resuelve mejor con un `findBySocioId` en el repositorio de membresías.

## 5. El mapper con dos parámetros

```java
public static MembresiaJpaEntity toEntity(Membresia membresia, SocioJpaEntity socio) {
    return new MembresiaJpaEntity(
            membresia.getTipo(), membresia.getFechaInicio(), membresia.getFechaFin(),
            membresia.getPrecio(), socio);
}

public static Membresia toDomain(MembresiaJpaEntity entity) {
    return new Membresia(
            entity.getId(), entity.getTipo(), entity.getFechaInicio(), entity.getFechaFin(),
            entity.getPrecio(), entity.getSocio().getId());
}
```

`toEntity` recibe **dos** argumentos porque el dominio solo tiene el `Long socioId`, pero la entidad JPA necesita el objeto `SocioJpaEntity`. Alguien tiene que hacer esa conversión, y ese alguien es quien llama al mapper.

`toDomain` hace el camino inverso: `entity.getSocio().getId()` baja del objeto al identificador.

## 6. El repositorio y el método derivado

```java
public interface SpringDataMembresiaRepository extends JpaRepository<MembresiaJpaEntity, Long> {
    List<MembresiaJpaEntity> findBySocioId(Long socioId);
}
```

`findBySocioId` funciona aunque `MembresiaJpaEntity` no tenga ningún campo llamado `socioId`: Spring Data **atraviesa la asociación**. Lee `socio` (la propiedad `@ManyToOne`) y luego `id` dentro de ella, y genera `WHERE socio.id = ?`.

## 7. Validar que el socio existe antes de guardar

El Service, antes de persistir, comprueba que el socio referenciado exista:

```java
SocioJpaEntity socio = socioRepository.findById(request.getSocioId())
        .orElseThrow(() -> new NoSuchElementException(
                "No existe un socio con id " + request.getSocioId()));
```

### ¿Por qué validar en la aplicación si PostgreSQL ya tiene la FK?

Pregunta del examen, y la respuesta tiene tres partes:

1. **Calidad del error.** Si dejas que falle la FK, el cliente recibe una excepción de driver de base de datos, ilegible y con detalles internos. Validando antes, puedes devolver un 404 con un mensaje claro.
2. **Momento.** La aplicación puede detectarlo antes de empezar a escribir, sin ensuciar la transacción.
3. **Responsabilidad.** "Una membresía debe pertenecer a un socio existente" es una **regla de negocio**, y las reglas de negocio viven en el caso de uso. Que además la base la garantice es **defensa en profundidad**, no duplicación innecesaria.

La frase corta: *la aplicación valida primero, la base de datos es la última barrera.* Las dos son necesarias y protegen en niveles distintos.

## 8. Verificar la relación en la base

```sql
SELECT m.id, m.tipo, m.fecha_inicio, m.fecha_fin, m.precio, m.socio_id, s.nombre AS nombre_socio
FROM fitclub.membresia m
JOIN fitclub.socio s ON s.id = m.socio_id;
```

Este JOIN es la prueba visual de que la FK apunta a donde debe. En la defensa, poder escribir este SELECT y explicar la condición `ON` vale más que recitar la definición de clave foránea.

## 9. Qué NO debes hacer

- No pongas la FK en la tabla del padre.
- No guardes el objeto `Socio` completo dentro del dominio `Membresia`.
- No agregues `@OneToMany` "por simetría".
- No uses `EAGER` por defecto sin poder justificarlo.
- No confíes solo en la FK para dar un buen mensaje de error.

## 10. Antes de dar por listo el capítulo

- [ ] Un POST con `socioId` válido crea la membresía y el `socio_id` queda correcto en la tabla.
- [ ] Un POST con `socioId` inexistente falla (por ahora feo; el Capítulo 08 lo arregla).
- [ ] Puedes explicar los tres niveles de la relación sin confundirlos.
- [ ] Puedes explicar por qué `@ManyToOne` no es la FK.
- [ ] Puedes explicar `LAZY` y el problema N+1.
- [ ] Puedes justificar por qué no hay `@OneToMany`.

## 11. Commit

```bash
git add .
git commit -m "feat: add 1:N relationship between socio and membresia"
```

## Actividad para practicar

Implementa la relación `Entrenador 1:N Rutina`.

1. Escribe el dominio `Rutina` con `entrenadorId` como `Long`. Decide el tipo de `duracionMinutos` y justifícalo (no es dinero: ¿qué tipo corresponde?).
2. Escribe `RutinaRequestDTO`. `nivel` debe aceptar solo `BASICO`, `INTERMEDIO` o `AVANZADO` — usa la misma técnica que `tipo` en Membresia. `duracionMinutos` debe ser mayor que cero.
3. Escribe `RutinaJpaEntity` con `@ManyToOne(fetch = LAZY, optional = false)` y `@JoinColumn` hacia `entrenador_id`.
4. Escribe `RutinaPersistenceMapper` con la misma firma de dos parámetros.
5. Escribe `SpringDataRutinaRepository` con `findByEntrenadorId`.
6. En `RutinaService`, valida que el entrenador exista antes de guardar.
7. Prueba en Postman: creación válida, creación con `entrenadorId` inexistente, y listado.
8. Verifica en DataGrip con un JOIN entre `rutina` y `entrenador`.

Preguntas para responder por escrito:

- ¿En qué tabla está `entrenador_id` y por qué no puede estar en la otra?
- Si pusieras `EAGER` en lugar de `LAZY` y listaras 50 rutinas, ¿cuántas consultas haría Hibernate y por qué?
- Tu `RutinaJpaEntity` tiene un objeto `EntrenadorJpaEntity`, pero tu dominio `Rutina` tiene un `Long`. Explica por qué esa diferencia es intencional y no una inconsistencia.
