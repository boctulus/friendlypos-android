package cl.friendlypos.mypos.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.UserSession
import cl.friendlypos.mypos.repository.AuthRepository
import cl.friendlypos.mypos.repository.CashboxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginFlowViewModel : ViewModel() {

    sealed class FlowState {
        object Login : FlowState()
        data class CashboxOpen(val storeId: String) : FlowState()
        data class Done(val session: UserSession) : FlowState()
    }

    private val _state = MutableStateFlow<FlowState>(FlowState.Login)
    val state: StateFlow<FlowState> = _state.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val authRepo = AuthRepository()
    private val cashboxRepo = CashboxRepository()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email y contraseña son requeridos"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authRepo.login(email.trim(), password)
                .onSuccess { session ->
                    if (session.role == "cashier") {
                        checkCashboxAndProceed(session)
                    } else {
                        _state.value = FlowState.Done(session)
                    }
                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Error al iniciar sesión"
                }
            _isLoading.value = false
        }
    }

    private suspend fun checkCashboxAndProceed(session: UserSession) {
        cashboxRepo.getCurrentSession()
            .onSuccess { currentSession ->
                if (currentSession != null) {
                    _state.value = FlowState.Done(session)
                } else {
                    val storeId = session.storeId ?: ""
                    _state.value = FlowState.CashboxOpen(storeId)
                }
            }
            .onFailure {
                val storeId = session.storeId ?: ""
                _state.value = FlowState.CashboxOpen(storeId)
            }
    }

    fun onCashboxOpened(session: UserSession) {
        _state.value = FlowState.Done(session)
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
