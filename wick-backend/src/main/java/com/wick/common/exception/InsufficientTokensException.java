package com.wick.common.exception;

import org.springframework.http.HttpStatus;

public class InsufficientTokensException extends ApiException {

    public InsufficientTokensException(String message) {
        super(message, HttpStatus.PAYMENT_REQUIRED);
    }
}
