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
public class MockTestQuestionsResponse {

    private int mockTestNumber;
    private String title;
    private int totalQuestions;
    private int totalMarks;
    private List<SectionDTO> sections;
    private PreviousAttempt previousAttempt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SectionDTO {
        private String subject;
        private int questionCount;
        private List<QuestionResponse> questions;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PreviousAttempt {
        private Long testId;
        private Double accuracy;
        private Integer correctAnswers;
        private Integer totalQuestions;
        private String takenAt;
    }
}