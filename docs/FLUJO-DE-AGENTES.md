# Flujo de agentes de Codex

Este documento define cómo usar Codex en Umbral sin perder el objetivo de
aprendizaje ni crear cambios simultáneos sobre el mismo árbol de trabajo.

## Principio de trabajo

Cada tarea del roadmap tiene un agente principal que conserva el contexto del
recorrido vertical y acompaña al usuario. Los subagentes son apoyos temporales,
no reemplazos del agente principal ni del código que debe escribir el usuario.

No se usan subagentes por defecto. Se delega únicamente una tarea acotada e
independiente, con resultado verificable y sin solapamiento de archivos.

## Roles

| Rol | Responsabilidad | Cambios permitidos |
| --- | --- | --- |
| Agente principal | Lee el contexto, delimita el corte, enseña un concepto por vez, revisa los cambios del usuario y coordina la verificación. | Solo los autorizados explícitamente por el usuario. |
| Investigador | Contrasta una decisión con el repositorio de referencia, las notas de Obsidian y documentación primaria. Entrega hallazgos y fuentes. | Ninguno. |
| Revisor | Revisa una propuesta o un diff para detectar inconsistencias de dominio, JPA, API, React, seguridad o mantenibilidad. | Ninguno. |
| Verificador | Ejecuta los controles acordados y describe resultados reproducibles. | Ninguno; no corrige fallos por su cuenta. |

El agente principal es el único que puede integrar resultados y proponer el
siguiente paso. Nunca debe haber más de un agente que edite el repositorio a la
vez.

## Ciclo por tarea durable

1. **Ubicación.** El agente principal lee `AGENTS.md`,
   `docs/CONTEXTO-UMBRAL.md`, `docs/HANDOFF-UMBRAL.md` y la tarea correspondiente
   de `docs/ROADMAP.md`. Confirma el estado del árbol y las herramientas si la
   sesión viene de otra máquina.
2. **Diseño.** Para cambios de persistencia, relaciones o contratos HTTP,
   investiga primero la solución objetivo. Puede delegar la investigación como
   tarea de solo lectura si es independiente. Presenta objetivo, datos,
   restricciones, archivos y verificación; espera aprobación antes de editar
   archivos funcionales.
3. **Implementación guiada.** El usuario escribe un bloque pequeño y coherente.
   El agente explica el objetivo del archivo, plantea un ejercicio o una pista y
   revisa el resultado antes de abrir el siguiente concepto.
4. **Revisión.** Cuando el cambio atraviesa varias capas, puede pedirse una
   revisión de solo lectura del diff. La revisión no reescribe código ni amplía
   el alcance.
5. **Verificación.** Se ejecutan los controles acordados y la prueba observable
   de la tarea. Un agente verificador puede informar resultados, pero no marcar
   la tarea como hecha si falla algún control relevante.
6. **Checkpoint Git.** Solo cuando el usuario lo solicita, se sigue la rutina
   de `AGENTS.md`: inspección, propuesta de alcance y commit, confirmación,
   stage selectivo y commit o publicación explícitamente autorizados.

## Cuándo delegar

Delegar solo si la tarea puede terminar con un informe independiente, por
ejemplo:

- investigar anotaciones JPA y restricciones necesarias antes de modelar una
  relación;
- comparar el diseño con `FacuPompa/java-spring` y las notas pertinentes;
- revisar el diff de un corte ya implementado;
- ejecutar y resumir verificaciones luego de que el cambio esté terminado.

No delegar la implementación de capas distintas del mismo recorrido, la
decisión pedagógica de qué explicar primero, ni tareas que exijan tocar los
mismos archivos.

## Límites de autonomía

- Leer archivos, inspeccionar diffs, consultar documentación y ejecutar controles
  no destructivos son acciones seguras dentro de la tarea.
- Crear o modificar archivos funcionales requiere la autorización explícita ya
  definida en `AGENTS.md`.
- Git add, commit, push, pull request, merge y cualquier acción externa siguen
  requiriendo la autorización indicada en la rutina de checkpoint.
- Un subagente no recibe autorización adicional por existir: respeta los mismos
  límites del agente principal.

## Prompts útiles

Para iniciar una tarea del roadmap:

> Empecemos la tarea N del roadmap. Quiero implementar yo. No escribas código
> todavía: explicame el objetivo del primer archivo, la relación con el flujo
> completo y dame una consigna para intentarlo.

Para pedir investigación independiente:

> Antes de implementar, delegá una investigación de solo lectura sobre [tema].
> Quiero restricciones, alternativas descartadas y fuentes; no modifiques
> archivos.

Para cerrar un corte:

> Revisá este corte en modo solo lectura. Contrastá dominio, JPA, contrato HTTP
> y React contra el roadmap. Después indicá las verificaciones pendientes; no
> edites código.
