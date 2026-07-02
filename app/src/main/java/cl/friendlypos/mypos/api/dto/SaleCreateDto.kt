package cl.friendlypos.mypos.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Payload de registro de venta — `POST /api/firestore/sales`.
 * Estructura derivada del POS WEB (modules/sales/.../shared-payment-processing.js).
 * Un hook `afterCreate` de SalesModel registra el movimiento de caja en el backend.
 */
data class SaleCreateRequestDto(
    val customer: SaleCustomerReqDto,
    @SerializedName("ticket_number") val ticketNumber: String,
    val items: List<SaleItemReqDto>,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("amount_paid") val amountPaid: Double,
    val change: Double,
    val status: String = "completed",
    @SerializedName("cashbox_session_id") val cashboxSessionId: String?,
    @SerializedName("store_id") val storeId: String?,
    @SerializedName("tipo_documento") val tipoDocumento: String? = null,
    @SerializedName("transfer_bank") val transferBank: String? = null,
    @SerializedName("transfer_account_type") val transferAccountType: String? = null,
    @SerializedName("transfer_account_number") val transferAccountNumber: String? = null
)

data class SaleCustomerReqDto(
    val name: String,
    val id: String? = null
)

data class SaleItemReqDto(
    @SerializedName("product_id") val productId: String?,
    val name: String,
    val description: String?,
    val quantity: Int,
    val price: Double,
    val subtotal: Double,
    @SerializedName("price_includes_taxes") val priceIncludesTaxes: Boolean = true
)

data class SaleCreateResponseDto(
    val success: Boolean,
    val data: SaleCreatedDataDto?,
    val error: String?,
    val message: String?
)

data class SaleCreatedDataDto(
    val id: String?,
    @SerializedName("ticket_number") val ticketNumber: String?,
    val change: Double?
)
