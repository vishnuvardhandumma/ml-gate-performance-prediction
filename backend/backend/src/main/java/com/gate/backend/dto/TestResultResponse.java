package com.gate.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestResultResponse {

    private Long testId;
    private String subject;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Integer skipped;
    private Double accuracy;
    private Integer timeTakenSecs;
    private String message;
    private java.util.Map<Long, String> answerKey;
}