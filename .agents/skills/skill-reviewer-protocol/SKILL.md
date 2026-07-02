---
name: skill-reviewer-protocol
description: Auditoría, corrección y normalización de skills defectuosos o incompletos
---

# SKILL: skill-reviewer-protocol

## ACTIVATION (ENTRY GATE)
This SKILL MUST only be applied when ALL of the following conditions are true:
- A skill file exists in `.agents/skills/` or `<agent>/skills/`
- The skill requires validation, correction, or structural normalization
- Structural integrity, header compliance, or naming consistency is in question

If conditions are NOT met:
→ ABORT this SKILL
→ CONTINUE with other relevant SKILLs

---

## EXECUTION PLAN (MANDATORY)

### STEP 1: Pre-Execution Audit
TYPE: COMMAND
COMMAND: node com skill audit --agent=agent
ON_FAILURE:
→ HALT
→ LOG: "Skill audit command failed. Check CLI environment or permissions."
→ EXIT

---

### STEP 2: Classify & Triage
TYPE: CHECK
CHECK:
- Missing or malformed YAML frontmatter (`---`)
- Missing, invalid, or non-kebab-case `name` field
- Empty or missing `description`
- Filename ≠ `name` value
- Missing mandatory sections (see STEP 3)
- Non-UTF-8 encoding or BOM presence

OUTPUT: Structured list of affected skills + issue tags.
ON_FAILURE:
→ LOG: "Classification partial. Proceeding with identifiable issues only."
→ CONTINUE to STEP 3 (skip auto-fix for ambiguous cases)

---

### STEP 3: Remediation & Normalization
TYPE: ACTION
ACTION: For each invalid skill:
1. Inject/repair YAML header: `name: <kebab-case>`, `description: <clear, actionable text>`
2. Rename file to match `name` exactly (`.md` extension)
3. Enforce minimum structure:
   ```
   # SKILL: <name>
   ## ACTIVATION
   ## EXECUTION PLAN
   ## REQUIRES
   ## TRIGGERS
   ```
4. Strip BOM, enforce UTF-8
5. Preserve existing content under appropriate sections
ON_FAILURE:
→ HALT
→ LOG: "Remediation failed for <skill-name>. Manual intervention required."
→ SKIP to next skill

---

### STEP 4: Post-Fix Validation
TYPE: COMMAND
COMMAND: node com skill audit --agent=agent
CHECK:
- Output matches: `✅ All skills passed structural validation`
- Zero encoding warnings
- All filenames aligned with `name` fields
ON_FAILURE:
→ LOG: "Validation post-fix failed. Review STEP 3 outputs."
→ EXIT

---

## Tooling
Use `node com` commands as primary automation layer. Fallback to manual parsing if CLI unavailable:
```bash
node com skill list --detailed
node com skill audit --agent=agent
node com skill dependency-tree --agent=agent
```
⚠️ Tooling complements but does not replace structural validation logic.

---

## REQUIRES (HARD DEPENDENCIES)
- `anti-hallucination-project-guard`
- `code-naming-conventions-contract`

PRE-FLIGHT CHECK:
→ Verify dependencies are loaded and active.
→ IF MISSING: `HALT` → `REQUEST DEPENDENCY INJECTION` → `AWAIT READY SIGNAL` → `RESTART STEP 1`

---

## SKILL ORDER EXECUTION
1. `anti-hallucination-project-guard`
2. `code-naming-conventions-contract`
3. `skill-reviewer-protocol` (this SKILL)

---

## TRIGGERS

### ON_EVENT
- `skill_file_created` → APPLY
- `skill_file_modified` → APPLY
- `skill_dependency_updated` → APPLY

### ON_CONDITION
- `audit.status == "warnings" || audit.status == "errors"` → APPLY
- `skill.metadata.completeness < 80%` → APPLY

### ON_COMPLETE
→ EMIT: `skill_normalization_report.json`
→ OPTIONALLY APPLY: `skill-conflict-resolution-protocol` (if version/header conflicts detected)

