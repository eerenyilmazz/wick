package com.wick.core.data.match

import com.wick.core.data.auth.toDomain
import com.wick.core.model.AppResult
import com.wick.core.model.Invitation
import com.wick.core.model.InvitationStatus
import com.wick.core.model.MatchSummary
import com.wick.core.model.UiError
import com.wick.core.network.di.BaseUrl
import com.wick.core.network.invitation.InvitationResponseDto
import com.wick.core.network.match.MatchApi
import com.wick.core.network.match.MatchResponseDto
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MatchListRepositoryImpl @Inject constructor(
    private val matchApi: MatchApi,
    @BaseUrl private val baseUrl: String,
) : MatchListRepository {

    override suspend fun getMatches(): AppResult<List<MatchSummary>> {
        return try {
            AppResult.Success(matchApi.getMyMatches().content.map { it.toDomain(baseUrl) })
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun unmatch(matchId: String): AppResult<Unit> {
        return try {
            matchApi.unmatch(matchId)
            AppResult.Success(Unit)
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    private fun MatchResponseDto.toDomain(baseUrl: String): MatchSummary = MatchSummary(
        id = id,
        otherUser = otherUser.toDomain(baseUrl),
        createdAt = createdAt,
        latestInvitation = latestInvitation?.toDomain(),
        sessionStatus = sessionStatus,
        sessionEndsAt = sessionEndsAt,
    )

    private fun InvitationResponseDto.toDomain(): Invitation = Invitation(
        id = id,
        matchId = matchId,
        senderId = senderId,
        status = runCatching { InvitationStatus.valueOf(status) }.getOrDefault(InvitationStatus.PENDING),
        createdAt = createdAt,
    )

    private fun HttpException.toUiError(): UiError = when (code()) {
        401 -> UiError.Unauthorized
        403 -> UiError.Forbidden
        429 -> UiError.RateLimited
        else -> UiError.Unknown
    }
}
