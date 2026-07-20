package com.wick.payment.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SubscriptionNotificationType {

    RECOVERED(1, EntitlementAction.GRANT),
    RENEWED(2, EntitlementAction.GRANT),
    CANCELED(3, EntitlementAction.NONE),
    PURCHASED(4, EntitlementAction.GRANT),
    ON_HOLD(5, EntitlementAction.REVOKE),
    IN_GRACE_PERIOD(6, EntitlementAction.GRANT),
    RESTARTED(7, EntitlementAction.GRANT),
    PRICE_CHANGE_CONFIRMED(8, EntitlementAction.NONE),
    DEFERRED(9, EntitlementAction.NONE),
    PAUSED(10, EntitlementAction.REVOKE),
    PAUSE_SCHEDULE_CHANGED(11, EntitlementAction.NONE),
    REVOKED(12, EntitlementAction.REVOKE),
    EXPIRED(13, EntitlementAction.REVOKE);

    private final int code;
    private final EntitlementAction action;

    SubscriptionNotificationType(int code, EntitlementAction action) {
        this.code = code;
        this.action = action;
    }

    public static SubscriptionNotificationType fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown subscription notification type: " + code));
    }
}
