## Capítulo 02 - Java 21

### Entidad padre
Socio (PK: `socio_id`). Es la entidad que existe de forma independiente: un socio se registra en el sistema sin necesitar que exista ninguna membresía todavía.

### Entidad dependiente
Membresía (FK: `socio_id` → `socio.socio_id`). Una membresía no puede existir sin un socio al que pertenezca.

### Relación
1:N porque un socio puede tener varias membresías a lo largo del tiempo (por ejemplo, si su membresía se vence y compra un plan nuevo más adelante, eso genera otra fila en `membresia`), pero cada membresía pertenece exactamente a un socio.

### Colección elegida
Usamos `Map<Long, Socio>` en `SocioRepositoryEnMemoria` porque necesitamos poder buscar un socio por su id de forma directa (`buscarPorId`), y un Map asocia esa clave (el id) con el valor (el socio) sin tener que recorrer toda una lista comparando uno por uno.

### Optional
`buscarPorId(Long id)` devuelve `Optional<Socio>` porque buscar por id puede fallar (que no exista ningún socio con ese id), y `Optional` hace visible esa posibilidad en la firma del método en vez de arriesgar un `NullPointerException` si alguien olvida comprobar si el resultado es null.

### Excepciones propias
- `SocioNoEncontradoException`: se lanza en `SocioService.obtener(id)` cuando no existe ningún socio con ese id.
- `DocumentoDuplicadoException`: se lanza en `SocioService.registrar(socio)` cuando el número de documento del socio nuevo ya pertenece a otro socio registrado.

### Enum
`EstadoMembresia` (creado en la etapa anterior) representa el estado de la entidad dependiente Membresía: `ACTIVA`, `INACTIVA`, `SUSPENDIDA`, `VENCIDA`, `CANCELADA` — un conjunto cerrado, tomado directamente del CHECK real de la tabla `membresia` en PostgreSQL (`ck_membresia_estado`), no inventado.

### Evidencia
Salida real de `Main.java` al ejecutarlo:

```
=== FITCLUB SISTEMA DE GESTION ===
Socios registrados: 2
Socio obtenido por id (1): Carlos Gómez
ERROR CONTROLADO: No existe el socio con id: 999
ERROR CONTROLADO: Ya existe un socio registrado con el número de documento: 12345678
Plan adquirido: Plan Anual VIP
Total de membresías del socio: 1
Estado de la membresía: ACTIVA
...
```

### Nota para la defensa
El campo único de Socio que usamos (`numeroDocumento`) todavía no lo confirmamos como restricción `UNIQUE` en PostgreSQL — lo elegimos porque conceptualmente dos personas no comparten el mismo número de documento. Si en la tabla real `socio` no existe todavía esa restricción `UNIQUE`, es buena idea agregarla más adelante para que la base también la exija (no solo el código Java).
