package cl.friendlypos.mypos.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cl.friendlypos.mypos.SessionManager
import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.api.dto.CloseSessionRequestDto
import cl.friendlypos.mypos.db.AppDatabase
import cl.friendlypos.mypos.utils.DeviceIdProvider
import retrofit2.HttpException

class PendingClosureWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!SessionManager.isLoggedIn(applicationContext)) {
            // Not authenticated — skip retry; LoginActivity handles recovery on next login
            return Result.success()
        }

        ApiClient.init(applicationContext)

        val deviceId = DeviceIdProvider.getDeviceId(applicationContext)
        val db = AppDatabase.getInstance(applicationContext)
        val pending = db.pendingCashboxOperationsDao().getAllPendingByDevice(deviceId)

        if (pending.isEmpty()) return Result.success()

        for (op in pending) {
            try {
                val response = ApiClient.service.closeCashboxSession(
                    sessionId = op.sessionId,
                    request = CloseSessionRequestDto(
                        finalAmount = op.finalAmount,
                        notes = op.notes,
                        operationId = op.operationId,
                        deviceId = op.deviceId
                    )
                )
                if (response.success) {
                    db.pendingCashboxOperationsDao().deleteBySessionId(op.sessionId)
                } else {
                    db.pendingCashboxOperationsDao().updateRetryCount(op.sessionId, op.retryCount + 1)
                    return Result.retry()
                }
            } catch (e: HttpException) {
                if (e.code() in listOf(403, 404)) {
                    db.pendingCashboxOperationsDao().updateStatus(op.sessionId, "failed_permanent")
                    return Result.failure()
                }
                db.pendingCashboxOperationsDao().updateRetryCount(op.sessionId, op.retryCount + 1)
                return Result.retry()
            } catch (e: Exception) {
                db.pendingCashboxOperationsDao().updateRetryCount(op.sessionId, op.retryCount + 1)
                return Result.retry()
            }
        }

        return Result.success()
    }
}
