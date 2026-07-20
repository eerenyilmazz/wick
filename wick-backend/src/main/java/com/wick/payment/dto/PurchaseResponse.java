package com.wick.payment.dto;

import java.util.UUID;

public record PurchaseResponse(UUID purchaseId, String productId, int tokenBalance, boolean premium) {
}
