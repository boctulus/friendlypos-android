package cl.friendlypos.mypos.api.dto

/**
 * Respuesta de `GET /api/price-verifier/lookup?ean=&store_id=`.
 *
 * El backend resuelve el EAN13 vía `ProductsModel.searchQuick` y devuelve la misma
 * forma de fila que `/api/products/search/quick`, por eso se reusa [ProductDto].
 */
data class PriceLookupResponseDto(
    val found: Boolean,
    val product: ProductDto?
)
