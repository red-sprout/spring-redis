package org.example.springredis.domain.ch4.seed;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.chat.repository.ChannelRepository;
import org.example.springredis.domain.ch4.comment.repository.CommentRepository;
import org.example.springredis.domain.ch4.location.repository.PlaceRepository;
import org.example.springredis.domain.ch4.post.repository.PostRepository;
import org.example.springredis.domain.ch4.post.repository.TagRepository;
import org.example.springredis.domain.ch4.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 초기화된 샘플 데이터의 ID를 한눈에 확인하는 조회 전용 엔드포인트.
 * Swagger 테스트 시 userId, channelId, postId 등을 파악할 때 사용합니다.
 */
@RestController
@RequestMapping("/api/ch4/seed-info")
@RequiredArgsConstructor
public class SeedInfoController {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ChannelRepository channelRepository;
    private final PlaceRepository placeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSeedInfo() {
        Map<String, Object> info = new LinkedHashMap<>();

        info.put("leaderboard_date", "260413");
        info.put("weekly_dates_example", List.of("260406", "260407", "260408"));

        info.put("users", userRepository.findAll().stream()
                .map(u -> Map.of("id", u.getId(), "username", u.getUsername()))
                .toList());

        info.put("tags", tagRepository.findAll().stream()
                .map(t -> Map.of("id", t.getId(), "name", t.getName()))
                .toList());

        info.put("posts", postRepository.findAll().stream()
                .map(p -> Map.of("id", p.getId(), "title", p.getTitle()))
                .toList());

        info.put("comments", commentRepository.findAll().stream()
                .map(c -> Map.of("id", c.getId(), "content", c.getContent()))
                .toList());

        info.put("channels", channelRepository.findAll().stream()
                .map(ch -> Map.of("id", ch.getId(), "name", ch.getName()))
                .toList());

        info.put("places", placeRepository.findAll().stream()
                .map(pl -> Map.of("name", pl.getName(), "lon", pl.getLongitude(), "lat", pl.getLatitude()))
                .toList());

        return ResponseEntity.ok(info);
    }
}
