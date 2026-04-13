package org.example.springredis.domain.ch3.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Sorted Set 자료구조")
class SortedSetServiceTest {

    @Autowired SortedSetService sortedSetService;
    @Autowired KeyManagementService keyManagementService;

    private static final String KEY = "score:220817";

    @BeforeEach
    void setUp() {
        keyManagementService.flushAll();
        // 책 예제와 동일하게 데이터 삽입
        // ZADD score:220817 100 user:B / 150 user:A 150 user:C 200 user:F 300 user:E
        sortedSetService.zadd(KEY, 100, "user:B");
        sortedSetService.zadd(KEY, 150, "user:A");
        sortedSetService.zadd(KEY, 150, "user:C");
        sortedSetService.zadd(KEY, 200, "user:F");
        sortedSetService.zadd(KEY, 300, "user:E");
    }

    /**
     * ZRANGE score:220817 1 3 WITHSCORES → user:A/150, user:C/150, user:F/200
     */
    @Test
    @DisplayName("ZRANGE 1 3 WITHSCORES → user:A(150), user:C(150), user:F(200)")
    void zrangeWithScores() {
        Set<ZSetOperations.TypedTuple<String>> result =
                sortedSetService.zrangeWithScores(KEY, 1, 3);

        List<String> members = result.stream()
                .map(ZSetOperations.TypedTuple::getValue).toList();
        List<Double> scores = result.stream()
                .map(ZSetOperations.TypedTuple::getScore).toList();

        assertThat(members).containsExactly("user:A", "user:C", "user:F");
        assertThat(scores).containsExactly(150.0, 150.0, 200.0);
    }

    /**
     * ZRANGE score:220817 1 3 WITHSCORES REV → user:F(200), user:C(150), user:A(150)
     */
    @Test
    @DisplayName("ZRANGE 1 3 WITHSCORES REV → user:F(200), user:C(150), user:A(150)")
    void zrangeRevWithScores() {
        Set<ZSetOperations.TypedTuple<String>> result =
                sortedSetService.zrangeRevWithScores(KEY, 1, 3);

        List<String> members = result.stream()
                .map(ZSetOperations.TypedTuple::getValue).toList();

        assertThat(members).containsExactly("user:F", "user:C", "user:A");
    }

    /**
     * ZRANGE score:220817 100 150 BYSCORE WITHSCORES
     * → user:B(100), user:A(150), user:C(150)
     */
    @Test
    @DisplayName("ZRANGE BYSCORE 100 150 WITHSCORES → user:B(100), user:A(150), user:C(150)")
    void zrangeByScoreWithScores() {
        Set<ZSetOperations.TypedTuple<String>> result =
                sortedSetService.zrangeByScoreWithScores(KEY, 100, 150);

        List<String> members = result.stream()
                .map(ZSetOperations.TypedTuple::getValue).toList();

        assertThat(members).containsExactly("user:B", "user:A", "user:C");
    }

    /**
     * ZRANGE score:220817 200 +inf BYSCORE WITHSCORES → user:F(200), user:E(300)
     */
    @Test
    @DisplayName("ZRANGE BYSCORE 200 +inf WITHSCORES → user:F(200), user:E(300)")
    void zrangeByScoreFromTo() {
        Set<ZSetOperations.TypedTuple<String>> result =
                sortedSetService.zrangeByScoreWithScores(KEY, 200, Double.POSITIVE_INFINITY);

        List<String> members = result.stream()
                .map(ZSetOperations.TypedTuple::getValue).toList();

        assertThat(members).containsExactly("user:F", "user:E");
    }

    /**
     * ZRANGE score:220817 +inf 200 BYSCORE WITHSCORES REV → user:E(300), user:F(200)
     */
    @Test
    @DisplayName("ZRANGE BYSCORE REV +inf 200 → user:E(300), user:F(200)")
    void zrangeByScoreRev() {
        Set<ZSetOperations.TypedTuple<String>> result =
                sortedSetService.zrangeByScoreRevWithScores(KEY, 200, Double.POSITIVE_INFINITY);

        List<String> members = result.stream()
                .map(ZSetOperations.TypedTuple::getValue).toList();

        assertThat(members).containsExactly("user:E", "user:F");
    }

    /**
     * ZADD mySortedSet 0 apple 0 banana 0 candy 0 dream 0 egg 0 frog
     * ZRANGE mySortedSet (b (f BYLEX → banana, candy, dream, egg
     * '(' = exclusive (해당 값 미포함)
     */
    @Test
    @DisplayName("ZRANGE BYLEX (b (f → banana, candy, dream, egg")
    void zrangeByLex() {
        String lexKey = "mySortedSet";
        sortedSetService.zadd(lexKey, 0, "apple");
        sortedSetService.zadd(lexKey, 0, "banana");
        sortedSetService.zadd(lexKey, 0, "candy");
        sortedSetService.zadd(lexKey, 0, "dream");
        sortedSetService.zadd(lexKey, 0, "egg");
        sortedSetService.zadd(lexKey, 0, "frog");

        // (b (f → b 초과 f 미만 (둘 다 exclusive)
        Set<String> result = sortedSetService.zrangeByLex(lexKey, "b", "f", false, false);
        assertThat(result).containsExactly("banana", "candy", "dream", "egg");
    }

    /**
     * ZADD key NX - 존재하지 않을 때만 삽입
     */
    @Test
    @DisplayName("ZADD NX - 이미 존재하는 멤버 score 는 변경되지 않음")
    void zaddNx() {
        sortedSetService.zaddNx(KEY, 999, "user:B"); // 이미 score=100 존재

        Set<ZSetOperations.TypedTuple<String>> result =
                sortedSetService.zrangeByScoreWithScores(KEY, 100, 100);
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getScore()).isEqualTo(100.0); // 변경 안됨
    }

    /**
     * ZADD key LT - 새 score 가 기존보다 작을 때만 업데이트
     */
    @Test
    @DisplayName("ZADD LT - 새 score 가 기존(100)보다 작으면(50) 업데이트")
    void zaddLt() {
        sortedSetService.zaddLt(KEY, 50, "user:B");  // 100 → 50 (작으므로 업데이트)
        sortedSetService.zaddLt(KEY, 200, "user:B"); // 50 → 200 시도 (크므로 무시)

        Set<ZSetOperations.TypedTuple<String>> result =
                sortedSetService.zrangeByScoreWithScores(KEY, 50, 50);
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getScore()).isEqualTo(50.0);
    }

    /**
     * ZADD key GT - 새 score 가 기존보다 클 때만 업데이트
     */
    @Test
    @DisplayName("ZADD GT - 새 score 가 기존(100)보다 크면(500) 업데이트")
    void zaddGt() {
        sortedSetService.zaddGt(KEY, 500, "user:B");  // 100 → 500 (크므로 업데이트)
        sortedSetService.zaddGt(KEY, 50, "user:B");   // 500 → 50 시도 (작으므로 무시)

        Set<ZSetOperations.TypedTuple<String>> result =
                sortedSetService.zrangeByScoreWithScores(KEY, 500, 500);
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getScore()).isEqualTo(500.0);
    }
}
