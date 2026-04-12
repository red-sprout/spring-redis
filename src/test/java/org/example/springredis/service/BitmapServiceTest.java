package org.example.springredis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Bitmap 자료구조")
class BitmapServiceTest {

    @Autowired BitmapService bitmapService;
    @Autowired KeyManagementService keyManagementService;

    @BeforeEach
    void setUp() {
        keyManagementService.flushAll();
    }

    /**
     * SETBIT mybitmap 2 1 → (integer) 0  (이전 값)
     * GETBIT mybitmap 2   → (integer) 1
     */
    @Test
    @DisplayName("SETBIT mybitmap 2 1 → 이전 값 0 반환 / GETBIT mybitmap 2 → 1")
    void setbitAndGetbit() {
        Boolean previous = bitmapService.setbit("mybitmap", 2, true);
        assertThat(previous).isFalse(); // 이전 값은 0(false)

        assertThat(bitmapService.getbit("mybitmap", 2)).isTrue();
    }

    /**
     * BITFIELD mybitmap SET u1 6 1 SET u1 10 1 SET u1 14 1
     * BITCOUNT mybitmap → 4 (offset 2, 6, 10, 14 총 4개)
     */
    @Test
    @DisplayName("BITFIELD SET u1 6/10/14 1 + SETBIT 2 → BITCOUNT 4")
    void bitfieldAndBitcount() {
        bitmapService.setbit("mybitmap", 2, true);                    // offset 2 = 1
        bitmapService.bitfield("mybitmap", 6L, 10L, 14L);             // offset 6, 10, 14 = 1

        assertThat(bitmapService.bitcount("mybitmap")).isEqualTo(4L);
    }

    @Test
    @DisplayName("GETBIT 설정하지 않은 offset → 0(false)")
    void getbitUnset() {
        assertThat(bitmapService.getbit("mybitmap", 99)).isFalse();
    }
}
