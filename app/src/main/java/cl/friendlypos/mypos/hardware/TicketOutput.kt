package cl.friendlypos.mypos.hardware

import cl.friendlypos.mypos.model.Ticket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Salida de un ticket. Abstracción que desacopla la generación del ticket de su destino.
 *
 * - Etapa 1: [ScreenTicketOutput] expone el ticket vía StateFlow para mostrarlo en pantalla.
 * - Etapa 2: una impl tipo `EscPosTicketOutput` / SDK del terminal imprimirá físicamente,
 *   sin tocar los flujos que generan el ticket.
 */
interface TicketOutput {
    fun present(ticket: Ticket)
}

/** Implementación de etapa 1: "imprime" publicando el ticket para que la UI lo previsualice. */
class ScreenTicketOutput : TicketOutput {
    private val _ticket = MutableStateFlow<Ticket?>(null)
    val ticket: StateFlow<Ticket?> = _ticket.asStateFlow()

    override fun present(ticket: Ticket) {
        _ticket.value = ticket
    }

    fun clear() {
        _ticket.value = null
    }
}
