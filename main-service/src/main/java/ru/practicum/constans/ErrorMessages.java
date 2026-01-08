package ru.practicum.constans;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessages {
    public static final String NOT_FOUND = "The required object was not found.";
    public static final String BAD_REQUEST = "Incorrectly made request.";
    public static final String FORBIDDEN = "Conditions are not met for this operation.";
    public static final String CONFLICT = "Integrity constraint has been violated.";
    public static final String INTERNAL_ERROR = "An unexpected error occurred.";
}
