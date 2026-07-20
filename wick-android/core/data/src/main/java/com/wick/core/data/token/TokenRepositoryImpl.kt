package com.wick.core.data.token

import com.wick.core.model.AppResult
import com.wick.core.model.TokenTransaction
import com.wick.core.model.UiError
import com.wick.core.network.token.TokenApi
import com.wick.core.network.token.TokenTransactionDto
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class TokenRepositoryImpl @Inject constructor(
    private val tokenApi: TokenApi,
) : TokenRepository {

    override suspend fun getBalance(): AppResult<Int> = runCatching { tokenApi.getBalance() }
        .fold(
            onSuccess = { AppResult.Success(it.balance) },
            onFailure = { it.toAppResult() },
        )

    override suspend fun getDailyLoginStatus(): AppResult<Boolean> = runCatching { tokenApi.getDailyLoginStatus() }
        .fold(
            onSuccess = { AppResult.Success(it.claimed) },
            onFailure = { it.toAppResult() },
        )

    override suspend fun claimDailyLogin(): AppResult<Int> = runCatching { tokenApi.claimDailyLogin() }
        .fold(
            onSuccess = { AppResult.Success(it.balance) },
            onFailure = { it.toAppResult() },
        )

    override suspend fun getTransactions(page: Int, size: Int): AppResult<List<TokenTransaction>> =
        runCatching { tokenApi.getTransactions(page = page, size = size).content.map { it.toDomain() } }
            .fold(
                onSuccess = { AppResult.Success(it) },
                onFailure = { it.toAppResult() },
            )

    private fun TokenTransactionDto.toDomain() = TokenTransaction(
        id = id,
        amount = amount,
        reason = reason,
        createdAt = createdAt,
    )

    private fun <T> Throwable.toAppResult(): AppResult<T> = when (this) {
        is HttpException -> AppResult.Failure(
            when (code()) {
                401 -> UiError.Unauthorized
                403 -> UiError.Forbidden
                409 -> UiError.Conflict
                429 -> UiError.RateLimited
                else -> UiError.Unknown
            },
        )
        is IOException -> AppResult.Failure(UiError.NoConnection)
        else -> throw this
    }
}
