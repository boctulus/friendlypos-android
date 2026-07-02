package cl.friendlypos.mypos.model

/**
 * Modelo unificado de ticket para previsualización en pantalla (etapa 1) y, más adelante,
 * impresión física (etapa 2). Sirve para apertura de caja, cierre de caja y venta.
 */
enum class TicketType { CASHBOX_OPEN, CASHBOX_CLOSE, SALE }

/**
 * Una línea del ticket. Si [value] es null se renderiza como texto a ancho completo
 * (útil para descripciones de ítems); si tiene valor, se renderiza label a la izquierda
 * y value a la derecha. [emphasize] resalta la línea (totales).
 */
data class TicketLine(
    val label: String,
    val value: String? = null,
    val emphasize: Boolean = false
)

data class Ticket(
    val type: TicketType,
    val title: String,
    val storeName: String,
    val logoUrl: String? = null,
    val headerLines: List<TicketLine> = emptyList(),
    val bodyLines: List<TicketLine> = emptyList(),
    val totalLines: List<TicketLine> = emptyList(),
    val footer: String? = null,
    val generatedAt: String
)
