package com.gate.backend.controller;

import com.gate.backend.service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/study-plan")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generatePlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {

        String email       = userDetails.getUsername();
        int    daysToExam  = request.containsKey("days_to_exam")
                ? ((Number) request.get("days_to_exam")).intValue() : 90;
        double hoursPerDay = request.containsKey("hours_per_day")
                ? ((Number) request.get("hours_per_day")).doubleValue() : 4.0;

        Map<String, Object> result =
                studyPlanService.generatePlan(email, daysToExam, hoursPerDay);

        return ResponseEntity.ok(result);
    }
}