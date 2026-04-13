package org.example.springredis.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories(basePackages = "org.example.springredis.domain.ch3.repository")
public class RedisConfig {

    /**
     * 범용 RedisTemplate — 키: String / 값: JSON (타입 자유)
     *
     * - GenericJackson2JsonRedisSerializer 는 Spring Data Redis 4.0 에서 deprecated
     *   → 후속 클래스인 GenericJacksonJsonRedisSerializer (Jackson 3.x 기반) 사용
     * - 기본 생성자가 내부적으로 default typing 이 활성화된 ObjectMapper 를 생성
     *   → JSON 에 @class 필드 포함, 역직렬화 시 원본 타입 자동 복원
     * - 키 타입을 String 으로, 값 타입을 Object 로 선언해 어떤 타입이든 유연하게 주입 가능
     *   (예: RedisTemplate<String, Product>, RedisTemplate<String, Map<?,?>> 등 주입 가능)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        GenericJacksonJsonRedisSerializer jsonSerializer = GenericJacksonJsonRedisSerializer.builder()
                .enableUnsafeDefaultTyping()
                .build();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        return template;
    }
}
