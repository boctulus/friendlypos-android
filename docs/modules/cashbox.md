# Módulo: Cashbox (Caja)

## Archivos

| Archivo | Rol |
|---|---|
| `api/dto/CashboxDto.kt` | DTOs de caja: `CashboxItemDto`, `StoreDto`, respuestas |
| `api/dto/CashboxSessionDto.kt` | DTOs de sesión: `CashboxSessionItemDto`, `SessionsListResponseDto` |
| `api/dto/CashboxMovementDto.kt` | DTOs de movimiento: `MovementTypeDto`, `RegisterMovementRequestDto`, `RegisterMovementResponseDto`, `MovementItemDto` |
| `api/ApiService.kt` | `getSessions`, `getCurrentCashboxSession`, `openCashboxSession`, `closeCashboxSession`, `getMovementTypes`, `registerMovement` |
| `repository/CashboxRepository.kt` | `getCurrentSession`, `openSession`, `closeSession`, `getMovementTypes`, `registerMovement` |
| `compose/viewmodel/CashboxViewModel.kt` | StateFlows + lógica de caja y movimientos |
| `ui/cashbox/CashboxFragment.kt` | Host Compose, Activity-scoped ViewModel |
| `compose/screen/CashboxScreen.kt` | Routing por rol; cajero → `CashboxCashierContent` con sub-navegación interna |
| `compose/screen/CashboxMenuScreen.kt` | Menú cajero: 3 pills (Abrir / Cerrar / Registrar Movimiento) |
| `compose/screen/CashboxOpenScreen.kt` | Apertura de sesión (cajero) |
| `compose/screen/CashboxMovementScreen.kt` | Registro de movimiento de caja |
| `compose/screen/CashboxSupermarketScreen.kt` | Vista de todas las sesiones abiertas (supermarket) |

---

## Estado del ViewModel

| StateFlow | Tipo | Descripción |
|---|---|---|
| `currentSession` | `CashboxSessionItemDto?` | Sesión activa del usuario actual. `null` si no hay sesión o aún carga |
| `availability` | `List<CashboxAvailabilityItemDto>` | Cajas disponibles/ocupadas de la tienda |
| `hasInitialLoadCompleted` | `Boolean` | `true` una vez que `loadCurrentSession` completó al menos una vez |
| `movementTypes` | `List<MovementTypeDto>` | Tipos de movimiento cargados desde API. Se carga lazy al entrar a "Registrar Movimiento" |
| `isLoadingMovementTypes` | `Boolean` | Indica carga en curso de tipos de movimiento |

---

## Routing de pantalla por rol

```
role == "supermarket"  → CashboxSupermarketScreen (todas las sesiones abiertas)
role != "supermarket"  → CashboxCashierContent → sub-navegación interna (CashboxSubScreen enum)
```

### Sub-navegación cajero (`CashboxSubScreen`)

```
MENU       → CashboxMenuScreen (3 pills: Abrir / Cerrar / Registrar Movimiento)
OPEN       → CashboxOpenScreen
CLOSE      → CashboxCloseContent (solo si hay sesión abierta)
MOVEMENT   → CashboxMovementScreen (solo si hay sesión abierta)
```

- Estado inicial siempre: `MENU`
- `BackHandler` activo cuando `subScreen != MENU` → vuelve a MENU
- Si se navega a CLOSE o MOVEMENT sin sesión abierta → `LaunchedEffect` redirige a MENU
- Éxito en OPEN o MOVEMENT → `LaunchedEffect(successMessage)` → limpia mensaje y vuelve a MENU

### Regla: cobertura de `LaunchedEffect(successMessage)`

Todo sub-screen que dispare una operación (`OPEN`, `MOVEMENT`, y cualquier futuro sub-screen similar) **debe** estar incluido en el `when` de `LaunchedEffect(successMessage)` en `CashboxCashierContent`.

**Razón:** si un sub-screen no limpia `successMessage` al navegar de vuelta a MENU, el mensaje queda en el StateFlow. Cuando el usuario entra a otro sub-screen que muestre `successMessage` (ej: `CashboxCloseContent`), verá el mensaje de la operación anterior como si fuera un resultado propio.

---

## Reglas de terminal (ABSOLUTO)

- Una terminal NO puede tener más de una caja abierta simultáneamente
- `terminalHasOpenSession = currentSession?.status == "open"`
- Si `terminalHasOpenSession == true`: ocultar "Abrir Caja" en toda caja, incluso para `role == "supermarket"`
- Si `terminalHasOpenSession == true`: la tarjeta de la sesión activa muestra "Ver detalles" + "Cerrar caja"
- Si `role != "supermarket"` y `currentSession != null`: la pantalla redirige a `CashboxCloseContent`

---

## Reglas

- `availableCashboxes` = cajas activas cuyo `serialNumber` NO aparece en ninguna sesión de `allOpenSessions`
- `isSessionOpen` en `HomeFragment` usa `hasInitialLoadCompleted` para distinguir "cargando" de "sin sesión"
  - `!hasLoaded` → `null` (ambos botones deshabilitados, estado loading)
  - `currentSession?.status == "open"` → `true` (Cerrar habilitado)
  - else → `false` (Abrir habilitado)
