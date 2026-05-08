package cl.friendlypos.mypos.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.api.dto.CashboxItemDto
import cl.friendlypos.mypos.api.dto.CashboxSessionItemDto
import cl.friendlypos.mypos.api.dto.StoreDto
import cl.friendlypos.mypos.repository.CashboxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CashboxViewModel : ViewModel() {

    private val repo = CashboxRepository()

    private val _currentSession = MutableStateFlow<CashboxSessionItemDto?>(null)
    val currentSession: StateFlow<CashboxSessionItemDto?> = _currentSession.asStateFlow()

    private val _stores = MutableStateFlow<List<StoreDto>>(emptyList())
    val stores: StateFlow<List<StoreDto>> = _stores.asStateFlow()

    private val _cashboxes = MutableStateFlow<List<CashboxItemDto>>(emptyList())
    val cashboxes: StateFlow<List<CashboxItemDto>> = _cashboxes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadCurrentSession()
    }

    fun loadCurrentSession() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getCurrentSession()
                .onSuccess { _currentSession.value = it }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun loadStores() {
        viewModelScope.launch {
            repo.getUserStores()
                .onSuccess { _stores.value = it }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun loadCashboxesForStore(storeId: String) {
        viewModelScope.launch {
            repo.getActiveCashboxes(storeId)
                .onSuccess { _cashboxes.value = it }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun openSession(storeId: String, cashboxNumber: Int, initialAmount: Double, notes: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repo.openSession(storeId, cashboxNumber, initialAmount, notes)
                .onSuccess { session ->
                    _currentSession.value = session
                    _successMessage.value = "Caja abierta exitosamente"
                }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun closeSession(sessionId: String, finalAmount: Double, notes: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repo.closeSession(sessionId, finalAmount, notes)
                .onSuccess { session ->
                    _currentSession.value = session
                    _successMessage.value = "Caja cerrada exitosamente"
                }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
