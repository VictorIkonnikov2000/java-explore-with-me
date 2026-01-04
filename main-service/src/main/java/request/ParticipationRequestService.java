package request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import request.dto.EventRequestStatusUpdateRequest;
import request.dto.EventRequestStatusUpdateResult;
import request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final ParticipationRequestMapper requestMapper;

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getParticipationRequests(Long userId) {
        return requestRepository.findByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {
        ParticipationRequest request = ParticipationRequest.builder()
                .requesterId(userId)
                .eventId(eventId)
                .status(ParticipationRequest.RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Transactional
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new RuntimeException("Request not found or not owned by user"));

        request.setStatus(ParticipationRequest.RequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(request));
    }


    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, EventRequestStatusUpdateRequest updateRequest) {
        List<ParticipationRequest> requests = requestRepository.findByIdIn(updateRequest.getRequestIds());
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();
        ParticipationRequest.RequestStatus newStatus = ParticipationRequest.RequestStatus.valueOf(updateRequest.getStatus());

        for (ParticipationRequest request : requests) {
            if (!request.getRequesterId().equals(userId)) {
                throw new SecurityException("User is not allowed to update this request.");
            }

            if (newStatus == ParticipationRequest.RequestStatus.CONFIRMED){
                request.setStatus(ParticipationRequest.RequestStatus.CONFIRMED);
                confirmed.add(requestMapper.toDto(request));
            } else {
                request.setStatus(ParticipationRequest.RequestStatus.REJECTED);
                rejected.add(requestMapper.toDto(request));
            }

        }

        requestRepository.saveAll(requests); // Сохраняем все измененные заявки
        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

}
