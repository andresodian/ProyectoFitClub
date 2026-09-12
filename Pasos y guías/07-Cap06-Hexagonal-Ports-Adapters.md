# Capítulo 06 — Arquitectura hexagonal: puertos, adaptadores e inversión de dependencias

Hasta ahora el `SocioService` dependía directamente de `SpringDataSocioRepository`. Funciona, pero significa que el corazón de tu aplicación conoce una librería de persistencia. Este capítulo arregla eso.

Es un refactor: al terminar, **el comportamiento externo de la API es idéntico**. Si corres la colección de Postman antes y después, los códigos son los mismos. Eso no es señal de que no sirvió: es la prueba de que el refactor salió bien.

## 0. Qué problema resuelve la arquitectura hexagonal

El objetivo es **proteger la lógica de negocio de los detalles externos**. Externo es todo lo que podría cambiar por razones ajenas al negocio: el framework web, el motor de base de datos, el formato de intercambio, el proveedor de correo.

La regla que lo resume: **las dependencias apuntan hacia el núcleo**. Las capas de afuera conocen las de adentro; el núcleo no conoce nada de afuera.

Tres zonas mentales:

| Zona | Responsabilidad | Qué vive ahí |
|---|---|---|
| **Dominio** | Qué *significa* el negocio | Modelos, reglas, excepciones de negocio, puertos |
| **Aplicación** | Qué *hace* el sistema | Casos de uso / servicios de aplicación |
| **Infraestructura** | *Cómo* entra y sale la información | Controllers, DTOs, entidades JPA, repositorios, adaptadores |

## 1. La regla de dependencia que debes poder recitar

La guía del profesor la enuncia literalmente así, y conviene memorizarla porque cada línea es una posible pregunta:

- El **Controller** NO depende del repositorio de Spring Data.
- El **Service** NO conoce la entidad JPA.
- El **RepositoryPort** (Port OUT) NO extiende `JpaRepository`.
- El **modelo de dominio** NO tiene `@Entity`.
- El **PersistenceAdapter** SÍ puede conocer Spring Data JPA, porque está en infraestructura.

Ese último punto es el que suele sorprender: el adaptador es el único lugar donde el detalle técnico está permitido. Para eso existe.

## 2. Puerto IN y puerto OUT

Un **puerto** es una interfaz que define un contrato. Se llaman así porque son los puntos por donde el núcleo se conecta con el exterior, como los puertos de una computadora.

**Port IN** — lo que el sistema **ofrece**. Es el contrato de capacidades: "aquí se puede registrar un socio, listar socios y buscar uno por id". Lo consume el Controller. No debe mencionar HTTP, JSON, Controller ni Spring Data: si lo hiciera, el contrato de negocio quedaría atado a una forma concreta de invocarlo.

**Port OUT** — lo que el sistema **necesita** del exterior. "Para funcionar necesito poder guardar y recuperar socios de algún lado". Lo implementa un adaptador. No debe extender `JpaRepository`: si lo hiciera, el núcleo volvería a depender de Spring Data, que es justo lo que queremos evitar.

La diferencia entre **Controller y Port IN**: ambos participan en la entrada, pero el Controller es *una forma concreta* de entrar (HTTP), mientras el Port IN es *la capacidad abstracta*. Puedes tener un Controller REST, un consumidor de mensajes y un comando de consola, los tres usando el mismo Port IN.

La diferencia entre **Port OUT y JpaRepository**: el Port OUT lo defines tú desde el núcleo, con los métodos que tu negocio necesita y hablando en lenguaje de dominio (devuelve `Socio`, no `SocioJpaEntity`). `JpaRepository` lo define Spring, trae veinte métodos que quizá no necesitas, y habla en entidades JPA.

## 3. La estructura final del módulo

```
com.fitclub.socio
├── domain
│   ├── model
│   │   └── Socio.java
│   └── port
│       ├── in
│       │   └── SocioUseCase.java              ← NUEVO (Port IN)
│       └── out
│           └── SocioRepositoryPort.java       ← NUEVO (Port OUT)
├── application
│   └── SocioService.java                       ← implementa el Port IN, usa el Port OUT
└── infrastructure
    └── adapter
        ├── in
        │   └── web
        │       ├── SocioController.java        ← ahora depende del Port IN
        │       ├── dto/
        │       └── mapper
        │           └── SocioWebMapper.java     ← NUEVO (DTO ↔ dominio)
        └── out
            └── persistence
                ├── SocioPersistenceAdapter.java ← NUEVO (implementa el Port OUT)
                ├── entity/
                ├── mapper/
                └── repository/
```

