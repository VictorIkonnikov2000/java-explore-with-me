package request;

import org.springframework.stereotype.Component;
import request.dto.ParticipationRequestDto;

@Component
public class ParticipationRequestMapper {

    public ParticipationRequestDto toDto(ParticipationRequest request) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(request.getId());
        dto.setEvent(request.getEventId());
        dto.setRequester(request.getRequesterId());
        dto.setStatus(request.getStatus().toString());
        dto.setCreated(request.getCreated());
        return dto;
    }

    public ParticipationRequest toEntity(ParticipationRequestDto dto) {
        ParticipationRequest request = new ParticipationRequest();
        request.setId(dto.getId());

        try {
            request.setStatus(ParticipationRequest.RequestStatus.valueOf(dto.getStatus()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + dto.getStatus());
        }

        request.setCreated(dto.getCreated());
        request.setEventId(dto.getEvent());
        request.setRequesterId(dto.getRequester());
        return request;
    }


}
