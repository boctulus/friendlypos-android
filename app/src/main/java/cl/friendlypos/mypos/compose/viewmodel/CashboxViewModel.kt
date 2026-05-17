package cl.friendlypos.mypos.compose.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import cl.friendlypos.mypos.SessionManager
import cl.friendlypos.mypos.api.dto.CashboxAvailabilityItemDto
import cl.friendlypos.mypos.api.dto.CashboxSessionItemDto
import cl.friendlypos.mypos.db.AppDatabase
import cl.friendlypos.mypos.db.entity.PendingCashboxOperation
import cl.friendlypos.mypos.repository.CashboxRepository
import cl.friendlypos.mypos.utils.DeviceIdProvider
import cl.friendlypos.mypos.work.PendingClosureWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

class CashboxViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = CashboxRepository()

    // Device scope
    private val _currentSession = MutableStateFlow<CashboxSessionItemDto?>(null)
    val currentSession: StateFlow<CashboxSessionItemDto?> = _currentSession.asStateFlow()

    private val _hasInitialLoadCompleted = MutableStateFlow(false)
    val hasInitialLoadCompleted: StateFlow<Boolean> = _hasInitialLoadCompleted.asStateFlow()

    // Store scope
    private val _availability = MutableStateFlow<List<CashboxAvailabilityItemDto>>(emptyList())
    val availability: StateFlow<List<CashboxAvailabilityItemDto>> = _availability.asStateFlow()

    private val _isLoadingAvailability = MutableStateFlow(false)
    val isLoadingAvailability: StateFlow<Boolean> = _isLoadingAvailability.asStateFlow()

    // Common
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
                // background refresh — no mostrar error al usuario
            _isLoading.value = false
            _hasInitialLoadCompleted.value = true
        }
    }

    fun loadAvailability(storeId: String) {
        viewModelScope.launch {
            _isLoadingAvailability.value = true
            repo.getCashboxAvailability(storeId)
                .onSuccess { _availability.value = it }
                .onFailure { /* non-critical: UI shows from prior state */ }
            _isLoadingAvailability.value = false
        }
    }

    fun openSession(storeId: String, cashboxId: String, cashboxLabel: String, initialAmount: Double, notes: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val operationId = UUID.randomUUID().toString()
            val deviceId = DeviceIdProvider.getDeviceId(getApplication())
            repo.openSession(storeId, cashboxId, cashboxLabel, initialAmount, notes, deviceId, operationId)
                .onSuccess { session ->
                    _currentSession.value = session
                    _successMessage.value = "Caja abierta exitosamente"
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                    // El backend puede rechazar con 401/409 si el terminal ya tiene caja abierta.
                    // Refrescar currentSession: si existe, limpiar error y rutear a cierre.
                    repo.getCurrentSession()
                        .onSuccess { session ->
                            if (session?.status == "open") {
                                _currentSession.value = session
                                _errorMessage.value = null
                            }
                        }
                }
            _isLoading.value = false
        }
    }

    fun closeSession(sessionId: String, finalAmount: Double, notes: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val operationId = UUID.randomUUID().toString()
            val deviceId = DeviceIdProvider.getDeviceId(getApplication())
            repo.closeSession(sessionId, finalAmount, notes, deviceId, operationId)
                .onSuccess { session ->
                    if (sessionId == _currentSession.value?.id) {
                        _currentSession.value = session
                    }
                    _availability.value = _availability.value.map { item ->
                        if (item.sessionId == sessionId)
                            item.copy(status = "available", sessionId = null, cashierName = null)
                        else item
                    }
                    _successMessage.value = "Caja cerrada exitosamente"
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                    savePendingClosure(sessionId, operationId, deviceId, finalAmount, notes)
                }
            _isLoading.value = false
        }
    }

    private suspend fun savePendingClosure(
        sessionId: String,
        operationId: String,
        deviceId: String,
        finalAmount: Double,
        notes: String?
    ) {
        val context = getApplication<Application>()
        val cashierId = SessionManager.get(context)?.uid ?: return
        val db = AppDatabase.getInstance(context)
        db.pendingCashboxOperationsDao().insert(
            PendingCashboxOperation(
                sessionId = sessionId,
                operationId = operationId,
                cashierId = cashierId,
                deviceId = deviceId,
                finalAmount = finalAmount,
                notes = notes,
                attemptedAt = System.currentTimeMillis()
            )
        )
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<PendingClosureWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
        )
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
