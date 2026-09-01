## Cómo armar una interfaz de repositorio (el "contrato")

### 0. ¿Qué es una interfaz?

Una interfaz es una lista de métodos SIN implementación — solo dice el nombre, qué recibe y qué devuelve cada método, pero no dice cómo lo hace. Es un contrato: "quien implemente esto, promete tener estos métodos funcionando."

```java
public interface SocioRepository {
    Socio guardar(Socio socio);
    Optional<Socio> buscarPorId(Long id);
    List<Socio> listarTodos();
    boolean existePorCampoUnico(String numeroDocumento);
}
```

Fíjate que ningún método tiene `{ }` con código adentro, solo terminan en `;`. Eso es lo que la hace una interfaz y no una clase normal.

### 1. ¿Para qué sirve esto en la práctica?

La idea es separar **QUÉ necesita hacer tu programa** (guardar un socio, buscarlo, listarlos) de **CÓMO se hace realmente** (¿en un `Map` en memoria? ¿en PostgreSQL? ¿en un archivo?). La interfaz solo define el QUÉ. El CÓMO lo define una clase aparte que la implementa.

Esto importa porque más adelante (Capítulo 03+, cuando conectemos PostgreSQL de verdad) vamos a poder cambiar el CÓMO —de un `Map` en memoria a la base de datos real— sin tocar el resto del programa, porque todo lo demás solo conoce la interfaz, nunca la implementación concreta.

### 2. Cómo se crea en IntelliJ

1. Clic derecho sobre el package donde va el contrato (ej. `socio.domain.port`) → **New** → **Java Class**.
2. En el diálogo, selecciona **Interface** en vez de "Class".
3. Ponle el nombre (ej. `SocioRepository`) y dale Enter.
4. Escribe ahí los métodos que necesitas, terminados en `;`, sin cuerpo.

¿Por qué en un package llamado `port`? Es una convención: `domain/port` significa "el contrato que expone el dominio hacia afuera" (viene de arquitectura hexagonal, que es hacia donde va este curso poco a poco). No es obligatorio el nombre exacto, pero así lo pide la guía del curso.

### 3. La implementación

Una clase normal, que usa la palabra `implements` para decir "yo cumplo este contrato", y tiene que escribir el cuerpo de TODOS los métodos de la interfaz (si te falta uno, no compila).

```java
public class SocioRepositoryEnMemoria implements SocioRepository {

    private final Map<Long, Socio> datos = new LinkedHashMap<>();

    @Override
    public Socio guardar(Socio socio) {
        datos.put(socio.getSocioId(), socio);
        return socio;
    }

    @Override
    public Optional<Socio> buscarPorId(Long id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Socio> listarTodos() {
        return new ArrayList<>(datos.values());
    }

    @Override
    public boolean existePorCampoUnico(String numeroDocumento) {
        return datos.values().stream()
                .anyMatch(s -> s.getNumeroDocumento().equalsIgnoreCase(numeroDocumento));
    }
}
```

`@Override` no es obligatorio para que compile, pero siempre ponlo: si te equivocas escribiendo el nombre de un método (por ejemplo `buscarPorid` en vez de `buscarPorId`), con `@Override` Java te avisa el error al momento; sin él, simplemente tendrías un método nuevo que no cumple el contrato, y te enterarías mucho después.

**En IntelliJ**, cuando escribes `implements SocioRepository` y todavía no tienes los métodos, IntelliJ te subraya la clase en rojo y te ofrece (Alt+Enter) **"Implement methods"** — selecciona todos y te genera automáticamente el esqueleto de los 4 métodos, ya con `@Override`, listo para que rellenes el cuerpo.

### 4. ¿Por qué `Optional<Socio>` y no `Socio` directo en `buscarPorId`?

Porque buscar por id puede fallar — puede que ese id no exista. `Optional` hace visible esa posibilidad en el tipo de retorno, en vez de arriesgarte a que el método devuelva `null` y en algún punto del código, sin darte cuenta, uses ese `null` y te salga un `NullPointerException`.

Para usarlo:

```java
Socio socio = repository.buscarPorId(id)
        .orElseThrow(() -> new SocioNoEncontradoException(id));
```

`.orElseThrow(...)` dice: "si lo encontraste, dame el Socio; si no, lanza esta excepción."

### 5. El servicio: depende de la interfaz, nunca de la implementación

