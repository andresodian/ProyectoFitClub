## Cómo funciona Main.java (y cómo probar tu código con él)

### 0. ¿Qué es?

`Main.java` es el punto de partida de cualquier programa Java — cuando le das "Run", Java busca una clase con un método escrito exactamente así, y empieza a ejecutar desde ahí:

```java
public class Main {
    public static void main(String[] args) {
        // aquí va el código que se ejecuta
    }
}
```

Sin este método, con esta firma exacta, Java no tiene por dónde empezar y no hay nada que correr.

### 1. La firma, explicada por partes

No hace falta memorizar la teoría completa de cada palabra, pero conviene reconocerlas:

| Palabra | Qué significa, en corto |
|---|---|
| `public` | Cualquiera puede acceder a este método, incluyendo Java mismo al arrancar el programa |
| `static` | Se puede ejecutar sin necesidad de crear primero un objeto `Main` — Java lo llama directo |
| `void` | No devuelve ningún valor (solo ejecuta código) |
| `main` | El nombre exacto que Java busca — no puede llamarse de otra forma |
| `(String[] args)` | Puede recibir argumentos de texto desde la línea de comandos (casi nunca lo vamos a usar por ahora, pero la firma lo exige igual) |

### 2. Cómo crearlo en IntelliJ

Si ya creaste el proyecto siguiendo `IntelliJ.md`, probablemente ya tengas uno. Si necesitas crear uno nuevo (por ejemplo, para un proyecto de práctica aparte):

1. Clic derecho sobre tu package base → **New** → **Java Class**.
2. Nombre: `Main`.
3. Adentro de la clase, escribe `psvm` y presiona **Tab** — es un atajo de IntelliJ que completa solo toda la firma `public static void main(String[] args) { }`.

### 3. Cómo correrlo

- Va a aparecer una flecha verde ▶ en el margen izquierdo, al lado de `public static void main` o al lado de `public class Main`. Dale clic — o usa el atajo **Shift+F10**.
- Lo que imprimas con `System.out.println(...)` aparece abajo, en el panel **Run**.
- Puedes volver a correrlo las veces que quieras; cada vez limpia la consola y ejecuta todo de nuevo desde cero (los objetos que creaste la vez anterior no quedan guardados en ningún lado — por eso todavía estamos usando el `Map` en memoria, no una base de datos real).

### 4. Qué escribir dentro de un Main de prueba

El patrón que vas a repetir casi siempre, tal como lo pide el curso:

1. Crea uno o dos objetos de tu entidad principal.
2. Si tienes un repositorio/service (como vimos en `Interfaz.md`), regístralos a través de ahí, no con `new` directo.
3. Prueba que las operaciones funcionen: listar, buscar uno que existe.
4. Provoca a propósito los casos que deberían fallar (buscar un id que no existe, registrar un valor único repetido) y confirma que salta la excepción correcta.
5. Imprime evidencia de todo con `System.out.println(...)`, para poder ver en la consola que efectivamente funcionó.

### 5. Cómo leer lo que te muestra la consola

Hay dos tipos de problemas que vas a ver, y son distintos:

**Error de compilación** (tu código está mal escrito, ni siquiera llegó a correr): IntelliJ te lo marca en rojo en el propio código ANTES de correr, y si igual le das Run, en la consola vas a ver algo como `error: cannot find symbol` o `';' expected`. Hay que corregir el código, no es algo que "pase" mientras corre.

**Excepción en tiempo de ejecución** (tu código compiló bien, pero algo salió mal mientras corría — por ejemplo, buscaste un id que no existe): esto es justo lo que esperamos cuando probamos a propósito una excepción propia, como vimos en `Interfaz.md`. Se ve así en la consola:

```
ERROR CONTROLADO: No existe el socio con id: 999
```

Si armaste tu propio mensaje dentro del `catch`, así es como se ve — no es un error real del programa, es tu código funcionando como debía, avisando que la regla se cumplió correctamente.

### 6. Un Main real de FitClub, explicado

Este es un fragmento real de tu proyecto (`Main.java`), para que veas el patrón completo funcionando:

```java
SocioRepository socioRepository = new SocioRepositoryEnMemoria();
SocioService socioService = new SocioService(socioRepository);

Socio socio1 = socioService.registrar(new Socio(
        1L, "12345678", "Carlos Gómez", "carlos@email.com", "70000000",
        LocalDate.of(1995, 5, 20), true, OffsetDateTime.now()
));

System.out.println("Socios registrados: " + socioService.listar().size());

try {
    socioService.obtener(999L);
} catch (SocioNoEncontradoException ex) {
    System.out.println("ERROR CONTROLADO: " + ex.getMessage());
}
```

Línea por línea: primero se decide el CÓMO (memoria, línea 1), se crea el service pasándole el repositorio (línea 2), se registra un socio a través del service —no con `new Socio(...)` suelto— (líneas 4-7), se imprime evidencia de que se guardó (línea 9), y al final se provoca a propósito un caso de error para comprobar que la excepción funciona (líneas 11-15).

### 7. Errores comunes

| Error | Por qué pasa |
|---|---|
| "Error: Main method not found" al correr | La clase que intentaste correr no tiene el método `main`, o le falta `static`, o el nombre no es exactamente `main` |
| Tener dos clases con método `main` y no saber cuál corrió | IntelliJ corre la que tenía el cursor o la que seleccionaste — revisa el nombre en la pestaña de la consola arriba, dice qué clase se ejecutó |
| Modificar el código y correr sin guardar | IntelliJ normalmente guarda solo, pero si algo no se refleja, revisa que no haya un asterisco (*) en la pestaña del archivo indicando cambios sin guardar |
| Pensar que un `catch` que imprime un error es un fallo del programa | Si tú mismo provocaste ese error a propósito para probar una excepción, eso es éxito, no fallo |

### 8. Antes de dar por lista tu prueba en Main

- [ ] Tu `Main` crea al menos dos objetos de tu entidad principal
- [ ] Si tienes repositorio/service, los usas a través de ahí (no `new` directo salvo para crear el objeto a registrar)
- [ ] Probaste al menos un caso que funciona bien (positivo)
- [ ] Probaste al menos un caso que debería fallar, y capturaste la excepción con `try/catch` (negativo)
- [ ] Todo lo importante queda impreso en la consola con `System.out.println(...)`

### 9. Practica: junta todo lo que armaste

Esta es la parte final de la práctica que empezaste en `Clase.md`, `Enum.md` e `Interfaz.md` con la clase `Libro`. Ya deberías tener: la clase `Libro` (con su `EstadoLibro`), la interfaz `LibroRepository`, `LibroRepositoryEnMemoria`, dos excepciones propias, y `LibroService`.

Ahora escribe el `Main` que lo prueba todo junto, exactamente con la misma estructura del ejemplo real de la sección 6:

1. Crea el repositorio y el service: `new LibroRepositoryEnMemoria()` y `new LibroService(repositorio)`.
2. Registra dos libros con ISBN distinto, a través del service.
3. Imprime cuántos libros hay registrados.
4. Busca uno por id que sí existe, e imprime su título.
5. Intenta buscar un id que no existe, dentro de un `try/catch`, y confirma que se imprime tu mensaje de `LibroNoEncontradoException`.
6. Intenta registrar un libro con un ISBN repetido, dentro de otro `try/catch`, y confirma que se imprime tu mensaje de `IsbnDuplicadoException`.
7. Corre el programa (Shift+F10) y revisa la consola: deberías ver exactamente 6 líneas de evidencia, una por cada paso.

Si te sale algún error, compártemelo (el mensaje completo de la consola) y lo revisamos juntos.
