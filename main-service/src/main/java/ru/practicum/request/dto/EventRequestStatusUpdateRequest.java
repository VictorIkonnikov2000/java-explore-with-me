package ru.practicum.request.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.request.model.RequestStatus;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateRequest {
    @NotEmpty(message = "Ids list cannot be empty")
    Set<Long> requestIds;

    RequestStatus status;
}
