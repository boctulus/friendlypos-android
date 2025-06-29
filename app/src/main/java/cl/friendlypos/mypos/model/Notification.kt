package cl.friendlypos.mypos.model

import java.time.LocalDateTime

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: LocalDateTime,
    val isRead: Boolean = false
)

enum class NotificationType {
    INFO, WARNING, ERROR, SUCCESS
}