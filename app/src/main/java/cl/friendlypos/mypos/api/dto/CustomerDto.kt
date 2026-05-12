package cl.friendlypos.mypos.api.dto

import cl.friendlypos.mypos.model.Customer
import com.google.gson.annotations.SerializedName

data class CustomerListResponseDto(
    val success: Boolean,
    val data: CustomerPageDataDto?
)

data class CustomerPageDataDto(
    val docs: List<CustomerDto>?,
    val pagination: CustomerPaginationDto?
)

data class CustomerPaginationDto(
    val found: Int?,
    val total: Int?,
    val hasMore: Boolean?,
    val nextCursor: String?,
    val limit: Int?
)

data class CustomerDto(
    val id: String,
    val name: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val rut: String?,
    @SerializedName("store_id") val storeId: String?,
    @SerializedName("total_purchases") val totalPurchases: Double?,
    @SerializedName("last_purchase_date") val lastPurchaseDate: String?,
    @SerializedName("created_at") val createdAt: String?
) {
    fun toCustomer() = Customer(
        id = id,
        name = name ?: "",
        email = email ?: "",
        phone = phone ?: "",
        address = address ?: "",
        totalPurchases = totalPurchases ?: 0.0,
        lastPurchaseDate = lastPurchaseDate ?: createdAt?.take(10) ?: ""
    )
}
