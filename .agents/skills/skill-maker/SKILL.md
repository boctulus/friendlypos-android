---
name: skill-maker
description: Creates and manages SKILL definitions for the agent.
---

# SKILL_DEFINITION: Skill Maker

This skill is responsible for creating new SKILL definitions.

## Behavior

When the user requests the creation of a new SKILL, follow this workflow.

---

# 1. Ask Scope

Ask the user whether the skill should be:

- **local (default)**
- **global**

If the user does not specify, assume **local**.

---

# 2. Determine Skill Name

## 🔑 SKILL NAMING RULE

Formato general:

```
<core-concept>[-qualifier]-<type>[--domain/context]
```

### 2.1 Core Concept (primer token)

* Representa **el problema, feature o comportamiento principal** del skill.
* Ejemplos: `deterministic-layout`, `api-input`, `grid-integrity`, `print-flow`

### 2.2 Qualifier (opcional)

* Si hace falta especificar un subtipo o detalle, se añade aquí usando **kebab-case**.
* Ejemplo: `thermal-ticket` en `thermal-ticket-layout-contract`

### 2.3 Type (siempre al final)

* Describe la función del skill en tu ecosistema. Usar solo uno de estos tipos:

  * `contract` → reglas inmutables / estándares
  * `guard` → validación / prevención
  * `enforcer` → ejecución activa / automatización
  * `protocol` → procesos / secuencias

### 2.4 Domain / Contexto (opcional, al final)

* Representa tecnología, área o contexto especial.
* Separado con doble guion `--` (CLI-safe, parseable)
* Ejemplo: `--bootstrap`, `--flexbox`, `--firestore`

---

## ✅ Reglas de estilo

1. Todo en **kebab-case**
2. Sin espacios, paréntesis, comillas o caracteres especiales
3. **Core concept primero**, **tipo al final**, dominio opcional al final
4. **No usar prefijos genéricos** como `view-`, `ui-`, `code-`
5. Metadata opcional dentro de `SKILL.md`:

```yaml
type: contract | guard | enforcer | protocol
domain: view | api | database | bootstrap

```

6. **Compatibilidad LLM (ASCII estricto en reglas)**:
   - ❌ Evita símbolos unicode en secciones ejecutables o condicionales (`∈`, `∉`, `→`, `✓`, `✗`).
   - ✅ Usa equivalentes en texto plano: `in`, `not in`, `->`, `PASS`, `FAIL`.
   - Los emojis o símbolos decorativos solo se permiten en comentarios o documentación no ejecutable.

### Normalize the name using these rules:

- Convert to **kebab-case**
- Remove special characters
- Trim spaces

Example:

```
"Skill Maker" → skill-maker
"Test Data Generator" → test-data-generator
```

7. **Consistencia de invocación**: El nombre final debe ser único, predecible y coincidir exactamente con la referencia en `REQUIRES`, `TRIGGERS` y rutas de archivo.


---

### 🔹 Ejemplos correctos

| Original                                        | Correct Naming                            |
| ----------------------------------------------- | ----------------------------------------- |
| "Deterministic Layout Contract"                 | `deterministic-layout-contract`           |
| "View Grid Integrity Guard (Bootstrap Flexbox)" | `grid-integrity-guard--bootstrap-flexbox` |
| "Scoped Style Contrast Fix (Bootstrap)"         | `scoped-style-contrast-fix--bootstrap`    |
| "Print Flow No Preview"                         | `print-flow-contract--no-preview`         |
| "API Input Validation"                          | `api-input-validation-contract`           |

---

# 3. Skill Creation

### Local Skill

```
.agents/
  skills/
    {skill-name}/
      SKILL.md
```

### Global Skill

Use the configured global skills directory if available.

---

# 4. Estructura Global de Skill (Obligatoria)

Todo SKILL DEBE seguir exactamente este orden:

```md
---
name: <kebab-case-name>
description: <string>
---

# SKILL_DEFINITION: <skill-id>

## ACTIVATION (ENTRY GATE)

<rules>

## EXECUTION PLAN (MANDATORY)

<steps>

## REQUIRES (HARD DEPENDENCIES)

<dependencies>

## SKILL ORDER EXECUTION (ENFORCED SEQUENCE)

<ordered list>

## TRIGGERS

<events>
```

---

# 5. Secciones Obligatorias

## 5.1 ACTIVATION (ENTRY GATE)

### 📏 Formato obligatorio

```md
## ACTIVATION (ENTRY GATE)

This SKILL MUST only be applied when ALL of the following conditions are true:

- <condition 1>
- <condition 2>
- <condition N>

---

If these conditions are NOT met:

→ DO NOT APPLY this SKILL
→ STOP reading further instructions
→ Continue with other relevant SKILLs
```

### ❗ Reglas

