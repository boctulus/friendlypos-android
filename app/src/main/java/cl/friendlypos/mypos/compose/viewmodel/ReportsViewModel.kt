package cl.friendlypos.mypos.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.data.DummyDataRepository
import cl.friendlypos.mypos.model.ReportSummary
import cl.friendlypos.mypos.model.SaleReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReportsViewModel : ViewModel() {
    
    private val _reportSummary = MutableStateFlow<ReportSummary?>(null)
    val reportSummary: StateFlow<ReportSummary?> = _reportSummary.asStateFlow()
    
    private val _salesReports = MutableStateFlow<List<SaleReport>>(emptyList())
    val salesReports: StateFlow<List<SaleReport>> = _salesReports.asStateFlow()
    
    private val _fromDate = MutableStateFlow("")
    val fromDate: StateFlow<String> = _fromDate.asStateFlow()
    
    private val _toDate = MutableStateFlow("")
    val toDate: StateFlow<String> = _toDate.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        // Set default dates (last 30 days)
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        _toDate.value = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, -30)
        _fromDate.value = dateFormat.format(calendar.time)
        
        loadReports()
    }
    
    fun updateDateRange(from: String, to: String) {
        _fromDate.value = from
        _toDate.value = to
        loadReports()
    }
    
    private fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            
            DummyDataRepository.getReportSummary(_fromDate.value, _toDate.value).collect { summary ->
                _reportSummary.value = summary
            }
            
            DummyDataRepository.getSalesReports().collect { reports ->
                _salesReports.value = reports
                _isLoading.value = false
            }
        }
    }
}