## Cómo armar una clase Java (paso a paso)

Esto es lo que casi siempre vas a hacer cada vez que crees una clase nueva que representa algo de tu base de datos (un Socio, una Clase, un Plan, etc.). Es el mismo patrón una y otra vez, así que una vez que lo entiendes, se vuelve automático.

### 0. ¿Dónde va esto?

Antes de escribir nada, la clase tiene que estar en el package correcto (revisa `IntelliJ.md` si tienes dudas de cómo crear packages). Por ejemplo, si estás creando la clase de un Plan, va en `com.fitclub.plan.domain.model`, y el archivo se llama `Plan.java` — mismo nombre que la clase pública que tiene adentro.

### 1. Declara los atributos como `private`

Escribe primero, uno por línea, cada columna de tu tabla como un atributo de la clase. Usa `private` siempre, nunca `public`.

```java
public class Plan {
    private Long planId;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer duracionDias;
    private Boolean activo;
}
```

**¿Por qué `private`?** Porque si lo dejas `public`, cualquier parte del programa podría cambiar el valor sin control (por ejemplo, poner un precio negativo). Con `private`, el único jeito de leer o cambiar el valor es a través de los métodos que tú defines (los getters y setters) — así el objeto controla su propio estado.

### 2. El constructor vacío

```java
public Plan() {
}
```

No recibe nada, no hace nada adentro. Sirve para poder crear un objeto "en blanco" y llenarlo después con los setters, uno por uno. Más adelante, cuando lleguemos a Spring Boot / JPA (Capítulo 03 en adelante), este constructor vacío es obligatorio porque el framework lo necesita para poder reconstruir tus objetos automáticamente desde la base de datos — por eso se acostumbra desde ya, aunque todavía no lo estés usando para eso.

### 3. El constructor con todo

Este sí recibe todos los atributos como parámetros, y los va asignando uno por uno con `this.atributo = atributo`.

```java
public Plan(Long planId, String nombre, String descripcion, BigDecimal precio, Integer duracionDias, Boolean activo) {
    this.planId = planId;
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.precio = precio;
    this.duracionDias = duracionDias;
    this.activo = activo;
}
```

`this.planId` es el atributo de la clase; `planId` (sin `this.`) es el parámetro que llegó al constructor. El `this.` es lo que le dice a Java "el de la izquierda es el de la clase, no el parámetro" — sin eso, Java no sabría distinguirlos porque se llaman igual.

Este constructor te deja crear un objeto completo de una sola vez: `new Plan(10L, "Plan Anual VIP", "...", precio, 365, true)`.

### 4. Los getters y setters

Por cada atributo, creas un método para leerlo (`get...`) y uno para cambiarlo (`set...`):

```java
public Long getPlanId() {
    return planId;
}

public void setPlanId(Long planId) {
    this.planId = planId;
}
```

Se repite igual para cada atributo. Sí, es repetitivo — por eso casi nadie lo escribe a mano.

### 5. El atajo: que IntelliJ los genere solos

No escribas los constructores ni los getters/setters a mano. En IntelliJ:

1. Con el cursor dentro de la clase (después de declarar los atributos), presiona **Alt+Insert** (o clic derecho → **Generate...**).
2. Elige **Constructor** → selecciona ningún campo (Ctrl+A para seleccionar todos y luego deselecciona todos, o simplemente dale a OK sin marcar nada) → esto te crea el constructor vacío.
3. Repite Alt+Insert → **Constructor** → esta vez selecciona TODOS los campos → esto te crea el constructor completo.
4. Repite Alt+Insert → **Getter and Setter** → selecciona todos los campos → esto te crea todos los getters y setters de un solo golpe.

Así es como se ve la clase completa una vez que armas las 4 partes:

```java
package com.fitclub.plan.domain.model;

import java.math.BigDecimal;

public class Plan {
    private Long planId;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer duracionDias;
    private Boolean activo;

    public Plan() {
    }

    public Plan(Long planId, String nombre, String descripcion, BigDecimal precio, Integer duracionDias, Boolean activo) {
        this.planId = planId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.duracionDias = duracionDias;
        this.activo = activo;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    // ... y así con el resto de los atributos
}
```

Este es literalmente el mismo `Plan.java` que ya tienes en tu proyecto real — no es un ejemplo inventado.

### 6. Errores comunes

| Error | Por qué está mal |
|---|---|
| Poner atributos `public` | Cualquiera podría cambiarlos sin control, rompiendo reglas de negocio |
| Olvidar el `this.` en el constructor | Java no sabría si te refieres al atributo o al parámetro |
| Que el orden de los parámetros del constructor no coincida con cómo lo llamas después | Vas a asignar valores al campo equivocado sin que Java te avise (si son del mismo tipo, ej. dos `String`) |
| Nombrar el archivo distinto a la clase pública | Java no compila — el archivo `Plan.java` debe contener `public class Plan` |

### 7. Antes de dar por lista tu clase

- [ ] Todos los atributos son `private`
- [ ] Tiene constructor vacío
- [ ] Tiene constructor con todos los atributos
- [ ] Tiene getter y setter de cada atributo
- [ ] El nombre del archivo coincide con el nombre de la clase
- [ ] Está en el package correcto

### 8. Practica: crea esto desde cero

Vas a crear una clase `Libro`, para un proyecto de biblioteca inventado (no es de FitClub, es solo para practicar). Puedes hacerlo en un proyecto nuevo de IntelliJ, o en un package aparte como `com.practica.biblioteca.domain`.

Atributos de `Libro`:
- `libroId` (Long)
- `titulo` (String)
- `autor` (String)
- `isbn` (String)
- `anioPublicacion` (Integer)

Sigue los 4 pasos: atributos `private` → constructor vacío → constructor con todo → getters y setters (usa Alt+Insert, no lo escribas a mano).

Después, en un `Main`, crea dos objetos `Libro` distintos e imprime el título y autor de cada uno con `System.out.println`. Si no sabes cómo crear o correr un `Main`, revisa `Main.md` — ahí está explicado paso a paso, y al final tiene la continuación de esta misma práctica.