* Debe usar **"ALL of the following conditions"**
* Debe incluir bloque de negación EXACTO
* No se permite texto adicional fuera del formato

---

## 5.2 EXECUTION PLAN (MANDATORY)

### 📏 Gramática estricta

```md
## EXECUTION PLAN (MANDATORY)

STEP <number>: <title> (<optional condition>)

TYPE: COMMAND | ACTION | CHECK

COMMAND: <shell command>
ACTION: <imperative action>
CHECK:
- <check 1>
- <check 2>

ON_FAILURE:
→ STOP
→ REPORT ERROR: <message>
```

---

### 📌 Reglas duras

* `STEP` debe ser secuencial (1, 2, 3…)
* Cada STEP debe tener **exactamente UN TYPE**
* TYPE determina campos válidos:

| TYPE    | Campos permitidos |
| ------- | ----------------- |
| COMMAND | COMMAND           |
| ACTION  | ACTION            |
| CHECK   | CHECK             |

---

### 🔁 Condicionales permitidos

```md
STEP 2: Create sample data (ONLY IF: datagrid_enabled == true)
```

Formato:

```
(ONLY IF: <boolean_expression>)
```

---

### 🧨 Manejo de errores (obligatorio en TODOS los pasos)

Cada STEP DEBE incluir:

```md
ON_FAILURE:
→ STOP
→ REPORT ERROR: <text>
```

---

## 5.3 REQUIRES (HARD DEPENDENCIES)

### 📏 Formato EXACTO (no modificable)

```md
## REQUIRES (HARD DEPENDENCIES)

These SKILLs MUST be active before this SKILL can execute:

- skill-one
- skill-two
- skill-three

If any are missing:
→ STOP
→ LOAD them
→ RESTART execution
```

---

### ❗ Reglas

* Lista obligatoria (mínimo 1)
* No texto adicional
* Keywords exactas:

  * `STOP`
  * `LOAD them`
  * `RESTART execution`

---

## 5.4 SKILL ORDER EXECUTION (ENFORCED SEQUENCE)

### 📏 Formato

```md
## SKILL ORDER EXECUTION (ENFORCED SEQUENCE)

Apply dependencies in this exact order:

1. skill-one
2. skill-two
3. skill-three
```

---

### ❗ Reglas

* Debe coincidir con REQUIRES
* Orden explícito obligatorio
* Numeración secuencial

---

## 5.5 TRIGGERS

### 📏 Formato estructurado

```md
## TRIGGERS

### ON_EVENT

EVENT: <event_name>
→ APPLY SKILL: <skill-name>

---

### ON_CONDITION

IF <boolean_expression>
→ APPLY SKILL: <skill-name>

---

### ON_COMPLETE

→ APPLY SKILL: <skill-name>
→ APPLY SKILL: <skill-name>
```

---

### ❗ Reglas de secciones válidas

Secciones disponibles:

  * `ON_EVENT` — disparado por cambio externo (reactivo)
  * `ON_CONDITION` — disparado por estado del código (evaluación activa)
  * `ON_COMPLETE` — disparado cuando el skill termina (encadenamiento secuencial)

Todas son opcionales individualmente, pero **TRIGGERS como bloque es obligatorio**.
El separador `---` es obligatorio entre subbloques.

---

### 📋 Tabla resumen de triggers

| Sección      | Disparador              | Naturaleza              | Obligatorio |
|--------------|-------------------------|-------------------------|-------------|
| ON_EVENT     | Cambio externo          | Reactivo                | No          |
| ON_CONDITION | Estado del código       | Evaluación activa       | No          |
| ON_COMPLETE  | Skill terminó           | Encadenamiento          | Sí (en TRIGGERS) |
| ON_FAILURE   | Step falló              | Abort control           | Sí (en cada STEP) |

> **Nota:** ON_FAILURE NO pertenece a TRIGGERS. ON_FAILURE es obligatorio dentro de cada STEP del EXECUTION PLAN.

---

### 🚫 Reglas críticas de uso

1. **Un solo TRIGGERS por skill** — no duplicar bloques TRIGGERS
2. **ON_COMPLETE ≠ ON_EVENT** — si ON_EVENT ya dispara el mismo skill, eliminar ON_EVENT
3. **ON_COMPLETE ≠ POST-EXECUTION** — no existir como secciones separadas. Usar solo ON_COMPLETE dentro de TRIGGERS
4. **ON_FAILURE en cada STEP** — ningún STEP puede existir sin su ON_FAILURE (esto va en EXECUTION PLAN, no en TRIGGERS)
5. **Diferentes targets** — si un skill tiene ON_EVENT y ON_COMPLETE, deben apuntar a skills distintos

---

### 🔍 Guía de uso por sección

#### ON_EVENT

**Qué lo dispara:** Algo externo *sucedió* — un cambio observable en el proyecto.

**Naturaleza:** Reactivo.

