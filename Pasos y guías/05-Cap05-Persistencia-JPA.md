# Capítulo 05 — Persistencia real con JPA, Hibernate y PostgreSQL

Hasta aquí los datos vivían en una lista en memoria y se perdían al reiniciar. Ahora van a una tabla real. La lista desaparece.

## 0. La cadena tecnológica (pregunta fija del examen)

Cuatro nombres que suelen confundirse y que el profesor pide distinguir:

```
Tu código  →  Spring Data JPA  →  JPA  →  Hibernate  →  JDBC  →  PostgreSQL
```

| Pieza | Qué es exactamente |
|---|---|
| **JDBC** | La API de bajo nivel de Java para hablar con una base. Abres conexión, mandas un String de SQL, recorres un `ResultSet`. Funciona, pero es verboso. |
| **ORM** | *Object-Relational Mapping*: la idea general de traducir entre objetos Java y filas de tablas. Es un concepto, no una librería. |
| **JPA** | La **especificación** oficial de Java para ORM. Define anotaciones (`@Entity`, `@Id`, `@Column`) y contratos. **Es solo el contrato: no ejecuta nada.** |
| **Hibernate** | La **implementación** de JPA que usamos. Es quien genera el SQL de verdad y lo manda por JDBC. |
| **Spring Data JPA** | Una capa encima: te deja declarar una interfaz (`JpaRepository`) y te regala la implementación con los métodos básicos y los derivados del nombre. |

La frase corta para defender: *JPA es la especificación, Hibernate la implementa, Spring Data JPA simplifica los repositorios, y JDBC es el que finalmente conecta con PostgreSQL.* No son sinónimos.

Y una advertencia que el profesor repite: **usar un ORM no te libera de entender SQL**. Hibernate genera consultas; si no sabes leerlas, no puedes diagnosticar por qué una operación va lenta o por qué trae datos de más.

## 1. La regla central del capítulo: dominio y entidad JPA son clases distintas

Esta es la decisión más importante del capítulo y la que más preguntas genera.

La tentación es poner `@Entity` sobre tu clase `Socio` y ahorrarte una clase. **No lo hagas.** La guía oficial de Capítulo 05 lo prohíbe explícitamente.

Por qué:

- El dominio representa el **negocio**; la entidad JPA representa una **tabla**. Son dos cosas que cambian por motivos distintos y a ritmos distintos.
- Si son la misma clase, cualquier cambio de la tabla te obliga a tocar el negocio, y viceversa.
- El dominio deja de poder probarse sin levantar Hibernate.
- JPA impone condiciones (constructor sin argumentos, nada de `final`, proxies de carga diferida) que contaminan un modelo que debería ser Java puro.

El precio de separarlas es que necesitas un **mapper** que traduzca en ambos sentidos. Es código repetitivo pero trivial, y es exactamente lo que se paga por mantener el núcleo limpio.

## 2. La estructura que se agrega

```
com.fitclub.socio
├── domain
│   └── model
│       └── Socio.java                         (sin cambios, sigue sin anotaciones)
├── application
│   └── SocioService.java                      (cambia: ya no usa lista en memoria)
└── infrastructure
    └── adapter
        ├── in/web/...                          (sin cambios)
        └── out
            └── persistence
                ├── entity
                │   └── SocioJpaEntity.java     ← NUEVO
                ├── mapper
                │   └── SocioPersistenceMapper.java  ← NUEVO
                └── repository
                    └── SpringDataSocioRepository.java  ← NUEVO
```

`in` es por dónde entra la información (HTTP). `out` es por dónde sale hacia el exterior (la base). Las dos son infraestructura.

## 3. La entidad JPA

`SocioJpaEntity.java`:

```java
package com.fitclub.socio.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "socio", schema = "fitclub")
public class SocioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "telefono", nullable = false)
    private String telefono;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    protected SocioJpaEntity() {
    }

    public SocioJpaEntity(String nombre, String email, String telefono, LocalDate fechaRegistro) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
}
```

Qué representa cada anotación:

| Anotación | Qué información de la tabla física representa |
|---|---|
| `@Entity` | "Esta clase se corresponde con una tabla". Sin esto, Hibernate la ignora. |
| `@Table(name, schema)` | El nombre exacto de la tabla y el schema. Aquí es donde se amarra `fitclub.socio`. |
| `@Id` | Cuál atributo es la clave primaria. |
| `@GeneratedValue(strategy = IDENTITY)` | Quién genera el id: la base, mediante su columna `IDENTITY`. Hibernate no inventa el número; lo lee después del INSERT. |
| `@Column(name = "...")` | El nombre físico de la columna. Necesario cuando difiere del atributo Java: `fechaRegistro` ↔ `fecha_registro`. |
| `nullable = false` / `unique = true` | Describen la restricción que **ya existe** en la base. |

Dos detalles del código que suelen preguntarse:

**El constructor `protected` sin argumentos** existe porque JPA lo exige: Hibernate necesita instanciar la entidad por reflexión antes de rellenar sus campos. Es `protected` y no `public` para que tu propio código no lo use por accidente.

