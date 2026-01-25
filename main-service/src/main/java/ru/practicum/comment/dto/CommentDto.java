package ru.practicum.comment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    private String text;
    private Long eventId;
    private Long authorId;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
