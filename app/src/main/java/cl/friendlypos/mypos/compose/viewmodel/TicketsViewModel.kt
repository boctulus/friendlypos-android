package cl.friendlypos.mypos.compose.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.api.dto.SaleDto
import cl.friendlypos.mypos.model.Ticket
import cl.friendlypos.mypos.repository.SaleRepository
import cl.friendlypos.mypos.tickets.TicketBuilders
import cl.friendlypos.mypos.tickets.TicketHtmlRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TicketsViewModel(application: Application) : AndroidViewModel(application) {

    private val saleRepo = SaleRepository()

    private val _sales = MutableStateFlow<List<SaleDto>>(emptyList())
    val sales: StateFlow<List<SaleDto>> = _sales.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedTicket = MutableStateFlow<Ticket?>(null)
    val selectedTicket: StateFlow<Ticket?> = _selectedTicket.asStateFlow()

    private val _selectedTicketHtml = MutableStateFlow<String?>(null)
    val selectedTicketHtml: StateFlow<String?> = _selectedTicketHtml.asStateFlow()

    private val _isLoadingTicket = MutableStateFlow(false)
    val isLoadingTicket: StateFlow<Boolean> = _isLoadingTicket.asStateFlow()

    init {
        loadSales()
    }

    fun loadSales() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            runCatching { ApiClient.service.getSales(limit = 100) }
                .onSuccess { response ->
                    _sales.value = if (response.success) response.data ?: emptyList() else emptyList()
                }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun loadSaleTicket(sale: SaleDto, storeName: String) {
        viewModelScope.launch {
            _isLoadingTicket.value = true
            val response = saleRepo.getSaleTicketData(sale.id).getOrNull()
            if (response != null) {
                _selectedTicketHtml.value = TicketHtmlRenderer.render(getApplication(), response)
            } else {
                _selectedTicket.value = TicketBuilders.fromSaleDto(sale, storeName)
            }
            _isLoadingTicket.value = false
        }
    }

    fun clearSelectedTicket() {
        _selectedTicket.value = null
        _selectedTicketHtml.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
