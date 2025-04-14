package cl.friendlypos.mypos.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cl.friendlypos.mypos.model.Sale

class CartViewModel : ViewModel() {
    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _subtotal = MutableLiveData<Int>(0)
    val subtotal: LiveData<Int> = _subtotal

    private val _itemCount = MutableLiveData<Int>(0)
    val itemCount: LiveData<Int> = _itemCount

    private val _currentSale = MutableLiveData<Sale>()
    val currentSale: LiveData<Sale> = _currentSale

    fun addItem(item: CartItem) {
        val currentItems = _cartItems.value?.toMutableList() ?: mutableListOf()
        val existingItemIndex = currentItems.indexOfFirst { it.id == item.id }
        if (existingItemIndex >= 0) {
            val existingItem = currentItems[existingItemIndex]
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + item.quantity)
            currentItems[existingItemIndex] = updatedItem
        } else {
            currentItems.add(item)
        }
        _cartItems.value = currentItems
        updateTotals()
    }

    fun removeItem(itemId: String) {
        val currentItems = _cartItems.value?.toMutableList() ?: mutableListOf()
        currentItems.removeAll { it.id == itemId }
        _cartItems.value = currentItems
        updateTotals()
    }

    fun updateItemQuantity(itemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItem(itemId)
            return
        }
        val currentItems = _cartItems.value?.toMutableList() ?: mutableListOf()
        val itemIndex = currentItems.indexOfFirst { it.id == itemId }
        if (itemIndex >= 0) {
            val item = currentItems[itemIndex]
            currentItems[itemIndex] = item.copy(quantity = newQuantity)
            _cartItems.value = currentItems
            updateTotals()
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        updateTotals()
    }

    private fun updateTotals() {
        val items = _cartItems.value ?: emptyList()
        _subtotal.value = items.sumOf { it.price * it.quantity }
        _itemCount.value = items.sumOf { it.quantity }
    }

    fun setCurrentSale(sale: Sale) {
        _currentSale.value = sale
    }

    fun formatPrice(price: Int): String {
        return "$${price.toString().replace(Regex("(\\d)(?=(\\d{3})+\$)"), "$1,")}"
    }

    fun setCartItems(items: List<CartItem>) {
        _cartItems.value = items
        updateTotals()
    }
}