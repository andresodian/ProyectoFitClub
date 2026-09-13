# FitClub

Sistema de gestión para un gimnasio/club deportivo — Proyecto PA-08, Programación Aplicada 2026-2.

Permite administrar socios, sus membresías y planes, las clases y sus horarios, reservas, asistencia y notificaciones.

## Equipo

- Joshua Villagomez
- Leonardo Aguilera
- Andres Odian
- Diego Merrys
- Valeria Olmos
- Valeria Chavez

## Estructura del repositorio

```
Fitclub/
├── backend/              # Código Java del backend
├── Pasos y guías/         # Guías del equipo: Git, GitHub, DataGrip, IntelliJ, Clase, Enum, Interfaz, Main
├── documentosclases/      # Material del curso (guías del docente, capítulos, etc.)
└── docs/                  # Visión, requisitos y decisiones del proyecto
```

## ¿Nuevo en el equipo?

Antes de tocar código, revisa la carpeta **`Pasos y guías/`** — ahí está explicado paso a paso cómo configurar Git, GitHub, DataGrip e IntelliJ, y cómo se arma una clase, un enum, una interfaz de repositorio y un `Main` de prueba, con ejemplos reales de este proyecto.

## Stack técnico

- Java 21
- Maven
- PostgreSQL (base de datos `fitclubsc`)
- (Spring Boot llega más adelante, en los próximos capítulos del curso)

## Módulos del dominio

| Módulo | Contiene |
|---|---|
| `socio` | Socio, Notificación, y el patrón de repositorio (interfaz + implementación en memoria + servicio) |
| `plan` | Plan, Membresía, Historial de membresía |
| `clase` | Clase, Instructor, Horario de clase, Reserva de clase, Asistencia |

## Estado actual

- **Capítulo 01** — modelo de dominio en Java puro: las 10 entidades del proyecto, con atributos `private`, constructores y encapsulamiento.
- **Capítulo 02** — enums para los 9 campos de estado/tipo cerrados (tomados de los `CHECK` reales de PostgreSQL), y el patrón de contrato de repositorio (interfaz + implementación en memoria + servicio + excepciones propias) aplicado a Socio → Membresía.
- Próximo: Capítulo 03 en adelante (Spring Boot, conexión real a PostgreSQL).
