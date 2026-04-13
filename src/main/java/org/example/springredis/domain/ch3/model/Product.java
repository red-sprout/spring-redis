package org.example.springredis.domain.ch3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

/**
 * @RedisHash("product") → Redis 키 prefix 가 "product" 로 지정됨
 *
 * 저장 구조:
 *   product:{id}              → Hash  (실제 객체 데이터)
 *   product                   → Set   (전체 id 목록, findAll 지원)
 *   product:name:{name}       → Set   (@Indexed 보조 인덱스, findByName 지원)
 */
@RedisHash("product")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    private String id;          // 키: product:{id}

    @Indexed                    // 보조 인덱스 생성 → findByName() 쿼리 메서드 사용 가능
    private String name;

    private int typeId;

    private String version;

    @TimeToLive                 // 초 단위 TTL 설정 (-1 또는 null 이면 만료 없음)
    private Long ttl;
}
