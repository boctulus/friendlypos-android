package cl.friendlypos.mypos.repository

import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository {

    suspend fun getAll(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.getProducts()
            if (response.success) {
                Result.success(response.products?.map { it.toProduct() } ?: emptyList())
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
                val products = response.products?.map { it.toProduct() } ?: emptyList()
                Result.success(products)
            } else {
                Result.failure(Exception("Error al buscar productos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
