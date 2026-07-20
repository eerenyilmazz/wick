package com.wick.core.data.invitation

import com.wick.core.model.AppResult
import com.wick.core.model.Invitation
import com.wick.core.model.InvitationStatus
import com.wick.core.model.UiError
import com.wick.core.network.invitation.InvitationApi
import com.wick.core.network.invitation.InvitationResponseDto
import com.wick.core.network.invitation.SendInvitationRequestDto
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class InvitationRepositoryImpl @Inject constructor(
    private val invitationApi: InvitationApi,
) : InvitationRepository {

    override suspend fun sendInvitation(matchId: String, useToken: Boolean): AppResult<Invitation> {
        return try {
            val response = invitationApi.send(SendInvitationRequestDto(matchId = matchId, useToken = useToken))
            AppResult.Success(response.toDomain())
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun getIncomingInvitations(): AppResult<List<Invitation>> {
        return try {
            AppResult.Success(invitationApi.getIncoming().map { it.toDomain() })
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun acceptInvitation(id: String): AppResult<Invitation> {
        return try {
            AppResult.Success(invitationApi.accept(id).toDomain())
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun declineInvitation(id: String): AppResult<Invitation> {
        return try {
            AppResult.Success(invitationApi.decline(id).toDomain())
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun cancelInvitation(id: String): AppResult<Invitation> {
        return try {
            AppResult.Success(invitationApi.cancel(id).toDomain())
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

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
        409 -> UiError.Conflict
        429 -> UiError.RateLimited
        else -> UiError.Unknown
    }
}
