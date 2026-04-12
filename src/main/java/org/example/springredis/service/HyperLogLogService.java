package org.example.springredis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis HyperLogLog 자료구조 실습
 * 집합의 카디널리티(고유 원소 수)를 추정하는 자료구조
 * Set 과 달리 원소를 직접 저장하지 않아 메모리 효율적 (최대 12KB)
 * 약 0.81% 오차율 허용
 */
@Service
@RequiredArgsConstructor
public class HyperLogLogService {

    private final RedisTemplate<String, String> redisTemplate;

    // PFADD key element [element ...] - 원소 추가
    public Long pfadd(String key, String... elements) {
        return redisTemplate.opsForHyperLogLog().add(key, elements);
    }

    // PFCOUNT key [key ...] - 카디널리티(고유 원소 수) 추정
    // 원소 자체는 저장되지 않으므로 개수 조회만 가능
    public Long pfcount(String... keys) {
        return redisTemplate.opsForHyperLogLog().size(keys);
    }
}
