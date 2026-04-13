package org.example.springredis.domain.ch4.comment.controller;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.comment.entity.Comment;
import org.example.springredis.domain.ch4.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/ch4")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comments")
    public ResponseEntity<Comment> createComment(@RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(commentService.createComment(request.postId(), request.userId(), request.content()));
    }

    @PostMapping("/comments/{commentId}/like/{userId}")
    public ResponseEntity<Map<String, Long>> addLike(@PathVariable Long commentId, @PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("likeCount", commentService.addLike(commentId, userId)));
    }

    @DeleteMapping("/comments/{commentId}/like/{userId}")
    public ResponseEntity<Map<String, Long>> removeLike(@PathVariable Long commentId, @PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("likeCount", commentService.removeLike(commentId, userId)));
    }

    @GetMapping("/comments/{commentId}/likes")
    public ResponseEntity<Map<String, Object>> getLikes(@PathVariable Long commentId) {
        Set<Object> users = commentService.getLikedUsers(commentId);
        return ResponseEntity.ok(Map.of(
                "likeCount", commentService.getLikeCount(commentId),
                "likedUsers", users != null ? users : Set.of()
        ));
    }

    @GetMapping("/comments")
    public ResponseEntity<List<Comment>> getAllComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    record CreateCommentRequest(Long postId, Long userId, String content) {}
}
