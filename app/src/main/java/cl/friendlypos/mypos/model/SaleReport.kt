package cl.friendlypos.mypos.model

import java.time.LocalDate

data class SaleReport(
    val id: String,
    val customerName: String,
    val total: Double,
    val paymentMethod: String,
    val date: LocalDate,
    val items: List<ProductSale> = emptyList(), // <-- FIX: Changed type from List<String> to List<ProductSale>
    val status: String = "Completed"
    // val totalAmount: List<ProductSale> = total, // <-- FIX: Removed this incorrect line
)

data class ProductSale(
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double
)

data class ReportSummary(
    val totalCustomers: Int,
    val totalProducts: Int,
    val totalSales: Double,
    val dateRange: String
)