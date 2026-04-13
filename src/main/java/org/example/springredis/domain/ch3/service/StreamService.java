package org.example.springredis.domain.ch3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Redis Stream 자료구조 실습
 * Redis 를 메시지 브로커로 사용하는 자료구조
 * Kafka 영향을 받아 Producer/Consumer 개념 존재
 * 메시지는 자동 생성 ID(타임스탬프-시퀀스) 로 관리
 */
@Service
@RequiredArgsConstructor
public class StreamService {

    private final StringRedisTemplate redisTemplate;

    // XADD key * field value [field value ...] - 스트림에 메시지 추가 (Producer)
    // * 는 ID 자동 생성, 반환값: 생성된 레코드 ID (예: 1638125133152-0)
    public RecordId xadd(String key, Map<String, String> fields) {
        return redisTemplate.opsForStream().add(
                StreamRecords.newRecord().in(key).ofMap(fields)
        );
    }

    // XREAD COUNT count STREAMS key id - 지정 ID 이후 메시지 읽기 (Consumer)
    // id: "0-0" → 처음부터 모든 메시지, "$" → 이후 새 메시지만
    public List<MapRecord<String, Object, Object>> xread(String key, String id) {
        return redisTemplate.opsForStream()
                .read(StreamOffset.create(key, ReadOffset.from(id)));
    }

    // XLEN key - 스트림에 저장된 메시지 수 조회
    public Long xlen(String key) {
        return redisTemplate.opsForStream().size(key);
    }
}
