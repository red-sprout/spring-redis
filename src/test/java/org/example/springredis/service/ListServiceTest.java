package org.example.springredis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("List 자료구조")
class ListServiceTest {

    @Autowired ListService listService;
    @Autowired KeyManagementService keyManagementService;

    @BeforeEach
    void setUp() {
        keyManagementService.flushAll();
    }

    /**
     * LPUSH mylist E       → [E]
     * RPUSH mylist B       → [E, B]
     * LPUSH mylist D A C B A → [A, B, C, A, D, E, B]
     * LRANGE mylist 0 -1  → A B C A D E B
     */
    @Test
    @DisplayName("LPUSH / RPUSH / LRANGE 0 -1")
    void lpushRpushLrange() {
        listService.lpush("mylist", "E");
        listService.rpush("mylist", "B");
        listService.lpush("mylist", "D", "A", "C", "B", "A");

        assertThat(listService.lrange("mylist", 0, -1))
                .containsExactly("A", "B", "C", "A", "D", "E", "B");
    }

    @Test
    @DisplayName("LRANGE mylist 0 3 → [A, B, C, A]")
    void lrangePartial() {
        listService.lpush("mylist", "E");
        listService.rpush("mylist", "B");
        listService.lpush("mylist", "D", "A", "C", "B", "A");

        assertThat(listService.lrange("mylist", 0, 3))
                .containsExactly("A", "B", "C", "A");
    }

    /**
     * LPOP mylist     → "A"  (제거 후 [B, C, A, D, E, B])
     * LPOP mylist 2   → [B, C]  (제거 후 [A, D, E, B])
     */
    @Test
    @DisplayName("LPOP mylist → \"A\" / LPOP mylist 2 → [B, C]")
    void lpop() {
        listService.lpush("mylist", "D", "A", "C", "B", "A");

        assertThat(listService.lpop("mylist")).isEqualTo("A");
        assertThat(listService.lpopCount("mylist", 2)).containsExactly("B", "C");
    }

    /**
     * LTRIM mylist 0 1 → [A, D] (0~1 인덱스 범위만 남기고 나머지 삭제)
     */
    @Test
    @DisplayName("LTRIM mylist 0 1 → [A, D]")
    void ltrim() {
        listService.lpush("mylist", "B", "E", "D", "A");  // [A, D, E, B]

        listService.ltrim("mylist", 0, 1);

        assertThat(listService.lrange("mylist", 0, -1))
                .containsExactly("A", "D");
    }

    /**
     * LINSERT mylist BEFORE B E
     * before: [A, B, C, D] → after: [A, E, B, C, D]
     */
    @Test
    @DisplayName("LINSERT BEFORE B E → [A, E, B, C, D]")
    void linsertBefore() {
        listService.rpush("mylist", "A", "B", "C", "D");

        listService.linsertBefore("mylist", "B", "E");

        assertThat(listService.lrange("mylist", 0, -1))
                .containsExactly("A", "E", "B", "C", "D");
    }

    /**
     * LSET mylist 2 F → index 2 를 F 로 교체
     * LINDEX mylist 3 → "C"
     */
    @Test
    @DisplayName("LSET mylist 2 F / LINDEX mylist 3 → \"C\"")
    void lsetAndLindex() {
        listService.rpush("mylist", "A", "E", "B", "C", "D");

        listService.lset("mylist", 2, "F");

        assertThat(listService.lrange("mylist", 0, -1))
                .containsExactly("A", "E", "F", "C", "D");
        assertThat(listService.lindex("mylist", 3)).isEqualTo("C");
    }

    @Test
    @DisplayName("고정 길이 큐 관리: LPUSH + LTRIM으로 최근 3개만 유지")
    void fixedLengthQueue() {
        for (int i = 1; i <= 5; i++) {
            listService.lpush("logdata", "log" + i);
            listService.ltrim("logdata", 0, 2); // 최근 3개만 유지
        }
        assertThat(listService.lrange("logdata", 0, -1))
                .hasSize(3)
                .containsExactly("log5", "log4", "log3");
    }
}
