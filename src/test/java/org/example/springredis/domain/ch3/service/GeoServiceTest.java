package org.example.springredis.domain.ch3.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
@DisplayName("Geospatial 자료구조")
class GeoServiceTest {

    @Autowired GeoService geoService;
    @Autowired KeyManagementService keyManagementService;

    private static final String KEY = "travel";

    @BeforeEach
    void setUp() {
        keyManagementService.flushAll();
        // 책 예제와 동일하게 데이터 삽입
        // GEOADD travel 14.39970 50.09924 prague
        // GEOADD travel 127.0016985 37.56421235 seoul -122.434538 37.785304 SanFrancisco
        geoService.geoadd(KEY, 14.39970, 50.09924, "prague");
        geoService.geoadd(KEY, 127.0016985, 37.56421235, "seoul");
        geoService.geoadd(KEY, -122.434538, 37.785304, "SanFrancisco");
    }

    /**
     * GEOPOS travel prague → 14.39969927072525, 50.099238974551746
     * Redis 는 geohash 인코딩으로 저장해서 미세한 부동소수점 오차 발생
     */
    @Test
    @DisplayName("GEOPOS travel prague → 약 (14.3997, 50.0992)")
    void geopos() {
        List<Point> positions = geoService.geopos(KEY, "prague");

        assertThat(positions).hasSize(1);
        Point prague = positions.get(0);
        assertThat(prague.getX()).isCloseTo(14.39970, within(0.0001)); // longitude
        assertThat(prague.getY()).isCloseTo(50.09924, within(0.0001)); // latitude
    }

    /**
     * GEODIST travel seoul prague KM → "8252.9961"
     */
    @Test
    @DisplayName("GEODIST travel seoul prague KM → 약 8252 km")
    void geodist() {
        Distance distance = geoService.geodist(KEY, "seoul", "prague");

        assertThat(distance.getValue()).isCloseTo(8252.9961, within(1.0));
        assertThat(distance.getMetric()).isEqualTo(Metrics.KILOMETERS);
    }

    @Test
    @DisplayName("GEODIST travel seoul SanFrancisco KM → 약 9000 km 이상")
    void geodistSeoulToSF() {
        Distance distance = geoService.geodistWithUnit(KEY, "seoul", "SanFrancisco", Metrics.KILOMETERS);

        assertThat(distance.getValue()).isGreaterThan(9000);
    }

    @Test
    @DisplayName("GEOPOS 여러 멤버 동시 조회")
    void geoposMultiple() {
        List<Point> positions = geoService.geopos(KEY, "prague", "seoul", "SanFrancisco");

        assertThat(positions).hasSize(3);
        assertThat(positions).noneMatch(p -> p == null);
    }
}
