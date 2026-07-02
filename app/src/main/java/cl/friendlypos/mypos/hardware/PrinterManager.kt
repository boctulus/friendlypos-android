package cl.friendlypos.mypos.hardware

import android.content.Context
import android.util.Log
import cl.friendlypos.mypos.model.Sale

class PrinterManager(private val context: Context) : IPrinter {

    override fun initialize(): Boolean {
        Log.d("PrinterManager", "Stub: impresora no disponible (sin SDK)")
        return false
    }

    override fun printReceipt(sale: Sale) {
        Log.d("PrinterManager", "Stub: impresión no disponible (sin SDK)")
    }
}
