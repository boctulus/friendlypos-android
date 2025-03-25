package cl.friendlypos.mypos.ui.sales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SalesCalculatorViewModel : ViewModel() {

    private val _currentAmount = MutableLiveData<String>("0")
    val currentAmount: LiveData<String> = _currentAmount

    private val _totalAmount = MutableLiveData<String>("0")
    val totalAmount: LiveData<String> = _totalAmount

    private val _currentItemName = MutableLiveData<String>("Nombre item 1")
    val currentItemName: LiveData<String> = _currentItemName

    // Keep track of items in the current sale
    private val saleItems = mutableListOf<Pair<String, Double>>()

    fun appendDigit(digit: String) {
        val current = _currentAmount.value ?: "0"
        if (current == "0" && digit != "00") {
            _currentAmount.value = digit
        } else if (current != "0") {
            _currentAmount.value = current + digit
        }
    }

    fun clearEntry() {
        _currentAmount.value = "0"
    }

    fun deleteLastDigit() {
        val current = _currentAmount.value ?: "0"
        if (current.length > 1) {
            _currentAmount.value = current.substring(0, current.length - 1)
        } else {
            _currentAmount.value = "0"
        }
    }

    fun addItemToSale() {
        val itemAmount = _currentAmount.value?.toDoubleOrNull() ?: 0.0
        val itemName = _currentItemName.value ?: "Item"

        if (itemAmount > 0) {
            saleItems.add(Pair(itemName, itemAmount))
            updateTotal()

            // Reset for next item
            _currentAmount.value = "0"
            _currentItemName.value = "Nombre item ${saleItems.size + 1}"
        }
    }

    private fun updateTotal() {
        val total = saleItems.sumOf { it.second }
        _totalAmount.value = total.toString()
    }

    fun processSale() {
        // Add current item if not empty
        val currentValue = _currentAmount.value?.toDoubleOrNull() ?: 0.0
        if (currentValue > 0) {
            addItemToSale()
        }

        // Process payment (in a real app, this would navigate to payment screen)
        // For now, just reset the sale
        saleItems.clear()
        _currentAmount.value = "0"
        _totalAmount.value = "0"
        _currentItemName.value = "Nombre item 1"
    }
}