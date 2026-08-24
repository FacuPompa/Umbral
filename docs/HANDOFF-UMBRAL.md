# Handoff de continuidad: Umbral

Leer primero `docs/CONTEXTO-UMBRAL.md` y `docs/AGENTS.md`. Este archivo refleja el estado al 20 de agosto de 2026.

## Forma de trabajo obligatoria

Umbral sigue siendo un proyecto de aprendizaje, pero no debe acumular pasos didácticos descartables. Antes de diseñar o implementar una feature, cambio de persistencia, entidad, endpoint o relación:

1. Revisar los archivos relevantes de `https://github.com/FacuPompa/java-spring`.
2. Buscar y leer solo las notas pertinentes de Obsidian.
3. Diseñar y explicar la solución durable para Umbral: datos, restricciones, archivos, carga inicial y prueba observable.
4. Obtener autorización explícita antes de editar archivos funcionales.

No usar una solución temporal si la solución objetivo ya se puede decidir. Un `spike pedagógico` requiere aprobación expresa y debe declarar qué se eliminará después.

La coordinación con Codex se rige además por `docs/FLUJO-DE-AGENTES.md`: un
agente principal por recorrido y subagentes de solo lectura solo cuando su tarea
sea independiente.

## Producto y límites del MVP

Umbral es una comunidad mínima para compartir análisis de videojuegos narrativos sin spoilers. La regla futura es:

```text
checkpoint de una publicación <= progreso del lector
```

El catálogo inicial contiene solo **Persona 5 Royal**. La historia principal se considera lineal para el MVP; confidentes, contenido opcional, rutas y finales alternativos quedan fuera. No implementar todavía login, comentarios, reacciones ni la barrera anti-spoilers real.

## Estado técnico actual

### Entorno local

- Java 26, Spring Boot 4.0.7, Gradle Groovy.
- React con Vite y JavaScript.
- PostgreSQL 17.10 corre en Docker mediante `docker-compose.yml` en la raíz.
- DBeaver conecta a `localhost:5432`, base `umbral`, usuario `umbral`, contraseña local `umbral_local`.
- El contenedor se llama `umbral-postgres` y usa un volumen persistente.

### Backend

La aplicación usa `spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa` y el driver PostgreSQL.

- `application.properties` configura el datasource local, `spring.jpa.hibernate.ddl-auto=update`, `spring.jpa.defer-datasource-initialization=true` y `spring.sql.init.mode=always`.
- `Game` es entidad JPA en `domain/entity`, mapeada a la tabla `games`; PostgreSQL genera su `id`.
- `GameRepository` extiende `JpaRepository<Game, Long>`.
- `data.sql` carga Persona 5 Royal de forma idempotente usando `WHERE NOT EXISTS`. No usar a la vez `CommandLineRunner` para el mismo catálogo.
- `GameCatalogService#getAllGames()` ya consulta `GameRepository` y transforma entidades a `GameResponse`.
- `GET /api/games` debe devolver la fila de PostgreSQL.
- `GET /api/games/{gameId}/checkpoints` y `CheckpointResponse` existen, pero sus tres checkpoints siguen en memoria. La selección de progreso no se persiste aún.

### Frontend

`GameCatalogPage` lista juegos, permite pedir checkpoints y seleccionar uno temporalmente en el estado de React. `gameApi.js` contiene `fetchGames()` y `fetchCheckpoints(gameId)`; Vite redirige `/api` a Spring.

## Próximo corte durable: checkpoints y progreso

El diseño de este corte fue revisado y aprobado; su implementación está en
curso. Ya existen las entidades JPA `Checkpoint`, `User` y
`UserGameProgress`, junto con `CheckpointRepository`; Hibernate creó sus
tablas correctamente en PostgreSQL. Aún no hay carga inicial de checkpoints o
usuario demo, ni lectura/guardado de progreso por HTTP.
Su planificación detallada vive en `docs/ROADMAP.md`. El alcance aprobado es:

```text
Game 1 --- N Checkpoint
Usuario temporal fijo (id 1) --- UserGameProgress --- Game
                                           |
                                           └--- checkpoint alcanzado
```

Antes de editar archivos funcionales, retomar el diseño aprobado del roadmap,
explicar el propósito de cada archivo a crear o modificar y confirmar el alcance
de implementación. El corte debe implementarse entero y de forma coherente.

## Ejecución

- Backend: iniciar `UmbralApplication` desde IntelliJ con su configuración de ejecución.
- Frontend: desde `frontend/`, `npm run dev`.
- Base: `docker compose up -d` desde la raíz; DBeaver es el cliente de inspección.

Evitar `./gradlew` hasta restaurar su permiso de ejecución; puede usarse la tarea Gradle desde IntelliJ.
