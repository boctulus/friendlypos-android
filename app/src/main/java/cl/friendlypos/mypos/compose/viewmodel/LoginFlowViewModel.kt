package cl.friendlypos.mypos.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.UserSession
import cl.friendlypos.mypos.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginFlowViewModel : ViewModel() {

    sealed class FlowState {
        object Login : FlowState()
        data class Done(val session: UserSession) : FlowState()
    }

    private val _state = MutableStateFlow<FlowState>(FlowState.Login)
    val state: StateFlow<FlowState> = _state.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val authRepo = AuthRepository()

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
                    _state.value = FlowState.Done(session)
                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Error al iniciar sesión"
                }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
