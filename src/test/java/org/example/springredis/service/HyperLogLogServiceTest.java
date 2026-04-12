package org.example.springredis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("HyperLogLog 자료구조")
class HyperLogLogServiceTest {

    @Autowired HyperLogLogService hyperLogLogService;
    @Autowired KeyManagementService keyManagementService;

    @BeforeEach
    void setUp() {
        keyManagementService.flushAll();
    }

    /**
     * PFADD members 123 → 1
     * PFADD members 500 → 1
     * PFADD members 12  → 1
     * PFCOUNT members   → 3
     */
    @Test
    @DisplayName("PFADD members 123/500/12 → PFCOUNT members 3")
    void pfaddAndPfcount() {
        hyperLogLogService.pfadd("members", "123");
        hyperLogLogService.pfadd("members", "500");
        hyperLogLogService.pfadd("members", "12");

        assertThat(hyperLogLogService.pfcount("members")).isEqualTo(3L);
    }

    @Test
    @DisplayName("중복 추가 → 카디널리티 변화 없음")
    void duplicateNotCounted() {
        hyperLogLogService.pfadd("members", "123");
        hyperLogLogService.pfadd("members", "123"); // 중복
        hyperLogLogService.pfadd("members", "123"); // 중복

        assertThat(hyperLogLogService.pfcount("members")).isEqualTo(1L);
    }

    @Test
    @DisplayName("여러 키의 합산 카디널리티 조회")
    void pfcountMultipleKeys() {
        hyperLogLogService.pfadd("hll:1", "a", "b", "c");
        hyperLogLogService.pfadd("hll:2", "c", "d", "e"); // c 는 중복

        // 합산 고유 원소: a, b, c, d, e → 5
        assertThat(hyperLogLogService.pfcount("hll:1", "hll:2")).isEqualTo(5L);
    }
}
