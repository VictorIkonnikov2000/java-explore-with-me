package ru.practicum.compilation.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {
    @Nullable
    @Builder.Default
    private List<Long> events = new ArrayList<>();
    @Builder.Default
    private Boolean pinned = false;
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 1, max = 50, message = "Длина заголовка должна быть от 1 до 50 символов")
    private String title;
}