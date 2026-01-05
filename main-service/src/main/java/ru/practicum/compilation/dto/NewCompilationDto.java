package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {

    @Builder.Default
    private List<Long> events = new ArrayList<>();

    @Builder.Default
    private Boolean pinned = false;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 1, max = 50, message = "Title length must be between 1 and 50 characters")
    private String title;
}