- `HomeFragment.onResume()` llama `refreshStoreState(storeId)` para mantener estado actualizado al volver a Inicio
- `CashboxFragment.LaunchedEffect(storeId)` carga cashboxes y sesiones abiertas al entrar al módulo

---

## Endpoints usados

| Endpoint | Método | Propósito |
|---|---|---|
| `api/firestore/cashbox/user-stores` | GET | Tiendas del usuario |
| `api/cashbox/cashboxes?filter=store_id:X,status:active` | GET | Cajas activas de la tienda |
| `api/firestore/cashbox/sessions?store_id=X&status=open` | GET | Todas las sesiones abiertas |
| `api/firestore/cashbox/sessions/current` | GET | Sesión activa del usuario autenticado |
| `api/firestore/cashbox/sessions` | POST | Abrir sesión |
| `api/firestore/cashbox/sessions/{id}/close` | PATCH | Cerrar sesión |
| `api/firestore/cashbox/movement-types` | GET | Tipos de movimiento disponibles (filtrados por rol) |
| `api/firestore/cashbox/movements` | POST | Registrar movimiento de caja |

---

## `closeSession` — manejo unificado

Un único método en el ViewModel cierra tanto la sesión propia (cajero) como la de otros (supermarket):

- Si `sessionId == currentSession.id` → actualiza `_currentSession`
- Siempre filtra `_availability` eliminando la sesión cerrada
- Para supermarket: `CashboxFragment.LaunchedEffect(successMessage)` recarga availability

---

## Registro de movimiento (`CashboxMovementScreen`)

**Body POST `/api/firestore/cashbox/movements`:**

```json
{
  "cashbox_session_id": "<id>",
  "movement_code": "income|withdrawal|expense|bank_deposit|internal_transfer|refund|adjustment_plus|adjustment_minus",
  "amount": 1000.0,
  "description": "texto obligatorio",
  "payment_method": "cash"
}
```

### Tipos de movimiento (fuente: `cashboxMovements.config.js` en Node.js)

| Código | Label | Signo | Requiere justificación |
|---|---|---|---|
| `income` | Ingreso | + | No |
| `withdrawal` | Retiro | - | No |
| `expense` | Gasto | - | No |
| `bank_deposit` | Depósito Bancario | - | No |
| `internal_transfer` | Transferencia Interna | - | No |
| `refund` | Devolución | - | No |
| `adjustment_plus` | Ajuste Positivo | + | Sí (≥10 chars) — solo admin |
| `adjustment_minus` | Ajuste Negativo | - | Sí (≥10 chars) — solo admin |

- `opening` y `sale` se excluyen del dropdown (son automáticos)
- La API filtra `requiresAuthorization` según rol — no-admin no los ve
- `requiresJustification == true` → descripción mínima 10 caracteres (validado client-side y server-side)
- El dropdown agrupa ingresos y egresos con separadores visuales

---

## Validación de cierre — diferencia y nota obligatoria

**Archivo:** `compose/screen/CashboxScreen.kt` → `CashboxCloseContent`

### Fuente del monto esperado

```
expectedAmount = session.totalCash ?: session.initialAmount
```

El backend calcula `expected_amount` via `enrichWithExpected()` en `GET /sessions/current`.  
`totalCash` es el equivalente en tiempo real: `initialAmount + totalSales - totalExpenses`.  
No se necesita endpoint adicional: la diferencia se calcula client-side igual que en el Node.js.

### Regla: refresh de sesión tras movimiento

`registerMovement` en el ViewModel **debe** llamar `loadCurrentSession()` tras éxito.

**Razón:** `session.totalCash` no se actualiza automáticamente en el StateFlow local. Sin el refresh, `expectedAmount` al cerrar usará el valor anterior al movimiento, silenciando diferencias reales.

### Reglas (idénticas al Node.js)

```
diff = abs(expectedAmount - finalAmount)
diffPercent = if (expectedAmount > 0) (diff / expectedAmount) * 100
              else if (diff > 0) 100.0 else 0.0
```

| Condición | Comportamiento |
|---|---|
| `diff == 0` | Cierre directo, sin diálogo |
| `diffPercent >= 1%` y `notes` vacío | **Bloquear** — campo notes marcado como error, se requiere nota |
| `diff > 0` y notes OK | Diálogo de confirmación antes de cerrar |

### Niveles del diálogo de confirmación

| Rango | Color | Título |
|---|---|---|
| `diffPercent > 5%` | Rojo `#E53935` | "Alta Diferencia Detectada" |
| `1% ≤ diffPercent ≤ 5%` | Naranja `#F57C00` | "Diferencia Intermedia Detectada" |
| `diffPercent < 1%` | Azul `#1976D2` | "Diferencia Mínima Detectada" |

### Estados del campo `notes`

- Label normal: `"Notas de cierre (obligatorio si diferencia ≥ 1%)"`
- Label error: `"Notas de cierre *"` + `supportingText` en rojo
- `isError = notesRequired && notes.isBlank()`
- Focus automático via `FocusRequester` cuando se activa `notesRequired`
