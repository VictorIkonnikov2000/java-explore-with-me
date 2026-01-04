package request.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import request.RequestStatus;


import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {
    @NotEmpty(message = "Список ID запросов не может быть пустым")
    @Builder.Default
    private Set<Long> requestIds = new HashSet<>();
    private RequestStatus status;
}