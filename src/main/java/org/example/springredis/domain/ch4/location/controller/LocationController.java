package org.example.springredis.domain.ch4.location.controller;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.location.entity.Place;
import org.example.springredis.domain.ch4.location.service.LocationService;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ch4/places")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<Place> addPlace(@RequestBody AddPlaceRequest request) {
        return ResponseEntity.ok(locationService.addPlace(request.name(), request.longitude(), request.latitude()));
    }

    @GetMapping("/{name}/position")
    public ResponseEntity<List<Point>> getPosition(@PathVariable String name) {
        return ResponseEntity.ok(locationService.getPosition(name));
    }

    @GetMapping("/search")
    public ResponseEntity<GeoResults<RedisGeoCommands.GeoLocation<Object>>> searchNearby(
            @RequestParam double lon,
            @RequestParam double lat,
            @RequestParam double radius,
            @RequestParam(defaultValue = "10") long count) {
        return ResponseEntity.ok(locationService.searchNearby(lon, lat, radius, count));
    }

    @GetMapping("/search/{placeName}")
    public ResponseEntity<GeoResults<RedisGeoCommands.GeoLocation<Object>>> searchNearbyFrom(
            @PathVariable String placeName,
            @RequestParam double radius,
            @RequestParam(defaultValue = "10") long count) {
        return ResponseEntity.ok(locationService.searchNearbyFrom(placeName, radius, count));
    }

    @GetMapping("/distance")
    public ResponseEntity<Distance> getDistance(@RequestParam String from, @RequestParam String to) {
        return ResponseEntity.ok(locationService.getDistance(from, to));
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> sync() {
        locationService.syncAllToRedis();
        return ResponseEntity.ok().build();
    }

    record AddPlaceRequest(String name, double longitude, double latitude) {}
}
