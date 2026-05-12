package cl.friendlypos.mypos

import android.content.Context
import android.content.SharedPreferences
import cl.friendlypos.mypos.api.ApiClient

data class UserSession(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: String,
    val storeId: String?
)

object SessionManager {

    private const val PREFS = "UserSession"
    private const val PREFS_LOGIN = "LoginPrefs"
    private const val KEY_UID = "uid"
    private const val KEY_EMAIL = "email"
    private const val KEY_DISPLAY_NAME = "displayName"
    private const val KEY_ROLE = "role"
    private const val KEY_STORE_ID = "storeId"
    private const val KEY_LOGGED_IN = "loggedIn"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun loginPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE)

    fun saveLastEmail(context: Context, email: String) {
        loginPrefs(context).edit().putString("lastEmail", email).apply()
    }

    fun getLastEmail(context: Context): String =
        loginPrefs(context).getString("lastEmail", "") ?: ""

    fun save(context: Context, session: UserSession) {
        prefs(context).edit()
            .putString(KEY_UID, session.uid)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_ROLE, session.role)
            .putString(KEY_STORE_ID, session.storeId)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    fun get(context: Context): UserSession? {
        val p = prefs(context)
        if (!p.getBoolean(KEY_LOGGED_IN, false)) return null
        return UserSession(
            uid = p.getString(KEY_UID, "") ?: "",
            email = p.getString(KEY_EMAIL, "") ?: "",
            displayName = p.getString(KEY_DISPLAY_NAME, "") ?: "",
            role = p.getString(KEY_ROLE, "") ?: "",
            storeId = p.getString(KEY_STORE_ID, null)
        )
    }

    fun getRole(context: Context): String =
        prefs(context).getString(KEY_ROLE, "") ?: ""

    fun isLoggedIn(context: Context): Boolean =
        prefs(context).getBoolean(KEY_LOGGED_IN, false)

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
        ApiClient.cookieJar.clearAll()
    }
}
