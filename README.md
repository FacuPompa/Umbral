# Umbral

Proyecto para hablar de juegos narrativos sin comerse spoilers.

Cada juego se divide en checkpoints de historia. Antes de leer o publicar una
entrada, cada persona indica hasta dónde llegó. El backend usa ese avance para
devolver solamente conversaciones que ya son seguras para esa persona.

Por ahora el catálogo de prueba es **Persona 5 Royal**. En vez de usar un
porcentaje, el progreso se guarda por tramos de la historia, como los
palacios.

## Qué tiene hasta ahora

- Landing pública que explica la idea de Umbral y carga el catálogo real.
- Tema claro/oscuro que recuerda la elección en el navegador.
- Catálogo, checkpoints y entradas de bitácora guardados en PostgreSQL.
- Un usuario demo con progreso por juego y entradas de ejemplo.
- El progreso se crea o actualiza al elegir un checkpoint y se restaura al
  recargar React.
- Se pueden publicar entradas de texto solamente hasta el checkpoint alcanzado.
- Cada entrada se clasifica como reflexión, duda, teoría o reseña.
- Las entradas pueden recibir respuestas directas para formar hilos breves.
- Las respuestas heredan el contexto anti-spoilers de su entrada padre: si una
  entrada no es segura, su hilo tampoco se devuelve.
- El feed de cada juego se filtra en Spring según el progreso del lector, para
  no devolver spoilers que React solo tendría que ocultar visualmente.
- Validaciones para no guardar un checkpoint de otro juego ni publicar más allá
  del avance actual.
- Datos iniciales cargados con `data.sql`.
- Esquema PostgreSQL versionado con Flyway.
- Tests de repository, service y controller con PostgreSQL temporal
  (Testcontainers).
- GitHub Actions corre los tests del backend en cada push y pull request.

Todavía no hay login: las rutas `/api/me/...` usan un usuario demo mientras se
diseña la parte de identidad.

## Stack

| Parte | Tecnologías |
| --- | --- |
| Backend | Java 26, Spring Boot, Gradle, Spring Data JPA, Flyway |
| Base de datos | PostgreSQL 17 + Docker Compose |
| Frontend | React, React Router, Vite, JavaScript |
| Tests | JUnit, MockMvc, Testcontainers |
| CI | GitHub Actions |

## Endpoints actuales

| Método | Ruta | Uso |
| --- | --- | --- |
| `GET` | `/api/games` | Lista el catálogo. |
| `GET` | `/api/games/{gameId}/checkpoints` | Lista los checkpoints del juego. |
| `GET` | `/api/me/game-progress` | Consulta el progreso del usuario demo. |
| `PUT` | `/api/me/games/{gameId}/progress` | Guarda el checkpoint alcanzado. |
| `GET` | `/api/games/{gameId}/journal-entries` | Lista solo las entradas seguras para el progreso actual. |
| `POST` | `/api/me/journal-entries` | Publica una entrada en un checkpoint ya alcanzado. |
| `GET` | `/api/journal-entries/{entryId}/replies` | Lista las respuestas de una entrada visible. |
| `POST` | `/api/me/journal-entries/{entryId}/replies` | Publica una respuesta en una entrada visible. |

## Levantarlo localmente

Primero levantá PostgreSQL desde la raíz:

```bash
docker compose up -d
```

Después iniciá `UmbralApplication` desde IntelliJ. Para el frontend:

```bash
cd frontend
npm install
npm run dev
```

El frontend queda en `http://localhost:5173` y el backend usa el puerto `8080`.
La ruta `/` muestra la presentación y el catálogo; `/games/1` abre el detalle
del juego de prueba.

Para verificar el frontend antes de abrir una Pull Request:

```bash
cd frontend
npm run check
```

## Lo próximo

- Reemplazar el usuario demo por identidad real.
- Diseñar perfiles y un espacio personal separado de la landing pública.
- Evaluar respuestas anidadas, menciones y moderación para los hilos.
- Incorporar búsqueda de juegos y usuarios, respetando la barrera anti-spoilers.
