package org.example.springredis.domain.ch4.search.controller;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.search.dto.KeywordEntry;
import org.example.springredis.domain.ch4.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ch4/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/{userId}/keyword")
    public ResponseEntity<Void> addKeyword(@PathVariable Long userId,
                                           @RequestBody KeywordRequest request) {
        searchService.addKeyword(userId, request.keyword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/keywords")
    public ResponseEntity<List<KeywordEntry>> getKeywords(@PathVariable Long userId) {
        return ResponseEntity.ok(searchService.getRecentKeywords(userId));
    }

    @DeleteMapping("/{userId}/keyword/{keyword}")
    public ResponseEntity<Void> deleteKeyword(@PathVariable Long userId,
                                              @PathVariable String keyword) {
        searchService.deleteKeyword(userId, keyword);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/keywords")
    public ResponseEntity<Void> clearHistory(@PathVariable Long userId) {
        searchService.clearHistory(userId);
        return ResponseEntity.ok().build();
    }

    record KeywordRequest(String keyword) {}
}
