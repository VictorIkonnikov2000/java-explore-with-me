package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;

import static ru.practicum.constans.StandardDateTimeFormats.DATE_TIME_FORMAT;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Validated
public class EventController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable Long eventId,
                                 HttpServletRequest request) {
        return eventService.getEvent(eventId, request);
    }

    @GetMapping
    public Collection<EventFullDto> getEventsPublic(@RequestParam(required = false) String text,
                                                    @RequestParam(required = false) Collection<Long> categories,
                                                    @RequestParam(required = false) Boolean paid,
                                                    @RequestParam(required = false)
                                                    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                                    LocalDateTime rangeStart,
                                                    @RequestParam(required = false)
                                                    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                                    LocalDateTime rangeEnd,
                                                    @RequestParam(defaultValue = "false") boolean onlyAvailable,
                                                    @RequestParam(required = false) String sort,
                                                    @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                    @RequestParam(defaultValue = "10") @Min(1) Integer size,
                                                    HttpServletRequest request) {

        return eventService.getEventsPublic(text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }
}
