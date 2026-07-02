---
name: context-sanitizer-contract
description: Enforces strict sanitization of all generated outputs to ensure portability and reusability across different projects and environments.
---

# SKILL_DEFINITION: Context Sanitizer

Ensures all generated outputs are portable, reusable, and free from environment-specific data.

---

# 1. Core Rule

All outputs MUST be copy-paste portable across projects.

If not → MUST be rewritten.

---

# 2. Forbidden Data

The assistant MUST NOT include:

## 2.1 File System

- Absolute paths
- OS-specific paths
- Real directory structures

❌
```js
cwd: 'D:\\nodejs\\project'
````

✅

```js
cwd: process.cwd()
```

---

## 2.2 Project Identity

There is a APP_NAME constant in .env that MUST be used instead of real project names.

This constant is ussualy part of the config in `config/appSetup.js` or similar.

---

## 2.3 Network / Infra

* IPs
* Real domains
* Non-generic ports

They must not be included in any form in the output (source code or documentation).

---

## 2.4 Machine Context

* Usernames
* Hostnames
* Local configs

Usernames, hostnames, and any local machine context MUST be excluded from outputs except in temporary script files.

---

# 3. Mandatory Replacements

| Problem        | Replace With    |
| -------------- | --------------- |
| Absolute paths | `process.cwd()` |
| Paths          | `path.join()`   |
| Project names  | `your-project`  |
| URLs           | `localhost`     |
| Config         | ENV variables   |

---

# 4. Code Rules

## 4.1 Dynamic Path Resolution ONLY

```js
import path from 'path';

const appRoot = process.cwd();

function appPath(...segments) {
  return path.join(appRoot, ...segments);
}
```

---

## 4.2 No Hidden Assumptions

Everything required must be:

* declared
* or inferable

---

# 5. Validation (MANDATORY)

Before output:

* Is this portable?
* Can it run in another project unchanged?
* Does it contain real identifiers?

If NOT portable, OR NOT runnable unchanged, OR contains real identifiers → sanitize.

---

# 6. Exception Handling

This skill MAY be bypassed ONLY if:

* `incident-docs-protocol` explicitly marks content as "incident layer"

Otherwise → ALWAYS enforced.

---

# 7. Priority

Overrides:

* docs-authoring-protocol
* prompt-output-constraints-contract

Portability > realism