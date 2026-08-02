package com.gate.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PredictionResponse {

    private Long predictionId;
    private Double predictedScoreMin;
    private Double predictedScoreMax;
    private Double predictedScore;
    private Double cutoffProbability;
    private List<WeakSubject> weakSubjects;
    private List<WeakTopic> weakTopics;  // NEW: Topic-level weak areas
    private String recommendation;
    private Long testId;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeakSubject
     {
        private String subject;
        private Double accuracy;
        private String message;
        // NEW FIELD: Severity level helps categorize weak areas
        // Values: "Critical" (< 40), "High" (40-50), "Medium" (50-60)
        private String severity;
    }
}