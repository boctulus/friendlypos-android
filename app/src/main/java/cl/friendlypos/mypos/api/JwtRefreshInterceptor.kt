package cl.friendlypos.mypos.api

import android.util.Log
import cl.friendlypos.mypos.api.dto.LoginResponseDto
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class JwtRefreshInterceptor : Interceptor {

    private val refreshLock = Any()
    private val gson = Gson()
    private val refreshClient by lazy { OkHttpClient() }

    companion object {
        private const val TAG = "JwtRefreshInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val response = chain.proceed(originalRequest)

        if (response.code != 401) return response

        val refreshToken = JwtTokenStorage.getRefreshToken()
        if (refreshToken == null) {
            return response
        }

        response.close()

        val refreshed = synchronized(refreshLock) {
            // Another thread may have already refreshed — check if the token changed
            val currentToken = JwtTokenStorage.getAccessToken()
            val sentToken = originalRequest.header("Authorization")?.removePrefix("Bearer ")
            if (currentToken != null && currentToken != sentToken) {
                true // Token was refreshed by another thread
            } else {
                tryRefresh(refreshToken)
            }
        }

        if (!refreshed) {
            // Re-execute to get a fresh 401 response body
            return chain.proceed(originalRequest)
        }

        val newToken = JwtTokenStorage.getAccessToken()
            ?: return chain.proceed(originalRequest)

        return chain.proceed(
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        )
    }

    private fun tryRefresh(refreshToken: String): Boolean {
        return try {
            val body = gson.toJson(mapOf("refresh_token" to refreshToken))
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}/api/auth/refresh")
                .post(body)
                .header("Accept", "application/json")
                .build()

            val response = refreshClient.newCall(request).execute()
            val responseBody = response.body?.string()
            response.close()

            if (!response.isSuccessful || responseBody == null) {
                Log.w(TAG, "Refresh failed: HTTP ${response.code}")
                return false
            }

            val dto = gson.fromJson(responseBody, LoginResponseDto::class.java)
            if (dto.success && dto.data != null) {
                JwtTokenStorage.saveTokens(dto.data.accessToken, dto.data.refreshToken)
                Log.d(TAG, "Token refreshed successfully")
                true
            } else {
                Log.w(TAG, "Refresh failed: ${dto.error}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Refresh exception: ${e.message}")
            false
        }
    }
}
