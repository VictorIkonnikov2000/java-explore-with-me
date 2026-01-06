package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.ParticipationRequestService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {

    private final EventService eventService;
    private final ParticipationRequestService participationRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                               @RequestBody @Valid NewEventDto request) {
        log.info("Private: создание события пользователем id={}. Request: {}", userId, request);
        return eventService.addEvent(userId, request);
    }

    @GetMapping
    public Collection<EventShortDto> getEventsUser(@PathVariable Long userId,
                                                   @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                   @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        log.info("Private: получение списка событий пользователя id={}", userId);
        return eventService.getEventsUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventUser(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Private: получение события id={} пользователем id={}", eventId, userId);
        return eventService.getEventUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventUser(@PathVariable Long userId,
                                        @PathVariable Long eventId,
                                        @RequestBody @Valid UpdateEventUserRequest request) {
        log.info("Private: обновление события id={} пользователем id={}. Request: {}", eventId, userId, request);
        return eventService.updateEventUser(userId, eventId, request);
    }

    @GetMapping("/{eventId}/requests")
    public Collection<ParticipationRequestDto> getRequestUser(@PathVariable Long userId,
                                                              @PathVariable Long eventId) {
        log.info("Private: получение заявок на событие id={} владельцем id={}", eventId, userId);
        return participationRequestService.getRequestUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        log.info("Private: изменение статуса заявок события id={} пользователем id={}", eventId, userId);
        return participationRequestService.updateRequestStatus(userId, eventId, request);
    }
}
