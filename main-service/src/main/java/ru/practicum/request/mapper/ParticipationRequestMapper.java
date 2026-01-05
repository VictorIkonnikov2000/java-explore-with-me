package ru.practicum.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParticipationRequestMapper {

    public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        if (request == null) {
            return null;
        }

        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated()) // Форматирование JSON произойдет через @JsonFormat в DTO
                .event(request.getEvent() != null ? request.getEvent().getId() : null)
                .requester(request.getRequester() != null ? request.getRequester().getId() : null)
                .status(request.getStatus())
                .build();
    }

    public List<ParticipationRequestDto> toFullDtoList(List<ParticipationRequest> requests) {
        if (requests == null) {
            return null;
        }

        return requests.stream()
                .map(this::toParticipationRequestDto)
                .collect(Collectors.toList());
    }
}
