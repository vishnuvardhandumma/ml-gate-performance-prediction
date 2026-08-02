package com.gate.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {

    private Long id;
    private String subject;
    private String topic;
    private String subtopic;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Integer marks;
    private Integer difficulty;
    // Note: correctOption is NOT included — never send answer to frontend
}