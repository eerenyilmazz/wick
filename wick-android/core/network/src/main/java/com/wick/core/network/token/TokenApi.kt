package com.wick.core.network.token

import com.wick.core.network.dto.PageResponseDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TokenApi {

    @GET("api/v1/tokens/balance")
    suspend fun getBalance(): TokenBalanceResponseDto

    @GET("api/v1/tokens/daily-login/status")
    suspend fun getDailyLoginStatus(): DailyLoginStatusDto

    @POST("api/v1/tokens/daily-login")
    suspend fun claimDailyLogin(): TokenBalanceResponseDto

    @GET("api/v1/tokens/transactions")
    suspend fun getTransactions(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PageResponseDto<TokenTransactionDto>
}
