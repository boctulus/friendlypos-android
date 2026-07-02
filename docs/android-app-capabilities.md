# FriendlyPOS — Capacidades de la App Android

## Visión General

FriendlyPOS es un sistema POS (Point of Sale) Android que se conecta a un backend NodeJS (repositorio externo en `D:\nodejs\friendlypos_nodejs\`). La app está construida con Kotlin + Jetpack Compose + Material 3, con fragments legacy XML coexistiendo.

---

## Lo que Falta — MVP vs Producto Completo

### MVP (prioridad — sin esto la app no sirve en producción)

| # | Feature | Esfuerzo |
|---|---|---|
| 1 | **Impresión ESC/POS básica** — ticket de venta (apertura/cierre caja post-MVP) | Semanas |
| 2 | **Pago con tarjeta vía WEB (POS azul)** — init + polling + confirm | Semanas |

---

## Capacidades Actuales

### 1. Autenticación y Sesión

| Capacidad | Estado | Detalle |
|---|---|---|
| Login con email/contraseña | ✅ Implementado | `POST /api/auth/login` → JWT (access + refresh tokens) |
| Persistencia de sesión | ✅ Implementado | SharedPreferences + JWT token storage |
| Refresh automático de token | ✅ Implementado | `JwtRefreshInterceptor` maneja HTTP 401 → refresh → retry |
| Logout | ✅ Implementado | Limpia sesión, tokens, cookies |
| Recordar último email | ✅ Implementado | SharedPreferences |
| Roles (admin / cashier / supervisor) | ✅ Implementado | Claims extraídos del JWT, UI se adapta según rol |
| Switch de cajero con PIN | ⏳ No iniciado | Planeado: `POST /api/auth/verify-pin` + `POST /api/auth/switch-cashier` |
| Fingerprint / biometría | 🔧 Parcial | `FingerprintUtils` existe pero no está integrado en flujo de login |

### 2. Módulo de Caja (Cashbox)

| Capacidad | Estado | Detalle |
|---|---|---|
| Ver tiendas del usuario | ✅ Implementado | `GET /api/firestore/cashbox/user-stores` |
| Ver disponibilidad de cajas | ✅ Implementado | `GET /api/firestore/cashbox/availability` |
| Abrir sesión de caja | ✅ Implementado | `POST /api/firestore/cashbox/sessions` con monto inicial |
| Ver sesión actual | ✅ Implementado | `GET /api/firestore/cashbox/sessions/current` |
| Cerrar sesión de caja | ✅ Implementado | `PATCH /api/firestore/cashbox/sessions/{id}/close` con validación de diferencia |
| Registrar movimientos (ingreso/egreso/ajuste) | ✅ Implementado | `POST /api/firestore/cashbox/movements` con 8 tipos de movimiento |
| Validación de diferencia al cerrar | ✅ Implementado | Cálculo % diferencia, nota obligatoria si ≥1%, diálogo por nivel (rojo/naranja/azul) |
| Reintento de cierre fallido offline | ✅ Implementado | Room + WorkManager — `PendingClosureWorker` reintenta con backoff exponencial |
| Vista supervisor (todas las cajas abiertas) | ✅ Implementado | Rol "supermarket" ve todas las sesiones activas |
| Regla: 1 caja abierta por terminal | ✅ Implementado | Validación client-side |

### 3. Módulo de Ventas

| Capacidad | Estado | Detalle |
|---|---|---|
| Calculadora de ventas (keypad numérico) | ✅ Implementado | `SalesCalculatorFragment` con entrada manual de montos |
| Carrito de compras | ✅ Implementado | `CartFragment` con items, subtotales, total |
| Búsqueda de productos en modal venta | ✅ Implementado | `GET /api/products/search/quick` con resultados en tiempo real |
| Selección de tipo de documento | ✅ Implementado | 5 tipos: Documento afecto/exento, Factura afecta/exenta, Sin documento |
| Pago en efectivo | ✅ Implementado | Ingreso de monto recibido, cálculo de vuelto |
| **Registro de venta (POST /api/sales)** | ❌ Dummy | El carrito se limpia localmente sin enviar datos al backend |
| Pago con tarjeta vía WEB (POS azul) | ⏳ No iniciado | Planeado: `POST /api/payment/init` + polling + confirmación |
| Split payment (efectivo + tarjeta + transferencia) | ⏳ No iniciado | Planeado |
| Pago por transferencia | ⏳ No iniciado | Planeado |

### 4. Productos e Inventario

| Capacidad | Estado | Detalle |
|---|---|---|
| Lista de productos con paginación | ✅ Implementado | `GET /api/products` con cursor-based pagination |
| Búsqueda de productos | ✅ Implementado | `GET /api/products/search/quick?q=...` |
| Vista de inventario con stock | ✅ Implementado | Misma data de productos, muestra stock + alerta de stock bajo |
| Categorías de productos | ✅ Implementado | Filtro por categoría en UI |
| CRUD de productos | ❌ No implementado | Solo lectura por ahora |

### 5. Clientes

| Capacidad | Estado | Detalle |
|---|---|---|
| Lista de clientes con paginación | ✅ Implementado | `GET /api/supabase/customers` con infinite scroll |
| Búsqueda de clientes | ✅ Implementado | Filtro client-side |
| CRUD de clientes | ❌ No implementado | Solo lectura por ahora |

### 6. Historial y Reportes

| Capacidad | Estado | Detalle |
|---|---|---|
| Lista de pagos/ventas | ✅ Implementado | `GET /api/sales` con filtros por fecha y búsqueda por cliente |
| Historial de transacciones | ✅ Implementado | Vista con resumen (header) + detalle |
| Reportes con gráfico | ✅ Implementado | Canvas chart + summary (totales, clientes, productos) |
| Filtros rápidos de fecha | ✅ Implementado | Esta semana, Este mes, Este año + selector personalizado |
| Filtros por rango de fecha | ✅ Implementado | DatePickerDialog con fechas desde/hasta |

### 7. Notificaciones

| Capacidad | Estado | Detalle |
|---|---|---|
| Lista de notificaciones | ✅ Implementado | UI completa con tipos (success, warning, error, info) |
| Filtros por tipo y fecha | ✅ Implementado | UI completa |
| Marcado como leído/no leído | ✅ Implementado | UI completa |
| **Datos reales desde API** | ❌ Dummy | Actualmente usa `DummyDataRepository` con 8 notificaciones hardcodeadas |

### 8. Escáner de Código de Barras

| Capacidad | Estado | Detalle |
|---|---|---|
| SDK ZCS (hardware propietario) | 🔧 Parcial | `ScannerActivity` tiene imports vivos + `initSdk()` real, pero barcode listener comentado |
| ML Kit (cámara) | ⏳ Planeado | Reemplazará al SDK ZCS |
| BarcodeScannerScreen (duplicada) | 🔧 Bug | Dos screens casi idénticas: `BarcodeScannerScreen.kt` y `BarcodeScannerDemoScreen.kt` |

### 9. Hardware (Impresora y Lector de Tarjetas)

| Capacidad | Estado | Detalle |
|---|---|---|
| PrinterManager | ❌ Stub | Clase definida con métodos, toda la lógica comentada |
| CardReaderManager | ❌ Stub | Clase definida con métodos, toda la lógica comentada |
| Impresión ESC/POS estándar (USB/Bluetooth) | ⏳ Planeado | Reemplazará al PrinterManager actual |
| Pago con tarjeta vía POS azul externo | ⏳ Planeado | Reemplazará al CardReaderManager |
| SDK ZCS (3 JARs) | 🔧 A eliminar | `core-3.2.1.jar`, `emv_2.0.0_R240607.jar`, `SmartPos_1.9.3_R241021.jar` |
| Assets EMV | 🔧 A eliminar | `assets/emv/AidRec.data`, `CapkRec.data` |

### 10. Infraestructura Técnica

| Capacidad | Estado | Detalle |
|---|---|---|
| API Client (Retrofit + OkHttp) | ✅ Implementado | Timeouts configurados, logging BODY |
| JWT Auth Interceptor | ✅ Implementado | Agrega `Authorization: Bearer` automáticamente |
| JWT Refresh Interceptor | ✅ Implementado | Thread-safe, con lock para evitar refreshes paralelos |
| Offline queue (Room) | ✅ Parcial | Solo para cierres de caja pendientes |
| WorkManager (background retry) | ✅ Implementado | `PendingClosureWorker` con backoff exponencial + constraints de red |
| MultiDex | ✅ Implementado | Para compatibilidad con librerías legacy |
| Desugaring | ✅ Implementado | Para compatibilidad Java 6/7 |
| ViewBinding + DataBinding | ✅ Implementado | En vistas legacy |
| Jetpack Compose + Material 3 | ✅ Implementado | BOM 2024.09.03 |
| Navigation Component | ✅ Implementado | Híbrido: NavGraph + startActivityForResult |
| Room Database | ✅ Implementado | Solo tabla `PendingCashboxOperation` |
| DI (Hilt/Koin/Dagger) | ❌ No implementado | ViewModels con `ViewModelProvider()` manual |
| Testing | ❌ No implementado | Dependencias en gradle, cero tests escritos |

---

## Stack Tecnológico Resumido

| Capa | Tecnología | Versión |
|---|---|---|
| Lenguaje | Kotlin + Java | 2.1.0 / 17 |
| UI | Jetpack Compose + Material 3 + XML legacy | BOM 2024.09.03 |
| Arquitectura | MVVM híbrido (sin capa de dominio) | — |
| API | Retrofit + OkHttp + Gson | 2.9.0 / 4.12.0 |
| Auth | JWT (access + refresh tokens) | — |
| Base de datos local | Room | — |
| Background work | WorkManager | — |
| Navegación | Navigation Component (híbrida) | 2.8.5 |
| Min SDK / Target | 23 (Android 6.0) / 34 (Android 14) | — |
| Backend | NodeJS (repositorio externo) | — |
