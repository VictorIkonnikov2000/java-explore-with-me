package request.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventRequestStatusUpdateResult {
    @Builder.Default
    private List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
    @Builder.Default
    private List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
}
