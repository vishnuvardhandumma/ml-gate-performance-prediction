package com.gate.backend.controller;

import com.gate.backend.dto.FullTestResponse;
import com.gate.backend.dto.MockTestQuestionsResponse;
import com.gate.backend.dto.QuestionResponse;
import com.gate.backend.dto.TestResultResponse;
import com.gate.backend.dto.TestSubmitRequest;
import com.gate.backend.service.MockTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tests")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MockTestController {

    private final MockTestService mockTestService;

    @GetMapping("/questions/{subject}")
    public ResponseEntity<List<QuestionResponse>> getQuestions(
            @PathVariable String subject) {
        return ResponseEntity.ok(
            mockTestService.getQuestions(subject));
    }

    @GetMapping("/mocktest/{number}")
    public ResponseEntity<MockTestQuestionsResponse> getMockTest(
            @PathVariable int number,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            mockTestService.getMockTestQuestions(
                number, userDetails.getUsername()));
    }

    @PostMapping("/submit")
    public ResponseEntity<FullTestResponse> submitTest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TestSubmitRequest request) {
        return ResponseEntity.ok(
            mockTestService.submitTest(
                userDetails.getUsername(), request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<TestResultResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            mockTestService.getHistory(
                userDetails.getUsername()));
    }
}