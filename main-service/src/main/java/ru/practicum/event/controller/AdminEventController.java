package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;

import static ru.practicum.constans.StandardDateTimeFormats.DATE_TIME_FORMAT;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public Collection<EventFullDto> getEventsForParameters(@RequestParam(required = false) Collection<Long> users,
                                                           @RequestParam(required = false) Collection<EventState> states,
                                                           @RequestParam(required = false) Collection<Long> categories,
                                                           @RequestParam(required = false)
                                                           @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                                           LocalDateTime rangeStart,
                                                           @RequestParam(required = false)
                                                           @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                                           LocalDateTime rangeEnd,
                                                           @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                           @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        return eventService.getEventsForParameters(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto eventUpdateAdmin(@PathVariable Long eventId,
                                         @RequestBody @Valid UpdateEventAdminRequest request) {
        return eventService.eventUpdateAdmin(eventId, request);
    }
}