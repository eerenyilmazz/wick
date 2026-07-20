package com.wick.payment.dto;

import java.time.Instant;
import java.util.UUID;

public record PurchaseExportItem(UUID id, String productId, Instant purchasedAt) {
}
