# Swagger / OpenAPI: documentar la API automáticamente

Este no es un capítulo del curso; es un agregado que el docente mencionó. Cuesta dos cambios y da una página web donde cualquiera puede ver y probar todos tus endpoints sin instalar nada.

## 0. Qué es cada nombre

Los tres se usan como sinónimos y no lo son:

- **OpenAPI** es la **especificación**: un formato estándar (un JSON o YAML) que describe una API REST — qué endpoints existen, qué reciben, qué devuelven, qué errores.
- **Swagger UI** es una **página web** que lee ese JSON y lo dibuja como una interfaz navegable, con botón para probar cada endpoint.
- **springdoc-openapi** es la **librería** que, en una aplicación Spring Boot, inspecciona tus Controllers y genera el JSON de OpenAPI solo, sin que escribas documentación a mano.

## 1. La dependencia

En el `pom.xml`, dentro de `<dependencies>`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.1.1</version>
</dependency>
```

**La versión importa y no es opcional.** springdoc tiene su propio versionado, que no coincide con el de Spring Boot:

| springdoc-openapi | Spring Boot compatible |
|---|---|
| 1.x | 2.7.x |
| 2.x | 3.x |
| **3.x** | **4.x** ← el nuestro |

Como este proyecto usa Spring Boot 4.1.1, la versión correcta es la línea **3.1.1**. Si copias un tutorial con `2.5.0` (pensado para Boot 3), no va a funcionar.

Y a diferencia de las dependencias de Spring, esta **sí lleva `<version>` explícita**: el `spring-boot-starter-parent` solo gestiona versiones de las librerías que conoce, y springdoc es de un tercero.

Después de agregarla, IntelliJ muestra un aviso de **Load Maven Changes** arriba a la derecha; si no aparece, clic derecho sobre `pom.xml` → `Maven → Reload Project`.

## 2. Los metadatos de la API

En la clase principal:

```java
package com.fitclub;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(
        title = "FitClub API",
        version = "1.0",
        description = "Documentación de los endpoints del backend de FitClub (Socio y Membresia)"
))
@SpringBootApplication
public class PruebaFitclubApplication {

    public static void main(String[] args) {
        SpringApplication.run(PruebaFitclubApplication.class, args);
    }
}
```

Esto es **opcional**: sin la anotación, Swagger funciona igual pero muestra un título genérico. Con ella, la página se identifica correctamente.

## 3. Dónde verlo

Arranca la aplicación y abre en el navegador:

```
http://localhost:8080/swagger-ui/index.html
```

Vas a ver tus endpoints agrupados por Controller (`socio-controller`, `membresia-controller`), cada uno con los parámetros que acepta, el esquema del body y los códigos de respuesta. El botón **Try it out** te deja enviar una petición real desde ahí mismo.

La especificación cruda está en:

```
http://localhost:8080/v3/api-docs
```

Ese JSON es lo que consumen otras herramientas: puedes importarlo en Postman para generar la colección automáticamente, o dárselo a quien haga el frontend para que genere el cliente.

## 4. Lo que hace solo (y por qué)

No tuviste que tocar ni un Controller. springdoc lee, en tiempo de arranque:

- Las anotaciones de mapeo (`@RestController`, `@RequestMapping`, `@PostMapping`, `@GetMapping`) para saber qué endpoints existen.
- Los tipos de los parámetros y del `@RequestBody` para deducir los esquemas de entrada.
- El tipo genérico de `ResponseEntity<T>` para deducir el esquema de salida.
- Las anotaciones de Jakarta Validation del DTO para marcar qué campos son obligatorios y con qué formato.

Es decir: **la documentación sale del código real**, así que no puede quedar desactualizada respecto de lo que la API hace de verdad. Ese es el argumento fuerte a favor frente a un documento escrito a mano.

## 5. Mejorarlo (opcional)

Si quieres descripciones en español en vez de los nombres derivados de los métodos:

```java
@Tag(name = "Socios", description = "Alta y consulta de socios del gimnasio")
@RestController
@RequestMapping("/api/socios")
public class SocioController {

    @Operation(summary = "Registrar un socio nuevo")
    @PostMapping
    public ResponseEntity<SocioResponseDTO> crear(@Valid @RequestBody SocioRequestDTO request) { ... }
}
```

No es obligatorio y el proyecto de práctica no lo usa. Si lo agregas, que sea porque aporta claridad, no por decorar.

## 6. Swagger no reemplaza a Postman

Se parecen pero sirven para cosas distintas:

| | Swagger UI | Postman |
|---|---|---|
| De dónde sale | Se genera solo del código | Lo armas tú |
| Para qué sirve | Documentar y explorar | Probar casos concretos y guardarlos como evidencia |
| Casos de error | No los guarda | Sí: tu colección conserva el 400, el 404 y el 409 |
| Evidencia para el profesor | Sirve para mostrar la API | Es lo que se pide como evidencia |

Sigue usando Postman para las evidencias. Swagger es para que otra persona entienda tu API en treinta segundos.

## 7. Antes de dar por listo este paso

- [ ] `http://localhost:8080/swagger-ui/index.html` abre y muestra tus dos (o tres) controllers.
- [ ] Probaste un endpoint con **Try it out** y funcionó.
- [ ] Puedes explicar la diferencia entre OpenAPI, Swagger UI y springdoc.
- [ ] Sabes por qué esta dependencia lleva `<version>` y las de Spring no.

## Actividad para practicar

1. Abre Swagger y localiza tus endpoints de `entrenadores` y `rutinas`. Comprueba que aparecieron **sin que hicieras nada**.
2. Entra a `http://localhost:8080/v3/api-docs` y busca en el JSON el esquema de `EntrenadorRequestDTO`. Encuentra dónde quedó registrado que `email` es obligatorio y con formato de correo. Anota de qué anotación de tu código salió ese dato.
3. Usa **Try it out** para crear un entrenador y compruébalo después en DataGrip.
4. Agrega `@Tag` y `@Operation` **solo al Controller de Rutina**, recarga Swagger y compara los dos controllers lado a lado.
5. Responde por escrito: si un compañero cambia un DTO y olvida actualizar la documentación escrita del proyecto, ¿qué pasa con Swagger? ¿Y por qué eso es un argumento a favor de generar documentación desde el código?
