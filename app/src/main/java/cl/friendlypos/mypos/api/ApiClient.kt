package cl.friendlypos.mypos.api

import android.annotation.SuppressLint
import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    @SuppressLint("StaticFieldLeak")
    @Volatile
    private var _cookieJar: PersistentCookieJar? = null

    private var _apiHttpClient: OkHttpClient? = null
    private var _service: ApiService? = null

    private val lock = Any()

    fun init(context: Context) {
        if (_cookieJar != null) return

        synchronized(lock) {
            if (_cookieJar != null) return

            val appContext = context.applicationContext
            _cookieJar = PersistentCookieJar(appContext)
            JwtTokenStorage.init(appContext)

            _apiHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(JwtAuthInterceptor())
                .addInterceptor(JwtRefreshInterceptor())
                .addInterceptor(DebugHttpInterceptor())
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()

            _service = Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL + "/")
                .client(_apiHttpClient!!)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }

    val cookieJar: PersistentCookieJar
        get() = _cookieJar ?: throw IllegalStateException("ApiClient not initialized. Call init(context) first.")

    val service: ApiService
        get() = _service ?: throw IllegalStateException("ApiClient not initialized. Call init(context) first.")

    fun hasValidSession(): Boolean {
        return try {
            JwtTokenStorage.isAccessTokenValid()
        } catch (e: Exception) {
            false
        }
    }
}
