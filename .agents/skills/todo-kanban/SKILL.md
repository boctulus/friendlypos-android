---
name: todo-kanban
description: Gestión del backlog Kanban en docs/to-do/. Estado = carpeta. Cambio de estado = mover archivo. Ver contrato completo en docs/contracts/todo-task-file-contract.md.
---

# SKILL: todo-kanban

## Propósito

Crear y gestionar tarjetas en el tablero Kanban de `docs/to-do/`. El módulo `/todo` (admin-only) lee y mueve estos archivos. La fuente de verdad es el archivo.

**No usar para journaling de ejecución activa** → SKILL `task-journaling`.

---

## Contrato completo

`docs/contracts/todo-task-file-contract.md`

Toda la especificación de frontmatter, estados, transiciones, validaciones y anti-patterns está ahí. Este SKILL es el punto de entrada; el contrato es la referencia normativa.

---

## Reglas esenciales

**Estado = carpeta** (nunca un campo en el frontmatter):

| Estado | Carpeta |
|---|---|
| `pending` | `docs/to-do/` (raíz) |
| `in-progress` | `docs/to-do/in-progress/` |
| `on-hold` | `docs/to-do/on-hold/` |
| `needs-review` | `docs/to-do/needs-review/` |
| `done` | `docs/to-do/done/` |
| `archived` | `docs/to-do/archived/` |
| `abandoned` | `docs/to-do/abandoned/` (soft-delete, no visible en la UI) |

**Cambio de estado = mover el archivo** — nunca editar texto para cambiar estado.

Tarea nueva → siempre en la raíz (`pending`).

---

## Frontmatter mínimo

```yaml
---
title: "Nombre de la tarea"
current_step: Paso actual
next_step: null
parallelizable_steps: []
parent: null
global_complexity: medium
for_agents: false
next_step_complexity: null
---
```

Complejidad — enum: `trivial | low | medium | high | extreme`

`for_agents: true` → tarea diseñada para un agente LLM; `false` → tarea para un humano.

❌ Sin `status` / `state` — el estado es la carpeta.

---

## Nombre de archivo

`kebab-case.md`, único en todo `docs/to-do/` (incluidas subcarpetas).

---

## Actualización al avanzar (AUTOMÁTICO — contrato §3.1)

Al terminar un paso o la tarea, actualizar el frontmatter **automáticamente, sin que el usuario lo pida y sin preguntar**. Re-evaluar TODOS juntos, no solo `current_step`:

| Campo | Regla |
|---|---|
| `current_step` | El nuevo paso en curso. |
| `next_step` | El siguiente paso planificado; `null` si no se conoce. |
| `next_step_complexity` | Complejidad del paso SIGUIENTE; `null` si no hay próximo paso. |
| `global_complexity` | Re-estimar si el alcance global cambió; si no, dejar igual. |
| `parallelizable_steps` | Ajustar si cambiaron. |

- ❌ Mover `current_step` dejando `next_step_complexity` del paso anterior.
- Actualizar frontmatter = editar contenido. Cambiar estado = mover el archivo (son dos cosas).
- Al cerrar: actualizar frontmatter **y** mover a `done/` / `needs-review/`.
