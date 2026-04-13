package org.example.springredis.domain.ch3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Redis Sorted Set 자료구조 실습
 * - 중복 없음 (Set과 유사)
 * - 각 아이템에 score 연결 (Hash와 유사)
 * - score 기준 정렬, 인덱스 접근 가능 (List와 유사)
 * - score 동일 시 사전 순 정렬
 */
@Service
@RequiredArgsConstructor
public class SortedSetService {

    private final StringRedisTemplate redisTemplate;

    // ZADD key score member - 기본 저장
    public Boolean zadd(String key, double score, String member) {
        return redisTemplate.opsForZSet().add(key, member, score);
    }

    // ZADD key NX score member - 존재하지 않을 때만 삽입 (기존 score 업데이트 X)
    public Boolean zaddNx(String key, double score, String member) {
        return redisTemplate.opsForZSet().addIfAbsent(key, member, score);
    }

    // ZADD key XX score member - 존재할 때만 score 업데이트
    public Boolean zaddXx(String key, double score, String member) {
        Double currentScore = redisTemplate.opsForZSet().score(key, member);
        if (currentScore != null) {
            return redisTemplate.opsForZSet().add(key, member, score);
        }
        return false;
    }

    // ZADD key LT score member - 새 score 가 기존보다 작을 때만 업데이트 (없으면 신규 삽입)
    public Boolean zaddLt(String key, double score, String member) {
        Double currentScore = redisTemplate.opsForZSet().score(key, member);
        if (currentScore == null || score < currentScore) {
            return redisTemplate.opsForZSet().add(key, member, score);
        }
        return false;
    }

    // ZADD key GT score member - 새 score 가 기존보다 클 때만 업데이트 (없으면 신규 삽입)
    public Boolean zaddGt(String key, double score, String member) {
        Double currentScore = redisTemplate.opsForZSet().score(key, member);
        if (currentScore == null || score > currentScore) {
            return redisTemplate.opsForZSet().add(key, member, score);
        }
        return false;
    }

    // ZRANGE key start stop WITHSCORES - 인덱스 기반 오름차순 + score 포함 조회
    public Set<ZSetOperations.TypedTuple<String>> zrangeWithScores(String key, long start, long stop) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, stop);
    }

    // ZRANGE key start stop WITHSCORES REV - 역순 + score 포함 조회
    public Set<ZSetOperations.TypedTuple<String>> zrangeRevWithScores(String key, long start, long stop) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, stop);
    }

    // ZRANGE key min max BYSCORE WITHSCORES - score 범위 오름차순 조회
    // Double.NEGATIVE_INFINITY / Double.POSITIVE_INFINITY 로 -inf / +inf 표현
    public Set<ZSetOperations.TypedTuple<String>> zrangeByScoreWithScores(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
    }

    // ZRANGE key max min BYSCORE WITHSCORES REV - score 범위 역순 조회
    public Set<ZSetOperations.TypedTuple<String>> zrangeByScoreRevWithScores(String key, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max);
    }

    // ZRANGE key min max BYLEX - 사전 순 범위 조회 (score 동일한 set 에서 유효)
    // inclusiveMin=false → '(' 미포함(초과), inclusiveMin=true → '[' 포함(이상)
    public Set<String> zrangeByLex(String key, String min, String max,
                                   boolean inclusiveMin, boolean inclusiveMax) {
        Range.Bound<String> lower = inclusiveMin
                ? Range.Bound.inclusive(min) : Range.Bound.exclusive(min);
        Range.Bound<String> upper = inclusiveMax
                ? Range.Bound.inclusive(max) : Range.Bound.exclusive(max);
        return redisTemplate.opsForZSet().rangeByLex(key, Range.of(lower, upper));
    }

    // ZSCAN key cursor [MATCH pattern] - sorted set 내부 커서 기반 조회
    public List<ZSetOperations.TypedTuple<String>> zscan(String key, String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
        List<ZSetOperations.TypedTuple<String>> result = new ArrayList<>();
        try (Cursor<ZSetOperations.TypedTuple<String>> cursor = redisTemplate.opsForZSet().scan(key, options)) {
            cursor.forEachRemaining(result::add);
        }
        return result;
    }
}
