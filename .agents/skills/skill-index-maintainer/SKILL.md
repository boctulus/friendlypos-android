---
name: skill-index-maintainer
description: Crea y mantiene el archivo `.agents/skills/index.md` — el índice maestro optimizado para LLM que define qué skill invocar en cada caso.
---

# SKILL_DEFINITION: skill-index-maintainer

## ACTIVATION (ENTRY GATE)

This SKILL MUST only be applied when ALL of the following conditions are true:

- The user requests to create, update, regenerate, or audit the `index.md` file
- The task involves maintaining the master routing guide for LLM skill invocation

---

If these conditions are NOT met:

→ DO NOT APPLY this SKILL
→ Return control immediately
→ Continue with other relevant SKILLs

## EXECUTION PLAN (MANDATORY)

STEP 1: Determine operation mode

TYPE: CHECK

CHECK:
- Check if `.agents/skills/index.md` exists
- Identify trigger: new creation, partial update, dependency tree regeneration, or full audit
- Map to one of: CREATE_FROM_ZERO, PARTIAL_UPDATE, REGENERATE_TREES, FULL_AUDIT

ON_FAILURE:
→ STOP
→ REPORT ERROR: Cannot determine operation mode

---

STEP 2: Collect skill information

TYPE: COMMAND

COMMAND: node com skill dependency-tree --agent=agent

ON_FAILURE:
→ FALLBACK: Infer dependencies from SKILL.md references in `.agents/skills/*/SKILL.md`
→ Mark trees as "inferred (non-authoritative)"
→ CONTINUE to STEP 3 with warning

---

STEP 3: Read each SKILL.md to extract metadata

TYPE: ACTION

ACTION: For each skill in `.agents/skills/`, read its `SKILL.md` and extract: name (from frontmatter), trigger (from frontmatter description), anti-trigger (MUST be explicit in frontmatter under `antiTriggers:` key), is_workflow (from dependency-tree output or inferred), dependencies (list of sub-skills)

NOTE: If `antiTriggers` is missing from a skill's frontmatter:
→ WARN: Missing anti-trigger for skill `{skill-name}` (do not infer automatically)
→ Continue but flag in final summary

ON_FAILURE:
→ STOP
→ REPORT ERROR: Failed to read skill metadata

---

STEP 4: Classify skills

TYPE: ACTION

ACTION: Group skills into three categories:
- WORKFLOW → Skills that orchestrate others, have mandatory children in dependency tree
- COMPOSABLE → Skills that may have dependencies but do not force them (optional sub-skills)
- STANDALONE → Skills without dependencies or invoked directly

ON_FAILURE:
→ STOP
→ REPORT ERROR: Skill classification failed

---

STEP 5: Build or update index.md

TYPE: ACTION

ACTION: Write `.agents/skills/index.md` using the canonical structure: (1) Header + Purpose, (2) Quick Decision Tree, (3) Tables by category (UI, API, DB, Infrastructure, Print, Docs, CLI), (4) Workflow Dependency Trees in ASCII, (5) Standalone Skills list alphabetically, (6) Anti-False-Positive Rules (5 rules). For partial updates, only modify affected sections. For dependency tree regeneration, run `node com skill dependency-tree --agent=agent` and compare against current ASCII trees, rewriting changed trees with proper `├──`, `│`, `└──` indentation (4 spaces per level). For full audit, verify all checklist items

ON_FAILURE:
→ STOP
→ REPORT ERROR: Failed to build or update index.md

---

STEP 6: Validate output

TYPE: CHECK

CHECK:
- All skills in `.agents/skills/` are represented in the index
- No deleted skill appears in the index
- Each workflow has its ASCII tree matching `dependency-tree` output
- Each skill has at least one "Do NOT Invoke When" row
- Quick Decision Tree covers all workflows (CHECK: Each workflow appears at least once in Quick Decision Tree)
- Standalone list is alphabetically sorted
- Document prefers <600 lines; if exceeded → split trees into references ONLY if >20 workflows

ON_FAILURE:
→ STOP
→ REPORT ERROR: Validation failed — index has inconsistencies

---

STEP 7: Report summary to user

TYPE: ACTION

ACTION: Show change summary: skills added (N), skills removed (N), trees updated (N), anti-FP rules affected (N). If Quick Decision Tree changed, highlight it explicitly. Ask user if they want a full audit (STEP 5 checklist) for final validation

ON_FAILURE:
→ STOP
→ REPORT ERROR: Failed to generate summary

## REQUIRES (HARD DEPENDENCIES)

These SKILLs MUST be active before this SKILL can execute:

