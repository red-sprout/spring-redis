package org.example.springredis.domain.ch12.controller;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch12.dto.AnalysisResult;
import org.example.springredis.domain.ch12.service.ForLoopAnalysisService;
import org.example.springredis.domain.ch12.service.PipelineAnalysisService;
import org.example.springredis.domain.ch12.service.TestDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ch12/analysis")
@RequiredArgsConstructor
public class KeyAnalysisController {

    private final TestDataService       testDataService;
    private final ForLoopAnalysisService  forLoopService;
    private final PipelineAnalysisService pipelineService;

    // POST /api/ch12/analysis/init
    // 테스트 데이터 삽입 (이미 존재하면 skip)
    @PostMapping("/init")
    public ResponseEntity<String> init() {
        boolean inserted = testDataService.init();
        String msg = inserted ? "테스트 데이터 삽입 완료" : "이미 초기화되어 있습니다 (skip)";
        return ResponseEntity.ok(msg);
    }

    // GET /api/ch12/analysis/for-loop
    // time curl -s http://localhost:8080/api/ch12/analysis/for-loop
    @GetMapping("/for-loop")
    public ResponseEntity<String> forLoop() {
        AnalysisResult result = forLoopService.analyze();
        return ResponseEntity.ok(result.format());
    }

    // GET /api/ch12/analysis/pipeline
    // time curl -s http://localhost:8080/api/ch12/analysis/pipeline
    @GetMapping("/pipeline")
    public ResponseEntity<String> pipeline() {
        AnalysisResult result = pipelineService.analyze();
        return ResponseEntity.ok(result.format());
    }
}
