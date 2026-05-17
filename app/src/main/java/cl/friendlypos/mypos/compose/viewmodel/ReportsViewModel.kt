package cl.friendlypos.mypos.compose.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.model.ChartData
import cl.friendlypos.mypos.model.ReportSummary
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

class ReportsViewModel : ViewModel() {

    private val repository = ReportRepository()

    private val _sales = MutableStateFlow<List<SaleReport>>(emptyList())
    val sales: StateFlow<List<SaleReport>> = _sales.asStateFlow()

    private val _fromDate = MutableStateFlow<LocalDate?>(LocalDate.of(LocalDate.now().year, 1, 1))
    val fromDate: StateFlow<LocalDate?> = _fromDate.asStateFlow()

    private val _toDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    val toDate: StateFlow<LocalDate?> = _toDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _reportSummary = MutableStateFlow<ReportSummary?>(null)
    val reportSummary: StateFlow<ReportSummary?> = _reportSummary.asStateFlow()

    val filteredSales: StateFlow<List<SaleReport>> = combine(
        _sales,
        _fromDate,
        _toDate
    ) { sales, fromDate, toDate ->
        sales.filter { sale ->
            val isAfterFromDate = fromDate == null || !sale.date.isBefore(fromDate)
            val isBeforeToDate = toDate == null || !sale.date.isAfter(toDate)
            isAfterFromDate && isBeforeToDate
        }.sortedByDescending { it.date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val chartData: StateFlow<List<ChartData>> = combine(
        filteredSales
    ) { (sales) ->
        sales.groupBy { it.date }
            .map { (date, salesList) ->
                ChartData(
                    date = date,
                    amount = salesList.sumOf { it.total }
                )
            }
            .sortedBy { it.date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadSales()
    }

    private fun loadSales() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching {
                repository.getSales(
                    fromDate = _fromDate.value,
                    toDate = _toDate.value
                )
            }.onSuccess { salesList ->
                Log.d("ReportsViewModel", "loadSales OK — ${salesList.size} items")
                _sales.value = salesList
                _reportSummary.value = buildSummary(salesList)
            }.onFailure { e ->
                Log.e("ReportsViewModel", "loadSales FAILED: ${e.javaClass.simpleName} — ${e.message}", e)
                _error.value = e.message ?: "Error al cargar ventas"
            }
            _isLoading.value = false
        }
    }

    private fun buildSummary(salesList: List<SaleReport>): ReportSummary {
        return ReportSummary(
            totalSales = salesList.sumOf { it.total },
            totalCustomers = salesList.map { it.customerName }.distinct().size,
            totalProducts = salesList.size,
            dateRange = "${_fromDate.value} — ${_toDate.value}"
        )
    }

    fun updateFromDate(date: LocalDate?) {
        _fromDate.value = date
        loadSales()
    }

    fun updateToDate(date: LocalDate?) {
        _toDate.value = date
        loadSales()
    }

    fun clearDateFilters() {
        _fromDate.value = null
        _toDate.value = null
        loadSales()
    }
}
