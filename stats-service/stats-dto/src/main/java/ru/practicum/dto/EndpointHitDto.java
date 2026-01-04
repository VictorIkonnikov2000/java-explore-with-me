package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

import static ru.practicum.constants.StandardDateTimeFormats.DATE_TIME_FORMAT;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitDto {
    @NotBlank(message = "Идентификатор сервиса не может быть пустым")
    private String app;
    @NotBlank(message = "URI не может быть пустым")
    private String uri;
    @NotBlank(message = "ip пользователя не может быть пустым")
    private String ip;
    @NotNull(message = "Дата и время запроса к эндпоинту не может быть пустым")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime timestamp;
}