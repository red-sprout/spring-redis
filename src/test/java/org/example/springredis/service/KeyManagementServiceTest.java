package org.example.springredis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("키 관리 커맨드")
class KeyManagementServiceTest {

    @Autowired KeyManagementService keyManagementService;
    @Autowired StringService stringService;
    @Autowired ListService listService;
    @Autowired SetService setService;
    @Autowired SortedSetService sortedSetService;

    @BeforeEach
    void setUp() {
        keyManagementService.flushAll();
    }

    // ── EXISTS ─────────────────────────────────────────────────

    @Test
    @DisplayName("EXISTS key → 있으면 true, 없으면 false")
    void exists() {
        assertThat(keyManagementService.exists("hello")).isFalse();
        stringService.set("hello", "world");
        assertThat(keyManagementService.exists("hello")).isTrue();
    }

    // ── KEYS ───────────────────────────────────────────────────

    /**
     * h?llo → hello, hallo
     * h*llo → hello, hllo, heeeello
     * h[ae]llo → hello, hallo
     */
    @Test
    @DisplayName("KEYS *er* → members, counter 매칭")
    void keys() {
        stringService.set("members", "1");
        stringService.set("counter", "100");
        stringService.set("hello", "world");

        Set<String> result = keyManagementService.keys("*er*");
        assertThat(result).containsExactlyInAnyOrder("members", "counter");
    }

    // ── SCAN ───────────────────────────────────────────────────

    @Test
    @DisplayName("SCAN MATCH *er* → KEYS 와 동일 결과, Non-blocking")
    void scan() {
        stringService.set("members", "1");
        stringService.set("counter", "100");
        stringService.set("hello", "world");

        List<String> result = keyManagementService.scan("*er*", 100);
        assertThat(result).containsExactlyInAnyOrder("members", "counter");
    }

    @Test
    @DisplayName("SCAN TYPE zset → zset 타입 키만 반환")
    void scanByType() {
        stringService.set("strkey", "val");
        listService.rpush("listkey", "a");
        sortedSetService.zadd("zsetkey", 1.0, "member");

        List<String> result = keyManagementService.scanByType("zset");
        assertThat(result).containsExactly("zsetkey");
    }

    // ── SORT ───────────────────────────────────────────────────

    @Test
    @DisplayName("SORT list → 숫자 오름차순")
    void sort() {
        listService.rpush("nums", "3", "1", "5", "2", "4");

        List<String> result = keyManagementService.sort("nums");
        assertThat(result).containsExactly("1", "2", "3", "4", "5");
    }

    @Test
    @DisplayName("SORT list ALPHA → 사전순 정렬")
    void sortAlpha() {
        listService.rpush("words", "banana", "apple", "cherry");

        List<String> result = keyManagementService.sortAlpha("words");
        assertThat(result).containsExactly("apple", "banana", "cherry");
    }

    @Test
    @DisplayName("SORT list LIMIT 0 3 DESC → 내림차순 3개")
    void sortWithLimit() {
        listService.rpush("nums", "1", "2", "3", "4", "5");

        List<String> result = keyManagementService.sortWithLimit("nums", 0, 3, true);
        assertThat(result).containsExactly("5", "4", "3");
    }

    // ── RENAME / RENAMENX ─────────────────────────────────────

    @Test
    @DisplayName("RENAME key newkey → 키 이름 변경")
    void rename() {
        stringService.set("oldkey", "value");

        keyManagementService.rename("oldkey", "newkey");

        assertThat(keyManagementService.exists("oldkey")).isFalse();
        assertThat(stringService.get("newkey")).isEqualTo("value");
    }

    @Test
    @DisplayName("RENAMENX key newkey → newkey 없을 때만 변경")
    void renameNx() {
        stringService.set("key1", "v1");
        stringService.set("key2", "v2"); // 이미 존재

        Boolean result = keyManagementService.renameNx("key1", "key2");
        assertThat(result).isFalse(); // key2 가 이미 있으므로 변경 안됨
    }

    // ── COPY ───────────────────────────────────────────────────

    @Test
    @DisplayName("COPY source dest → 복사, REPLACE=false 이면 dest 존재 시 실패")
    void copy() {
        stringService.set("src", "hello");

        assertThat(keyManagementService.copy("src", "dst", false)).isTrue();
        assertThat(stringService.get("dst")).isEqualTo("hello");

        // dst 이미 존재 → REPLACE false 이면 실패
        assertThat(keyManagementService.copy("src", "dst", false)).isFalse();

        // REPLACE true 이면 덮어씀
        stringService.set("src", "updated");
        assertThat(keyManagementService.copy("src", "dst", true)).isTrue();
        assertThat(stringService.get("dst")).isEqualTo("updated");
    }

    // ── TYPE ───────────────────────────────────────────────────

    @Test
    @DisplayName("TYPE key → 자료구조 타입 반환")
    void type() {
        stringService.set("strkey", "val");
        listService.rpush("listkey", "a");
        setService.sadd("setkey", "a");
        sortedSetService.zadd("zsetkey", 1.0, "a");

        assertThat(keyManagementService.type("strkey")).isEqualTo("string");
        assertThat(keyManagementService.type("listkey")).isEqualTo("list");
        assertThat(keyManagementService.type("setkey")).isEqualTo("set");
        assertThat(keyManagementService.type("zsetkey")).isEqualTo("zset");
        assertThat(keyManagementService.type("nonexistent")).isEqualTo("none");
    }

    // ── OBJECT ENCODING / IDLETIME ────────────────────────────

