package com.wick.payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wick.common.exception.ApiException;
import com.wick.common.exception.ConflictException;
import com.wick.common.exception.ResourceNotFoundException;
import com.wick.eventlog.domain.EventType;
import com.wick.eventlog.service.EventLogService;
import com.wick.payment.config.BillingProperties;
import com.wick.payment.domain.EntitlementAction;
import com.wick.payment.domain.ProductType;
import com.wick.payment.domain.Purchase;
import com.wick.payment.domain.SubscriptionNotificationType;
import com.wick.payment.dto.DeveloperNotification;
import com.wick.payment.repository.PurchaseRepository;
import com.wick.payment.service.PaymentService;
import com.wick.payment.service.PlayBillingVerifier;
import com.wick.token.config.TokenProperties;
import com.wick.token.domain.TokenReason;
import com.wick.token.service.TokenService;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PurchaseRepository purchaseRepository;
    private final PlayBillingVerifier playBillingVerifier;
    private final BillingProperties billingProperties;
    private final UserService userService;
    private final TokenService tokenService;
    private final TokenProperties tokenProperties;
    private final ObjectMapper objectMapper;
    private final EventLogService eventLogService;

    @Override
    @Transactional
    public Purchase processPurchase(UUID userId, String productId, String purchaseToken) {
        if (purchaseRepository.existsByPurchaseToken(purchaseToken)) {
            throw new ConflictException("This purchase has already been processed");
        }
        if (!playBillingVerifier.isValid(productId, purchaseToken)) {
            throw new ApiException("Purchase could not be verified", HttpStatus.BAD_REQUEST);
        }

        BillingProperties.ProductConfig config = billingProperties.getProducts().get(productId);
        if (config == null) {
            throw new ResourceNotFoundException("Unknown product: " + productId);
        }

        User user = userService.getById(userId);

        Purchase newPurchase = new Purchase();
        newPurchase.setUser(user);
        newPurchase.setProductId(productId);
        newPurchase.setPurchaseToken(purchaseToken);

        Purchase purchase;
        try {
            purchase = purchaseRepository.saveAndFlush(newPurchase);
        } catch (DataIntegrityViolationException alreadyProcessedConcurrently) {
            throw new ConflictException("This purchase has already been processed");
        }

        applyEntitlement(user, config);
        eventLogService.log(EventType.PURCHASE_COMPLETED, userId, productId);
        return purchase;
    }

    private void applyEntitlement(User user, BillingProperties.ProductConfig config) {
        if (config.getType() == ProductType.TOKEN_PACK) {
            tokenService.earn(user.getId(), config.getAmount(), TokenReason.PURCHASED);
        } else if (config.getType() == ProductType.PREMIUM_SUBSCRIPTION) {
            userService.grantPremium(user.getId());
            tokenService.earn(user.getId(), tokenProperties.getPremiumMonthlyBonus(), TokenReason.PREMIUM_GRANT);
        }
    }

    @Override
    @Transactional
    public void handlePushNotification(String base64Data) {
        DeveloperNotification notification = decode(base64Data);
        if (notification.subscriptionNotification() == null) {
            return;
        }
        DeveloperNotification.SubscriptionNotification subscriptionNotification = notification.subscriptionNotification();
        SubscriptionNotificationType type = SubscriptionNotificationType.fromCode(subscriptionNotification.notificationType());
        if (type.getAction() == EntitlementAction.NONE) {
            return;
        }

        Purchase purchase = purchaseRepository.findByPurchaseToken(subscriptionNotification.purchaseToken())
                .orElseThrow(() -> new ResourceNotFoundException("No purchase found for token"));
        UUID userId = purchase.getUser().getId();
        if (type.getAction() == EntitlementAction.GRANT) {
            userService.grantPremium(userId);
        } else {
            userService.revokePremium(userId);
        }
    }

    @Override
    public List<Purchase> getPurchasesForUser(UUID userId) {
        return purchaseRepository.findAllByUserId(userId);
    }

    private DeveloperNotification decode(String base64Data) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Data);
            return objectMapper.readValue(decoded, DeveloperNotification.class);
        } catch (IOException | IllegalArgumentException e) {
            throw new ApiException("Invalid RTDN payload", HttpStatus.BAD_REQUEST);
        }
    }
}
