package ru.practicum.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private String status;
    private String reason;
    private String message;
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    public static ApiError of(
            HttpStatus status,
            String reason,
            String message,
            List<String> errors
    ) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.name())
                .reason(reason)
                .message(message)
                .errors(errors != null ? errors : new ArrayList<>())
                .build();
    }
}
