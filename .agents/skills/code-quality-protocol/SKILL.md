---
name: code-quality-protocol
description: Orchestrates the code quality lifecycle: anti-hallucination guard → defensive refactoring → naming conventions → cross-layer naming consistency → skill review.
---

# SKILL_DEFINITION: code-quality-protocol

## ACTIVATION (ENTRY GATE)

This SKILL MUST only be applied when ALL of the following conditions are true:

- Existing production code is being refactored or modified
- The change involves structural improvements without behavior changes
- File or directory naming may be affected
- Cross-layer identifier consistency must be maintained

---

If these conditions are NOT met:

→ DO NOT APPLY this SKILL
→ STOP reading further instructions
→ Continue with other relevant SKILLs

## EXECUTION PLAN (MANDATORY)

STEP 1: Verify against real project structure

TYPE: ACTION

ACTION: Enforce anti-hallucination-project-guard — inventory existing files/modules, verify all references point to real filesystem elements, declare missing pieces explicitly, do not invent architecture

ON_FAILURE:
→ STOP
→ REPORT ERROR: Project structure verification failed

---

STEP 2: Apply defensive refactoring

TYPE: ACTION

ACTION: Enforce code-defensive-refactoring — create backup before any modification, apply changes in phases (structural → behavioral), preserve semantic meaning, validate context integrity against backup

ON_FAILURE:
→ STOP
→ REPORT ERROR: Defensive refactoring could not be applied

---

STEP 3: Enforce file naming conventions

TYPE: ACTION

ACTION: Enforce code-naming-conventions-contract — all files use kebab-case, classes use PascalCase, variables/functions use camelCase, DB uses snake_case, no fallback mappings, no tolerant layers

ON_FAILURE:
→ STOP
→ REPORT ERROR: Naming conventions could not be enforced

---

STEP 4: Enforce cross-layer naming consistency

TYPE: ACTION

ACTION: Enforce cross-layer-naming-consistency-contract — same concept has same name across all layers (frontend, API, backend, DB), all identifiers in English, no synonyms or mapping layers

ON_FAILURE:
→ STOP
→ REPORT ERROR: Cross-layer naming consistency check failed

---

STEP 5: Review and validate

TYPE: CHECK

CHECK:
- All referenced files/modules exist in filesystem
- Backups created before modifications are intact
- No critical identifiers changed unintentionally
- Same concept uses same name across all layers
- No Spanish identifiers in code (only in visible UI text)
- File names use kebab-case
- Public contracts remain stable

ON_FAILURE:
→ STOP
→ REPORT ERROR: Code quality validation failed

## REQUIRES (HARD DEPENDENCIES)

These SKILLs MUST be active before this SKILL can execute:

- anti-hallucination-project-guard
- code-defensive-refactoring
- code-naming-conventions-contract
- cross-layer-naming-consistency-contract
- skill-reviewer-protocol

If any are missing:
→ STOP
→ LOAD them
→ RESTART execution

## SKILL ORDER EXECUTION (ENFORCED SEQUENCE)

Apply dependencies in this exact order:

1. anti-hallucination-project-guard
2. code-defensive-refactoring
3. code-naming-conventions-contract
4. cross-layer-naming-consistency-contract
5. skill-reviewer-protocol

## TRIGGERS

### ON_EVENT

EVENT: code_refactor_requested
→ APPLY SKILL: code-quality-protocol

---

### ON_CONDITION

IF existing code has naming inconsistencies
→ APPLY SKILL: code-quality-protocol

---

### ON_COMPLETE

→ APPLY SKILL: skill-reviewer-protocol
