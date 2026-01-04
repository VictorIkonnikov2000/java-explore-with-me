package request;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import request.dto.ParticipationRequestDto;

import java.util.Collection;

@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable Long userId,
                                          @RequestParam(required = false) Long eventId) {
        return requestService.saveRequest(userId, eventId);
    }

    @GetMapping
    public Collection<ParticipationRequestDto> getRequests(@PathVariable Long userId) {
        return requestService.getRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto requestUpdate(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        return requestService.requestUpdate(userId, requestId);
    }
}
