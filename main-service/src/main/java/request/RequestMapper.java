package request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import request.dto.ParticipationRequestDto;


import java.util.List;

import static constans.StandardDateTimeFormats.DATE_TIME_FORMAT;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "created", source = "request.created",
            dateFormat = DATE_TIME_FORMAT)
    @Mapping(target = "event", source = "request.event.id")
    @Mapping(target = "requester", source = "request.requester.id")
    ParticipationRequestDto mapToParticipationRequestDto(ParticipationRequest request);

    List<ParticipationRequestDto> toFullDtoList(List<ParticipationRequest> requests);
}
