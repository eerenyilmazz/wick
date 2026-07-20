package com.wick.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyPurchaseRequest(@NotBlank String productId, @NotBlank String purchaseToken) {
}
