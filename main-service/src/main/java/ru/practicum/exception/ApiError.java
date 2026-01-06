package ru.practicum.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constans.StandardDateTimeFormats.DATE_TIME_FORMAT;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime timestamp;
    private String status; // Обычно хранят HttpStatus (name), либо переименовывают в "code"
    private String reason;
    private String message;
    private List<String> errors;

    public static ApiError of(HttpStatus status, String reason, String message) {
        return of(status, reason, message, null);
    }

    public static ApiError of(HttpStatus status, String reason, String message, List<String> errors) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.name())
                .reason(reason)
                .message(message)
                .errors(errors)
                .build();
    }
}
