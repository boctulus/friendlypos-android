package cl.friendlypos.mypos.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.data.DummyDataRepository
import cl.friendlypos.mypos.model.SaleReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PaymentsViewModel : ViewModel() {

    private val _payments = MutableStateFlow<List<SaleReport>>(emptyList())
    val payments: StateFlow<List<SaleReport>> = _payments.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _fromDate = MutableStateFlow<LocalDate?>(null)
    val fromDate: StateFlow<LocalDate?> = _fromDate.asStateFlow()

    private val _toDate = MutableStateFlow<LocalDate?>(null)
    val toDate: StateFlow<LocalDate?> = _toDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filteredPayments: StateFlow<List<SaleReport>> = combine(
        _payments,
        _searchQuery,
        _fromDate,
        _toDate
    ) { payments, query, fromDate, toDate ->
        var filtered = payments

        // Filtrar por búsqueda de customer
        if (query.isNotBlank()) {
            filtered = filtered.filter { payment ->
                payment.customerName.contains(query, ignoreCase = true)
            }
        }

        // Filtrar por fechas
        if (fromDate != null) {
            filtered = filtered.filter { payment ->
                payment.date >= fromDate
            }
        }

        if (toDate != null) {
            filtered = filtered.filter { payment ->
                payment.date <= toDate
            }
        }

        // Ordenar por fecha más reciente primero
        filtered.sortedByDescending { it.date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadPayments()
    }

    private fun loadPayments() {
        viewModelScope.launch {
            _isLoading.value = true
            DummyDataRepository.getSalesReports().collect { paymentList ->
                _payments.value = paymentList
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

    fun updateFromDate(date: LocalDate?) {
        _fromDate.value = date
    }

    fun updateToDate(date: LocalDate?) {
        _toDate.value = date
    }

    fun clearDateFilters() {
        _fromDate.value = null
        _toDate.value = null
    }
}