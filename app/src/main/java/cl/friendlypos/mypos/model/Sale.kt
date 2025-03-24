package cl.friendlypos.mypos.model

import java.util.Date

data class Sale(
    val id: String,
    val items: List<SaleItem>,
    val total: Double,
    val paymentMethod: String,
    val date: Date,
    val customerName: String = ""
)

data class SaleItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)