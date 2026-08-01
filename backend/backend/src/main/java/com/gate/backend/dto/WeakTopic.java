package com.gate.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeakTopic {
    private String subject;
    private String topic;
    private Double accuracy;
    private Integer correctCount;
    private Integer totalCount;
    private String severity;
    private String message;
}