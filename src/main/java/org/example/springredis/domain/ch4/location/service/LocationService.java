package org.example.springredis.domain.ch4.location.service;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.location.entity.Place;
import org.example.springredis.domain.ch4.location.repository.PlaceRepository;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private static final String GEO_KEY = "places";

    private final PlaceRepository placeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // MySQL 저장 + GEOADD places {lon} {lat} {name}
    public Place addPlace(String name, double longitude, double latitude) {
        Place place = placeRepository.save(Place.builder()
                .name(name).longitude(longitude).latitude(latitude).build());
        redisTemplate.opsForGeo().add(GEO_KEY, new Point(longitude, latitude), name);
        return place;
    }

    // GEOPOS places {name}
    public List<Point> getPosition(String name) {
        return redisTemplate.opsForGeo().position(GEO_KEY, name);
    }

    // GEOSEARCH places FROMLONLAT {lon} {lat} BYRADIUS {radius} KM ASC COUNT {count}
    public GeoResults<RedisGeoCommands.GeoLocation<Object>> searchNearby(
            double longitude, double latitude, double radiusKm, long count) {
        return redisTemplate.opsForGeo().search(
                GEO_KEY,
                GeoReference.fromCoordinate(new Point(longitude, latitude)),
                new Distance(radiusKm, Metrics.KILOMETERS),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().sortAscending().limit(count)
        );
    }

    // GEOSEARCH places FROMMEMBER {placeName} BYRADIUS {radius} KM ASC COUNT {count}
    public GeoResults<RedisGeoCommands.GeoLocation<Object>> searchNearbyFrom(
            String placeName, double radiusKm, long count) {
        return redisTemplate.opsForGeo().search(
                GEO_KEY,
                GeoReference.fromMember(placeName),
                new Distance(radiusKm, Metrics.KILOMETERS),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().sortAscending().limit(count)
        );
    }

    // GEODIST places {place1} {place2} KM
    public Distance getDistance(String place1, String place2) {
        return redisTemplate.opsForGeo().distance(GEO_KEY, place1, place2, Metrics.KILOMETERS);
    }

    public List<Place> getAllPlaces() {
        return placeRepository.findAll();
    }

    // MySQL 전체 → Redis 동기화
    public void syncAllToRedis() {
        placeRepository.findAll().forEach(p ->
                redisTemplate.opsForGeo().add(GEO_KEY, new Point(p.getLongitude(), p.getLatitude()), p.getName())
        );
    }
}
