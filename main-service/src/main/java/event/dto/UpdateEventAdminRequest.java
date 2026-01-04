package event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import event.UpdateAdminStateAction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

import static constans.StandardDateTimeFormats.DATE_TIME_FORMAT;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000, message = "Длина краткого описания события должна быть от 20 до 2000 символов")
    private String annotation;
    private Long category;
    @Size(min = 20, max = 7000, message = "Полное описание события должна быть от 20 до 7000 символов")
    private String description;
    @Future(message = "Дата события должна быть в будущем")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime eventDate;
    @Valid
    private LocationDto location;
    private Boolean paid;
    @Min(value = 0, message = "Лимит участников не может быть отрицательным")
    private Integer participantLimit;
    private Boolean requestModeration;
    private UpdateAdminStateAction stateAction;
    @Size(min = 3, max = 120, message = "Заголовок события должен быть от 3 до 120 символов")
    private String title;
}
