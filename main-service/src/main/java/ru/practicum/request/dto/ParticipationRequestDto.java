package ru.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.request.model.RequestStatus;

import java.time.LocalDateTime;

import static ru.practicum.constans.StandardDateTimeFormats.DATE_TIME_FORMAT_WITH_MILLIS;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequestDto {
    Long id;
    @JsonFormat(pattern = DATE_TIME_FORMAT_WITH_MILLIS)
    LocalDateTime created;
    Long event;
    Long requester;
    RequestStatus status;
}