```java
public class SocioService {

    private final SocioRepository repository;   // <- la interfaz, NO SocioRepositoryEnMemoria

    public SocioService(SocioRepository repository) {
        this.repository = repository;
    }

    public Socio registrar(Socio socio) {
        if (repository.existePorCampoUnico(socio.getNumeroDocumento())) {
            throw new DocumentoDuplicadoException(socio.getNumeroDocumento());
        }
        return repository.guardar(socio);
    }

    public Socio obtener(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new SocioNoEncontradoException(id));
    }

    public List<Socio> listar() {
        return repository.listarTodos();
    }
}
```

Esta es la parte que más se pregunta en la defensa: el atributo `repository` es de tipo `SocioRepository` (la interfaz), no `SocioRepositoryEnMemoria` (la implementación). Así, `SocioService` no sabe ni le importa si por dentro hay un `Map` en memoria o PostgreSQL — solo sabe que cumple el contrato.

### 6. Cómo se conecta todo desde `Main`

```java
SocioRepository socioRepository = new SocioRepositoryEnMemoria();   // decides el CÓMO aquí, una sola vez
SocioService socioService = new SocioService(socioRepository);       // el service solo ve el contrato

Socio socio1 = socioService.registrar(new Socio(...));
```

El único lugar de todo el programa donde aparece la palabra `SocioRepositoryEnMemoria` es esta línea. En todos los demás lados —el service, cualquier otra clase que reciba el repositorio— solo se usa `SocioRepository`. Ese es el punto: el día que cambiemos memoria por PostgreSQL, esta es la ÚNICA línea que cambia.

### 7. Excepciones propias (para completar el flujo)

Van de la mano con esto — se lanzan desde el service cuando algo no cumple una regla:

```java
public class SocioNoEncontradoException extends RuntimeException {
    public SocioNoEncontradoException(Long id) {
        super("No existe el socio con id: " + id);
    }
}
```

Se crean igual que cualquier clase (New → Java Class), solo que heredan de `RuntimeException` (con `extends`) y su constructor arma el mensaje de error.

### 8. Errores comunes

| Error | Por qué está mal |
|---|---|
| Que el service reciba `SocioRepositoryEnMemoria` en vez de `SocioRepository` | Rompe todo el propósito del patrón — acopla el service a una implementación concreta |
| Devolver `null` en vez de `Optional.empty()` en una búsqueda que puede fallar | Vuelve al riesgo del `NullPointerException` que `Optional` justamente evita |
| Que la interfaz mencione SQL, PostgreSQL o JPA en sus métodos | Rompe la separación QUÉ vs. CÓMO — la interfaz no debe saber nada de la tecnología de persistencia |
| Olvidar `@Override` en los métodos de la implementación | No rompe el código, pero pierdes la ayuda de Java para detectar errores de nombre |

### 9. Antes de dar por listo tu repositorio

- [ ] La interfaz no menciona SQL, PostgreSQL ni JPA en ningún método
- [ ] La implementación usa `@Override` en cada método
- [ ] `buscarPorId` (o cualquier búsqueda que pueda fallar) devuelve `Optional`
- [ ] El service (o cualquier clase que use el repositorio) declara el atributo con el tipo de la interfaz, no de la implementación
- [ ] Tienes al menos una excepción propia para el caso "no encontrado" y otra para el caso "valor único duplicado"

### 10. Practica: crea esto desde cero

Con tu `Libro` (ya con su `EstadoLibro`, de la práctica de `Enum.md`), arma el mismo patrón de repositorio que hicimos con `Socio`:

1. Crea la interfaz `LibroRepository` con: `guardar`, `buscarPorId` (devolviendo `Optional<Libro>`), `listarTodos`, y `existePorCampoUnico` (usando `isbn` como campo único, ya que dos libros no deberían compartir el mismo ISBN).
2. Crea `LibroRepositoryEnMemoria`, que implementa la interfaz con un `Map`.
3. Crea dos excepciones propias: `LibroNoEncontradoException` e `IsbnDuplicadoException`.
4. Crea `LibroService`, con `registrar` (que valide el ISBN duplicado), `obtener` (que use `Optional.orElseThrow`), y `listar`.

Todavía no escribas el `Main` que prueba todo esto — esa es la última parte de la práctica, y está en `Main.md`, con instrucciones detalladas de qué probar y en qué orden.
