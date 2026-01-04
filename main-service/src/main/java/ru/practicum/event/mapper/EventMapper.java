package ru.practicum.event.mapper;

import org.mapstruct.*;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

import static constants.StandardDateTimeFormats.DATE_TIME_FORMAT;
import static ru.practicum.event.model.EventState.PENDING;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {

    @Mapping(target = "eventDate", source = "event.eventDate",
            dateFormat = DATE_TIME_FORMAT)
    @Mapping(target = "initiator", source = "event.initiator")
    @Mapping(target = "category", source = "event.category")
    @Mapping(target = "confirmedRequests", source = "event.confirmedRequests")
    @Mapping(target = "views", ignore = true)
    EventShortDto mapToEventShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "eventDate", source = "request.eventDate", dateFormat = DATE_TIME_FORMAT)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "eventState", ignore = true)
    Event mapToEvent(NewEventDto request);

    @AfterMapping
    default void setDefaults(@MappingTarget Event event) {
        if (event.getCreatedOn() == null) event.setCreatedOn(LocalDateTime.now());
        if (event.getConfirmedRequests() == null) event.setConfirmedRequests(0L);
        if (event.getEventState() == null) event.setEventState(PENDING);
    }

    @Mapping(target = "createdOn", source = "event.createdOn", dateFormat = DATE_TIME_FORMAT)
    @Mapping(target = "eventDate", source = "event.eventDate", dateFormat = DATE_TIME_FORMAT)
    @Mapping(target = "publishedOn", source = "event.publishedOn", dateFormat = DATE_TIME_FORMAT)
    @Mapping(target = "state", source = "event.eventState")
    @Mapping(target = "initiator", source = "event.initiator")
    @Mapping(target = "category", source = "event.category")
    @Mapping(target = "location", source = "event.location")
    @Mapping(target = "confirmedRequests", source = "event.confirmedRequests")
    @Mapping(target = "views", ignore = true)
    EventFullDto mapToEventFullDto(Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "eventState", ignore = true)
    @Mapping(target = "eventDate", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "annotation", source = "request.annotation")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "location", source = "request.location")
    @Mapping(target = "paid", source = "request.paid")
    @Mapping(target = "participantLimit", source = "request.participantLimit")
    @Mapping(target = "requestModeration", source = "request.requestModeration")
    @Mapping(target = "title", source = "request.title")
    void updateFromRequestUser(UpdateEventUserRequest request, @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "eventState", ignore = true)
    @Mapping(target = "eventDate", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "annotation", source = "annotation")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "paid", source = "paid")
    @Mapping(target = "participantLimit", source = "participantLimit")
    @Mapping(target = "requestModeration", source = "requestModeration")
    @Mapping(target = "title", source = "title")
    void updateFromRequestAdmin(UpdateEventAdminRequest request, @MappingTarget Event event);

    List<EventFullDto> toFullDtoList(List<Event> eventList);

    List<EventShortDto> toShortDtoList(List<Event> eventList);
}



