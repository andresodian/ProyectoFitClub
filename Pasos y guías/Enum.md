## Cómo y cuándo usar un enum

### 0. ¿Qué es?

Un `enum` es un tipo de dato que solo puede valer una de un conjunto fijo de opciones que tú defines. Nada más. No es una clase "con límite de 5 cosas" (eso era un recuerdo mezclado de clase) — puede tener 2, 5, 20, las que necesites. Lo único que importa es que el conjunto sea **cerrado**: que no tenga sentido que alguien escriba cualquier texto ahí.

### 1. ¿Cuándo lo uso?

La señal más clara: cuando en PostgreSQL tienes una columna con un `CHECK ... = ANY (ARRAY[...])` que solo permite un puñado de valores fijos. Si la base de datos ya está diciendo "esta columna solo puede ser una de estas opciones", esa es tu pista para usar `enum` en Java en vez de `String`.

Ejemplo real de tu proyecto: la tabla `membresia` tiene esta restricción:

```sql
CHECK (estado = ANY (ARRAY['ACTIVA','INACTIVA','SUSPENDIDA','VENCIDA','CANCELADA']))
```

Cinco valores fijos, ni uno más ni uno menos — de ahí salió el enum `EstadoMembresia` que ya tienes en tu proyecto. (Este es probablemente el origen de lo del "máximo 5" que recordabas de clase: no es una regla del lenguaje, es que ESTE campo específico tenía 5 valores.)

**Lo que NUNCA debes hacer:** inventarte los valores del enum a ojo. Siempre sácalos de la restricción real de tu tabla (`CHECK` en PostgreSQL) o de las reglas de tu proyecto. Si no la tienes a la mano, puedes consultarla desde DataGrip.

### 2. Cómo se ve un enum

```java
public enum EstadoMembresia {
    ACTIVA,
    INACTIVA,
    SUSPENDIDA,
    VENCIDA,
    CANCELADA
}
```

Así de simple. Cada palabra en MAYÚSCULAS_CON_GUION_BAJO es una constante — un valor posible del enum. No lleva `private`, no lleva constructor (a menos que necesites algo más avanzado, que no es el caso todavía), no lleva `;` después de cada valor, solo comas entre ellos.

### 3. Cómo se crea en IntelliJ

1. Clic derecho sobre el package donde va (ej. `plan.domain.model`) → **New** → **Java Class**.
2. En el diálogo que aparece, en vez de dejarlo en "Class", selecciona **Enum**.
3. Ponle el nombre (ej. `EstadoMembresia`) y dale Enter.
4. Dentro, escribe los valores separados por comas.

### 4. Cómo se usa dentro de otra clase

Antes de tener el enum, el campo era texto libre:

```java
private String estado;   // cualquiera podía poner "activa", "Activa123", lo que sea
```

Con el enum, el campo pasa a ser del tipo del enum, no `String`:

```java
private EstadoMembresia estado;   // solo puede valer una de las 5 constantes, nada más
```

Y el getter/setter cambian su tipo igual:

```java
public EstadoMembresia getEstado() {
    return estado;
}

public void setEstado(EstadoMembresia estado) {
    this.estado = estado;
}
```

### 5. Cómo se usa al crear un objeto

Para dar un valor, escribes `NombreDelEnum.VALOR`:

```java
Membresia membresia = new Membresia(
        100L, socioId, planId, fechaInicio, fechaFin,
        EstadoMembresia.ACTIVA,   // <- así se usa, no como texto "ACTIVA"
        OffsetDateTime.now()
);
```

Si intentas poner `"ACTIVA"` (con comillas, como texto) donde se espera un `EstadoMembresia`, el proyecto ni siquiera va a compilar — y esa es justo la ventaja: el error te lo avisa Java al momento, no lo descubres después con datos corruptos en la base.

### 6. Comparar valores de un enum

Para comparar, usa `==` (no `.equals()` como harías con String) o un `switch`:

```java
if (membresia.getEstado() == EstadoMembresia.VENCIDA) {
    // lo que corresponda hacer si está vencida
}
```

Funciona con `==` porque cada constante del enum es un único objeto fijo en memoria — no hay dos formas distintas de "ser" `EstadoMembresia.VENCIDA`.

### 7. Errores comunes

| Error | Por qué está mal |
|---|---|
| Inventar valores que no están en el CHECK real de la tabla | Vas a tener datos que la base de datos rechazaría, o que no reflejan la regla real del negocio |
| Dejar el campo como `String` "por si acaso" | Pierdes toda la protección: cualquier texto pasaría, incluso mal escrito |
| Comparar con `.equals("ACTIVA")` en vez de `== EstadoMembresia.ACTIVA` | No es un error que rompa el código, pero no es la forma correcta en Java para enums |
| Poner los valores en minúsculas o mixtos | La convención de Java es MAYÚSCULAS_CON_GUION_BAJO para las constantes de enum |

### 8. Antes de dar por listo tu enum

- [ ] Los valores salen de un `CHECK` real de PostgreSQL (o de una regla confirmada del proyecto), no inventados
- [ ] Está en MAYÚSCULAS_CON_GUION_BAJO
- [ ] El campo que lo usa cambió de `String` al tipo del enum (en el atributo, constructor, getter y setter)
- [ ] En cualquier lugar donde antes escribías el valor como texto (`"ACTIVA"`), ahora usas `EstadoMembresia.ACTIVA`

### 9. Practica: crea esto desde cero

Sobre el `Libro` que creaste en la práctica de `Clase.md`: agrégale un atributo de estado, usando un enum nuevo llamado `EstadoLibro` con estos valores: `DISPONIBLE`, `PRESTADO`, `EN_REPARACION`, `PERDIDO`.

1. Crea el enum `EstadoLibro` (New → Java Class → Enum).
2. Agrega el atributo `estado` de tipo `EstadoLibro` a la clase `Libro` (con su constructor y su getter/setter — vas a tener que regenerar el constructor completo para que incluya este nuevo atributo).
3. En tu `Main`, crea dos libros con estados distintos (uno `DISPONIBLE`, otro `PRESTADO`).
4. Escribe un `if` que compare el estado de un libro con `EstadoLibro.DISPONIBLE` usando `==`, e imprime un mensaje distinto según el resultado.

La continuación de esta misma práctica sigue en `Interfaz.md` y termina en `Main.md`.
