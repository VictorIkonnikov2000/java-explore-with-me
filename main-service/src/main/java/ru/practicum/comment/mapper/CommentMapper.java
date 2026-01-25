package ru.practicum.comment.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE) // Чтобы нельзя было создать экземпляр класса
public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setEventId(comment.getEvent().getId());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setCreatedOn(comment.getCreatedOn());
        dto.setUpdatedOn(comment.getUpdatedOn());
        return dto;
    }

    public static Comment toComment(CommentDto commentDto, Event event, User author) {
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setText(commentDto.getText());
        comment.setEvent(event);
        comment.setAuthor(author);
        comment.setCreatedOn(commentDto.getCreatedOn());
        comment.setUpdatedOn(commentDto.getUpdatedOn());
        return comment;
    }

    //Перегрузка для создания нового комментария
    public static Comment toComment(CommentDto commentDto, Event event, User author, LocalDateTime createdOn) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setEvent(event);
        comment.setAuthor(author);
        comment.setCreatedOn(createdOn);
        return comment;
    }
}
