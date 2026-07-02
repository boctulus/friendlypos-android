# Módulo: Customers

## Archivos

| Archivo | Rol |
|---|---|
| `api/dto/CustomerDto.kt` | DTOs de respuesta + mapper a `model/Customer` |
| `repository/CustomerRepository.kt` | `getPage(cursor, limit)` — data class `CustomerPage` |
| `compose/viewmodel/CustomersViewModel.kt` | Carga, paginación y filtrado de clientes |
| `compose/screen/CustomersScreen.kt` | UI clientes — consume `CustomersViewModel` |

---

## Endpoint

```
GET /api/supabase/customers?startAfterDocId={id}&limit={n}&orderBy=created_at&order=desc
```

Requiere sesión activa (cookie). `store_id` lo resuelve el backend desde la sesión.

Parámetros paginación:
- `startAfterDocId` — ID del último cliente de la página anterior (omitir en primera página)
- `limit` — items por página (default 50)
- `orderBy` — campo de ordenamiento (default `created_at`)
- `order` — dirección `asc`/`desc` (default `desc`)

Respuesta:
```json
{
  "success": true,
  "data": {
    "docs": [...],
    "pagination": {
      "found": 50,
      "total": 320,
      "hasMore": true,
      "nextCursor": "uuid-del-ultimo-cliente",
      "limit": 50
    }
  }
}
```

### Mecanismo cursor

Endpoint usa `SupabaseController` genérico → `model.read()` → `PostgresReader.read()`. Internamente llama `find()` con `LIMIT limit+1` para detectar `hasMore`, devuelve `nextCursor` = ID del último item de la página. Android pasa ese valor como `startAfterDocId` en la siguiente request.

---

## Mapeo de campos

| Backend `CustomerDto` | Android `Customer` |
|---|---|
| `id` | `id` |
| `name` | `name` |
| `email` | `email` |
| `phone` | `phone` |
| `address` | `address` |
| `total_purchases` | `totalPurchases` |
| `last_purchase_date` | `lastPurchaseDate` |
| `created_at` (fallback) | `lastPurchaseDate` si el campo anterior es nulo |

Mapper en `CustomerDto.toCustomer()`.

---

## Paginación — CustomersViewModel

- `loadCustomers()` → resetea cursor, carga primera página.
- `loadMore()` → usa `nextCursor` guardado, appends a `_customers`. Guard: no ejecuta si `isLoadingMore`, `!hasMore`, o `nextCursor == null`.
- `CustomersScreen` dispara `loadMore()` vía `LaunchedEffect` cuando el último item visible está a 4 posiciones del fondo.
- El buscador filtra client-side sobre los items ya cargados (name, email, phone, address).

---

## Restricciones

- Sin offline — no hay fallback local.
- El buscador filtra solo los items ya paginados localmente; no ejecuta búsqueda en backend.
