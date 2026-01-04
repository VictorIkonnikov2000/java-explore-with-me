package ru.practicum.event.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
    @NotNull(message = "Широта не может быть null")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Float lat;
    @NotNull(message = "Долгота не может быть null")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Float lon;
}