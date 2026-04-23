package org.example.springredis.domain.ch12.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class TestDataService {

    private static final String INIT_SENTINEL = "ch12:analysis:initialized";
    private static final int BATCH_SIZE = 1_000;

    // 키 개수 상수 — 필요에 따라 조절
    private static final int STRING_COUNT = 100_000;
    private static final int SET_COUNT    = 1_000;
    private static final int ZSET_COUNT   = 100;
    private static final int HASH_COUNT   = 10;
    private static final int LIST_COUNT   = 10;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 테스트 데이터를 한 번만 삽입 (이미 존재하면 skip).
     * @return true: 새로 삽입, false: 이미 존재
     */
    public boolean init() {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(INIT_SENTINEL))) {
            return false;
        }

        insertStrings();
        insertSets();
        insertZSets();
        insertHashes();
        insertLists();

        stringRedisTemplate.opsForValue().set(INIT_SENTINEL, "1");
        return true;
    }

    // STRING: ch12:str:{i} → "value-{i}"
    private void insertStrings() {
        for (int i = 0; i < STRING_COUNT; i += BATCH_SIZE) {
            final int from = i;
            final int to   = Math.min(i + BATCH_SIZE, STRING_COUNT);
            stringRedisTemplate.executePipelined((RedisCallback<Object>) conn -> {
                for (int j = from; j < to; j++) {
                    conn.stringCommands().set(
                            ("ch12:str:" + j).getBytes(StandardCharsets.UTF_8),
                            ("value-" + j).getBytes(StandardCharsets.UTF_8)
                    );
                }
                return null;
            });
        }
    }

    // SET: ch12:set:{i} → {member-0, member-1, member-2}
    private void insertSets() {
        for (int i = 0; i < SET_COUNT; i += BATCH_SIZE) {
            final int from = i;
            final int to   = Math.min(i + BATCH_SIZE, SET_COUNT);
            stringRedisTemplate.executePipelined((RedisCallback<Object>) conn -> {
                for (int j = from; j < to; j++) {
                    byte[] key = ("ch12:set:" + j).getBytes(StandardCharsets.UTF_8);
                    conn.setCommands().sAdd(key,
                            "member-0".getBytes(StandardCharsets.UTF_8),
                            "member-1".getBytes(StandardCharsets.UTF_8),
                            "member-2".getBytes(StandardCharsets.UTF_8)
                    );
                }
                return null;
            });
        }
    }

    // ZSET: ch12:zset:{i} → {m0:1.0, m1:2.0, m2:3.0}
    private void insertZSets() {
        stringRedisTemplate.executePipelined((RedisCallback<Object>) conn -> {
            for (int i = 0; i < ZSET_COUNT; i++) {
                byte[] key = ("ch12:zset:" + i).getBytes(StandardCharsets.UTF_8);
                conn.zSetCommands().zAdd(key, 1.0, "m0".getBytes(StandardCharsets.UTF_8));
                conn.zSetCommands().zAdd(key, 2.0, "m1".getBytes(StandardCharsets.UTF_8));
                conn.zSetCommands().zAdd(key, 3.0, "m2".getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });
    }

    // HASH: ch12:hash:{i} → {field0:val0, field1:val1, field2:val2}
    private void insertHashes() {
        stringRedisTemplate.executePipelined((RedisCallback<Object>) conn -> {
            for (int i = 0; i < HASH_COUNT; i++) {
                byte[] key = ("ch12:hash:" + i).getBytes(StandardCharsets.UTF_8);
                conn.hashCommands().hSet(key, "field0".getBytes(StandardCharsets.UTF_8), "val0".getBytes(StandardCharsets.UTF_8));
                conn.hashCommands().hSet(key, "field1".getBytes(StandardCharsets.UTF_8), "val1".getBytes(StandardCharsets.UTF_8));
                conn.hashCommands().hSet(key, "field2".getBytes(StandardCharsets.UTF_8), "val2".getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });
    }

    // LIST: ch12:list:{i} → [elem-0, elem-1, elem-2]
    private void insertLists() {
        stringRedisTemplate.executePipelined((RedisCallback<Object>) conn -> {
            for (int i = 0; i < LIST_COUNT; i++) {
                byte[] key = ("ch12:list:" + i).getBytes(StandardCharsets.UTF_8);
                conn.listCommands().rPush(key,
                        "elem-0".getBytes(StandardCharsets.UTF_8),
                        "elem-1".getBytes(StandardCharsets.UTF_8),
                        "elem-2".getBytes(StandardCharsets.UTF_8)
                );
            }
            return null;
        });
    }
}
