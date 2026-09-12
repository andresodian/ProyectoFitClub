# Crear el proyecto Spring Boot (Capítulos 03–04)

Esta guía cubre lo que hay que tener listo *antes* de escribir el primer endpoint: el proyecto, las dependencias, la configuración y comprobar que arranca. Es el punto de partida de todo lo demás.

## 0. Qué es Spring Boot y qué NO es

Spring Framework es el conjunto de librerías que resuelve inyección de dependencias, web, transacciones y acceso a datos. Spring Boot es una capa encima que te ahorra la configuración manual: trae un servidor Tomcat embebido, autoconfigura lo que encuentra en el classpath y te deja arrancar la aplicación con un `main()` normal de Java.

Dicho de otra forma: **Spring Framework es el motor, Spring Boot es el auto ya armado**. Esa distinción es una pregunta literal del banco de defensa, así que conviene tenerla clara desde el primer día.

## 1. Crear el proyecto en IntelliJ

1. `File → New → Project`.
2. En la lista de la izquierda eliges **Spring Boot** (si usas Community, la opción es *Spring Initializr*; si no aparece, entra a `start.spring.io` en el navegador, generas el ZIP y lo abres con IntelliJ).
3. Completa los campos:

| Campo | Valor usado en el proyecto de práctica |
|---|---|
| Name | `PruebaFitclub` |
| Language | Java |
| Type | **Maven** |
| Group | `com.fitclub` |
| Artifact | `PruebaFitclub` |
| Package name | `com.fitclub` |
| JDK / Java | **21** |
| Packaging | Jar |

4. En la siguiente pantalla eliges las dependencias. Marca:
   - **Spring Web** (o *Spring Web MVC*, según la versión del Initializr)
   - **Validation**
   - **Spring Data JPA**
   - **PostgreSQL Driver**
5. Create. IntelliJ descarga las dependencias — espera a que termine la barra de progreso de abajo antes de tocar nada.

## 2. Cómo quedó el `pom.xml`

El `pom.xml` es el archivo que le dice a Maven qué librerías necesita tu proyecto y con qué versión. Este es el del proyecto de práctica, limpio de comentarios:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.1</version>
    <relativePath/>
</parent>

<groupId>com.fitclub</groupId>
<artifactId>PruebaFitclub</artifactId>
<version>0.0.1-SNAPSHOT</version>

<properties>
    <java.version>21</java.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Qué hace cada una, por si te lo preguntan:

| Dependencia | Para qué sirve |
|---|---|
| `spring-boot-starter-webmvc` | Spring MVC + Tomcat embebido + Jackson (el que convierte JSON ↔ Java). Es lo que hace que existan `@RestController` y compañía. |
| `spring-boot-starter-validation` | Jakarta Validation: `@NotBlank`, `@Email`, `@Pattern`, `@Positive`. Sin esto, `@Valid` no hace nada. |
| `spring-boot-starter-data-jpa` | JPA + Hibernate + Spring Data. Lo que permite `JpaRepository` y `@Entity`. |
| `postgresql` | El driver JDBC. Con `runtime` porque tu código no lo importa nunca; solo lo necesita la máquina virtual al conectarse. |
| `spring-boot-starter-webmvc-test` | Librerías de prueba (JUnit, MockMvc). Con `test` porque no viaja al empaquetado final. |

Dos detalles que conviene notar. El primero: **no hay número de versión en las dependencias de Spring**. Eso lo resuelve el `<parent>`: el `spring-boot-starter-parent` trae una lista de versiones compatibles entre sí, y tú solo declaras el nombre. Por eso `springdoc`, que no es de Spring, sí lleva versión explícita (ver `09-Swagger-OpenAPI.md`).

El segundo: en Spring Boot 4 el starter se llama `spring-boot-starter-webmvc`. En Spring Boot 3 y anteriores se llamaba `spring-boot-starter-web`. Si copias código de un tutorial viejo y no resuelve la dependencia, es probablemente por eso.

## 3. La clase principal

IntelliJ la genera sola. Debe quedar así y **no debe crecer nunca**:

```java
package com.fitclub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PruebaFitclubApplication {

    public static void main(String[] args) {
        SpringApplication.run(PruebaFitclubApplication.class, args);
    }
}
```

`@SpringBootApplication` son en realidad tres anotaciones en una:

- `@Configuration`: declara que esta clase puede definir beans.
- `@EnableAutoConfiguration`: activa la autoconfiguración según lo que encuentre en el classpath (si ve el driver de PostgreSQL y una URL de datasource, configura la conexión solo).
- `@ComponentScan`: escanea **el paquete de esta clase y todos los de abajo** buscando `@RestController`, `@Service`, `@Component`, `@Repository`.

Ese último punto explica por qué toda tu aplicación tiene que vivir dentro de `com.fitclub`. Si creas una clase en `com.otracosa`, Spring no la va a ver y te va a dar un error de bean no encontrado que parece inexplicable.

