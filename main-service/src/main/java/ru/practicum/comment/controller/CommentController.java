package ru.practicum.comment.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long eventId,
            @RequestHeader("X-Sharer-User-Id") Long userId,  //TODO: Use Spring Security for authentication
            @Valid @RequestBody AddCommentRequest request
    ) {
        CommentDto commentDto = commentService.addComment(eventId, userId, request.getText());
        return new ResponseEntity<>(commentDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CommentDto>> getCommentsByEventId(@PathVariable Long eventId) {
        List<CommentDto> comments = commentService.getCommentsByEventId(eventId);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getCommentById(@PathVariable Long commentId) {
        CommentDto comment = commentService.getCommentById(commentId);
        return new ResponseEntity<>(comment, HttpStatus.OK);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        commentService.deleteComment(commentId, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody UpdateCommentRequest request) {
        CommentDto comment = commentService.updateComment(commentId, userId, request.getText());
        return new ResponseEntity<>(comment, HttpStatus.OK);
    }


    // Helper classes for request bodies with validation
    @Data
    static class AddCommentRequest {
        @NotBlank
        @Size(max = 2000)
        private String text;
    }

    @Data
    static class UpdateCommentRequest {
        @NotBlank
        @Size(max = 2000)
        private String text;
    }
}
