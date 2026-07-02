---
name: git-safety-protocol
description: Atomic execution workflow with integrated git safety constraints
---

# SKILL_DEFINITION: Git Execution & Safety Protocol

## 1. Purpose

Este SKILL define un modelo unificado para:

- ejecución controlada de desarrollo (atomicidad y trazabilidad)
- estandarización de commits
- prevención de riesgos en operaciones git destructivas

Combina:
- disciplina de ejecución
- protección operativa

---

## 2. Task Decomposition (Required but Lightweight)

Antes de implementar:

- Dividir el requerimiento en sub-tareas **mínimas y trazables**
- Cada sub-tarea debe representar **una intención única de cambio**

Ejemplos válidos:
- add product model
- implement ean padding logic
- fix checkout total calculation

❌ Evitar:
- tareas ambiguas o multi-propósito

---

## 3. Atomic Execution Loop (Mandatory)

Cada sub-tarea sigue este ciclo:

### A. Implementación
- Escribir solo el código necesario para la sub-tarea actual
- Permitir refactor **solo si es:**
  - local
  - necesario
  - acotado al objetivo

### B. Validación
- Verificar que la funcionalidad cumple lo esperado
- Asegurar **no regresión** en:
  - core
  - lógica existente
  - contratos

Si falla:
- corregir dentro de la misma sub-tarea
- NO avanzar

### C. Commit Atómico
- Un commit por sub-tarea
- Cada commit debe representar una única intención

---

## 4. Commit Convention (Strict)

Formato obligatorio:

```

<type>(<scope>): <description in lowercase>

```

Tipos permitidos:
- feat
- fix
- docs
- style
- refactor
- perf
- test
- chore

Ejemplos:
```

feat(products): add ean validation
fix(cart): correct floating point rounding
refactor(api): simplify error handler

```

Reglas:
- descripción corta, clara y en minúsculas
- sin mezclar múltiples cambios

---

## 5. Flow Control Rules

- No mezclar múltiples sub-tareas en un commit
- Se permite trabajo iterativo local (WIP) sin commit
- No avanzar a una nueva sub-tarea sin:
  - validación completa
  - commit realizado

---

## 6. Git Safety Protocol (Non-Negotiable)

Ningún agente puede ejecutar:

- checkout
- reset
- revert
- stash
- rebase

sin cumplir:

### Requisitos obligatorios:

1. Explicar el comando exacto
2. Explicar qué archivos serán afectados
3. Explicar el impacto esperado
4. Esperar aprobación explícita del usuario

---

## 7. Safety Default

Si existe cualquier ambigüedad:

→ Se asume **NO ejecutar la operación**

---

## 8. Design Principles

- Atomicidad > velocidad
- Claridad > conveniencia
- Trazabilidad > volumen de cambios
- Seguridad > automatización

---

## 9. Expected Outcomes

Aplicando este SKILL:

- Historial git limpio y auditables
- Debugging eficiente (git bisect viable)
- Menor necesidad de operaciones destructivas
- Reducción de deuda técnica temprana
- Mayor control sobre cambios críticos

---

# Observaciones finales (importantes)

* Este diseño **no es rígido artificialmente**, pero sí **estricto donde importa**.
