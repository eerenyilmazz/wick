package com.wick.core.network.payment

import kotlinx.serialization.Serializable

@Serializable
data class VerifyPurchaseRequestDto(val productId: String, val purchaseToken: String)

@Serializable
data class PurchaseResponseDto(
    val purchaseId: String,
    val productId: String,
    val tokenBalance: Int,
    val premium: Boolean,
)
