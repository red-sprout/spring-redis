package org.example.springredis.domain.ch12.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PipelineService {

    private final StringRedisTemplate stringRedisTemplate;

    // Pipeline: SET name Redi / INCR counter / GET name
    public List<Object> runPipeline() {
        return stringRedisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations ops) {
                ops.opsForValue().set("name", "Redi");   // SET name Redi
                ops.opsForValue().increment("counter");   // INCR counter
                ops.opsForValue().get("name");            // GET name
                return null; // 파이프라인 콜백은 항상 null 반환 — 결과는 executePipelined 반환값으로 수집
            }
        });
    }
}
