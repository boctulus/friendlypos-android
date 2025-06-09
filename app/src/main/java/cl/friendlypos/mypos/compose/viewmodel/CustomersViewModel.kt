package cl.friendlypos.mypos.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.data.DummyDataRepository
import cl.friendlypos.mypos.model.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class CustomersViewModel : ViewModel() {

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filteredCustomers: StateFlow<List<Customer>> = combine(_customers, _searchQuery) { customers, query ->
        if (query.isBlank()) {
            customers
        } else {
            customers.filter { customer ->
                customer.name.contains(query, ignoreCase = true) ||
                        customer.email.contains(query, ignoreCase = true) ||
                        customer.phone.contains(query, ignoreCase = true) ||
                        customer.address.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadCustomers()
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            _isLoading.value = true
            DummyDataRepository.getCustomers().collect { customerList ->
                _customers.value = customerList
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