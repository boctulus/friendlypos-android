package cl.friendlypos.mypos.tickets

import android.os.Build
import android.text.Html
import cl.friendlypos.mypos.model.Ticket
import cl.friendlypos.mypos.model.TicketLine

object TicketTextRenderer {
    private const val TICKET_WIDTH = 32
    private val divider = "-".repeat(TICKET_WIDTH)

    fun render(ticket: Ticket): String = buildString {
        appendCentered(ticket.storeName)
        appendCentered(ticket.title)
        appendCentered(ticket.generatedAt)
        appendLine(divider)
        ticket.headerLines.appendLinesTo(this)
        if (ticket.bodyLines.isNotEmpty()) {
            appendLine(divider)
            ticket.bodyLines.appendLinesTo(this)
        }
        if (ticket.totalLines.isNotEmpty()) {
            appendLine(divider)
            ticket.totalLines.appendLinesTo(this)
        }
        ticket.footer?.takeIf { it.isNotBlank() }?.let {
            appendLine(divider)
            appendCentered(it)
        }
        appendLine()
        appendLine()
    }

    fun renderHtml(html: String): String {
        val compactHtml = html
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</(div|p|tr|h1|h2|h3|li)>"), "\n")
            .replace(Regex("(?i)</(td|th)>"), " ")
        val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(compactHtml, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(compactHtml)
        }
        return spanned.toString()
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n", postfix = "\n\n")
    }

    private fun List<TicketLine>.appendLinesTo(builder: StringBuilder) {
        forEach { line ->
            builder.appendLine(formatLine(line))
        }
    }

    private fun formatLine(line: TicketLine): String {
        val value = line.value
        if (value == null) return line.label.take(TICKET_WIDTH)

        val cleanLabel = line.label.trim()
        val cleanValue = value.trim()
        val minSpace = 1
        val availableLabel = TICKET_WIDTH - cleanValue.length - minSpace
        if (availableLabel <= 0) return cleanValue.takeLast(TICKET_WIDTH)

        val label = cleanLabel.take(availableLabel)
        val spaces = TICKET_WIDTH - label.length - cleanValue.length
        return label + " ".repeat(spaces.coerceAtLeast(minSpace)) + cleanValue
    }

    private fun StringBuilder.appendCentered(text: String) {
        val clean = text.trim()
        if (clean.length >= TICKET_WIDTH) {
            appendLine(clean.take(TICKET_WIDTH))
            return
        }
        val leftPadding = (TICKET_WIDTH - clean.length) / 2
        appendLine(" ".repeat(leftPadding) + clean)
    }
}
