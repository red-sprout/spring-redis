package org.example.springredis.domain.ch3.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Set 자료구조")
class SetServiceTest {

    @Autowired SetService setService;
    @Autowired KeyManagementService keyManagementService;

    @BeforeEach
    void setUp() {
        keyManagementService.flushAll();
    }

    /**
     * SADD myset A
     * SADD myset A A A C B D D E F F F F G → 중복 제거 후 {A,B,C,D,E,F,G}
     */
    @Test
    @DisplayName("SADD myset A A A C B D D E F F F F G → 중복 제거 {A,B,C,D,E,F,G}")
    void saddDuplicates() {
        setService.sadd("myset", "A");
        setService.sadd("myset", "A", "A", "A", "C", "B", "D", "D", "E", "F", "F", "F", "F", "G");

        Set<String> members = setService.smembers("myset");
        assertThat(members).containsExactlyInAnyOrder("A", "B", "C", "D", "E", "F", "G");
    }

    /**
     * SREM myset B → B 삭제
     * SPOP myset   → 랜덤 원소 반환 및 삭제
     */
    @Test
    @DisplayName("SREM myset B → B 제거 / SPOP myset → 랜덤 원소 반환 후 삭제")
    void sremAndSpop() {
        setService.sadd("myset", "A", "B", "C", "D", "E", "F", "G");

        setService.srem("myset", "B");
        assertThat(setService.smembers("myset")).doesNotContain("B");

        String popped = setService.spop("myset");
        assertThat(popped).isNotNull();
        assertThat(setService.smembers("myset")).doesNotContain(popped);
    }

    /**
     * set:111 = {A,B,C,D,E}
     * set:222 = {D,E,F,G,H}
     * SINTER → {D, E}
     * SUNION → {A,B,C,D,E,F,G,H}
     * SDIFF set:111 set:222 → {A,B,C}
     */
    @Test
    @DisplayName("SINTER set:111 set:222 → {D, E}")
    void sinter() {
        setService.sadd("set:111", "A", "B", "C", "D", "E");
        setService.sadd("set:222", "D", "E", "F", "G", "H");

        assertThat(setService.sinter("set:111", "set:222"))
                .containsExactlyInAnyOrder("D", "E");
    }

    @Test
    @DisplayName("SUNION set:111 set:222 → {A,B,C,D,E,F,G,H}")
    void sunion() {
        setService.sadd("set:111", "A", "B", "C", "D", "E");
        setService.sadd("set:222", "D", "E", "F", "G", "H");

        assertThat(setService.sunion("set:111", "set:222"))
                .containsExactlyInAnyOrder("A", "B", "C", "D", "E", "F", "G", "H");
    }

    @Test
    @DisplayName("SDIFF set:111 set:222 → {A, B, C}")
    void sdiff() {
        setService.sadd("set:111", "A", "B", "C", "D", "E");
        setService.sadd("set:222", "D", "E", "F", "G", "H");

        assertThat(setService.sdiff("set:111", "set:222"))
                .containsExactlyInAnyOrder("A", "B", "C");
    }

    @Test
    @DisplayName("SSCAN myset MATCH *A* → A 포함")
    void sscan() {
        setService.sadd("myset", "A", "B", "C", "AB");

        var result = setService.sscan("myset", "*A*");
        assertThat(result).containsExactlyInAnyOrder("A", "AB");
    }
}
