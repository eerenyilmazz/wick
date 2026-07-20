package com.wick.core.data.safety

import com.wick.core.model.AppResult
import com.wick.core.model.ReportReason
import com.wick.core.model.UiError
import com.wick.core.network.safety.ReportRequestDto
import com.wick.core.network.safety.SafetyApi
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SafetyRepositoryImpl @Inject constructor(
    private val safetyApi: SafetyApi,
) : SafetyRepository {

    override suspend fun block(userId: String): AppResult<Unit> {
        return try {
            safetyApi.block(userId)
            AppResult.Success(Unit)
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun unblock(userId: String): AppResult<Unit> {
        return try {
            safetyApi.unblock(userId)
            AppResult.Success(Unit)
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun report(userId: String, reason: ReportReason, details: String?): AppResult<Unit> {
        return try {
            safetyApi.report(userId, ReportRequestDto(reason = reason.name, details = details))
            AppResult.Success(Unit)
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    private fun HttpException.toUiError(): UiError = when (code()) {
        401 -> UiError.Unauthorized
        403 -> UiError.Forbidden
        409 -> UiError.Conflict
        429 -> UiError.RateLimited
        else -> UiError.Unknown
    }
}
