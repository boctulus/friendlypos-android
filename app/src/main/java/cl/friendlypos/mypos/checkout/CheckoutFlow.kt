package cl.friendlypos.mypos.checkout

import android.content.Context
import cl.friendlypos.mypos.model.Ticket
import cl.friendlypos.mypos.repository.SaleRepository
import cl.friendlypos.mypos.tickets.TicketBuilders
import cl.friendlypos.mypos.tickets.TicketHtmlRenderer
import kotlinx.coroutines.delay

/**
 * Orquesta el cierre de la venta tras registrarla: emite el DTE cuando corresponde y resuelve
 * el ticket a mostrar. Centraliza la lógica compartida por `CashPaymentActivity` y
 * `MockPaymentActivity` (DRY).
 */
object CheckoutFlow {

    /** Espera de propagación de la escritura en Firestore antes de leer/emitir. */
    private const val FIRESTORE_PROPAGATION_MS = 1500L

    sealed class Outcome {
        data class ShowHtml(val html: String) : Outcome()
        data class ShowTicket(val ticket: Ticket) : Outcome()
        /** La emisión del DTE falló; la venta YA está registrada. Permite reintentar. */
        data class DteError(val saleId: String, val message: String) : Outcome()
    }

    /**
     * Flujo principal post-venta: espera propagación, emite DTE si [documentType] lo requiere
     * y devuelve el ticket a renderizar. Un fallo de emisión produce [Outcome.DteError].
     */
    suspend fun completeSale(
        context: Context,
        repo: SaleRepository,
        sale: SaleRepository.SaleResult,
        documentType: DocumentType,
        receptor: CheckoutHolder.ReceptorData?,
        storeName: String
    ): Outcome {
        delay(FIRESTORE_PROPAGATION_MS)

        val saleId = sale.id
        if (!documentType.emitsDte || saleId == null) {
            return renderSaleTicket(context, repo, sale, documentType, storeName)
        }
        return emitAndRender(context, repo, sale, documentType, receptor, storeName)
    }

    /** Reintenta la emisión del DTE para una venta ya registrada. */
    suspend fun retryDte(
        context: Context,
        repo: SaleRepository,
        sale: SaleRepository.SaleResult,
        documentType: DocumentType,
        receptor: CheckoutHolder.ReceptorData?,
        storeName: String
    ): Outcome = emitAndRender(context, repo, sale, documentType, receptor, storeName)

    /** Cierra sin documento tributario: renderiza el ticket no fiscal. */
    suspend fun continueWithoutDocument(
        context: Context,
        repo: SaleRepository,
        sale: SaleRepository.SaleResult,
        storeName: String
    ): Outcome = renderSaleTicket(context, repo, sale, DocumentType.SIN_DOCUMENTO, storeName)

    /** Emite el DTE y, según el resultado, renderiza el ticket DTE o devuelve el error. */
    private suspend fun emitAndRender(
        context: Context,
        repo: SaleRepository,
        sale: SaleRepository.SaleResult,
        documentType: DocumentType,
        receptor: CheckoutHolder.ReceptorData?,
        storeName: String
    ): Outcome {
        val saleId = sale.id ?: return Outcome.DteError("", "Venta sin identificador")
        return when (val result = repo.emitDte(saleId, documentType.emitTipo ?: "boleta", receptor)) {
            is SaleRepository.DteResult.Success ->
                renderDteTicket(context, repo, sale, documentType, receptor, storeName, result.folio, result.timbreBase64)
            is SaleRepository.DteResult.AlreadyEmitted ->
                renderDteTicket(context, repo, sale, documentType, receptor, storeName, result.folio, null)
            is SaleRepository.DteResult.Failure ->
                Outcome.DteError(saleId, result.message)
        }
    }

    /**
     * Renderiza el ticket DTE (`dte-58mm.html`) con el folio y el timbre (PNG/PDF417) reales.
     * Usa `ticket-data/sale` solo como fuente de datos de tienda/ítems; el carácter tributario,
     * folio y timbre provienen de la emisión.
     */
    private suspend fun renderDteTicket(
        context: Context,
        repo: SaleRepository,
        sale: SaleRepository.SaleResult,
        documentType: DocumentType,
        receptor: CheckoutHolder.ReceptorData?,
        storeName: String,
        folio: Long?,
        timbreBase64: String?
    ): Outcome {
        val docLabel = if (documentType == DocumentType.FACTURA_ELECTRONICA)
            "FACTURA ELECTRÓNICA" else "BOLETA ELECTRÓNICA"

        val ticketResponse = sale.id?.let { repo.getSaleTicketData(it).getOrNull() }
        if (ticketResponse != null) {
            return Outcome.ShowHtml(
                TicketHtmlRenderer.renderDte(
                    context = context,
                    response = ticketResponse,
                    folioOverride = folio?.toString(),
                    timbreBase64 = timbreBase64,
                    docTypeLabelOverride = docLabel,
                    receptorOverride = receptor
                )
            )
        }
        // Fallback sin datos del backend: ticket local con etiqueta del documento + folio.
        val titulo = folio?.let { "$docLabel  N° $it" } ?: docLabel
        return Outcome.ShowTicket(
            TicketBuilders.sale(
                lines = CheckoutHolder.lines,
                ticketNumber = sale.ticketNumber,
                total = sale.total,
                amountPaid = sale.amountPaid,
                change = sale.change,
                paymentMethod = sale.paymentMethod,
                documentType = titulo,
                storeName = storeName
            )
        )
    }

    /**
     * Lee los datos del ticket no fiscal del backend (`ticket-data/sale`) y renderiza el HTML.
     * Si el backend no responde, cae a un ticket construido localmente.
     */
    private suspend fun renderSaleTicket(
        context: Context,
        repo: SaleRepository,
        sale: SaleRepository.SaleResult,
        documentType: DocumentType,
        storeName: String
    ): Outcome {
        val ticketResponse = sale.id?.let { repo.getSaleTicketData(it).getOrNull() }
        return if (ticketResponse != null) {
            Outcome.ShowHtml(TicketHtmlRenderer.render(context, ticketResponse))
        } else {
            Outcome.ShowTicket(
                TicketBuilders.sale(
                    lines = CheckoutHolder.lines,
                    ticketNumber = sale.ticketNumber,
                    total = sale.total,
                    amountPaid = sale.amountPaid,
                    change = sale.change,
                    paymentMethod = sale.paymentMethod,
                    documentType = documentType.label,
                    storeName = storeName
                )
            )
        }
    }
}
