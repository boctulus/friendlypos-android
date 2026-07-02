# Design Tokens — FriendlyPOS

## Locations

| Layer | File |
|---|---|
| XML (legacy / XML views) | `app/src/main/res/values/colors.xml` |
| Compose | `app/src/main/java/cl/friendlypos/mypos/compose/theme/AppColors.kt` |

---

## Token Reference

| Token name (Compose) | XML name | Hex | Semantic use |
|---|---|---|---|
| `AppColors.BrandPrimary` | `@color/brand_primary` | `#1e31c7` | Icon bg next to section titles; btn **Nuevo**; btn **Guardar** |
| `AppColors.BrandAction` | `@color/brand_action` | `#ff4e03` | Btn **Ingresar** (login); btn **Buscar**; title text color |
| `AppColors.BrandError` | `@color/brand_error` | `#ff0000` | Error states |
| `AppColors.SurfaceCard` | `@color/surface_card` | `#bdc5cb` | Card bg; datagrid bg; btn **Cancelar** bg; inactive tabs bg |
| `AppColors.ChartTeal` | `@color/chart_teal` | `#00a48b` | Bar chart slot 1 |
| `AppColors.SurfaceModal` | `@color/surface_modal` | `#8a9196` | Modal window bg; search / advanced-filter card bg |
| `AppColors.TableHeaderBg` | `@color/table_header_bg` | `#283140` | `<th>` / header row bg in tables and datatables (text = `SurfaceCard`) |
| `AppColors.AccentYellow` | `@color/accent_yellow` | `#ffce00` | Accent |
| `AppColors.MetricPurple` | `@color/metric_purple` | `#8e6dda` | Metric card slot 1 |
| `AppColors.BrandSecondary` | `@color/brand_secondary` | `#138cb9` | Btn **Subir Logo**; btn **Guardar Contraseña**; bar chart slot 2 |

---

## Metric Card Color Rotation

When 2 or more metric cards appear together, cycle through `AppColors.MetricColors`:

```kotlin
// AppColors.MetricColors — ordered rotation
listOf(MetricPurple, BrandAction, ChartTeal, BrandSecondary)
// #8e6dda  #ff4e03  #00a48b  #138cb9
```

---

## Theme Mode

- App forces **Light Mode** globally via `FriendlyPOSApp.onCreate()` (`AppCompatDelegate.MODE_NIGHT_NO`).
- Dark mode is **not supported**. All tokens are light-mode-only values.
- `values-night/themes.xml` exists but is irrelevant while forced light mode is active.
- To add dark theme support in the future: migrate hardcoded hex values in layouts to `?attr/` or `values-night/colors.xml` variants, then remove the `MODE_NIGHT_NO` call.

---

## Rules

- New screens MUST use `AppColors.*` (Compose) or `@color/brand_*` / `@color/surface_*` / `@color/table_*` (XML).
- Legacy XML color names (e.g. `colorAccent`, `colorPrimary`, `successGreen`) are deprecated — do not use in new code.
- Green (`#4CAF50` and variants) is deprecated as a primary UI color.
- Do NOT add `values-night/colors.xml` entries without first removing the forced light mode.
