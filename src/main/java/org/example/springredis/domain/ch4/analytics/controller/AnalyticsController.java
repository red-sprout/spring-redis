package org.example.springredis.domain.ch4.analytics.controller;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ch4/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/visit/{date}/{userId}")
    public ResponseEntity<Void> recordVisit(@PathVariable String date, @PathVariable long userId) {
        analyticsService.recordVisit(date, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dau/{date}")
    public ResponseEntity<Map<String, Long>> getDau(@PathVariable String date) {
        return ResponseEntity.ok(Map.of("dau", analyticsService.getDau(date)));
    }

    @GetMapping("/visit/{date}/{userId}")
    public ResponseEntity<Map<String, Boolean>> hasVisited(@PathVariable String date, @PathVariable long userId) {
        return ResponseEntity.ok(Map.of("visited", analyticsService.hasVisited(date, userId)));
    }

    @PostMapping("/retained")
    public ResponseEntity<Map<String, Long>> getRetainedUsers(@RequestBody RetainedRequest request) {
        return ResponseEntity.ok(Map.of("retainedUsers", analyticsService.getRetainedUsers(request.dates())));
    }

    @PostMapping("/api-call/{userId}/{logId}")
    public ResponseEntity<Void> recordApiCall(@PathVariable Long userId, @PathVariable String logId) {
        analyticsService.recordApiCall(userId, logId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api-call/{userId}")
    public ResponseEntity<Map<String, Long>> getApiCallCount(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("apiCallCount", analyticsService.getApiCallCount(userId)));
    }

    record RetainedRequest(List<String> dates) {}
}
