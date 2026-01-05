package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.error.exceptions.BadRequestException;
import ru.practicum.error.exceptions.ConflictException;
import ru.practicum.error.exceptions.NotFoundException;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.ParticipationRequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.ParticipationRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.request.model.RequestStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationRequestService {

    private final ParticipationRequestRepository repository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    public ParticipationRequestDto saveRequest(Long userId, Long eventId) {
        if (eventId == null) {
            throw new BadRequestException("Идентификатор события не может быть пустым.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор не может подавать заявку на участие в собственном событии.");
        }

        if (event.getEventState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии.");
        }

        if (event.getParticipantLimit() > 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников данного события исчерпан.");
        }

        if (repository.existsByEventAndRequesterAndStatusNot(event, user, CANCELED)) {
            throw new ConflictException("Запрос от данного пользователя уже существует.");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .requester(user)
                .event(event)
                .status(PENDING)
                .build();

        // Если модерация не требуется или лимит равен 0 (безлимит)
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(CONFIRMED);
            if (eventRepository.incrementConfirmedRequestsIfWithinLimit(eventId) == 0) {
                throw new ConflictException("Не удалось подтвердить участие: достигнут лимит.");
            }
        }

        return participationRequestMapper.toParticipationRequestDto(repository.save(request));
    }

    @Transactional(readOnly = true)
    public Collection<ParticipationRequestDto> getRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));

        return repository.findByRequester(user).stream()
                .map(participationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    public ParticipationRequestDto requestUpdate(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));

        ParticipationRequest request = repository.findByIdAndRequester(requestId, user)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден для данного пользователя."));

        if (request.getStatus() == CONFIRMED) {
            eventRepository.decrementConfirmedRequests(request.getEvent().getId());
        }

        request.setStatus(CANCELED);
        return participationRequestMapper.toParticipationRequestDto(repository.save(request));
    }

    @Transactional(readOnly = true)
    public Collection<ParticipationRequestDto> getRequestUser(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));

        Event event = eventRepository.findByIdAndInitiator(eventId, user)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено у пользователя " + userId));

        return repository.findByEvent(event).stream()
                .map(participationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Данные для обновления статуса отсутствуют.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));

        Event event = eventRepository.findByIdAndInitiator(eventId, user)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено у текущего пользователя."));

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Данному событию не требуется подтверждение заявок.");
        }

        if (event.getParticipantLimit() > 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников события уже заполнен.");
        }

        EventRequestStatusUpdateResult requestResult = new EventRequestStatusUpdateResult();
        List<ParticipationRequest> validRequests;

        if (request.getRequestIds() != null && !request.getRequestIds().isEmpty()) {
            validRequests = repository.findValidRequestsForEvent(request.getRequestIds(), eventId, PENDING);
        } else {
            validRequests = repository.findByEventIdAndStatus(eventId, PENDING);
        }

        long availableSeats = event.getParticipantLimit() - event.getConfirmedRequests();

        if (request.getStatus() == REJECTED) {
            validRequests.forEach(req -> {
                req.setStatus(REJECTED);
                requestResult.getRejectedRequests().add(participationRequestMapper.toParticipationRequestDto(req));
            });
        } else if (request.getStatus() == CONFIRMED) {
            for (ParticipationRequest req : validRequests) {
                if (availableSeats > 0) {
                    req.setStatus(CONFIRMED);
                    eventRepository.incrementConfirmedRequestsIfWithinLimit(eventId);
                    requestResult.getConfirmedRequests().add(participationRequestMapper.toParticipationRequestDto(req));
                    availableSeats--;
                } else {
                    req.setStatus(REJECTED);
                    requestResult.getRejectedRequests().add(participationRequestMapper.toParticipationRequestDto(req));
                }
            }
        } else {
            throw new BadRequestException("Указан некорректный статус: " + request.getStatus());
        }

        repository.saveAll(validRequests);
        return requestResult;
    }
}
