package cl.friendlypos.mypos.model

data class Customer(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val totalPurchases: Double,
    val lastPurchaseDate: String
)