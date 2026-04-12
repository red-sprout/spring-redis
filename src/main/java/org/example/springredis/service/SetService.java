package org.example.springredis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Redis Set 자료구조 실습
 * 정렬되지 않은 문자열 모음, 중복 제거
 */
@Service
@RequiredArgsConstructor
public class SetService {

    private final StringRedisTemplate redisTemplate;

    // SADD key member [member ...] - 아이템 추가 (중복 무시)
    public Long sadd(String key, String... members) {
        return redisTemplate.opsForSet().add(key, members);
    }

    // SMEMBERS key - 전체 아이템 조회
    public Set<String> smembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    // SREM key member [member ...] - 아이템 삭제
    public Long srem(String key, String... members) {
        return redisTemplate.opsForSet().remove(key, (Object[]) members);
    }

    // SPOP key - 랜덤 아이템 반환 후 삭제
    public String spop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }

    // SUNION key [key ...] - 합집합
    public Set<String> sunion(String key1, String key2) {
        return redisTemplate.opsForSet().union(key1, key2);
    }

    // SINTER key [key ...] - 교집합
    public Set<String> sinter(String key1, String key2) {
        return redisTemplate.opsForSet().intersect(key1, key2);
    }

    // SDIFF key [key ...] - 차집합 (key1 기준, key2 에만 있는 원소 제외)
    public Set<String> sdiff(String key1, String key2) {
        return redisTemplate.opsForSet().difference(key1, key2);
    }

    // SSCAN key cursor [MATCH pattern] - set 내부 커서 기반 조회
    public List<String> sscan(String key, String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
        List<String> result = new ArrayList<>();
        try (Cursor<String> cursor = redisTemplate.opsForSet().scan(key, options)) {
            cursor.forEachRemaining(result::add);
        }
        return result;
    }
}
