package com.wick.payment.service;

public interface PlayBillingVerifier {

    boolean isValid(String productId, String purchaseToken);
}
