# Hallazgos — FriendlyPOS

> Relevamiento de scaffolding, patrones, tecnologías, arquitectura y grado de implementación real.
> Fecha: 2026-05-24

---

## 1. Tecnologías

### Android App (`app/`)

| Tecnología | Versión | Estado |
|---|---|---|
| Kotlin | 2.1.0 | En uso |
| AGP | 8.7.3 | En uso |
| Jetpack Compose + Material 3 | BOM 2024.09.03 | En uso |
| Compose Compiler | Kotlin 2.1.0 plugin | En uso |
| ViewBinding / DataBinding | — | En uso (legacy views) |
| Navigation Component | 2.8.5 | En uso |
| Lifecycle (LiveData, ViewModel) | 2.8.7 | En uso |
| Retrofit + OkHttp + Gson | 2.9.0 / 4.12.0 | **Solo dependencias — sin implementación** |
| Coroutines | 1.7.3 | En uso |
| Firebase (Auth) | — | Mencionado en README, sin dependencias visibles en build.gradle.kts |
| MultiDex | 2.0.1 | En uso |
| Desugar JDK Libs | 2.1.5 | En uso |
| MinSDK / TargetSDK | 23 / 34 | — |
| ConstraintLayout | 1.1.3 / 2.2.1 | Legacy views |
| Material (View) | 1.12.0 | Legacy views |

### Backend / CLI (`cmd/`)

| Tecnología | Versión | Estado |
|---|---|---|
| Node.js | — | CLI tool |
| Supabase JS | ^2.90.1 | Dependencia |
| Firebase Admin | ^12.0.0 | Dependencia |
| PostgreSQL (pg) | ^8.16.3 | Dependencia |
| MySQL (mysql2) | ^3.14.3 | Dependencia |
| SQLite3 | ^5.1.7 | Dependencia |
| Redis (ioredis) | ^5.6.1 | Dependencia |
| Winston (logging) | ^3.17.0 | Dependencia |
| Yargs (CLI parsing) | ^17.7.2 | Dependencia |
| Jest | ^29.7.0 | Testing |
| Babel | ^7.28.0 | Transpilación |

