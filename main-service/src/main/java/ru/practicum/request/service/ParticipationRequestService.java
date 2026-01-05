package ru.practicum.request.service;

import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.Collection;

public interface ParticipationRequestService {

    ParticipationRequestDto saveRequest(Long userId, Long eventId);

    Collection<ParticipationRequestDto> getRequests(Long userId);

    ParticipationRequestDto requestUpdate(Long userId, Long requestId);

    Collection<ParticipationRequestDto> getRequestUser(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest request);
}