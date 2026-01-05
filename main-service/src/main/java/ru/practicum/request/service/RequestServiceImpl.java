package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.error.exceptions.BadRequestException;
import ru.practicum.error.exceptions.ConflictException;
import ru.practicum.error.exceptions.NotFoundException;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.*;

import static ru.practicum.request.model.RequestStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {

    private final RequestRepository repository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Override
    public ParticipationRequestDto saveRequest(Long userId, Long eventId) {

        if (eventId == null) {
            throw new BadRequestException("Id события должно быть указано");
        }

        Event event = isContainsEvent(eventId);
        User user = isContainsUser(userId);

        if (event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictException("Инициатор события не может подать заявку в своё событие");
        }

        if (event.getEventState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя подать запрос на участие в не опубликованном событии");
        }

        checkLimitConfirmedRequest(event);

        if (repository.existsByEventAndRequesterAndStatusNot(event, user, CANCELED)) {
            throw new ConflictException("Нельзя подать повторный запрос на участие в событии");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .requester(user)
                .event(event)
                .status(PENDING)
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(CONFIRMED);
            int up = eventRepository.incrementConfirmedRequestsIfWithinLimit(eventId);

            if (up == 0) {
                throw new ConflictException("Достигнут лимит участников");
            }
        }

        repository.save(request);

        return requestMapper.mapToParticipationRequestDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ParticipationRequestDto> getRequests(Long userId) {
        User user = isContainsUser(userId);
        Collection<ParticipationRequest> requests = repository.findByRequester(user);
        List<ParticipationRequest> requestList = new ArrayList<>(requests);

        return requestMapper.toFullDtoList(requestList);
    }

    @Override
    public ParticipationRequestDto requestUpdate(Long userId, Long requestId) {
        User user = isContainsUser(userId);

        Optional<ParticipationRequest> requestOpt = repository.findByIdAndRequester(requestId, user);

        if (requestOpt.isEmpty()) {
            throw new NotFoundException("Запрос на участие с id: " + requestId + " для пользователя с id " + userId + " в базе отсутствует");
        }

        ParticipationRequest request = requestOpt.get();

        if (request.getStatus() == CONFIRMED) {
            eventRepository.decrementConfirmedRequests(request.getEvent().getId());
        }

        request.setStatus(CANCELED);

        return requestMapper.mapToParticipationRequestDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ParticipationRequestDto> getRequestUser(Long userId, Long eventId) {
        User user = isContainsUser(userId);
        Event event = checkEventForUser(user, eventId);

        Collection<ParticipationRequest> requests = repository.findByEvent(event);
        List<ParticipationRequest> requestList = new ArrayList<>(requests);

        return requestMapper.toFullDtoList(requestList);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        User user = isContainsUser(userId);
        Event event = checkEventForUser(user, eventId);

        if (request == null) {
            throw new BadRequestException("Запрос на изменения статуса поданных заявок не может быть равен null");
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Подтверждения заявок для данного события не требуется");
        }

        checkLimitConfirmedRequest(event);
        EventRequestStatusUpdateResult requestResult = new EventRequestStatusUpdateResult();
        List<ParticipationRequest> validRequests;
        long availableSeats = event.getParticipantLimit() - event.getConfirmedRequests();

        if (!request.getRequestIds().isEmpty()) {
            validRequests = repository.findValidRequestsForEvent(request.getRequestIds(),
                    eventId, PENDING);
        } else {
            validRequests = repository.findByEventIdAndStatus(eventId, PENDING);
        }

        int toProcess = Math.min(validRequests.size(), (int) availableSeats);

        switch (request.getStatus()) {
            case CONFIRMED:
                for (int i = 0; i < toProcess; i++) {
                    validRequests.get(i).setStatus(CONFIRMED);
                    eventRepository.incrementConfirmedRequestsIfWithinLimit(eventId);
                    requestResult.getConfirmedRequests().add(requestMapper.mapToParticipationRequestDto(validRequests.get(i)));
                }

                for (int i = toProcess; i < validRequests.size(); i++) {
                    validRequests.get(i).setStatus(REJECTED);
                    requestResult.getRejectedRequests().add(requestMapper.mapToParticipationRequestDto(validRequests.get(i)));
                }
                break;
            case REJECTED:
                for (int i = 0; i < validRequests.size(); i++) {
                    validRequests.get(i).setStatus(REJECTED);
                    requestResult.getRejectedRequests().add(requestMapper.mapToParticipationRequestDto(validRequests.get(i)));
                }
                break;
            default:
                throw new BadRequestException("Неизвестное значение статуса");
        }
        repository.saveAll(validRequests);

        return requestResult;
    }

    private void checkLimitConfirmedRequest(Event event) {

        if (event.getParticipantLimit() > 0 &&
                event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит поданных подтверждённых заявок на участие в событии");
        }
    }

    private Event checkEventForUser(User user, Long eventId) {
        Optional<Event> eventOpt = eventRepository.findByIdAndInitiator(eventId, user);

        if (eventOpt.isEmpty()) {
            throw new NotFoundException("Событие созданное пользователем с id " + user.getId() + " не найдено");
        }

        return eventOpt.get();
    }

    private Event isContainsEvent(Long id) {
        Optional<Event> optEvent = eventRepository.findById(id);

        if (optEvent.isEmpty()) {
            throw new NotFoundException("Событие с id: " + id + " в базе отсутствует");
        }

        return optEvent.get();
    }

    private User isContainsUser(Long id) {
        Optional<User> optUser = userRepository.findById(id);

        if (optUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id: " + id + " в базе отсутствует");
        }

        return optUser.get();
    }

    private ParticipationRequest isContainsRequest(Long id) {
        Optional<ParticipationRequest> optRequest = repository.findById(id);

        if (optRequest.isEmpty()) {
            throw new NotFoundException("Запрос на участие с id: " + id + " в базе отсутствует");
        }

        return optRequest.get();
    }
}