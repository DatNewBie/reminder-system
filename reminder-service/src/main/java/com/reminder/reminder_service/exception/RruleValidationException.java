package com.reminder.reminder_service.exception;

public class RruleValidationException extends RuntimeException {

    public RruleValidationException(String rrule, String reason) {
        super("Invalid RRULE '" + rrule + "': " + reason);
    }
}
