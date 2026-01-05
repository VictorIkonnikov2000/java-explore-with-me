package ru.practicum.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Object... args) {
        super(String.format(message, args));
    }
}