package cl.friendlypos.mypos.api.dto

import cl.friendlypos.mypos.model.Product
import com.google.gson.annotations.SerializedName

data class ProductSearchResponseDto(
    val success: Boolean,
    val products: List<ProductDto>?,
    val total: Int?,
    @SerializedName("hasMore") val hasMore: Boolean? = null,
    @SerializedName("nextCursor") val nextCursor: String? = null
)

data class ProductDto(
    val id: String,
    val description: String,       // nombre del producto en el backend
    val brand: String?,
    val category: String?,
    val ean13: String?,
    val price: Double,
    val stock: Double?,
    @SerializedName("is_active") val isActive: Boolean?,
    val sellable: Boolean?
) {
    fun toProduct() = Product(
        id = id,
        name = description,
        description = brand ?: "",
        price = price,
        stock = stock?.toInt() ?: 0,
        category = category ?: "",
        barcode = ean13
    )
}
