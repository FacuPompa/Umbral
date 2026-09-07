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
- Registro e inicio de sesión con sesiones HTTP seguras, contraseñas hasheadas
  con BCrypt y protección CSRF.
- El detalle de cada juego y todas las acciones personales requieren una sesión;
  el catálogo y los checkpoints siguen siendo públicos.
- Roles iniciales `MEMBER` y `MODERATOR`. La primera cuenta cuyo email coincida
  con una variable local se convierte en moderadora; el rol nunca viene desde
  el formulario.
- Catálogo, checkpoints y entradas de bitácora guardados en PostgreSQL.
- Usuarios demo históricos para poblar las conversaciones de ejemplo. No son
  cuentas de acceso.
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

## Stack

| Parte | Tecnologías |
| --- | --- |
| Backend | Java 26, Spring Boot, Spring Security, Gradle, Spring Data JPA, Flyway |
| Base de datos | PostgreSQL 17 + Docker Compose |
| Frontend | React, React Router, Vite, JavaScript |
| Tests | JUnit, MockMvc, Testcontainers |
| CI | GitHub Actions |

## Endpoints actuales

| Método | Ruta | Uso |
| --- | --- | --- |
| `GET` | `/api/games` | Lista el catálogo. |
| `GET` | `/api/games/{gameId}/checkpoints` | Lista los checkpoints del juego. |
| `GET` | `/api/auth/csrf` | Entrega el token necesario para las operaciones que escriben datos. |
| `POST` | `/api/auth/register` | Crea una cuenta. |
| `POST` | `/api/auth/login` | Inicia una sesión HTTP. |
| `GET` | `/api/auth/me` | Devuelve la cuenta de la sesión actual. |
| `POST` | `/api/auth/logout` | Cierra la sesión actual. |
| `GET` | `/api/me/game-progress` | Consulta el progreso de la persona autenticada. |
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
del juego de prueba después de iniciar sesión.

### Primera cuenta moderadora local

Antes de registrar la primera cuenta, elegí el email que será moderador y
exportalo en la terminal con la que iniciás el backend:

```bash
export UMBRAL_BOOTSTRAP_MODERATOR_EMAIL="tu-email@ejemplo.com"
```

La primera cuenta registrada con ese email obtiene el rol `MODERATOR`; las
demás nacen como `MEMBER`. Esta variable no se versiona y evita publicar un
email personal en el repositorio. Para la demo actual, el rol todavía no abre
un panel administrativo: prepara la regla para aprobar juegos y checkpoints en
el siguiente corte.

Para verificar el frontend antes de abrir una Pull Request:

```bash
cd frontend
npm run check
```

## Lo próximo

- Diseñar perfiles y un espacio personal separado de la landing pública.
- Diseñar el flujo de sugerencias de juegos y su aprobación por moderación.
- Evaluar respuestas anidadas, menciones y moderación para los hilos.
- Incorporar búsqueda de juegos y usuarios, respetando la barrera anti-spoilers.
