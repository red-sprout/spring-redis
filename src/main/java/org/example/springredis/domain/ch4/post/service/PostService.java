package org.example.springredis.domain.ch4.post.service;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.post.entity.Post;
import org.example.springredis.domain.ch4.post.entity.PostTag;
import org.example.springredis.domain.ch4.post.entity.Tag;
import org.example.springredis.domain.ch4.post.repository.PostRepository;
import org.example.springredis.domain.ch4.post.repository.PostTagRepository;
import org.example.springredis.domain.ch4.post.repository.TagRepository;
import org.example.springredis.domain.ch4.user.entity.User;
import org.example.springredis.domain.ch4.user.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final String TAG_KEY_PREFIX = "tag:";

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public Post createPost(String title, String content, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return postRepository.save(Post.builder()
                .title(title).content(content).author(author).build());
    }

    public Tag createTag(String name) {
        return tagRepository.save(Tag.builder().name(name).build());
    }

    @Transactional
    public void addTagToPost(Long postId, Long tagId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));

        // MySQL 저장
        postTagRepository.save(PostTag.builder().post(post).tag(tag).build());

        // Redis Set 캐시 동기화: SADD tag:{tagId} {postId}
        redisTemplate.opsForSet().add(TAG_KEY_PREFIX + tagId, postId.toString());
    }

    // Redis 캐시만 갱신 (MySQL은 이미 저장된 경우)
    public void cacheTagPost(Long tagId, Long postId) {
        redisTemplate.opsForSet().add(TAG_KEY_PREFIX + tagId, postId.toString());
    }

    // SINTER tag:{id1} tag:{id2} ... → postId 목록 → MySQL 조회
    public List<Post> getPostsByTagIntersection(List<Long> tagIds) {
        if (tagIds.isEmpty()) return List.of();

        String[] keys = tagIds.stream()
                .map(id -> TAG_KEY_PREFIX + id)
                .toArray(String[]::new);

        Set<Object> postIdObjects = redisTemplate.opsForSet().intersect(keys[0],
                List.of(keys).subList(1, keys.length));

        if (postIdObjects == null || postIdObjects.isEmpty()) return List.of();

        List<Long> postIds = postIdObjects.stream()
                .map(o -> Long.parseLong((String) o))
                .collect(Collectors.toList());

        return postRepository.findAllById(postIds);
    }

    public List<Tag> getTagsByPost(Long postId) {
        return postTagRepository.findByPostId(postId).stream()
                .map(PostTag::getTag)
                .toList();
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
    }
}