## 4. El Port OUT

```java
package com.fitclub.socio.domain.port.out;

import com.fitclub.socio.domain.model.Socio;

import java.util.List;
import java.util.Optional;

public interface SocioRepositoryPort {
    Socio guardar(Socio socio);
    List<Socio> listar();
    Optional<Socio> buscarPorId(Long id);
}
```

Tres cosas a notar: no importa nada de Spring ni de JPA; habla de `Socio` (dominio) en las dos direcciones; y los nombres son del negocio (`guardar`, no `save`).

## 5. El Port IN

```java
package com.fitclub.socio.domain.port.in;

import com.fitclub.socio.domain.model.Socio;

import java.util.List;
import java.util.Optional;

public interface SocioUseCase {
    Socio registrar(Socio socio);
    List<Socio> listar();
    Optional<Socio> buscarPorId(Long id);
}
```

Tampoco menciona DTOs. Los DTOs son el contrato **HTTP**, y el Port IN es el contrato **del negocio**. La traducción entre ambos la hace el Web Mapper.

## 6. El Service: implementa uno, usa el otro

```java
package com.fitclub.socio.application;

import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.domain.port.in.SocioUseCase;
import com.fitclub.socio.domain.port.out.SocioRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SocioService implements SocioUseCase {

    private final SocioRepositoryPort socioRepositoryPort;

    public SocioService(SocioRepositoryPort socioRepositoryPort) {
        this.socioRepositoryPort = socioRepositoryPort;
    }

    @Override
    public Socio registrar(Socio socio) {
        return socioRepositoryPort.guardar(socio);
    }

    @Override
    public List<Socio> listar() {
        return socioRepositoryPort.listar();
    }

    @Override
    public Optional<Socio> buscarPorId(Long id) {
        return socioRepositoryPort.buscarPorId(id);
    }
}
```

Mira la lista de imports: `Socio`, los dos puertos y `@Service`. **Nada de DTOs, nada de `SocioJpaEntity`, nada de Spring Data.** Esa lista de imports es la mejor evidencia de que la regla de dependencia se cumple, y es lo que conviene mostrar si te piden demostrarlo.

## 7. El Persistence Adapter

```java
package com.fitclub.socio.infrastructure.adapter.out.persistence;

import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.domain.port.out.SocioRepositoryPort;
import com.fitclub.socio.infrastructure.adapter.out.persistence.entity.SocioJpaEntity;
import com.fitclub.socio.infrastructure.adapter.out.persistence.mapper.SocioPersistenceMapper;
import com.fitclub.socio.infrastructure.adapter.out.persistence.repository.SpringDataSocioRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SocioPersistenceAdapter implements SocioRepositoryPort {

    private final SpringDataSocioRepository springDataSocioRepository;

    public SocioPersistenceAdapter(SpringDataSocioRepository springDataSocioRepository) {
        this.springDataSocioRepository = springDataSocioRepository;
    }

    @Override
    public Socio guardar(Socio socio) {
        SocioJpaEntity entity = SocioPersistenceMapper.toEntity(socio);
        SocioJpaEntity guardado = springDataSocioRepository.save(entity);
        return SocioPersistenceMapper.toDomain(guardado);
    }

    @Override
    public List<Socio> listar() {
        return springDataSocioRepository.findAll().stream()
                .map(SocioPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Socio> buscarPorId(Long id) {
        return springDataSocioRepository.findById(id)
                .map(SocioPersistenceMapper::toDomain);
    }
}
```

Este es el **patrón Adapter** en estado puro: traduce una interfaz esperada (`SocioRepositoryPort`, en lenguaje de dominio) a una tecnología concreta (`SpringDataSocioRepository`, en lenguaje JPA). Toda la entidad JPA queda encerrada aquí dentro; nada de ella se escapa hacia arriba.

Se anota con `@Component` y no con `@Service` porque `@Service` se reserva, por convención, para la capa de aplicación. Funcionalmente son idénticas — las dos registran un bean — pero la anotación comunica intención.

## 8. El Web Mapper

