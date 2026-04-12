package org.example.springredis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Redis Bitmap 자료구조 실습
 * String + bit 연산, 대용량 플래그/상태 관리에 메모리 효율적
 */
@Service
@RequiredArgsConstructor
public class BitmapService {

    private final RedisTemplate<String, String> redisTemplate;

    // SETBIT key offset value - 특정 오프셋에 비트 설정 (이전 값 반환)
    public Boolean setbit(String key, long offset, boolean value) {
        return redisTemplate.opsForValue().setBit(key, offset, value);
    }

    // GETBIT key offset - 특정 오프셋 비트 조회
    public Boolean getbit(String key, long offset) {
        return redisTemplate.opsForValue().getBit(key, offset);
    }

    // BITFIELD key SET u1 offset 1 [SET u1 offset 1 ...] - 여러 오프셋 비트 한 번에 설정
    // u1 = unsigned 1bit, 각 offset 에 1 을 SET
    public List<Long> bitfield(String key, long... offsets) {
        BitFieldSubCommands subCommands = BitFieldSubCommands.create();
        for (long offset : offsets) {
            subCommands = subCommands
                    .set(BitFieldSubCommands.BitFieldType.unsigned(1))
                    .valueAt(offset)
                    .to(1L);
        }
        final BitFieldSubCommands finalCmd = subCommands;
        return redisTemplate.execute(
                (RedisCallback<List<Long>>) conn ->
                        conn.stringCommands().bitField(key.getBytes(), finalCmd)
        );
    }

    // BITCOUNT key - 1 로 설정된 비트 수 반환
    public Long bitcount(String key) {
        return redisTemplate.execute(
                (RedisCallback<Long>) conn ->
                        conn.stringCommands().bitCount(key.getBytes())
        );
    }
}
