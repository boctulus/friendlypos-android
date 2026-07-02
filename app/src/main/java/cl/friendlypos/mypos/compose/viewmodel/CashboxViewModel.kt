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
import cl.friendlypos.mypos.api.HttpApiException
import cl.friendlypos.mypos.api.dto.CashboxAvailabilityItemDto
import cl.friendlypos.mypos.api.dto.CashboxSessionItemDto
import cl.friendlypos.mypos.api.dto.MovementTypeDto
import cl.friendlypos.mypos.db.AppDatabase
import cl.friendlypos.mypos.db.entity.PendingCashboxOperation
import cl.friendlypos.mypos.hardware.ScreenTicketOutput
import cl.friendlypos.mypos.model.Ticket
import cl.friendlypos.mypos.repository.CashboxRepository
import cl.friendlypos.mypos.tickets.TicketBuilders
import cl.friendlypos.mypos.tickets.TicketHtmlRenderer
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

    private val ticketOutput = ScreenTicketOutput()
    val ticketToShow: StateFlow<Ticket?> = ticketOutput.ticket

    private val _ticketHtml = MutableStateFlow<String?>(null)
    val ticketHtml: StateFlow<String?> = _ticketHtml.asStateFlow()

    private fun storeName(): String =
        SessionManager.get(getApplication())?.storeId?.takeIf { it.isNotBlank() } ?: "FriendlyPOS"

    fun clearTicket() {
        ticketOutput.clear()
        _ticketHtml.value = null
    }

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

    // Movement types
    private val _movementTypes = MutableStateFlow<List<MovementTypeDto>>(emptyList())
    val movementTypes: StateFlow<List<MovementTypeDto>> = _movementTypes.asStateFlow()

    private val _isLoadingMovementTypes = MutableStateFlow(false)
    val isLoadingMovementTypes: StateFlow<Boolean> = _isLoadingMovementTypes.asStateFlow()

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
                    val ticketData = repo.getOpeningTicketData(session.id).getOrNull()
                    if (ticketData != null) {
                        _ticketHtml.value = TicketHtmlRenderer.renderCashboxOpening(getApplication(), ticketData)
                    } else {
                        ticketOutput.present(TicketBuilders.cashboxOpen(session, storeName()))
                    }
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
                    val ticketData = repo.getCloseTicketData(sessionId).getOrNull()
                    if (ticketData != null) {
                        _ticketHtml.value = TicketHtmlRenderer.renderCashboxClose(getApplication(), ticketData)
                    } else {
                        ticketOutput.present(TicketBuilders.cashboxClose(session, finalAmount, notes, storeName()))
                    }
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                    if (error !is HttpApiException) {
                        savePendingClosure(sessionId, operationId, deviceId, finalAmount, notes)
                    }
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

    fun loadMovementTypes() {
        if (_movementTypes.value.isNotEmpty()) return
        viewModelScope.launch {
            _isLoadingMovementTypes.value = true
            repo.getMovementTypes()
                .onSuccess { _movementTypes.value = it }
                .onFailure { _errorMessage.value = it.message }
            _isLoadingMovementTypes.value = false
        }
    }

    fun registerMovement(
        sessionId: String,
        movementCode: String,
        amount: Double,
        description: String,
        paymentMethod: String = "cash",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repo.registerMovement(sessionId, movementCode, amount, description, paymentMethod)
                .onSuccess {
                    _successMessage.value = "Movimiento registrado exitosamente"
                    loadCurrentSession()
                    onSuccess()
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
