# Skills Index — FriendlyPOS Agent

**Purpose:** Master routing guide. LLM reads this first to select which SKILL(s) apply to the current task.

---

## Quick Decision Tree

```
User request → What kind of task is this?

├─ Any code task (creation, refactor, modification)
│   └─ → skill-orchestrator (always active as global orchestrator)
│
├─ "Create or modify an Android Activity"
│   └─ → activity-theming-contract--android
│
├─ "Refactor or improve code quality"
│   └─ → code-quality-protocol
│
├─ "Create or update documentation"
│   └─ → docs-authoring-protocol
│
├─ "Document a production bug or incident"
│   └─ → incident-docs-protocol
│
├─ "Use an external library, SDK, or API"
│   └─ → fresh-research-protocol
│
├─ "Docker / Supabase connection error (ECONNRESET, socket)"
│   └─ → docker-network-reliability-guard
│
├─ "Git commit, push, or destructive git op"
│   └─ → git-safety-protocol
│
├─ "Create or verify a change snapshot"
│   └─ → git-snapshot-verification
│
├─ "Two or more SKILLs apply to the same task"
│   └─ → skill-conflict-resolution-protocol
│
├─ "Create, audit, or update a SKILL"
│   └─ → skill-maker (create) / skill-reviewer-protocol (audit)
│
└─ "Update skills/index.md"
    └─ → skill-index-maintainer
```

---

## Skill Tables

### 🔵 Android / UI

| Skill | Invoke When | Do NOT Invoke When |
|---|---|---|
| `activity-theming-contract--android` | Creating or modifying any Android Activity class | Task does not touch an Activity (fragments, composables, repos, etc.) |

---

### ⚙️ Code Quality & Verification

| Skill | Invoke When | Do NOT Invoke When |
|---|---|---|
| `code-quality-protocol` | Refactoring existing production code (structural, naming, or consistency) | Writing new code — use relevant lifecycle skill instead |
| `post-task-verification-strict` | After any completed task — verifying intent, scope, encoding, naming | Before the task starts |
| `fresh-research-protocol` | Implementing code using an external library, SDK, or API | Working entirely within internal project code |

---

### 🔴 Infrastructure

| Skill | Invoke When | Do NOT Invoke When |
|---|---|---|
| `docker-network-reliability-guard` | ECONNRESET, UND_ERR_SOCKET, or similar Docker/WSL networking errors | Application-layer bugs with no containerized services involved |

---

### 🔧 Git

| Skill | Invoke When | Do NOT Invoke When |
|---|---|---|
| `git-safety-protocol` | Any git commit, push, stash, reset, rebase, or destructive operation | Read-only git operations (log, diff, status) |
| `git-snapshot-verification` | Creating a reversible snapshot for diff analysis before/after a task | Permanent commits — use git-safety-protocol instead |

---

### ⚪ Documentation

| Skill | Invoke When | Do NOT Invoke When |
|---|---|---|
| `docs-authoring-protocol` | Creating or updating any project documentation file | Code-only changes with no doc impact |
| `incident-docs-protocol` | Documenting a production bug, error log, or incident | Documenting a feature or architecture decision |

---

### 🟣 Skills Meta

| Skill | Invoke When | Do NOT Invoke When |
|---|---|---|
| `skill-orchestrator` | Any task requiring code changes, file creation, or refactoring (global) | Pure informational or read-only questions |
| `skill-maker` | Creating a new SKILL definition | Updating an existing skill — use skill-reviewer-protocol |
| `skill-reviewer-protocol` | Auditing, correcting, or normalizing a skill file | Creating a brand-new skill — use skill-maker |
| `skill-conflict-resolution-protocol` | Two or more SKILLs have overlapping or contradictory rules | Only one SKILL applies clearly |
| `skill-index-maintainer` | Updating or regenerating this index.md | Any other task |

---

### 🟢 Agent Foundations (always-on or auto-applied)

| Skill | Invoke When | Do NOT Invoke When |
|---|---|---|
| `anti-hallucination-project-guard` | Before any code — verifies filesystem, prevents invented structure | Never skip — always mandatory |
| `context-sanitizer-contract` | Before generating any output with paths, names, or env data | Output is already sanitized and portable |
| `prompt-execution-control-protocol` | User says "suspend" or "suspende" | Any other trigger |
| `prompt-output-constraints-contract` | Ambiguous tasks requiring mode selection (Exploration/Silent/Audit) | Task is explicitly defined with clear output expectations |

---

## Workflow Dependency Trees

```
skill-orchestrator
├── anti-hallucination-project-guard
└── context-sanitizer-contract

code-quality-protocol
├── anti-hallucination-project-guard
├── code-defensive-refactoring          ⚠️ no SKILL.md found
├── code-naming-conventions-contract    ⚠️ no SKILL.md found
├── cross-layer-naming-consistency-contract  ⚠️ no SKILL.md found
└── skill-reviewer-protocol
    ├── anti-hallucination-project-guard  <- shared (ejecuta una sola vez)
    └── code-naming-conventions-contract  <- shared

skill-index-maintainer
└── skill-reviewer-protocol
    ├── anti-hallucination-project-guard
    └── code-naming-conventions-contract  ⚠️ no SKILL.md found

activity-theming-contract--android
└── anti-hallucination-project-guard

docs-authoring-protocol
└── context-sanitizer-contract

fresh-research-protocol
└── anti-hallucination-project-guard

skill-conflict-resolution-protocol
└── anti-hallucination-project-guard
```

---

## Standalone Skills (No Dependencies)

These skills work independently — invoke directly when their trigger is met:

- `anti-hallucination-project-guard`
- `context-sanitizer-contract`
- `docker-network-reliability-guard`
- `git-safety-protocol`
- `git-snapshot-verification`
- `incident-docs-protocol`
- `post-task-verification-strict`
- `prompt-execution-control-protocol`
- `prompt-output-constraints-contract`
- `skill-maker`

---

## Anti-False-Positive Rules

1. **Workflow absorbs sub-skills** — If `skill-orchestrator` or `code-quality-protocol` is active, their sub-skills are implicitly invoked. Do not call them separately.
2. **Single-responsibility trigger** — Invoke a skill only when its specific condition is met. Do not apply `fresh-research-protocol` for internal project code changes.
3. **New code vs existing code** — `code-quality-protocol` is for refactoring existing code. For new code, use the relevant lifecycle skill (e.g. `activity-theming-contract--android` for new Activities).
4. **No double-invocation** — If `anti-hallucination-project-guard` appears in multiple active workflows, execute it only once at the start.
5. **Incident vs knowledge** — `incident-docs-protocol` is for real bugs/logs with real paths. `docs-authoring-protocol` is for reusable, sanitized knowledge. Never mix them.

---

## Known Gaps (action required)

| Missing SKILL.md | Referenced by | Action |
|---|---|---|
| `code-defensive-refactoring` | `code-quality-protocol` | Create SKILL.md or remove from REQUIRES |
| `code-naming-conventions-contract` | `code-quality-protocol`, `skill-reviewer-protocol` | Create SKILL.md or remove from REQUIRES |
| `cross-layer-naming-consistency-contract` | `code-quality-protocol` | Create SKILL.md or remove from REQUIRES |

## Anti-Trigger Gaps (audit warning)

17 of 18 skills are missing the `antiTriggers:` frontmatter key.
Only `activity-theming-contract--android` has it.
Add `antiTriggers:` to all SKILL.md frontmatter to enable proper routing disambiguation.
