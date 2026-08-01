package com.gate.backend.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class TestSubmitRequest {

    @NotBlank(message = "Subject is required")
    private String subject;

    private Integer mockTestNumber;

    @NotEmpty(message = "Answers cannot be empty")
    private Map<Long, String> answers;

    private Integer timeTakenSecs;
}