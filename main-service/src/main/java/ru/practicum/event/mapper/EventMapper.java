package ru.practicum.event.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.event.model.EventState.PENDING;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    public EventShortDto toEventShortDto(Event event) {
        if (event == null) return null;

        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .initiator(userMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(0L) // По умолчанию 0, заполняется в сервисе
                .build();
    }

    public Event toEvent(NewEventDto request) {
        if (request == null) return null;

        return Event.builder()
                .annotation(request.getAnnotation())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .location(mapToLocation(request.getLocation()))
                .paid(request.getPaid())
                .participantLimit(request.getParticipantLimit())
                .requestModeration(request.getRequestModeration())
                .title(request.getTitle())
                .createdOn(LocalDateTime.now())
                .confirmedRequests(0L)
                .eventState(PENDING)
                .build();
    }

    public EventFullDto toEventFullDto(Event event) {
        if (event == null) return null;

        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(userMapper.toUserShortDto(event.getInitiator()))
                .location(toLocationDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getEventState())
                .title(event.getTitle())
                .views(0L) // Заполняется отдельно
                .build();
    }

    public void updateFromRequestUser(UpdateEventUserRequest request, Event event) {
        if (request == null) return;

        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getLocation() != null) event.setLocation(mapToLocation(request.getLocation()));
        // Поля category, stateAction и eventDate обрабатываются в сервисе,
        // так как требуют обращения к БД или специфичной бизнес-логики
    }

    public void updateFromRequestAdmin(UpdateEventAdminRequest request, Event event) {
        if (request == null) return;

        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getLocation() != null) event.setLocation(mapToLocation(request.getLocation()));
        // Поля category, stateAction и eventDate обрабатываются в сервисе
    }

    public List<EventFullDto> toFullDtoList(List<Event> eventList) {
        return eventList.stream().map(this::toEventFullDto).collect(Collectors.toList());
    }

    public List<EventShortDto> toShortDtoList(List<Event> eventList) {
        return eventList.stream().map(this::toEventShortDto).collect(Collectors.toList());
    }

    // Вспомогательные методы для Location (Embedded в Entity vs DTO)
    private ru.practicum.event.model.Location mapToLocation(LocationDto dto) {
        if (dto == null) return null;
        return ru.practicum.event.model.Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    private LocationDto toLocationDto(ru.practicum.event.model.Location location) {
        if (location == null) return null;
        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}
