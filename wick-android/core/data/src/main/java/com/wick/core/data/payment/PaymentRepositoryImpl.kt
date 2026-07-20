package com.wick.core.data.payment

import android.app.Activity
import com.wick.core.billing.BillingClientGateway
import com.wick.core.model.AppResult
import com.wick.core.model.BillingProduct
import com.wick.core.model.BillingProductIds
import com.wick.core.model.PurchaseResult
import com.wick.core.model.UiError
import com.wick.core.network.payment.PaymentApi
import com.wick.core.network.payment.VerifyPurchaseRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val billingClientGateway: BillingClientGateway,
    private val paymentApi: PaymentApi,
) : PaymentRepository {

    override suspend fun getProductCatalog(): AppResult<List<BillingProduct>> =
        billingClientGateway.queryProducts(BillingProductIds.ALL)

    override suspend fun launchPurchase(activity: Activity, productId: String): AppResult<Unit> =
        billingClientGateway.launchPurchaseFlow(activity, productId)

    override fun observeCompletedPurchases(): Flow<AppResult<PurchaseResult>> =
        billingClientGateway.purchaseUpdates().map { receipt ->
            try {
                val response = paymentApi.verifyPurchase(
                    VerifyPurchaseRequestDto(productId = receipt.productId, purchaseToken = receipt.purchaseToken),
                )

                if (receipt.productId in BillingProductIds.TOKEN_PACKS) {
                    billingClientGateway.consumePurchase(receipt.purchaseToken)
                } else {
                    billingClientGateway.acknowledgePurchase(receipt.purchaseToken)
                }

                AppResult.Success(
                    PurchaseResult(
                        purchaseId = response.purchaseId,
                        productId = response.productId,
                        tokenBalance = response.tokenBalance,
                        premium = response.premium,
                    ),
                )
            } catch (httpError: HttpException) {
                AppResult.Failure(httpError.toUiError())
            } catch (networkError: IOException) {
                AppResult.Failure(UiError.NoConnection)
            }
        }

    private fun HttpException.toUiError(): UiError = when (code()) {
        401 -> UiError.Unauthorized
        403 -> UiError.Forbidden
        409 -> UiError.Conflict
        429 -> UiError.RateLimited
        else -> UiError.Unknown
    }
}
