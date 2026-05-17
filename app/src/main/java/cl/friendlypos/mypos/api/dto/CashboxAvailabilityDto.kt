package cl.friendlypos.mypos.api.dto

import com.google.gson.annotations.SerializedName

data class CashboxAvailabilityResponseDto(
    val success: Boolean,
    val availability: List<CashboxAvailabilityItemDto>?
)

data class CashboxAvailabilityItemDto(
    @SerializedName("cashbox_id") val cashboxId: String,
    @SerializedName("cashbox_label") val cashboxLabel: Int = 0,
    @SerializedName("display_name") val displayName: String?,
    val status: String,
    @SerializedName("session_id") val sessionId: String?,
    @SerializedName("cashier_name") val cashierName: String?
) {
    val isAvailable: Boolean get() = status == "available"
    val isOccupied: Boolean get() = status == "occupied"
}
