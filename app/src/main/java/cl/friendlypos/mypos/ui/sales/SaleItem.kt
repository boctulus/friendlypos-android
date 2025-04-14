package cl.friendlypos.mypos.ui.sales

import java.util.UUID

data class SaleItem(
    val id: String = UUID.randomUUID().toString(),
    val unitPrice: Int,
    val quantity: Int,
    val name: String = ""
)