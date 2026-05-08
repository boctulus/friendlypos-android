package cl.friendlypos.mypos.api

import cl.friendlypos.mypos.BuildConfig

object ApiConfig {
    val BASE_URL: String get() = BuildConfig.BASE_URL_BACKEND
    val FIREBASE_API_KEY: String get() = BuildConfig.FIREBASE_API_KEY
}
