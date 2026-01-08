package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventMapper eventMapper;
    private final EventRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;

    private static final String EVENT = "/events/";

    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto request) {
        log.info("Добавление нового события пользователем с id={}", userId);

        User user = getUserOrThrow(userId);
        if (request == null) {
            log.error("Ошибка при добавлении события: запрос равен null для userId={}", userId);
            throw new BadRequestException("Запрос на добавление нового события не может быть null");
        }

        validateEventTime(request.getEventDate());
        Category category = getCategoryOrThrow(request.getCategory());

        Event event = eventMapper.toEvent(request);
        event.setInitiator(user);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setEventState(PENDING);

        Event saveEvent = repository.save(event);
        log.info("Событие сохранено с id={}", saveEvent.getId());

        return eventMapper.toEventFullDto(saveEvent);
    }

    public Collection<EventShortDto> getEventsUser(Long userId, int from, int size) {
        log.info("Получение списка событий пользователя с id={} (from={}, size={})", userId, from, size);
        User user = getUserOrThrow(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> eventPage = repository.findByInitiator(user, pageable);
        List<Event> eventList = eventPage.getContent();

        log.debug("Найдено {} событий для пользователя id={}", eventList.size(), userId);
        return eventMapper.toShortDtoList(eventList);
    }

    public EventFullDto getEventUser(Long userId, Long eventId) {
        log.info("Получение полной информации о событии id={} для пользователя id={}", eventId, userId);
        User user = getUserOrThrow(userId);
        Event event = getEventIfBelongsToUser(userId, eventId);
        return eventMapper.toEventFullDto(event);
    }

    @Transactional
    public EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        log.info("Обновление события id={} пользователем id={}", eventId, userId);
        if (request == null) {
            throw new BadRequestException("Запрос на обновление события не может быть null");
        }

        User user = getUserOrThrow(userId);
        Event event = getEventIfBelongsToUser(userId, eventId);
        ensureEventIsPending(event);

        eventMapper.updateFromRequestUser(request, event);

        if (request.getCategory() != null) {
            Category category = getCategoryOrThrow(request.getCategory());
            event.setCategory(category);
        }

        if (request.getEventDate() != null) {
            LocalDateTime dateTime = LocalDateTime.now().plusHours(2);
            if (request.getEventDate().isBefore(dateTime)) {
                log.warn("Попытка установить некорректную дату события: id={}", eventId);
                throw new ForbiddenException("Событие не удовлетворяет правилам редактирования");
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getStateAction() != null) {
            log.info("Смена состояния события id={} на {}", eventId, request.getStateAction());
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setEventState(PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setEventState(CANCELED);
                    break;
            }
        }

        return eventMapper.toEventFullDto(repository.save(event));
    }

    public Collection<EventFullDto> getEventsForParameters(Collection<Long> users, Collection<EventState> states,
                                                           Collection<Long> categories, LocalDateTime rangeStart,
                                                           LocalDateTime rangeEnd, int from, int size) {
        log.info("Поиск событий администратором по фильтрам: users={}, states={}, categories={}", users, states, categories);

        if ((rangeStart != null && rangeEnd != null) && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("rangeStart не может быть позже rangeEnd");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> events = repository.findByParameters(users, states, categories, rangeStart, rangeEnd, pageable);

        log.debug("Администратором найдено {} событий", events.getTotalElements());
        return loadStatForList(events.getContent(), true);
    }

    @Transactional
    public EventFullDto eventUpdateAdmin(Long eventId, UpdateEventAdminRequest request) {
        log.info("Обновление события id={} администратором", eventId);
        Event event = getEventOrThrow(eventId);

        eventMapper.updateFromRequestAdmin(request, event);

        if (request.getCategory() != null) {
            event.setCategory(getCategoryOrThrow(request.getCategory()));
        }

        if (request.getStateAction() != null) {
            log.info("Действие администратора {} для события id={}", request.getStateAction(), eventId);
            switch (request.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getEventState() != PENDING) {
                        throw new ConflictException("Событие можно публиковать только в состоянии ожидания");
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
                        throw new ConflictException("Нельзя отклонить опубликованное событие");
                    }
                    event.setEventState(CANCELED);
                    break;
            }
        }

        return eventMapper.toEventFullDto(repository.save(event));
    }


    public EventFullDto getEvent(Long eventId, HttpServletRequest servletRequest) {
        log.info("Публичный просмотр события id={}, IP={}", eventId, servletRequest.getRemoteAddr());

        Optional<Event> eventOpt = repository.findByIdAndEventState(eventId, PUBLISHED);
        if (eventOpt.isEmpty()) {
            log.warn("Событие id={} не найдено или не опубликовано", eventId);
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        }

        String uri = EVENT + eventId;
        sendHit(servletRequest, uri); // Вынес логику отправки в статистику для чистоты

        Event event = eventOpt.get();
        Long views = loadViews(event, uri, true);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        eventFullDto.setViews(views);

        return eventFullDto;
    }

    public Collection<EventFullDto> getEventsPublic(String text, Collection<Long> categories, Boolean paid,
                                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, boolean onlyAvailable,
                                                    String sort, int from, int size, HttpServletRequest request) {
        log.info("Публичный поиск событий по тексту: '{}'", text);

        if ((rangeStart != null && rangeEnd != null) && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("Дата окончания не может быть раньше даты начала");
        }

        sendHit(request, request.getRequestURI());

        LocalDateTime dataTime = (rangeStart == null) ? LocalDateTime.now() : rangeStart;
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> eventPage = repository.findByParametersForPublicController(text, categories, dataTime,
                rangeEnd, paid, onlyAvailable, pageable);

        List<EventFullDto> eventFullDtoList = loadStatForList(eventPage.getContent(), true);

        if ("VIEWS".equals(sort)) {
            eventFullDtoList.sort(Comparator.comparing(EventFullDto::getViews).reversed());
        } else if ("EVENT_DATE".equals(sort)) {
            eventFullDtoList.sort(Comparator.comparing(EventFullDto::getEventDate).reversed());
        }

        return eventFullDtoList;
    }

    private void sendHit(HttpServletRequest request, String uri) {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(uri)
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.hit(endpointHitDto);
        log.debug("Отправлена статистика просмотра: URI={}, IP={}", uri, request.getRemoteAddr());
    }


    private Event getEventIfBelongsToUser(Long userId, Long eventId) {
        Event event = getEventOrThrow(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие не принадлежит указанному пользователю");
        }
        return event;
    }

    private Event getEventOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
    }

    private void validateEventTime(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Событие должно произойти не ранее чем через 2 часа");
        }
    }

    private void ensureEventIsPending(Event event) {
        if (event.getEventState() == PUBLISHED) {
            throw new ConflictException("Опубликованное событие нельзя изменить");
        }
    }


    private Long loadViews(Event event, String uri, boolean unique) {
        if (event.getPublishedOn() == null) return 0L;

        List<ViewStatsDto> stats = statsClient.getStats(event.getPublishedOn(), LocalDateTime.now(), List.of(uri), unique);
        return (stats != null && !stats.isEmpty()) ? stats.get(0).getHits() : 0L;
    }

    private List<EventFullDto> loadStatForList(List<Event> eventList, boolean unique) {
        List<Event> publishedEvents = eventList.stream()
                .filter(e -> e.getEventState() == PUBLISHED && e.getPublishedOn() != null)
                .toList();

        Map<String, Long> viewsMap = new HashMap<>();

        if (!publishedEvents.isEmpty()) {
            LocalDateTime minPublished = publishedEvents.stream()
                    .map(Event::getPublishedOn)
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now().minusYears(1));

            List<String> uris = publishedEvents.stream().map(e -> EVENT + e.getId()).toList();
            List<ViewStatsDto> stats = statsClient.getStats(minPublished, LocalDateTime.now(), uris, unique);

            if (stats != null) {
                viewsMap = stats.stream().collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));
            }
        }

        Map<String, Long> finalViewsMap = viewsMap;
        return eventList.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toEventFullDto(event);
                    dto.setViews(finalViewsMap.getOrDefault(EVENT + event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}