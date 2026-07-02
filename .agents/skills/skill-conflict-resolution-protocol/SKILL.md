---
name: skill-conflict-resolution-protocol
description: Detectar, prevenir y resolver conflictos entre SKILLs que puedan generar ambigüedad en decisiones, degradación de calidad o comportamientos inconsistentes del agente
---

# SKILL_DEFINITION: skill-conflict-resolution-protocol

## ACTIVATION (ENTRY GATE)

This SKILL MUST only be applied when ALL of the following conditions are true:

- Multiple SKILLs could apply to the same problem or task
- There is potential for conflicting rules, priorities, or scopes
- Code generation, refactoring, or architecture decisions are being made

---

If these conditions are NOT met:

→ DO NOT APPLY this SKILL
→ STOP reading further instructions
→ Continue with other relevant SKILLs

## EXECUTION PLAN (MANDATORY)

STEP 1: Detect overlap between applicable SKILLs

TYPE: CHECK

CHECK:
- Identify ALL SKILLs that match the current task
- Read each SKILL's ACTIVATION conditions and EXECUTION PLAN
- List rules, constraints, and behaviors from each SKILL
- Identify incompatible or contradictory directives

ON_FAILURE:
→ STOP
→ REPORT ERROR: Could not detect SKILL overlap

---

STEP 2: Classify conflict type

TYPE: CHECK

CHECK:
- **Direct Conflict**: Two SKILLs indicate incompatible actions
- **Priority Conflict**: Both apply but one should prevail (performance vs readability, security vs convenience)
- **Scope Conflict**: One SKILL applies globally, another locally
- **Redundant Enforcement**: Two SKILLs cover the same area with different rules

ON_FAILURE:
→ STOP
→ REPORT ERROR: Could not classify conflict type

---

STEP 3: Apply resolution rules

TYPE: ACTION

ACTION: Apply resolution rules in order of priority:

**Rule 1 — Safety > Everything**
Always wins: data integrity, no breaking production, avoid information loss
Example: database-schema-guard > any refactor

**Rule 2 — Explicit Contract > Convention**
Defined contracts have priority over conventions
Example: cli-light-contracts > code-naming-conventions-contract

**Rule 3 — Local Context > Global Rule**
If a SKILL is context-specific, it wins
Example: module-implementation > generic rules

**Rule 4 — New Code > Legacy Preservation (CONTROLLED)**
In new code: apply strict standards
In existing code: respect stability

**Rule 5 — Eliminate Duplication**
If two SKILLs do the same thing: use one as source of truth, ignore or delegate the other

ON_FAILURE:
→ STOP
→ REPORT ERROR: Resolution rules could not be applied

---

STEP 4: Enforce decision

TYPE: ACTION

ACTION:
- Choose one clear strategy
- DO NOT mix rules partially
- DO NOT create silent fallbacks
- Document (internally) which rule wins and why

ON_FAILURE:
→ STOP
→ REPORT ERROR: Decision enforcement failed

## REQUIRES (HARD DEPENDENCIES)

These SKILLs MUST be active before this SKILL can execute:

- anti-hallucination-project-guard

If any are missing:
→ STOP
→ LOAD them
→ RESTART execution

## SKILL ORDER EXECUTION (ENFORCED SEQUENCE)

Apply dependencies in this exact order:

1. anti-hallucination-project-guard

## TRIGGERS

### ON_EVENT

EVENT: multiple_skills_detected_for_task
→ APPLY SKILL: skill-conflict-resolution-protocol

---

### ON_CONDITION

IF two or more SKILLs have overlapping rules
→ APPLY SKILL: skill-conflict-resolution-protocol

IF SKILL priority is ambiguous
→ APPLY SKILL: skill-conflict-resolution-protocol

IF scope conflict exists between global and local SKILLs
→ APPLY SKILL: skill-conflict-resolution-protocol

---

### ON_COMPLETE

→ APPLY SKILL: skill-reviewer-protocol
