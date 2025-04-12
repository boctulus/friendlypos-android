package cl.friendlypos.mypos.ui.cart

data class CartItem(
    val id: String,
    val name: String,
    val price: Int,
    val quantity: Int = 1
) {
    val total: Int
        get() = price * quantity
    
    val formattedPrice: String
        get() = "$${price.toString().replace(Regex("(\\d)(?=(\\d{3})+\$)"), "$1,")}"
    
    val formattedTotal: String
        get() = "$${total.toString().replace(Regex("(\\d)(?=(\\d{3})+\$)"), "$1,")}"
    
    val quantityText: String
        get() = "Cantidad: $quantity"
}