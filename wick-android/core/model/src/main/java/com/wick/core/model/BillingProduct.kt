package com.wick.core.model

enum class BillingProductType { TOKEN_PACK, PREMIUM_SUBSCRIPTION }

data class BillingProduct(
    val productId: String,
    val type: BillingProductType,
    val title: String,
    val description: String,
    val formattedPrice: String,
)

data class PurchaseReceipt(
    val productId: String,
    val purchaseToken: String,
    val orderId: String?,
)

data class PurchaseResult(
    val purchaseId: String,
    val productId: String,
    val tokenBalance: Int,
    val premium: Boolean,
)
