package com.wick.core.data.token

import com.wick.core.model.AppResult
import com.wick.core.model.TokenTransaction

interface TokenRepository {
    suspend fun getBalance(): AppResult<Int>
    suspend fun getDailyLoginStatus(): AppResult<Boolean>
    suspend fun claimDailyLogin(): AppResult<Int>
    suspend fun getTransactions(page: Int = 0, size: Int = 20): AppResult<List<TokenTransaction>>
}
