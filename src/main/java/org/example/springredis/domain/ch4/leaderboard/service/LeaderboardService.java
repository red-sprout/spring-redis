package org.example.springredis.domain.ch4.leaderboard.service;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.leaderboard.dto.PlayerScore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private static final String DAILY_KEY_PREFIX = "daily-score:";
    private static final String WEEKLY_KEY_PREFIX = "weekly-score:";

    private final RedisTemplate<String, Object> redisTemplate;

    // ZADD daily-score:{date} {score} player:{userId}
    public void addScore(String date, Long userId, double score) {
        redisTemplate.opsForZSet().add(DAILY_KEY_PREFIX + date, "player:" + userId, score);
    }

    // ZINCRBY daily-score:{date} {delta} player:{userId}
    public Double incrementScore(String date, Long userId, double delta) {
        return redisTemplate.opsForZSet().incrementScore(DAILY_KEY_PREFIX + date, "player:" + userId, delta);
    }

    // ZREVRANGE daily-score:{date} 0 n-1 WITHSCORES
    public List<PlayerScore> getTopN(String date, int n) {
        Set<ZSetOperations.TypedTuple<Object>> result =
                redisTemplate.opsForZSet().reverseRangeWithScores(DAILY_KEY_PREFIX + date, 0, n - 1);
        return toPlayerScores(result);
    }

    // ZREVRANK daily-score:{date} player:{userId}
    public Long getRank(String date, Long userId) {
        return redisTemplate.opsForZSet().reverseRank(DAILY_KEY_PREFIX + date, "player:" + userId);
    }

    // ZSCORE daily-score:{date} player:{userId}
    public Double getScore(String date, Long userId) {
        return redisTemplate.opsForZSet().score(DAILY_KEY_PREFIX + date, "player:" + userId);
    }

    // ZUNIONSTORE weekly-score:{weekKey} n daily-score:{d1} daily-score:{d2} ...
    public void mergeWeeklyScore(String weekKey, List<String> dates) {
        List<String> dailyKeys = dates.stream()
                .map(d -> DAILY_KEY_PREFIX + d)
                .toList();
        redisTemplate.opsForZSet().unionAndStore(
                dailyKeys.get(0),
                dailyKeys.subList(1, dailyKeys.size()),
                WEEKLY_KEY_PREFIX + weekKey
        );
    }

    // ZREVRANGE weekly-score:{weekKey} 0 n-1 WITHSCORES
    public List<PlayerScore> getWeeklyTopN(String weekKey, int n) {
        Set<ZSetOperations.TypedTuple<Object>> result =
                redisTemplate.opsForZSet().reverseRangeWithScores(WEEKLY_KEY_PREFIX + weekKey, 0, n - 1);
        return toPlayerScores(result);
    }

    private List<PlayerScore> toPlayerScores(Set<ZSetOperations.TypedTuple<Object>> tuples) {
        if (tuples == null) return List.of();
        return tuples.stream()
                .map(t -> new PlayerScore((String) t.getValue(), t.getScore()))
                .toList();
    }

}
