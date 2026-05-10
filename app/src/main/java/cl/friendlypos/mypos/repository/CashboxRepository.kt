package cl.friendlypos.mypos.repository

import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.api.dto.CashboxAvailabilityItemDto
import cl.friendlypos.mypos.api.dto.CashboxSessionItemDto
import cl.friendlypos.mypos.api.dto.CloseSessionRequestDto
import cl.friendlypos.mypos.api.dto.OpenSessionRequestDto
import cl.friendlypos.mypos.api.dto.StoreDto
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class CashboxRepository {

    suspend fun getUserStores(): Result<List<StoreDto>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.getUserStores()
            if (response.success && response.stores != null) {
                Result.success(response.stores)
            } else {
                Result.failure(Exception("Error al obtener tiendas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCashboxAvailability(storeId: String): Result<List<CashboxAvailabilityItemDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.service.getCashboxAvailability(storeId)
                Result.success(response.availability ?: emptyList())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getCurrentSession(): Result<CashboxSessionItemDto?> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.getCurrentCashboxSession()
            Result.success(response.session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun openSession(
        storeId: String,
        cashboxId: String,
        initialAmount: Double,
        notes: String? = null,
        deviceId: String? = null,
        operationId: String? = null
    ): Result<CashboxSessionItemDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.openCashboxSession(
                OpenSessionRequestDto(
                    storeId = storeId,
                    cashboxId = cashboxId,
                    initialAmount = initialAmount,
                    notes = notes,
                    operationId = operationId,
                    deviceId = deviceId
                )
            )
            if (response.success && response.session != null) {
                Result.success(response.session)
            } else {
                Result.failure(Exception(response.error ?: response.message ?: "Error al abrir la caja"))
            }
        } catch (e: HttpException) {
            val msg = parseHttpErrorBody(e) ?: "Error al abrir la caja (HTTP ${e.code()})"
            Result.failure(Exception(msg))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseHttpErrorBody(e: HttpException): String? {
        return try {
            val body = e.response()?.errorBody()?.string() ?: return null
            val json = Gson().fromJson(body, JsonObject::class.java)
            json?.get("error")?.asString ?: json?.get("message")?.asString
        } catch (_: Exception) {
            null
        }
    }

    suspend fun closeSession(
        sessionId: String,
        finalAmount: Double,
        notes: String? = null,
        deviceId: String? = null,
        operationId: String? = null
    ): Result<CashboxSessionItemDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.closeCashboxSession(
                sessionId = sessionId,
                request = CloseSessionRequestDto(
                    finalAmount = finalAmount,
                    notes = notes,
                    operationId = operationId,
                    deviceId = deviceId
                )
            )
            if (response.success && response.session != null) {
                Result.success(response.session)
            } else {
                Result.failure(Exception(response.error ?: "Error al cerrar la caja"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
