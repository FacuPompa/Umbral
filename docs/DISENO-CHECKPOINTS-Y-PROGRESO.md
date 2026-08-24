# Diseño canónico: checkpoints y progreso persistente

**Estado:** aprobado para implementación; todavía no implementado.

Este documento define el corte durable de la tarea 1 de
`docs/ROADMAP.md`. Reemplaza los checkpoints fabricados en memoria y la
selección temporal de React por una única fuente de verdad en PostgreSQL.

## Objetivo y límites

La persona puede elegir el punto narrativo alcanzado en Persona 5 Royal. La
selección se guarda, se recupera al volver a abrir la aplicación y será la base
de la futura regla anti-spoilers.

Este corte incluye catálogo, checkpoints, un usuario demostración persistido y
su progreso por juego. No incluye login, Spring Security, registro, entradas de
bitácora, comentarios, reacciones ni filtrado anti-spoilers de publicaciones.

## Casos de pantalla que sostienen el modelo

1. La persona abre el catálogo y ve Persona 5 Royal.
2. Presiona “Ver checkpoints” y recibe los checkpoints seguros y ordenados de
   ese juego.
3. Elige uno. La aplicación guarda el cambio para el usuario demostración.
4. Recarga la página y ve el checkpoint guardado.
5. Si cambia de opinión, puede seleccionar otro checkpoint del mismo juego. El
   progreso representa el límite que declara seguro para leer, por lo que se
   permite corregirlo hacia adelante o atrás.

## Modelo de dominio y persistencia

```text
app_users 1 ── N user_game_progress N ── 1 games
                                  │
                                  N
                                  │
                                  1
                             checkpoints N ── 1 games
```

Las relaciones JPA serán unidireccionales desde las entidades que poseen las
claves foráneas. No se agregan colecciones en `Game` o `User` todavía: ninguna
pantalla actual las necesita y evitamos cargas o serializaciones accidentales.

### `Game` → tabla `games`

Se conserva como entidad del catálogo. Se refuerzan sus columnas para que
`title` sea obligatorio y único, y `description` sea obligatoria. No almacena
colecciones de checkpoints ni de progresos.

### `Checkpoint` → tabla `checkpoints`

Representa un punto narrativo seguro, ordenado y perteneciente a un único juego.

| Campo | Regla |
| --- | --- |
| `id` | Clave primaria `Long` generada por PostgreSQL. |
| `game_id` | Obligatorio; FK a `games`. Relación `@ManyToOne` hacia `Game`. |
| `label` | Obligatorio; texto seguro para mostrar en la interfaz. |
| `position` | Obligatorio y positivo; define el orden narrativo dentro del juego. |

Restricción única: `(game_id, position)`. Dos checkpoints del mismo juego no
pueden ocupar la misma posición. El repositorio los lee ordenados de forma
ascendente por `position`.

### `User` temporal → tabla `app_users`

Es una cuenta de dominio mínima, no una implementación adelantada de
autenticación.

| Campo | Regla |
| --- | --- |
| `id` | Clave primaria `Long`. El usuario demostración usa el valor fijo `1`. |
| `handle` | Obligatorio y único; identifica al usuario demostración. |

No se agregan email, contraseña, roles, tokens ni dependencias de Spring
Security en este corte.

### `UserGameProgress` → tabla `user_game_progress`

Representa una sola declaración de progreso para un usuario y un juego.

| Campo | Regla |
| --- | --- |
| `id` | Clave primaria `Long` generada por PostgreSQL. |
| `user_id` | Obligatorio; FK a `app_users`. Relación `@ManyToOne` hacia `User`. |
| `game_id` | Obligatorio; FK a `games`. Relación `@ManyToOne` hacia `Game`. |
| `checkpoint_id` | Obligatorio; referencia el checkpoint elegido. |

Restricción única: `(user_id, game_id)`. No puede haber dos progresos del mismo
juego para la misma persona.

La invariante central es que `checkpoint_id` debe pertenecer a `game_id`. Se
protege en dos niveles:

1. El servicio rechaza una selección cuyo checkpoint no pertenezca al juego de
   la URL.
2. La base debe tener una FK compuesta
   `(checkpoint_id, game_id) → checkpoints(id, game_id)`. Para permitirla,
   `checkpoints` declara también la unicidad técnica `(id, game_id)`.

La FK compuesta hace imposible persistir por SQL un progreso que combine un
juego con un checkpoint de otro juego. Es una garantía necesaria para que una
futura regla anti-spoilers no dependa solo de código de aplicación.

