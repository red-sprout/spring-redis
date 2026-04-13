package org.example.springredis.domain.ch3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Redis List 자료구조 실습
 * 순서가 있는 문자열 목록, 스택/큐로 활용
 */
@Service
@RequiredArgsConstructor
public class ListService {

    private final StringRedisTemplate redisTemplate;

    // LPUSH key value [value ...] - 왼쪽(head)에 추가
    public Long lpush(String key, String... values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    // RPUSH key value [value ...] - 오른쪽(tail)에 추가
    public Long rpush(String key, String... values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    // LRANGE key start stop - 범위 조회 (-1 은 마지막 인덱스)
    public List<String> lrange(String key, long start, long stop) {
        return redisTemplate.opsForList().range(key, start, stop);
    }

    // LPOP key - 첫 번째 아이템 반환 및 삭제
    public String lpop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    // LPOP key count - 여러 아이템 반환 및 삭제
    public List<String> lpopCount(String key, long count) {
        return redisTemplate.opsForList().leftPop(key, count);
    }

    // LTRIM key start stop - 지정 범위 외 아이템 삭제 (반환 없음)
    // 고정 길이 큐 관리에 활용: LPUSH logdata <data> + LTRIM logdata 0 999
    public void ltrim(String key, long start, long stop) {
        redisTemplate.opsForList().trim(key, start, stop);
    }

    // LINSERT key BEFORE pivot value - pivot 앞에 삽입
    public Long linsertBefore(String key, String pivot, String value) {
        return redisTemplate.opsForList().leftPush(key, pivot, value);
    }

    // LINSERT key AFTER pivot value - pivot 뒤에 삽입
    public Long linsertAfter(String key, String pivot, String value) {
        return redisTemplate.opsForList().rightPush(key, pivot, value);
    }

    // LSET key index value - 인덱스 위치 덮어씀 (범위 초과 시 에러)
    public void lset(String key, long index, String value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    // LINDEX key index - 인덱스 위치 데이터 조회
    public String lindex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }
}
