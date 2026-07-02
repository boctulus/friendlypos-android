package cl.friendlypos.mypos.ui.sales

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.model.Product
import cl.friendlypos.mypos.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SalesCalculatorViewModel : ViewModel() {

    private val productRepo = ProductRepository()

    private val _productSearchResults = MutableLiveData<List<Product>>(emptyList())
    val productSearchResults: LiveData<List<Product>> = _productSearchResults

    private val _productSearchLoading = MutableLiveData<Boolean>(false)
    val productSearchLoading: LiveData<Boolean> = _productSearchLoading

    private val _productSearchError = MutableLiveData<String?>(null)
    val productSearchError: LiveData<String?> = _productSearchError

    private var searchJob: Job? = null

    fun searchProducts(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _productSearchResults.value = emptyList()
            _productSearchLoading.value = false
            _productSearchError.value = null
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _productSearchLoading.value = true
            _productSearchError.value = null
            val result = productRepo.searchQuick(query)
            result.fold(
                onSuccess = {
                    _productSearchResults.value = it
                    _productSearchLoading.value = false
                },
                onFailure = { e ->
                    _productSearchError.value = e.message
                    _productSearchLoading.value = false
                    Log.e("SalesCalcVM", "Error buscando productos: ${e.message}", e)
                }
            )
        }
    }

    fun clearProductSearch() {
        searchJob?.cancel()
        _productSearchResults.value = emptyList()
        _productSearchLoading.value = false
        _productSearchError.value = null
    }
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

    private val _cartSearchQuery = MutableLiveData<String>("")
    val cartSearchQuery: LiveData<String> = _cartSearchQuery

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

        Log.d("Calc", "Current amount: $current")
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
                    val priceStr = parts[0].trim().replace(",", "")
                    unitPrice = priceStr.toIntOrNull() ?: 0
                    quantity = parts[1].trim().toIntOrNull() ?: 1
                    Log.d("Calc", "Parsed multiplication: $unitPrice x $quantity")
                }
            } else {
                unitPrice = entry.replace(",", "").toIntOrNull() ?: 0
                Log.d("Calc", "Parsed single price: $unitPrice")
            }

            if (unitPrice <= 0) {
                Log.d("Calc", "Invalid price: $unitPrice, operation cancelled")
                return
            }

            val itemName = _currentItemName.value ?: getGenericItemName()
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
            _currentItemName.value = itemName

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

    fun getGenericItemName(): String {
        val currentItems = _saleItems.value?.toMutableList() ?: mutableListOf()
        return "Nombre item ${currentItems.size + 1}"
    }

    // Actualizar el nombre del item
    fun updateItemName(name: String) {
        // Si el nombre está vacío, asignar "Item" por defecto
        val updatedName = if (name.isBlank()) getGenericItemName() else name
        val capitalized = updatedName.replaceFirstChar { it.uppercase() }

        _currentItemName.value = capitalized
        Log.d("SalesCalcVM", "Nombre actualizado: $capitalized")
    }

    fun setAmount(amount: String) {
        _currentAmount.value = amount
    }

    fun clearCart() {
        _saleItems.value = emptyList()
        _totalAmount.value = "0"
        _cartItemCount.value = 0
    }

    fun updateCartSearchQuery(query: String) {
        _cartSearchQuery.value = query
    }

    fun updateSaleItem(updatedItem: SaleItem) {
        val currentItems = _saleItems.value?.toMutableList() ?: return
        val index = currentItems.indexOfFirst { it.id == updatedItem.id }
        if (index < 0) return
        currentItems[index] = updatedItem
        _saleItems.value = currentItems
        _totalAmount.value = currentItems.sumOf { it.unitPrice * it.quantity }.toString()
        _cartItemCount.value = currentItems.sumOf { it.quantity }
    }

    // ── Escaneo de productos al carrito ───────────────────────────────────────

    /** Feedback transitorio emitido al escanear (la UI muestra snackbar + vibración). */
    sealed interface ScanFeedback {
        data class Added(val name: String) : ScanFeedback
        data class NotFound(val code: String) : ScanFeedback
        data class Error(val message: String) : ScanFeedback
    }

    private val _scanFeedback = MutableSharedFlow<ScanFeedback>(extraBufferCapacity = 4)
    val scanFeedback: SharedFlow<ScanFeedback> = _scanFeedback.asSharedFlow()

    // Guard contra dobles lecturas: ignora escaneos mientras hay un lookup en curso.
    @Volatile
    private var isLookupInProgress: Boolean = false

    /**
     * Agrega un producto del catálogo al carrito. Si ya existe una línea del mismo
     * producto (mismo [Product.id]) incrementa su cantidad +1 EN SU POSICIÓN (no
     * reordena la lista). Si no existe, agrega una línea nueva al final.
     */
    fun addProductToCart(product: Product) {
        val currentItems = _saleItems.value?.toMutableList() ?: mutableListOf()
        val index = currentItems.indexOfFirst { it.productId == product.id }
        if (index >= 0) {
            val existing = currentItems[index]
            currentItems[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentItems.add(
                SaleItem(
                    productId = product.id,
                    name = product.name,
                    unitPrice = product.price.toInt(),
                    quantity = 1
                )
            )
        }
        _saleItems.value = currentItems
        _totalAmount.value = currentItems.sumOf { it.unitPrice * it.quantity }.toString()
        _cartItemCount.value = currentItems.sumOf { it.quantity }
    }

    /**
     * Resuelve un EAN13 escaneado a un producto y lo agrega al carrito.
     * Emite [ScanFeedback] para que la UI muestre el resultado.
     */
    fun scanBarcode(code: String, storeId: String) {
        if (isLookupInProgress) return
        if (!code.matches(Regex("\\d{13}"))) {
            _scanFeedback.tryEmit(ScanFeedback.NotFound(code))
            return
        }
        isLookupInProgress = true
        viewModelScope.launch {
            try {
                productRepo.lookupByEan(code, storeId).fold(
                    onSuccess = { product ->
                        if (product != null) {
                            addProductToCart(product)
                            _scanFeedback.tryEmit(ScanFeedback.Added(product.name))
                        } else {
                            _scanFeedback.tryEmit(ScanFeedback.NotFound(code))
                        }
                    },
                    onFailure = { e ->
                        Log.e("SalesCalcVM", "Error en lookup EAN $code: ${e.message}", e)
                        _scanFeedback.tryEmit(ScanFeedback.Error(e.message ?: "Error de red"))
                    }
                )
            } finally {
                isLookupInProgress = false
            }
        }
    }
}