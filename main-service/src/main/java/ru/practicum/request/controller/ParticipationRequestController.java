package ru.practicum.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.ParticipationRequestService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
public class ParticipationRequestController {

    private final ParticipationRequestService participationRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable @Positive Long userId,
                                          @RequestParam(required = false) Long eventId) {
        log.info("Private: Запрос на создание заявки на участие. UserID={}, EventID={}", userId, eventId);
        return participationRequestService.addRequest(userId, eventId);
    }

    @GetMapping
    public Collection<ParticipationRequestDto> getRequests(@PathVariable @Positive Long userId) {
        log.info("Private: Получение заявок пользователя id={}", userId);
        return participationRequestService.getRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto requestUpdate(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        log.info("Private: Отмена заявки id={} пользователем id={}", requestId, userId);
        return participationRequestService.requestUpdate(userId, requestId);
    }
}

