# Módulo: Products

## Archivos

| Archivo | Rol |
|---|---|
| `api/dto/ProductDto.kt` | DTO de respuesta + mapper a `model/Product` |
| `repository/ProductRepository.kt` | `getPage(cursor, limit)` y `searchQuick(query)` — data class `ProductPage` |
| `compose/viewmodel/InventoryViewModel.kt` | Carga y filtrado de inventario |
| `compose/screen/InventoryScreen.kt` | UI inventario — consume `InventoryViewModel` |
| `ui/sales/SalesCalculatorViewModel.kt` | Estado de búsqueda modal + escaneo al carrito (`addProductToCart`, `scanBarcode`, `scanFeedback`) |
| `compose/screen/SalesCalculatorScreen.kt` | Modal `ProductSearchModal` (lupa) + icono barras → escáner-carrito |
| `compose/screen/ScanCartScreen.kt` | Pantalla escáner+carrito: escanea EAN13 y agrega al carrito completo |
| `ui/sales/ScanCartFragment.kt` | Host de `ScanCartScreen`; comparte el ViewModel (scope Activity) |
| `compose/components/SaleItemComposables.kt` | `SaleItemRow` + `EditItemDialog` compartidos (Cart y ScanCart) |

---

## Endpoints

```
GET /api/products?cursor={id}&limit={n}        ← inventario paginado
GET /api/products/search/quick?q={q}           ← búsqueda rápida (modal lupa Calculadora)
GET /api/price-verifier/lookup?ean={e}&store_id={s}  ← lookup EAN13 (escáner-carrito)
```

Requiere `Authorization: Bearer <access_token>` (salvo lookup). `store_id` lo resuelve el backend desde los claims JWT.

### Lookup por EAN13 (escáner-carrito)

- `lookup` es **público** (sin JWT, módulo price-verifier) → `store_id` se envía explícito (cliente: `SessionManager.get(context)?.storeId`).
- Valida EAN de 13 dígitos; responde `{ found: true, product }` o `{ found: false }`. `product` tiene la misma forma que un item de `search/quick` (mismo `ProductsModel.searchQuick`) → se parsea con `ProductDto`.
- Reglas escáner-carrito: re-escaneo del mismo `product.id` → cantidad +1 en su línea (no reordena); no encontrado → snackbar + vibración; guard `isLookupInProgress` evita lookups concurrentes; orden "más reciente arriba" solo en UI (`asReversed`).

Parámetros paginación:
- `cursor` — ID del último producto de la página anterior (omitir en primera página)
- `limit` — items por página (default 50, máx 100)

Respuesta:
```json
{
  "success": true,
  "products": [...],
  "total": 50,
  "hasMore": true,
  "nextCursor": "uuid-del-ultimo-producto"
}
```

### Mecanismo cursor

`PostgresReader.find()` ejecuta `LIMIT limit+1` internamente. El backend detecta si hay más items comparando `rawProducts.length > pageLimit`, devuelve solo `pageLimit` items y el ID del último como `nextCursor`.

En la siguiente request Android envía `?cursor={nextCursor}` → PostgresReader genera `WHERE (created_at < cursor_value OR ...)` para continuar desde ese punto.

---

## Mapeo de campos crítico

El backend usa `description` como nombre del producto. El modelo Android usa `name`.

| Backend | Android `Product` |
|---|---|
| `description` | `name` |
| `brand` | `description` |
| `ean13` | `barcode` |
| `stock` (Double) | `stock` (Int) |
| `category` | `category` |
| `price` | `price` |

Mapper en `ProductDto.toProduct()`.

---

## Comportamiento del modal

- Query vacío → lista vacía, muestra "Escriba para buscar productos". No llama a la API.
- Query con contenido → debounce 300ms → llama `/search/quick`. Cancela el job anterior si el usuario sigue escribiendo.
- X en el campo → limpia query + cancela búsqueda pendiente.
- Cerrar modal → `clearProductSearch()` en ViewModel → resetea todo el estado.

---

## Paginación — InventoryViewModel

- `loadProducts()` → resetea cursor, carga primera página.
- `loadMore()` → usa `nextCursor` guardado, appends a `_products`. Guard: no ejecuta si `isLoadingMore`, `!hasMore`, o `nextCursor == null`.
- `InventoryScreen` dispara `loadMore()` vía `LaunchedEffect` cuando el último item visible está a 4 posiciones del fondo.
- El buscador filtra client-side sobre los items ya cargados. Para buscar en todo el catálogo usar el modal de Calculadora.

---

## Restricciones

- `GET /api/products` filtra solo productos activos (`is_active: true` por defecto).
- `searchQuick` devuelve máximo 15 resultados.
- Sin offline — no hay fallback local.
