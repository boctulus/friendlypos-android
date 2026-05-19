package cl.friendlypos.mypos.api.dto

import com.google.gson.annotations.SerializedName

data class MovementTypeDto(
    val code: String,
    val label: String,
    val type: String,
    val sign: String,
    val description: String?,
    @SerializedName("allowNegativeBalance") val allowNegativeBalance: Boolean = false,
    @SerializedName("requiresAuthorization") val requiresAuthorization: Boolean = false,
    @SerializedName("requiresJustification") val requiresJustification: Boolean = false
)

data class MovementTypesResponseDto(
    val success: Boolean,
    @SerializedName("movementTypes") val movementTypes: List<MovementTypeDto>?
)

data class RegisterMovementRequestDto(
    @SerializedName("cashbox_session_id") val cashboxSessionId: String,
    @SerializedName("movement_code") val movementCode: String,
    val amount: Double,
    val description: String,
    @SerializedName("payment_method") val paymentMethod: String = "cash",
    @SerializedName("reference_id") val referenceId: String? = null
)

data class RegisterMovementResponseDto(
    val success: Boolean,
    val movement: MovementItemDto?,
    val message: String?,
    val error: String?
)

data class MovementItemDto(
    val id: String?,
    @SerializedName("cashbox_session_id") val cashboxSessionId: String?,
    val type: String?,
    @SerializedName("movement_code") val movementCode: String?,
    val amount: Double?,
    val description: String?
)
