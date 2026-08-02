package com.gate.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gate.backend.entity.Question;
import com.gate.backend.service.QuestionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private final QuestionService questionService;

    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getAllQuestions() {
        return ResponseEntity.ok(
            questionService.getAllQuestions());
    }

    @GetMapping("/mocktest/{mockTestNumber}/questions")
    public ResponseEntity<List<Question>> getByMockTest(
            @PathVariable Integer mockTestNumber) {
        return ResponseEntity.ok(
            questionService.getByMockTestNumber(mockTestNumber));
    }

    @GetMapping("/questions/{subject}")
    public ResponseEntity<List<Question>> getBySubject(
            @PathVariable String subject) {
        return ResponseEntity.ok(
            questionService.getBySubject(subject));
    }

    @PostMapping("/questions")
    public ResponseEntity<Question> addQuestion(
            @RequestBody Question question) {
        return ResponseEntity.ok(
            questionService.addQuestion(question));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<Question> updateQuestion(
            @PathVariable Long id,
            @RequestBody Question question) {
        return ResponseEntity.ok(
            questionService.updateQuestion(id, question));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Map<String, String>> deleteQuestion(
            @PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(
            Map.of("message", "Question deleted"));
    }

    @PostMapping("/questions/upload")
public ResponseEntity<Map<String, String>> uploadExcel(
        @RequestParam("file") MultipartFile file) {

    System.out.println("========== UPLOAD HIT ==========");
    System.out.println(file.getOriginalFilename());

    String result = questionService.importFromExcel(file);

    return ResponseEntity.ok(Map.of("message", result));
}

    @PostMapping("/mocktest/{mockTestNumber}/upload")
    public ResponseEntity<Map<String, String>> uploadExcelForMockTest(
            @PathVariable Integer mockTestNumber,
            @RequestParam("file") MultipartFile file) {
        String result = questionService.importFromExcel(file, mockTestNumber);
        return ResponseEntity.ok(Map.of("message", result));
    }
    
}
