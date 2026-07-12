package cl.friendlypos.mypos.hardware

import android.graphics.Bitmap
import cl.friendlypos.mypos.model.Ticket

interface IPrinter {
    fun initialize(): Boolean
    fun printTicket(ticket: Ticket): Result<Unit>
    fun printBitmap(bitmap: Bitmap): Result<Unit>
}
