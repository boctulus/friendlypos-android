package cl.friendlypos.mypos.utils

import android.os.Build

class SystemUtils {
    companion object {
        private val supportedAbis = Build.SUPPORTED_ABIS

        @JvmStatic
        fun isEmulator(): Boolean {
            return (
            supportedAbis.any { it.startsWith("x86") } ||
                    Build.PRODUCT == "google_sdk" ||
                    Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.lowercase().contains("emulator") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    (
                        Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                    )
            )
        }
    }
}