- skill-reviewer-protocol

If any are missing:
→ REPORT missing dependency
→ SUGGEST loading: skill-reviewer-protocol
→ HALT

## SKILL ORDER EXECUTION (ENFORCED SEQUENCE)

Apply dependencies in this exact order:

1. skill-reviewer-protocol

## TRIGGERS

### ON_EVENT

EVENT: skill_added_or_removed
→ APPLY SKILL: skill-reviewer-protocol

---

### ON_COMPLETE

→ APPLY SKILL: post-task-verification

---

## FULL AUDIT

Verificar en orden:
- [ ] Todos los skills en `.agents/skills/` están representados en el índice
- [ ] Ningún skill eliminado aparece en el índice
- [ ] Cada workflow tiene su árbol ASCII y coincide con `dependency-tree`
- [ ] Cada skill tiene al menos una fila "Do NOT Invoke When"
- [ ] El Quick Decision Tree cubre todos los workflows (cada workflow aparece al menos una vez)
- [ ] La Standalone Skills list está ordenada alfabéticamente
- [ ] Las 5 Anti-False-Positive Rules siguen siendo válidas
- [ ] El documento prefiere <600 líneas; si excede → mover árboles a `references/dependency-trees.md` SOLO si >20 workflows

Reportar al usuario:
- ✅ Items correctos
- ⚠️ Items con discrepancias (con diff específico)
- 🔴 Items críticos que rompen el routing

ON_FAILURE:
→ STOP
→ REPORT ERROR: Failed to build or update index.md

---

## WRITING RULES

**Tablas — cada fila debe cumplir:**
- **Invoke When** — condición concreta (verbo + objeto). Ej: *"Creating a new reusable UI component"*, no *"when components are needed"*
- **Do NOT Invoke When** — referencia al skill que SÍ aplica, o condición excluyente. Ej: *"`view-lifecycle-protocol` is already active"* / *"Already covered by `[workflow]`"*

**Formato:**
- Emojis por categoría en `###`: 🔵 UI, 🟢 API, 🟠 DB, 🔴 Infra, 🟣 Print, ⚪ Docs
- Árboles ASCII: `├──`, `│   `, `└──`, 4 espacios por nivel
- Skills compartidos: anotar `← shared (ejecuta una sola vez)`
- Standalone list: alfabética, backticks, un item por línea
- Preferir <600 líneas; si excede → mover árboles a `references/dependency-trees.md` SOLO si >20 workflows
- Clasificación: WORKFLOW, COMPOSABLE, STANDALONE

---

## MINI-EXAMPLES

### Mini-ejemplo A — Quick Decision Tree

```
User request → What kind of task is this?

├─ "Create a new module"
│   └─ → module-implementation
│
├─ "Create or refactor a view/UI"
│   ├─ New view?   → view-lifecycle-protocol
│   └─ Refactor?   → view-refactoring-module
│
└─ None of the above?
    └─ ↓ Check specialized skills below ↓
```

### Mini-ejemplo B — Workflow Dependency Tree

```
endpoint-lifecycle-protocol
├── api-input-validation-contract
│   └── api-error-handling-contract
│       ├── code-naming-conventions-contract
│       └── cross-layer-naming-consistency-contract
├── api-error-handling-contract   ← shared (ejecuta una sola vez)
└── endpoint-testing-enforcer
```

### Mini-ejemplo C — Standalone Skills list

```md
### Standalone Skills (No Dependencies)
These skills work independently — invoke directly when their trigger is met:

- `alert-usage-contract`
- `database-migrations`
- `git-safety-protocol`
- `post-task-verification`
```

### Mini-ejemplo C.1 — Composable Skills list

```md
### Composable Skills (Optional Dependencies)
These skills may have sub-skills but do not force them — invoke when their specific trigger is met:

- `non-blocking-enrichment`
- `push-notifications-architecture`
```

### Mini-ejemplo D — Anti-False-Positive Rules

```md
## Anti-False-Positive Rules

1. **Workflow absorbs sub-skills** — Si un workflow está activo, sus sub-skills están implícitamente
   invocados. No los llames por separado.
2. **Single-responsibility trigger** — Solo invoca un skill cuando su condición específica se cumple.
3. **Existing vs New** — Skills de refactoring son solo para código existente. Para código nuevo,
   usa el lifecycle correspondiente.
4. **No double-invocation** — Si un sub-skill aparece en múltiples workflows activos, ejecuta solo una vez.
5. **Context matters** — `database-design-protocol` es solo para selección de DB en módulos nuevos.
   No lo invoques al agregar tablas a módulos existentes.
```

