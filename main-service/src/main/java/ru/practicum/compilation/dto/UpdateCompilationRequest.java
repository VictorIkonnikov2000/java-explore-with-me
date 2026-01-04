package ru.practicum.compilation.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompilationRequest {
    @Nullable
    private List<Long> events;
    @Nullable
    private Boolean pinned;
    @Nullable
    @Size(min = 1, max = 50, message = "Длина заголовка должна быть от 1 до 50 символов")
    private String title;
}