```java
package com.fitclub.socio.infrastructure.adapter.in.web.mapper;

import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.infrastructure.adapter.in.web.dto.SocioRequestDTO;
import com.fitclub.socio.infrastructure.adapter.in.web.dto.SocioResponseDTO;

import java.time.LocalDate;

public final class SocioWebMapper {

    private SocioWebMapper() {}

    public static Socio toDomain(SocioRequestDTO request) {
        return new Socio(null, request.getNombre(), request.getEmail(),
                request.getTelefono(), LocalDate.now());
    }

    public static SocioResponseDTO toResponseDTO(Socio socio) {
        return new SocioResponseDTO(socio.getId(), socio.getNombre(), socio.getEmail(),
                socio.getTelefono(), socio.getFechaRegistro());
    }
}
```

### Por qué hacen falta DOS mappers

Pregunta del banco: *"¿por qué son necesarias traducciones separadas entre Web DTO ↔ Domain y Domain ↔ JPA Entity?"*

Porque son **dos fronteras distintas**, y cada una puede cambiar por su cuenta:

```
JSON ↔ [SocioWebMapper] ↔ Dominio ↔ [SocioPersistenceMapper] ↔ Tabla
```

Si el cliente pide que la API devuelva el nombre partido en `nombre` y `apellido`, cambia solo el Web Mapper. Si se renombra una columna, cambia solo el Persistence Mapper. El dominio, en el medio, no se entera de ninguno de los dos cambios. Un solo mapper que hiciera todo volvería a pegar la API a la tabla.

## 9. El Controller ahora depende del Port IN

```java
@RestController
@RequestMapping("/api/socios")
public class SocioController {

    private final SocioUseCase socioUseCase;

    public SocioController(SocioUseCase socioUseCase) {
        this.socioUseCase = socioUseCase;
    }

    @PostMapping
    public ResponseEntity<SocioResponseDTO> crear(@Valid @RequestBody SocioRequestDTO request) {
        Socio guardado = socioUseCase.registrar(SocioWebMapper.toDomain(request));
        return ResponseEntity.status(201).body(SocioWebMapper.toResponseDTO(guardado));
    }

    @GetMapping
    public List<SocioResponseDTO> listar() {
        return socioUseCase.listar().stream()
                .map(SocioWebMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
```

El campo es de tipo `SocioUseCase` (la interfaz), no `SocioService` (la clase). Es un cambio de una palabra con una consecuencia grande: el Controller ya no sabe **quién** implementa el caso de uso.

### ¿Cómo sabe Spring qué inyectar?

No hay configuración extra. Spring busca beans que implementen `SocioUseCase`, encuentra exactamente uno (`SocioService`, anotado con `@Service`) y lo inyecta. Si hubiera dos, fallaría al arrancar pidiendo que desambigües con `@Qualifier` o `@Primary`.

Eso es **inversión de control**: tú declaras qué necesitas, el contenedor decide quién lo provee.

## 10. Inversión de dependencias, IoC e inyección: tres cosas distintas

Se confunden todo el tiempo y el examen las separa.

- **Inversión de dependencias (DIP)**: un principio de diseño. *Los módulos de alto nivel no deben depender de los de bajo nivel; ambos deben depender de abstracciones.* Aquí: `SocioService` (alto nivel) no depende de `SpringDataSocioRepository` (bajo nivel); los dos dependen de `SocioRepositoryPort`.
- **Inversión de control (IoC)**: un mecanismo. Quien controla la creación y el ciclo de vida de los objetos no es tu código, es el contenedor.
- **Inyección de dependencias (DI)**: la técnica concreta con que el contenedor entrega las dependencias. Aquí, por constructor.

Dicho corto: **DIP es el principio, IoC es el mecanismo, DI es la técnica.** Puedes cumplir DIP sin Spring (pasando la implementación a mano en el `main`); Spring solo lo hace cómodo.

Y notar la dirección de la flecha: `SocioPersistenceAdapter` (infraestructura) depende de `SocioRepositoryPort` (dominio). **La infraestructura depende del núcleo, nunca al revés.** Eso es lo que significa "las dependencias apuntan hacia adentro".

## 11. Colaboración entre módulos: Membresia consulta a Socio

`MembresiaService` necesita saber si un socio existe. Tiene tres formas de averiguarlo, y solo una es correcta:

| Opción | Veredicto |
|---|---|
| Inyectar `SpringDataSocioRepository` | **Mal.** La aplicación de Membresia pasaría a depender de Spring Data y de la persistencia interna de otro módulo. |
| Inyectar `SocioRepositoryPort` | **Regular.** Ya no depende de Spring Data, pero se mete con el puerto de salida *privado* de otro módulo. |
| Inyectar `SocioUseCase` | **Correcto.** Consume la capacidad pública que el módulo Socio ofrece. |

