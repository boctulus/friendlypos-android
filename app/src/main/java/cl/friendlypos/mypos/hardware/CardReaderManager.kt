package cl.friendlypos.mypos.hardware

import android.content.Context
import android.util.Log

class CardReaderManager(private val context: Context) : ICardReader {

    override fun initialize(): Boolean {
        Log.d("CardReaderManager", "Stub: lector de tarjetas no disponible (sin SDK)")
        return false
    }

    override fun readCard(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        Log.d("CardReaderManager", "Stub: lectura de tarjeta no disponible (sin SDK)")
        onError("SDK no disponible")
    }
}
