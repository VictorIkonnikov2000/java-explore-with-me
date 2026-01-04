package event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import event.dto.*;
import exception.NotFoundException;
import category.Category;
import user.User;
import category.CategoryRepository;
import user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(() -> new NotFoundException("Category not found"));
        Event event = eventMapper.toEvent(newEventDto, category, user);
        event = eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    public List<EventShortDto> getEventsByUserId(Long userId) {
        List<Event> events = eventRepository.findByInitiatorId(userId);
        return events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto getEventById(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
        //Проверка является ли пользователь создателем события
        if (!event.getInitiator().getId().equals(userId)) throw new NotFoundException("User is not the creator");
        return eventMapper.toEventFullDto(event);
    }

    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
        //Проверка является ли пользователь создателем события
        if (!event.getInitiator().getId().equals(userId)) throw new NotFoundException("User is not the creator");

        Category category = null;
        if (updateEventUserRequest.getCategory() != null) {
            category = categoryRepository.findById(updateEventUserRequest.getCategory()).orElseThrow(() -> new NotFoundException("Category not found"));
        }

        event = eventMapper.updateEventFromDto(updateEventUserRequest, event, category);
        event = eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }
}
