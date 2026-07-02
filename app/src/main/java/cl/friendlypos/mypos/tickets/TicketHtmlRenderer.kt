package cl.friendlypos.mypos.tickets

import android.content.Context
import cl.friendlypos.mypos.api.dto.TicketDataResponseDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TicketHtmlRenderer {

    private val clpLocale = Locale("es", "CL")

    private fun now(): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", clpLocale).format(Date())

    private fun String?.esc(): String =
        this?.replace("&", "&amp;")?.replace("<", "&lt;")?.replace(">", "&gt;") ?: ""

    private fun Double?.fmt(): String =
        if (this == null || this == 0.0) "$ 0"
        else "$ " + String.format(clpLocale, "%,.0f", this)

    private val screenCssOverride = """
<style>
/* @page override must be top-level — cannot nest inside @media.
   Declared after the template's @page rule so cascade gives it priority.
   This prevents Chromium WebView from using the 58mm print page size
   as the natural document width, which causes bitmap upscaling and blur. */
@page { size: auto; margin: 0; }
@media screen {
  body { width: 100% !important; max-width: 100% !important; box-sizing: border-box; margin: 0 !important; }
  .ticket { padding: 8px 16px !important; max-width: 100% !important; box-sizing: border-box !important; }
}
</style>
</head>""".trimIndent()

    private fun loadTemplate(context: Context, name: String): String {
        val raw = context.assets.open("tickets/$name").bufferedReader().use { it.readText() }
        return raw.replace("</head>", screenCssOverride)
    }

    // Contrato común a ambos backends (friendlypos y mypos): el DTE llega como "sale-dte".
    fun render(context: Context, response: TicketDataResponseDto): String = when (response.type) {
        "cashbox-opening"  -> renderCashboxOpening(context, response)
        "cashbox-close"    -> renderCashboxClose(context, response)
        "dte", "sale-dte"  -> renderDte(context, response)
        else               -> renderSaleTicket(context, response)
    }

    fun renderSaleTicket(context: Context, response: TicketDataResponseDto): String {
        val data = response.data ?: return ""
        val store = data.store
        val ticketHdr = data.ticket
        val customer = data.customer
        val summary = data.summary
        val payment = data.payment
        val logoUrl = response.logoAbsoluteUrl ?: response.logoUrl
        val isTributary = data.isTributary ?: false

        val logoSection = if (!logoUrl.isNullOrBlank())
            """<div class="logo-container"><img src="${logoUrl.esc()}" alt="Logo" /></div>"""
        else ""

        val storeAddress = if (!store?.address.isNullOrBlank())
            """<div class="store-info">${store?.address.esc()}</div>""" else ""
        val storePhone = if (!store?.phone.isNullOrBlank())
            """<div class="store-info">Tel: ${store?.phone.esc()}</div>""" else ""
        val storeTaxId = if (!store?.taxId.isNullOrBlank())
            """<div class="store-info">RUT: ${store?.taxId.esc()}</div>""" else ""

        val headerTextSection = if (!data.headerText.isNullOrBlank())
            """<div class="header-text">${data.headerText.esc()}</div>""" else ""

        val cashboxRow = if (!ticketHdr?.cashboxId.isNullOrBlank())
            """<div class="ticket-row"><span>Caja:</span><span>${ticketHdr?.cashboxId.esc()}</span></div>""" else ""
        val customerRow = if (!customer?.name.isNullOrBlank())
            """<div class="ticket-row"><span>Cliente:</span><span>${customer?.name.esc()}</span></div>""" else ""

        val itemsRows = buildString {
            data.items?.forEach { item ->
                val qty = item.quantity ?: 1
                val name = item.name.esc()
                val price = item.priceFormatted?.esc() ?: item.price.fmt()
                val subtotal = item.subtotalFormatted?.esc() ?: item.subtotal.fmt()
                append("""<tr>
                  <td>$name</td>
                  <td class="center">$qty</td>
                  <td class="right">$price</td>
                  <td class="right">$subtotal</td>
                </tr>""")
                val disc = item.discount ?: 0.0
                if (disc != 0.0) {
                    val discFmt = item.discountFormatted?.esc() ?: disc.fmt()
                    append("""<tr><td colspan="3" style="font-size:0.8em;color:#555;"> Desc:</td><td class="right" style="font-size:0.8em;color:#555;">-$discFmt</td></tr>""")
                }
            }
        }

        val subtotalRow = if ((summary?.subtotal ?: 0.0) != 0.0)
            """<div class="total-row"><span>Subtotal:</span><span>${summary?.subtotalFormatted?.esc() ?: summary?.subtotal.fmt()}</span></div>""" else ""

        val discountsRow = if ((summary?.discountsAmount ?: 0.0) != 0.0)
            """<div class="total-row"><span>Descuentos:</span><span>-${summary?.discountsFormatted?.esc() ?: summary?.discountsAmount.fmt()}</span></div>""" else ""

        val taxRows = buildString {
            val breakdown = summary?.taxBreakdown
            if (!breakdown.isNullOrEmpty()) {
                breakdown.forEach { tb ->
                    append("""<div class="total-row"><span>${tb.name.esc()}:</span><span>${tb.amountFormatted?.esc() ?: tb.amount.fmt()}</span></div>""")
                }
            } else if ((summary?.tax ?: 0.0) != 0.0) {
                val pct = summary?.taxRatePercent ?: 19
                val taxFmt = summary?.taxFormatted?.esc() ?: summary?.tax.fmt()
                append("""<div class="total-row"><span>IVA ($pct%):</span><span>$taxFmt</span></div>""")
            }
        }

        val paymentSection = buildString {
            val hasMultiple = payment?.hasMultiplePayments == true
            if (hasMultiple && !payment?.payments.isNullOrEmpty()) {
                append("""<div class="ticket-row"><span class="bold">Pagos:</span></div>""")
                payment?.payments?.forEach { p ->
                    val label = p.methodLabel?.esc() ?: p.method?.esc() ?: "Pago"
                    val amt = p.amountFormatted?.esc() ?: p.amount.fmt()
                    append("""<div class="ticket-row"><span>$label:</span><span>$amt</span></div>""")
                }
            } else {
                val label = payment?.methodLabel?.esc() ?: "Pago"
                val paid = payment?.amountPaidFormatted?.esc() ?: payment?.amountPaid.fmt()
                append("""<div class="ticket-row"><span>$label:</span><span>$paid</span></div>""")
            }
            val change = payment?.change ?: 0.0
            if (change > 0) {
                val chgFmt = payment?.changeFormatted?.esc() ?: change.fmt()
                append("""<div class="ticket-row"><span>Vuelto:</span><span>$chgFmt</span></div>""")
            }
        }

        val noFiscalBadge = if (!isTributary)
            """<div class="no-fiscal">TICKET SIN VALOR TRIBUTARIO</div>""" else ""

        val footerText = if (!data.footerText.isNullOrBlank())
            """<div>${data.footerText.esc()}</div>""" else ""
        val storeWebsite = if (!store?.website.isNullOrBlank())
            """<div>${store?.website.esc()}</div>""" else ""

        return loadTemplate(context, "ticket-58mm.html")
            .replace("{{LOGO_SECTION}}", logoSection)
            .replace("{{STORE_NAME}}", store?.name.esc())
            .replace("{{STORE_ADDRESS}}", storeAddress)
            .replace("{{STORE_PHONE}}", storePhone)
            .replace("{{STORE_TAX_ID}}", storeTaxId)
            .replace("{{HEADER_TEXT_SECTION}}", headerTextSection)
            .replace("{{DOC_TYPE_LABEL}}", data.docTypeLabel.esc())
            .replace("{{TICKET_NUMBER}}", ticketHdr?.number.esc())
            .replace("{{TICKET_DATE}}", ticketHdr?.dateFormatted.esc())
            .replace("{{TICKET_CASHIER}}", ticketHdr?.cashier.esc())
            .replace("{{TICKET_CASHBOX_ROW}}", cashboxRow)
            .replace("{{CUSTOMER_ROW}}", customerRow)
            .replace("{{ITEMS_ROWS}}", itemsRows)
            .replace("{{SUBTOTAL_ROW}}", subtotalRow)
            .replace("{{DISCOUNTS_ROW}}", discountsRow)
            .replace("{{TAX_ROWS}}", taxRows)
            .replace("{{TOTAL_FORMATTED}}", summary?.totalFormatted?.esc() ?: summary?.total.fmt())
            .replace("{{PAYMENT_SECTION}}", paymentSection)
            .replace("{{NO_FISCAL_BADGE}}", noFiscalBadge)
            .replace("{{FOOTER_TEXT}}", footerText)
            .replace("{{STORE_WEBSITE}}", storeWebsite)
    }

    fun renderCashboxOpening(context: Context, response: TicketDataResponseDto): String {
        val data = response.data ?: return ""
        val store = data.store
        val session = data.session
        val logoUrl = response.logoAbsoluteUrl ?: response.logoUrl

        val logoSection = if (!logoUrl.isNullOrBlank())
            """<div class="logo-container"><img src="${logoUrl.esc()}" alt="Logo" /></div>"""
        else ""

        val storeAddress = if (!store?.address.isNullOrBlank())
            """<div class="store-info">${store?.address.esc()}</div>""" else ""
        val storePhone = if (!store?.phone.isNullOrBlank())
            """<div class="store-info">Tel: ${store?.phone.esc()}</div>""" else ""
        val storeTaxId = if (!store?.taxId.isNullOrBlank())
            """<div class="store-info">RUT: ${store?.taxId.esc()}</div>""" else ""

        val posSerialRow = if (!session?.posSerial.isNullOrBlank())
            """<div class="row"><span class="label">POS:</span><span class="value">${session?.posSerial.esc()}</span></div>""" else ""

        val notesSection = if (!session?.notes.isNullOrBlank())
            """<div style="font-size:0.85em; font-style:italic; margin:3px 0;">${session?.notes.esc()}</div>""" else ""

        val cashboxNum = session?.cashboxNumber?.toString() ?: "—"

        val footerSection = """<div class="footer">Comprobante de apertura de caja</div>"""

        return loadTemplate(context, "cashbox-opening-58mm.html")
            .replace("{{LOGO_SECTION}}", logoSection)
            .replace("{{STORE_NAME}}", store?.name.esc())
            .replace("{{STORE_ADDRESS}}", storeAddress)
            .replace("{{STORE_PHONE}}", storePhone)
            .replace("{{STORE_TAX_ID}}", storeTaxId)
            .replace("{{CASHIER_NAME}}", session?.cashierName.esc())
            .replace("{{CASHBOX_NUMBER}}", cashboxNum)
            .replace("{{POS_SERIAL_ROW}}", posSerialRow)
            .replace("{{OPENED_AT}}", session?.openedAtFormatted.esc())
            .replace("{{INITIAL_AMOUNT}}", session?.initialAmountFormatted?.esc() ?: session?.initialAmount.fmt())
            .replace("{{NOTES_SECTION}}", notesSection)
            .replace("{{SESSION_ID}}", session?.id.esc())
            .replace("{{GENERATED_AT}}", now())
            .replace("{{FOOTER_SECTION}}", footerSection)
    }

    fun renderCashboxClose(context: Context, response: TicketDataResponseDto): String {
        val data = response.data ?: return ""
        val store = data.store
        val session = data.session
        val summary = data.summary
        val logoUrl = response.logoAbsoluteUrl ?: response.logoUrl

        val logoSection = if (!logoUrl.isNullOrBlank())
            """<div class="logo-container"><img src="${logoUrl.esc()}" alt="Logo" /></div>"""
        else ""

        val storeAddress = if (!store?.address.isNullOrBlank())
            """<div class="store-info">${store?.address.esc()}</div>""" else ""
        val storePhone = if (!store?.phone.isNullOrBlank())
            """<div class="store-info">Tel: ${store?.phone.esc()}</div>""" else ""
        val storeTaxId = if (!store?.taxId.isNullOrBlank())
            """<div class="store-info">RUT: ${store?.taxId.esc()}</div>""" else ""

        val posSerialRow = if (!session?.posSerial.isNullOrBlank())
            """<div class="row"><span class="label">POS:</span><span class="value-normal">${session?.posSerial.esc()}</span></div>""" else ""

        val cashboxNum = session?.cashboxNumber?.toString() ?: "—"

        val extraIncomes = buildString {
            data.movements?.filter { it.type == "income" || it.movementCode?.startsWith("ING") == true }
                ?.forEach { m ->
                    val desc = (m.description ?: m.movementCode ?: "Ingreso").esc()
                    val amt = m.amountFormatted?.esc() ?: m.amount.fmt()
                    append("""<div class="row"><span class="label">+ $desc:</span><span class="value-normal">$amt</span></div>""")
                }
        }

        val extraExpenses = buildString {
            data.movements?.filter { it.type == "expense" || it.movementCode?.startsWith("EGR") == true }
                ?.forEach { m ->
                    val desc = (m.description ?: m.movementCode ?: "Egreso").esc()
                    val amt = m.amountFormatted?.esc() ?: m.amount.fmt()
                    append("""<div class="row"><span class="label">- $desc:</span><span class="value-normal">$amt</span></div>""")
                }
        }

        val diffPercent = summary?.diffPercent
        val diffPercentRow = if (diffPercent != null && diffPercent != 0.0)
            """<div style="font-size:0.8em;">${String.format(clpLocale, "%.1f%%", diffPercent)}</div>""" else ""

        val statusLabel = when (summary?.status?.uppercase()) {
            "SOBRANTE" -> "✓ CUADRADA / SOBRANTE"
            "FALTANTE" -> "✗ FALTANTE"
            "CUADRADA" -> "✓ CUADRADA"
            else -> summary?.status?.esc() ?: ""
        }

        val notesSection = if (!session?.notes.isNullOrBlank())
            """<div class="notes-box">${session?.notes.esc()}</div>""" else ""

        val posSerialTech = if (!session?.posSerial.isNullOrBlank())
            """<div>POS: ${session?.posSerial.esc()}</div>""" else ""

        val footerSection = """<div class="footer">Comprobante de cierre de caja</div>"""

        return loadTemplate(context, "cashbox-close-58mm.html")
            .replace("{{LOGO_SECTION}}", logoSection)
            .replace("{{STORE_NAME}}", store?.name.esc())
            .replace("{{STORE_ADDRESS}}", storeAddress)
            .replace("{{STORE_PHONE}}", storePhone)
            .replace("{{STORE_TAX_ID}}", storeTaxId)
            .replace("{{CASHIER_NAME}}", session?.cashierName.esc())
            .replace("{{CASHBOX_NUMBER}}", cashboxNum)
            .replace("{{POS_SERIAL_ROW}}", posSerialRow)
            .replace("{{OPENED_AT}}", session?.openedAtFormatted.esc())
            .replace("{{CLOSED_AT}}", session?.closedAtFormatted.esc())
            .replace("{{INITIAL_AMOUNT}}", summary?.initialAmountFormatted?.esc() ?: summary?.initialAmount.fmt())
            .replace("{{TOTAL_SALES}}", summary?.totalSalesFormatted?.esc() ?: summary?.totalSales.fmt())
            .replace("{{EXTRA_INCOMES}}", extraIncomes)
            .replace("{{EXTRA_EXPENSES}}", extraExpenses)
            .replace("{{EXPECTED_AMOUNT}}", summary?.expectedAmountFormatted?.esc() ?: summary?.expectedAmount.fmt())
            .replace("{{FINAL_AMOUNT}}", summary?.finalAmountFormatted?.esc() ?: summary?.finalAmount.fmt())
            .replace("{{DIFFERENCE}}", summary?.differenceFormatted?.esc() ?: summary?.difference.fmt())
            .replace("{{DIFF_PERCENT_ROW}}", diffPercentRow)
            .replace("{{DIFFERENCE_STATUS}}", statusLabel)
            .replace("{{NOTES_SECTION}}", notesSection)
            .replace("{{SESSION_ID}}", session?.id.esc())
            .replace("{{POS_SERIAL_TECH}}", posSerialTech)
            .replace("{{GENERATED_AT}}", now())
            .replace("{{FOOTER_SECTION}}", footerSection)
    }

    /**
     * Renderiza un ticket DTE (`dte-58mm.html`).
     *
     * Cuando la venta se acaba de emitir, el endpoint `ticket-data/sale` aún no refleja el
     * DTE, por eso se aceptan overrides con los datos reales devueltos por `emitir-dte`:
     * [folioOverride], [timbreBase64] (PNG del PDF417), [docTypeLabelOverride] y
     * [receptorOverride] (datos del receptor de factura capturados en el POS).
     */
    fun renderDte(
        context: Context,
        response: TicketDataResponseDto,
        folioOverride: String? = null,
        timbreBase64: String? = null,
        docTypeLabelOverride: String? = null,
        receptorOverride: cl.friendlypos.mypos.checkout.CheckoutHolder.ReceptorData? = null
    ): String {
        val data = response.data ?: return ""
        val store = data.store
        val ticketHdr = data.ticket
        val customer = data.customer
        val summary = data.summary
        val logoUrl = response.logoAbsoluteUrl ?: response.logoUrl

        val logoSection = if (!logoUrl.isNullOrBlank())
            """<div class="logo-container"><img src="${logoUrl.esc()}" alt="Logo" /></div>"""
        else ""

        val storeRut = if (!store?.taxId.isNullOrBlank())
            """<div class="store-info">RUT: ${store?.taxId.esc()}</div>""" else ""
        val storeAddress = if (!store?.address.isNullOrBlank())
            """<div class="store-info">${store?.address.esc()}</div>""" else ""
        val storePhone = if (!store?.phone.isNullOrBlank())
            """<div class="store-info">Tel: ${store?.phone.esc()}</div>""" else ""
        val storeEmail = if (!store?.email.isNullOrBlank())
            """<div class="store-info">${store?.email.esc()}</div>""" else ""

        val folio = folioOverride?.takeIf { it.isNotBlank() } ?: data.folio
        val folioRow = if (!folio.isNullOrBlank())
            """<div class="doc-folio">N° ${folio.esc()}</div>""" else ""

        val headerTextSection = if (!data.headerText.isNullOrBlank())
            """<div class="header-text">${data.headerText.esc()}</div>""" else ""

        val receptorSection = buildString {
            val rName = receptorOverride?.razonSocial ?: customer?.name
            val rRut = receptorOverride?.rut ?: customer?.rut
            val rGiro = receptorOverride?.giro
            val rDir = receptorOverride?.direccion
            if (!rName.isNullOrBlank() || !rRut.isNullOrBlank()) {
                append("""<div class="section-label">Receptor</div>""")
                append("""<div class="receptor-block">""")
                if (!rName.isNullOrBlank()) append("""<div class="receptor-row"><span>Razón Social:</span><span>${rName.esc()}</span></div>""")
                if (!rRut.isNullOrBlank()) append("""<div class="receptor-row"><span>RUT:</span><span>${rRut.esc()}</span></div>""")
                if (!rGiro.isNullOrBlank()) append("""<div class="receptor-row"><span>Giro:</span><span>${rGiro.esc()}</span></div>""")
                if (!rDir.isNullOrBlank()) append("""<div class="receptor-row"><span>Dirección:</span><span>${rDir.esc()}</span></div>""")
                append("""</div>""")
            }
        }

        val itemsRows = buildString {
            data.items?.forEach { item ->
                val qty = item.quantity ?: 1
                val desc = item.name.esc()
                val price = item.priceFormatted?.esc() ?: item.price.fmt()
                val subtotal = item.subtotalFormatted?.esc() ?: item.subtotal.fmt()
                append("""<tr><td>$desc</td><td class="right">$qty</td><td class="right">$price</td><td class="right">$subtotal</td></tr>""")
            }
        }

        val netoRow = if ((summary?.subtotal ?: 0.0) != 0.0)
            """<div class="total-row"><span>Neto:</span><span>${summary?.subtotalFormatted?.esc() ?: summary?.subtotal.fmt()}</span></div>""" else ""
        val ivaRow = if ((summary?.tax ?: 0.0) != 0.0) {
            val pct = summary?.taxRatePercent ?: 19
            """<div class="total-row"><span>IVA ($pct%):</span><span>${summary?.taxFormatted?.esc() ?: summary?.tax.fmt()}</span></div>"""
        } else ""
        val exentoRow = ""

        val tedSection = if (!timbreBase64.isNullOrBlank())
            """<div class="ted-block"><div class="ted-label">Timbre Electrónico SII</div><img src="data:image/png;base64,${timbreBase64.trim()}" alt="Timbre" style="max-width:100%;height:auto;" /><div class="ted-stamp">Verifique en www.sii.cl</div></div>"""
        else
            """<div class="ted-block"><div class="ted-label">Timbre Electrónico SII</div><div class="ted-stamp">[ Ver documento en sii.cl ]</div></div>"""

        val siiLegend = """<div class="sii-legend">Resolución SII N° 45 del 2003</div>"""
        val footerText = if (!data.footerText.isNullOrBlank())
            """<div>${data.footerText.esc()}</div>""" else ""
        val storeWebsite = if (!store?.website.isNullOrBlank())
            """<div>${store?.website.esc()}</div>""" else ""

        return loadTemplate(context, "dte-58mm.html")
            .replace("{{LOGO_SECTION}}", logoSection)
            .replace("{{STORE_NAME}}", store?.name.esc())
            .replace("{{STORE_RUT}}", storeRut)
            .replace("{{STORE_ADDRESS}}", storeAddress)
            .replace("{{STORE_PHONE}}", storePhone)
            .replace("{{STORE_EMAIL}}", storeEmail)
            .replace("{{DOC_TYPE_LABEL}}", (docTypeLabelOverride ?: data.docTypeLabel).esc())
            .replace("{{FOLIO_ROW}}", folioRow)
            .replace("{{TICKET_DATE}}", ticketHdr?.dateFormatted.esc())
            .replace("{{HEADER_TEXT_SECTION}}", headerTextSection)
            .replace("{{RECEPTOR_SECTION}}", receptorSection)
            .replace("{{ITEMS_ROWS}}", itemsRows)
            .replace("{{NETO_ROW}}", netoRow)
            .replace("{{IVA_ROW}}", ivaRow)
            .replace("{{EXENTO_ROW}}", exentoRow)
            .replace("{{TOTAL_FORMATTED}}", summary?.totalFormatted?.esc() ?: summary?.total.fmt())
            .replace("{{TED_SECTION}}", tedSection)
            .replace("{{SII_LEGEND}}", siiLegend)
            .replace("{{FOOTER_TEXT}}", footerText)
            .replace("{{STORE_WEBSITE}}", storeWebsite)
    }
}
