package cl.friendlypos.mypos.api

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class SessionCookieJar : CookieJar {
    private val store = mutableMapOf<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val existing = store.getOrPut(host) { mutableListOf() }
        cookies.forEach { newCookie ->
            existing.removeAll { it.name == newCookie.name }
            existing.add(newCookie)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return store[url.host]?.filter { cookie ->
            !cookie.persistent || cookie.expiresAt > System.currentTimeMillis()
        } ?: emptyList()
    }

    fun clearAll() = store.clear()
}
