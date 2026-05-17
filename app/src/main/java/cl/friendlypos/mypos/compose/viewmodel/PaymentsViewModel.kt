package cl.friendlypos.mypos.compose.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.model.SaleReport
import cl.friendlypos.mypos.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class PaymentsViewModel : ViewModel() {

    private val repository = ReportRepository()

    private val _payments = MutableStateFlow<List<SaleReport>>(emptyList())
    val payments: StateFlow<List<SaleReport>> = _payments.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _fromDate = MutableStateFlow<LocalDate?>(LocalDate.of(LocalDate.now().year, 1, 1))
    val fromDate: StateFlow<LocalDate?> = _fromDate.asStateFlow()

    private val _toDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    val toDate: StateFlow<LocalDate?> = _toDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val filteredPayments: StateFlow<List<SaleReport>> = combine(
        _payments,
        _searchQuery,
        _fromDate,
        _toDate
    ) { payments, query, fromDate, toDate ->
        var filtered = payments

        if (query.isNotBlank()) {
            filtered = filtered.filter { payment ->
                payment.customerName.contains(query, ignoreCase = true)
            }
        }

        if (fromDate != null) {
            filtered = filtered.filter { payment -> payment.date >= fromDate }
        }

        if (toDate != null) {
            filtered = filtered.filter { payment -> payment.date <= toDate }
        }

        filtered.sortedByDescending { it.date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadPayments()
    }

    fun loadPayments() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            runCatching {
                repository.getSales(
                    fromDate = _fromDate.value,
                    toDate = _toDate.value
                )
            }.onSuccess { list ->
                Log.d("PaymentsViewModel", "loadPayments OK — ${list.size} items")
                _payments.value = list
            }.onFailure { e ->
                Log.e("PaymentsViewModel", "loadPayments FAILED: ${e.javaClass.simpleName} — ${e.message}", e)
                _errorMessage.value = e.message ?: "Error al cargar pagos"
            }
            _isLoading.value = false
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
        loadPayments()
    }

    fun updateToDate(date: LocalDate?) {
        _toDate.value = date
        loadPayments()
    }

    fun clearDateFilters() {
        _fromDate.value = null
        _toDate.value = null
        loadPayments()
    }
}
