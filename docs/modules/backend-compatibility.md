# Backend Compatibility (cliente Android)

## Regla

El cliente Android es **agnóstico de backend**: funciona contra cualquier backend
FriendlyPOS-compatible que exponga el contrato HTTP descrito aquí. El backend se
selecciona solo por **base URL** (build flavor / configuración) — no hay lógica
específica por backend en el código.

## Contrato consumido

Base URL configurable. Auth: token + cookie de sesión (`PersistentCookieJar`).

| Área | Endpoint | Notas |
|---|---|---|
| Auth | `POST api/auth/login` | |
| Productos | `GET api/products`, `GET api/products/search/quick` | |
| Clientes | `GET api/supabase/customers` | |
| Ventas | `POST api/firestore/sales` | requiere sesión de caja abierta (ver abajo) |
| DTE | `POST api/sales/{saleId}/emitir-dte` | `tipo: boleta|factura`; respuesta con `folio` + `timbre` (PNG base64) |
| Contribuyente | `GET api/sales/taxpayer/{rut}` | autocompletado de factura |
| Caja | `GET/POST/PATCH api/firestore/cashbox/sessions...` | |
| Logo | `GET api/printer/logo/{storeId}` | `{ success, store_id, logo_url, absolute_url }` |
| Ticket data | `GET api/printer/ticket-data/{sale|dte|cashbox-opening|cashbox-close}/...` | JSON estructurado (no PDF) |

## Endpoints de ticket-data (impresión por pantalla)

Respuesta común: `{ success, type, paper_size, logo_url, logo_absolute_url, data }`
→ `TicketDataResponseDto`. El render se elige por `type`:

| `type` | Render |
|---|---|
| `sale` | ticket sin valor tributario (`ticket-58mm.html`) |
| `dte` **o** `sale-dte` | ticket DTE (`dte-58mm.html`) — ambos valores se tratan igual |
| `cashbox-opening` | apertura |
| `cashbox-close` | cierre |

Mapeo en `TicketHtmlRenderer.render()`.

## Compatibilidad amplia (tolerancia de variantes)

Distintos backends compatibles pueden divergir en detalles. El cliente NO asume un único
backend; tolera estas variantes:

- **DTE**: el `type` puede llegar como `dte` o `sale-dte`. Ambos → ticket DTE.
- **Cierre de caja con desfase ≥ 1% sin nota**: el rechazo puede variar en `status`
  (`400` o `422`) y `code` (`CLOSE_NOTE_REQUIRED` o `NOTE_REQUIRED`). El cliente NO debe
  depender de un valor único: leer los campos estables `diff_percent`, `level`
  (`info|warning|danger`) y/o `data { expected_amount, final_amount, difference,
  diff_percent, level }`, presentes en todos los backends compatibles.
- **Logo / URLs absolutas**: derivadas del request en el backend (proxy-aware); el cliente
  usa `logo_absolute_url ?? logo_url`.

## Regla de negocio: venta exige caja abierta

Una venta NO puede registrarse sin una sesión de caja abierta. Se valida en dos capas:

- **Servidor** (fuente de verdad, todos los backends): rechazo si no hay sesión abierta.
- **Cliente** (defensa): `SaleRepository.createSale` falla si no hay sesión;
  `HomeFragment` bloquea el acceso a la calculadora y redirige a abrir caja.

## Pendiente

- Si un backend compatible no enviara aún `diff_percent`/`level`, el cliente debe degradar
  con gracia (mostrar el `error` textual). No hardcodear `code` ni `status`.
