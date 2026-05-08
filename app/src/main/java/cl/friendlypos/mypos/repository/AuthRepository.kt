package cl.friendlypos.mypos.repository

import cl.friendlypos.mypos.UserSession
import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.api.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AuthRepository {

    suspend fun login(email: String, password: String): Result<UserSession> =
        withContext(Dispatchers.IO) {
            try {
                val jsonBody = """{"email":"${email.trim()}","password":"$password"}"""
                val request = Request.Builder()
                    .url("${ApiConfig.BASE_URL}/auth/login")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .header("Accept", "application/json")
                    .build()

                // Execute login — expect 302; cookie is captured by CookieJar
                val response = ApiClient.loginClient.newCall(request).execute()
                response.close()

                val location = response.header("Location", "") ?: ""

                // Backend redirects to /auth/login on failure, /dashboard* on success
                if (location.contains("/auth/login")) {
                    return@withContext Result.failure(Exception("Credenciales incorrectas o usuario sin permisos"))
                }

                // Fetch user session info
                val keepAlive = ApiClient.service.getSessionKeepAlive()
                if (!keepAlive.success || keepAlive.user == null) {
                    return@withContext Result.failure(Exception("No se pudo obtener la sesión del usuario"))
                }

                val user = keepAlive.user
                Result.success(
                    UserSession(
                        uid = user.uid,
                        email = user.email ?: email.trim(),
                        displayName = user.displayName ?: user.email ?: email.trim(),
                        role = user.role,
                        storeId = user.storeId
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
