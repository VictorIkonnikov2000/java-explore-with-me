package event;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import event.dto.EventFullDto;
import event.dto.EventShortDto;
import event.dto.NewEventDto;
import event.dto.UpdateEventUserRequest;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    //Private
    @PostMapping("/users/{userId}/events")
    public ResponseEntity<EventFullDto> addEvent(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        return new ResponseEntity<>(eventService.addEvent(userId, newEventDto), HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}/events")
    public ResponseEntity<List<EventShortDto>> getEventsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.getEventsByUserId(userId));
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventById(userId, eventId));
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long userId, @PathVariable Long eventId, @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return ResponseEntity.ok(eventService.updateEvent(userId, eventId, updateEventUserRequest));
    }

    //Admin
    //Публичный
}
