package org.example.springredis.domain.ch4.leaderboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.leaderboard.dto.PlayerScore;
import org.example.springredis.domain.ch4.leaderboard.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ch4/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @PostMapping("/{date}/score")
    public ResponseEntity<Void> addScore(@PathVariable String date,
                                         @RequestBody AddScoreRequest request) {
        leaderboardService.addScore(date, request.userId(), request.score());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{date}/score")
    public ResponseEntity<Double> incrementScore(@PathVariable String date,
                                                  @RequestBody IncrementScoreRequest request) {
        return ResponseEntity.ok(leaderboardService.incrementScore(date, request.userId(), request.delta()));
    }

    @GetMapping("/{date}/top/{n}")
    public ResponseEntity<List<PlayerScore>> getTopN(@PathVariable String date,
                                                                         @PathVariable int n) {
        return ResponseEntity.ok(leaderboardService.getTopN(date, n));
    }

    @GetMapping("/{date}/rank/{userId}")
    public ResponseEntity<Long> getRank(@PathVariable String date, @PathVariable Long userId) {
        return ResponseEntity.ok(leaderboardService.getRank(date, userId));
    }

    @PostMapping("/weekly")
    public ResponseEntity<Void> mergeWeekly(@RequestBody MergeWeeklyRequest request) {
        leaderboardService.mergeWeeklyScore(request.weekKey(), request.dates());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/weekly/{weekKey}/top/{n}")
    public ResponseEntity<List<PlayerScore>> getWeeklyTopN(@PathVariable String weekKey,
                                                                                @PathVariable int n) {
        return ResponseEntity.ok(leaderboardService.getWeeklyTopN(weekKey, n));
    }

    record AddScoreRequest(Long userId, double score) {}
    record IncrementScoreRequest(Long userId, double delta) {}
    record MergeWeeklyRequest(String weekKey, List<String> dates) {}
}
