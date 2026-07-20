package com.wick.payment.service.impl;

import com.wick.payment.service.PlayBillingVerifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.billing", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockPlayBillingVerifier implements PlayBillingVerifier {

    @Override
    public boolean isValid(String productId, String purchaseToken) {
        return !productId.isBlank() && !purchaseToken.isBlank();
    }
}