> **Nota:** `cmd/` es un **CLI tool**, no un servidor REST API. El backend REST real está referenciado en `D:\nodejs\friendlypos_nodejs\` (fuera de este repo).

---

## 2. Arquitectura

### Android App

```
┌─────────────────────────────────────────────┐
│  MainActivity (NavHost + BottomNavigation)   │
│  ├── UnlockActivity (pre-session)            │
│  └── ScannerActivity (barcode)               │
│                                               │
│  Fragments (legacy views):                    │
│  ├── HomeFragment (dashboard grid)            │
│  ├── SalesCalculatorFragment (keypad + cart)  │
│  ├── CartFragment (shared VM con SalesCalc)   │
│  ├── PaymentActivity → BillingActivity        │
│  │                  ├── CashPaymentActivity   │
│  │                  ├── WebPaymentActivity    │  ← PLANIFICADO
│  │                  └── SplitPaymentActivity  │  ← PLANIFICADO
│  └── SetupAccessFragment (API key)            │
│                                               │
│  Fragments (Compose hosts):                   │
│  ├── ProductsFragment → ProductsScreen        │
│  ├── CustomersFragment → CustomersScreen      │
│  ├── InventoryFragment → InventoryScreen      │
│  ├── ReportsFragment → ReportsScreen          │
│  ├── PaymentsFragment → PaymentsScreen        │
│  ├── HistoryFragment → HistoryScreen          │
│  └── NotificationsFragment → NotifScreen      │
│                                               │
│  ViewModels:                                  │
│  ├── SalesCalculatorViewModel (cart logic)    │
│  ├── ProductsViewModel (StateFlow)            │
│  ├── CustomersViewModel (StateFlow)           │
│  ├── InventoryViewModel (StateFlow)           │
│  ├── ReportsViewModel (StateFlow)             │
│  ├── PaymentsViewModel (StateFlow)            │
│  ├── NotificationsViewModel (StateFlow)       │
│  └── BarcodeScannerViewModel (StateFlow)      │
│                                               │
│  Data:                                        │
│  └── DummyDataRepository (object singleton)   │
│       → flowOf() con datos hardcodeados       │
│                                               │
│  Hardware (scaffold):                         │
│  ├── CardReaderManager (todo comentado)       │
│  └── PrinterManager (todo comentado)          │
└─────────────────────────────────────────────┘
```

### Patrón general: **MVVM híbrido**

- **Vistas legacy**: Fragment + ViewModel + ViewBinding/DataBinding
- **Compose**: Fragment host → `ComposeView` → Screen composable → ViewModel via `viewModel()`
- **Sin DI**: No Hilt, Koin ni Dagger. ViewModels con `ViewModelProvider()` o `viewModel()` manual.
- **Sin capa de dominio**: No hay use cases ni interactors.
- **Sin capa de datos real**: `DummyDataRepository` es singleton con datos hardcodeados.
- **API parcial**: Retrofit/OkHttp con `ApiClient`, `ApiService`, interceptores JWT implementados. Consume datos reales de productos, clientes, pagos, reportes. Falta `POST /api/sales` (registro venta dummy).
- **Sin Room**: No hay base de datos local.

### Navegación

- **Híbrida**: Jetpack Navigation (NavGraph) para fragments + `startActivityForResult` para flujos de pago/escaneo.
- Bottom nav con 5 tabs: Home, Products, Payments, History, Notifications.
- Solo `navigation_home` está configurada como destino top-level en `AppBarConfiguration`.
- Los otros 4 tabs navegan programáticamente sin back-stack management completo.

---

## 3. Estructura del Proyecto

```
FriendlyPOS/
├── app/                          # Android App (Kotlin)
│   ├── build.gradle.kts          # Dependencias de la app
│   └── src/main/
│       ├── AndroidManifest.xml   # Permisos: BT, NFC, Camera, Network, Location
│       ├── java/cl/friendlypos/mypos/
│       │   ├── *.kt / *.java     # 56 Kotlin + 4 Java (67 source files)
│       │   ├── model/            # 6 data classes (Product, Customer, Sale, etc.)
│       │   ├── data/             # 1 archivo (DummyDataRepository)
│       │   ├── hardware/         # 2 managers (scaffold)
│       │   ├── ui/               # Fragments + ViewModels legacy + Compose hosts
│       │   ├── compose/          # Screens + ViewModels + components
│       │   └── utils/            # BitmapUtils, DialogUtils, etc.
│       ├── res/
│       │   ├── layout/           # 33 layouts XML
│       │   ├── drawable/         # 50 recursos gráficos
│       │   ├── menu/             # 3 menús
│       │   ├── navigation/       # 1 NavGraph
│       │   └── values/           # colors, strings, styles, themes
│       └── assets/
│           ├── font/             # Montserrat-Regular.ttf
│           └── emv/              # AidRec.data, CapkRec.data (pagos EMV)
├── cmd/                          # NodeJS CLI tool (no REST API)
│   ├── core/                     # CLI engine, database abstraction, libs
│   ├── controllers/              # AuthController, FirebaseBaseController
│   ├── models/                   # UsersModel
│   ├── commands/                 # 4 grupos: file, sql, skill, supabase
│   ├── config/                   # databases, modules, supabase configs
│   └── scripts/                  # Utilidades
├── docs/
│   └── API_INTEGRATION_PLAN.md   # Plan detallado para integrar API REST
├── .claude/                      # Skills y configs del agente
├── .opencode/                    # Skills de opencode
├── build.gradle.kts              # Root Gradle build
├── settings.gradle.kts           # module :app
├── gradle.properties             # AndroidX, Jetifier, Compose experimental
└── gradle/libs.versions.toml     # Version catalog
```

---

## 4. Patrones en el Scaffolding

### Scaffolding existente

1. **Dual UI paradigm**: Vistas XML legacy + Jetpack Compose conviviendo. Las pantallas Compose están envueltas en Fragments con `ComposeView`, lo que agrega complejidad innecesaria.

2. **ViewModel compartido por scope de Activity**: `SalesCalculatorFragment` y `CartFragment` comparten `SalesCalculatorViewModel` via `requireActivity()`. Esto permite comunicación directa entre fragments hermanos sin navegación.

3. **DummyDataRepository como singleton**: Un único `object` con datos hardcodeados (10 productos, 8 clientes, 4 ventas, 8 notificaciones) alimenta todas las pantallas Compose vía `StateFlow`.

4. **Dos BarcodeScannerScreen casi idénticas**: `BarcodeScannerScreen.kt` y `BarcodeScannerDemoScreen.kt` coexisten. La Activity usa `BarcodeScannerDemoScreen`. La otra parece un refactor abandonado.

5. **ViewModels legacy muertos**: `HomeViewModel`, `DashboardFragment`+`DashboardViewModel`, y `NotificationsViewModel` (package `ui/`) no son usados por nadie. Son resabios de templates de Android Studio.

6. **Hardware managers scaffold + SDK ZCS a eliminar**: `CardReaderManager` y `PrinterManager` son stubs con lógica comentada. El SDK ZCS (`app/libs/core-3.2.1.jar`, `emv_2.0.0_R240607.jar`, `SmartPos_1.9.3_R241021.jar`) se usaba para lector chip, NFC y escáner de código de barras. **Se elimina por completo** — la terminal destino ya no usa este fabricante. El escáner se reemplazará por cámara estándar (ML Kit). La impresión usará protocolos estándar (USB/Bluetooth térmica). La lectura de tarjetas se delega al POS azul vía API.

7. **Assets EMV a eliminar**: `assets/emv/AidRec.data` y `CapkRec.data` formaban parte del SDK ZCS. Se eliminan junto con los JARs. La nueva arquitectura no requiere datos EMV locales.

8. **API plan detallado pero sin código**: `API_INTEGRATION_PLAN.md` tiene 6 fases, estructura de archivos, endpoints, flujo DTE async, idempotency keys, y SalePendingQueue. Cero líneas escritas.

### Modelos duplicados / inconsistentes

| Concepto | Ubicación 1 | Ubicación 2 | Problema |
|---|---|---|---|
| SaleItem | `model/Sale.kt` (productId, productName, subtotal) | `ui/sales/SaleItem.kt` (UUID, unitPrice, quantity) | No unificados |
| NotificationsViewModel | `ui/notifications/` (legacy) | `compose/viewmodel/` (en uso) | Código muerto |
| Dashboard | `ui/dashboard/` (legacy, no referenciado) | `HomeFragment` (en uso) | Template muerto |

---

## 5. Grado de Implementación Real

### 5.1 Completamente implementado (funcional, sin datos reales)

| Feature | Archivos | % UI | % Datos |
|---|---|---|---|
| Flujo de ventas (calculadora + carrito) | `SalesCalculatorFragment`, `CartFragment`, `SalesCalculatorViewModel` | 90% | 90% (búsqueda productos via API real, registro venta dummy) |
| Pago en efectivo (único método disponible) | `CashPaymentActivity` | 90% | 50% (procesamiento dummy — sin POST a backend) |
| Selección de documento tributario | `BillingActivity` | 100% | 100% (retorna selección) |
| Home Dashboard | `HomeFragment` | 100% | 100% (navegación funcional) |
| Setup de API Key | `SetupAccessFragment` | 100% | 100% (SharedPreferences) |

### 5.2 UI completa, datos dummy

| Feature | UI | Datos | Fuente |
|---|---|---|---|
| Notifications | Type/date filters + read/unread | 8 notificaciones hardcodeadas | `DummyDataRepository` |

### 5.2.1 UI completa, datos reales (JWT auth — 2026-05-17)

| Feature | UI | Datos | Fuente |
|---|---|---|---|
| Búsqueda de productos (modal calculadora) | Modal con loading/error/resultados | API real | `GET /api/products/search/quick` |
| Inventario / Productos | Lista + buscador + stock-bajo warning | API real | `GET /api/products` |
| Clientes | Lista + buscador + infinite scroll | API real | `GET /api/supabase/customers` |
| Pagos | Search + date filters + list | API real | `GET /api/sales` |
| Historial de Transacciones | Summary header + filters + detail | API real | `GET /api/sales` |
| Reports | Date pickers + Canvas chart + summary | API real | `GET /api/sales` |

---

### 5.3 Parcial / Buggy

| Feature | Problema |
|---|---|
| Barcode Scanner | SDK ZCS activo en `ScannerActivity.kt` (3 imports vivos + `initSdk()` real), pero barcode manager comentado. Dos screens casi idénticas (una es refactor abandonado). El SDK ZCS completo será eliminado. |
| `SalesCalculatorViewModel.processSale()` | Posible bug: usa `saleItems.value?.toIntOrNull()` sobre estado incorrecto. |
| Código SDK ZCS mezclado | `ScannerActivity.kt` tiene imports vivos de `com.zcs.sdk.*` + `initSdk()` real; `MainActivity.kt` lo tiene comentado. `CardReaderManager` y `PrinterManager` son stubs. Todo el SDK ZCS (3 JARs) será eliminado. |

### 5.4 Scaffold / Placeholder solamente

| Feature | Estado |
|---|---|
| `CardReaderManager` | Eliminar — reemplazado por API de pago vía WEB (POS azul) |
| `PrinterManager` | Refactorizar a impresión estándar (USB/Bluetooth térmica, sin SDK ZCS) |
| JARs SDK ZCS (`core-3.2.1.jar`, `emv_2.0.0_R240607.jar`, `SmartPos_1.9.3_R241021.jar`) | Eliminar — la terminal destino usa otro fabricante |
| Assets EMV (`assets/emv/AidRec.data`, `CapkRec.data`) | Eliminar — vestigios del SDK ZCS |
| `CashFundActivity` | Activity sin lógica, solo infla binding |
| `DashboardFragment` + `DashboardViewModel` | Template muerto de Android Studio |
| `HomeViewModel` | No usado por `HomeFragment` |
| `NotificationsViewModel` (en `ui/`) | No usado, el de `compose/` es el real |
| `ViewPagerAdapter` | Nunca instanciado |

### 5.5 No iniciado (planeado)

| Feature | Documentación | Código |
|---|---|---|
| Integración API REST | `API_INTEGRATION_PLAN.md` (6 fases, estructura completa) | Fase 1 parcial |
| Firebase Auth | Plan con `TokenProvider`, `AuthInterceptor`, `TokenRefreshInterceptor` | 0% |
| Repositorios (Product, Customer, Sale, etc.) | 7 repos planeados | `ProductRepository` ✅ `CustomerRepository` ✅ |
| DTOs | 5 DTOs planeados | `ProductDto` ✅ `CustomerDto` ✅ |
| `SalePendingQueue` | Diseño con estados PENDING/SENDING/FAILED/SYNCED | 0% |
| `ConnectivityObserver` | Plan con ping a `/health` + debounce | 0% |
| DTE (facturación electrónica) | Flujo async completo con polling y backoff | 0% |
| Eliminación SDK ZCS | Remover JARs, imports, assets EMV, `CardReaderManager`, refactor `PrinterManager` y `ScannerActivity` | **Urgente** (bloquea compilación en terminal destino) |
| Pago vía WEB (POS azul) | Pago con tarjeta delegado a terminal externo vía backend | 0% |
| Split payment | División de pago entre efectivo + tarjeta + transferencia | 0% |
| Pago por transferencia | Débito/crédito vía transferencia bancaria | 0% |
| Switch cajeros con PIN | Login rápido con PIN 4 dígitos para cambio de cajero | 0% |
| Impresión tickets estándar | Apertura/cierre de caja, ticket venta, re-impresión con protocolo estándar | 0% (PrinterManager a refactorizar) |
| Generación boletas/facturas | Conversión venta → boleta/factura imprimible (sin DTE) | 0% |
| Seguridad por roles | Restricción de funcionalidades según rol del usuario | 0% |
| Room database | Explícitamente fuera de scope para MVP | 0% |
| DI (Hilt/Koin) | No planeado | 0% |

### 5.6 Resumen por capa

| Capa | % Real | Notas |
|---|---|---|
| UI (View legacy) | ~80% | Flujo ventas completo, hardware integrado pero comentado |
| UI (Compose) | ~70% | UI completa, todos los datos son dummy |
| ViewModel | ~70% | Lógica de UI funcional, sin conexión a datos reales |
| Data Layer | 0% | `DummyDataRepository` debe ser reemplazado por completo |
| API Layer | 40% | ApiClient, ApiService, interceptores JWT implementados. Consume datos reales (GET). Falta POST /api/sales |
| Domain Layer | 0% | No existe |
| DI | 0% | No existe |
| Hardware | 5% | Managers definidos, lógica 100% comentada |
| Navigation | 70% | Híbrida funcional, back-stack incompleto en bottom nav |
| Testing | ~5% | Dependencias en gradle, sin tests escritos |

---

## 6. Observaciones Clave

1. **La app compila y ejecuta un flujo POS funcional con integración API parcial**. Productos, clientes, pagos, historial y reportes ya consumen datos reales del backend NodeJS vía JWT (sección 5.2.1). Sin embargo, el registro de ventas (`POST /api/sales`) sigue siendo dummy — el carrito solo se limpia localmente sin enviar datos al backend.

2. **cmd/** es un CLI tool de NodeJS (sistema de comandos auto-descubribles, abstracción multi-base de datos, controladores de auth, utilidades). El backend REST real referido en la documentación está en `D:\nodejs\friendlypos_nodejs\` (fuera de este repositorio).

3. **Hay una deuda técnica considerable por el dual UI paradigm**. Migrar completamente a Compose eliminaría 33 layouts XML y ~6 fragments legacy.

4. **El plan de integración API está muy bien definido** (endpoints, estructura, flujos, manejo de errores). La implementación es directa siguiendo ese plan.

5. **SDK ZCS se elimina por completo**: 3 JARs (`core-3.2.1.jar`, `emv_2.0.0_R240607.jar`, `SmartPos_1.9.3_R241021.jar`), assets EMV (`AidRec.data`, `CapkRec.data`), y todo el código asociado (imports, `DriverManager`, `CardReaderManager`). La terminal destino usa otro fabricante. El escáner de código de barras se reemplazará por cámara (ML Kit). La impresión usará protocolos estándar USB/Bluetooth térmica.

6. **Sin DI ni testing**, la mantenibilidad a largo plazo está comprometida. Cada ViewModel crea sus propias dependencias, lo que hace difícil mockear en tests.

7. **`PaymentActivity` solo soporta pago en efectivo**. No hay UI ni flujo para pago con tarjeta, transferencia o split payment. El flujo de pago web con POS azul debe agregarse como nuevo método de pago.

---

## 7. FALTANTES para completar app ANDROID

| # | Feature | Prioridad | Estado actual |
|---|---|---|---|
| 0 | **Eliminación SDK ZCS** — remover JARs (`core-3.2.1.jar`, `emv_2.0.0_R240607.jar`, `SmartPos_1.9.3_R241021.jar`), assets EMV, imports propietarios, `CardReaderManager`; refactor `PrinterManager` a estándar, reemplazar escáner ZCS por ML Kit | **Bloqueante** | SDK activo en `ScannerActivity.kt`, comentado en `MainActivity.kt`, stubs en managers |
| 1 | **Switch entre "cajeros" con PIN 4 dígitos** — acceso rápido con PIN tras login inicial con email/password | Crítico | No implementado |
| 2 | **Impresión tickets apertura y cierre de caja** — reporte de fondo inicial, ventas, retiros, fondo final | Requerido | `PrinterManager` stub, sin lógica |
| 3 | **Pago por transferencia** — generar datos de transferencia o link de pago bancario | Requerido? | No implementado |
| 4 | **Pago con tarjeta vía WEB (POS azul)** — iniciar pago, mostrar QR/link, esperar confirmación del POS externo | Requerido | No implementado |
| 5 | **Split payment** — dividir una venta entre efectivo + tarjeta + transferencia | Importante | No implementado |
| 6 | **Impresión ticket de venta** — ticket con detalle de items, total, método pago, fecha, cajero | Requerido | `PrinterManager` stub |
| 7 | **Registro de la venta** — `POST /api/sales` al backend tras confirmación del pago | Requerido | Dummy (solo limpia carrito local) |
| 8 | **Re-impresión de tickets** — buscar venta por ticket number y re-imprimir | Crítico | No implementado |
| 9 | **Generar boletas/facturas sin DTE** — convertir venta en boleta/factura imprimible (PDF/ticket) | Crítico | No implementado |
| 10 | **Seguridad: restricción por roles** — filtrar UI y acciones según `role` del JWT (admin, cajero, supervisor) | Muy importante | No implementado |
| 11 | **Seguridad: registro de terminal** — procedimiento para registrar esta terminal en el Dashboard NodeJS backend | Opcional | No implementado |

---

## 8. Arquitectura de Pago vía WEB (POS azul)

La app FriendlyPOS **no procesa tarjetas localmente**. La captura de datos de tarjeta (chip, NFC, banda) se delega a un terminal POS Android externo ("POS azul") que se comunica con el mismo backend NodeJS.

> **Aclaración**: El POS azul es un terminal Android que **ya se utiliza como POS WEB** para pagos. No está vinculado al SDK ZCS actual. Usa su propio mecanismo de lectura de tarjetas (API estándar, no librería propietaria). FriendlyPOS se comunica con él exclusivamente a través del backend NodeJS vía API REST. La impresión de tickets en FriendlyPOS usará protocolos **estándar** (USB/Bluetooth térmica ESC/POS), no el SDK ZCS.

### Flujo propuesto

```
FriendlyPOS                     Backend NodeJS                    POS azul (otro terminal)
     │                               │                                  │
     │  1. POST /api/payment/init    │                                  │
     │     { amount, sale_id }       │                                  │
     │◄──── { payment_id, qr/token } │                                  │
     │                               │                                  │
     │  2. Muestra QR / código       │                                  │
     │     en pantalla               │                                  │
     │                               │  3. GET /api/payment/pending     │
     │                               │◄──── POS solicita pendientes ────┤
     │                               │── { payment_id, amount } ──────►│
     │                               │                                  │
     │                               │         4. POS lee tarjeta       │
     │                               │            (chip / NFC / banda)  │
     │                               │                                  │
     │                               │  5. POST /api/payment/confirm    │
     │                               │◄──── { payment_id, result } ─────┤
     │                               │                                  │
     │  6. Polling o WebSocket       │                                  │
     │◄──── { status: "approved" } ──┤                                  │
     │                               │                                  │
     │  7. POST /api/sales           │                                  │
     │     (registra venta)          │                                  │
     │◄──── { sale_id, ticket_url } ─┤                                  │
     │                               │                                  │
     │  8. PrinterManager            │                                  │
     │     imprime ticket            │                                  │
