---
name: prompt-execution-control-protocol
description: Guides about how to suspend an user prompt execution
---

# SKILL_DEFINITION: Suspend_Task

### 1. DESCRIPTION

A protocol to safely pause the execution of complex tasks. It ensures preservation of the current state, logic flow, and memory by serializing context and defining specific resumption points in the file system.

### 2. TRIGGERS

This skill is activated when the user explicitly uses the command:

* `"suspend"`

or the user express that in Spanish using words as "suspende" o "suspender"

### 3. WORKFLOW

Upon activation, perform the following sequence strictly:

**Step 1: Serialize Context**

* Capture the current state of the conversation and memory efficiently.
* **Action:** Save this data as a `.md` file.
* **Path:** `docs\contexts\`

**Step 2: Document In-Progress State**

* Generate a status report containing:
1. Steps that were actively being executed.
2. The specific step where the suspension occurred.
3. The expected result/outcome to be obtained when the task resumes.


* **Action:** Save this as a `.md` file.
* **Path:** `docs\to-do\in-progress`

**Step 3: Cross-Reference**

* **Action:** Update the file created in **Step 1** (`docs\contexts\`) to include a direct reference link to the file created in **Step 2** (`docs\to-do\in-progress`).

### 4. CONSTRAINTS & STANDARDS

* **File Naming:** Must be descriptive, clearly indicating the suspended task. Append date/time if necessary to ensure uniqueness.
* **Goal:** Ensure zero data loss and allow future resumption without needing to re-explain the context.

---

### Por qué este formato funciona mejor para un LLM:

1. **Etiquetas Claras (DESCRIPTION, TRIGGERS):** El modelo sabe exactamente qué es la habilidad y qué palabra la detona.
2. **Workflow Secuencial:** Al numerar los pasos ("Step 1", "Step 2"), fuerzas al modelo a seguir un orden lógico y no saltarse la creación de archivos.
3. **Rutas Explícitas:** Se separan las acciones abstractas ("Capture state") de las acciones concretas de sistema ("Save to path...").

**¿Te gustaría que genere un ejemplo de cómo se vería el archivo `.md` resultante de una suspensión?**