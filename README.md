# Umbral

Proyecto para hablar de juegos narrativos sin comerse spoilers.

**Estado actual:** La última release publicada es
[`v1.1.0`](https://github.com/FacuPompa/Umbral/releases/tag/v1.1.0). El flujo
principal está validado localmente; todavía no es una versión de producción ni
una aplicación social terminada.

**Demo en vivo:** [umbral-facupompa.netlify.app](https://umbral-facupompa.netlify.app)

Cada juego se divide en checkpoints de historia. Antes de leer o publicar una
entrada, cada persona indica hasta dónde llegó. El backend usa ese avance para
devolver solamente conversaciones que ya son seguras para esa persona.


## Qué tiene hasta ahora

- Landing pública que explica la idea de Umbral y carga el catálogo real.
- Tema claro/oscuro que recuerda la elección en el navegador.
- Navegación con menú de cuenta en escritorio y panel lateral accesible en
  móvil; incluye búsqueda, perfil, biblioteca y opciones según el rol.
- Registro e inicio de sesión con sesiones HTTP seguras, contraseñas hasheadas
  con BCrypt y protección CSRF.
- El catálogo y la ficha del juego se pueden explorar sin sesión. Guardar
  avance, publicar y leer las conversaciones seguras requieren autenticación.
- Roles iniciales `MEMBER` y `MODERATOR`. Las cuentas nuevas nacen como
  `MEMBER`; el rol nunca viene desde el formulario.
- Búsqueda de juegos desde RAWG para sugerir una edición concreta. La clave de
  esa API queda únicamente del lado de Spring, nunca llega al navegador.
- Panel mínimo de moderación: una sugerencia pendiente se aprueba con una
  descripción segura y recién entonces entra al catálogo local. Los checkpoints
  se proponen para cada juego y solo se habilitan tras una revisión moderadora.
- Catálogo, checkpoints y entradas de bitácora guardados en PostgreSQL.
- Usuarios demo históricos para poblar las conversaciones de ejemplo. No son
  cuentas de acceso.
- El progreso se crea o actualiza al elegir un checkpoint y se restaura al
  recargar React.
- Perfil personal en `/me` y biblioteca en `/me/library` para organizar juegos
  como `Quiero jugar`, `Jugando` o `Terminado` y marcarlos como favoritos.
- Perfil público en `/users/{handle}`: comparte el alias, métricas generales y
  juegos favoritos, sin exponer correo, rol, estados individuales ni progreso.
- Al guardar progreso por primera vez, el juego entra automáticamente a la
  biblioteca como `Jugando`; quitarlo de la biblioteca no borra progreso ni
  publicaciones.
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
| Frontend | React, React Router, Vite, JavaScript, Tailwind CSS 4 |
| Interfaz | Archivo Variable, Lucide, Motion, primitivas Radix adaptadas de shadcn/ui, Embla |
| Tests | JUnit, MockMvc, Testcontainers |
| CI | GitHub Actions |

## Endpoints actuales

| Método | Ruta | Uso |
| --- | --- | --- |
| `GET` | `/api/games` | Lista el catálogo. |
| `GET` | `/api/games/{gameId}/checkpoints` | Lista los checkpoints del juego. |
| `POST` | `/api/games/{gameId}/checkpoint-suggestions` | Propone un checkpoint para revisión. |
| `GET` | `/api/auth/csrf` | Entrega el token necesario para las operaciones que escriben datos. |
| `POST` | `/api/auth/register` | Crea una cuenta. |
| `POST` | `/api/auth/login` | Inicia una sesión HTTP. |
| `GET` | `/api/auth/me` | Devuelve la cuenta de la sesión actual. |
| `POST` | `/api/auth/logout` | Cierra la sesión actual. |
| `GET` | `/api/me/game-progress` | Consulta el progreso de la persona autenticada. |
| `PUT` | `/api/me/games/{gameId}/progress` | Guarda el checkpoint alcanzado. |
| `GET` | `/api/me/library` | Lista la biblioteca de la persona autenticada. |
| `POST` | `/api/me/library` | Agrega un juego a la biblioteca. |
| `PATCH` | `/api/me/library/{gameId}/status` | Actualiza el estado de un juego guardado. |
| `PATCH` | `/api/me/library/{gameId}/favorite` | Marca o quita un juego de favoritos. |
| `DELETE` | `/api/me/library/{gameId}` | Quita un juego de la biblioteca, sin borrar su progreso. |
| `GET` | `/api/users/{handle}` | Muestra un perfil público con métricas y favoritos. |
| `GET` | `/api/search?query=...` | Busca hasta cinco juegos y cinco perfiles públicos por título o alias. |
| `GET` | `/api/games/{gameId}/journal-entries` | Lista solo las entradas seguras para el progreso actual. |
| `POST` | `/api/me/journal-entries` | Publica una entrada en un checkpoint ya alcanzado. |
| `GET` | `/api/journal-entries/{entryId}/replies` | Lista las respuestas de una entrada visible. |
| `POST` | `/api/me/journal-entries/{entryId}/replies` | Publica una respuesta en una entrada visible. |
| `GET` | `/api/game-suggestions/search?query=...` | Busca ediciones en RAWG para una cuenta autenticada. |
| `POST` | `/api/game-suggestions` | Envía una edición a revisión. |
| `GET` | `/api/moderation/game-suggestions` | Lista sugerencias pendientes para una cuenta moderadora. |
| `POST` | `/api/moderation/game-suggestions/{id}/approve` | Aprueba y agrega el juego al catálogo local. |
| `POST` | `/api/moderation/game-suggestions/{id}/reject` | Rechaza una sugerencia pendiente. |
| `GET` | `/api/moderation/checkpoint-suggestions` | Lista checkpoints pendientes para moderación. |
| `POST` | `/api/moderation/checkpoint-suggestions/{id}/approve` | Aprueba y habilita un checkpoint. |
| `POST` | `/api/moderation/checkpoint-suggestions/{id}/reject` | Rechaza una propuesta de checkpoint. |

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
del juego de prueba. Las acciones personales piden iniciar sesión.

### Variables locales

El backend admite un archivo `.env` local —ignorado por Git— tanto en la raíz
del repositorio como dentro de `backend/`. Partí de `.env.example` y nunca
subas valores reales. La búsqueda de sugerencias usa:

```properties
RAWG_API_KEY=tu-clave-local
```

En producción, la misma clave debe configurarse como variable de entorno y no
como archivo.

### Despliegue

La aplicación está preparada para desplegar el frontend en Netlify, el backend
en Render mediante `backend/Dockerfile` y PostgreSQL en Supabase. El backend
recibe la conexión por `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`; el frontend usa
`VITE_API_BASE_URL`. Los valores reales se cargan en los paneles de cada
plataforma, nunca en el repositorio.

En el plan gratuito de Render, el backend puede detenerse después de un período
sin tráfico. El home muestra un estado de espera y reintenta el catálogo mientras
el servicio vuelve a iniciar. Las migraciones de Flyway se ejecutan al arrancar
el backend y actualizan el esquema de Supabase.

Para verificar el frontend antes de abrir una Pull Request:

```bash
cd frontend
npm run check
```