**No hay setters.** El único constructor útil recibe los datos; el `id` lo pone Hibernate. Así la entidad no puede quedar a medio construir desde tu código.

### `unique = true` en `@Column` vs `UNIQUE` en PostgreSQL

Pregunta del banco. La respuesta: con `ddl-auto=validate`, la anotación **no crea ni garantiza nada** — solo documenta y sirve para que Hibernate valide el esquema. La restricción real, la que impide físicamente el duplicado, es la de PostgreSQL. **La base de datos es la última barrera de integridad**, porque protege incluso frente a un bug de la aplicación o a alguien insertando por SQL directo.

## 4. El mapper de persistencia

`SocioPersistenceMapper.java`:

```java
package com.fitclub.socio.infrastructure.adapter.out.persistence.mapper;

import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.infrastructure.adapter.out.persistence.entity.SocioJpaEntity;

public final class SocioPersistenceMapper {

    private SocioPersistenceMapper() {}

    public static SocioJpaEntity toEntity(Socio socio) {
        return new SocioJpaEntity(
                socio.getNombre(), socio.getEmail(), socio.getTelefono(), socio.getFechaRegistro());
    }

    public static Socio toDomain(SocioJpaEntity entity) {
        return new Socio(
                entity.getId(), entity.getNombre(), entity.getEmail(),
                entity.getTelefono(), entity.getFechaRegistro());
    }
}
```

Es `final` con constructor privado porque es una clase de utilidades: solo métodos estáticos, nunca se instancia.

Fíjate que `toEntity` **no pasa el id**: al crear, el id todavía no existe; lo genera la base. `toDomain` sí lo lee, porque después del `save` ya está.

## 5. El repositorio Spring Data

`SpringDataSocioRepository.java`:

```java
package com.fitclub.socio.infrastructure.adapter.out.persistence.repository;

import com.fitclub.socio.infrastructure.adapter.out.persistence.entity.SocioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataSocioRepository extends JpaRepository<SocioJpaEntity, Long> {
    Optional<SocioJpaEntity> findByEmail(String email);
}
```

Es una **interfaz sin implementación**, y aun así funciona. Spring Data genera la clase concreta en tiempo de ejecución y la registra como bean. En el log de arranque lo ves: `Found 2 JPA repository interfaces`.

Al extender `JpaRepository<SocioJpaEntity, Long>` (entidad, tipo de la PK) heredas `save`, `findById`, `findAll`, `deleteById`, `count`, `existsById` y más.

`findByEmail` es un **método derivado**: Spring Data lee el nombre, reconoce el patrón `findBy` + nombre de propiedad, y escribe el `WHERE email = ?` por ti. Si escribes mal el nombre de la propiedad, falla **al arrancar**, no en tiempo de ejecución — es una ventaja, no un problema.

Advertencia conceptual: un `JpaRepository` **no es el modelo de dominio**. Es un detalle técnico de infraestructura. En el Capítulo 06 vas a esconderlo detrás de un puerto justamente por eso.

## 6. El Service deja de usar la lista

```java
package com.fitclub.socio.application;

import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.infrastructure.adapter.in.web.dto.SocioRequestDTO;
import com.fitclub.socio.infrastructure.adapter.in.web.dto.SocioResponseDTO;
import com.fitclub.socio.infrastructure.adapter.out.persistence.entity.SocioJpaEntity;
import com.fitclub.socio.infrastructure.adapter.out.persistence.mapper.SocioPersistenceMapper;
import com.fitclub.socio.infrastructure.adapter.out.persistence.repository.SpringDataSocioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SocioService {

    private final SpringDataSocioRepository socioRepository;

    public SocioService(SpringDataSocioRepository socioRepository) {
        this.socioRepository = socioRepository;
    }

    public SocioResponseDTO crear(SocioRequestDTO request) {
        Socio socio = new Socio(null, request.getNombre(), request.getEmail(),
                request.getTelefono(), LocalDate.now());
        SocioJpaEntity guardado = socioRepository.save(SocioPersistenceMapper.toEntity(socio));
        return toResponseDTO(SocioPersistenceMapper.toDomain(guardado));
    }

    public List<SocioResponseDTO> listar() {
        return socioRepository.findAll().stream()
                .map(SocioPersistenceMapper::toDomain)
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<SocioResponseDTO> buscarPorId(Long id) {
        return socioRepository.findById(id)
                .map(SocioPersistenceMapper::toDomain)
                .map(this::toResponseDTO);
    }

    private SocioResponseDTO toResponseDTO(Socio socio) {
        return new SocioResponseDTO(socio.getId(), socio.getNombre(), socio.getEmail(),
                socio.getTelefono(), socio.getFechaRegistro());
    }
}
```

**El Controller no cambia ni una línea.** Eso no es casualidad: como el Controller solo conoce al Service y al DTO, cambiar dónde se guardan los datos no lo afecta. Es la primera prueba concreta de que separar capas sirve para algo.

