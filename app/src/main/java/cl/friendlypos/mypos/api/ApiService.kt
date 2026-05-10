package cl.friendlypos.mypos.api

import cl.friendlypos.mypos.api.dto.CashboxAvailabilityResponseDto
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
}
