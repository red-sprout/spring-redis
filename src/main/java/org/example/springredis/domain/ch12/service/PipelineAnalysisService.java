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
public class PipelineAnalysisService {

    private static final int BATCH_SIZE = 1_000;
    private static final byte[] MEMORY_USAGE_SCRIPT =
            "return redis.call('MEMORY', 'USAGE', KEYS[1])".getBytes(StandardCharsets.UTF_8);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 파이프라이닝: 배치 단위로 TYPE + MEMORY USAGE 를 한 번에 전송.
     * 라운드트립이 배치 수 × 2 로 줄어 → 대용량에서 극적인 속도 차이가 남.
     *
     * 결과 배열 구조 (배치 크기 N):
     *   results[0..N-1]     → DataType  (TYPE 명령 결과)
     *   results[N..2N-1]    → Long      (MEMORY USAGE 명령 결과)
     */
    public AnalysisResult analyze() {
        List<String> allKeys = scanAllKeys();

        Map<String, Long> typeCounts = new HashMap<>();
        long totalMemory = 0;
        long validCount  = 0;

        for (int i = 0; i < allKeys.size(); i += BATCH_SIZE) {
            List<String> batch = allKeys.subList(i, Math.min(i + BATCH_SIZE, allKeys.size()));
            int n = batch.size();

            // 배치 내 TYPE * n + MEMORY USAGE * n 을 단일 파이프라인으로 전송
            // eval(ReturnType.INTEGER) → IntegerOutput 사용 → Long 정상 반환
            List<Object> results = stringRedisTemplate.executePipelined((RedisCallback<Object>) conn -> {
                for (String key : batch) {
                    conn.keyCommands().type(key.getBytes(StandardCharsets.UTF_8));
                }
                for (String key : batch) {
                    conn.scriptingCommands().eval(MEMORY_USAGE_SCRIPT, ReturnType.INTEGER, 1, key.getBytes(StandardCharsets.UTF_8));
                }
                return null;
            });

            for (int j = 0; j < n; j++) {
                DataType type = (DataType) results.get(j);
                if (type == DataType.NONE) continue;
                typeCounts.merge(type.code(), 1L, Long::sum);

                Object mem = results.get(n + j);
                if (mem instanceof Long memLong) {
                    totalMemory += memLong;
                    validCount++;
                }
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
