package cl.friendlypos.mypos.hardware

import cl.friendlypos.mypos.model.Ticket

interface IPrinter {
    fun initialize(): Boolean
    fun printTicket(ticket: Ticket): Result<Unit>
    fun printHtml(html: String): Result<Unit>
}
