# FriendlyPOS | Android app

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
├── build.gradle.kts              # Root Gradle build
├── settings.gradle.kts           # module :app
├── gradle.properties             # AndroidX, Jetifier, Compose experimental
└── gradle/libs.versions.toml     # Version catalog
```

---
