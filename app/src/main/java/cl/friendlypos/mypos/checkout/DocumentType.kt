package cl.friendlypos.mypos.checkout

/**
 * Tipos de documento alineados con el backend (vistas EJS / módulo `tax_documents`).
 *
 * - [emitTipo] es el valor que espera `POST /api/sales/:saleId/emitir-dte` (`"boleta"` | `"factura"`).
 *   `null` ⇒ no se emite DTE (ticket sin valor tributario).
 */
enum class DocumentType(val label: String, val emitTipo: String?) {
    BOLETA_ELECTRONICA("Boleta Electrónica", "boleta"),
    FACTURA_ELECTRONICA("Factura Electrónica", "factura"),
    SIN_DOCUMENTO("Sin documento", null);

    /** `true` si esta selección genera un DTE en el backend. */
    val emitsDte: Boolean get() = emitTipo != null

    /** `true` si requiere capturar los datos del receptor (RUT, razón social, etc.). */
    val requiresReceptor: Boolean get() = this == FACTURA_ELECTRONICA

    companion object {
        /** Selección por defecto en el flujo de venta. */
        val DEFAULT = BOLETA_ELECTRONICA

        fun fromName(name: String?): DocumentType =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
