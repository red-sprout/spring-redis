package org.example.springredis.domain.ch12.service;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch12.dto.AnalysisResult;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ForLoopAnalysisService {

    // conn.execute("MEMORY", ...) → ByteArrayOutput → Long 반환 시 UnsupportedOperationException
    // eval()로 우회하면 ReturnType.INTEGER → IntegerOutput(LongOutput)을 사용해 정상 동작
    private static final byte[] MEMORY_USAGE_SCRIPT =
            "return redis.call('MEMORY', 'USAGE', KEYS[1])".getBytes(StandardCharsets.UTF_8);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 단순 for 루프: 키마다 TYPE, MEMORY USAGE를 개별 호출.
     * 라운드트립이 키 수 × 2 만큼 발생 → 느리지만 코드가 직관적.
     */
    public AnalysisResult analyze() {
        List<String> allKeys = scanAllKeys();

        Map<String, Long> typeCounts = new HashMap<>();
        long totalMemory = 0;
        long validCount  = 0;

        for (String key : allKeys) {
            // TYPE key
            DataType type = stringRedisTemplate.type(key);
            if (type == DataType.NONE) continue;
            typeCounts.merge(type.code(), 1L, Long::sum);

            // MEMORY USAGE key
            Object mem = stringRedisTemplate.execute((RedisCallback<Object>) conn ->
                    conn.scriptingCommands().eval(MEMORY_USAGE_SCRIPT, ReturnType.INTEGER, 1, key.getBytes(StandardCharsets.UTF_8)));
            if (mem instanceof Long memLong) {
                totalMemory += memLong;
                validCount++;
            }
        }

        double avgMem = validCount > 0 ? (double) totalMemory / validCount : 0;
        return new AnalysisResult(typeCounts, avgMem);
    }

    // RedisTemplate.scan()은 Cursor<String>을 반환 → byte[] 변환 불필요
    private List<String> scanAllKeys() {
        List<String> keys = new ArrayList<>();
        try (Cursor<String> cursor = stringRedisTemplate.scan(ScanOptions.scanOptions().count(1000).build())) {
            cursor.forEachRemaining(keys::add);
        }
        return keys;
    }
}
