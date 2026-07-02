package cl.friendlypos.mypos.api.dto

import com.google.gson.annotations.SerializedName

data class LogoResponseDto(
    val success: Boolean,
    @SerializedName("store_id") val storeId: String?,
    @SerializedName("logo_url") val logoUrl: String?,
    @SerializedName("absolute_url") val absoluteUrl: String?
)
