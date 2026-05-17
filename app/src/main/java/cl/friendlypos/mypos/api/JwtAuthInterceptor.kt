package cl.friendlypos.mypos.api

import okhttp3.Interceptor
import okhttp3.Response

class JwtAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = JwtTokenStorage.getAccessToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
