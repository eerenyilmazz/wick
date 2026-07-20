package com.wick.core.data.match

import com.wick.core.data.auth.toDomain
import com.wick.core.model.AppResult
import com.wick.core.model.SwipeDirection
import com.wick.core.model.SwipeResult
import com.wick.core.model.UiError
import com.wick.core.model.User
import com.wick.core.network.di.BaseUrl
import com.wick.core.network.match.MatchApi
import com.wick.core.network.match.SwipeRequestDto
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DiscoverRepositoryImpl @Inject constructor(
    private val matchApi: MatchApi,
    @BaseUrl private val baseUrl: String,
) : DiscoverRepository {

    override suspend fun getDiscoverableUsers(): AppResult<List<User>> {
        return try {
            AppResult.Success(matchApi.discover().content.map { it.toDomain(baseUrl) })
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun swipe(targetUserId: String, direction: SwipeDirection): AppResult<SwipeResult> {
        return try {
            val response = matchApi.swipe(SwipeRequestDto(targetUserId = targetUserId, direction = direction.name))
            AppResult.Success(SwipeResult(matched = response.matched, matchId = response.matchId))
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    private fun HttpException.toUiError(): UiError = when (code()) {
        401 -> UiError.Unauthorized
        402 -> UiError.InsufficientTokens
        403 -> UiError.Forbidden
        409 -> UiError.Conflict
        429 -> UiError.RateLimited
        else -> UiError.Unknown
    }
}
