package cl.friendlypos.mypos.repository

import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.api.dto.EmitDteRequestDto
import cl.friendlypos.mypos.api.dto.EmitDteResponseDto
import cl.friendlypos.mypos.api.dto.ReceptorReqDto
import cl.friendlypos.mypos.api.dto.SaleCreateRequestDto
import cl.friendlypos.mypos.api.dto.TaxpayerDataDto
import cl.friendlypos.mypos.api.dto.TicketDataResponseDto
import cl.friendlypos.mypos.api.dto.SaleCustomerReqDto
import cl.friendlypos.mypos.api.dto.SaleItemReqDto
import cl.friendlypos.mypos.checkout.CheckoutHolder
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class SaleRepository {

    private val cashboxRepo = CashboxRepository()

    data class SaleResult(
        val id: String?,
        val ticketNumber: String,
        val total: Double,
        val amountPaid: Double,
        val change: Double,
        val paymentMethod: String
    )

    /** Resultado de la emisión de un DTE (`emitir-dte`). */
    sealed class DteResult {
        /** [timbreBase64] es el PNG (base64) del Timbre Electrónico SII (código PDF417). */
        data class Success(
            val folio: Long?,
            val timbreBase64: String?,
            val pdfLocalUrl: String?
        ) : DteResult()
        /** El backend respondió 400 porque la venta ya tenía DTE; se trata como éxito. */
        data class AlreadyEmitted(val folio: Long?) : DteResult()
        data class Failure(val message: String) : DteResult()
    }

    /**
     * Registra la venta en el backend (`POST /api/firestore/sales`) usando el snapshot del
     * carrito en [CheckoutHolder]. Resuelve la sesión de caja actual para asociar
     * `cashbox_session_id` y `store_id` (el backend registra el movimiento de caja).
     */
    suspend fun createSale(
        lines: List<CheckoutHolder.Line>,
        paymentMethod: String,
        amountPaid: Double,
        tipoDocumento: String?,
        customerName: String = "Cliente genérico",
        transferBank: String? = null,
        transferAccountType: String? = null,
        transferAccountNumber: String? = null
    ): Result<SaleResult> = withContext(Dispatchers.IO) {
        try {
            if (lines.isEmpty()) {
                return@withContext Result.failure(Exception("No hay productos en la venta"))
            }

            // Regla de negocio: no se puede vender sin una sesión de caja abierta.
            val session = cashboxRepo.getCurrentSession().getOrNull()
            if (session?.id == null) {
                return@withContext Result.failure(
                    Exception("Debes abrir una sesión de caja antes de vender")
                )
            }
            val total = lines.sumOf { it.lineTotal.toDouble() }
            val ticketNumber = "SALE-${System.currentTimeMillis()}"
            val change = (amountPaid - total).coerceAtLeast(0.0)

            val items = lines.map {
                SaleItemReqDto(
                    productId = null,
                    name = it.name,
                    description = it.name,
                    quantity = it.quantity,
                    price = it.unitPrice.toDouble(),
                    subtotal = it.lineTotal.toDouble(),
                    priceIncludesTaxes = true
                )
            }

            val request = SaleCreateRequestDto(
                customer = SaleCustomerReqDto(name = customerName),
                ticketNumber = ticketNumber,
                items = items,
                subtotal = total,
                tax = 0.0,
                total = total,
                paymentMethod = paymentMethod,
                amountPaid = amountPaid,
                change = change,
                cashboxSessionId = session?.id,
                storeId = session?.storeId,
                tipoDocumento = tipoDocumento,
                transferBank = transferBank.takeIf { paymentMethod == "transfer" },
                transferAccountType = transferAccountType.takeIf { paymentMethod == "transfer" },
                transferAccountNumber = transferAccountNumber.takeIf { paymentMethod == "transfer" }
            )

            val response = ApiClient.service.createSale(request)
            if (response.success && response.data != null) {
                Result.success(
                    SaleResult(
                        id = response.data.id,
                        ticketNumber = response.data.ticketNumber ?: ticketNumber,
                        total = total,
                        amountPaid = amountPaid,
                        change = response.data.change ?: change,
                        paymentMethod = paymentMethod
                    )
                )
            } else {
                Result.failure(Exception(response.error ?: response.message ?: "Error al registrar la venta"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e) ?: "Error al registrar la venta (HTTP ${e.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSaleTicketData(saleId: String): Result<TicketDataResponseDto> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = ApiClient.service.getSaleTicketData(saleId)
                if (response.data == null) throw Exception("No ticket data")
                response
            }
        }

    /**
     * Emite un DTE (boleta/factura) para una venta ya registrada.
     * `POST /api/sales/:saleId/emitir-dte`.
     *
     * Nota: el backend recalcula totales desde la venta. Para facturas (tipo 33) el backend
     * espera precios SIN IVA; el split de IVA correcto es responsabilidad del backend
     * (fuera del alcance de este cliente).
     */
    suspend fun emitDte(
        saleId: String,
        tipo: String,
        receptor: CheckoutHolder.ReceptorData?
    ): DteResult = withContext(Dispatchers.IO) {
        try {
            val request = EmitDteRequestDto(
                tipo = tipo,
                receptor = receptor?.let {
                    ReceptorReqDto(
                        rutReceptor = it.rut,
                        razonSocialReceptor = it.razonSocial,
                        giroReceptor = it.giro,
                        direccionReceptor = it.direccion,
                        comunaReceptor = it.comuna,
                        emailReceptor = it.email
                    )
                }
            )
            val response = ApiClient.service.emitDte(saleId, request)
            if (response.success) {
                DteResult.Success(response.folio, response.timbre, response.pdfLocalUrl)
            } else {
                DteResult.Failure(response.error ?: response.message ?: "Error al emitir el documento")
            }
        } catch (e: HttpException) {
            val parsed = parseEmitError(e)
            if (parsed?.existingDTE != null) {
                DteResult.AlreadyEmitted(parsed.existingDTE.folio)
            } else {
                DteResult.Failure(
                    parsed?.error ?: parsed?.message
                        ?: "Error al emitir el documento (HTTP ${e.code()})"
                )
            }
        } catch (e: Exception) {
            DteResult.Failure(e.message ?: "Error al emitir el documento")
        }
    }

    /** Consulta datos de un contribuyente por RUT (autocompletado de Factura). */
    suspend fun getTaxpayer(rut: String): Result<TaxpayerDataDto> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = ApiClient.service.getTaxpayer(rut)
                if (response.success && response.data != null) {
                    response.data
                } else {
                    throw Exception(response.error ?: "No se encontraron datos para el RUT")
                }
            }
        }

    private fun parseEmitError(e: HttpException): EmitDteResponseDto? = try {
        val body = e.response()?.errorBody()?.string()
        if (body.isNullOrBlank()) null
        else Gson().fromJson(body, EmitDteResponseDto::class.java)
    } catch (_: Exception) {
        null
    }

    private fun parseHttpError(e: HttpException): String? = try {
        val body = e.response()?.errorBody()?.string() ?: return null
        val json = Gson().fromJson(body, JsonObject::class.java)
        json?.get("error")?.asString ?: json?.get("message")?.asString
    } catch (_: Exception) {
        null
    }
}
