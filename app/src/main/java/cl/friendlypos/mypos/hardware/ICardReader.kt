package cl.friendlypos.mypos.hardware

interface ICardReader {
    fun initialize(): Boolean
    fun readCard(onSuccess: (String) -> Unit, onError: (String) -> Unit)
}
