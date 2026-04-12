package org.example.springredis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Redis Geospatial 자료구조 실습
 * 경도/위도 데이터 쌍의 집합
 * 내부적으로 Sorted Set 으로 저장 (geohash 인코딩)
 */
@Service
@RequiredArgsConstructor
public class GeoService {

    private final RedisTemplate<String, String> redisTemplate;

    // GEOADD key longitude latitude member - 위치 정보 저장
    public Long geoadd(String key, double longitude, double latitude, String member) {
        return redisTemplate.opsForGeo().add(key, new Point(longitude, latitude), member);
    }

    // GEOPOS key member [member ...] - 저장된 경도/위도 조회
    public List<Point> geopos(String key, String... members) {
        return redisTemplate.opsForGeo().position(key, members);
    }

    // GEODIST key member1 member2 KM - 두 지점 간 거리 조회 (km 단위)
    public Distance geodist(String key, String member1, String member2) {
        return redisTemplate.opsForGeo().distance(key, member1, member2, Metrics.KILOMETERS);
    }

    // GEODIST key member1 member2 [unit] - 단위 지정 거리 조회
    // Metrics.KILOMETERS, Metrics.MILES, Metrics.NEUTRAL(m) 지원
    public Distance geodistWithUnit(String key, String member1, String member2, Metrics unit) {
        return redisTemplate.opsForGeo().distance(key, member1, member2, unit);
    }
}
