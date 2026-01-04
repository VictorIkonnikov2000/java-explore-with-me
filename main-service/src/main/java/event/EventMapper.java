package event;

import category.dto.CategoryDto;
import category.Category;

import event.dto.LocationDto;
import org.springframework.stereotype.Component;
import event.dto.EventFullDto;
import event.dto.EventShortDto;
import event.dto.NewEventDto;
import event.dto.UpdateEventUserRequest;
import user.User;
import user.dto.UserShortDto;

import java.time.LocalDateTime;

@Component
public class EventMapper {

    public EventFullDto toEventFullDto(Event event) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setCreatedOn(event.getCreatedOn());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState().toString());
        dto.setTitle(event.getTitle());
        dto.setViews(event.getViews());
        dto.setLocation(LocationMapper.toLocationDto(event.getLocation()));

        return dto;
    }

    public Event toEvent(NewEventDto newEventDto, Category category, User initiator) {
        Event event = new Event();
        event.setAnnotation(newEventDto.getAnnotation());
        event.setCategory(category);
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(newEventDto.getEventDate());
        event.setPaid(newEventDto.getPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.getRequestModeration());
        event.setTitle(newEventDto.getTitle());
        event.setConfirmedRequests(0);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);
        event.setViews(0);
        event.setInitiator(initiator);

        // Set Location details from NewEventDto
        event.setLat(newEventDto.getLat());
        event.setLon(newEventDto.getLon());

        return event;
    }

    public EventShortDto toEventShortDto(Event event) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(event.getViews());

        return dto;
    }

    public Event updateEventFromDto(UpdateEventUserRequest updateEventUserRequest, Event event, Category category) {
        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (category != null) {
            event.setCategory(category);
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }

        // Update lat and lon if provided
        if (updateEventUserRequest.getLat() != null) {
            event.setLat(updateEventUserRequest.getLat());
        }
        if (updateEventUserRequest.getLon() != null) {
            event.setLon(updateEventUserRequest.getLon());
        }


        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        return event;
    }

    private static class CategoryMapper {
        public static CategoryDto toCategoryDto(Category category) {
            CategoryDto dto = new CategoryDto();
            dto.setId(category.getId());
            dto.setName(category.getName());
            return dto;
        }
    }

    private static class UserMapper {
        public static UserShortDto toUserShortDto(User user) {
            UserShortDto dto = new UserShortDto();
            dto.setId(user.getId());
            dto.setName(user.getName());
            return dto;
        }
    }

    private static class LocationMapper {
        public static LocationDto toLocationDto(Location location) {
            LocationDto dto = new LocationDto();
            dto.setLat(location.getLat());
            dto.setLon(location.getLon());
            return dto;
        }
    }
}
