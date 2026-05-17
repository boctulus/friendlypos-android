package cl.friendlypos.mypos.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import cl.friendlypos.mypos.api.dto.JwtPayload
import com.google.gson.Gson

object JwtTokenStorage {

    private const val PREFS = "JwtTokens"
    private const val KEY_ACCESS_TOKEN = "accessToken"
    private const val KEY_REFRESH_TOKEN = "refreshToken"
    private const val TAG = "JwtTokenStorage"

    @Volatile private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    private fun prefs(): SharedPreferences =
        requireNotNull(appContext) { "JwtTokenStorage not initialized" }
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs().edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? = prefs().getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs().getString(KEY_REFRESH_TOKEN, null)

    fun isAccessTokenValid(): Boolean {
        val payload = decodePayload() ?: return false
        val bufferSeconds = 60L
        return System.currentTimeMillis() / 1000 < payload.exp - bufferSeconds
    }

    fun decodePayload(): JwtPayload? {
        val token = getAccessToken() ?: return null
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING)
            val payloadJson = String(payloadBytes, Charsets.UTF_8)
            Gson().fromJson(payloadJson, JwtPayload::class.java)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode JWT payload: ${e.message}")
            null
        }
    }

    fun clear() {
        prefs().edit().clear().apply()
    }
}
