package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationRequestService {

    private final ParticipationRequestRepository repository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        if (eventId == null) {
            log.error("Попытка создания заявки без ID события (userId={})", userId);
            throw new BadRequestException("Идентификатор события не может быть пустым.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Событие id={} не найдено при создании заявки", eventId);
                    return new NotFoundException("Событие с id=" + eventId + " не найдено.");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь id={} не найден при создании заявки", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден.");
                });

        if (event.getInitiator().getId().equals(userId)) {
            log.warn("Конфликт: пользователь id={} инициатор события id={}", userId, eventId);
            throw new ConflictException("Инициатор не может подавать заявку на участие в собственном событии.");
        }

        if (event.getEventState() != EventState.PUBLISHED) {
            log.warn("Конфликт: событие id={} еще не опубликовано", eventId);
            throw new ConflictException("Нельзя участвовать в неопубликованном событии.");
        }

        if (event.getParticipantLimit() > 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            log.warn("Конфликт: лимит заявок исчерпан для события id={}", eventId);
            throw new ConflictException("Лимит участников данного события исчерпан.");
        }

        if (repository.existsByEventAndRequesterAndStatusNot(event, user, CANCELED)) {
            log.warn("Конфликт: дубликат заявки от пользователя id={} на событие id={}", userId, eventId);
            throw new ConflictException("Запрос от данного пользователя уже существует.");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .requester(user)
                .event(event)
                .status(PENDING)
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(CONFIRMED);
            log.debug("Автоматическое подтверждение заявки для пользователя id={}", userId);
            if (eventRepository.incrementConfirmedRequestsIfWithinLimit(eventId) == 0) {
                log.error("Гонка данных: не удалось инкрементировать счетчик для события id={}", eventId);
                throw new ConflictException("Не удалось подтвердить участие: достигнут лимит.");
            }
        }

        log.info("Создана новая заявка на участие: id={}, статус={}", request.getId(), request.getStatus());
        return participationRequestMapper.toParticipationRequestDto(repository.save(request));
    }

    @Transactional(readOnly = true)
    public Collection<ParticipationRequestDto> getRequests(Long userId) {
        log.info("Получение всех заявок пользователя id={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));

        return repository.findByRequester(user).stream()
                .map(participationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    public ParticipationRequestDto requestUpdate(Long userId, Long requestId) {
        log.info("Отмена заявки id={} пользователем id={}", requestId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));

        ParticipationRequest request = repository.findByIdAndRequester(requestId, user)
                .orElseThrow(() -> {
                    log.warn("Заявка id={} не найдена для пользователя id={}", requestId, userId);
                    return new NotFoundException("Запрос с id=" + requestId + " не найден для данного пользователя.");
                });

        if (request.getStatus() == CONFIRMED) {
            log.debug("Уменьшение количества подтвержденных заявок для события id={}", request.getEvent().getId());
            eventRepository.decrementConfirmedRequests(request.getEvent().getId());
        }

        request.setStatus(CANCELED);
        return participationRequestMapper.toParticipationRequestDto(repository.save(request));
    }

    @Transactional(readOnly = true)
    public Collection<ParticipationRequestDto> getRequestUser(Long userId, Long eventId) {
        log.info("Получение заявок на событие id={} владельцем id={}", eventId, userId);
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
        log.info("Массовое обновление статуса заявок для события id={}: статус={}", eventId, request.getStatus());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));

        Event event = eventRepository.findByIdAndInitiator(eventId, user)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено у текущего пользователя."));

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            log.warn("Попытка модерации события id={}, где она не требуется", eventId);
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
        log.debug("Доступно мест для события id={}: {}", eventId, availableSeats);

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
            log.error("Указан некорректный статус для обновления: {}", request.getStatus());
            throw new BadRequestException("Указан некорректный статус: " + request.getStatus());
        }

        repository.saveAll(validRequests);
        log.info("Результат обновления: {} подтверждено, {} отклонено",
                requestResult.getConfirmedRequests().size(), requestResult.getRejectedRequests().size());
        return requestResult;
    }
}
