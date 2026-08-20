# Contexto de proyecto: Umbral

Este documento es el traspaso de contexto para iniciar **Umbral** en una tarea nueva de Codex. Debe copiarse a la raíz del nuevo proyecto y leerse antes de diseñar o escribir código.

## 1. Estado actual

- Umbral todavía no tiene repositorio, carpetas ni código.
- La idea fue elegida después de terminar un curso de Spring Boot.
- El objetivo es dejar de encadenar cursos y consolidar lo aprendido construyendo una aplicación full stack propia, publicable en GitHub y útil para el CV.
- El proyecto de práctica anterior fue `cine-java`, basado en el código del profesor. Allí se trabajó con Java 21, Spring Boot, REST, Controller, Service, Repository, DTO, entidades, PostgreSQL, Spring Data JPA, validaciones, excepciones, OpenAPI, Docker Compose, Render y una integración pequeña con LangChain4j.
- El usuario todavía no entiende con seguridad cómo conectar un frontend con un backend ni cómo organizar una aplicación full stack completa. Ese aprendizaje es una prioridad central de Umbral.
- El usuario tiene experiencia previa con React, JavaScript, HTML y CSS, pero quiere comprender la conexión de punta a punta, no copiar una solución terminada.
- Se estaba por comenzar un curso de Spring Data JPA. No es requisito terminarlo antes de Umbral: puede utilizarse como consulta puntual cuando el proyecto requiera relaciones, consultas, paginación o transacciones.

## 2. Forma de acompañamiento solicitada

- Enseñar un concepto por vez, con lenguaje simple y explicando para qué sirve cada archivo.
- No entregar de golpe toda la aplicación ni resolver automáticamente cada ejercicio.
- Priorizar objetivos pequeños, preguntas, pistas y revisión del intento del usuario.
- Dar código completo solamente cuando el usuario lo pida expresamente o cuando sea infraestructura mecánica que no aporta valor educativo escribir a mano.
- Trabajar mediante recorridos verticales pequeños: una acción debe atravesar frontend, HTTP, backend y base de datos antes de abrir muchas funcionalidades nuevas.
- Explicar siempre el recorrido de los datos y la responsabilidad de cada capa.
- Verificar cada paso con una prueba observable antes de continuar.
- No incorporar tecnologías solamente para que el proyecto parezca más complejo.
- Mantener Git con ramas de funcionalidad, commits intencionales, Pull Requests y merge manual. Automatizar primero compilación y tests mediante CI.

## 2.1. Material de estudio que puede consultarse

En la nueva tarea se puede dar acceso a las notas del curso de Spring y a las notas de Java del usuario. Ese material debe utilizarse como contexto pedagógico, no como una lista de temas que el usuario ya domina.

Reglas para consultar las notas:

- Buscar primero el concepto concreto que haga falta para el paso actual; no leer ni resumir todo el material sin necesidad.
- Distinguir entre código copiado del profesor, conceptos explicados en apuntes y cosas que el usuario realmente implementó o puede explicar.
- No asumir que por existir una nota sobre JPA, seguridad, SQL, testing o arquitectura el usuario ya sabe aplicarlo de forma autónoma.
- Antes de usar una anotación, dependencia, patrón o herramienta que no haya aparecido en el recorrido, explicar su propósito con un ejemplo pequeño.
- Si las notas del curso y una práctica recomendada difieren, mantener primero el objetivo de aprendizaje y explicar la diferencia; no hacer refactors amplios del material del curso.
- Las notas sirven para adaptar el ritmo y el vocabulario, no para convertir Umbral en una copia de `cine-java`.

Material de referencia esperado:

- Notas del curso de Spring Boot y Spring Data JPA.
- Notas de Java, SQL, backend, testing y seguridad que el usuario haya trabajado.
- El repositorio anterior `cine-java`, solamente como referencia de conceptos y capas ya vistos.

La primera tarea debe confirmar qué carpetas o archivos de notas están disponibles antes de consultarlos y debe pedir permiso práctico sólo si necesita abrir un archivo específico fuera del proyecto nuevo.

## 3. Visión del producto

**Nombre provisional:** Umbral.

**Concepto:** plataforma para registrar y compartir experiencias, teorías y análisis sobre videojuegos narrativos sin recibir spoilers de partes todavía no jugadas.

**Problema:** en una comunidad común, ocultar un texto con una etiqueta genérica de spoiler no indica hasta qué punto de una historia es seguro leerlo. Incluso títulos, imágenes, nombres o porcentajes de progreso pueden revelar información.

**Propuesta:** cada publicación pertenece a un checkpoint narrativo. Cada usuario registra el checkpoint que alcanzó en cada juego. El backend sólo devuelve publicaciones cuyo checkpoint sea igual o anterior al progreso del lector.

