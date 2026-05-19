package cl.friendlypos.mypos

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class FriendlyPOSApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