La guía del profesor lo dice explícitamente: un módulo consulta a otro **mediante su caso de uso público, no mediante su repositorio interno**. El acoplamiento que se evita es el acoplamiento a la implementación: mañana Socio podría guardarse en otro sistema y Membresia no se enteraría.

```java
@Service
public class MembresiaService implements MembresiaUseCase {

    private final MembresiaRepositoryPort membresiaRepositoryPort;
    private final SocioUseCase socioUseCase;

    public MembresiaService(MembresiaRepositoryPort membresiaRepositoryPort, SocioUseCase socioUseCase) {
        this.membresiaRepositoryPort = membresiaRepositoryPort;
        this.socioUseCase = socioUseCase;
    }

    @Override
    public Membresia registrar(Membresia membresia) {
        socioUseCase.buscarPorId(membresia.getSocioId())
                .orElseThrow(() -> new SocioNoEncontradoException(membresia.getSocioId()));
        return membresiaRepositoryPort.guardar(membresia);
    }
}
```

### La excepción: el adaptador SÍ puede cruzar

`MembresiaPersistenceAdapter` necesita un `SocioJpaEntity` para construir la FK, y lo obtiene así:

```java
SocioJpaEntity socioRef = springDataSocioRepository.getReferenceById(membresia.getSocioId());
```

¿No contradice lo anterior? No. La regla de "consultar por caso de uso" protege la **capa de aplicación**. Este código está en **infraestructura**, y infraestructura hablando con infraestructura es legítimo: son dos detalles técnicos del mismo nivel.

`getReferenceById` devuelve un proxy sin ejecutar ningún SELECT: solo necesitamos la referencia para la FK, y la existencia del socio ya la validó el Service.

## 12. Verificar que el refactor no rompió nada

Corre la colección completa de Postman. Todos los códigos deben ser **idénticos** a los de antes del refactor. Si alguno cambió, algo se rompió.

## 13. Qué NO debes hacer

- No hagas que el Port OUT extienda `JpaRepository`.
- No menciones HTTP ni DTOs en los puertos.
- No inyectes el repositorio de Spring Data en un Service.
- No inyectes `SocioRepositoryPort` desde otro módulo: usa `SocioUseCase`.
- No crees interfaces y carpetas "porque la arquitectura hexagonal lo pide" si no hay una dependencia real que invertir. El propio profesor lo marca como error.

## 14. Antes de dar por listo el capítulo

- [ ] La lista de imports de tus Services no contiene DTOs, entidades JPA ni Spring Data.
- [ ] Los puertos están en `domain/port/in` y `domain/port/out`.
- [ ] Los Controllers declaran el tipo del Port IN, no el de la clase Service.
- [ ] La colección de Postman da los mismos códigos que antes.
- [ ] Puedes explicar la diferencia entre Port IN y Controller, y entre Port OUT y `JpaRepository`.
- [ ] Puedes distinguir DIP, IoC y DI.

## 15. Commit

```bash
git add .
git commit -m "refactor: introduce hexagonal ports and persistence adapters"
```

## Actividad para practicar

Refactoriza `Entrenador` y `Rutina` a la arquitectura hexagonal.

1. Crea `EntrenadorRepositoryPort` en `domain/port/out` y `EntrenadorUseCase` en `domain/port/in`.
2. Crea `EntrenadorPersistenceAdapter` que implemente el puerto de salida.
3. Crea `EntrenadorWebMapper` y saca la conversión DTO↔dominio del Service.
4. Cambia `EntrenadorService` para que implemente el Port IN y dependa solo del Port OUT.
5. Cambia `EntrenadorController` para que declare el Port IN.
6. Repite todo para `Rutina`, con un detalle: `RutinaService` debe validar la existencia del entrenador **a través de `EntrenadorUseCase`**, no del repositorio.
7. Corre la colección de Postman antes y después y compara los códigos uno por uno.

Ejercicio de verificación: abre `RutinaService` y mira sus imports. Si aparece `RutinaJpaEntity`, `SpringDataRutinaRepository` o cualquier DTO, el refactor no está terminado — corrígelo antes de seguir.

Preguntas para responder por escrito:

- ¿Qué dependencia concreta evita tu `RutinaRepositoryPort`?
- Si mañana cambiaras PostgreSQL por archivos en disco, ¿cuáles de tus clases tendrías que reescribir y cuáles no tocarías?
- ¿Por qué `RutinaService` no puede inyectar `SpringDataEntrenadorRepository`, si técnicamente compilaría igual?
