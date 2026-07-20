package com.wick.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.wick.core.model.AppResult
import com.wick.core.model.BillingProduct
import com.wick.core.model.BillingProductIds
import com.wick.core.model.BillingProductType
import com.wick.core.model.PurchaseReceipt
import com.wick.core.model.UiError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Google Play Billing implementation of [BillingClientGateway]. This is the ONLY file that
 * touches the Play Billing SDK directly — replacing the store later means writing a new
 * implementation of [BillingClientGateway] and changing the binding in [com.wick.core.billing.di.BillingModule],
 * nothing else in the app needs to change.
 */
@Singleton
class GooglePlayBillingClientGateway @Inject constructor(
    @ApplicationContext private val context: Context,
) : BillingClientGateway {

    private val purchaseUpdates = MutableSharedFlow<PurchaseReceipt>(extraBufferCapacity = 8)

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { purchase ->
                purchase.products.firstOrNull()?.let { productId ->
                    purchaseUpdates.tryEmit(
                        PurchaseReceipt(
                            productId = productId,
                            purchaseToken = purchase.purchaseToken,
                            orderId = purchase.orderId,
                        ),
                    )
                }
            }
        } else {
            Timber.w("Billing purchase flow ended with code %s", billingResult.responseCode)
        }
    }

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
            )
            .build()
    }

    override fun purchaseUpdates(): SharedFlow<PurchaseReceipt> = purchaseUpdates

    private suspend fun ensureConnected(): AppResult<Unit> {
        if (billingClient.isReady) return AppResult.Success(Unit)

        return suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (continuation.isActive) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            continuation.resume(AppResult.Success(Unit))
                        } else {
                            continuation.resume(AppResult.Failure(UiError.Unknown))
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Timber.w("Billing service disconnected")
                }
            })
        }
    }

    override suspend fun queryProducts(productIds: List<String>): AppResult<List<BillingProduct>> {
        val connected = ensureConnected()
        if (connected is AppResult.Failure) return connected

        val billingProducts = mutableListOf<BillingProduct>()
        for ((productType, idsOfType) in productIds.groupBy { it.toBillingProductType() }) {
            val products = idsOfType.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(productType)
                    .build()
            }

            val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
            val result = billingClient.queryProductDetails(params)

            if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                return AppResult.Failure(UiError.Unknown)
            }

            result.productDetailsList.orEmpty().forEach { details ->
                val price = details.oneTimePurchaseOfferDetails?.formattedPrice
                    ?: details.subscriptionOfferDetails?.firstOrNull()
                        ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    ?: ""
                billingProducts.add(
                    BillingProduct(
                        productId = details.productId,
                        type = details.productId.toDomainProductType(),
                        title = details.title,
                        description = details.description,
                        formattedPrice = price,
                    ),
                )
            }
        }
        return AppResult.Success(billingProducts)
    }

    override suspend fun launchPurchaseFlow(activity: Activity, productId: String): AppResult<Unit> {
        val connected = ensureConnected()
        if (connected is AppResult.Failure) return connected

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(productId.toBillingProductType())
                        .build(),
                ),
            )
            .build()

        val queryResult = billingClient.queryProductDetails(params)
        val productDetails = queryResult.productDetailsList.orEmpty().firstOrNull()
            ?: return AppResult.Failure(UiError.ProductNotFound)

        val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken?.let {
            productParamsBuilder.setOfferToken(it)
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
            .build()

        val launchResult = billingClient.launchBillingFlow(activity, flowParams)
        return if (launchResult.responseCode == BillingClient.BillingResponseCode.OK) {
            AppResult.Success(Unit)
        } else {
            AppResult.Failure(UiError.Unknown)
        }
    }

    override suspend fun acknowledgePurchase(purchaseToken: String): AppResult<Unit> {
        val params = com.android.billingclient.api.AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        val result = billingClient.acknowledgePurchase(params)
        return if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            AppResult.Success(Unit)
        } else {
            AppResult.Failure(UiError.Unknown)
        }
    }

    override suspend fun consumePurchase(purchaseToken: String): AppResult<Unit> {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
        val result = billingClient.consumePurchase(params)
        return if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            AppResult.Success(Unit)
        } else {
            AppResult.Failure(UiError.Unknown)
        }
    }

    private fun String.toBillingProductType(): String =
        if (this in BillingProductIds.SUBSCRIPTIONS) ProductType.SUBS else ProductType.INAPP

    private fun String.toDomainProductType(): BillingProductType =
        if (this in BillingProductIds.SUBSCRIPTIONS) BillingProductType.PREMIUM_SUBSCRIPTION else BillingProductType.TOKEN_PACK
}
