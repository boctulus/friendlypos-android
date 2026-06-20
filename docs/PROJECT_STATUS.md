# FriendlyPOS | Android app

> Relevamiento de scaffolding, patrones, tecnologías, arquitectura y grado de implementación real.
> Fecha original: 2026-05-24 · **Actualizado: 2026-06-20**

---

## 1. Tecnologías

### Android App (`app/`)

| Tecnología | Versión | Estado |
|---|---|---|
| Kotlin | 2.1.0 | En uso |
| AGP | 8.7.3 | En uso |
| Jetpack Compose + Material 3 | BOM 2024.09.03 | En uso |
| Compose Compiler | Kotlin 2.1.0 plugin | En uso |
| ViewBinding / DataBinding | — | En uso (vistas legacy) |
| Navigation Component | 2.8.5 | En uso |
| Lifecycle (LiveData, ViewModel) | 2.8.7 | En uso |
| Retrofit + OkHttp + Gson | 2.9.0 / 4.12.0 | **Implementado** (ApiClient, ApiService, interceptores JWT, cookie jar, refresh) |
| Room | (libs catalog) + KSP | **En uso** (AppDatabase, entity/dao de operaciones pendientes) |
| WorkManager | (libs catalog) | **En uso** (PendingClosureWorker) |
| Coroutines | 1.7.3 | En uso |
| Firebase (Auth) | — | Mencionado en README; login real via backend REST (JWT) |
| MultiDex | 2.0.1 | En uso |
| Desugar JDK Libs | 2.1.5 | En uso |
| SDK POS (JARs) | SmartPos 1.9.3 / emv 2.0.0 / core 3.2.1 | Presentes en `app/libs`; usados solo en `ScannerActivity` (ZCS), resto comentado |
| MinSDK / TargetSDK | 23 / 34 (compileSdk 35) | — |
| Flavors | emulator / device / production | `BASE_URL_BACKEND` por flavor |

> **Pendientes de dependencia**: NO hay CameraX ni ML Kit / ZXing (lectura EAN13 por cámara aún no integrada). NO hay cliente HTTP/DTOs para Haulmer/OpenFactura.

---

## 2. Arquitectura

### Patrón general: **MVVM híbrido**

- **Vistas legacy**: Fragment + ViewModel + ViewBinding/DataBinding.
- **Compose**: Fragment host → `ComposeView` → Screen composable → ViewModel.
- **Sin DI**: No Hilt/Koin/Dagger. ViewModels manuales.
- **Capa de datos REAL via REST**: `ApiClient` + `ApiService` + repositorios
  (`ProductRepository`, `CustomerRepository`, `ReportRepository`, `CashboxRepository`,
  `SettingsRepository`). `DummyDataRepository` aún existe pero la mayoría de pantallas
  consume datos reales.
- **Persistencia local**: Room (`AppDatabase`) para operaciones de caja pendientes +
  `PendingClosureWorker` (reintento offline de cierres).
- **Auth**: login REST con JWT (`JwtAuthInterceptor`, `JwtRefreshInterceptor`,
  `JwtTokenStorage`, `PersistentCookieJar`), `SessionManager`, `UnlockActivity`.

### Navegación
- Híbrida: Jetpack Navigation (NavGraph) + `startActivityForResult` para pago/escaneo.
- Bottom nav: Home, Products, Payments, History, Notifications.

---

## 3. Grado de implementación por feature

| Feature | Estado | Detalle |
|---|---|---|
| Login / sesión / JWT | ✅ Funcional | REST + interceptores + persistencia |
| Apertura de caja | ✅ Funcional | `POST api/firestore/cashbox/sessions` (open) + pantallas |
| Cierre de caja | ✅ Funcional | `PATCH .../sessions/{id}/close` + worker offline |
| Movimientos de caja | ✅ Funcional | `POST api/firestore/cashbox/movements` |
| Listado de productos / búsqueda | ✅ Funcional | `GET api/products`, `api/products/search/quick` |
| Clientes | ✅ Funcional | `GET api/supabase/customers` |
| Reportes / historial de ventas | ✅ Funcional (lectura) | `GET api/sales` |
| Carrito / calculadora de venta | 🟡 Parcial | `SalesCalculatorViewModel` en memoria; sin checkout ni POST |
| **Lectura EAN13 por cámara** | ❌ No iniciado | `ScannerActivity` usa SDK ZCS; `BarcodeScannerScreen` es mock visual |
| **Registro de venta (REST)** | ❌ Falta endpoint | No existe `POST api/sales`; ver §5 |
| **Ticket apertura/cierre/venta** | ❌ No iniciado | `TicketsScreen` = placeholder "Próximamente"; `PrinterManager` stub comentado |
| **Pago por transferencia** | ❌ No iniciado | — |
| **Pago con tarjeta (Haulmer)** | ❌ No iniciado | Sin cliente REST OpenFactura |
| Impresión física | ❌ No iniciado | `PrinterManager` todo comentado (JAR SmartPos) |

