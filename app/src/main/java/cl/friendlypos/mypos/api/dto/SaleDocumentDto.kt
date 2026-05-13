package cl.friendlypos.mypos.api.dto

import cl.friendlypos.mypos.model.ProductSale
import cl.friendlypos.mypos.model.SaleReport
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class SaleListResponseDto(
    val success: Boolean,
    val data: List<SaleDto>?,
    val total: Int?,
    val limit: Int?,
    val offset: Int?
)

data class SaleDto(
    val id: String,
    @SerializedName("ticket_number") val ticketNumber: String?,
    val customer: SaleCustomerDto?,
    val items: List<SaleItemDto>?,
    val subtotal: Double?,
    val tax: Double?,
    val total: Double?,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("amount_paid") val amountPaid: Double?,
    val status: String?,
    @SerializedName("cashier_name") val cashierName: String?,
    @SerializedName("tipo_documento") val tipoDocumento: String?,
    @SerializedName("is_invoiced") val isInvoiced: Boolean?,
    @SerializedName("created_at") val createdAt: String?
) {
    fun toSaleReport(): SaleReport {
        val date = createdAt?.let {
            runCatching {
                LocalDate.parse(it.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
            }.getOrNull()
        } ?: LocalDate.now()

        val productSales = items?.map { item ->
            ProductSale(
                productName = item.productName ?: "",
                quantity = item.quantity ?: 0,
                unitPrice = item.unitPrice ?: 0.0,
                total = item.total ?: 0.0
            )
        } ?: emptyList()

        return SaleReport(
            id = ticketNumber ?: id,
            customerName = customer?.name ?: customer?.rut ?: "Sin cliente",
            total = total ?: 0.0,
            paymentMethod = paymentMethod ?: tipoDocumento ?: "—",
            date = date,
            items = productSales,
            status = status ?: "Completado"
        )
    }
}

data class SaleCustomerDto(
    val name: String?,
    val rut: String?,
    val email: String?
)

data class SaleItemDto(
    @SerializedName("productName") val productName: String?,
    val quantity: Int?,
    @SerializedName("unitPrice") val unitPrice: Double?,
    val total: Double?
)
