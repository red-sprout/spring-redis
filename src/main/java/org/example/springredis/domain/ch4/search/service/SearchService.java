package org.example.springredis.domain.ch4.search.service;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.search.dto.KeywordEntry;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final String KEY_PREFIX = "search-keyword:";
    private static final int MAX_KEYWORDS = 5;

    private final RedisTemplate<String, Object> redisTemplate;

    // ZADD + ZREMRANGEBYRANK 로 최대 5개 유지
    public void addKeyword(Long userId, String keyword) {
        String key = KEY_PREFIX + userId;
        redisTemplate.opsForZSet().add(key, keyword, System.currentTimeMillis());
        // 6번째 이상 오래된 항목 제거 (음수 인덱스: -6 은 오래된 쪽 6번째)
        redisTemplate.opsForZSet().removeRange(key, 0, -(MAX_KEYWORDS + 1));
    }

    // ZREVRANGE 0 -1 WITHSCORES
    public List<KeywordEntry> getRecentKeywords(Long userId) {
        Set<ZSetOperations.TypedTuple<Object>> result =
                redisTemplate.opsForZSet().reverseRangeWithScores(KEY_PREFIX + userId, 0, -1);
        if (result == null) return List.of();
        return result.stream()
                .map(t -> new KeywordEntry((String) t.getValue(), t.getScore()))
                .toList();
    }

    // ZREM
    public void deleteKeyword(Long userId, String keyword) {
        redisTemplate.opsForZSet().remove(KEY_PREFIX + userId, keyword);
    }

    // DEL
    public void clearHistory(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }

}