**Ejemplo válido:**
```
EVENT: new_endpoint_created
→ APPLY SKILL: endpoint-testing-enforcer
```

**Cuándo usar:** Cuando un cambio en archivos, rutas, o comandos debe disparar otro skill automáticamente.

**No usar para:** Evaluar estado del código.

---

#### ON_CONDITION

**Qué lo dispara:** Algo es *verdadero* en el código actual — un patrón, estado o heurística detectable.

**Naturaleza:** Evaluación activa (el LLM analiza el código y decide).

**Ejemplo válido:**
```
IF module uses datagrid
→ APPLY SKILL: adaptive-datagrid-contract
```

**Cuándo usar:** Cuando la aplicación del skill depende de una condición que solo se puede verificar leyendo el código.

**No usar para:** Eventos de cambio (para eso existe ON_EVENT).

---

#### ON_COMPLETE

**Qué lo dispara:** El skill actual *terminó* de ejecutarse exitosamente.

**Naturaleza:** Encadenamiento secuencial.

**Ejemplo válido:**
```
→ APPLY SKILL: endpoint-testing-enforcer
```

**Cuándo usar:** Para definir el siguiente paso natural en un workflow. Cada skill encadena al que le corresponde.

**Regla crítica:** ON_COMPLETE NO debe duplicar otro trigger del mismo skill. Si ON_EVENT ya apunta a `skill-X`, ON_COMPLETE no debe apuntar a `skill-X` también.

---

# 6. Secciones Opcionales

Estas NO afectan ejecución pero pueden existir:

---

## 🟡 6.1 OVERVIEW

```md
## Overview

<free text>
```

---

## 🟡 6.2 DEFINITIONS

```md
## DEFINITIONS

datagrid_enabled = true | false
```

---

## 🟡 6.3 CONTEXT

```md
## CONTEXT

<additional structured context>
```

---

## 🟡 6.4 EXAMPLES

```md
## EXAMPLES

<code or usage>
```

---

## 🟡 6.5 NOTES (NO EJECUTABLE)

```md
## NOTES

<non-operational guidance>
```

---

# 7. Restricciones Globales (Críticas)

## ❌ 7.1 PROHIBIDO

* Texto libre dentro de EXECUTION PLAN
* Múltiples TYPE en un STEP
* Pasos sin `ON_FAILURE`
* Condiciones implícitas (todo debe ser `ONLY IF`)
* Mezclar documentación con ejecución

---

## ✅ 7.2 OBLIGATORIO

* Determinismo total
* Sin ambigüedad
* Parseable línea por línea
* Sin inferencias del LLM

---

# 8. Ejemplo Completo (Válido)

```md
---
name: module-implementation
description: Creates and registers a module
---

# SKILL_DEFINITION: module-implementer

## ACTIVATION (ENTRY GATE)

This SKILL MUST only be applied when ALL of the following conditions are true:

- The user wants to create a module
- Filesystem changes are required under modules/

---

If these conditions are NOT met:

→ DO NOT APPLY this SKILL  
→ STOP reading further instructions  
→ Continue with other relevant SKILLs

## EXECUTION PLAN (MANDATORY)

STEP 1: Create module

TYPE: COMMAND

COMMAND: node com dev make-module --name={module}

ON_FAILURE:
→ STOP
→ REPORT ERROR: Module creation failed

---

STEP 2: Register module

TYPE: ACTION

ACTION: Append "{module}" to config/modules.config.js

ON_FAILURE:
→ STOP
→ REPORT ERROR: Module registration failed

---

STEP 3: Validate module

TYPE: CHECK

CHECK:
- Module directory exists
- Module appears in config
- Routes load correctly

ON_FAILURE:
→ STOP
→ REPORT ERROR: Validation failed

## REQUIRES (HARD DEPENDENCIES)

These SKILLs MUST be active before this SKILL can execute:

- database-design-protocol
- code-naming-conventions-contract

If any are missing:
→ STOP
→ LOAD them
→ RESTART execution

## SKILL ORDER EXECUTION (ENFORCED SEQUENCE)

Apply dependencies in this exact order:

1. database-design-protocol
2. code-naming-conventions-contract

## TRIGGERS

### ON_EVENT

EVENT: schema_modified
→ APPLY SKILL: database-migrations

---

### ON_COMPLETE

→ APPLY SKILL: endpoint-testing-enforcer
```

---

# 9. Guidelines

When writing skills:

- Keep instructions **deterministic**
- Prefer **step-based behavior**
- Avoid ambiguous language
- Use **Markdown headings**
- Separate **decision rules** from **execution steps**
- Check for circular reference among two or more skills
- Check for skills with redundant/duplicate before write a new skill.

---

# 10. Goal

Ensure SKILL definitions are:

- consistent
- structured
- easily reusable
- compatible with the agent runtime

