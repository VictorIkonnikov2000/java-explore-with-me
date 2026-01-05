package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.ParticipationRequestService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
public class ParticipationRequestController {

    private final ParticipationRequestService participationRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable Long userId,
                                          @RequestParam(required = false) Long eventId) {
        return participationRequestService.addRequest(userId, eventId);
    }

    @GetMapping
    public Collection<ParticipationRequestDto> getRequests(@PathVariable Long userId) {
        return participationRequestService.getRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto requestUpdate(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        return participationRequestService.requestUpdate(userId, requestId);
    }
}
