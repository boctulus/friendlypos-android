---
name: skill-orchestrator
description: Orchestrates the execution of all other SKILLs, ensuring proper order, dependency management, and enforcement of preconditions and postconditions
---

# SKILL_DEFINITION: skill-orchestrator

## ACTIVATION (ENTRY GATE)

This SKILL MUST only be applied when ALL of the following conditions are true:

- Any task requiring code changes, file creation, or refactoring is requested
- Multiple SKILLs could potentially apply to the same problem
- Global execution order and dependency enforcement is needed

---

If these conditions are NOT met:

→ DO NOT APPLY this SKILL
→ STOP reading further instructions
→ Continue with other relevant SKILLs

## EXECUTION PLAN (MANDATORY)

STEP 1: Load global preconditions

TYPE: ACTION

ACTION: Apply mandatory pre-execution SKILLs in order:
1. anti-hallucination-project-guard
2. context-sanitizer-contract

ON_FAILURE:
→ STOP
→ REPORT ERROR: Global preconditions could not be loaded

---

STEP 2: Detect applicable SKILLs

TYPE: CHECK

CHECK:
- Identify all SKILLs that match the current task
- Evaluate ON_CONDITION triggers for each candidate
- Check for conflicts between multiple applicable SKILLs

ON_FAILURE:
→ STOP
→ REPORT ERROR: Could not detect applicable SKILLs

---

STEP 3: Resolve conflicts and establish order

TYPE: ACTION

ACTION: Apply resolution strategy:
1. Safety (guards) first
2. Data integrity second
3. Contracts third
4. UI last
- Enforce dependency order via REQUIRES and SKILL ORDER EXECUTION sections

ON_FAILURE:
→ STOP
→ REPORT ERROR: Conflict resolution failed

---

STEP 4: Execute SKILLs in order

TYPE: ACTION

ACTION: For each SKILL in resolved order:
- Verify PRE-conditions (ACTIVATION ENTRY GATE)
- Execute EXECUTION PLAN steps sequentially
- Verify POST-conditions (ON_COMPLETE triggers)
- Never execute a SKILL in isolation
- Always check dependencies before execution

ON_FAILURE:
→ STOP
→ REPORT ERROR: SKILL execution failed

## REQUIRES (HARD DEPENDENCIES)

These SKILLs MUST be active before this SKILL can execute:

- anti-hallucination-project-guard
- context-sanitizer-contract

If any are missing:
→ STOP
→ LOAD them
→ RESTART execution

## SKILL ORDER EXECUTION (ENFORCED SEQUENCE)

Apply dependencies in this exact order:

1. anti-hallucination-project-guard
2. context-sanitizer-contract

## TRIGGERS

### ON_EVENT

EVENT: task_started
→ APPLY SKILL: skill-orchestrator

EVENT: code_change_requested
→ APPLY SKILL: skill-orchestrator

---

### ON_CONDITION

IF multiple SKILLs apply to the same task
→ APPLY SKILL: skill-orchestrator

IF dependency resolution is needed
→ APPLY SKILL: skill-orchestrator

---

### ON_COMPLETE

→ APPLY SKILL: skill-reviewer-protocol

---

## BEST PRACTICES

Be careful to avoid execute skills in circular dependencies. Always check the REQUIRES and SKILL ORDER EXECUTION sections before applying any skill.