---

## 4. Plan de trabajo (etapa 1: hasta vista previa de ticket, sin imprimir)

Objetivo: avanzar sin quedar bloqueados por hardware (impresora/lector). En etapa 1
**no se imprime**: se muestra el ticket en pantalla (preview). La impresión física
(`PrinterManager` + JAR SmartPos) queda para etapa 2.

### Fase 0 — Cimientos compartidos
- Definir modelo `Ticket` (header tienda/caja/cajero/fecha, líneas, totales, pie) y un
  composable `TicketPreviewScreen` reutilizable para los 3 tipos (apertura, cierre, venta).
- Interfaz `TicketRenderer` con impl `ScreenTicketRenderer` (etapa 1) para luego sumar
  `PrinterTicketRenderer` (etapa 2) sin tocar los flujos.

### Fase 1 — Ticket de apertura de caja
- La apertura ya funciona vía REST → al éxito, construir `Ticket` desde la sesión y
  mostrar `TicketPreviewScreen`.

### Fase 2 — Ticket de cierre de caja
- El cierre ya funciona → mostrar preview con totales/arqueo de la sesión cerrada.

### Fase 3 — Lectura EAN13 por cámara
- Agregar **CameraX + ML Kit Barcode** (o ZXing) como módulo `scanner` desacoplado del
  SDK ZCS. Reemplazar el mock de `BarcodeScannerScreen` por preview de cámara real.
- Al escanear EAN13 → `api/products/search/quick` → agregar al carrito.

### Fase 4 — Registro de venta + ticket de compra
- **Bloqueante**: definir/confirmar `POST api/sales` (ver §5).
- Checkout: `SalesCalculatorViewModel` → DTO de venta → POST → `Ticket` de compra → preview.

### Fase 5 — Pago por transferencia
- Método de pago "transferencia" en el checkout (registro de venta con `payment_method`).

### Fase 6 — Pago con tarjeta (Haulmer/OpenFactura)
- Cliente REST OpenFactura (Retrofit independiente con su `BASE_URL` de `ApiConfig`).
- Emisión de boleta/factura DTE + asociación al registro de venta.

### Etapa 2 (posterior)
- `PrinterTicketRenderer` sobre JAR SmartPos para impresión física de los 3 tickets.
- Mantener (o no) la vista previa según performance.

---

## 5. Endpoints REST faltantes / a confirmar

1. **`POST api/sales`** (o equivalente) para registrar la venta: items, totales, cliente,
   `payment_method` (efectivo / transferencia / tarjeta), sesión de caja, y respuesta con
   `ticket_number`. **No existe en `ApiService` ni en el backend conocido** → confirmar si
   ya está en otro proyecto paralelo al POS WEB.
2. Posible endpoint para **pago/confirmación de transferencia** (si el backend valida).
3. Integración **Haulmer/OpenFactura** (`/dte/emit`, `/dte/status/{token}`): definir cómo
   se enlaza el DTE con la venta registrada.

---

## 6. Estructura del Proyecto

```
FriendlyPOS/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml      # Permisos: BT, NFC, Camera, Network, Location
│       ├── java/cl/friendlypos/mypos/
│       │   ├── api/                 # ApiClient, ApiService, DTOs, interceptores JWT
│       │   ├── repository/          # Product/Customer/Report/Cashbox/Settings
│       │   ├── db/                  # Room: AppDatabase, entity, dao
│       │   ├── work/                # PendingClosureWorker
│       │   ├── model/               # data classes
│       │   ├── hardware/            # CardReaderManager, PrinterManager (stub)
│       │   ├── ui/                  # Fragments + ViewModels legacy
│       │   ├── compose/             # Screens + ViewModels + components
│       │   └── utils/
│       ├── res/
│       └── libs/                    # JARs SDK POS (SmartPos, emv, core)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml
```
