package com.wick.core.network.match

import com.wick.core.network.dto.PageResponseDto
import com.wick.core.network.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MatchApi {

    @GET("api/v1/matches/discover")
    suspend fun discover(@Query("size") size: Int = 20): PageResponseDto<UserResponseDto>

    @POST("api/v1/matches/swipe")
    suspend fun swipe(@Body request: SwipeRequestDto): SwipeResponseDto

    @GET("api/v1/matches")
    suspend fun getMyMatches(@Query("size") size: Int = 50): PageResponseDto<MatchResponseDto>

    @DELETE("api/v1/matches/{id}")
    suspend fun unmatch(@Path("id") matchId: String)
}
