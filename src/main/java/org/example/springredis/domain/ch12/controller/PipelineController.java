package org.example.springredis.domain.ch12.controller;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch12.service.PipelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ch12/pipeline")
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineService pipelineService;

    // POST /api/ch12/pipeline
    // 파이프라인 실행 후 [SET 결과, INCR 결과(counter 값), GET 결과(name 값)] 순서로 반환
    // curl -X POST http://localhost:8080/api/ch12/pipeline
    // [true,1,"Redi"]
    @PostMapping
    public ResponseEntity<List<Object>> runPipeline() {
        List<Object> results = pipelineService.runPipeline();
        return ResponseEntity.ok(results);
    }
}
