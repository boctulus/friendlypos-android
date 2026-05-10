package cl.friendlypos.mypos.api.dto

import com.google.gson.annotations.SerializedName

data class OpenSessionRequestDto(
    @SerializedName("store_id") val storeId: String,
    @SerializedName("cashbox_id") val cashboxId: String,
    @SerializedName("initial_amount") val initialAmount: Double,
    val notes: String? = null,
    @SerializedName("operation_id") val operationId: String? = null,
    @SerializedName("device_id") val deviceId: String? = null
)

data class CloseSessionRequestDto(
    @SerializedName("final_amount") val finalAmount: Double,
    val notes: String? = null,
    @SerializedName("operation_id") val operationId: String? = null,
    @SerializedName("device_id") val deviceId: String? = null
)

data class SessionResponseDto(
    val success: Boolean,
    val session: CashboxSessionItemDto?,
    val message: String?,
    val error: String?
)

data class CurrentSessionResponseDto(
    val success: Boolean,
    val session: CashboxSessionItemDto?,
    val message: String?
)

data class CashboxSessionItemDto(
    val id: String,
    @SerializedName("store_id") val storeId: String,
    @SerializedName("cashbox_id") val cashboxId: String?,
    @SerializedName("cashbox_label") val cashboxLabel: String?,
    @SerializedName("initial_amount") val initialAmount: Double,
    @SerializedName("final_amount") val finalAmount: Double?,
    @SerializedName("expected_amount") val expectedAmount: Double?,
    @SerializedName("total_cash") val totalCash: Double?,
    val status: String,
    @SerializedName("opened_at") val openedAt: String?,
    @SerializedName("closed_at") val closedAt: String?,
    @SerializedName("cashier_name") val cashierName: String?,
    @SerializedName("device_id") val deviceId: String?
)

data class SessionsListResponseDto(
    val success: Boolean,
    val sessions: List<CashboxSessionItemDto>?,
    val count: Int?
)
