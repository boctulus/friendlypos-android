package cl.friendlypos.mypos.api.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs para emisión de DTE — `POST /api/sales/:saleId/emitir-dte`.
 * Contrato real: `modules/sales/routes/api.js` (FriendlyPOS backend).
 *
 * Nota: el backend toma los `items` y `totales` desde la venta ya registrada, por lo que
 * solo enviamos [tipo] y, para facturas, [receptor].
 */
data class EmitDteRequestDto(
    val tipo: String, // "boleta" | "factura"
    val receptor: ReceptorReqDto? = null
)

data class ReceptorReqDto(
    val rutReceptor: String,
    val razonSocialReceptor: String,
    val giroReceptor: String,
    val direccionReceptor: String,
    val comunaReceptor: String,
    val emailReceptor: String? = null
)

data class EmitDteResponseDto(
    val success: Boolean = false,
    val folio: Long? = null,
    val token: String? = null,
    /** PNG (base64) del Timbre Electrónico SII (TED / código PDF417). */
    val timbre: String? = null,
    /** PDF (base64) del documento oficial completo. */
    val pdf: String? = null,
    @SerializedName("pdfLocalUrl") val pdfLocalUrl: String? = null,
    @SerializedName("pdfFirebaseUrl") val pdfFirebaseUrl: String? = null,
    val error: String? = null,
    val message: String? = null,
    /** Presente cuando el backend responde 400 por DTE ya emitido (anti-duplicado). */
    val existingDTE: ExistingDteDto? = null
)

data class ExistingDteDto(
    val tipo: Int? = null,
    val folio: Long? = null,
    val fechaEmision: String? = null,
    val pdfLocalUrl: String? = null
)
