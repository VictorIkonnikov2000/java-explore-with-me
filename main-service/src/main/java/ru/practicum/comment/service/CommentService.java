package ru.practicum.comment.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.BadRequestException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentDto addComment(Long eventId, Long userId, String text) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        CommentDto commentDto = new CommentDto();
        commentDto.setText(text);
        LocalDateTime createdOn = LocalDateTime.now();

        Comment comment = CommentMapper.toComment(commentDto, event, author, createdOn);
        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(savedComment);
    }

    public List<CommentDto> getCommentsByEventId(Long eventId) {
        List<Comment> comments = commentRepository.findByEventId(eventId);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public CommentDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));
        return CommentMapper.toCommentDto(comment);
    }

    @Transactional
    public void deleteComment(long commentId, long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new BadRequestException("User is not the author of the comment"); // Заменили SecurityException на BadRequestException
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public CommentDto updateComment(long commentId, long userId, String text) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new BadRequestException("User is not the author of the comment"); // Заменили SecurityException на BadRequestException
        }
        Event event = comment.getEvent();
        User author = comment.getAuthor();
        CommentDto existingCommentDto = CommentMapper.toCommentDto(comment);
        existingCommentDto.setText(text);

        comment = CommentMapper.toComment(existingCommentDto, event, author);
        comment.setUpdatedOn(LocalDateTime.now());
        comment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }
}



