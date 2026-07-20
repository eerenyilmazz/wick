package com.wick.payment.service;

import com.wick.payment.domain.Purchase;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    Purchase processPurchase(UUID userId, String productId, String purchaseToken);

    void handlePushNotification(String base64Data);

    List<Purchase> getPurchasesForUser(UUID userId);
}
