# Roadmap de Umbral

Este roadmap organiza recorridos verticales durables. Una tarea se considera
terminada solo cuando cumple su criterio observable y sus verificaciones.

## 1. Persistir checkpoints y progreso del usuario temporal

- **Estado:** En progreso.
- **Objetivo:** reemplazar los checkpoints y el progreso en memoria por datos
  persistidos en PostgreSQL, sin incorporar autenticación todavía.
- **Alcance incluido:**
  - `Checkpoint`, `User` temporal fijo y `UserGameProgress` como entidades JPA;
  - relaciones y restricciones de integridad entre juego, checkpoint, usuario y
    progreso;
  - carga idempotente en `data.sql` de Persona 5 Royal, sus checkpoints seguros
    y el usuario demostración con id `1`;
  - lectura de checkpoints desde PostgreSQL;
  - lectura y guardado idempotente del progreso mediante `/api/me`;
  - reemplazo del estado temporal de React por el progreso confirmado por la API;
  - pruebas de repositorio, servicio y HTTP para el flujo y sus errores;
  - verificación en DBeaver y en la interfaz tras recargar la página.
- **Alcance excluido:** login, Spring Security, registro, perfiles, entradas de
  bitácora, comentarios, reacciones y barrera anti-spoilers sobre publicaciones.
- **Dependencias:** PostgreSQL local disponible; diseño durable aprobado; mantener
  la carga inicial exclusivamente con `data.sql`.
- **Avance actual:** entidades JPA `Checkpoint`, `User` y `UserGameProgress`, y
  `CheckpointRepository` creados. Hibernate creó las tablas del modelo en
  PostgreSQL y el arranque fue verificado desde IntelliJ. Aún faltan carga
  inicial, repositories restantes, servicios, contratos HTTP, React y pruebas.
- **Criterio observable de terminado:** una persona elige un checkpoint para
  Persona 5 Royal, recarga la página y ve el mismo progreso; DBeaver muestra una
  única fila de progreso para el usuario demostración y el juego; las pruebas
  previstas pasan.

## 2. Crear entradas de bitácora ligadas a un checkpoint

- **Estado:** Pendiente.
- **Objetivo:** permitir que el usuario temporal cree y consulte sus propias
  entradas, asociadas de forma consistente a un juego y a un checkpoint.
- **Alcance incluido:** modelo `JournalEntry`, contrato HTTP de creación y
  consulta personal, validaciones de texto y relaciones con autor, juego y
  checkpoint; interfaz React para escribir y listar las entradas propias; pruebas
  del recorrido completo.
- **Alcance excluido:** feed comunitario, lectura de entradas ajenas, comentarios,
  reacciones, edición, eliminación y autorización real.
- **Dependencias:** tarea 1 hecha; la relación checkpoint--juego y el contexto
  temporal `/api/me` deben estar persistidos.
- **Criterio observable de terminado:** se crea una entrada desde React, queda en
  PostgreSQL con su checkpoint correcto y continúa visible luego de recargar.

## 3. Aplicar la barrera anti-spoilers al feed comunitario

- **Estado:** Bloqueada.
- **Objetivo:** servir solamente entradas cuyo checkpoint sea igual o anterior al
  progreso del lector.
- **Alcance incluido:** consulta de feed filtrada en Spring, dos contextos de
  usuario con progresos distintos para demostrar la regla, pruebas de servicio y
  endpoint, y una interfaz que presente solo el contenido autorizado.
- **Alcance excluido:** login real, ocultamiento basado solo en React,
  recomendaciones, comentarios, reacciones y soporte de narrativas ramificadas.
- **Dependencias:** tareas 1 y 2 hechas; debe decidirse cómo representar dos
  usuarios de demostración sin anticipar el diseño de autenticación.
- **Criterio observable de terminado:** ante el mismo juego y dos lectores con
  distinto progreso, el backend devuelve colecciones diferentes y las pruebas
  verifican `checkpoint de la entrada <= progreso del lector`.

## 4. Incorporar identidad y autorización real

- **Estado:** Bloqueada.
- **Objetivo:** reemplazar el usuario temporal por cuentas autenticadas sin cambiar
  los contratos de progreso basados en `/api/me` ni perder los datos asociados.
- **Alcance incluido:** registro, inicio de sesión, credenciales protegidas,
  Spring Security, resolución del usuario autenticado, estrategia de migración de
  la cuenta demo y pruebas de autorización.
- **Alcance excluido:** proveedores sociales, recuperación de contraseña,
  verificación de correo, roles administrativos y perfiles sociales avanzados.
- **Dependencias:** tarea 3 hecha y una decisión explícita sobre el mecanismo de
  autenticación y el ciclo de vida de la cuenta demo.
- **Criterio observable de terminado:** dos cuentas reales inician sesión y cada
  una recupera únicamente su propio progreso y el feed autorizado por ese
  progreso.

## 5. Extender la comunidad sin romper la regla de spoilers

- **Estado:** Bloqueada.
- **Objetivo:** sumar interacciones sociales únicamente cuando respeten la misma
  frontera narrativa que las entradas.
- **Alcance incluido:** comentarios y reacciones, sus relaciones, consultas
  filtradas por checkpoint y pruebas que aseguren que no revelan contenido no
  autorizado.
- **Alcance excluido:** chat en tiempo real, notificaciones, seguidores,
  moderación automática, rutas narrativas ramificadas e IA.
- **Dependencias:** tareas 3 y 4 hechas; definición específica de cómo el
  checkpoint de una interacción hereda o declara su alcance narrativo.
- **Criterio observable de terminado:** ningún endpoint ni pantalla permite ver
  una entrada, comentario o reacción cuyo checkpoint sea posterior al progreso
  del lector.
