package org.example.springredis.service;

import org.example.springredis.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("CrudRepository — @RedisHash")
class ProductServiceTest {

    @Autowired ProductService productService;
    @Autowired KeyManagementService keyManagementService;

    @BeforeEach
    void setUp() {
        keyManagementService.flushAll();
    }

    /**
     * save → findById
     * 내부적으로: HSET product:1 id 1 name "Happy Hacking" typeId 35 version 2002
     *            HGETALL product:1
     */
    @Test
    @DisplayName("save / findById → 저장 후 필드 검증")
    void saveAndFindById() {
        Product product = new Product("1", "Happy Hacking", 35, "2002", null);
        productService.save(product);

        Optional<Product> found = productService.findById("1");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Happy Hacking");
        assertThat(found.get().getTypeId()).isEqualTo(35);
        assertThat(found.get().getVersion()).isEqualTo("2002");
    }

    @Test
    @DisplayName("findAll → 저장한 전체 객체 반환")
    void findAll() {
        productService.save(new Product("1", "Happy Hacking", 35, "2002", null));
        productService.save(new Product("2", "Track Ball", 32, "2001", null));

        assertThat(productService.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("count → 저장 수 반환")
    void count() {
        productService.save(new Product("1", "Happy Hacking", 35, "2002", null));
        productService.save(new Product("2", "Track Ball", 32, "2001", null));

        assertThat(productService.count()).isEqualTo(2L);
    }

    /**
     * @Indexed 필드(name)로 보조 인덱스 조회
     * 내부적으로: SMEMBERS product:name:Happy Hacking → id 목록 → 각 HGETALL
     */
    @Test
    @DisplayName("findByName → @Indexed 보조 인덱스로 조회")
    void findByName() {
        productService.save(new Product("1", "Happy Hacking", 35, "2002", null));
        productService.save(new Product("2", "Track Ball", 32, "2001", null));
        productService.save(new Product("3", "Happy Hacking", 36, "2003", null)); // 동일 name

        List<Product> result = productService.findByName("Happy Hacking");
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> "Happy Hacking".equals(p.getName()));
    }

    @Test
    @DisplayName("existsById → true / deleteById → false")
    void deleteById() {
        productService.save(new Product("1", "Happy Hacking", 35, "2002", null));

        assertThat(productService.existsById("1")).isTrue();

        productService.delete("1");

        assertThat(productService.existsById("1")).isFalse();
    }

    /**
     * @TimeToLive → 초 단위 TTL 설정
     */
    @Test
    @DisplayName("@TimeToLive → TTL 10초로 저장된 키가 존재함")
    void timeToLive() {
        Product product = new Product("1", "Temp", 0, "v1", 10L);
        productService.save(product);

        assertThat(productService.existsById("1")).isTrue();
    }

    // ── objectRedisTemplate (JSON 직렬화) ────────────────────

    @Test
    @DisplayName("objectRedisTemplate — 객체를 JSON 으로 저장 후 원본 타입으로 복원")
    void saveAndGetAsJson() {
        Product original = new Product("1", "Happy Hacking", 35, "2002", null);

        productService.saveAsJson("cache:product:1", original);
        Product restored = productService.getAsJson("cache:product:1");

        assertThat(restored).isNotNull();
        assertThat(restored.getId()).isEqualTo("1");
        assertThat(restored.getName()).isEqualTo("Happy Hacking");
    }
}
