package cl.friendlypos.mypos.tickets

import cl.friendlypos.mypos.api.dto.CashboxSessionItemDto
import cl.friendlypos.mypos.api.dto.SaleDto
import cl.friendlypos.mypos.api.dto.TicketDataResponseDto
import cl.friendlypos.mypos.checkout.CheckoutHolder
import cl.friendlypos.mypos.model.Ticket
import cl.friendlypos.mypos.model.TicketLine
import cl.friendlypos.mypos.model.TicketType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Construye objetos [Ticket] a partir de los datos de dominio (sesiones de caja, ventas).
 * Centraliza el formato para que tanto la previsualización (etapa 1) como la impresión
 * (etapa 2) usen exactamente el mismo contenido.
 */
object TicketBuilders {

    private val clpLocale = Locale("es", "CL")

    fun money(amount: Double?): String =
        "$ " + String.format(clpLocale, "%,.0f", amount ?: 0.0)

    private fun now(): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", clpLocale).format(Date())

    /** Normaliza timestamps ISO ("2026-06-20T21:34:00...") a "2026-06-20 21:34". */
    private fun isoToReadable(iso: String?): String? =
        iso?.takeIf { it.isNotBlank() }?.take(16)?.replace("T", " ")

    fun cashboxOpen(session: CashboxSessionItemDto, storeName: String): Ticket {
        val header = buildList {
            session.cashboxLabel?.let { add(TicketLine("Caja", it)) }
            session.cashierName?.let { add(TicketLine("Cajero", it)) }
            isoToReadable(session.openedAt)?.let { add(TicketLine("Apertura", it)) }
        }
        return Ticket(
            type = TicketType.CASHBOX_OPEN,
            title = "APERTURA DE CAJA",
            storeName = storeName,
            headerLines = header,
            totalLines = listOf(
                TicketLine("Monto inicial", money(session.initialAmount), emphasize = true)
            ),
            footer = "Comprobante de apertura de caja",
            generatedAt = now()
        )
    }

    fun cashboxClose(
        session: CashboxSessionItemDto,
        finalAmount: Double,
        notes: String?,
        storeName: String
    ): Ticket {
        val expected = session.expectedAmount ?: session.totalCash ?: session.initialAmount
        val diff = finalAmount - expected

        val header = buildList {
            session.cashboxLabel?.let { add(TicketLine("Caja", it)) }
            session.cashierName?.let { add(TicketLine("Cajero", it)) }
            isoToReadable(session.openedAt)?.let { add(TicketLine("Apertura", it)) }
            isoToReadable(session.closedAt)?.let { add(TicketLine("Cierre", it)) }
        }

        val body = buildList {
            add(TicketLine("Monto inicial", money(session.initialAmount)))
            add(TicketLine("Monto esperado", money(expected)))
            if (!notes.isNullOrBlank()) add(TicketLine("Notas", notes))
        }

        val diffLabel = if (diff >= 0) "Sobrante" else "Faltante"
        return Ticket(
            type = TicketType.CASHBOX_CLOSE,
            title = "CIERRE DE CAJA",
            storeName = storeName,
            headerLines = header,
            bodyLines = body,
            totalLines = listOf(
                TicketLine("Monto final", money(finalAmount), emphasize = true),
                TicketLine(diffLabel, money(kotlin.math.abs(diff)), emphasize = true)
            ),
            footer = "Comprobante de cierre de caja",
            generatedAt = now()
        )
    }

