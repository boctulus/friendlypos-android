package cl.friendlypos.mypos.model

data class SaleReport(
    val id: String,
    val date: String,
    val customerName: String,
    val products: List<ProductSale>,
    val total: Double,
    val paymentMethod: String
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