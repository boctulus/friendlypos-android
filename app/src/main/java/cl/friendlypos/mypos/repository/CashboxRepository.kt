package cl.friendlypos.mypos.repository

import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.api.dto.CashboxItemDto
import cl.friendlypos.mypos.api.dto.CashboxSessionItemDto
import cl.friendlypos.mypos.api.dto.CloseSessionRequestDto
import cl.friendlypos.mypos.api.dto.OpenSessionRequestDto
import cl.friendlypos.mypos.api.dto.StoreDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun getActiveCashboxes(storeId: String): Result<List<CashboxItemDto>> =
        withContext(Dispatchers.IO) {
            try {
                val filter = "store_id:$storeId,status:active"
                val response = ApiClient.service.getCashboxes(filter)
                if (response.success && response.data?.docs != null) {
                    Result.success(response.data.docs)
                } else {
                    Result.failure(Exception("No hay cajas activas para esta tienda"))
                }
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
        cashboxNumber: Int,
        initialAmount: Double,
        notes: String? = null
    ): Result<CashboxSessionItemDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.openCashboxSession(
                OpenSessionRequestDto(
                    storeId = storeId,
                    cashboxNumber = cashboxNumber,
                    initialAmount = initialAmount,
                    notes = notes
                )
            )
            if (response.success && response.session != null) {
                Result.success(response.session)
            } else {
                Result.failure(Exception(response.error ?: "Error al abrir la caja"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closeSession(
        sessionId: String,
        finalAmount: Double,
        notes: String? = null
    ): Result<CashboxSessionItemDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.closeCashboxSession(
                sessionId = sessionId,
                request = CloseSessionRequestDto(finalAmount = finalAmount, notes = notes)
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