### Decisión de mapeo JPA para la FK compuesta

`UserGameProgress` comparte la columna `game_id` entre su relación a `Game` y
su relación compuesta a `Checkpoint`. Para que Hibernate no intente escribir la
misma columna dos veces, la relación a `Checkpoint` será la propietaria
escribible de `(checkpoint_id, game_id)`.

La relación a `Game` reutiliza `game_id` solo para lectura, con sus operaciones
de insert y update desactivadas. De este modo:

- el servicio valida primero que el checkpoint pertenece al `gameId` de la URL;
- al guardar, se asignan usuario y checkpoint; el checkpoint aporta también el
  valor de `game_id`;
- la relación a `Game` permite leer el juego del progreso y expresar la
  unicidad `(user_id, game_id)`, sin competir por la escritura de la columna.

No se modela una clave primaria compuesta para `UserGameProgress`: mantiene su
id propio y la unicidad de negocio permanece como restricción de tabla.

## Carga inicial con `data.sql`

`data.sql` es la única estrategia de carga inicial. No se usará
`CommandLineRunner` ni datos alternativos en memoria.

En cada inicio de desarrollo, el script debe:

1. Insertar Persona 5 Royal de manera idempotente.
2. Insertar sus checkpoints seguros, obteniendo el `game_id` mediante el título
   del juego y evitando duplicados por `(game_id, position)`.
3. Insertar el usuario demostración con `id = 1` y un `handle` estable, por
   ejemplo `umbral-demo`.
4. Ajustar la secuencia de `app_users` después de la inserción explícita del id
   `1`, para que futuros usuarios no colisionen con esa clave.
5. No insertar un progreso inicial: la primera selección desde React debe
   demostrar que el guardado funciona.

Se conservan `spring.jpa.defer-datasource-initialization=true` y
`spring.sql.init.mode=always`. En este entorno local, Hibernate crea las tablas
nuevas con `ddl-auto=update`; antes de desplegar se migrará a un esquema
versionado y producción usará validación, no generación automática.

## Contrato HTTP

Los IDs de usuario nunca salen desde React ni se aceptan en el body. El prefijo
`/api/me` representa al usuario del contexto actual: hoy el usuario demostración
`id = 1`; en el futuro, el usuario autenticado.

### Endpoints que se conservan

| Endpoint | Comportamiento |
| --- | --- |
| `GET /api/games` | Se conserva sin cambios. Devuelve el catálogo persistido. |
| `GET /api/games/{gameId}/checkpoints` | Conserva `CheckpointResponse(id, label, position)`, pero ahora consulta PostgreSQL y ordena por posición. Un juego inexistente responde `404`. |

### Endpoints nuevos

#### `GET /api/me/game-progress`

Devuelve todos los progresos guardados del usuario actual. Si todavía no eligió
ningún checkpoint, devuelve una lista vacía.

```json
[
  {
    "gameId": 1,
    "checkpointId": 2,
    "checkpointLabel": "Progreso intermedio",
    "position": 2
  }
]
```

#### `PUT /api/me/games/{gameId}/progress`

Crea el progreso si no existe o lo reemplaza si ya existe. Es `PUT` porque el
recurso está identificado por usuario actual y juego; repetir la misma solicitud
deja el mismo estado.

```json
{
  "checkpointId": 2
}
```

Responde `200 OK` con el mismo contrato de un elemento de progreso. Errores:

| Estado | Situación |
| --- | --- |
| `400 Bad Request` | Body ausente o `checkpointId` inválido. |
| `404 Not Found` | El juego o checkpoint solicitado no existe. |
| `409 Conflict` | El checkpoint existe, pero pertenece a otro juego. |

Para validar el request se agrega el starter de validación y se usa un DTO de
entrada separado de las entidades.

## Cambios en React

`GameCatalogPage` deja de tratar `selectedCheckpoint` como fuente de verdad.

1. Al iniciar, carga catálogo y progresos guardados.
2. Conserva los progresos recibidos en un mapa indexado por `gameId`; ese estado
   solo refleja respuestas exitosas de la API.
3. Al elegir un checkpoint, llama al `PUT` y actualiza la interfaz recién cuando
   la API confirma el guardado.
4. Mientras guarda, deshabilita la nueva selección y muestra un error si la
   solicitud falla.
5. Al recargar, el `GET /api/me/game-progress` restaura la selección persistida.

`gameApi.js` conserva `fetchGames` y `fetchCheckpoints`, y suma funciones para
leer y guardar progreso. No se agrega Axios, estado global ni React Router.

