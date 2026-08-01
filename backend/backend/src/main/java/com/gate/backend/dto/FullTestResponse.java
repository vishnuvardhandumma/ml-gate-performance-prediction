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
public class FullTestResponse {

    private TestResultResponse testResult;
    private PredictionResponse prediction;
    private List<WeakTopic> weakTopics;
}