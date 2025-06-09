package cl.friendlypos.mypos.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.data.DummyDataRepository
import cl.friendlypos.mypos.model.ChartData
import cl.friendlypos.mypos.model.ReportSummary
import cl.friendlypos.mypos.model.SaleReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReportsViewModel : ViewModel() {

    private val _sales = MutableStateFlow<List<SaleReport>>(emptyList())
    val sales: StateFlow<List<SaleReport>> = _sales.asStateFlow()

    private val _fromDate = MutableStateFlow<LocalDate?>(LocalDate.now().minusDays(30))
    val fromDate: StateFlow<LocalDate?> = _fromDate.asStateFlow()

    private val _toDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    val toDate: StateFlow<LocalDate?> = _toDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _reportSummary = MutableStateFlow<ReportSummary?>(null)
    val reportSummary: StateFlow<ReportSummary?> = _reportSummary.asStateFlow()

    // --- CORRECCIÓN 1: Devolver explícitamente la lista filtrada y ordenada ---
    val filteredSales: StateFlow<List<SaleReport>> = combine(
        _sales,
        _fromDate,
        _toDate
    ) { sales, fromDate, toDate ->
        sales.filter { sale ->
            val isAfterFromDate = fromDate == null || !sale.date.isBefore(fromDate)
            val isBeforeToDate = toDate == null || !sale.date.isAfter(toDate)
            isAfterFromDate && isBeforeToDate
        }.sortedByDescending { it.date } // Esta expresión es el valor de retorno
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- CORRECCIÓN 2: Acceder al primer elemento del array en el combine ---
    val chartData: StateFlow<List<ChartData>> = combine(
        filteredSales
    ) { (sales) -> // Desestructurar para obtener la lista directamente
        // Agrupar ventas por fecha y sumar totales
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
        loadReportSummary()
    }

    private fun loadSales() {
        viewModelScope.launch {
            _isLoading.value = true
            DummyDataRepository.getSalesReports().collect { salesList ->
                _sales.value = salesList
                _isLoading.value = false
            }
        }
    }

    private fun loadReportSummary() {
        viewModelScope.launch {
            val fromDateStr = _fromDate.value?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""
            val toDateStr = _toDate.value?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""

            DummyDataRepository.getReportSummary(fromDateStr, toDateStr).collect { summary ->
                _reportSummary.value = summary
            }
        }
    }

    fun updateFromDate(date: LocalDate?) {
        _fromDate.value = date
        loadReportSummary()
    }

    fun updateToDate(date: LocalDate?) {
        _toDate.value = date
        loadReportSummary()
    }

    fun clearDateFilters() {
        _fromDate.value = LocalDate.now().minusDays(30)
        _toDate.value = LocalDate.now()
        loadReportSummary()
    }
}