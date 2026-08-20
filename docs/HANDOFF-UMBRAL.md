# Handoff de continuidad: Umbral

Leer primero `CONTEXTO-UMBRAL.md`. Este archivo agrega el estado real del proyecto al 19 de agosto de 2026.

## Objetivo y forma de trabajo

Umbral es una comunidad mínima para compartir análisis de videojuegos narrativos sin spoilers. La regla futura es:

```text
checkpoint de una publicación <= progreso del lector
```

El catálogo inicial contiene solo **Persona 5 Royal**. Para el MVP, la historia principal se trata como lineal; confidentes, contenido opcional, rutas y finales alternativos quedan fuera.

El usuario está aprendiendo full stack. Avanzar un recorrido vertical por vez, explicar el viaje de los datos y no entregar una aplicación terminada. El usuario quiere escribir el código propio; ayudar con objetivos, preguntas, pistas y revisión. Dar bloques de código solo cuando los pida expresamente. No crear archivos funcionales sin explicarlos antes.

## Estado actual: primer recorrido completo

El recorrido `GET /api/games` funciona de punta a punta:

```text
React en http://localhost:5173
  -> GET /api/games
  -> proxy de Vite
  -> Spring en http://localhost:8080
  -> GameController
  -> GameCatalogService
  -> lista temporal en memoria
  -> JSON
  -> React muestra Persona 5 Royal
```

No hay PostgreSQL, JPA, entidades, repositories, usuarios, checkpoints, login, comentarios ni reacciones todavía.

### Backend existente

```text
backend/src/main/java/com/umbral/
├── UmbralApplication.java
├── domain/
│   ├── dto/GameResponse.java
│   └── service/GameCatalogService.java
└── web/controller/GameController.java
```

- `GameResponse` es un `record` con `Long id`, `String title` y `String description`. Es DTO de respuesta, no entidad JPA.
- `GameCatalogService` tiene `@Service` y `getAllGames()`. Devuelve temporalmente una `ArrayList` con Persona 5 Royal y ID `1L`.
- `GameController` tiene `@RestController`, `@RequestMapping("/api/games")` y un `@GetMapping`. Recibe el service por constructor y devuelve `ResponseEntity<List<GameResponse>>` con `200 OK`.
- El backend usa Spring Boot 4.0.7, Gradle Groovy y Java 26 (`build.gradle`). La primera respuesta de Spring Initializr con la versión 4.1.0 falló en el generador, por eso se eligió 4.0.7.

### Frontend existente

```text
frontend/src/
├── main.jsx
├── App.jsx
└── features/games/
    ├── gameApi.js
    └── GameCatalogPage.jsx
```

- `main.jsx` es la entrada automática de React/Vite.
- `App.jsx` solo renderiza `GameCatalogPage`.
- `gameApi.js` concentra la petición con `fetchGames()` y la URL relativa `/api/games`.
- `GameCatalogPage.jsx` mantiene los estados `games`, `loading` y `error`; usa `useEffect` para pedir el catálogo y renderiza una tarjeta simple por juego.
- `vite.config.js` configura el proxy de desarrollo: `'/api'` se reenvía a `http://localhost:8080`.
- El frontend es React con JavaScript, no TypeScript. La primera generación de Vite fue incorrecta y se reemplazó por la plantilla React correcta.

## Ejecutar el proyecto en otra PC

Usar una ruta sin caracteres acentuados. Hubo un fallo real de Gradle con la ruta que contenía `Programación`; al renombrarla como `Programacion`, el build funcionó.

Prerequisitos esperados:

- JDK 26, porque el `toolchain` de Gradle solicita Java 26.
- Node.js y npm.
- Acceso a Internet la primera vez, para que Gradle y npm descarguen dependencias.

Backend, desde `backend/`:

```bash
./gradlew test
./gradlew bootRun
```

Comprobar en otra terminal:

```bash
curl http://localhost:8080/api/games
```

Frontend, desde `frontend/`:

```bash
npm install
npm run dev
```

Abrir la URL local que indique Vite, normalmente `http://localhost:5173`.

Mantener backend y frontend ejecutándose a la vez. El proxy de Vite evita CORS durante el desarrollo local; no agregar `@CrossOrigin` por ahora.

## Próximo recorrido recomendado

Antes de crear entidades o PostgreSQL, completar el segundo recorrido vertical de forma temporal en memoria:

```text
Seleccionar Persona 5 Royal en React
  -> GET /api/games/{id}/checkpoints
  -> Spring devuelve checkpoints seguros y ordenados
  -> React permite elegir el progreso alcanzado
```

Primero discutir y documentar ejemplos concretos de checkpoints que no revelen spoilers. Evitar nombres de arcos, personajes, eventos y también evitar mostrar el total de checkpoints.

Solo después de comprender ese recorrido, decidir qué datos necesitan `Game`, `Checkpoint` y `UserGameProgress`, y recién entonces introducir PostgreSQL, JPA, entidades y persistencia.

No implementar todavía la barrera anti-spoilers real: requiere progreso del lector y publicaciones, que pertenecen a un recorrido posterior.

## Notas de entorno

`docs/notas-de-entorno.md` conserva la evidencia del problema anterior de Gradle y ruta con `ó`.
