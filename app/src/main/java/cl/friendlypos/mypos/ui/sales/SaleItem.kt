package cl.friendlypos.mypos.ui.sales

import java.util.UUID

data class SaleItem(
    val id: String = UUID.randomUUID().toString(),
    val unitPrice: Int,
    val quantity: Int,
    val name: String = "",
    // Identidad del producto del catálogo (solo ítems escaneados/seleccionados).
    // Los ítems ingresados manualmente quedan en null. Se usa para consolidar
    // re-escaneos del mismo producto (sumar cantidad en su línea existente).
    val productId: String? = null
)