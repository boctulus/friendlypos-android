package cl.friendlypos.mypos.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.data.DummyDataRepository
import cl.friendlypos.mypos.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class ProductsViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filteredProducts: StateFlow<List<Product>> = combine(_products, _searchQuery) { products, query ->
        if (query.isBlank()) {
            products
        } else {
            products.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            DummyDataRepository.getProducts().collect { productList ->
                _products.value = productList
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}