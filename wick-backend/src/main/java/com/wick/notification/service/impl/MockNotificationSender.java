package com.wick.notification.service.impl;

import com.wick.notification.service.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.notification", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockNotificationSender implements NotificationSender {

    @Override
    public void send(String deviceToken, String title, String body, String type) {
        log.info("Mock push notification to {}: [{}] {} - {}", deviceToken, type, title, body);
    }
}
