package ru.practicum.compilation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.*;
import ru.practicum.event.dto.EventShortDto;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @Nullable
    @Builder.Default
    private List<EventShortDto> events = new ArrayList<>();
    private Boolean pinned;
    private String title;
}