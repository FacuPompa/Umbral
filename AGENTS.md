# Umbral: instrucciones de proyecto

## Contexto y tutoría

- Antes de diseñar o implementar, leer `CONTEXTO-UMBRAL.md` y `docs/HANDOFF-UMBRAL.md`.
- Umbral es un proyecto de aprendizaje full stack. Avanzar un concepto y un recorrido vertical por vez.
- El usuario escribe el código propio. Explicar el objetivo de cada archivo, hacer preguntas, dar pistas y revisar sus cambios. Dar código completo solo cuando lo pida expresamente.
- No crear entidades, repositories, controllers, services, componentes u otros archivos funcionales sin explicar primero su propósito y recibir autorización explícita.
- Mantener las decisiones del MVP: catálogo inicial Persona 5 Royal; sin login, comentarios, reacciones ni barrera anti-spoilers real hasta llegar a esos recorridos.

## Diseño durable y referencia previa

- Antes de proponer una nueva feature, persistencia, entidad, contrato HTTP, modelo de datos o refactor relevante, investigar primero la solución objetivo:
  1. revisar los archivos pertinentes del repositorio `https://github.com/FacuPompa/java-spring`;
  2. buscar y leer solamente las notas relevantes de Obsidian sobre ese concepto;
  3. contrastar ambos con el estado actual de Umbral y, si hay comportamiento dependiente de versión, consultar documentación primaria actual.
- Antes de editar código, presentar una propuesta breve con: objetivo durable, datos que viven en memoria o en PostgreSQL, archivos que cambiarán, y cómo se verificará. Esperar autorización explícita si se crearán o modificarán archivos funcionales.
- Un recorrido vertical pequeño no justifica código descartable. No agregar mocks en memoria, datos hardcodeados, inicializadores alternativos ni modelos provisionales si la siguiente etapa previsiblemente los reemplazará.
- Un experimento temporal solo se permite si el usuario lo aprueba expresamente como `spike pedagógico`; antes de crearlo se debe indicar su alcance, qué se eliminará después y por qué no se implementa todavía la alternativa durable.
- Cuando exista una solución durable conocida en el proyecto de referencia, adaptarla a Umbral en vez de inventar una variante para luego reemplazarla. Ejemplo: para catálogo inicial fijo en desarrollo, elegir una sola estrategia de carga (`data.sql` idempotente) y no combinarla con `CommandLineRunner`.
- Antes de modelar una relación, usar casos concretos de pantalla y datos para definir las entidades, restricciones y ciclo de vida completo. No crear una entidad aislada que no forme parte de un diseño ya revisado.
- Si el usuario pide una solución basada en su repositorio anterior y sus notas, entregar una solución completa y coherente para el alcance acordado, explicando las diferencias necesarias; no dosificarla artificialmente en cambios que se sabe que se reemplazarán.

## Rutina de checkpoint Git

Activar esta rutina cuando el usuario diga, por ejemplo: "cerramos checkpoint", "hay que commitear", "preparemos el commit" o "quiero sincronizar entre PCs".

1. Inspeccionar primero, sin cambiar nada:
   - `git status --short --branch`
   - diff y archivos modificados relevantes
   - rama actual y remoto, si Git permite consultarlos
2. Si Git informa `detected dubious ownership` u otro bloqueo, explicar el error y detener la rutina. No cambiar configuraciones globales de Git sin autorización explícita.
3. Separar los archivos que pertenecen al checkpoint de cualquier cambio ajeno. Informar el alcance encontrado.
4. Ejecutar verificaciones según el alcance:
   - cambios de backend: ejecutar los tests de Gradle;
   - cambios de frontend: ejecutar el build de Vite;
   - cambios de ambos: ejecutar ambos controles.
5. Informar resultados, resumir qué cambió y proponer:
   - si corresponde una rama de feature;
   - un mensaje de commit claro;
   - los paths exactos que deberían incluirse.
6. Esperar confirmación explícita del usuario antes de hacer `git add`, commit, push o crear un Pull Request.
7. Al recibir autorización, stagear solamente los paths aprobados con `git add -- <paths>`. Nunca usar `git add .`, `git add -A` ni equivalentes masivos.
8. No hacer push, Pull Request, merge ni cambios directos a `main`/`master` salvo petición explícita. Los merges son manuales.

### Checkpoint de sincronización

Si el usuario necesita cambiar de PC antes de terminar una feature, proponer un commit de sincronización descriptivo, por ejemplo `wip: checkpoints en memoria`. Aun así, requerir confirmación antes de crear o publicar ese commit.

## Verificación

- No afirmar que una feature está lista si las verificaciones relevantes no se ejecutaron o fallaron.
- Si el proyecto se abre desde otra máquina, revisar primero el handoff y comprobar versiones/herramientas antes de editar código.
