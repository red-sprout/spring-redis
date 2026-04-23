package org.example.springredis.domain.ch12.dto;

import java.util.Map;

public record AnalysisResult(Map<String, Long> typeCounts, double avgMemoryUsage) {

    public String format() {
        StringBuilder sb = new StringBuilder();
        typeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> sb.append(String.format("Type: '%s', Count: %d%n", e.getKey(), e.getValue())));
        sb.append(String.format("Average Memory Usage: %.3f bytes%n", avgMemoryUsage));
        return sb.toString();
    }
}
