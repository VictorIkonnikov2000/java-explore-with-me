package request;

import request.dto.EventRequestStatusUpdateRequest;
import request.dto.EventRequestStatusUpdateResult;
import request.dto.ParticipationRequestDto;

import java.util.Collection;

public interface RequestService {

    ParticipationRequestDto saveRequest(Long userId, Long eventId);

    Collection<ParticipationRequestDto> getRequests(Long userId);

    ParticipationRequestDto requestUpdate(Long userId, Long requestId);

    Collection<ParticipationRequestDto> getRequestUser(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest request);
}
