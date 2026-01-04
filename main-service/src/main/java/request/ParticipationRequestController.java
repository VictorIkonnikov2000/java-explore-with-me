package request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import request.dto.EventRequestStatusUpdateRequest;
import request.dto.EventRequestStatusUpdateResult;
import request.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class ParticipationRequestController {

    private final ParticipationRequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getParticipationRequests(@PathVariable Long userId) {
        return requestService.getParticipationRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createParticipationRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        return requestService.createParticipationRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelParticipationRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return requestService.cancelParticipationRequest(userId, requestId);
    }

    @PatchMapping("/status")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId, @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return requestService.updateRequestStatus(userId, updateRequest);
    }
}
