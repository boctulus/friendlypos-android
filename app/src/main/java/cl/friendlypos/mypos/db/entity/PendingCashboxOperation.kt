package cl.friendlypos.mypos.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_cashbox_operations")
data class PendingCashboxOperation(
    @PrimaryKey val sessionId: String,
    val operationId: String,
    val cashierId: String,
    val deviceId: String,
    val finalAmount: Double,
    val notes: String?,
    val attemptedAt: Long,
    val retryCount: Int = 0,
    val status: String = "pending"
)
