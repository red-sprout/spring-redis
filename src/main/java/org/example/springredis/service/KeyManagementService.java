package org.example.springredis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.core.query.SortCriterion;
import org.springframework.data.redis.core.query.SortQueryBuilder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 키 관리 커맨드 실습
 * 키의 조회/삭제/만료 시간 관리 등 전반적인 키 관련 기능
 */
@Service
@RequiredArgsConstructor
public class KeyManagementService {

    private final StringRedisTemplate redisTemplate;

    // EXISTS key - 키 존재 여부 확인 (있으면 true)
    public Boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    // KEYS pattern - 패턴에 일치하는 모든 키 한 번에 조회
    // 주의: 싱글 스레드 블로킹 → 키 수 증가 시 타임아웃/서비스 장애 위험, 운영 환경 사용 지양
    // 패턴 예시: h?llo, h*llo, h[ae]llo, h[^e]llo, h[a-b]llo
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    // SCAN cursor MATCH pattern COUNT count - 커서 기반 분할 조회 (KEYS 대체)
    // Non-blocking, 소량씩 나눠 조회 → 서버 부하 최소화
    public List<String> scan(String pattern, long count) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(count).build();
        List<String> result = new ArrayList<>();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            cursor.forEachRemaining(result::add);
        }
        return result;
    }

    // SCAN cursor TYPE type - 타입 필터 조회 (string, list, hash, set, zset, stream)
    public List<String> scanByType(String type) {
        ScanOptions options = ScanOptions.scanOptions().type(type).build();
        List<String> result = new ArrayList<>();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            cursor.forEachRemaining(result::add);
        }
        return result;
    }

    // SORT key - list, set, sorted set 숫자 오름차순 정렬
    public List<String> sort(String key) {
        return redisTemplate.sort(SortQueryBuilder.sort(key).build());
    }

    // SORT key ALPHA - 사전 순 정렬
    public List<String> sortAlpha(String key) {
        return redisTemplate.sort(SortQueryBuilder.sort(key).alphabetical(true).build());
    }

    // SORT key LIMIT offset count ASC|DESC - 범위 + 정렬 방향 지정
    public List<String> sortWithLimit(String key, long offset, long count, boolean desc) {
        SortCriterion<String> criterion = SortQueryBuilder.sort(key).limit(offset, count);
        if (desc) {
            criterion = criterion.order(SortParameters.Order.DESC);
        }
        return redisTemplate.sort(criterion.build());
    }

    // RENAME key newkey - 키 이름 변경
    public void rename(String key, String newKey) {
        redisTemplate.rename(key, newKey);
    }

    // RENAMENX key newkey - 새 키가 존재하지 않을 때만 변경
    public Boolean renameNx(String key, String newKey) {
        return redisTemplate.renameIfAbsent(key, newKey);
    }

    // COPY source destination [REPLACE] - 키 복사
    // replace=true → destination 키가 이미 있어도 덮어씀
    public Boolean copy(String source, String destination, boolean replace) {
        return redisTemplate.copy(source, destination, replace);
    }

    // TYPE key - 자료구조 타입 반환 (string, list, hash, set, zset, stream, none)
    public String type(String key) {
        return redisTemplate.type(key).code();
    }

    // OBJECT ENCODING key - 내부 인코딩 방식 조회 (예: embstr, int, listpack, skiplist 등)
    public String objectEncoding(String key) {
        return redisTemplate.execute((RedisCallback<String>) conn -> {
            byte[] result = (byte[]) conn.execute("OBJECT", "ENCODING".getBytes(), key.getBytes());
            return result != null ? new String(result) : null;
        });
    }

    // OBJECT IDLETIME key - 마지막 접근 이후 경과 시간(초) 조회
    public Long objectIdletime(String key) {
        return redisTemplate.execute((RedisCallback<Long>) conn ->
                (Long) conn.execute("OBJECT", "IDLETIME".getBytes(), key.getBytes())
        );
    }

    // FLUSHALL - 전체 데이터 삭제 (주의: 복구 불가, lazyfree-lazy-user-flush 설정에 따라 동기/비동기)
    public void flushAll() {
        redisTemplate.execute((RedisCallback<Void>) conn -> {
            conn.serverCommands().flushAll();
            return null;
        });
    }

    // DEL key [key ...] - 동기 삭제, 즉시 메모리 회수
    // 대용량 키 삭제 시 싱글 스레드 블로킹 발생 가능
    public Long del(String... keys) {
        return redisTemplate.delete(Arrays.asList(keys));
    }

    // UNLINK key [key ...] - 비동기 삭제, 키만 먼저 제거 후 메모리 해제는 백그라운드 처리
    // 대용량 키 삭제 시 권장 (서비스 영향 최소화)
    public Long unlink(String... keys) {
        return redisTemplate.unlink(Arrays.asList(keys));
    }

    // EXPIRE key seconds - 만료 시간 설정
    public Boolean expire(String key, long seconds) {
        return redisTemplate.expire(key, Duration.ofSeconds(seconds));
    }

    // EXPIRE key seconds NX - 만료 시간이 설정되지 않은 경우에만 설정 (Redis 7.0+)
    public Boolean expireNx(String key, long seconds) {
        return redisTemplate.execute((RedisCallback<Boolean>) conn -> {
            Object result = conn.execute("EXPIRE",
                    key.getBytes(), String.valueOf(seconds).getBytes(), "NX".getBytes());
            return Long.valueOf(1L).equals(result);
        });
    }

    // EXPIRE key seconds XX - 만료 시간이 이미 있을 때만 업데이트 (Redis 7.0+)
    public Boolean expireXx(String key, long seconds) {
        return redisTemplate.execute((RedisCallback<Boolean>) conn -> {
            Object result = conn.execute("EXPIRE",
                    key.getBytes(), String.valueOf(seconds).getBytes(), "XX".getBytes());
            return Long.valueOf(1L).equals(result);
        });
    }

    // EXPIRE key seconds GT - 새 만료 시간이 현재보다 클 때만 업데이트 (Redis 7.0+)
    public Boolean expireGt(String key, long seconds) {
        return redisTemplate.execute((RedisCallback<Boolean>) conn -> {
            Object result = conn.execute("EXPIRE",
                    key.getBytes(), String.valueOf(seconds).getBytes(), "GT".getBytes());
            return Long.valueOf(1L).equals(result);
        });
    }

    // EXPIRE key seconds LT - 새 만료 시간이 현재보다 작을 때만 업데이트 (Redis 7.0+)
    public Boolean expireLt(String key, long seconds) {
        return redisTemplate.execute((RedisCallback<Boolean>) conn -> {
            Object result = conn.execute("EXPIRE",
                    key.getBytes(), String.valueOf(seconds).getBytes(), "LT".getBytes());
            return Long.valueOf(1L).equals(result);
        });
    }

    // EXPIREAT key unix-time-seconds - 만료 시각을 유닉스 타임스탬프로 지정
    public Boolean expireAt(String key, Instant instant) {
        return redisTemplate.expireAt(key, instant);
    }

    // EXPIRETIME key - 키가 삭제되는 유닉스 타임스탬프(초) 반환
    // -1: 만료 미설정, -2: 키 없음 (Redis 7.0+)
    public Long expiretime(String key) {
        return redisTemplate.execute((RedisCallback<Long>) conn ->
                (Long) conn.execute("EXPIRETIME", key.getBytes())
        );
    }

    // TTL key - 만료까지 남은 초 반환
    // -1: 만료 미설정, -2: 키 없음
    public Long ttl(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
}
