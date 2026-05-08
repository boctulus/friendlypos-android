package cl.friendlypos.mypos.api.dto

import com.google.gson.annotations.SerializedName

data class KeepAliveResponseDto(
    val success: Boolean,
    val user: UserDto?
)

data class UserDto(
    val uid: String,
    val email: String?,
    @SerializedName("displayName") val displayName: String?,
    val role: String,
    @SerializedName("store_id") val storeId: String?
)

data class UserListResponseDto(
    val success: Boolean,
    val data: UserListDataDto?
)

data class UserListDataDto(
    val docs: List<UserItemDto>?
)

data class UserItemDto(
    val id: String,
    val uid: String,
    val email: String,
    @SerializedName("displayName") val displayName: String?,
    val role: String
)

data class UpdateUserResponseDto(
    val success: Boolean,
    val message: String?
)
