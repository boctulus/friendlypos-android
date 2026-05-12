package cl.friendlypos.mypos.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest

object FingerprintUtils {

    fun deviceFingerprintHash(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: ""
        val fingerprint = listOf(
            androidId,
            Build.BRAND,
            Build.MODEL,
            Build.MANUFACTURER,
            Build.HARDWARE
        ).joinToString("|")

        android.util.Log.d("FingerprintUtils", fingerprint)

        return sha256(fingerprint)
    }

    fun isEmulator(): Boolean {
        return (
            Build.FINGERPRINT.startsWith("generic")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("sdk_gphone")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HARDWARE.contains("ranchu")
        )
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
