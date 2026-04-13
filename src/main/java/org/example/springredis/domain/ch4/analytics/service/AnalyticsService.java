package org.example.springredis.domain.ch4.analytics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final String UV_KEY_PREFIX = "uv:";
    private static final String API_METER_KEY_PREFIX = "api-meter:";

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    // SETBIT uv:{date} {userId} 1
    public void recordVisit(String date, long userId) {
        stringRedisTemplate.opsForValue().setBit(UV_KEY_PREFIX + date, userId, true);
    }

    // BITCOUNT uv:{date}
    public Long getDau(String date) {
        String key = UV_KEY_PREFIX + date;
        return stringRedisTemplate.execute((RedisCallback<Long>) connection ->
                connection.stringCommands().bitCount(key.getBytes(StandardCharsets.UTF_8)));
    }

    // GETBIT uv:{date} {userId}
    public Boolean hasVisited(String date, long userId) {
        return stringRedisTemplate.opsForValue().getBit(UV_KEY_PREFIX + date, userId);
    }

    // BITOP AND retained:{key} uv:{date1} uv:{date2} ... → BITCOUNT
    public Long getRetainedUsers(List<String> dates) {
        String destKey = "retained:" + String.join("-", dates);
        byte[][] sourceKeyBytes = dates.stream()
                .map(d -> (UV_KEY_PREFIX + d).getBytes(StandardCharsets.UTF_8))
                .toArray(byte[][]::new);

        stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
            connection.stringCommands().bitOp(
                    RedisStringCommands.BitOperation.AND,
                    destKey.getBytes(StandardCharsets.UTF_8),
                    sourceKeyBytes
            );
            return null;
        });

        return stringRedisTemplate.execute((RedisCallback<Long>) connection ->
                connection.stringCommands().bitCount(destKey.getBytes(StandardCharsets.UTF_8)));
    }

    // PFADD api-meter:{userId} {logId}
    public Long recordApiCall(Long userId, String logId) {
        return redisTemplate.opsForHyperLogLog().add(API_METER_KEY_PREFIX + userId, logId);
    }

    // PFCOUNT api-meter:{userId}
    public Long getApiCallCount(Long userId) {
        return redisTemplate.opsForHyperLogLog().size(API_METER_KEY_PREFIX + userId);
    }
}
