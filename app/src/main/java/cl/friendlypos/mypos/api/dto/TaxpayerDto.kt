package cl.friendlypos.mypos.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Respuesta de consulta de contribuyente por RUT — `GET /api/sales/taxpayer/:rut`.
 * Proxy del SDK de OpenFactura/Haulmer. El backend puede devolver el nombre como
 * `razon_social` o `razonSocial` según la fuente, por eso se mapean ambos.
 */
data class TaxpayerResponseDto(
    val success: Boolean = false,
    val data: TaxpayerDataDto? = null,
    val error: String? = null
)

data class TaxpayerDataDto(
    @SerializedName("razon_social") val razonSocialSnake: String? = null,
    @SerializedName("razonSocial") val razonSocialCamel: String? = null,
    val nombre: String? = null,
    val direccion: String? = null,
    val giro: String? = null,
    val comuna: String? = null,
    val ciudad: String? = null,
    val email: String? = null,
    val telefono: String? = null
) {
    fun resolveRazonSocial(): String? =
        razonSocialCamel ?: razonSocialSnake ?: nombre
}
