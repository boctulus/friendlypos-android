---
name: anti-hallucination-project-guard
description: Enforce strict alignment with the real project structure. Prevents hallucinated modules, files, commands, or architecture expansions. Forces verification-first workflow using the filesystem as the single source of truth.
---

# PURPOSE

Eliminate hallucinations when working on real codebases by enforcing:

- Zero invention of files, modules, or architecture
- Filesystem as the only source of truth
- Verification before implementation
- Explicit handling of missing information

---

# CORE PRINCIPLE

> If it is not explicitly present, it does not exist.

---

# HARD RULES (NON-NEGOTIABLE)

## 1. FILESYSTEM IS SOURCE OF TRUTH

- Only use files, modules, and folders explicitly provided
- Never assume structure based on patterns
- Never infer existence from naming conventions

Forbidden:
- “There should be a module for…”
- “Typically this project would have…”
- “Let’s create X to handle this” (unless explicitly requested)

---

## 2. ZERO INVENTION POLICY

You MUST NOT:

- Create non-existing modules when not explicitly asked by the user.
- Reference undefined classes/functions/files
- Expand architecture implicitly
- Auto-complete missing layers (services, controllers, etc.)

If something is missing:

→ STOP  
→ ASK or DECLARE MISSING

---

## 3. VERIFICATION-FIRST WORKFLOW (MANDATORY)

Before writing ANY code:

### Step 1 — Inventory

List all relevant existing elements:

- Files
- Modules
- Classes
- Entry points

If inventory is incomplete:

→ DO NOT PROCEED

---

### Step 2 — Scope Lock

Define explicitly:

- What exists
- What will be used
- What will NOT be assumed

---

### Step 3 — Gap Detection

Identify missing pieces:

- Dependencies not found
- Undefined modules
- Incomplete contracts

---

### Step 4 — Explicit Decision

For each gap:

- ASK for clarification  
OR  
- PROCEED with a constrained fallback (only if safe)

---

### Step 5 — Implementation

Only after all previous steps are resolved.

---

## 4. NO ARCHITECTURAL EXPANSION

Do NOT:

- Introduce new patterns (MVC layers, services, etc.)
- Restructure directories
- Add abstractions not already present

Unless explicitly requested:

→ “Design X”  
→ “Refactor architecture”

---

## 5. STRICT NAME ALIGNMENT

- Use exact names as provided
- Do not normalize, translate, or “improve” naming
- Do not create variants

Example forbidden:
- `PrinterService` if only `printer.js` exists

---

## 6. FAIL > INVENT

If uncertain:

- Prefer stopping over guessing
- Prefer partial answer over fabricated completeness

---

## 7. DATASOURCE SOURCE OF TRUTH (`config/databases.config.js`)

Before assuming where an entity/collection lives (PostgreSQL/Supabase vs Firebase/Firestore),
you MUST read the backend file:

```
config/databases.config.js
```

This file is the **single source of truth** for the datasource of each entity.

Hard rules:

- An entity MAY physically exist in BOTH PostgreSQL and Firebase due to history or
  synchronization. This does NOT make either one the source of truth.
- The authoritative datasource is ONLY the one declared in `config/databases.config.js`.
- NEVER infer the datasource from:
  - The route prefix (e.g. `/api/firestore/...` does NOT imply Firestore)
  - Model names, table names, or collection names
  - The presence of data in one store
- If `config/databases.config.js` is not available or the entity is not listed:
  → STOP and ASK. Do NOT guess.

Forbidden:
- “It's under `/firestore/`, so it's in Firestore.”
- “There's a Firestore sample, so sales live in Firebase.”

Required:
- “`config/databases.config.js` maps `sales → supabase`; that is the source of truth.”

---

# EXECUTION MODES

## MODE: SAFE IMPLEMENTATION (DEFAULT)

- Full verification-first workflow
- No assumptions allowed
- Missing data blocks execution

---

## MODE: ASSISTED (EXPLICIT ONLY)

Activated only if user says:

- “You can assume…”
- “Create missing parts…”

Even in this mode:

- All assumptions must be explicitly declared
- No hidden invention allowed

---

# OUTPUT PROTOCOL

When working on a task, structure response as:

## 1. VERIFIED CONTEXT

List only confirmed elements

## 2. MISSING / UNCERTAIN

Explicit gaps

## 3. DECISION

- Ask
OR
- Proceed with constraints

## 4. IMPLEMENTATION

Strictly aligned with verified context

---

# FAILURE PATTERNS TO AVOID

- Pattern completion from similar projects
- Mixing contexts from previous tasks
- “Helpful” auto-expansion
- Filling gaps with best practices

---

# SELF-CHECK (MANDATORY BEFORE OUTPUT)

Before responding, validate:

- Did I reference anything not explicitly provided?
- Did I assume a file/module exists?
- Did I introduce new architecture?
- Did I rename or normalize anything?

If ANY answer is YES:

→ REVISE

---

# EXAMPLE (GOOD)

User provides:
- `/modules/printer/printer.js`

Valid:
- Works only within `printer.js`
- Asks if strategy layer exists before creating one

---

# EXAMPLE (BAD)

User provides:
- `/modules/printer/printer.js`

Invalid:
- Creates `PrinterService.js`
- Adds `/strategies/escpos/`
- Introduces new architecture without request

---

# PHILOSOPHY

This SKILL prioritizes:

- Correctness over completeness
- Determinism over creativity
- Real system alignment over ideal architecture