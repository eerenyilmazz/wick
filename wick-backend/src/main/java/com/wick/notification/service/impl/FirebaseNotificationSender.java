package com.wick.notification.service.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.wick.notification.config.NotificationProperties;
import com.wick.notification.service.NotificationSender;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.notification", name = "provider", havingValue = "firebase")
public class FirebaseNotificationSender implements NotificationSender {

    private final NotificationProperties notificationProperties;

    private FirebaseMessaging firebaseMessaging;

    @PostConstruct
    void init() throws IOException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(notificationProperties.getServiceAccountKeyPath()));
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
        FirebaseApp app = FirebaseApp.initializeApp(options);
        this.firebaseMessaging = FirebaseMessaging.getInstance(app);
    }

    @Override
    public void send(String deviceToken, String title, String body, String type) {
        Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putData("type", type)
                .build();
        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("Failed to send push notification to {}", deviceToken, e);
        }
    }
}
