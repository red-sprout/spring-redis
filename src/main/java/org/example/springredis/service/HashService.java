package org.example.springredis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Redis Hash 자료구조 실습
 * 키 - (필드 - 값) 구조, 필드/값 모두 String
 * 키 네이밍 관례: Product:123, user:100:cart 처럼 ':' 로 계층 구조(네임스페이스) 표현
 */
@Service
@RequiredArgsConstructor
public class HashService {

    private final StringRedisTemplate redisTemplate;

    // HSET key field value - 단일 필드 저장
    public void hset(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    // HSET key field value [field value ...] - 여러 필드 한 번에 저장
    public void hmset(String key, Map<String, String> entries) {
        redisTemplate.opsForHash().putAll(key, entries);
    }

    // HGET key field
    public Object hget(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    // HMGET key field [field ...] - 여러 필드 한 번에 조회
    @SuppressWarnings("unchecked")
    public List<Object> hmget(String key, String... fields) {
        return redisTemplate.opsForHash().multiGet(key, (List<Object>) (List<?>) Arrays.asList(fields));
    }

    // HGETALL key - 모든 필드-값 쌍 반환
    public Map<Object, Object> hgetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    // HSCAN key cursor [MATCH pattern] - hash 내부 커서 기반 조회
    public List<Map.Entry<Object, Object>> hscan(String key, String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
        List<Map.Entry<Object, Object>> result = new ArrayList<>();
        try (Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(key, options)) {
            cursor.forEachRemaining(result::add);
        }
        return result;
    }
}
