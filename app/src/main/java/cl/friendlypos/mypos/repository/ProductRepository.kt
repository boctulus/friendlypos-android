package cl.friendlypos.mypos.repository

import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ProductPage(
    val products: List<Product>,
    val hasMore: Boolean,
    val nextCursor: String?
)

class ProductRepository {

    suspend fun getPage(cursor: String? = null, limit: Int = 50): Result<ProductPage> =
        withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.service.getProducts(cursor = cursor, limit = limit)
                if (response.success) {
                    Result.success(
                        ProductPage(
                            products = response.products?.map { it.toProduct() } ?: emptyList(),
                            hasMore = response.hasMore ?: false,
                            nextCursor = response.nextCursor
                        )
                    )
                } else {
                    Result.failure(Exception("Error al cargar productos"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun searchQuick(query: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.searchProductsQuick(query)
            if (response.success) {
                Result.success(response.products?.map { it.toProduct() } ?: emptyList())
            } else {
                Result.failure(Exception("Error al buscar productos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
