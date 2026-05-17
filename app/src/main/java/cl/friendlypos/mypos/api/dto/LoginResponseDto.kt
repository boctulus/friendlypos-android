package cl.friendlypos.mypos.api.dto

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class LoginResponseDto(
    val success: Boolean,
    val data: LoginTokensDto?,
    val error: String?,
    val code: String?
)

data class LoginTokensDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("token_type") val tokenType: String
)

data class JwtPayload(
    val sub: String,
    val email: String,
    val role: String,
    @SerializedName("store_id") val storeId: String?,
    val exp: Long
)
