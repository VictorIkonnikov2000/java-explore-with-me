package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.category.model.Category;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.event.model.EventState.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventMapper eventMapper;
    private final EventRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    private final StatsClient statsClient;

    private static final String EVENT = "/events/";

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

        Event saveEvent = repository.save(event);

        return eventMapper.mapToEventFullDto(saveEvent);
    }

    @Override
    public Collection<EventShortDto> getEventsUser(Long userId, int from, int size) {
        User user = isContainsUser(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> eventPage = repository.findByInitiator(user, pageable);
        List<Event> eventList = eventPage.getContent();
        return eventMapper.toShortDtoList(eventList);
    }

    @Override
    public EventFullDto getEventUser(Long userId, Long eventId) {
        User user = isContainsUser(userId);
        Event event = checkEventForUserAffiliation(userId, eventId);
        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest request) {

        if (request == null) {
            throw new BadRequestException("Запрос на обновление события не может быть null");
        }

        User user = isContainsUser(userId);
        Event event = checkEventForUserAffiliation(userId, eventId);
        checkEventCanBeUpdated(event);
        eventMapper.updateFromRequestUser(request, event);

        if (request.getCategory() != null) {
            Category category = isContainsCategory(request.getCategory());
            event.setCategory(category);
        }

        if (request.getEventDate() != null) {
            LocalDateTime dateTime = LocalDateTime.now().plusHours(2);

            if (request.getEventDate().isBefore(dateTime)) {
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

        Collection<Long> usersId = (users == null || users.isEmpty()) ? null : users;
        Collection<Long> categoriesId = (categories == null || categories.isEmpty()) ? null : categories;
        Collection<EventState> stateValid = (states == null || states.isEmpty()) ? null : states;

        if ((rangeStart != null && rangeEnd != null) && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("rangeStart не может быть позже rangeEnd");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> events = repository.findByParameters(usersId, stateValid, categoriesId,
                rangeStart, rangeEnd, pageable);

        List<Event> eventList = events.getContent();

        return loadStatForList(eventList, true);
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
            Category category = isContainsCategory(request.getCategory());
            event.setCategory(category);
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
                        throw new ConflictException("Событие можно публиковать, только если оно в состоянии ожидания публикации");
                    }

                    LocalDateTime minEventDate = LocalDateTime.now().plusHours(1);

                    if (event.getEventDate().isBefore(minEventDate)) {
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
        Optional<Event> eventOpt = repository.findByIdAndEventState(eventId, PUBLISHED);

        if (eventOpt.isEmpty()) {
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        }

        String uri = EVENT + eventId;

        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(uri)
                .ip(servletRequest.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.hit(endpointHitDto);
        Event event = eventOpt.get();

        Long views = loadViews(event, uri, true);

        EventFullDto eventFullDto = eventMapper.mapToEventFullDto(event);
        eventFullDto.setViews(views);

        return eventFullDto;
    }

    @Override
    public Collection<EventFullDto> getEventsPublic(String text, Collection<Long> categories, Boolean paid,
                                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, boolean onlyAvailable,
                                                    String sort, int from, int size, HttpServletRequest request) {

        if ((rangeStart != null && rangeEnd != null) && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("Дата окончания не может быть раньше даты начала");
        }

        Collection<Long> categoryId = (categories != null && !categories.isEmpty()) ? categories : null;
        LocalDateTime dataTime = (rangeStart == null) ? LocalDateTime.now() : rangeStart;

        Pageable pageable = PageRequest.of(from / size, size);

        Page<Event> eventPage = repository.findByParametersForPublicController(text, categoryId, dataTime,
                rangeEnd, paid, onlyAvailable, pageable);

        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.hit(endpointHitDto);
        List<EventFullDto> eventFullDtoList = loadStatForList(eventPage.getContent(), true);

        if ("VIEWS".equals(sort)) {
            eventFullDtoList.sort(Comparator.comparing(EventFullDto::getViews).reversed());
        } else if ("EVENT_DATE".equals(sort)) {
            eventFullDtoList.sort(Comparator.comparing(EventFullDto::getEventDate).reversed());
        }

        return eventFullDtoList;
    }

    private Event checkEventForUserAffiliation(Long userId, Long eventId) {
        Event event = isContainsEvent(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие c eventId = " + eventId + " , пользователя с userId " + userId + " не найдено");
        }

        return event;
    }

    private Event isContainsEvent(Long id) {
        Optional<Event> optEvent = repository.findById(id);

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

    private Category isContainsCategory(Long id) {
        Optional<Category> optCategories = categoryRepository.findById(id);

        if (optCategories.isEmpty()) {
            throw new NotFoundException("Категория с id: " + id + " в базе отсутствует");
        }

        return optCategories.get();
    }

    private void checkEventDate(LocalDateTime eventDate) {
        LocalDateTime dateTime = LocalDateTime.now().plusHours(2);

        if (eventDate.isBefore(dateTime)) {
            throw new ConflictException("Событие должно быть запланировано не ранее чем за 2 часа до начала");
        }
    }

    private void checkEventCanBeUpdated(Event event) {

        if (event.getEventState() != CANCELED &&
                event.getEventState() != PENDING) {
            throw new ConflictException("Событие не удовлетворяет правилам редактирования");
        }
    }

    private void checkEventCanBeUpdatedAdmin(Event event) {

        if (event.getPublishedOn() == null) {
            return;
        }

        if (event.getEventDate().isBefore(event.getPublishedOn().plusHours(1))) {
            throw new ConflictException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации.");
        }
    }

    private Long loadViews(Event event, String uri, boolean unique) {
        List<ViewStatsDto> stats = statsClient.getStats(event.getPublishedOn(), LocalDateTime.now(),
                List.of(uri), unique);

        Long views;

        if (stats != null && !stats.isEmpty()) {
            views = stats.get(0).getHits();
        } else {
            views = 0L;
        }

        return views;
    }

    private List<EventFullDto> loadStatForList(List<Event> eventList, boolean unique) {

        List<Event> publisherEvent = eventList.stream()
                .filter(event -> event.getEventState() == PUBLISHED && event.getPublishedOn() != null)
                .toList();

        List<String> uris = publisherEvent.stream()
                .map(event -> EVENT + event.getId())
                .toList();

        Optional<LocalDateTime> minPublished = publisherEvent.stream()
                .map(Event::getPublishedOn)
                .min(LocalDateTime::compareTo);

        Map<String, Long> viewsEvents;

        if (!uris.isEmpty() && minPublished.isPresent()) {
            List<ViewStatsDto> stats = statsClient.getStats(
                    minPublished.get(),
                    LocalDateTime.now(),
                    uris,
                    unique
            );

            viewsEvents = stats.stream()
                    .collect(Collectors.toMap(
                            ViewStatsDto::getUri,
                            ViewStatsDto::getHits
                    ));
        } else {
            viewsEvents = Map.of();
        }

        return eventList.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.mapToEventFullDto(event);

                    if (event.getEventState() == PUBLISHED && event.getPublishedOn() != null) {
                        String uri = EVENT + event.getId();
                        Long views = viewsEvents.getOrDefault(uri, 0L);
                        dto.setViews(views);
                    } else {
                        dto.setViews(0L);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
}