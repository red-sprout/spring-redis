package org.example.springredis.domain.ch4.post.controller;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.post.entity.Post;
import org.example.springredis.domain.ch4.post.entity.Tag;
import org.example.springredis.domain.ch4.post.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ch4")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(@RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.createPost(request.title(), request.content(), request.userId()));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @PostMapping("/tags")
    public ResponseEntity<Tag> createTag(@RequestBody CreateTagRequest request) {
        return ResponseEntity.ok(postService.createTag(request.name()));
    }

    @PostMapping("/posts/{postId}/tags/{tagId}")
    public ResponseEntity<Void> addTagToPost(@PathVariable Long postId, @PathVariable Long tagId) {
        postService.addTagToPost(postId, tagId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/posts/by-tags")
    public ResponseEntity<List<Post>> getPostsByTags(@RequestParam List<Long> tagIds) {
        return ResponseEntity.ok(postService.getPostsByTagIntersection(tagIds));
    }

    @GetMapping("/posts/{postId}/tags")
    public ResponseEntity<List<Tag>> getTagsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getTagsByPost(postId));
    }

    record CreatePostRequest(String title, String content, Long userId) {}
    record CreateTagRequest(String name) {}
}
