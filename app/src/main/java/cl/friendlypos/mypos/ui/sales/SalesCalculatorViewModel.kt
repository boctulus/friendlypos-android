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
    private val saleItems = mutableListOf<SaleItem>()

    // (Opcional) LiveData para contar la cantidad total de productos (suma de cantidades)
    private val _cartItemCount = MutableLiveData<Int>(0)
    val cartItemCount: LiveData<Int> = _cartItemCount

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
            // Captura la entrada actual y quita posibles símbolos $
            val entry = _currentAmount.value?.replace("$", "") ?: "0"
            Log.d("Calc", "Processing entry: $entry")

            var unitPrice = 0.0
            var quantity = 1

            // Procesa la multiplicación si existe
            if (entry.contains("x", ignoreCase = true)) {
                val parts = entry.split("x", ignoreCase = true)
                if (parts.size == 2) {
                    unitPrice = parts[0].toDoubleOrNull() ?: 0.0
                    quantity = parts[1].toIntOrNull() ?: 1
                    Log.d("Calc", "Parsed multiplication: $unitPrice x $quantity")
                }
            } else {
                unitPrice = entry.toDoubleOrNull() ?: 0.0
                Log.d("Calc", "Parsed single price: $unitPrice")
            }

            // Verifica que el precio no sea cero
            if (unitPrice <= 0) {
                Log.d("Calc", "Invalid price: $unitPrice, operation cancelled")
                return
            }

            // Calcula el total del ítem
            val totalItem = unitPrice * quantity
            accumulator += totalItem
            Log.d("Calc", "Item total: $totalItem, Accumulator: $accumulator")

            // Agrega el ítem a la lista
            val itemName = _currentItemName.value ?: "Item"
            saleItems.add(SaleItem(unitPrice, quantity, itemName))

            // Actualiza LiveData
            _totalAmount.value = accumulator.toString()

            // Actualiza el contador del carrito
            val cartCount = (_cartItemCount.value ?: 0) + quantity
            _cartItemCount.value = cartCount
            Log.d("Calc", "Cart count updated to: $cartCount")

            // Reinicia para el siguiente ítem
            _currentAmount.value = "0"
            _currentItemName.value = "Nombre item ${saleItems.size + 1}"

            Log.d("Calc", "Item added successfully")
        } catch (e: Exception) {
            Log.e("SalesCalc", "Error adding item: ${e.message}", e)
        }
    }

    fun processSale() {
        // Si hay un valor pendiente, lo agrega antes de procesar la venta
        val currentValue = _currentAmount.value?.toDoubleOrNull() ?: 0.0
        if (currentValue > 0) {
            addItemToSale()
        }
        // Aquí se podría procesar el pago real.
        // Para el ejemplo, se reinician los valores.
        saleItems.clear()
        accumulator = 0.0
        _currentAmount.value = "0"
        _totalAmount.value = "0"
        _currentItemName.value = "Nombre item 1"
        _cartItemCount.value = 0
    }
}
