package com.gate.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gate.backend.client.MLServiceClient;
import com.gate.backend.dto.FullTestResponse;
import com.gate.backend.dto.PredictionResponse;
import com.gate.backend.dto.TestResultResponse;
import com.gate.backend.dto.WeakTopic;
import com.gate.backend.entity.MockTest;
import com.gate.backend.entity.PredictionResult;
import com.gate.backend.entity.Question;
import com.gate.backend.entity.TestAnswer;
import com.gate.backend.entity.User;
import com.gate.backend.repository.MockTestRepository;
import com.gate.backend.repository.PredictionResultRepository;
import com.gate.backend.repository.TestAnswerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final MLServiceClient mlServiceClient;
    private final PredictionResultRepository predictionResultRepository;
    private final MockTestRepository mockTestRepository;
    private final TestAnswerRepository testAnswerRepository;
    private final ObjectMapper objectMapper;

    public FullTestResponse predict(User user, MockTest mockTest) {

        // Build subject scores from user's test history
        Map<String, Double> subjectScores = buildSubjectScores(user);

        // Calculate specific accuracy for each subject in this 30-question test
        if (mockTest.getSubject() != null && mockTest.getSubject().startsWith("Mock Test")) {
            List<TestAnswer> currentAnswers = testAnswerRepository.findByMockTest(mockTest);
            Map<String, List<TestAnswer>> grouped = new HashMap<>();
            for (TestAnswer ta : currentAnswers) {
                if (ta.getQuestion() != null) {
                    grouped.computeIfAbsent(ta.getQuestion().getSubject(), k -> new ArrayList<>()).add(ta);
                }
            }

            for (String sub : grouped.keySet()) {
                List<TestAnswer> subAns = grouped.get(sub);
                long correct = subAns.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect()).count();
                double subAcc = subAns.size() > 0 ? (double) correct / subAns.size() * 100.0 : 0.0;
                subjectScores.put(sub, subAcc);
            }
        } else {
            // Subject-specific test (10 questions) - only update that subject
            subjectScores.put(mockTest.getSubject(), mockTest.getAccuracy());
        }

        // Count total attempts
        long totalAttempts = mockTestRepository.countByUser(user);

        // Build payload for ML service
        Map<String, Object> payload = new HashMap<>();
        payload.put("subject_scores", subjectScores);
        payload.put("time_taken_secs", mockTest.getTimeTakenSecs() != null
                ? mockTest.getTimeTakenSecs() : 1800);
        payload.put("total_attempts", totalAttempts);

        // Call ML service
        Map<String, Object> mlResult = mlServiceClient.predict(payload);

        // Parse results
        Double scoreMin   = toDouble(mlResult.get("predicted_score_min"));
        Double scoreMax   = toDouble(mlResult.get("predicted_score_max"));
        Double score      = toDouble(mlResult.get("predicted_score"));
        Double cutoffProb = toDouble(mlResult.get("cutoff_probability"));
        String recommendation = (String) mlResult.getOrDefault(
                "recommendation", "Keep practicing!");

        // Parse weak subjects
        List<PredictionResponse.WeakSubject> weakSubjects = new ArrayList<>();
        Object wsObj = mlResult.get("weak_subjects");
        if (wsObj instanceof List<?> wsList) {
            for (Object ws : wsList) {
                if (ws instanceof Map<?,?> wsMap) {
                    PredictionResponse.WeakSubject weakSubject =
                            new PredictionResponse.WeakSubject();
                    weakSubject.setSubject(
                            (String) wsMap.get("subject"));
                    weakSubject.setAccuracy(
                            toDouble(wsMap.get("accuracy")));
                    weakSubject.setMessage(
                            (String) wsMap.get("message"));
                    // NEW: Parse severity level (Critical, High, Medium)
                    weakSubject.setSeverity(
                            (String) wsMap.get("severity"));
                    weakSubjects.add(weakSubject);
                }
            }
        }

        // Calculate weak topics
        Map<String, Map<String, TopicStats>> topicAccuracies = 
            calculateTopicAccuracy(user);

        List<WeakTopic> weakTopics = new ArrayList<>();

        for (String subject : topicAccuracies.keySet()) {
            Map<String, TopicStats> topics = topicAccuracies.get(subject);
            
            for (String topic : topics.keySet()) {
                TopicStats stats = topics.get(topic);
                double accuracy = stats.getAccuracy();
                
                // Detect weak topics: < 60% accuracy
                if (accuracy < 60) {
                    String severity;
                    String message;
                    
                    if (accuracy < 40) {
                        severity = "Critical";
                        message = "Critical weakness in " + topic + "! Immediate focused practice needed.";
                    } else if (accuracy < 50) {
                        severity = "High";
                        message = "Low accuracy in " + topic + ". Priority: Study fundamental concepts.";
                    } else {
                        severity = "Medium";
                        message = "Room for improvement in " + topic + ". Practice more problems.";
                    }
                    
                    weakTopics.add(WeakTopic.builder()
                        .subject(subject)
                        .topic(topic)
                        .accuracy(Math.round(accuracy * 100.0) / 100.0)
                        .correctCount(stats.correctCount)
                        .totalCount(stats.totalCount)
                        .severity(severity)
                        .message(message)
                        .build());
                }
            }
        }

        // Save prediction to database
        String weakSubjectsJson = weakSubjectsToJson(weakSubjects);

        PredictionResult result = PredictionResult.builder()
                .user(user)
                .mockTest(mockTest)
                .predictedScoreMin(scoreMin)
                .predictedScoreMax(scoreMax)
                .cutoffProbability(cutoffProb)
                .weakSubjects(weakSubjectsJson)
                .build();

        PredictionResult saved = predictionResultRepository.save(result);

        // Build PredictionResponse
        PredictionResponse prediction = PredictionResponse.builder()
                .predictionId(saved.getId())
                .predictedScoreMin(scoreMin)
                .predictedScoreMax(scoreMax)
                .predictedScore(score)
                .cutoffProbability(cutoffProb)
                .weakSubjects(weakSubjects)
                .recommendation(recommendation)
                .testId(mockTest.getId())
                .build();

        // Build Answer Key for Review section
        Map<Long, String> answerKey = new HashMap<>();
        List<TestAnswer> currentAnswers = testAnswerRepository.findByMockTest(mockTest);
        for (TestAnswer ta : currentAnswers) {
            if (ta.getQuestion() != null) {
                answerKey.put(ta.getQuestion().getId(), ta.getQuestion().getCorrectOption());
            }
        }

        // Build TestResultResponse
        TestResultResponse testResult = TestResultResponse.builder()
                .testId(mockTest.getId())
                .subject(mockTest.getSubject())
                .totalQuestions(mockTest.getTotalQuestions())
                .correctAnswers(mockTest.getCorrectAnswers())
                .wrongAnswers(mockTest.getWrongAnswers())
                .skipped(mockTest.getTotalQuestions() - (mockTest.getCorrectAnswers() + mockTest.getWrongAnswers()))
                .accuracy(mockTest.getAccuracy())
                .timeTakenSecs(mockTest.getTimeTakenSecs())
                .answerKey(answerKey)
                .message("Test submitted successfully")
                .build();

        // Return complete response with all three components
        return FullTestResponse.builder()
                .testResult(testResult)
                .prediction(prediction)
                .weakTopics(weakTopics)
                .build();
    }

    private Map<String, Double> buildSubjectScores(User user) {
        Map<String, Double> scores = new HashMap<>();
        // Default baselines for a new student
        scores.put("Computer Science", 30.0);
        scores.put("Mathematics", 30.0);
        scores.put("General Aptitude", 30.0);

        List<MockTest> history = mockTestRepository.findByUserOrderByTakenAtDesc(user);
        if (history.isEmpty()) return scores;

        // Process history from OLDEST to NEWEST to apply EMA properly
        List<MockTest> chronHistory = new ArrayList<>(history);
        java.util.Collections.reverse(chronHistory);

        double alpha = 0.3; // Weight for the new test

        for (MockTest test : chronHistory) {
            String subject = test.getSubject();
            double acc = test.getAccuracy();
            
            if (subject != null && subject.startsWith("Mock Test")) {
                scores.put("Computer Science", (acc * alpha) + (scores.get("Computer Science") * (1 - alpha)));
                scores.put("Mathematics", (acc * alpha) + (scores.get("Mathematics") * (1 - alpha)));
                scores.put("General Aptitude", (acc * alpha) + (scores.get("General Aptitude") * (1 - alpha)));
            } else if (scores.containsKey(subject)) {
                scores.put(subject, (acc * alpha) + (scores.get(subject) * (1 - alpha)));
            }
        }
        return scores;
    }

    /** Public entry point for StudyPlanService to reuse topic accuracy logic. */
    public Map<String, Map<String, TopicStats>> getTopicAccuracyForUser(User user) {
        return calculateTopicAccuracy(user);
    }

    // Calculate accuracy by topic across all tests
    private Map<String, Map<String, TopicStats>> calculateTopicAccuracy(User user) {
        Map<String, Map<String, TopicStats>> subjectTopics = new HashMap<>();
        List<MockTest> history = mockTestRepository.findByUserOrderByTakenAtDesc(user);
        
        log.info("Calculating topic accuracy for user: {}, tests count: {}", user.getEmail(), history.size());
        
        for (MockTest test : history) {
            List<TestAnswer> answers = testAnswerRepository.findByMockTest(test);
            log.info("Test: {}, answers count: {}", test.getId(), answers.size());
            
            for (TestAnswer answer : answers) {
                // Ensure question is loaded
                Question question = answer.getQuestion();
                if (question == null) {
                    log.warn("Question is null for answer: {}", answer.getId());
                    continue;
                }
                
                // Shift taxonomy for granular UI reporting: Subject becomes Topic, Topic becomes Subtopic
                String subject = question.getTopic();
                String topic = question.getSubtopic() != null ? question.getSubtopic() : question.getTopic();
                
                if (subject == null || topic == null) {
                    log.warn("Subject or topic is null for question: {}", question.getId());
                    continue;
                }
                
                subjectTopics.putIfAbsent(subject, new HashMap<>());
                subjectTopics.get(subject).putIfAbsent(topic, new TopicStats(0, 0));
                
                TopicStats stats = subjectTopics.get(subject).get(topic);
                stats.totalCount++;
                if (answer.getIsCorrect() != null && answer.getIsCorrect()) {
                    stats.correctCount++;
                }
            }
        }
        
        log.info("Topic accuracy calculation complete. Subjects: {}", subjectTopics.keySet());
        return subjectTopics;
    }

    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double d) return d;
        if (value instanceof Integer i) return i.doubleValue();
        if (value instanceof Number n) return n.doubleValue();
        return 0.0;
    }

    private String weakSubjectsToJson(List<PredictionResponse.WeakSubject> weakSubjects) {
        try {
            return objectMapper.writeValueAsString(weakSubjects);
        } catch (Exception e) {
            return "[]";
        }
    }

    // Helper class to track topic statistics
    public static class TopicStats {
        public int correctCount;
        public int totalCount;
        
        public TopicStats(int correct, int total) {
            this.correctCount = correct;
            this.totalCount = total;
        }
        
        public double getAccuracy() {
            return totalCount > 0 ? (correctCount * 100.0) / totalCount : 0.0;
        }
    }
}