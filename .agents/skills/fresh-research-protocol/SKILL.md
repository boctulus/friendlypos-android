---
name: fresh-research-protocol
description: Mandatory Fresh Research Before Implementation Policy
type: protocol
domain: research
---

# SKILL_DEFINITION: fresh-research-protocol

## ACTIVATION (ENTRY GATE)

This SKILL MUST only be applied when ALL of the following conditions are true:

- The task involves implementing code, integrations, or using external libraries/APIs
- The prompt contains words like: “implementa”, “código”, “ejemplo”, “cómo usar”, library names, SDK names, or technical integration requests

---

If these conditions are NOT met:

→ DO NOT APPLY this SKILL
→ STOP reading further instructions
→ Continue with other relevant SKILLs

## EXECUTION PLAN (MANDATORY)

STEP 1: Identify what to research

TYPE: ACTION

ACTION: List all external tools, libraries, frameworks, or APIs involved in the task before writing any code

ON_FAILURE:
→ STOP
→ REPORT ERROR: Could not identify research targets

---

STEP 2: Perform web search

TYPE: ACTION

ACTION: For each identified tool, execute web searches prioritizing:
1. GitHub (recent activity)
2. Official documentation
3. Recent issues/discussions
4. StackOverflow (support only)

Use queries like:
- `”{tool} official documentation 2025”`
- `”{tool} github latest version”`
- `”{tool} breaking changes”`

MUST retrieve >= 2 sources. MUST include official docs OR GitHub with recent commits.

ON_FAILURE:
→ STOP
→ REPORT ERROR: Could not retrieve minimum required sources

---

STEP 3: Validate freshness of sources

TYPE: CHECK

CHECK:
- Last update < 2 years ago
- No unanswered critical issues
- README is current
- Versions mentioned match current stable release
- API referenced is not deprecated

Reject or flag as suspect if any filter fails.

ON_FAILURE:
→ WARN user: sources may be outdated
→ DO NOT PROCEED without explicit user approval

---

STEP 4: Implement based on verified research

TYPE: ACTION

ACTION: Generate implementation using only verified, current information.
Output format MUST be:

```
## Research Summary
- Source 1: (link + date)
- Source 2: (link + date)
- Project status: active / deprecated

## Considerations
- Recent breaking changes
- Risks

## Implementation (updated)
(code based on research)
```

If unsure about any API detail → DO NOT GUESS → SEARCH AGAIN

ON_FAILURE:
→ STOP
→ REPORT ERROR: Could not produce implementation from verified sources

---

## Reglas adicionales

### Estrategia de búsqueda

Prioriza:

1. GitHub (actividad reciente)
2. Documentación oficial
3. Issues/Discussions recientes
4. StackOverflow (solo como apoyo)

### Filtros de frescura

Rechazar o marcar como sospechoso si:

* Última actualización > 2 años
* Issues sin respuesta
* README desactualizado
* Versiones mencionadas < actuales

### Checklist antes de implementar

* La librería sigue mantenida
* La versión usada es la más reciente estable
* La API sigue vigente (no deprecated)
* No hay breaking changes recientes que afecten la implementación

---

## 🧩 Nota histórica (contexto del diseño)

### 2. Regla dura (ENFORCEMENT)

Antes de responder:

```text
IF task involves code or tools:
    MUST perform web search
    MUST retrieve >= 2 sources
    MUST include:
        - official docs OR
        - GitHub repo (último commit reciente)
    ELSE:
        refuse or ask to search
```

---

### 3. Estrategia de búsqueda

Prioriza:

1. GitHub (actividad reciente)
2. Documentación oficial
3. Issues/Discussions recientes
4. StackOverflow (solo como apoyo)

Queries tipo:

```text
"{tool} official documentation 2025"
"{tool} github latest version"
"{tool} breaking changes"
"{tool} example usage latest"
```

---

### 4. Filtros de frescura

Rechazar o marcar como sospechoso si:

* Última actualización > 2 años
* Issues sin respuesta
* README desactualizado
* Versiones mencionadas < actuales

---

### 5. Validación antes de implementar

Checklist obligatorio:

* ✅ ¿La librería sigue mantenida?
* ✅ ¿La versión usada es la más reciente estable?
* ✅ ¿La API sigue vigente (no deprecated)?
* ✅ ¿Hay breaking changes recientes?

---

### 6. Output estructurado

Forzar que el agente responda así:

```markdown
## 🔎 Research Summary
- Fuente 1: (link + fecha)
- Fuente 2: (link + fecha)
- Estado del proyecto: activo / deprecated

## ⚠️ Consideraciones
- Cambios recientes
- Riesgos

## 💻 Implementación (actualizada)
(código basado en lo investigado)
```

---

## 🧪 Ejemplo de Prompt del Skill

Puedes inyectarlo como system prompt o middleware:

```text
You MUST perform a web search before generating any technical implementation.

Rules:
- Always verify latest official documentation
- Prefer GitHub repos with recent commits (< 6 months)
- Reject outdated approaches
- Explicitly mention sources used
- If no recent sources found, warn user

Never produce code without research validation.
```

---

## 🚀 Versión avanzada (muy recomendada)

### 🔁 “Plan → Search → Read → Then Code”

1. Plan:

   * identificar qué buscar

2. Search:

   * múltiples queries

3. Read:

   * resumir fuentes

4. THEN:

   * recién generar código

---

## 🧠 Mejora clave: Score de confiabilidad

Puedes hacer que el agente evalúe:

```text
confidence_score =
    recency_weight +
    official_source_weight +
    github_activity_weight
```

Y si es bajo → no implementar.

---

## 🧰 Bonus: Anti-hallucination guard

```text
If unsure about API:
    DO NOT GUESS
    SEARCH AGAIN
```

---

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

### ON_CONDITION

IF task involves external libraries, frameworks, or APIs
→ APPLY SKILL: fresh-research-protocol

IF prompt contains: "implementa", "integra", "usa", "ejemplo de", library or SDK name
→ APPLY SKILL: fresh-research-protocol

---

### ON_COMPLETE

→ APPLY SKILL: skill-reviewer-protocol
