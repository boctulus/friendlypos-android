package cl.friendlypos.mypos.compose.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // Primary: icon bg, btn Nuevo, btn Guardar
    val BrandPrimary = Color(0xFF1e31c7)

    // Action: btn Ingresar (login), btn Buscar, title text color
    val BrandAction = Color(0xFFff4e03)

    // Error
    val BrandError = Color(0xFFff0000)

    // Surface: card bg, datagrid bg, btn Cancelar, inactive tabs
    val SurfaceCard = Color(0xFFbdc5cb)

    // Chart teal — bar chart slot 1
    val ChartTeal = Color(0xFF00a48b)

    // Modal: modal window bg, search/filter card bg
    val SurfaceModal = Color(0xFF8a9196)

    // Table / datatable header bg (text on top = SurfaceCard)
    val TableHeaderBg = Color(0xFF283140)

    // Accent yellow
    val AccentYellow = Color(0xFFffce00)

    // Metric purple — metric card slot 1
    val MetricPurple = Color(0xFF8e6dda)

    // Secondary: btn Subir Logo, btn Guardar Contraseña, bar chart slot 2
    val BrandSecondary = Color(0xFF138cb9)

    // Metric card color rotation (use in order when 2+ cards)
    val MetricColors = listOf(MetricPurple, BrandAction, ChartTeal, BrandSecondary)
}
