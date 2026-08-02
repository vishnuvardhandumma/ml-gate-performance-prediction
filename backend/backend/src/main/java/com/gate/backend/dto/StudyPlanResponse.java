package com.gate.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudyPlanResponse {
    private Integer totalWeeks;
    private Integer daysToExam;
    private Double hoursPerDay;
    private List<String> weakSubjects;
    private String phase;
    private List<Map<String, Object>> weeklyPlan;
    private String overallAdvice;
    private List<Map<String, Object>> dailySchedule;
}