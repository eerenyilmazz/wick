package com.wick.core.data.payment

import android.app.Activity
import com.wick.core.model.AppResult
import com.wick.core.model.BillingProduct
import com.wick.core.model.PurchaseResult
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {

    /** Product catalog (token packs + subscription) as priced by the store right now. */
    suspend fun getProductCatalog(): AppResult<List<BillingProduct>>

    /** Launches the store's native purchase UI. The outcome arrives via [observeCompletedPurchases]. */
    suspend fun launchPurchase(activity: Activity, productId: String): AppResult<Unit>

    /** Verified-with-backend purchases: token balance / premium status already up to date by the time this emits. */
    fun observeCompletedPurchases(): Flow<AppResult<PurchaseResult>>
}