```

### Decisiones de arquitectura

| Decisión | Valor |
|---|---|
| Comunicación POS ↔ Backend | API REST (polling o WebSocket) |
| FriendlyPOS espera pago | Polling a `GET /api/payment/status/:id` con backoff |
| Identificador de pago | UUID generado por backend en paso 1 |
| Timeout de pago | 5 minutos desde creación, configurable |
| Offline | No aplica para pago con tarjeta (requiere conexión) |
| Seguridad | POS azul debe autenticarse contra backend con su propio API key |
| Split payment | Se crea un `payment_id` por cada método; la venta se registra solo cuando todos están aprobados |

### Implicancias en el código existente

1. **`CardReaderManager`**: Eliminar por completo — reemplazado por API de pago vía WEB.
2. **SDK ZCS**: Eliminar 3 JARs de `app/libs/`, remover imports `com.zcs.sdk.*` de `ScannerActivity.kt` y código comentado en `MainActivity.kt`.
3. **Assets EMV**: Eliminar `assets/emv/AidRec.data` y `CapkRec.data`.
4. **`ScannerActivity`**: Refactorizar para usar ML Kit (cámara) en vez del barcode SDK de ZCS.
5. **`PrinterManager`**: Refactorizar a protocolo estándar ESC/POS (USB/Bluetooth térmica) — sin dependencia ZCS.
6. **`PaymentActivity`**: Agregar opciones "Tarjeta", "Transferencia" y "Split" como nuevas `CardView`.
7. **Nuevo `WebPaymentViewModel`**: Maneja el ciclo de vida del pago web (init, polling, timeout, cancelación).
8. **Nuevo `WebPaymentFragment`/`Screen`**: Muestra QR o código de pago, estado del polling, botón cancelar.
9. **API nuevos endpoints**: `POST /api/payment/init`, `GET /api/payment/status/:id` y `POST /api/sales` deben existir en el backend NodeJS.
10. **`build.gradle.kts`**: Remover `implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))` (líneas 100-101 y 160-161) y dependencias de constraintlayout legacy (línea 104).
