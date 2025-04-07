package cl.friendlypos.mypos.ui.sales

data class SaleItem(
    val unitPrice: Double,
    val quantity: Int,
    val name: String = ""
)

