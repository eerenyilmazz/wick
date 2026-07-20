package com.wick.payment.dto;

public record DeveloperNotification(String packageName, SubscriptionNotification subscriptionNotification) {

    public record SubscriptionNotification(int notificationType, String purchaseToken, String subscriptionId) {
    }
}
