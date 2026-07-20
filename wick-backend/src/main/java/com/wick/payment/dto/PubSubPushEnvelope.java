package com.wick.payment.dto;

public record PubSubPushEnvelope(PubSubMessage message) {

    public record PubSubMessage(String data, String messageId) {
    }
}
