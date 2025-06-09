package cl.friendlypos.mypos.model

import java.time.LocalDate

data class SaleReport(
    val id: String,
    val customerName: String,
    val total: Double,
    val paymentMethod: String,
    val date: LocalDate,
    val items: List<ProductSale> = emptyList(),
    val status: String = "Completed"
)

data class ProductSale(
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double
)

data class ChartData(
    val date: LocalDate,
    val amount: Double
)

data class ReportSummary(
    val totalCustomers: Int,
    val totalProducts: Int,
    val totalSales: Double,
    val dateRange: String,
    val chartData: List<ChartData> = emptyList()
)