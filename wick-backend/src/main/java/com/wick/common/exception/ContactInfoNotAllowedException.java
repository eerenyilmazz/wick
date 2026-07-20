package com.wick.common.exception;

import org.springframework.http.HttpStatus;

public class ContactInfoNotAllowedException extends ApiException {

    public ContactInfoNotAllowedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
