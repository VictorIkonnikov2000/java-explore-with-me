package event.dto;


import category.dto.CategoryDto;
import event.Location;
import lombok.Data;
import lombok.NoArgsConstructor;
import user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EventFullDto {
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;
    private LocalDateTime createdOn;
    private String description;
    private LocalDateTime eventDate;
    private Long id;
    private UserShortDto initiator;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    private String state;
    private String title;
    private Integer views;
}

