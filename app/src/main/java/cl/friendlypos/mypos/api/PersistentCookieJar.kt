package cl.friendlypos.mypos.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class PersistentCookieJar(private val context: Context) : CookieJar {

    private val store = mutableMapOf<String, MutableList<Cookie>>()
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("CookiePrefs", Context.MODE_PRIVATE)
    }
    private val gson = Gson()

    init {
        loadFromPrefs()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val existing = store.getOrPut(host) { mutableListOf() }
        cookies.forEach { newCookie ->
            existing.removeAll { it.name == newCookie.name }
            existing.add(newCookie)
        }
        saveToPrefs()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val allCookies = mutableListOf<Cookie>()

        store.forEach { (storedHost, cookies) ->
            if (hostMatches(storedHost, host)) {
                val validCookies = cookies.filter { cookie ->
                    !cookie.persistent || cookie.expiresAt > System.currentTimeMillis()
                }
                allCookies.addAll(validCookies)
            }
        }

        return allCookies
    }

    private fun hostMatches(storedHost: String, requestHost: String): Boolean {
        if (storedHost == requestHost) return true
        
        val normalizedStored = if (storedHost.startsWith(".")) storedHost else ".$storedHost"
        val normalizedRequest = ".$requestHost"
        
        return normalizedRequest.endsWith(normalizedStored) || 
               normalizedStored.endsWith(normalizedRequest)
    }

    fun hasValidCookiesForHost(host: String): Boolean {
        val url = "https://$host/".toHttpUrlOrNull() ?: return false
        val cookies = loadForRequest(url)
        return cookies.isNotEmpty()
    }

    fun clearAll() {
        store.clear()
        prefs.edit().clear().apply()
    }

    private fun loadFromPrefs() {
        val json = prefs.getString("cookies", null) ?: return
        try {
            val type = object : TypeToken<List<SerializableCookie>>() {}.type
            val serializable: List<SerializableCookie> = gson.fromJson(json, type)
            store.clear()
            serializable.forEach { sc ->
                val cookie = sc.toOkHttpCookie()
                val host = sc.host ?: sc.domain ?: ApiConfig.BASE_URL.replace("https://", "")
                store.getOrPut(host) { mutableListOf() }.add(cookie)
            }
        } catch (e: Exception) {
            Log.w("PersistentCookieJar", "Failed to load cookies: ${e.message}")
        }
    }

    private fun saveToPrefs() {
        try {
            val serializable = mutableListOf<SerializableCookie>()
            store.forEach { (host, cookies) ->
                cookies.forEach { cookie ->
                    serializable.add(SerializableCookie.fromOkHttpCookie(cookie, host))
                }
            }
            val json = gson.toJson(serializable)
            prefs.edit().putString("cookies", json).apply()
        } catch (e: Exception) {
            Log.w("PersistentCookieJar", "Failed to save cookies: ${e.message}")
        }
    }

    data class SerializableCookie(
        val name: String,
        val value: String,
        val host: String? = null,
        val domain: String? = null,
        val path: String? = null,
        val expiresAt: Long? = null,
        val secure: Boolean = false,
        val httpOnly: Boolean = false,
        val persistent: Boolean = false,
        val hostOnly: Boolean = false
    ) {
        fun toOkHttpCookie(): Cookie {
            val builder = Cookie.Builder()
                .name(name)
                .value(value)

            domain?.let { 
                if (it.startsWith(".")) {
                    builder.domain(it.removePrefix("."))
                } else {
                    builder.domain(it)
                }
            }
            path?.let { builder.path(it) }
            expiresAt?.let { builder.expiresAt(it) }
            if (secure) builder.secure()
            if (httpOnly) builder.httpOnly()
            if (hostOnly && domain != null) builder.hostOnlyDomain(domain)

            return builder.build()
        }

        companion object {
            fun fromOkHttpCookie(cookie: Cookie, host: String): SerializableCookie {
                return SerializableCookie(
                    name = cookie.name,
                    value = cookie.value,
                    host = host,
                    domain = cookie.domain,
                    path = cookie.path,
                    expiresAt = if (cookie.persistent) cookie.expiresAt else null,
                    secure = cookie.secure,
                    httpOnly = cookie.httpOnly,
                    persistent = cookie.persistent,
                    hostOnly = cookie.hostOnly
                )
            }
        }
    }
}
