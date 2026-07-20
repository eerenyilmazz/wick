package com.wick.notification.service;

public interface NotificationSender {

    void send(String deviceToken, String title, String body, String type);
}
