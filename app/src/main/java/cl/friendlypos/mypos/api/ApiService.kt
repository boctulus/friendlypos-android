package cl.friendlypos.mypos.api

import cl.friendlypos.mypos.api.dto.CashboxAvailabilityResponseDto
import cl.friendlypos.mypos.api.dto.EmitDteRequestDto
import cl.friendlypos.mypos.api.dto.EmitDteResponseDto
import cl.friendlypos.mypos.api.dto.LogoResponseDto
import cl.friendlypos.mypos.api.dto.TaxpayerResponseDto
import cl.friendlypos.mypos.api.dto.CustomerListResponseDto
import cl.friendlypos.mypos.api.dto.LoginRequestDto
import cl.friendlypos.mypos.api.dto.LoginResponseDto
import cl.friendlypos.mypos.api.dto.MovementTypesResponseDto
import cl.friendlypos.mypos.api.dto.PriceLookupResponseDto
import cl.friendlypos.mypos.api.dto.ProductSearchResponseDto
import cl.friendlypos.mypos.api.dto.RegisterMovementRequestDto
import cl.friendlypos.mypos.api.dto.RegisterMovementResponseDto
import cl.friendlypos.mypos.api.dto.SaleCreateRequestDto
import cl.friendlypos.mypos.api.dto.SaleCreateResponseDto
import cl.friendlypos.mypos.api.dto.SaleListResponseDto
import cl.friendlypos.mypos.api.dto.TicketDataResponseDto
import cl.friendlypos.mypos.api.dto.CloseSessionRequestDto
import cl.friendlypos.mypos.api.dto.CurrentSessionResponseDto
import cl.friendlypos.mypos.api.dto.KeepAliveResponseDto
import cl.friendlypos.mypos.api.dto.OpenSessionRequestDto
import cl.friendlypos.mypos.api.dto.SessionResponseDto
import cl.friendlypos.mypos.api.dto.SessionsListResponseDto
import cl.friendlypos.mypos.api.dto.UpdateUserResponseDto
import cl.friendlypos.mypos.api.dto.UserListResponseDto
import cl.friendlypos.mypos.api.dto.UserStoresResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @GET("auth/session/keepalive")
    suspend fun getSessionKeepAlive(): KeepAliveResponseDto

    @GET("api/firestore/cashbox/user-stores")
    suspend fun getUserStores(): UserStoresResponseDto

    @GET("api/firestore/cashbox/availability")
    suspend fun getCashboxAvailability(@Query("store_id") storeId: String): CashboxAvailabilityResponseDto

    @GET("api/firestore/cashbox/sessions")
    suspend fun getSessions(
        @Query("store_id") storeId: String,
        @Query("status") status: String = "open"
    ): SessionsListResponseDto

    @GET("api/firestore/cashbox/sessions/current")
    suspend fun getCurrentCashboxSession(): CurrentSessionResponseDto

    @POST("api/firestore/cashbox/sessions")
    suspend fun openCashboxSession(@Body request: OpenSessionRequestDto): SessionResponseDto

    @PATCH("api/firestore/cashbox/sessions/{id}/close")
    suspend fun closeCashboxSession(
        @Path("id") sessionId: String,
        @Body request: CloseSessionRequestDto
    ): SessionResponseDto

    @GET("api/users/list")
    suspend fun listUsers(@Query("q") query: String, @Query("limit") limit: Int = 1): UserListResponseDto

    @PATCH("api/users/{uid}")
    suspend fun updateUser(@Path("uid") uid: String, @Body body: Map<String, String>): UpdateUserResponseDto

    @GET("api/products/search/quick")
    suspend fun searchProductsQuick(@Query("q") query: String): ProductSearchResponseDto

    @GET("api/products")
    suspend fun getProducts(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 50
    ): ProductSearchResponseDto

    @GET("api/price-verifier/lookup")
    suspend fun lookupProductByEan(
        @Query("ean") ean: String,
        @Query("store_id") storeId: String
    ): PriceLookupResponseDto

    @GET("api/supabase/customers")
    suspend fun getCustomers(
        @Query("startAfterDocId") startAfterDocId: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("orderBy") orderBy: String = "created_at",
        @Query("order") order: String = "desc"
    ): CustomerListResponseDto

    @GET("api/firestore/cashbox/movement-types")
    suspend fun getMovementTypes(): MovementTypesResponseDto

    @POST("api/firestore/cashbox/movements")
    suspend fun registerMovement(@Body request: RegisterMovementRequestDto): RegisterMovementResponseDto

    @GET("api/sales")
    suspend fun getSales(
        @Query("fecha_desde") fechaDesde: String? = null,
        @Query("fecha_hasta") fechaHasta: String? = null,
        @Query("limit") limit: Int = 100
    ): SaleListResponseDto

    @POST("api/firestore/sales")
    suspend fun createSale(@Body request: SaleCreateRequestDto): SaleCreateResponseDto

    @GET("api/printer/ticket-data/cashbox-opening/{sessionId}")
    suspend fun getCashboxOpeningTicketData(@Path("sessionId") sessionId: String): TicketDataResponseDto

    @GET("api/printer/ticket-data/cashbox-close/{sessionId}")
    suspend fun getCashboxCloseTicketData(@Path("sessionId") sessionId: String): TicketDataResponseDto

    @GET("api/printer/ticket-data/sale/{saleId}")
    suspend fun getSaleTicketData(@Path("saleId") saleId: String): TicketDataResponseDto

    @GET("api/printer/logo/{storeId}")
    suspend fun getStoreLogo(@Path("storeId") storeId: String): LogoResponseDto

    @POST("api/sales/{saleId}/emitir-dte")
    suspend fun emitDte(
        @Path("saleId") saleId: String,
        @Body request: EmitDteRequestDto
    ): EmitDteResponseDto

    @GET("api/sales/taxpayer/{rut}")
    suspend fun getTaxpayer(@Path("rut") rut: String): TaxpayerResponseDto
}
