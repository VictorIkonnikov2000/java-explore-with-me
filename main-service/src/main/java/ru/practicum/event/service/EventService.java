package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.Collection;

public interface EventService {
    EventFullDto saveEvent(Long userId, NewEventDto request);

    Collection<EventShortDto> getEventsUser(Long userId, int from, int size);

    EventFullDto getEventUser(Long userId, Long eventId);

    EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest request);

    Collection<EventFullDto> getEventsForParameters(Collection<Long> users, Collection<EventState> states,
                                                    Collection<Long> categories, LocalDateTime rangeStart,
                                                    LocalDateTime rangeEnd, int from, int size);

    EventFullDto eventUpdateAdmin(Long eventId, UpdateEventAdminRequest request);

    EventFullDto getEvent(Long eventId, HttpServletRequest servletRequest);

    Collection<EventFullDto> getEventsPublic(String text, Collection<Long> categories, Boolean paid,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, boolean onlyAvailable,
                                             String sort, int from, int size, HttpServletRequest request);
}