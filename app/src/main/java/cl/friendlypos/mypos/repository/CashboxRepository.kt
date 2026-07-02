package cl.friendlypos.mypos.repository

import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.api.dto.CashboxAvailabilityItemDto
import cl.friendlypos.mypos.api.dto.CashboxSessionItemDto
import cl.friendlypos.mypos.api.dto.CloseSessionRequestDto
import cl.friendlypos.mypos.api.HttpApiException
import cl.friendlypos.mypos.api.dto.MovementItemDto
import cl.friendlypos.mypos.api.dto.MovementTypeDto
import cl.friendlypos.mypos.api.dto.OpenSessionRequestDto
import cl.friendlypos.mypos.api.dto.RegisterMovementRequestDto
import cl.friendlypos.mypos.api.dto.StoreDto
import cl.friendlypos.mypos.api.dto.TicketDataResponseDto
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
        cashboxLabel: String,
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
                    cashboxLabel = cashboxLabel,
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

    suspend fun getMovementTypes(): Result<List<MovementTypeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.getMovementTypes()
            if (response.success && response.movementTypes != null) {
                Result.success(response.movementTypes)
            } else {
                Result.failure(Exception("Error al obtener tipos de movimiento"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerMovement(
        sessionId: String,
        movementCode: String,
        amount: Double,
        description: String,
        paymentMethod: String = "cash"
    ): Result<MovementItemDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.registerMovement(
                RegisterMovementRequestDto(
                    cashboxSessionId = sessionId,
                    movementCode = movementCode,
                    amount = amount,
                    description = description,
                    paymentMethod = paymentMethod
                )
            )
            if (response.success && response.movement != null) {
                Result.success(response.movement)
            } else {
                Result.failure(Exception(response.error ?: response.message ?: "Error al registrar movimiento"))
            }
        } catch (e: HttpException) {
            val msg = parseHttpErrorBody(e) ?: "Error al registrar movimiento (HTTP ${e.code()})"
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
        } catch (e: HttpException) {
            val msg = parseHttpErrorBody(e) ?: "Error al cerrar la caja (HTTP ${e.code()})"
            Result.failure(HttpApiException(msg, e.code()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOpeningTicketData(sessionId: String): Result<TicketDataResponseDto> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = ApiClient.service.getCashboxOpeningTicketData(sessionId)
                if (response.data == null) throw Exception("No ticket data")
                response
            }
        }

    suspend fun getCloseTicketData(sessionId: String): Result<TicketDataResponseDto> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = ApiClient.service.getCashboxCloseTicketData(sessionId)
                if (response.data == null) throw Exception("No ticket data")
                response
            }
        }
}
