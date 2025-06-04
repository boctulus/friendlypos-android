package cl.friendlypos.mypos.model

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val category: String,
    val barcode: String? = null,
    val imageUrl: String? = null
)