package com.wick.core.model

object BillingProductIds {
    const val TOKEN_PACK_5 = "token_pack_5"
    const val TOKEN_PACK_20 = "token_pack_20"
    const val PREMIUM_MONTHLY = "premium_monthly"

    val TOKEN_PACKS = listOf(TOKEN_PACK_5, TOKEN_PACK_20)
    val SUBSCRIPTIONS = listOf(PREMIUM_MONTHLY)
    val ALL = TOKEN_PACKS + SUBSCRIPTIONS
}