    @Test
    @DisplayName("OBJECT ENCODING - 정수 → int, 짧은 문자열 → embstr")
    void objectEncoding() {
        stringService.set("intkey", "12345");
        stringService.set("strkey", "hello");

        assertThat(keyManagementService.objectEncoding("intkey")).isEqualTo("int");
        assertThat(keyManagementService.objectEncoding("strkey")).isEqualTo("embstr");
    }

    @Test
    @DisplayName("OBJECT IDLETIME → 마지막 접근 후 경과 시간(초), 0 이상")
    void objectIdletime() {
        stringService.set("mykey", "val");
        stringService.get("mykey"); // 접근해서 idle time 초기화

        Long idleTime = keyManagementService.objectIdletime("mykey");
        assertThat(idleTime).isGreaterThanOrEqualTo(0L);
    }

    // ── DEL / UNLINK ──────────────────────────────────────────

    @Test
    @DisplayName("DEL key → 동기 삭제, 반환값 삭제된 키 수")
    void del() {
        stringService.set("a", "1");
        stringService.set("b", "2");
        stringService.set("c", "3");

        Long deleted = keyManagementService.del("a", "b");
        assertThat(deleted).isEqualTo(2L);
        assertThat(keyManagementService.exists("a")).isFalse();
        assertThat(keyManagementService.exists("c")).isTrue(); // c 는 삭제 안됨
    }

    @Test
    @DisplayName("UNLINK key → 비동기 삭제 (대용량 키 권장)")
    void unlink() {
        stringService.set("bigkey", "value");

        Long unlinked = keyManagementService.unlink("bigkey");
        assertThat(unlinked).isEqualTo(1L);
        assertThat(keyManagementService.exists("bigkey")).isFalse();
    }

    // ── EXPIRE / TTL / EXPIRETIME ────────────────────────────

    @Test
    @DisplayName("EXPIRE key 100 / TTL key → 100초 이하")
    void expireAndTtl() {
        stringService.set("mykey", "val");

        keyManagementService.expire("mykey", 100L);

        Long ttl = keyManagementService.ttl("mykey");
        assertThat(ttl).isGreaterThan(0L).isLessThanOrEqualTo(100L);
    }

    @Test
    @DisplayName("TTL key (만료 미설정) → -1")
    void ttlNoExpiry() {
        stringService.set("mykey", "val");
        assertThat(keyManagementService.ttl("mykey")).isEqualTo(-1L);
    }

    @Test
    @DisplayName("TTL key (존재하지 않는 키) → -2")
    void ttlNonExistentKey() {
        assertThat(keyManagementService.ttl("nonexistent")).isEqualTo(-2L);
    }

    @Test
    @DisplayName("EXPIREAT key unix-time → 특정 시각 만료 설정")
    void expireAt() {
        stringService.set("mykey", "val");
        Instant future = Instant.now().plusSeconds(300);

        keyManagementService.expireAt("mykey", future);

        Long ttl = keyManagementService.ttl("mykey");
        assertThat(ttl).isGreaterThan(0L).isLessThanOrEqualTo(300L);
    }

    @Test
    @DisplayName("EXPIRETIME key → 만료 유닉스 타임스탬프 반환 (만료 미설정: -1)")
    void expiretime() {
        stringService.set("noexpiry", "val");
        assertThat(keyManagementService.expiretime("noexpiry")).isEqualTo(-1L);

        stringService.set("withexpiry", "val");
        keyManagementService.expire("withexpiry", 300L);
        assertThat(keyManagementService.expiretime("withexpiry"))
                .isGreaterThan(Instant.now().getEpochSecond());
    }

    @Test
    @DisplayName("EXPIRE NX → 만료 미설정 키에만 적용")
    void expireNx() {
        stringService.set("mykey", "val");
        keyManagementService.expire("mykey", 100L); // 이미 만료 설정

        keyManagementService.expireNx("mykey", 999L); // NX: 이미 있으므로 무시

        assertThat(keyManagementService.ttl("mykey")).isLessThanOrEqualTo(100L);
    }

    @Test
    @DisplayName("EXPIRE GT → 새 만료 시간이 더 클 때만 업데이트")
    void expireGt() {
        stringService.set("mykey", "val");
        keyManagementService.expire("mykey", 100L);

        keyManagementService.expireGt("mykey", 50L);  // 50 < 100 → 무시
        assertThat(keyManagementService.ttl("mykey")).isGreaterThan(50L);

        keyManagementService.expireGt("mykey", 200L); // 200 > 100 → 업데이트
        assertThat(keyManagementService.ttl("mykey")).isGreaterThan(100L);
    }

    @Test
    @DisplayName("EXPIRE LT → 새 만료 시간이 더 작을 때만 업데이트")
    void expireLt() {
        stringService.set("mykey", "val");
        keyManagementService.expire("mykey", 100L);

        keyManagementService.expireLt("mykey", 200L); // 200 > 100 → 무시
        assertThat(keyManagementService.ttl("mykey")).isLessThanOrEqualTo(100L);

        keyManagementService.expireLt("mykey", 30L);  // 30 < 100 → 업데이트
        assertThat(keyManagementService.ttl("mykey")).isLessThanOrEqualTo(30L);
    }

    // ── FLUSHALL ──────────────────────────────────────────────

    @Test
    @DisplayName("FLUSHALL → 모든 키 삭제")
    void flushAll() {
        stringService.set("a", "1");
        stringService.set("b", "2");

        keyManagementService.flushAll();

        assertThat(keyManagementService.keys("*")).isEmpty();
    }
}
