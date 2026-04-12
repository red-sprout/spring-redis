package org.example.springredis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("String 자료구조")
class StringServiceTest {

    @Autowired StringService stringService;
    @Autowired KeyManagementService keyManagementService;

    @BeforeEach
    void setUp() {
        keyManagementService.flushAll();
    }

    @Test
    @DisplayName("SET hello world / GET hello → \"world\"")
    void setAndGet() {
        stringService.set("hello", "world");
        assertThat(stringService.get("hello")).isEqualTo("world");
    }

    @Test
    @DisplayName("SET hello newval NX → nil (키가 이미 존재하므로 저장 안됨)")
    void setNxExistingKey() {
        stringService.set("hello", "world");
        assertThat(stringService.setNx("hello", "newval")).isFalse();
        assertThat(stringService.get("hello")).isEqualTo("world"); // 변경 없음
    }

    @Test
    @DisplayName("SET newkey val NX → OK (키가 없으므로 저장됨)")
    void setNxNewKey() {
        assertThat(stringService.setNx("newkey", "val")).isTrue();
        assertThat(stringService.get("newkey")).isEqualTo("val");
    }

    @Test
    @DisplayName("SET hello newval XX → OK / GET hello → \"newval\"")
    void setXxExistingKey() {
        stringService.set("hello", "world");
        assertThat(stringService.setXx("hello", "newval")).isTrue();
        assertThat(stringService.get("hello")).isEqualTo("newval");
    }

    @Test
    @DisplayName("SET counter 100 / INCR counter → 101 / INCRBY counter 50 → 151")
    void incrAndIncrBy() {
        stringService.set("counter", "100");
        assertThat(stringService.incr("counter")).isEqualTo(101L);
        assertThat(stringService.incrBy("counter", 50L)).isEqualTo(151L);
    }

    @Test
    @DisplayName("DECR counter → 99 / DECRBY counter 50 → 49")
    void decrAndDecrBy() {
        stringService.set("counter", "100");
        assertThat(stringService.decr("counter")).isEqualTo(99L);
        assertThat(stringService.decrBy("counter", 50L)).isEqualTo(49L);
    }

    @Test
    @DisplayName("MSET a 10 b 20 c 30 / MGET a b c → [10, 20, 30]")
    void msetAndMget() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("a", "10");
        map.put("b", "20");
        map.put("c", "30");
        stringService.mset(map);
        assertThat(stringService.mget(List.of("a", "b", "c")))
                .containsExactly("10", "20", "30");
    }
}
