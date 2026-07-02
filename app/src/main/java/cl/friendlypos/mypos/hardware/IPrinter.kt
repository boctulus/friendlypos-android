package cl.friendlypos.mypos.hardware

import cl.friendlypos.mypos.model.Sale

interface IPrinter {
    fun initialize(): Boolean
    fun printReceipt(sale: Sale)
}
