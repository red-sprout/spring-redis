package org.example.springredis.domain.ch3.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Stream 자료구조")
class StreamServiceTest {

    @Autowired StreamService streamService;
    @Autowired KeyManagementService keyManagementService;

    private static final String KEY = "mystream";

    @BeforeEach
    void setUp() {
        keyManagementService.flushAll();
    }

    /**
     * XADD mystream * name Alice action login
     * → 자동 생성 ID 반환 (타임스탬프-시퀀스 형식, 예: 1638125133152-0)
     */
    @Test
    @DisplayName("XADD mystream * name Alice action login → RecordId 반환")
    void xadd() {
        RecordId id = streamService.xadd(KEY, Map.of("name", "Alice", "action", "login"));

        assertThat(id).isNotNull();
        assertThat(id.getValue()).contains("-"); // 타임스탬프-시퀀스 형식
    }

    /**
     * XLEN mystream → 추가된 메시지 수
     */
    @Test
    @DisplayName("XADD 3개 후 XLEN → 3")
    void xlen() {
        streamService.xadd(KEY, Map.of("event", "A"));
        streamService.xadd(KEY, Map.of("event", "B"));
        streamService.xadd(KEY, Map.of("event", "C"));

        assertThat(streamService.xlen(KEY)).isEqualTo(3L);
    }

    /**
     * XREAD COUNT 10 STREAMS mystream 0-0
     * 0-0 → 처음부터 모든 메시지 읽기
     */
    @Test
    @DisplayName("XREAD STREAMS mystream 0-0 → 추가한 메시지 전체 반환")
    void xreadFromBeginning() {
        streamService.xadd(KEY, Map.of("name", "Alice", "action", "login"));
        streamService.xadd(KEY, Map.of("name", "Bob", "action", "purchase"));

        List<MapRecord<String, Object, Object>> records =
                streamService.xread(KEY, "0-0");

        assertThat(records).hasSize(2);
        // 첫 번째 메시지 필드 검증
        assertThat(records.get(0).getValue())
                .containsEntry("name", "Alice")
                .containsEntry("action", "login");
    }

    @Test
    @DisplayName("XREAD STREAMS mystream 특정ID → 그 이후 메시지만 반환")
    void xreadFromId() {
        RecordId firstId = streamService.xadd(KEY, Map.of("seq", "1"));
        streamService.xadd(KEY, Map.of("seq", "2"));
        streamService.xadd(KEY, Map.of("seq", "3"));

        // 첫 번째 메시지 이후만 읽기
        List<MapRecord<String, Object, Object>> records =
                streamService.xread(KEY, firstId.getValue());

        assertThat(records).hasSize(2);
        assertThat(records.get(0).getValue()).containsEntry("seq", "2");
        assertThat(records.get(1).getValue()).containsEntry("seq", "3");
    }
}
