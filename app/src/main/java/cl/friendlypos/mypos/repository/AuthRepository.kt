package cl.friendlypos.mypos.repository

import cl.friendlypos.mypos.UserSession
import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.api.JwtTokenStorage
import cl.friendlypos.mypos.api.dto.LoginRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {

    suspend fun login(email: String, password: String): Result<UserSession> =
        withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.service.login(
                    LoginRequestDto(email = email.trim(), password = password)
                )

                if (!response.success || response.data == null) {
                    val msg = response.error ?: "Credenciales incorrectas o usuario sin permisos"
                    return@withContext Result.failure(Exception(msg))
                }

                val tokens = response.data
                JwtTokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)

                val claims = JwtTokenStorage.decodePayload()
                    ?: return@withContext Result.failure(Exception("Token de sesión inválido"))

                Result.success(
                    UserSession(
                        uid = claims.sub,
                        email = claims.email,
                        displayName = claims.email,
                        role = claims.role,
                        storeId = claims.storeId
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
