package com.wick.core.network.token

import kotlinx.serialization.Serializable

@Serializable
data class TokenBalanceResponseDto(val balance: Int)

@Serializable
data class DailyLoginStatusDto(val claimed: Boolean)

@Serializable
data class TokenTransactionDto(
    val id: String,
    val amount: Int,
    val reason: String,
    val createdAt: String,
)
