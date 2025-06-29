package cl.friendlypos.mypos.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.friendlypos.mypos.data.DummyDataRepository
import cl.friendlypos.mypos.model.Notification
import cl.friendlypos.mypos.model.NotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.time.LocalDate

class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _fromDate = MutableStateFlow<LocalDate?>(null)
    val fromDate: StateFlow<LocalDate?> = _fromDate.asStateFlow()

    private val _toDate = MutableStateFlow<LocalDate?>(null)
    val toDate: StateFlow<LocalDate?> = _toDate.asStateFlow()

    private val _selectedType = MutableStateFlow<NotificationType?>(null)
    val selectedType: StateFlow<NotificationType?> = _selectedType.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filteredNotifications: StateFlow<List<Notification>> = combine(
        _notifications,
        _fromDate,
        _toDate,
        _selectedType
    ) { notifications, fromDate, toDate, selectedType ->
        var filtered = notifications

        // Filtrar por fechas
        if (fromDate != null) {
            filtered = filtered.filter { notification ->
                notification.timestamp.toLocalDate() >= fromDate
            }
        }

        if (toDate != null) {
            filtered = filtered.filter { notification ->
                notification.timestamp.toLocalDate() <= toDate
            }
        }

        // Filtrar por tipo/categoría
        if (selectedType != null) {
            filtered = filtered.filter { notification ->
                notification.type == selectedType
            }
        }

        // Ordenar por fecha más reciente primero
        filtered.sortedByDescending { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            DummyDataRepository.getNotifications().collect { notificationList ->
                _notifications.value = notificationList
                _isLoading.value = false
            }
        }
    }

    fun updateFromDate(date: LocalDate?) {
        _fromDate.value = date
    }

    fun updateToDate(date: LocalDate?) {
        _toDate.value = date
    }

    fun updateSelectedType(type: NotificationType?) {
        _selectedType.value = type
    }

    fun clearDateFilters() {
        _fromDate.value = null
        _toDate.value = null
    }

    fun clearAllFilters() {
        _fromDate.value = null
        _toDate.value = null
        _selectedType.value = null
    }

    fun markAsRead(notificationId: String) {
        _notifications.value = _notifications.value.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }
    }
}