package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class EventPrivateController {

    private final EventService eventService;
    private final ParticipationRequestService participationRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                               @RequestBody @Valid NewEventDto request) {
        return eventService.saveEvent(userId, request);
    }

    @GetMapping
    public Collection<EventShortDto> getEventsUser(@PathVariable Long userId,
                                                   @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                   @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        return eventService.getEventsUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventUser(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getEventUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventUser(@PathVariable Long userId,
                                        @PathVariable Long eventId,
                                        @RequestBody @Valid UpdateEventUserRequest request) {

        return eventService.updateEventUser(userId, eventId, request);
    }

    @GetMapping("/{eventId}/requests")
    public Collection<ParticipationRequestDto> getRequestUser(@PathVariable Long userId,
                                                              @PathVariable Long eventId) {
        return participationRequestService.getRequestUser(userId, eventId);
    }

    @PatchMapping("{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        return participationRequestService.updateRequestStatus(userId, eventId, request);
    }
}