    fun sale(
        lines: List<CheckoutHolder.Line>,
        ticketNumber: String,
        total: Double,
        amountPaid: Double,
        change: Double,
        paymentMethod: String,
        documentType: String?,
        storeName: String
    ): Ticket {
        val header = buildList {
            add(TicketLine("Ticket", ticketNumber))
            documentType?.takeIf { it.isNotBlank() }?.let { add(TicketLine("Documento", it)) }
        }

        val body = lines.map { line ->
            val qtyPrefix = if (line.quantity > 1) "${line.quantity} x ${money(line.unitPrice.toDouble())}  " else ""
            TicketLine("$qtyPrefix${line.name}", money(line.lineTotal.toDouble()))
        }

        val totals = buildList {
            add(TicketLine("TOTAL", money(total), emphasize = true))
            add(TicketLine("Pago (${paymentMethodLabel(paymentMethod)})", money(amountPaid)))
            if (change > 0) add(TicketLine("Vuelto", money(change)))
        }

        return Ticket(
            type = TicketType.SALE,
            title = "COMPROBANTE DE VENTA",
            storeName = storeName,
            headerLines = header,
            bodyLines = body,
            totalLines = totals,
            footer = "¡Gracias por su compra!",
            generatedAt = now()
        )
    }

    private fun paymentMethodLabel(method: String): String = when (method.lowercase()) {
        "cash" -> "Efectivo"
        "transfer" -> "Transferencia"
        "card" -> "Tarjeta"
        "mixed" -> "Mixto"
        else -> method
    }

    fun fromOpeningTicketData(response: TicketDataResponseDto): Ticket {
        val data = response.data!!
        val storeName = data.store?.name ?: "FriendlyPOS"
        val session = data.session
        val header = buildList {
            session?.cashboxNumber?.let { add(TicketLine("Caja", it.toString())) }
            session?.cashierName?.let { add(TicketLine("Cajero", it)) }
            session?.openedAtFormatted?.let { add(TicketLine("Apertura", it)) }
        }
        return Ticket(
            type = TicketType.CASHBOX_OPEN,
            title = data.docTypeLabel ?: "APERTURA DE CAJA",
            storeName = storeName,
            logoUrl = response.logoAbsoluteUrl ?: response.logoUrl,
            headerLines = header,
            totalLines = listOf(
                TicketLine(
                    "Monto inicial",
                    session?.initialAmountFormatted ?: money(session?.initialAmount),
                    emphasize = true
                )
            ),
            footer = "Comprobante de apertura de caja",
            generatedAt = now()
        )
    }

    fun fromCloseTicketData(response: TicketDataResponseDto): Ticket {
        val data = response.data!!
        val storeName = data.store?.name ?: "FriendlyPOS"
        val session = data.session
        val summary = data.summary
        val header = buildList {
            session?.cashboxNumber?.let { add(TicketLine("Caja", it.toString())) }
            session?.cashierName?.let { add(TicketLine("Cajero", it)) }
            session?.openedAtFormatted?.let { add(TicketLine("Apertura", it)) }
            session?.closedAtFormatted?.let { add(TicketLine("Cierre", it)) }
        }
        val body = buildList {
            add(TicketLine("Monto inicial", summary?.initialAmountFormatted ?: money(summary?.initialAmount)))
            add(TicketLine("Monto esperado", summary?.expectedAmountFormatted ?: money(summary?.expectedAmount)))
            if (!session?.notes.isNullOrBlank()) add(TicketLine("Notas", session?.notes ?: ""))
            data.movements?.forEach { mov ->
                val sign = if (mov.code in listOf("income", "adjustment_plus")) "+" else "-"
                add(TicketLine("  ${mov.description ?: mov.code ?: ""}", "$sign ${money(mov.amount)}"))
            }
        }
        val diffLabel = when (summary?.status?.uppercase()) {
            "SOBRANTE" -> "Sobrante"
            else -> "Faltante"
        }
        val diff = kotlin.math.abs(summary?.difference ?: 0.0)
        return Ticket(
            type = TicketType.CASHBOX_CLOSE,
            title = data.docTypeLabel ?: "CIERRE DE CAJA",
            storeName = storeName,
            logoUrl = response.logoAbsoluteUrl ?: response.logoUrl,
            headerLines = header,
            bodyLines = body,
            totalLines = listOf(
                TicketLine("Monto final", summary?.finalAmountFormatted ?: money(summary?.finalAmount), emphasize = true),
                TicketLine(diffLabel, money(diff), emphasize = true)
            ),
            footer = "Comprobante de cierre de caja",
            generatedAt = now()
        )
    }

