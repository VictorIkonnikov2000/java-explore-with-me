package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.error.exceptions.BadRequestException;
import ru.practicum.error.exceptions.ConflictException;
import ru.practicum.error.exceptions.ForbiddenException;
import ru.practicum.error.exceptions.NotFoundException;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.event.model.EventState.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventMapper eventMapper;
    private final EventRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;

    private static final String EVENT_PATH_PREFIX = "/events/";
    private static final String APP_NAME = "ewm-main-service";

    @Override
    @Transactional
    public EventFullDto saveEvent(Long userId, NewEventDto request) {
        User user = isContainsUser(userId);
        if (request == null) {
            throw new BadRequestException("Запрос на добавление нового события не может быть null");
        }
        checkEventDate(request.getEventDate());
        Category category = isContainsCategory(request.getCategory());

        Event event = eventMapper.mapToEvent(request);
        event.setInitiator(user);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setEventState(PENDING);

        return eventMapper.mapToEventFullDto(repository.save(event));
    }

    @Override
    public Collection<EventShortDto> getEventsUser(Long userId, int from, int size) {
        User user = isContainsUser(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        return eventMapper.toShortDtoList(repository.findByInitiator(user, pageable).getContent());
    }

    @Override
    public EventFullDto getEventUser(Long userId, Long eventId) {
        isContainsUser(userId);
        Event event = checkEventForUserAffiliation(userId, eventId);
        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        if (request == null) {
            throw new BadRequestException("Запрос на обновление события не может быть null");
        }
        isContainsUser(userId);
        Event event = checkEventForUserAffiliation(userId, eventId);
        checkEventCanBeUpdated(event);

        eventMapper.updateFromRequestUser(request, event);

        if (request.getCategory() != null) {
            event.setCategory(isContainsCategory(request.getCategory()));
        }

        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ForbiddenException("Событие не удовлетворяет правилам редактирования");
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setEventState(PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setEventState(CANCELED);
                    break;
                default:
                    throw new BadRequestException("Неизвестное значение: " + request.getStateAction());
            }
        }
        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    public Collection<EventFullDto> getEventsForParameters(Collection<Long> users, Collection<EventState> states,
                                                           Collection<Long> categories, LocalDateTime rangeStart,
                                                           LocalDateTime rangeEnd, int from, int size) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("rangeStart не может быть позже rangeEnd");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> events = repository.findByParameters(
                (users != null && users.isEmpty()) ? null : users,
                (states != null && states.isEmpty()) ? null : states,
                (categories != null && categories.isEmpty()) ? null : categories,
                rangeStart, rangeEnd, pageable);

        return loadStatForList(events.getContent(), true);
    }

    @Override
    @Transactional
    public EventFullDto eventUpdateAdmin(Long eventId, UpdateEventAdminRequest request) {
        if (request == null) {
            throw new BadRequestException("Запрос на обновление события не может быть null");
        }
        Event event = isContainsEvent(eventId);
        eventMapper.updateFromRequestAdmin(request, event);

        if (request.getCategory() != null) {
            event.setCategory(isContainsCategory(request.getCategory()));
        }

        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }

        if (event.getPublishedOn() != null && request.getEventDate() != null) {
            checkEventCanBeUpdatedAdmin(event);
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getEventState() != PENDING) {
                        throw new ConflictException("Событие можно публиковать, только если оно в состоянии ожидания");
                    }
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                        throw new ConflictException("Событие должно начинаться не ранее чем через 1 час");
                    }
                    event.setPublishedOn(LocalDateTime.now());
                    event.setEventState(PUBLISHED);
                    break;
                case REJECT_EVENT:
                    if (event.getEventState() == PUBLISHED) {
                        throw new ConflictException("Событие можно отклонить, только если оно не опубликовано");
                    }
                    event.setEventState(CANCELED);
                    break;
                default:
                    throw new BadRequestException("Неизвестное значение: " + request.getStateAction());
            }
        }
        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto getEvent(Long eventId, HttpServletRequest servletRequest) {
        Event event = repository.findByIdAndEventState(eventId, PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        String uri = EVENT_PATH_PREFIX + eventId;
        sendHit(servletRequest, uri);

        Long views = loadViews(event, uri, true);
        EventFullDto eventFullDto = eventMapper.mapToEventFullDto(event);
        eventFullDto.setViews(views);

        return eventFullDto;
    }

    @Override
    public Collection<EventFullDto> getEventsPublic(String text, Collection<Long> categories, Boolean paid,
                                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, boolean onlyAvailable,
                                                    String sort, int from, int size, HttpServletRequest request) {

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("Дата окончания не может быть раньше даты начала");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime startSearch = (rangeStart == null) ? LocalDateTime.now() : rangeStart;

        Page<Event> eventPage = repository.findByParametersForPublicController(
                text, (categories != null && categories.isEmpty()) ? null : categories,
                startSearch, rangeEnd, paid, onlyAvailable, pageable);

        sendHit(request, request.getRequestURI());

        List<EventFullDto> eventFullDtoList = loadStatForList(eventPage.getContent(), true);

        if ("VIEWS".equals(sort)) {
            eventFullDtoList.sort(Comparator.comparing(EventFullDto::getViews).reversed());
        } else if ("EVENT_DATE".equals(sort)) {
            eventFullDtoList.sort(Comparator.comparing(EventFullDto::getEventDate).reversed());
        }

        return eventFullDtoList;
    }

    // --- Private Search/Check Methods ---

    private User isContainsUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + id + " не найден"));
    }

    private Event isContainsEvent(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id: " + id + " не найдено"));
    }

    private Category isContainsCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id: " + id + " не найдена"));
    }

    private Event checkEventForUserAffiliation(Long userId, Long eventId) {
        Event event = isContainsEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие c id=" + eventId + " пользователя id=" + userId + " не найдено");
        }
        return event;
    }

    private void checkEventDate(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Дата события должна быть в будущем");
        }
    }

    private void checkEventCanBeUpdated(Event event) {
        if (event.getEventState() == PUBLISHED) {
            throw new ConflictException("Опубликованное событие нельзя изменить");
        }
    }

    private void checkEventCanBeUpdatedAdmin(Event event) {
        if (event.getEventDate().isBefore(event.getPublishedOn().plusHours(1))) {
            throw new ConflictException("Дата начала события должна быть не ранее чем через час после публикации");
        }
    }

    private void sendHit(HttpServletRequest request, String uri) {
        statsClient.hit(EndpointHitDto.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }

    private Long loadViews(Event event, String uri, boolean unique) {
        if (event.getPublishedOn() == null) return 0L;
        List<ViewStatsDto> stats = statsClient.getStats(event.getPublishedOn(), LocalDateTime.now(), List.of(uri), unique);
        return (stats != null && !stats.isEmpty()) ? stats.get(0).getHits() : 0L;
    }

    private List<EventFullDto> loadStatForList(List<Event> eventList, boolean unique) {
        if (eventList.isEmpty()) return Collections.emptyList();

        List<String> uris = eventList.stream()
                .map(e -> EVENT_PATH_PREFIX + e.getId())
                .collect(Collectors.toList());

        LocalDateTime start = eventList.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusYears(10));

        List<ViewStatsDto> stats = statsClient.getStats(start, LocalDateTime.now(), uris, unique);
        Map<String, Long> viewsMap = stats.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));

        return eventList.stream()
                .map(e -> {
                    EventFullDto dto = eventMapper.mapToEventFullDto(e);
                    dto.setViews(viewsMap.getOrDefault(EVENT_PATH_PREFIX + e.getId(), 0L));
                    return dto;
                }).collect(Collectors.toList());
    }
}
