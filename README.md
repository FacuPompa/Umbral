# Umbral

Umbral es una aplicación web para hablar de videojuegos narrativos sin recibir
spoilers. Cada juego se divide en checkpoints narrativos y cada persona declara
hasta cuál llegó. Esa información será la base para mostrar contenido seguro
según el progreso del lector.

El catálogo inicial contiene Persona 5 Royal y usa tramos narrativos por
palacios, en lugar de un porcentaje genérico de avance.

## Estado actual

El backend ya persiste el catálogo, los checkpoints y el progreso de un usuario
de demostración en PostgreSQL. La interfaz ya muestra el catálogo y los
checkpoints; su próxima integración será guardar y restaurar ese progreso a
través de la API.

### Implementado

- Catálogo persistente de Persona 5 Royal.
- Diez checkpoints narrativos ordenados.
- Usuario de demostración persistido (`id = 1`).
- Un único progreso por usuario y juego, actualizable mediante `PUT`.
- Validación del checkpoint y de su pertenencia al juego.
- Carga inicial idempotente con `data.sql`.
- PostgreSQL local en Docker y exploración de datos con DBeaver.
- Frontend React que lista juegos y permite consultar sus checkpoints.

### API disponible

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/api/games` | Devuelve el catálogo. |
| `GET` | `/api/games/{gameId}/checkpoints` | Devuelve los checkpoints ordenados del juego. |
| `GET` | `/api/me/game-progress` | Devuelve el progreso del usuario demo. |
| `PUT` | `/api/me/games/{gameId}/progress` | Crea o actualiza el checkpoint alcanzado. |

Por ahora `/api/me` siempre representa al usuario demo. No hay login ni
autorización real todavía.

## Próximos pasos

1. Conectar React al `GET` y `PUT` de progreso, incluyendo restauración al
   recargar la página.
2. Agregar pruebas de repository, service y HTTP con PostgreSQL aislado.
3. Permitir crear entradas de bitácora asociadas a un checkpoint.
4. Aplicar la regla anti-spoilers en el feed.
5. Incorporar identidad y autorización reales.

## Stack

- Java 26, Spring Boot y Gradle.
- Spring Web MVC, Spring Data JPA y Bean Validation.
- PostgreSQL 17 en Docker Compose.
- React, Vite y JavaScript.

## Ejecutar en local

1. Levantá PostgreSQL desde la raíz:

   ```bash
   docker compose up -d
   ```

2. Iniciá `UmbralApplication` desde IntelliJ.
3. En otra terminal, levantá el frontend:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

El backend usa PostgreSQL en `localhost:5432`, base `umbral`, con credenciales
locales definidas en `backend/src/main/resources/application.properties`.
