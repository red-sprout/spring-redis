package org.example.springredis.domain.ch3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Redis String 자료구조 실습
 * 문자열, 숫자 등 단순 값 저장에 사용
 */
@Service
@RequiredArgsConstructor
public class StringService {

    private final StringRedisTemplate redisTemplate;

    // SET key value
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // GET key
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // SET key value NX - 키가 없을 때만 저장
    public Boolean setNx(String key, String value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    // SET key value XX - 키가 있을 때만 덮어씀
    public Boolean setXx(String key, String value) {
        return redisTemplate.opsForValue().setIfPresent(key, value);
    }

    // INCR key - 1 증가
    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    // INCRBY key increment - 지정 값만큼 증가
    public Long incrBy(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // DECR key - 1 감소
    public Long decr(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    // DECRBY key decrement - 지정 값만큼 감소
    public Long decrBy(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // MSET key value [key value ...] - 여러 키 한 번에 저장, 대규모 시스템에 유리
    public void mset(Map<String, String> map) {
        redisTemplate.opsForValue().multiSet(map);
    }

    // MGET key [key ...] - 여러 키 한 번에 조회
    public List<String> mget(List<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }
}