En este punto el Service todavía depende directamente de `SpringDataSocioRepository`, lo cual es aceptable para el Capítulo 05 pero no es lo definitivo. El Capítulo 06 lo arregla.

## 7. `ddl-auto=validate` — la configuración que define el capítulo

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.default_schema=fitclub
```

`ddl-auto` le dice a Hibernate qué hacer con el esquema al arrancar:

| Valor | Qué hace | ¿Sirve aquí? |
|---|---|---|
| `validate` | Compara entidades contra las tablas y **falla si no coinciden**. No modifica nada. | **Sí. Es el que usamos.** |
| `update` | Intenta alterar las tablas para que calcen. | No: toma decisiones silenciosas sobre tu esquema y nunca borra nada, así que acumula basura. |
| `create` | Borra y recrea las tablas en cada arranque. | No: pierdes los datos cada vez. |
| `create-drop` | Igual, y además borra al apagar. | No. |
| `none` | No hace nada. | Válido, pero pierdes la verificación. |

Por qué `validate` es **coherente con una base diseñada previamente**: tu modelo relacional se diseñó primero, con criterio, en las clases de base de datos. La base es la fuente de verdad, no un subproducto del código Java. `validate` respeta esa jerarquía y además te avisa apenas el código y el esquema se desalinean, en vez de dejarte descubrirlo en producción.

## 8. Cómo comprobar que un POST realmente persistió

Esta es la pregunta 20 del banco de la ruleta, y pide relacionar **cuatro evidencias distintas**:

1. **La respuesta HTTP**: `201 Created` con un `id` que antes no existía. Es necesaria pero no suficiente — un servidor podría inventar el id sin guardar nada.
2. **El log SQL de Hibernate** en la consola de IntelliJ:
   ```
   insert into fitclub.socio (email, fecha_registro, nombre, telefono) values (?, ?, ?, ?)
   ```
   Demuestra que se generó y ejecutó la sentencia.
3. **DataGrip**: `SELECT * FROM fitclub.socio ORDER BY id DESC;` muestra la fila.
4. **La fila física**, con sus valores correctos en cada columna.

Si las cuatro coinciden, persistió de verdad. Si la 1 está pero la 3 no, algo hizo rollback.

## 9. Qué NO debes hacer

- No pongas `@Entity` sobre tu clase de dominio.
- No uses `ddl-auto=update` ni `create`.
- No dejes que el Controller conozca `SocioJpaEntity`.
- No devuelvas entidades JPA por la API (expones la tabla y arrastras problemas de carga diferida).
- No inventes nombres de columna: tienen que ser los que existen en PostgreSQL.

## 10. Antes de dar por listo el capítulo

- [ ] La aplicación arranca sin `SchemaManagementException`.
- [ ] En el log aparece el `insert into fitclub.socio` real tras un POST.
- [ ] La fila se ve en DataGrip.
- [ ] Los datos sobreviven a un reinicio de la aplicación.
- [ ] Puedes explicar la cadena JDBC → JPA → Hibernate → Spring Data JPA → PostgreSQL sin confundir las piezas.
- [ ] Puedes explicar por qué `Socio` y `SocioJpaEntity` son clases distintas.

## 11. Commit

```bash
git add .
git commit -m "feat: add JPA persistence for socio with schema validation"
```

## Actividad para practicar

Dale persistencia real a `Entrenador` (las tablas ya las creaste en la actividad de la guía 02).

1. Antes de escribir Java, corre en DataGrip:
   ```sql
   SELECT column_name, data_type, is_nullable
   FROM information_schema.columns
   WHERE table_schema = 'fitclub' AND table_name = 'entrenador'
   ORDER BY ordinal_position;
   ```
   Escribe la entidad **a partir de ese resultado**, no de memoria.
2. Crea `EntrenadorJpaEntity` con `@Entity`, `@Table(name, schema)`, `@Id`, `@GeneratedValue` y `@Column` en cada campo. Ojo con `fecha_contratacion`.
3. Crea `EntrenadorPersistenceMapper` con `toEntity` y `toDomain`.
4. Crea `SpringDataEntrenadorRepository`. Agrégale un método derivado `findByEspecialidad` que devuelva una `List`. Piensa: ¿por qué este devuelve `List` y `findByEmail` devuelve `Optional`?
5. Cambia `EntrenadorService` para que use el repositorio en vez de la lista en memoria.
6. Arranca. Si falla con `missing table` o `wrong column type`, **no cambies la base**: corrige la entidad. Ese es el punto de `validate`.
7. Haz un POST desde Postman y reúne las cuatro evidencias de la sección 8.
8. Reinicia la aplicación y haz `GET /api/entrenadores`. Los datos deben seguir ahí. Explica por qué ahora sí y antes no.

Pregunta final para responder por escrito: si mañana renombras la columna `especialidad` a `area` en PostgreSQL y no tocas el Java, ¿qué pasa al arrancar y por qué es preferible a que la aplicación arranque igual?
