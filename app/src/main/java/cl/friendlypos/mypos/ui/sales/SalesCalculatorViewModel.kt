package cl.friendlypos.mypos.ui.sales

import android.util.Log
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

    private val _saleItems = MutableLiveData<List<SaleItem>>(emptyList())
    val saleItems: LiveData<List<SaleItem>> = _saleItems

    private val _cartItemCount = MutableLiveData<Int>(0)
    val cartItemCount: LiveData<Int> = _cartItemCount

    init {
        Log.d("SalesCalcVM", "ViewModel creado: $this")
    }

    fun appendDigit(digit: String) {
        val current = _currentAmount.value ?: "0"
        if (current == "0") {
            _currentAmount.value = digit
        } else {
            _currentAmount.value = current + digit
        }
    }

    fun appendOperator(operator: String) {
        val current = _currentAmount.value ?: "0"
        if (current == "0" || current.contains(operator)) return
        _currentAmount.value = current + operator
    }

    fun appendDecimal(symbol: String) {
        val current = _currentAmount.value ?: "0"
        if (current.contains(symbol)) return
        _currentAmount.value = current + symbol
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
        try {
            val entry = _currentAmount.value?.replace("$", "") ?: "0"
            Log.d("Calc", "Processing entry: $entry")

            var unitPrice = 0
            var quantity = 1

            if (entry.contains("x", ignoreCase = true)) {
                val parts = entry.split("x", ignoreCase = true)
                if (parts.size == 2) {
                    quantity = parts[0].trim().toIntOrNull() ?: 1
                    val priceStr = parts[1].trim().replace(",", "")
                    unitPrice = priceStr.toIntOrNull() ?: 0
                    Log.d("Calc", "Parsed multiplication: $quantity x $unitPrice")
                }
            } else {
                unitPrice = entry.replace(",", "").toIntOrNull() ?: 0
                Log.d("Calc", "Parsed single price: $unitPrice")
            }

            if (unitPrice <= 0) {
                Log.d("Calc", "Invalid price: $unitPrice, operation cancelled")
                return
            }

            val itemName = _currentItemName.value ?: "Item"
            val newItem = SaleItem(unitPrice = unitPrice, quantity = quantity, name = itemName)
            val currentItems = _saleItems.value?.toMutableList() ?: mutableListOf()
            currentItems.add(newItem)

            _saleItems.value = currentItems
            Log.d("SalesCalcVM", "Item añadido: $newItem, Total items: ${currentItems.size}")

            val newTotal = currentItems.sumOf { it.unitPrice * it.quantity }
            _totalAmount.value = newTotal.toString()

            val newCount = currentItems.sumOf { it.quantity }
            _cartItemCount.value = newCount

            _currentAmount.value = "0"
            _currentItemName.value = "Nombre item ${currentItems.size + 1}"
            Log.d("Calc", "Item added successfully")
        } catch (e: Exception) {
            Log.e("SalesCalc", "Error adding item: ${e.message}", e)
        }
    }

    fun processSale() {
        val currentValue = _currentAmount.value?.toIntOrNull() ?: 0
        if (currentValue > 0) {
            addItemToSale()
        }

        _saleItems.value = emptyList()
        _totalAmount.value = "0"
        _currentAmount.value = "0"
        _currentItemName.value = "Nombre item 1"
        _cartItemCount.value = 0
    }

    fun removeSaleItem(item: SaleItem) {
        val currentItems = _saleItems.value?.toMutableList() ?: mutableListOf()
        if (currentItems.remove(item)) {
            _saleItems.value = currentItems
            val newTotal = currentItems.sumOf { it.unitPrice * it.quantity }
            _totalAmount.value = newTotal.toString()
            val newCount = currentItems.sumOf { it.quantity }
            _cartItemCount.value = newCount
        }
    }
}