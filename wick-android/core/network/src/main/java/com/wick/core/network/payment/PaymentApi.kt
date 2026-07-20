package com.wick.core.network.payment

import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentApi {

    @POST("api/v1/payments/verify")
    suspend fun verifyPurchase(@Body request: VerifyPurchaseRequestDto): PurchaseResponseDto
}
