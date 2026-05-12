package cl.friendlypos.mypos.repository

import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.model.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CustomerPage(
    val customers: List<Customer>,
    val hasMore: Boolean,
    val nextCursor: String?
)

class CustomerRepository {

    suspend fun getPage(cursor: String? = null, limit: Int = 50): Result<CustomerPage> =
        withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.service.getCustomers(startAfterDocId = cursor, limit = limit)
                if (response.success) {
                    val docs = response.data?.docs?.map { it.toCustomer() } ?: emptyList()
                    val pagination = response.data?.pagination
                    Result.success(
                        CustomerPage(
                            customers = docs,
                            hasMore = pagination?.hasMore ?: false,
                            nextCursor = pagination?.nextCursor
                        )
                    )
                } else {
                    Result.failure(Exception("Error al cargar clientes"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
