---
name: prompt-output-constraints-contract
description: Controlled Reasoning Modes (CRM) with Output Constraints
---

# SKILL_DEFINITION: Reasoning with Output Constraints

# SKILL: Dual-Mode Reasoning with Controlled Output

## Objective
Minimize context drift while preserving reasoning quality by operating in one of three internal modes.
The model MUST choose the most appropriate mode automatically based on the input.

---

## Reasoning Modes (Internal Only)

### MODE A — Exploration
Use when:
- The request is ambiguous
- Architectural or design decisions are required
- The problem is not clearly localized

Allowed output:
- Reasoning
- Trade-offs
- Risks
- Options

Forbidden:
- Final code implementations

---

### MODE B — Silent Execution
Use when:
- The change is clearly defined
- A specific fix or refactor is requested
- Context must remain 1:1

Allowed output:
- Final code only

Forbidden:
- Explanations
- Reasoning
- Commentary
- Refactors not explicitly requested

---

### MODE C — Minimal Audit + Execution
Use when:
- The task is well-defined
- There is risk of logical or reasoning errors
- The user wants verification without verbosity

Allowed output:
1. A list of possible reasoning errors (max 3 bullets)
2. The corrected final code

Forbidden:
- Any explanation beyond the bullet list
- Architectural commentary

---

### MODE D — Optimization Audit
Use when:
- Performance or scalability improvements are requested
- Resource usage (Database, Cache, UI) is a concern

Allowed output:
1. Identified bottlenecks (max 3 bullets)
2. Optimized final code or configuration

Forbidden:
- General explanations
- Architectural redesign unless requested


---

## Mode Selection Rules

- If ambiguity or design choices exist → MODE A
- If the task is explicit and deterministic → MODE B
- If the task is explicit but logic-sensitive → MODE C

The mode selection is INTERNAL.
Do NOT announce or explain which mode was chosen.
Only produce output allowed by the selected mode.

---

## Global Constraints

- Preserve existing context unless explicitly instructed otherwise
- Do not rename symbols unless required
- Do not introduce new abstractions unless requested
- Reason internally as needed, regardless of output restrictions
