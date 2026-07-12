package cl.friendlypos.mypos.hardware

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import cl.friendlypos.mypos.model.Ticket
import cl.friendlypos.mypos.model.TicketLine
import com.pos.sdk.PosConstants
import com.pos.sdk.printer.POIPrinterManager
import com.pos.sdk.printer.models.BitmapPrintLine
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
        executePrint("texto") { printer ->
            ticket.toPrintLines().forEach { line ->
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
        }

    override fun printBitmap(bitmap: Bitmap): Result<Unit> =
        executePrint("bitmap=${bitmap.width}x${bitmap.height}") { printer ->
            require(!bitmap.isRecycled) { "No se puede imprimir un bitmap reciclado" }
            require(bitmap.width == PRINT_WIDTH_PX) {
                "El bitmap debe tener $PRINT_WIDTH_PX px de ancho y tiene ${bitmap.width} px"
            }
            Log.i(tag, "Preparando bitmap termico ${bitmap.width}x${bitmap.height}")
            printer.addPrintLine(BitmapPrintLine(bitmap, PrintLine.CENTER))
        }

    private fun executePrint(
        jobType: String,
        populate: (POIPrinterManager) -> Unit
    ): Result<Unit> {
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

            populate(printer)
            printer.addBlankView(END_FEED_LINES)
            val jobLength =
                "tipo=$jobType, largo previo=${printer.getBeforePrinterLength()}, " +
                    "largo total=${printer.getPrinterLength()}"
            Log.i(
                tag,
                "Enviando trabajo: $jobLength"
            )
            printer.beginPrint(object : POIPrinterManager.IPrinterListener {
                override fun onStart() {
                    Log.i(tag, "Inicio de impresion POS PRO2; estado=${printer.getPrinterState()}")
                }

                override fun onFinish() {
                    Log.i(tag, "Ticket impreso correctamente; estado=${printer.getPrinterState()}")
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
                val timeoutDetails =
                    "$jobLength, estado=${printer.getPrinterState()}"
                Log.e(tag, "Timeout de impresion POS PRO2; $timeoutDetails")
                throw IllegalStateException(
                    "No se pudo imprimir: timeout esperando respuesta de impresora ($timeoutDetails)"
                )
            }
            printError.get()?.let { throw it }
            Unit
        }.onFailure { error ->
            Log.e(tag, "No se pudo imprimir ticket en impresora POS PRO2", error)
        }.also {
            runCatching { printer.close() }
        }
    }

    private fun validatePrinterState(state: Int) {
        when (state) {
            POIPrinterManager.STATUS_IDLE -> return
            POIPrinterManager.STATUS_PRINTING -> throw IllegalStateException(
                "No se pudo imprimir: impresora ocupada"
            )
            POIPrinterManager.STATUS_OVERHEAT -> throw IllegalStateException(
                "No se pudo imprimir: impresora sobrecalentada"
            )
            POIPrinterManager.STATUS_NO_PAPER -> throw IllegalStateException(
                "No se pudo imprimir: la impresora no tiene papel"
            )
            POIPrinterManager.STATUS_NO_PRINTER -> throw IllegalStateException(
                "No se pudo imprimir: impresora no disponible"
            )
            else -> throw IllegalStateException(
                "No se pudo imprimir: estado de impresora desconocido ($state)"
            )
        }
    }

    private fun printerErrorMessage(errorCode: Int, msg: String?): String =
        when (errorCode) {
            POIPrinterManager.ERROR_INIT -> "No se pudo imprimir: error inicializando impresora"
            POIPrinterManager.ERROR_PRINT ->
                "No se pudo imprimir: error durante la impresion ${msg.orEmpty()}".trim()
            POIPrinterManager.ERROR_OVERHEAT ->
                "No se pudo imprimir: impresora sobrecalentada"
            POIPrinterManager.ERROR_NO_PAPER ->
                "No se pudo imprimir: la impresora no tiene papel"
            POIPrinterManager.ERROR_OTHER ->
                "No se pudo imprimir: error de impresora ${msg.orEmpty()}".trim()
            else ->
                "No se pudo imprimir: error de impresora $errorCode ${msg.orEmpty()}".trim()
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

    private fun List<TicketLine>.addTicketLinesTo(target: MutableList<PosPrintLine>) {
        forEach { line ->
            val value = line.value
            if (value == null) {
                target.add(
                    PosPrintLine(line.label, PrintLine.LEFT, TEXT_SIZE, line.emphasize)
                )
            } else {
                target.add(
                    PosPrintLine(
                        formatColumns(line.label, value),
                        PrintLine.LEFT,
                        TEXT_SIZE,
                        line.emphasize
                    )
                )
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

    private data class PosPrintLine(
        val text: String,
        val align: Int = PrintLine.LEFT,
        val size: Int = TEXT_SIZE,
        val bold: Boolean = false,
        val italic: Boolean = false
    )

    private companion object {
        private const val PRINT_WIDTH_PX = 384
        private const val TICKET_WIDTH = 32
        private const val TITLE_SIZE = 22
        private const val TEXT_SIZE = 20
        private const val SMALL_TEXT_SIZE = 18
        private const val DEFAULT_PRINT_GRAY = 1800
        private const val DEFAULT_LINE_SPACE = 4
        private const val BLANK_LINE_HEIGHT = 24
        private const val END_FEED_LINES = 5
        private const val PRINT_TIMEOUT_SECONDS = 30L
    }
}
