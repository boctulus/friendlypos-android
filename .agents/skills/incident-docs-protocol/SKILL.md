---
name: incident-docs-protocol
description: Use when documenting a production bug, incident, or error. Separates incident-specific context from reusable knowledge.
---

# SKILL_DEFINITION: Dual Layer Documentation

Forces strict separation between:

- Contextual (non-portable) information
- Reusable (portable) knowledge

---

# 1. Core Principle

Documentation MUST NEVER mix:

- Real-world incident data
- Reusable solutions

---

# 2. Layer Definitions

## 2.1 Incident Layer

Used for:

- Production bugs
- Logs
- Real errors

Allowed:

- Real paths
- Real names
- Exact logs

Requirements:

- Must be clearly labeled
- Must NOT contain reusable code

---

## 2.2 Knowledge Layer

Used for:

- Fixes
- Patterns
- Architecture

Must follow:

- context-sanitizer-contract (STRICT)

---

# 3. Mandatory Structure

All docs MUST follow:

````

## Incident (Contextual)

## Root Cause (Abstracted)

## Reusable Fix (Sanitized)

## Portable Validation

````

---

# 4. Validation Rules

Before output:

* Are layers separated?
* Is reusable code sanitized?
* Is incident clearly marked?

If not → rewrite.

---

# 5. Failure Modes

## 5.1 Mixed Layers

Most common error → MUST fix

