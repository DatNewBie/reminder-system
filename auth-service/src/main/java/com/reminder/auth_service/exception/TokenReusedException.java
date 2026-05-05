package com.reminder.auth_service.exception;

public class TokenReusedException extends RuntimeException {
    public TokenReusedException(String message) {
        super(message);
    }
}
