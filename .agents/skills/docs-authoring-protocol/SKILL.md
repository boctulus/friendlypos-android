---
name: docs-authoring-protocol
description: Rules for creating, updating, and classifying project documentation to minimize context drift
---

# SKILL_DEFINITION: documentation

## Purpose

This SKILL defines **how documentation must be written and classified** in the project.

---

## Decision Rule (MANDATORY)

When creating or updating a document, classify it using **exactly one** of the following:

- **SKILL**
  → If the document explains **how to act without breaking a rule**
  → Normative, prescriptive, repeatable  
  → Example: “How views must be structured”, “How modules are generated”

- **DOC**
  → If the document explains **how to investigate, verify, debug, or validate**
  → Procedural, diagnostic, exploratory  
  → Example: “How to trace a rendering issue”, “How to verify a build”

- **ISSUE**
  → If the document explains **what happened once**
  → Historical, contextual, non-repeatable  
  → Example: “Bug caused by incorrect SDK version on POS device X”

If classification is unclear → **DO NOT CREATE THE DOCUMENT**

---

## Writing Rules

All documents MUST follow these rules:

- Be **short**
- Be **explicit**
- Be **actionable**
- Avoid narrative, motivation, or background
- Avoid redundancy with existing documents
- Avoid subjective language

The document should answer:
> “What must an LLM do or avoid doing?”

Not:
> “Why this exists”  
> “What we learned”  
> “How someone felt”

---

## Anti-Context-Drift Rules

Documentation MUST:

- Encode **constraints**, not explanations
- Prefer **rules over examples**
- Prefer **lists over paragraphs**
- Prefer **imperatives over descriptions**

Avoid:
- Historical context
- Comparative discussions
- Long explanations
- Cross-document storytelling

Each document must be **locally complete** and **globally minimal**.

---

## Update Rules

- Do NOT append explanations
- Do NOT expand scope
- Do NOT rephrase for clarity unless behavior changes
- If behavior changes → update rules only
- If behavior is no longer valid → delete the document

Versioning is implicit through git history, not prose.

---

## Validation Checklist

Before committing documentation, verify:

- [ ] Classified as SKILL / DOC / ISSUE
- [ ] Less than necessary words
- [ ] No historical or narrative content
- [ ] Useful as LLM feedback
- [ ] Reduces ambiguity instead of adding context

If any item fails → revise or discard.

---

## REQUIRES (HARD DEPENDENCIES)

These SKILLs MUST be active before this SKILL can execute:

- context-sanitizer-contract

If any are missing:
→ STOP
→ LOAD them
→ RESTART execution

## SKILL ORDER EXECUTION (ENFORCED SEQUENCE)

Apply dependencies in this exact order:

1. context-sanitizer-contract

## TRIGGERS

### ON_COMPLETE

→ APPLY SKILL: skill-reviewer-protocol