Regla central:

```text
checkpoint de la publicación <= checkpoint alcanzado por el lector
```

Esta autorización debe aplicarse en Spring. No alcanza con descargar todo y ocultarlo mediante React.

## 4. Alcance inicial recomendado

- Aplicación web, no móvil nativa.
- Comunidad social con bitácoras personales, aunque falta confirmar si el primer MVP será social desde el comienzo o privado primero.
- Soportar inicialmente juegos con progreso lineal.
- Cargar manualmente un catálogo pequeño de aproximadamente tres juegos.
- Cargar manualmente checkpoints seguros y ordenados para esos juegos.
- No depender inicialmente de IGDB, RAWG, Steam ni APIs externas de videojuegos.
- Evitar mostrar el número total de checkpoints si esa cantidad puede revelar cuánto falta para terminar una historia.
- Las historias con rutas alternativas, decisiones ramificadas y múltiples finales quedan fuera del MVP.

## 5. Entidades tentativas

Estas entidades son una hipótesis inicial, no un diseño congelado:

- `User`: cuenta y perfil de una persona.
- `Game`: videojuego disponible en Umbral.
- `Checkpoint`: punto narrativo ordenado y perteneciente a un juego.
- `UserGameProgress`: checkpoint máximo alcanzado por un usuario en un juego.
- `JournalEntry`: publicación o entrada de bitácora asociada a autor, juego y checkpoint.
- `Comment`: se agregará después del flujo central y deberá respetar la misma barrera de spoilers.
- `Reaction`: funcionalidad social posterior.

Relaciones conceptuales:

```text
Game 1 ----- N Checkpoint
User 1 ----- N UserGameProgress N ----- 1 Game
User 1 ----- N JournalEntry N ----- 1 Game
Checkpoint 1 ----- N JournalEntry
```

Antes de implementar estas relaciones se deben escribir ejemplos concretos y revisar qué información necesita cada pantalla.

## 6. Arquitectura tentativa

Se recomienda un monorepo para que el producto completo sea fácil de explorar:

```text
umbral/
├── backend/              Spring Boot y Gradle
├── frontend/             React y Vite
├── docs/                 producto, decisiones y arquitectura
├── docker-compose.yml    PostgreSQL local, cuando corresponda
└── README.md
```

Tecnologías iniciales recomendadas:

- Java 21.
- Spring Boot, eligiendo una versión estable y compatible con las demás dependencias.
- Gradle.
- Spring Web MVC.
- Spring Data JPA.
- PostgreSQL.
- React con Vite.
- JavaScript inicialmente; no sumar TypeScript mientras se aprende la integración full stack, salvo decisión posterior.
- `fetch` como cliente HTTP inicial; no agregar Axios sin una necesidad concreta.
- React Router cuando haya más de una pantalla real.
- JUnit y pruebas de Spring para reglas y endpoints.
- Una herramienta de tests de frontend solamente cuando exista comportamiento significativo.
- GitHub Actions para ejecutar tests en push y Pull Requests.

No usar inicialmente microservicios, Kubernetes, Kafka, WebSockets, Redux, una arquitectura distribuida ni múltiples bases de datos.

## 7. Recorrido que debe comprenderse

```text
Usuario realiza una acción en React
    -> React envía una solicitud HTTP con JSON
    -> Controller recibe y valida la solicitud
    -> Service aplica la regla de negocio
    -> Repository consulta o modifica PostgreSQL
    -> Spring responde con estado HTTP y JSON
    -> React actualiza la interfaz
```

Durante desarrollo, frontend y backend probablemente se ejecutarán en puertos distintos. Se deberá explicar CORS o el proxy de Vite cuando aparezca el primer bloqueo real, no antes.

## 8. Primeros recorridos verticales

### Recorrido 1: listar juegos

1. Spring expone `GET /api/games`.
2. React solicita ese endpoint.
3. React muestra los juegos recibidos.
4. Verificación: cambiar un dato del backend y comprobar que cambia en la interfaz.

### Recorrido 2: ver checkpoints de un juego

1. El usuario selecciona un juego.
2. React solicita sus checkpoints.
3. Spring devuelve solamente información considerada segura.
4. React permite seleccionar el progreso alcanzado.

### Recorrido 3: crear una entrada

1. El usuario escribe una entrada y elige su checkpoint.
2. React envía un `POST`.
3. Spring valida y persiste la entrada.
4. React muestra la respuesta creada.

### Recorrido 4: barrera anti-spoilers

1. Se preparan dos usuarios o contextos con progresos diferentes.
2. Se solicitan las publicaciones del mismo juego.
3. Spring devuelve resultados diferentes según el progreso.
4. Se prueba la regla en el Service y mediante un endpoint.

