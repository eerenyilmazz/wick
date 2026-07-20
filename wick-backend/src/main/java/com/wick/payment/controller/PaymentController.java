package com.wick.payment.controller;

import com.wick.common.exception.ApiException;
import com.wick.payment.dto.PubSubPushEnvelope;
import com.wick.payment.dto.PurchaseResponse;
import com.wick.payment.dto.VerifyPurchaseRequest;
import com.wick.payment.mapper.PaymentMapper;
import com.wick.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @Value("${app.billing.webhook-secret:}")
    private String webhookSecret;

    @PostMapping("/verify")
    public PurchaseResponse verify(@AuthenticationPrincipal String userId, @Valid @RequestBody VerifyPurchaseRequest request) {
        var purchase = paymentService.processPurchase(UUID.fromString(userId), request.productId(), request.purchaseToken());
        return paymentMapper.toResponse(purchase);
    }

    @PostMapping("/rtdn")
    public void receiveRealtimeDeveloperNotification(@RequestBody PubSubPushEnvelope envelope,
                                                      @RequestParam(required = false) String token) {
        if (!webhookSecret.isBlank() && !webhookSecret.equals(token)) {
            throw new ApiException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        paymentService.handlePushNotification(envelope.message().data());
    }
}