## Archivos afectados al implementar

| Acción | Archivos |
| --- | --- |
| Crear | Entidades `Checkpoint`, `User` y `UserGameProgress`; sus repositories; DTO de request y DTO de respuesta de progreso; servicio de progreso y resolvedor del usuario temporal; controller de progreso; excepciones y manejo HTTP mínimo para `404` y `409`; pruebas del corte. |
| Modificar | `Game`, `GameRepository`, `GameCatalogService`, `GameController`, `data.sql`, `application.properties` si hiciera falta para el esquema, `build.gradle`, `GameCatalogPage.jsx` y `gameApi.js`. |
| Conservar | `GameResponse`, `CheckpointResponse`, `GET /api/games`, proxy de Vite, PostgreSQL de Docker Compose y la estructura controller → service → repository actual. |
| Reemplazar | Checkpoints creados a mano y la condición fija `gameId == 1` en `GameCatalogService`; selección `selectedCheckpoint` volátil de React; respuesta vacía para un juego inexistente. |

Los nombres de clases de excepción o del resolvedor temporal podrán ajustarse al
estilo que ya tenga Umbral, pero sus responsabilidades y los contratos HTTP de
este documento no cambian.

## Pruebas y verificación

### Backend

- Repository: checkpoints de un juego ordenados por posición; unicidad de
  posición; un único progreso por usuario y juego; e imposibilidad de combinar
  un checkpoint con otro juego.
- Servicio: crea progreso, lo reemplaza y rechaza un checkpoint de otro juego.
- HTTP: lista de checkpoints, lista vacía de progreso, creación y actualización
  por `PUT`, y respuestas `400`, `404` y `409`.

### Entorno de pruebas decidido

Los tests de persistencia e integración usarán PostgreSQL mediante
Testcontainers. Docker ya forma parte del entorno local y esta alternativa
ejecuta los tests contra el mismo motor que producción, con una base aislada y
reproducible.

| Capa | Estrategia |
| --- | --- |
| Repository | `@DataJpaTest` con PostgreSQL de Testcontainers; verifica orden, restricciones y FKs reales. |
| Servicio | Tests de regla de negocio: alta, reemplazo y rechazo de checkpoint ajeno. |
| HTTP | Tests de endpoints y contratos, incluidos `400`, `404` y `409`. |

No se usa la base local de desarrollo, un perfil test conectado a ella ni H2:
no aíslan los datos o no reproducen con fidelidad PostgreSQL, `ON CONFLICT`,
secuencias y la FK compuesta. Las dependencias de test necesarias se agregan
junto con la implementación, incluyendo el soporte de Spring Boot para
Testcontainers, JUnit y PostgreSQL.

### Verificación manual

1. Iniciar PostgreSQL, backend y frontend.
2. En DBeaver, comprobar las tablas, claves foráneas, restricciones y carga
   idempotente de catálogo, checkpoints y usuario demostración.
3. Elegir un checkpoint en React.
4. Confirmar una sola fila en `user_game_progress`.
5. Recargar la página y comprobar que aparece el mismo progreso.
6. Elegir otro checkpoint y confirmar que actualiza la fila existente en vez de
   crear una segunda.

## Estrategia futura de login

`User` se conserva como entidad de perfil y propietario de los progresos. Al
incorporar autenticación se agregan credenciales y Spring Security en ese corte,
sin cambiar la relación de `UserGameProgress` ni las rutas basadas en `/api/me`.

El resolvedor temporal que devuelve el usuario `id = 1` se sustituirá por el
principal autenticado. La cuenta demo podrá mantenerse para una demostración o
completarse/migrarse como cuenta real; esa decisión pertenece al diseño de
registro, no a este corte.

## Evidencia consultada

- Estado actual: entidades, servicio, controller, DTOs, `data.sql`, Gradle y
  React bajo `backend/` y `frontend/`.
- Referencia: `FacuPompa/java-spring`, especialmente `MovieEntity`, el repository
  CRUD adaptado y su `data.sql` idempotente.
- Notas de Obsidian: entidades JPA, repositories CRUD, endpoints POST y PUT, y
  validaciones automáticas.

## Decisiones pendientes reales

No hay decisiones funcionales ni de diseño pendientes para iniciar la
implementación. Durante la primera ejecución se verifica en DBeaver que
Hibernate haya materializado la FK compuesta. Si `ddl-auto=update` no la aplica
de forma confiable sobre la base existente, se incorpora una migración de
esquema mínima para esa restricción; no se degrada la invariante a una validación
solo en el servicio.
