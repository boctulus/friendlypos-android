---
name: git-snapshot-verification
description: Create an isolated and reversible snapshot of current changes using Git without polluting history, enabling accurate diff analysis
---

# GIT SNAPSHOT VERIFICATION

## Objective

Provide a **reliable, reversible, and non-destructive snapshot** of current changes for verification purposes.

---

## 🔥 Core Design Decision

### ❌ NO usar commit por defecto

### ✅ Usar `git stash` como snapshot efímero

---

## Modes

```json
{
  "mode": "stash | commit | worktree",
  "default": "stash"
}
```

---

# 🧠 MODE 1 — STASH (RECOMENDADO)

## Step 1 — Save Snapshot

```bash
git stash push -u -m "snapshot: verification"
```

Incluye:

* tracked files
* untracked files (`-u`)

---

## Step 2 — Diff Against Snapshot

```bash
git stash show -p stash@{0}
```

o más preciso:

```bash
git diff stash@{0}^1 stash@{0}
```

---

## Step 3 — Restore State

```bash
git stash pop
```

---

## ✅ Ventajas

* No contamina historial
* Reversible
* Seguro para CI mental
* No requiere ramas

---

## ⚠️ Riesgos

* Conflictos al hacer `pop`
* Stash stack puede ensuciarse si no se limpia

---

# 🧠 MODE 2 — COMMIT (CONTROLADO)

Solo si necesitas exactitud total en tooling externo.

## Step 1 — Snapshot Commit

```bash
git add -A
git commit -m "temp(snapshot): verification"
```

---

## Step 2 — Diff

```bash
git diff HEAD~1 HEAD
```

---

## Step 3 — Rollback

```bash
git reset HEAD~1
```

---

## ⚠️ Problemas

* Hooks pueden ejecutarse
* Riesgo de olvidar rollback
* Historia “sucia” si falla

---

# 🧠 MODE 3 — WORKTREE (AVANZADO)

## Idea

Crear un entorno aislado:

```bash
git worktree add ../snapshot HEAD
```

Trabajar ahí sin tocar el repo principal.

---

## ⚠️ Tradeoffs

* Más complejo
* Overhead de filesystem
* No siempre necesario

---

# 🧪 Step — File Change Extraction

Después del snapshot:

```bash
git diff --name-only
```

Clasificación:

```bash
git diff --name-status
```

---

# 🧪 Step — Fine-grained Diff

```bash
git diff -U0
```

→ útil para detectar funciones modificadas

---

# 🔒 Safety Rules

* Nunca dejar stash sin limpiar
* Nunca dejar commit temporal sin revertir
* Nunca usar `reset --hard` sin contexto

---

# 🔁 Cleanup Protocol

### Para STASH

```bash
git stash drop stash@{0}
```

(si no hiciste pop)

---

### Para COMMIT

```bash
git reset --soft HEAD~1
```

---

# 🔗 Integración con `post-task-verification`

Pipeline ideal:

```
1. Ejecutar tarea
2. git-snapshot-verification (stash)
3. post-task-verification (análisis)
4. cleanup snapshot
```

---

# ⚠️ Failure Scenarios (reales)

## 1. Repo sucio antes de empezar

→ snapshot incluye cambios previos

👉 Solución:

```bash
git status
```

y FAIL si no está limpio

---

## 2. Archivos ignorados (.gitignore)

`stash -u` NO incluye ignored files

👉 Si necesitas todo:

```bash
git stash push -a
```

---

## 3. Conflictos en stash pop

→ posible si el agente modificó archivos después

---

# 💡 Mejora PRO (muy recomendable)

Añadir checksum:

```bash
git ls-files -s
```

o incluso:

```bash
sha1sum <files>
```

→ detectas cambios invisibles (encoding, whitespace)

---

# 🚨 Insight clave

Este SKILL no es para ver “qué cambió”.

Es para garantizar esto:

👉 **“Lo que estoy verificando es exactamente lo que el agente tocó en esta ejecución”**
