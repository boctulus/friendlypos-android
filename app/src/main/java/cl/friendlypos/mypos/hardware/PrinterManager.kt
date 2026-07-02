package cl.friendlypos.mypos.hardware

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.Log
import cl.friendlypos.mypos.model.Ticket
import cl.friendlypos.mypos.model.TicketLine
import com.pos.sdk.PosConstants
import com.pos.sdk.printer.POIPrinterManager
import com.pos.sdk.printer.models.PrintLine
import com.pos.sdk.printer.models.TextPrintLine
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class PrinterManager(private val context: Context) : IPrinter {

    private val tag = "PrinterManager"

    override fun initialize(): Boolean =
        runCatching {
            POIPrinterManager(context.applicationContext).apply {
                open()
                close()
            }
        }.isSuccess

    override fun printTicket(ticket: Ticket): Result<Unit> =
        printLines(ticket.toPrintLines())

    override fun printHtml(html: String): Result<Unit> =
        printLines(html.toPrintLines())

    private fun printLines(lines: List<PosPrintLine>): Result<Unit> {
        val printer = POIPrinterManager(context.applicationContext)
        val printError = AtomicReference<Throwable?>()
        val done = CountDownLatch(1)

        return runCatching {
            printer.open()
            validatePrinterState(printer.getPrinterState())
            printer.cleanCache()
            printer.setPrintType(PosConstants.PRINT_TYPE_NORMAL)
            printer.setPrintGray(DEFAULT_PRINT_GRAY)
            printer.setLineSpace(DEFAULT_LINE_SPACE)

            lines.forEach { line ->
                if (line.text.isBlank()) {
                    printer.addBlankView(BLANK_LINE_HEIGHT)
                } else {
                    printer.addPrintLine(
                        TextPrintLine(
                            line.text,
                            line.align,
                            line.size,
                            line.bold,
                            line.italic
                        )
                    )
                }
            }

            printer.addBlankView(END_BLANK_LINES)
            printer.beginPrint(object : POIPrinterManager.IPrinterListener {
                override fun onStart() {
                    Log.i(tag, "Inicio de impresion POS PRO2")
                }

                override fun onFinish() {
                    Log.i(tag, "Ticket impreso correctamente")
                    done.countDown()
                }

                override fun onError(errorCode: Int, msg: String?) {
                    val message = printerErrorMessage(errorCode, msg)
                    Log.e(tag, message)
                    printError.set(IllegalStateException(message))
                    done.countDown()
                }
            })
            printer.cleanCache()

            if (!done.await(PRINT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw IllegalStateException("No se pudo imprimir: timeout esperando respuesta de impresora")
            }
            printError.get()?.let { throw it }
            Unit
        }.onFailure { error ->
            Log.e(tag, "No se pudo imprimir ticket en impresora POS PRO2", error)
        }.also {
            printer.close()
        }
    }

    private fun validatePrinterState(state: Int) {
        when (state) {
            POIPrinterManager.STATUS_IDLE -> return
            POIPrinterManager.STATUS_PRINTING -> throw IllegalStateException("No se pudo imprimir: impresora ocupada")
            POIPrinterManager.STATUS_OVERHEAT -> throw IllegalStateException("No se pudo imprimir: impresora sobrecalentada")
            POIPrinterManager.STATUS_NO_PAPER -> throw IllegalStateException("No se pudo imprimir: la impresora no tiene papel")
            POIPrinterManager.STATUS_NO_PRINTER -> throw IllegalStateException("No se pudo imprimir: impresora no disponible")
            else -> throw IllegalStateException("No se pudo imprimir: estado de impresora desconocido ($state)")
        }
    }

    private fun printerErrorMessage(errorCode: Int, msg: String?): String =
        when (errorCode) {
            POIPrinterManager.ERROR_INIT -> "No se pudo imprimir: error inicializando impresora"
            POIPrinterManager.ERROR_PRINT -> "No se pudo imprimir: error durante la impresion ${msg.orEmpty()}".trim()
            POIPrinterManager.ERROR_OVERHEAT -> "No se pudo imprimir: impresora sobrecalentada"
            POIPrinterManager.ERROR_NO_PAPER -> "No se pudo imprimir: la impresora no tiene papel"
            POIPrinterManager.ERROR_OTHER -> "No se pudo imprimir: error de impresora ${msg.orEmpty()}".trim()
            else -> "No se pudo imprimir: error de impresora $errorCode ${msg.orEmpty()}".trim()
        }

    private fun Ticket.toPrintLines(): List<PosPrintLine> = buildList {
        add(PosPrintLine(storeName, PrintLine.CENTER, TITLE_SIZE, bold = true))
        addBlankLine()
        add(PosPrintLine(title, PrintLine.CENTER, TEXT_SIZE, bold = true))
        addBlankLine()
        add(PosPrintLine(generatedAt, PrintLine.CENTER, SMALL_TEXT_SIZE))
        addBlankLine()
        addDivider()
        headerLines.addTicketLinesTo(this)
        if (bodyLines.isNotEmpty()) {
            addDivider()
            bodyLines.addTicketLinesTo(this)
        }
        if (totalLines.isNotEmpty()) {
            addDivider()
            totalLines.addTicketLinesTo(this)
        }
        footer?.takeIf { it.isNotBlank() }?.let {
            addDivider()
            add(PosPrintLine(it, PrintLine.CENTER, SMALL_TEXT_SIZE))
        }
    }

    private fun String.toPrintLines(): List<PosPrintLine> {
        val body = substringAfter("<body", this)
            .substringAfter(">", this)
            .substringBeforeLast("</body>", this)
            .replace(Regex("(?is)<script\\b[^>]*>.*?</script>"), "")
            .replace(Regex("(?is)<style\\b[^>]*>.*?</style>"), "")
            .replace(Regex("(?is)<head\\b[^>]*>.*?</head>"), "")
            .replace(Regex("(?is)<!--.*?-->"), "")

        return buildList {
            parseHtmlBlocks(body).forEach { block ->
                when {
                    block.isDivider -> addDivider()
                    block.isBlank -> add(PosPrintLine(""))
                    block.value != null -> add(
                        PosPrintLine(
                            formatColumns(block.label, block.value),
                            PrintLine.LEFT,
                            if (block.bold) TEXT_SIZE else SMALL_TEXT_SIZE,
                            bold = block.bold,
                            italic = block.italic
                        )
                    )
                    else -> add(
                        PosPrintLine(
                            block.label,
                            block.align,
                            if (block.bold) TEXT_SIZE else SMALL_TEXT_SIZE,
                            bold = block.bold,
                            italic = block.italic
                        )
                    )
                }
            }
        }.ifEmpty {
            listOf(PosPrintLine(htmlToPlainText(body), PrintLine.LEFT, SMALL_TEXT_SIZE))
        }
    }

    private fun parseHtmlBlocks(html: String): List<HtmlBlock> {
        var working = html
        val blocks = mutableListOf<HtmlBlock>()
        val blockPattern = Regex("(?is)<(div|tr|p|h1|h2|h3)[^>]*>(.*?)</\\1>")
        while (true) {
            val match = blockPattern.find(working) ?: break
            val openTag = match.value.substringBefore(">")
            val inner = match.groupValues[2]
            val nestedBlocks = htmlBlocksFrom(openTag, inner)
            blocks.addAll(nestedBlocks)
            working = working.substring(match.range.last + 1)
        }
        return blocks
    }

    private fun htmlBlocksFrom(openTag: String, innerHtml: String): List<HtmlBlock> {
        val tagName = Regex("(?is)<\\s*([a-z0-9]+)").find(openTag)?.groupValues?.get(1).orEmpty()
        val className = Regex("(?is)class\\s*=\\s*\"([^\"]*)\"").find(openTag)?.groupValues?.get(1).orEmpty()
        if (className.contains("divider")) return listOf(HtmlBlock("", isDivider = true))

        val hasNestedBlocks = innerHtml.contains(Regex("(?is)<(div|tr|p|h1|h2|h3)\\b"))
        val shouldParseChildren = hasNestedBlocks && !className.containsAny(
            "row",
            "ticket-row",
            "total-row",
            "receptor-row",
            "calc-total",
            "amount-box",
            "result-box",
            "doc-type-block"
        )
        if (shouldParseChildren) {
            val childBlocks = parseHtmlBlocks(innerHtml)
            return if (className.containsAny("store-header", "ticket-info")) {
                childBlocks.withTrailingBlank()
            } else {
                childBlocks
            }
        }

        val cells = Regex("(?is)<(?:span|td|th)[^>]*>(.*?)</(?:span|td|th)>")
            .findAll(innerHtml)
            .map { htmlToPlainText(it.groupValues[1]) }
            .filter { it.isNotBlank() }
            .toList()
        val plain = htmlToPlainText(innerHtml)
        if (plain.isBlank() && cells.isEmpty()) return emptyList()

        if (tagName.equals("tr", ignoreCase = true) && cells.size >= 4) {
            return tableRowBlocks(cells, innerHtml)
        }

        val bold = className.containsAny("bold", "grand-total", "store-name", "doc-type", "amount-value", "result-value")
        val italic = className.contains("header-text") || className.contains("sii-legend")
        val align = when {
            className.contains("center") ||
                className.contains("store-name") ||
                className.contains("store-header") ||
                className.contains("doc-type") ||
                className.contains("footer") ||
                className.contains("amount-box") ||
                className.contains("result-box") -> PrintLine.CENTER
            className.contains("right") -> PrintLine.RIGHT
            else -> PrintLine.LEFT
        }

        if (className.contains("doc-type-block")) {
            return listOf(
                HtmlBlock(
                    label = plain,
                    align = PrintLine.CENTER,
                    bold = true
                ),
                HtmlBlock("")
            )
        }

        return if (cells.size >= 2 && className.containsAny("row", "ticket-row", "total-row", "receptor-row", "calc-total")) {
            listOf(HtmlBlock(
                label = cells.first(),
                value = cells.drop(1).joinToString(" "),
                bold = bold || className.contains("value"),
                italic = italic
            ))
        } else {
            listOf(HtmlBlock(
                label = plain,
                align = align,
                bold = bold,
                italic = italic
            ))
        }
    }

    private fun tableRowBlocks(cells: List<String>, rowHtml: String): List<HtmlBlock> {
        val first = cells[0]
        val second = cells[1]
        val third = cells[2]
        val fourth = cells[3]
        val isHeader = rowHtml.contains("<th", ignoreCase = true)
        val isDiscount = first.contains("Desc", ignoreCase = true)

        return when {
            isHeader -> listOf(
                HtmlBlock(formatItemHeader(first, second, third, fourth), bold = true)
            )
            isDiscount -> listOf(
                HtmlBlock(formatColumns(first, fourth), bold = false)
            )
            else -> listOf(
                HtmlBlock(first, bold = false),
                HtmlBlock(formatItemAmounts(second, third, fourth), bold = false)
            )
        }
    }

    private fun List<TicketLine>.addTicketLinesTo(target: MutableList<PosPrintLine>) {
        forEach { line ->
            val value = line.value
            if (value == null) {
                target.add(PosPrintLine(line.label, PrintLine.LEFT, TEXT_SIZE, line.emphasize))
            } else {
                target.add(PosPrintLine(formatColumns(line.label, value), PrintLine.LEFT, TEXT_SIZE, line.emphasize))
            }
        }
    }

    private fun MutableList<PosPrintLine>.addBlankLine() {
        add(PosPrintLine(""))
    }

    private fun MutableList<PosPrintLine>.addDivider() {
        add(PosPrintLine("-".repeat(TICKET_WIDTH), PrintLine.CENTER, SMALL_TEXT_SIZE))
    }

    private fun formatColumns(label: String, value: String): String {
        val cleanLabel = label.trim()
        val cleanValue = value.trim()
        val availableLabel = TICKET_WIDTH - cleanValue.length - 1
        if (availableLabel <= 0) return cleanValue.takeLast(TICKET_WIDTH)
        val trimmedLabel = cleanLabel.take(availableLabel)
        val spaces = TICKET_WIDTH - trimmedLabel.length - cleanValue.length
        return trimmedLabel + " ".repeat(spaces.coerceAtLeast(1)) + cleanValue
    }

    private fun formatItemHeader(product: String, quantity: String, unitPrice: String, total: String): String =
        product.take(PRODUCT_COLUMN_WIDTH).padEnd(PRODUCT_COLUMN_WIDTH) +
            " " +
            quantity.take(QUANTITY_COLUMN_WIDTH).padStart(QUANTITY_COLUMN_WIDTH) +
            " " +
            unitPrice.take(PRICE_COLUMN_WIDTH).padStart(PRICE_COLUMN_WIDTH) +
            " " +
            total.take(TOTAL_COLUMN_WIDTH).padStart(TOTAL_COLUMN_WIDTH)

    private fun formatItemAmounts(quantity: String, unitPrice: String, total: String): String =
        "".padEnd(PRODUCT_COLUMN_WIDTH) +
            " " +
            quantity.takeLast(QUANTITY_COLUMN_WIDTH).padStart(QUANTITY_COLUMN_WIDTH) +
            " " +
            unitPrice.takeLast(PRICE_COLUMN_WIDTH).padStart(PRICE_COLUMN_WIDTH) +
            " " +
            total.takeLast(TOTAL_COLUMN_WIDTH).padStart(TOTAL_COLUMN_WIDTH)

    private fun htmlToPlainText(html: String): String {
        val normalized = html
            .replace(Regex("(?is)<br\\s*/?>"), "\n")
            .replace(Regex("(?is)</(div|p|tr|h1|h2|h3|li)>"), "\n")
            .replace(Regex("(?is)</(td|th|span)>"), " ")
        val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(normalized, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(normalized)
        }
        return spanned.toString()
            .replace('\u00A0', ' ')
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()
    }

    private fun String.containsAny(vararg needles: String): Boolean =
        needles.any { contains(it, ignoreCase = true) }

    private fun List<HtmlBlock>.withTrailingBlank(): List<HtmlBlock> =
        if (isEmpty() || last().isBlank) this else this + HtmlBlock("")

    private data class PosPrintLine(
        val text: String,
        val align: Int = PrintLine.LEFT,
        val size: Int = TEXT_SIZE,
        val bold: Boolean = false,
        val italic: Boolean = false
    )

    private data class HtmlBlock(
        val label: String,
        val value: String? = null,
        val align: Int = PrintLine.LEFT,
        val bold: Boolean = false,
        val italic: Boolean = false,
        val isDivider: Boolean = false
    ) {
        val isBlank: Boolean = label.isBlank() && value == null && !isDivider
    }

    private companion object {
        private const val TICKET_WIDTH = 32
        private const val PRODUCT_COLUMN_WIDTH = 11
        private const val QUANTITY_COLUMN_WIDTH = 3
        private const val PRICE_COLUMN_WIDTH = 7
        private const val TOTAL_COLUMN_WIDTH = 8
        private const val TITLE_SIZE = 22
        private const val TEXT_SIZE = 20
        private const val SMALL_TEXT_SIZE = 18
        private const val DEFAULT_PRINT_GRAY = 1200
        private const val DEFAULT_LINE_SPACE = 4
        private const val BLANK_LINE_HEIGHT = 24
        private const val END_BLANK_LINES = 5
        private const val PRINT_TIMEOUT_SECONDS = 30L
    }
}