    fun fromSaleDto(sale: SaleDto, storeName: String): Ticket {
        val header = buildList {
            add(TicketLine("Ticket", sale.ticketNumber ?: "#${sale.id.takeLast(6)}"))
            sale.tipoDocumento?.takeIf { it.isNotBlank() }?.let { add(TicketLine("Documento", it)) }
            sale.cashierName?.takeIf { it.isNotBlank() }?.let { add(TicketLine("Cajero", it)) }
            sale.customer?.name?.takeIf { it.isNotBlank() }?.let { add(TicketLine("Cliente", it)) }
        }
        val body = sale.items?.map { item ->
            val qty = item.quantity ?: 1
            val unitPrice = item.unitPrice?.toDoubleOrNull() ?: 0.0
            val lineTotal = item.total?.toDoubleOrNull() ?: (unitPrice * qty)
            val qtyPrefix = if (qty > 1) "$qty x ${money(unitPrice)}  " else ""
            TicketLine("$qtyPrefix${item.productName ?: ""}", money(lineTotal))
        } ?: emptyList()
        val total = sale.total?.toDoubleOrNull() ?: 0.0
        val amountPaid = sale.amountPaid?.toDoubleOrNull() ?: total
        val totals = buildList {
            add(TicketLine("TOTAL", money(total), emphasize = true))
            add(TicketLine("Pago (${paymentMethodLabel(sale.paymentMethod ?: "")})", money(amountPaid)))
        }
        return Ticket(
            type = TicketType.SALE,
            title = "COMPROBANTE DE VENTA",
            storeName = storeName,
            headerLines = header,
            bodyLines = body,
            totalLines = totals,
            footer = "¡Gracias por su compra!",
            generatedAt = isoToReadable(sale.createdAt) ?: now()
        )
    }

    fun fromSaleTicketData(response: TicketDataResponseDto): Ticket {
        val data = response.data!!
        val storeName = data.store?.name ?: "FriendlyPOS"
        val ticketHeader = data.ticket
        val summary = data.summary
        val payment = data.payment
        val header = buildList {
            ticketHeader?.number?.let { add(TicketLine("Ticket", it)) }
            ticketHeader?.cashier?.let { add(TicketLine("Cajero", it)) }
            ticketHeader?.dateFormatted?.let { add(TicketLine("Fecha", it)) }
            data.customer?.name?.takeIf { it.isNotBlank() }?.let { add(TicketLine("Cliente", it)) }
        }
        val body = data.items?.map { item ->
            val qty = item.quantity ?: 1
            val qtyPrefix = if (qty > 1) "$qty x ${item.priceFormatted ?: money(item.price)}  " else ""
            TicketLine("$qtyPrefix${item.name ?: ""}", item.subtotalFormatted ?: money(item.subtotal))
        } ?: emptyList()
        val totals = buildList {
            add(TicketLine("TOTAL", summary?.totalFormatted ?: money(summary?.total), emphasize = true))
            val payLabel = payment?.methodLabel ?: paymentMethodLabel(payment?.method ?: "")
            add(TicketLine("Pago ($payLabel)", payment?.amountPaidFormatted ?: money(payment?.amountPaid)))
            val change = payment?.change ?: 0.0
            if (change > 0) add(TicketLine("Vuelto", payment?.changeFormatted ?: money(change)))
        }
        return Ticket(
            type = TicketType.SALE,
            title = data.docTypeLabel ?: "COMPROBANTE DE VENTA",
            storeName = storeName,
            logoUrl = response.logoAbsoluteUrl ?: response.logoUrl,
            headerLines = header,
            bodyLines = body,
            totalLines = totals,
            footer = data.footerText ?: "¡Gracias por su compra!",
            generatedAt = ticketHeader?.dateFormatted ?: now()
        )
    }
}
