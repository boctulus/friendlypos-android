package cl.friendlypos.mypos.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.friendlypos.mypos.db.entity.PendingCashboxOperation

@Dao
interface PendingCashboxOperationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(op: PendingCashboxOperation)

    @Query("SELECT * FROM pending_cashbox_operations WHERE deviceId = :deviceId AND cashierId = :cashierId AND status = 'pending' LIMIT 1")
    suspend fun getByDeviceAndCashier(deviceId: String, cashierId: String): PendingCashboxOperation?

    @Query("SELECT * FROM pending_cashbox_operations WHERE deviceId = :deviceId AND status = 'pending'")
    suspend fun getAllPendingByDevice(deviceId: String): List<PendingCashboxOperation>

    @Query("DELETE FROM pending_cashbox_operations WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: String)

    @Query("UPDATE pending_cashbox_operations SET retryCount = :count WHERE sessionId = :sessionId")
    suspend fun updateRetryCount(sessionId: String, count: Int)

    @Query("UPDATE pending_cashbox_operations SET status = :status WHERE sessionId = :sessionId")
    suspend fun updateStatus(sessionId: String, status: String)
}
