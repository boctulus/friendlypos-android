package cl.friendlypos.mypos.api.dto

import com.google.gson.annotations.SerializedName

data class UserStoresResponseDto(
    val success: Boolean,
    val stores: List<StoreDto>?
)

data class StoreDto(
    val id: String,
    val name: String?,
    @SerializedName("display_name") val displayName: String?
)

data class CashboxListResponseDto(
    val success: Boolean,
    val data: CashboxDataDto?
)

data class CashboxDataDto(
    val docs: List<CashboxItemDto>?
)

data class CashboxItemDto(
    val id: String,
    @SerializedName("serial_number") val serialNumber: Int,
    @SerializedName("display_name") val displayName: String?,
    val active: Boolean
)
