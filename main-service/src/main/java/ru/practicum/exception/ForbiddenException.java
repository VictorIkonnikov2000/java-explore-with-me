package ru.practicum.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Object... args) {
        super(String.format(message, args));
    }
}