Lo que NO debe estar en esta clase: endpoints, reglas de negocio, consultas, validaciones. Es una puerta de arranque técnica, nada más.

## 4. `application.properties`

Vive en `src/main/resources/application.properties`. Es el archivo de configuración externa: cambias comportamiento sin recompilar. Este es el del proyecto de práctica, ya completo:

```properties
spring.application.name=PruebaFitclub

spring.datasource.url=jdbc:postgresql://localhost:5432/pruebafitclub
spring.datasource.username=pruebafitclub_admin
spring.datasource.password=fitclub123

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.default_schema=fitclub
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

logging.level.org.hibernate.SQL=debug

server.port=8080
```

Línea por línea:

| Propiedad | Qué hace |
|---|---|
| `spring.datasource.url` | Dónde está la base: protocolo JDBC, host, puerto y **nombre de la base de datos**. Fíjate que aquí NO va el schema. |
| `spring.datasource.username` / `.password` | Credenciales del rol de PostgreSQL. |
| `spring.jpa.hibernate.ddl-auto=validate` | Hibernate compara tus `@Entity` contra las tablas reales y **falla el arranque si no coinciden**. No crea ni modifica nada. |
| `spring.jpa.properties.hibernate.default_schema=fitclub` | El schema por defecto. Sin esto, Hibernate buscaría las tablas en `public` y no las encontraría. |
| `format_sql` / `show-sql` / `logging.level...SQL=debug` | Hacen que el SQL real aparezca formateado en la consola. Es tu evidencia de que algo se guardó de verdad. |
| `spring.jpa.open-in-view=false` | Cierra la sesión de persistencia al salir del caso de uso, en vez de mantenerla abierta hasta que se renderiza la respuesta. Evita consultas fantasma y fugas de la capa de datos hacia la web. |
| `server.port=8080` | El puerto. Si ya tienes algo ocupándolo, cámbialo aquí. |

Una advertencia que el profesor repite: **la contraseña no debe versionarse en Git** en un proyecto real. En un proyecto de práctica local es tolerable, pero si te preguntan, la respuesta correcta es que va en variables de entorno o en un `application-local.properties` ignorado por Git.

## 5. Primer arranque

Presiona el botón verde de play sobre la clase principal (o `Shift+F10`). En la consola debes ver algo así:

```
Tomcat started on port 8080 (http) with context path '/'
Started PruebaFitclubApplication in 6.86 seconds
```

Si en cambio ves un error de conexión a PostgreSQL, es normal en este punto: todavía no has creado la base. Eso lo resuelve la guía `02-PostgreSQL-Base-Usuario-Schema.md`. Si quieres arrancar antes de tener base, comenta temporalmente las cuatro líneas de `spring.datasource` y `ddl-auto`, y quita la dependencia de `data-jpa`… pero es más simple hacer la guía 02 primero y volver.

## 6. Qué NO debes hacer todavía

- No crees `@Entity` ni repositorios en este punto: eso es Capítulo 05.
- No pongas todas las clases sueltas en `com.fitclub`. La estructura por módulos empieza en la guía 03.
- No cambies la versión de Spring Boot del `<parent>` "porque salió una nueva". Las versiones de todo lo demás dependen de esa.

## 7. Antes de dar por listo este paso

- [ ] El proyecto compila sin errores (`mvn clean compile` o el botón de build).
- [ ] La clase principal está en `com.fitclub` y solo tiene el `main`.
- [ ] `application.properties` existe y tiene los datos de conexión.
- [ ] Puedes explicar qué hacen las tres anotaciones dentro de `@SpringBootApplication`.
- [ ] Puedes explicar por qué las dependencias de Spring no llevan `<version>`.

## 8. Subir el avance a Git

```bash
git checkout main
git pull
git checkout -b feature/proyecto-spring-boot
git add .
git commit -m "feat: crear proyecto Spring Boot con dependencias base"
git push -u origin feature/proyecto-spring-boot
```

## Actividad para practicar

En esta guía todavía no hay código de negocio que escribir, así que la actividad es de comprensión y preparación:

1. Abre tu `pom.xml` y, sin mirar la tabla de arriba, escribe en un papel para qué sirve cada una de las cinco dependencias. Después compara.
2. Cambia temporalmente `server.port` a `8081`, arranca la aplicación y confirma en la consola que el puerto cambió. Devuélvelo a `8080`.
3. Cambia temporalmente `spring.jpa.show-sql` a `false`, arranca y observa que el SQL desaparece del log. Devuélvelo a `true` — lo vas a necesitar como evidencia en las guías siguientes.
4. Responde por escrito, con tus palabras: *si Spring escanea "el paquete de la clase principal y los de abajo", ¿qué pasaría si pongo un `@RestController` en un paquete llamado `com.otracosa.web`?*
5. Prepara el terreno para la actividad de las próximas guías: decide ya qué atributos va a tener tu `Entrenador` y tu `Rutina`, y escríbelos. Los de referencia están en `00-Indice-Cap-04-08.md`, pero conviene que los tengas a mano en tu propio cuaderno.
