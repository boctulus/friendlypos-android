# Hallazgos — FriendlyPOS

> Relevamiento de scaffolding, patrones, tecnologías, arquitectura y grado de implementación real.
> Fecha: 2026-05-08

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
│  │                  └── CashPaymentActivity   │
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
- **Sin API**: Retrofit/OkHttp en gradle pero cero código de API.
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

6. **Hardware managers scaffold**: `CardReaderManager` y `PrinterManager` tienen clases definidas con métodos, pero toda la implementación está comentada. El SDK de hardware ZCS está comentado en `MainActivity` y `ScannerActivity`.

7. **Assets EMV presentes**: `assets/emv/AidRec.data` y `CapkRec.data` indican que se planeó integración con pagos EMV (tarjetas de crédito/débito chip).

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
| Flujo de ventas (calculadora + carrito) | `SalesCalculatorFragment`, `CartFragment`, `SalesCalculatorViewModel` | 90% | 90% (cart logic real, pero sin API) |
| Pago en efectivo | `CashPaymentActivity` | 90% | 50% (procesamiento dummy) |
| Selección de documento tributario | `BillingActivity` | 100% | 100% (retorna selección) |
| Home Dashboard | `HomeFragment` | 100% | 100% (navegación funcional) |
| Setup de API Key | `SetupAccessFragment` | 100% | 100% (SharedPreferences) |

### 5.2 UI completa, datos dummy

| Feature | UI | Datos | Fuente |
|---|---|---|---|
| Products | LazyColumn + search | 10 productos hardcodeados | `DummyDataRepository` |
| Customers | LazyColumn + search | 8 clientes hardcodeados | `DummyDataRepository` |
| Inventory | Stock + low-stock warning | Mismos 10 productos | `DummyDataRepository` |
| Reports | Date pickers + Canvas chart + summary | 4 ventas hardcodeadas | `DummyDataRepository` |
| Payments | Search + date filters + list | Mismas 4 ventas | `DummyDataRepository` |
| History | Summary header + filters + detail | Mismas 4 ventas | `DummyDataRepository` |
| Notifications | Type/date filters + read/unread | 8 notificaciones hardcodeadas | `DummyDataRepository` |

### 5.3 Parcial / Buggy

| Feature | Problema |
|---|---|
| Barcode Scanner | SDK init parcialmente comentado, listener comentado. Dos screens casi idénticas (una es refactor abandonado). |
| `SalesCalculatorViewModel.processSale()` | Posible bug: usa `saleItems.value?.toIntOrNull()` sobre estado incorrecto. |
| Código SDK comentado | `MainActivity`, `ScannerActivity`, `CardReaderManager`, `PrinterManager` — todo hardware está comentado para compilar sin SDK físico. |

### 5.4 Scaffold / Placeholder solamente

| Feature | Estado |
|---|---|
| `CardReaderManager` | Clase vacía, toda la lógica comentada |
| `PrinterManager` | Clase vacía, toda la lógica comentada |
| `CashFundActivity` | Activity sin lógica, solo infla binding |
| `DashboardFragment` + `DashboardViewModel` | Template muerto de Android Studio |
| `HomeViewModel` | No usado por `HomeFragment` |
| `NotificationsViewModel` (en `ui/`) | No usado, el de `compose/` es el real |
| `ViewPagerAdapter` | Nunca instanciado |

### 5.5 No iniciado (planeado)

| Feature | Documentación | Código |
|---|---|---|
| Integración API REST | `API_INTEGRATION_PLAN.md` (6 fases, estructura completa) | 0% |
| Firebase Auth | Plan con `TokenProvider`, `AuthInterceptor`, `TokenRefreshInterceptor` | 0% |
| Repositorios (Product, Customer, Sale, etc.) | 7 repos planeados | 0% |
| DTOs | 5 DTOs planeados | 0% |
| `SalePendingQueue` | Diseño con estados PENDING/SENDING/FAILED/SYNCED | 0% |
| `ConnectivityObserver` | Plan con ping a `/health` + debounce | 0% |
| DTE (facturación electrónica) | Flujo async completo con polling y backoff | 0% |
| Room database | Explícitamente fuera de scope para MVP | 0% |
| DI (Hilt/Koin) | No planeado | 0% |

### 5.6 Resumen por capa

| Capa | % Real | Notas |
|---|---|---|
| UI (View legacy) | ~80% | Flujo ventas completo, hardware integrado pero comentado |
| UI (Compose) | ~70% | UI completa, todos los datos son dummy |
| ViewModel | ~70% | Lógica de UI funcional, sin conexión a datos reales |
| Data Layer | 0% | `DummyDataRepository` debe ser reemplazado por completo |
| API Layer | 0% | Retrofit presente en gradle, cero código |
| Domain Layer | 0% | No existe |
| DI | 0% | No existe |
| Hardware | 5% | Managers definidos, lógica 100% comentada |
| Navigation | 70% | Híbrida funcional, back-stack incompleto en bottom nav |
| Testing | ~5% | Dependencias en gradle, sin tests escritos |

---

## 6. Observaciones Clave

1. **La app compila y ejecuta un flujo POS funcional** pero completamente aislado (sin backend, sin API, sin datos reales).

2. **cmd/** es un CLI tool de NodeJS (sistema de comandos auto-descubribles, abstracción multi-base de datos, controladores de auth, utilidades). El backend REST real referido en la documentación está en `D:\nodejs\friendlypos_nodejs\` (fuera de este repositorio).

3. **Hay una deuda técnica considerable por el dual UI paradigm**. Migrar completamente a Compose eliminaría 33 layouts XML y ~6 fragments legacy.

4. **El plan de integración API está muy bien definido** (endpoints, estructura, flujos, manejo de errores). La implementación es directa siguiendo ese plan.

5. **Los assets EMV (`AidRec.data`, `CapkRec.data`) y permisos NFC/BT sugieren** que el target son terminales POS Android con hardware físico (lector chip, impresora térmica).

6. **Sin DI ni testing**, la mantenibilidad a largo plazo está comprometida. Cada ViewModel crea sus propias dependencias, lo que hace difícil mockear en tests.
