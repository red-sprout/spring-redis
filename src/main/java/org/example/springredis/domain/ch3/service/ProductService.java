package org.example.springredis.domain.ch3.service;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch3.model.Product;
import org.example.springredis.domain.ch3.repository.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Redis 객체 저장 두 가지 방식을 함께 보여주는 서비스
 *
 * 1. CrudRepository    — @RedisHash 기반, 객체 ↔ Redis Hash 자동 매핑
 * 2. RedisTemplate     — JSON 직렬화(GenericJacksonJsonRedisSerializer), 수동으로 객체 저장/조회
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // RedisConfig 에서 등록한 RedisTemplate<String, Object> 빈 (JSON 직렬화)
    private final RedisTemplate<String, Object> redisTemplate;

    // ── CrudRepository 방식 ───────────────────────────────────

    // HSET product:{id} id {id} name {name} typeId {typeId} version {version}
    public Product save(Product product) {
        return productRepository.save(product);
    }

    // HGETALL product:{id}
    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    // 전체 id Set 순회 → 각 id 에 대해 HGETALL
    public Iterable<Product> findAll() {
        return productRepository.findAll();
    }

    // @Indexed 보조 인덱스로 조회: product:name:{name} Set → id 목록 → HGETALL
    public List<Product> findByName(String name) {
        return productRepository.findByName(name);
    }

    // DEL product:{id} + 인덱스 Set 정리
    public void delete(String id) {
        productRepository.deleteById(id);
    }

    public boolean existsById(String id) {
        return productRepository.existsById(id);
    }

    // SCARD product (전체 저장 수)
    public long count() {
        return productRepository.count();
    }

    // ── RedisTemplate 방식 (JSON 직렬화) ─────────────────────────

    /**
     * 객체를 JSON 으로 직렬화하여 String 키에 저장
     * Redis 저장 형태: {"@class":"...Product","id":"1","name":"Happy Hacking",...}
     */
    public void saveAsJson(String key, Product product) {
        redisTemplate.opsForValue().set(key, product);
    }

    /**
     * JSON 에서 역직렬화하여 객체 반환
     * @class 필드 덕분에 별도 타입 지정 없이 원본 타입으로 복원
     */
    public Product getAsJson(String key) {
        return (Product) redisTemplate.opsForValue().get(key);
    }
}
