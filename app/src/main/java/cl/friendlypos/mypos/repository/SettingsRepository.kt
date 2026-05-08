package cl.friendlypos.mypos.repository

import cl.friendlypos.mypos.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepository {

    suspend fun resetUserPassword(email: String, newPassword: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // Step 1: find user by email to get uid
                val listResponse = ApiClient.service.listUsers(query = email, limit = 1)
                val user = listResponse.data?.docs?.firstOrNull { it.email == email }
                    ?: return@withContext Result.failure(Exception("Usuario con email '$email' no encontrado"))

                // Step 2: update password
                val updateResponse = ApiClient.service.updateUser(
                    uid = user.uid,
                    body = mapOf("password" to newPassword)
                )
                if (updateResponse.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Error al actualizar la contraseña"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
