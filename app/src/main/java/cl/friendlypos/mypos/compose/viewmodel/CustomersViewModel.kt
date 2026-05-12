package cl.friendlypos.mypos.compose.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.model.Customer
import cl.friendlypos.mypos.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomersViewModel : ViewModel() {

    private val repo = CustomerRepository()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

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

    val filteredCustomers: StateFlow<List<Customer>> = combine(_customers, _searchQuery) { customers, query ->
        if (query.isBlank()) customers
        else customers.filter { customer ->
            customer.name.contains(query, ignoreCase = true) ||
            customer.email.contains(query, ignoreCase = true) ||
            customer.phone.contains(query, ignoreCase = true) ||
            customer.address.contains(query, ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            nextCursor = null
            hasMore = true

            val result = repo.getPage(cursor = null)
            result.fold(
                onSuccess = { page ->
                    _customers.value = page.customers
                    nextCursor = page.nextCursor
                    hasMore = page.hasMore
                },
                onFailure = { e ->
                    _errorMessage.value = e.message
                    Log.e("CustomersVM", "Error cargando clientes: ${e.message}", e)
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
                    _customers.value = _customers.value + page.customers
                    nextCursor = page.nextCursor
                    hasMore = page.hasMore
                },
                onFailure = { e ->
                    Log.e("CustomersVM", "Error cargando más clientes: ${e.message}", e)
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
