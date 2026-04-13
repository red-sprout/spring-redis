package org.example.springredis.domain.ch4.comment.service;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.comment.entity.Comment;
import org.example.springredis.domain.ch4.comment.repository.CommentRepository;
import org.example.springredis.domain.ch4.post.entity.Post;
import org.example.springredis.domain.ch4.post.repository.PostRepository;
import org.example.springredis.domain.ch4.user.entity.User;
import org.example.springredis.domain.ch4.user.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final String LIKE_KEY_PREFIX = "comment-like:";

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public Comment createComment(Long postId, Long userId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return commentRepository.save(Comment.builder()
                .post(post).author(author).content(content).build());
    }

    // SADD comment-like:{commentId} {userId}
    public Long addLike(Long commentId, Long userId) {
        redisTemplate.opsForSet().add(LIKE_KEY_PREFIX + commentId, userId.toString());
        return getLikeCount(commentId);
    }

    // SREM comment-like:{commentId} {userId}
    public Long removeLike(Long commentId, Long userId) {
        redisTemplate.opsForSet().remove(LIKE_KEY_PREFIX + commentId, userId.toString());
        return getLikeCount(commentId);
    }

    // SCARD comment-like:{commentId}
    public Long getLikeCount(Long commentId) {
        Long size = redisTemplate.opsForSet().size(LIKE_KEY_PREFIX + commentId);
        return size != null ? size : 0L;
    }

    // SISMEMBER comment-like:{commentId} {userId}
    public Boolean hasLiked(Long commentId, Long userId) {
        return redisTemplate.opsForSet().isMember(LIKE_KEY_PREFIX + commentId, userId.toString());
    }

    // SMEMBERS comment-like:{commentId}
    public Set<Object> getLikedUsers(Long commentId) {
        return redisTemplate.opsForSet().members(LIKE_KEY_PREFIX + commentId);
    }

    public List<Comment> getCommentsByPost(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }
}
