package com.wick.payment.service.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.wick.payment.config.BillingProperties;
import com.wick.payment.config.GooglePlayBillingProperties;
import com.wick.payment.domain.ProductType;
import com.wick.payment.service.PlayBillingVerifier;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.billing", name = "provider", havingValue = "google-play")
public class GooglePlayBillingVerifier implements PlayBillingVerifier {

    private static final int PRODUCT_PURCHASED = 0;
    private static final int SUBSCRIPTION_PAYMENT_RECEIVED = 1;
    private static final int SUBSCRIPTION_FREE_TRIAL = 2;

    private final GooglePlayBillingProperties googlePlayBillingProperties;
    private final BillingProperties billingProperties;

    private AndroidPublisher androidPublisher;

    @PostConstruct
    void init() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(googlePlayBillingProperties.getServiceAccountKeyPath()))
                .createScoped(AndroidPublisherScopes.ANDROIDPUBLISHER);
        this.androidPublisher = new AndroidPublisher.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("wick-backend")
                .build();
    }

    @Override
    public boolean isValid(String productId, String purchaseToken) {
        BillingProperties.ProductConfig config = billingProperties.getProducts().get(productId);
        if (config == null) {
            return false;
        }
        try {
            return config.getType() == ProductType.PREMIUM_SUBSCRIPTION
                    ? isActiveSubscription(productId, purchaseToken)
                    : isPurchasedProduct(productId, purchaseToken);
        } catch (IOException e) {
            log.warn("Google Play purchase verification failed for product {}", productId, e);
            return false;
        }
    }

    private boolean isPurchasedProduct(String productId, String purchaseToken) throws IOException {
        ProductPurchase purchase = androidPublisher.purchases().products()
                .get(googlePlayBillingProperties.getPackageName(), productId, purchaseToken)
                .execute();
        return purchase.getPurchaseState() != null && purchase.getPurchaseState() == PRODUCT_PURCHASED;
    }

    private boolean isActiveSubscription(String subscriptionId, String purchaseToken) throws IOException {
        SubscriptionPurchase purchase = androidPublisher.purchases().subscriptions()
                .get(googlePlayBillingProperties.getPackageName(), subscriptionId, purchaseToken)
                .execute();
        Integer paymentState = purchase.getPaymentState();
        boolean paid = paymentState != null
                && (paymentState == SUBSCRIPTION_PAYMENT_RECEIVED || paymentState == SUBSCRIPTION_FREE_TRIAL);
        boolean notExpired = purchase.getExpiryTimeMillis() != null
                && purchase.getExpiryTimeMillis() > System.currentTimeMillis();
        return paid && notExpired;
    }
}
