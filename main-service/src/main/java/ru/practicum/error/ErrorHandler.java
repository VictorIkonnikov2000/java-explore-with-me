package ru.practicum.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.error.exceptions.BadRequestException;
import ru.practicum.error.exceptions.ConflictException;
import ru.practicum.error.exceptions.ForbiddenException;
import ru.practicum.error.exceptions.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.constants.ErrorReasons.*;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex,
                                                   HttpServletRequest request) {
        log.warn("Conflict [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage());

        ApiError error = ApiError.of(
                HttpStatus.CONFLICT,
                CONFLICT,
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex,
                                                   HttpServletRequest request) {
        log.warn("Not Found [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage());

        ApiError error = ApiError.of(
                HttpStatus.NOT_FOUND,
                NOT_FOUND,
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex,
                                                     HttpServletRequest request) {
        log.warn("Bad Request [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage());

        ApiError error = ApiError.of(
                HttpStatus.BAD_REQUEST,
                BAD_REQUEST,
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex,
                                                    HttpServletRequest request) {
        log.warn("Forbidden [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage());

        ApiError error = ApiError.of(
                HttpStatus.FORBIDDEN,
                FORBIDDEN,
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("Validation failed [{} {}] - errors: {}",
                request.getMethod(),
                request.getRequestURI(),
                errors);

        ApiError error = ApiError.of(
                HttpStatus.BAD_REQUEST,
                BAD_REQUEST,
                "Validation failed",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception ex,
                                                        HttpServletRequest request) {
        log.error("Internal error [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(), ex);

        ApiError error = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                INTERNAL_ERROR,
                "An unexpected error occurred",
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}