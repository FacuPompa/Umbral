# Umbral

Proyecto para hablar de juegos narrativos sin comerse spoilers.

La idea es que cada juego tenga checkpoints de historia. Antes de leer una
entrada, cada persona indica hasta dónde llegó; con eso, más adelante, el feed
podrá esconder lo que todavía no debería mostrar.

Por ahora el catálogo de prueba es **Persona 5 Royal**. En vez de usar un
porcentaje, el progreso se guarda por tramos de la historia, como los
palacios.

## Qué tiene hasta ahora

- Catálogo y checkpoints guardados en PostgreSQL.
- Un usuario demo con un progreso por juego.
- El progreso se crea o actualiza al elegir un checkpoint.
- La pantalla de React restaura el progreso al recargar.
- Validaciones para no guardar un checkpoint de otro juego.
- Datos iniciales cargados con `data.sql`.
- Tests de repository, service y controller con PostgreSQL temporal
  (Testcontainers).
- GitHub Actions corre los tests del backend en cada push y pull request.

Todavía no hay login: las rutas `/api/me/...` usan un usuario demo mientras se
diseña la parte de identidad.

## Stack

| Parte | Tecnologías |
| --- | --- |
| Backend | Java 26, Spring Boot, Gradle, Spring Data JPA |
| Base de datos | PostgreSQL 17 + Docker Compose |
| Frontend | React, Vite, JavaScript |
| Tests | JUnit, MockMvc, Testcontainers |
| CI | GitHub Actions |

## Endpoints actuales

| Método | Ruta | Uso |
| --- | --- | --- |
| `GET` | `/api/games` | Lista el catálogo. |
| `GET` | `/api/games/{gameId}/checkpoints` | Lista los checkpoints del juego. |
| `GET` | `/api/me/game-progress` | Consulta el progreso del usuario demo. |
| `PUT` | `/api/me/games/{gameId}/progress` | Guarda el checkpoint alcanzado. |

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

## Lo próximo

- Diseñar una entrada de bitácora asociada a un juego y checkpoint.
- Usar el progreso de quien lee para filtrar spoilers.
- Reemplazar el usuario demo por identidad real.
