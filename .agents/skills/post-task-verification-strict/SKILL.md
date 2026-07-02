---
name: post-task-verification-strict
description: Enforce strict post-task validation with fail-fast rules, intent alignment, encoding integrity, and scope control
---
```

# POST TASK VERIFICATION — STRICT MODE

## Objective

Guarantee that any completed task:

* strictly follows the declared intent
* does not modify out-of-scope elements
* preserves encoding and structural integrity
* does not introduce regressions

---

## Step 0 — Mode

```json
{
  "mode": "strict",
  "fail_on_scope_violation": true,
  "fail_on_encoding_issue": true,
  "fail_on_unexpected_side_effects": true
}
```

---

## Step 1 — Declare Intent Contract (MANDATORY)

Before verification, define:

```json
{
  "intent": "...",
  "allowed_files": [],
  "allowed_functions": [],
  "forbidden_files": [],
  "tolerance": {
    "allow_refactor": false,
    "allow_formatting": false,
    "allow_side_effects": false
  }
}
```

❗ Si esto no está → **FAIL automático**

---

## Step 2 — Change Detection (Git-backed)

### Preferred (no history pollution)

```bash
git diff --name-only
git diff
```

### Optional Strict Snapshot

```bash
git add -A
git commit -m "temp: verification snapshot"
git diff HEAD~1 HEAD
```

---

## Step 3 — Build Change Graph

```json
{
  "files": {
    "modified": [],
    "added": [],
    "deleted": []
  },
  "functions": {
    "modified": [],
    "added": [],
    "removed": []
  }
}
```

---

## Step 4 — HARD VALIDATIONS (Fail Fast)

### 4.1 Scope Violation

FAIL if:

* file ∉ allowed_files
* file ∈ forbidden_files

---

### 4.2 Missing Expected Changes

FAIL if:

* expected file/function NOT modified

---

### 4.3 Overreach Detection

FAIL if:

* unrelated functions modified
* large diff outside intent

---

### 4.4 Duplicate / UI Regression Heuristics

FAIL if detected:

* duplicate buttons (e.g. Save)
* duplicated handlers
* conflicting bindings

---

## Step 5 — Encoding Integrity (CRITICAL)

Check:

* UTF-8 validity
* No replacement chars: `�`
* No mixed encodings
* Consistent line endings

FAIL if:

* encoding inconsistency detected
* BOM introduced unexpectedly

---

## Step 6 — Naming Convention Enforcement (Your SKILL standard)

FAIL if violations:

* files not kebab-case
* classes not PascalCase
* functions/vars not camelCase
* DB not snake_case

---

## Step 7 — Side Effects Analysis

FAIL if:

* changes propagate outside declared scope
* hidden dependency breakage likely

---

## Step 8 — Risk Scoring

```json
{
  "status": "OK | FAIL",
  "fail_reasons": [],
  "warnings": [],
  "confidence": 0-100
}
```

---

## Step 9 — MANDATORY FAILURE BEHAVIOR

If `status = FAIL`:

The agent MUST:

1. Stop execution
2. Explicitly list violations
3. Suggest minimal rollback or fix
4. NOT continue with further tasks

---

## Step 10 — Final Report

```
## STRICT VERIFICATION REPORT

### Intent Contract
...

### Detected Changes
...

### Violations
...

### Encoding Status
...

### Naming Compliance
...

### Verdict
✅ OK / ❌ FAIL
```

---
