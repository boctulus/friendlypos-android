---
name: activity-theming-contract--android
description: Enforces correct theming rules when creating or modifying Android Activities in FriendlyPOS. Prevents dark-mode breakage and hardcoded color misuse.
antiTriggers: Task does not create or modify an Android Activity class.
---

# SKILL_DEFINITION: activity-theming-contract--android

## ACTIVATION (ENTRY GATE)

This SKILL MUST only be applied when ALL of the following conditions are true:

- The task creates a new Android Activity class, OR modifies an existing one
- The project is FriendlyPOS (Android)

---

If these conditions are NOT met:

-> DO NOT APPLY this SKILL
-> STOP reading further instructions
-> Continue with other relevant SKILLs

## EXECUTION PLAN (MANDATORY)

STEP 1: Read design token documentation

TYPE: ACTION

ACTION: Read `docs/modules/design-tokens.md` in full before writing any Activity or layout code. Extract: forced light mode policy, token reference, theme mode rules.

ON_FAILURE:
-> STOP
-> REPORT ERROR: Cannot read docs/modules/design-tokens.md — do not proceed without it

---

STEP 2: Enforce Activity base class

TYPE: CHECK

CHECK:
- New Activity MUST extend `AppCompatActivity`, NOT `ComponentActivity`
- Reason: `AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)` set in `FriendlyPOSApp` only applies correctly to `AppCompatActivity`

ON_FAILURE:
-> STOP
-> REPORT ERROR: Activity extends ComponentActivity — change to AppCompatActivity

---

STEP 3: Enforce Compose color scheme (ONLY IF: Activity uses Jetpack Compose via setContent)

TYPE: CHECK

CHECK:
- `MaterialTheme` call MUST receive an explicit `colorScheme = lightColorScheme()` parameter
- Do NOT use `MaterialTheme {}` without explicit colorScheme — it may inherit system dark mode

ON_FAILURE:
-> STOP
-> REPORT ERROR: MaterialTheme missing explicit lightColorScheme() — add colorScheme parameter

---

STEP 4: Enforce color token usage in layouts

TYPE: CHECK

CHECK:
- Layout XML MUST NOT use hardcoded hex color values (e.g. android:textColor="#000000")
- Use `@color/brand_*`, `@color/surface_*`, `@color/table_*` tokens from colors.xml
- Compose screens MUST use `AppColors.*` from AppColors.kt
- Legacy names (`colorAccent`, `colorPrimary`, `successGreen`) are forbidden in new code

ON_FAILURE:
-> STOP
-> REPORT ERROR: Hardcoded hex color detected — replace with design token

---

STEP 5: Validate theme registration in manifest

TYPE: CHECK

CHECK:
- Activity in AndroidManifest.xml MUST use `@style/Theme.FriendlyPOS` or omit theme (inherits app-level)
- Do NOT assign a DayNight or night-specific theme to any Activity

ON_FAILURE:
-> STOP
-> REPORT ERROR: Activity registered with incorrect theme in AndroidManifest.xml

## ANTI-PATTERNS (FORBIDDEN — treat as hard errors)

### Anti-pattern 1: ComponentActivity instead of AppCompatActivity

FORBIDDEN:
```kotlin
class MyActivity : ComponentActivity() { ... }
```

REQUIRED:
```kotlin
class MyActivity : AppCompatActivity() { ... }
```

WHY: `AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)` is called in `FriendlyPOSApp.onCreate()`.
That call only takes effect for activities that go through AppCompat's delegate.
A `ComponentActivity` bypasses the delegate entirely — the activity receives the system dark mode
configuration regardless of the global override, causing black text on dark backgrounds.

---

### Anti-pattern 2: MaterialTheme without explicit colorScheme

FORBIDDEN:
```kotlin
setContent {
    MaterialTheme {          // inherits system dark mode — WRONG
        MyScreen()
    }
}
```

REQUIRED:
```kotlin
import androidx.compose.material3.lightColorScheme

setContent {
    MaterialTheme(
        colorScheme = lightColorScheme()   // always light — CORRECT
    ) {
        MyScreen()
    }
}
```

WHY: `MaterialTheme {}` with no `colorScheme` resolves to `MaterialTheme.colorScheme` from the
composition locals. If no parent sets it, Material3 defaults to light — BUT some components read
`isSystemInDarkTheme()` internally and may produce dark-mode colors. Explicit `lightColorScheme()`
makes the intent unambiguous and prevents any component from reading the system configuration.

---

## REQUIRES (HARD DEPENDENCIES)

These SKILLs MUST be active before this SKILL can execute:

- anti-hallucination-project-guard

If any are missing:
-> STOP
-> LOAD them
-> RESTART execution

## SKILL ORDER EXECUTION (ENFORCED SEQUENCE)

Apply dependencies in this exact order:

1. anti-hallucination-project-guard

## TRIGGERS

### ON_EVENT

EVENT: new_activity_created
-> APPLY SKILL: activity-theming-contract--android

---

### ON_COMPLETE

-> APPLY SKILL: post-task-verification-strict
