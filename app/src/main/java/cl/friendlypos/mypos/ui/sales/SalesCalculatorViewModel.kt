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

    // Acumulador para el total de la venta
    private var accumulator: Double = 0.0

    // Lista para almacenar cada ítem de la venta
    private val _saleItems = MutableLiveData<List<SaleItem>>(emptyList())
    val saleItems: LiveData<List<SaleItem>> = _saleItems

    // LiveData para contar la cantidad total de productos (suma de cantidades)
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

    // Nueva función para agregar un operador (ej. "x" para multiplicación)
    fun appendOperator(operator: String) {
        Log.d("Calc", "Operator to be appended: $operator")

        val current = _currentAmount.value ?: "0"
        // Evitar agregar operador al inicio o duplicado
        if (current == "0") return
        if (current.contains(operator)) return
        _currentAmount.value = current + operator
    }

    fun appendDecimal(symbol: String) {
        Log.d("Calc", "Decimal symbol to be appended: $symbol")

        val current = _currentAmount.value ?: "0"
        // Evitar agregar operador al inicio o duplicado
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

            val totalItem = unitPrice * quantity
            accumulator += totalItem
            Log.d("Calc", "Item total: $totalItem, Accumulator: $accumulator")

            val itemName = _currentItemName.value ?: "Item"
            val newItem = SaleItem(unitPrice = unitPrice, quantity = quantity, name = itemName)
            val currentItems = _saleItems.value?.toMutableList() ?: mutableListOf()
            currentItems.add(newItem)

            _saleItems.value = currentItems
            _totalAmount.value = accumulator.toInt().toString()
            Log.d("SalesCalcVM", "Item añadido: $newItem, Total items: ${currentItems.size}")

            val cartCount = (_cartItemCount.value ?: 0) + quantity
            _cartItemCount.value = cartCount
            Log.d("Calc", "Cart count updated to: $cartCount")

            _currentAmount.value = "0"
            _currentItemName.value = "Nombre item ${currentItems.size + 1}"
            Log.d("Calc", "Item added successfully")
        } catch (e: Exception) {
            Log.e("SalesCalc", "Error adding item: ${e.message}", e)
        }
    }

    fun processSale() {
        // Si hay un valor pendiente, lo agrega antes de procesar la venta
        val currentValue = _currentAmount.value?.toIntOrNull() ?: 0
        if (currentValue > 0) {
            addItemToSale()
        }

        // Aquí se podría procesar el pago real.
        // Se reinician los valores.

        _saleItems.value = emptyList()
        accumulator = 0.0
        _currentAmount.value = "0"
        _totalAmount.value = "0"
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
