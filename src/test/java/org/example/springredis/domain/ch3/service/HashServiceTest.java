package org.example.springredis.domain.ch3.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Hash 자료구조")
class HashServiceTest {

    @Autowired HashService hashService;
    @Autowired KeyManagementService keyManagementService;

    @BeforeEach
    void setUp() {
        keyManagementService.flushAll();
    }

    /**
     * HSET Product:123 Name "Happy Hacking"
     * HSET Product:123 TypeID 35
     * HSET Product:123 Version 2002
     */
    @Test
    @DisplayName("HSET 단일 필드 / HGET → 책 예제와 동일")
    void hsetAndHget() {
        hashService.hset("Product:123", "Name", "Happy Hacking");
        hashService.hset("Product:123", "TypeID", "35");
        hashService.hset("Product:123", "Version", "2002");

        assertThat(hashService.hget("Product:123", "TypeID")).isEqualTo("35");
    }

    /**
     * HSET Product:234 Name "Track Ball" TypeID 32
     * HMGET Product:234 Name TypeID → ["Track Ball", "32"]
     */
    @Test
    @DisplayName("HSET 다중 필드 / HMGET Product:234 Name TypeID → [Track Ball, 32]")
    void hmsetAndHmget() {
        hashService.hmset("Product:234", Map.of("Name", "Track Ball", "TypeID", "32"));

        List<Object> result = hashService.hmget("Product:234", "Name", "TypeID");
        assertThat(result).containsExactlyInAnyOrder("Track Ball", "32");
    }

    /**
     * HGETALL Product:234 → Name/Track Ball/TypeID/32
     */
    @Test
    @DisplayName("HGETALL Product:234 → {Name=Track Ball, TypeID=32}")
    void hgetAll() {
        hashService.hmset("Product:234", Map.of("Name", "Track Ball", "TypeID", "32"));

        Map<Object, Object> result = hashService.hgetAll("Product:234");
        assertThat(result)
                .containsEntry("Name", "Track Ball")
                .containsEntry("TypeID", "32");
    }

    @Test
    @DisplayName("HSCAN Product:123 MATCH *ID* → TypeID 포함")
    void hscan() {
        hashService.hset("Product:123", "Name", "Happy Hacking");
        hashService.hset("Product:123", "TypeID", "35");
        hashService.hset("Product:123", "Version", "2002");

        var result = hashService.hscan("Product:123", "*ID*");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKey()).isEqualTo("TypeID");
    }

    @Test
    @DisplayName("키 네이밍 관례 - ':' 구분자로 네임스페이스 표현 (Product:123, Product:234)")
    void keyNamingConvention() {
        hashService.hset("Product:123", "Name", "Happy Hacking");
        hashService.hset("Product:234", "Name", "Track Ball");

        // SCAN 으로 Product:* 패턴 조회 가능
        List<String> keys = keyManagementService.scan("Product:*", 100);
        assertThat(keys).hasSize(2).allMatch(k -> k.startsWith("Product:"));
    }
}
