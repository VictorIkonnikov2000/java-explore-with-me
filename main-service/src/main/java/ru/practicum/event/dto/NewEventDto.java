package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

import static ru.practicum.constants.StandardDateTimeFormats.DATE_TIME_FORMAT;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @NotBlank(message = "Краткое описание события не может быть пустым")
    @Size(min = 20, max = 2000, message = "Длина краткого описания события должна быть от 20 до 2000 символов")
    private String annotation;
    @NotNull(message = "id категории к которой относится событие должно быть указано")
    private Long category;
    @NotBlank(message = "Полное описание события не может быть пустым")
    @Size(min = 20, max = 7000, message = "Полное описание события должна быть от 20 до 7000 символов")
    private String description;
    @NotNull(message = "Поле eventDate не может быть пустым")
    @Future(message = "Дата события должна быть в будущем")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime eventDate;
    @NotNull(message = "Координаты проведения события должны быть указаны")
    @Valid
    private LocationDto location;
    @Builder.Default
    private Boolean paid = false;
    @Builder.Default
    @Min(value = 0, message = "Лимит участников не может быть отрицательным")
    private Integer participantLimit = 0;
    @Builder.Default
    private Boolean requestModeration = true;
    @NotBlank(message = "Заголовок события не может быть пустым")
    @Size(min = 3, max = 120, message = "Заголовок события должен быть от 3 до 120 символов")
    private String title;
}