package com.wick.core.billing

import android.app.Activity
import com.wick.core.model.AppResult
import com.wick.core.model.BillingProduct
import com.wick.core.model.PurchaseReceipt
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over whatever store/SDK actually sells products (Google Play Billing today).
 * Everything above this interface (core:data, feature modules) only ever talks to this
 * contract — swapping the underlying billing provider means swapping the binding in
 * [com.wick.core.billing.di.BillingModule] and adding a new implementation here, nothing else.
 */
interface BillingClientGateway {

    /** Completed/pending purchases as they arrive from the store (purchase flow, app resume, etc). */
    fun purchaseUpdates(): Flow<PurchaseReceipt>

    suspend fun queryProducts(productIds: List<String>): AppResult<List<BillingProduct>>

    /** Launches the store's native purchase UI. The result arrives later via [purchaseUpdates]. */
    suspend fun launchPurchaseFlow(activity: Activity, productId: String): AppResult<Unit>

    /** Must be called after the backend has verified the purchase, so the store releases it. */
    suspend fun acknowledgePurchase(purchaseToken: String): AppResult<Unit>

    /** Same as [acknowledgePurchase] but for consumable products (token packs) that can be rebought. */
    suspend fun consumePurchase(purchaseToken: String): AppResult<Unit>
}
