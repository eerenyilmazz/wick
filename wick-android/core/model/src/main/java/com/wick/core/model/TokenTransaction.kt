package com.wick.core.model

data class TokenTransaction(
    val id: String,
    val amount: Int,
    val reason: String,
    val createdAt: String,
)
