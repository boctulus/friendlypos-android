package cl.friendlypos.mypos.api.dto

import com.google.gson.annotations.SerializedName

data class TicketDataResponseDto(
    val success: Boolean,
    val type: String?,
    @SerializedName("paper_size") val paperSize: String?,
    @SerializedName("paper_sizes_available") val paperSizes: List<String>?,
    @SerializedName("logo_url") val logoUrl: String?,
    @SerializedName("logo_absolute_url") val logoAbsoluteUrl: String?,
    val data: TicketDataDto?
)

data class TicketDataDto(
    val docTypeLabel: String?,
    val store: TicketStoreDto?,
    val session: TicketSessionDto?,
    val ticket: TicketSaleHeaderDto?,
    val customer: TicketCustomerDto?,
    val items: List<TicketItemDto>?,
    val summary: TicketSummaryDto?,
    val movements: List<TicketMovementDto>?,
    val payment: TicketPaymentDto?,
    val footerText: String?,
    val headerText: String?,
    val isTributary: Boolean?,
    val folio: String?,
    @SerializedName("showBarcode") val showBarcode: Boolean?,
    @SerializedName("showLogo") val showLogo: Boolean?
)

data class TicketStoreDto(
    val name: String?,
    val address: String?,
    val phone: String?,
    val taxId: String?,
    val email: String?,
    val website: String?
)

data class TicketSessionDto(
    val id: String?,
    val cashierName: String?,
    val cashboxNumber: Any?,
    val posSerial: String?,
    val openedAtFormatted: String?,
    val closedAtFormatted: String?,
    val initialAmount: Double?,
    val initialAmountFormatted: String?,
    val notes: String?
)

data class TicketSaleHeaderDto(
    val number: String?,
    val dateFormatted: String?,
    val cashier: String?,
    val cashboxId: String?
)

data class TicketCustomerDto(
    val name: String?,
    val rut: String?,
    val email: String?
)

data class TicketItemDto(
    val name: String?,
    val quantity: Int?,
    val price: Double?,
    val priceFormatted: String?,
    val subtotal: Double?,
    val subtotalFormatted: String?,
    val discount: Double?,
    val discountFormatted: String?
)

data class TaxBreakdownDto(
    val name: String?,
    val amount: Double?,
    val amountFormatted: String?
)

data class PaymentDetailDto(
    val method: String?,
    val methodLabel: String?,
    val amount: Double?,
    val amountFormatted: String?
)

data class TicketSummaryDto(
    val initialAmount: Double?,
    val initialAmountFormatted: String?,
    val totalSales: Double?,
    val totalSalesFormatted: String?,
    val totalExpenses: Double?,
    val totalExpensesFormatted: String?,
    val expectedAmount: Double?,
    val expectedAmountFormatted: String?,
    val finalAmount: Double?,
    val finalAmountFormatted: String?,
    val difference: Double?,
    val differenceFormatted: String?,
    val diffPercent: Double?,
    val status: String?,
    val level: String?,
    val subtotal: Double?,
    val subtotalFormatted: String?,
    val tax: Double?,
    val taxFormatted: String?,
    val taxRatePercent: Int?,
    val taxBreakdown: List<TaxBreakdownDto>?,
    @SerializedName("discountsAmount") val discountsAmount: Double?,
    @SerializedName("discountsFormatted") val discountsFormatted: String?,
    val total: Double?,
    val totalFormatted: String?
)

data class TicketMovementDto(
    val code: String?,
    val movementCode: String?,
    val type: String?,
    val amount: Double?,
    val amountFormatted: String?,
    val description: String?,
    @SerializedName("created_at") val createdAt: String?,
    val createdAtFormatted: String?
)

data class TicketPaymentDto(
    val method: String?,
    val methodLabel: String?,
    val amountPaid: Double?,
    val amountPaidFormatted: String?,
    val change: Double?,
    val changeFormatted: String?,
    val hasMultiplePayments: Boolean?,
    val payments: List<PaymentDetailDto>?
)
