package cl.friendlypos.mypos.compose.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.model.Product
import cl.friendlypos.mypos.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductsViewModel : ViewModel() {

    private val repo = ProductRepository()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var nextCursor: String? = null
    private var hasMore: Boolean = true

    val filteredProducts: StateFlow<List<Product>> = combine(_products, _searchQuery) { products, query ->
        if (query.isBlank()) products
        else products.filter { product ->
            product.name.contains(query, ignoreCase = true) ||
                product.description.contains(query, ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            nextCursor = null
            hasMore = true

            val result = repo.getPage(cursor = null)
            result.fold(
                onSuccess = { page ->
                    _products.value = page.products
                    nextCursor = page.nextCursor
                    hasMore = page.hasMore
                },
                onFailure = { e ->
                    _errorMessage.value = e.message
                    Log.e("ProductsVM", "Error cargando productos: ${e.message}", e)
                }
            )
            _isLoading.value = false
        }
    }

    fun loadMore() {
        if (_isLoadingMore.value || !hasMore || nextCursor == null) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val result = repo.getPage(cursor = nextCursor)
            result.fold(
                onSuccess = { page ->
                    _products.value = _products.value + page.products
                    nextCursor = page.nextCursor
                    hasMore = page.hasMore
                },
                onFailure = { e ->
                    Log.e("ProductsVM", "Error cargando más productos: ${e.message}", e)
                }
            )
            _isLoadingMore.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}
