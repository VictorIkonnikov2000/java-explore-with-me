package ru.practicum.constans;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorReasons {
    public static final String NOT_FOUND = "The requested resource was not found";
    public static final String BAD_REQUEST = "Request parameters are not valid";
    public static final String FORBIDDEN = "For the requested operation the conditions are not met";
    public static final String CONFLICT = "The request could not be completed due to a conflict";
    public static final String INTERNAL_ERROR = "Internal server error occurred";
}