### Después

- Registro e inicio de sesión.
- Spring Security y autorización real.
- Perfiles.
- Comentarios y reacciones.
- Paginación y búsqueda.
- Recomendaciones mediante IA.
- Tests más completos, CI, documentación y despliegue.

## 9. Uso propuesto de IA

La IA es una funcionalidad posterior, no la base del MVP.

Caso recomendado: sugerir el próximo juego según juegos terminados, calificaciones, géneros, duración, plataforma y preferencias del usuario.

Flujo seguro:

```text
Spring consulta el catálogo
    -> Java descarta juegos ya jugados y aplica filtros obligatorios
    -> se envía al modelo una lista cerrada de candidatos válidos
    -> la IA selecciona y explica hasta tres recomendaciones
    -> Spring valida que los IDs de la respuesta existan entre los candidatos
    -> React muestra la recomendación
```

La IA no debe:

- Inventar juegos como si existieran en la base de datos.
- Ser la única encargada de decidir qué contenido puede ver un usuario.
- Clasificar automáticamente una publicación como libre de spoilers y publicarla sin control.
- Recibir entradas correspondientes a checkpoints que el usuario no puede ver.

Se puede reutilizar LangChain4j porque ya fue usado en `cine-java`. Al comenzar esta etapa se debe consultar documentación actual y elegir starters compatibles con la versión de Spring Boot. Spring AI puede evaluarse más adelante como comparación, no simultáneamente con la primera integración frontend-backend.

## 10. Exclusiones deliberadas del MVP

- Aplicación móvil.
- Chat en tiempo real.
- Importación automática de bibliotecas de Steam o consolas.
- Catálogo masivo de videojuegos.
- Recomendador con embeddings o RAG desde el primer día.
- Moderación automática completa.
- Rutas narrativas ramificadas.
- Panel administrativo complejo.
- Seguidores, notificaciones y logros antes de validar la regla principal.

## 11. Definición de éxito

Umbral será un proyecto de portfolio convincente si permite:

- Abrir una demo funcional.
- Entender el problema en menos de un minuto.
- Crear o usar dos cuentas con progresos diferentes.
- Demostrar que cada cuenta recibe contenido diferente por la barrera anti-spoilers.
- Explicar el recorrido React -> HTTP -> Spring -> PostgreSQL -> React.
- Mostrar tests de la regla principal.
- Leer un README con capturas, arquitectura, decisiones, instalación y limitaciones conocidas.
- Revisar un historial de Git con funcionalidades construidas gradualmente.

No es necesario que tenga docenas de funciones ni miles de usuarios.

## 12. Decisiones todavía abiertas

La nueva tarea debe resolver estas preguntas antes de crear muchas carpetas:

1. ¿El primer MVP será una comunidad desde el comienzo o una bitácora privada que luego se comparte? Recomendación actual: comunidad mínima, porque vuelve esencial la barrera anti-spoilers.
2. ¿Qué tres juegos servirán como catálogo inicial y cómo se definirán checkpoints sin incluir spoilers en nombres visibles?
3. ¿Qué estilo visual y tono tendrá Umbral?
4. ¿Qué información mínima necesita una publicación además de texto, juego y checkpoint?
5. ¿Se comenzará con datos temporales en memoria para aprender la conexión o directamente con PostgreSQL? Recomendación: completar primero un `GET` vertical muy pequeño y luego persistir.
6. ¿Qué parte implementará el usuario y qué parte será infraestructura preparada junto con Codex?

## 13. Primera instrucción para la nueva tarea de Codex

Copiar y enviar este mensaje en la primera conversación dentro del proyecto nuevo:

> Leé completamente `CONTEXTO-UMBRAL.md` antes de actuar. Quiero construir Umbral como proyecto personal full stack para aprender, no recibir una aplicación terminada. Primero revisá conmigo las decisiones abiertas y ayudame a definir el MVP. Avanzá un concepto y un recorrido vertical por vez, explicando el propósito de cada archivo y cómo viajan los datos entre React, Spring y PostgreSQL. No escribas todavía toda la estructura ni todas las entidades. Empecemos por confirmar el producto, elegir el catálogo inicial y diseñar el primer recorrido `GET /api/games` hasta React.

## 14. Referencia del proyecto anterior

El proyecto usado durante el curso está en:

```text
E:\Carpetas\Programación\programacion solo\back\Java\004-javaSpring\cine-java
```

Puede inspeccionarse para recordar conceptos o comparar capas, pero no debe copiarse ciegamente. Umbral debe comenzar en una carpeta y repositorio nuevos.
