package cl.friendlypos.mypos.checkout

import cl.friendlypos.mypos.ui.sales.SaleItem

/**
 * Snapshot en memoria del carrito al iniciar el flujo de pago.
 *
 * El carrito vive en `SalesCalculatorViewModel` (scope de la Activity principal), pero el
 * flujo de pago corre en Activities separadas (`PaymentActivity` → `CashPaymentActivity`)
 * que solo reciben el total por intent. Este holder transporta las líneas para poder
 * registrar la venta real (`POST /api/firestore/sales`) y construir el ticket de compra.
 */
object CheckoutHolder {

    data class Line(val name: String, val unitPrice: Int, val quantity: Int) {
        val lineTotal: Int get() = unitPrice * quantity
    }

    /** Datos del receptor para Factura Electrónica (DTE tipo 33). */
    data class ReceptorData(
        val rut: String,
        val razonSocial: String,
        val giro: String,
        val direccion: String,
        val comuna: String,
        val email: String? = null
    )

    var lines: List<Line> = emptyList()
        private set

    var documentType: DocumentType = DocumentType.DEFAULT

    /** Solo se usa cuando [documentType] == [DocumentType.FACTURA_ELECTRONICA]. */
    var facturaReceptor: ReceptorData? = null

    val total: Int get() = lines.sumOf { it.lineTotal }

    fun setFromCart(items: List<SaleItem>) {
        lines = items.map { Line(it.name, it.unitPrice, it.quantity) }
    }

    fun clear() {
        lines = emptyList()
        documentType = DocumentType.DEFAULT
        facturaReceptor = null
    }
}
