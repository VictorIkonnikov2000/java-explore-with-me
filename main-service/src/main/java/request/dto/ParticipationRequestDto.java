package request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import request.RequestStatus;


import java.time.LocalDateTime;

import static constans.StandardDateTimeFormats.DATE_TIME_FORMAT_WITH_MILLIS;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequestDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT_WITH_MILLIS)
    private LocalDateTime created;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long event;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long requester;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private RequestStatus status;
}