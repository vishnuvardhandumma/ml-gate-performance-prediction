package com.gate.backend.service;

import com.gate.backend.client.MLServiceClient;
import com.gate.backend.entity.User;
import com.gate.backend.exception.AppException;
import com.gate.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final MLServiceClient mlServiceClient;
    private final UserRepository userRepository;
    private final PredictionService predictionService;

    /**
     * Generates a personalised AI study plan for the given user.
     * Weak topics are computed automatically from the user's full test history
     * so the plan is always up-to-date, regardless of what the frontend sends.
     *
     * @param email       authenticated user's email
     * @param daysToExam  days until the GATE exam
     * @param hoursPerDay study hours available per day
     * @return ML service plan response enriched with weakTopicsSummary
     */
    public Map<String, Object> generatePlan(String email, int daysToExam, double hoursPerDay) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        // Compute weak topics from full test history
        Map<String, Map<String, PredictionService.TopicStats>> topicAccuracy =
                predictionService.getTopicAccuracyForUser(user);

        List<String> weakSubjects = new ArrayList<>();
        List<Map<String, Object>> weakTopicsSummary = new ArrayList<>();

        for (Map.Entry<String, Map<String, PredictionService.TopicStats>> subjectEntry : topicAccuracy.entrySet()) {
            String subject = subjectEntry.getKey();
            boolean subjectIsWeak = false;

            for (Map.Entry<String, PredictionService.TopicStats> topicEntry : subjectEntry.getValue().entrySet()) {
                String topic = topicEntry.getKey();
                PredictionService.TopicStats stats = topicEntry.getValue();
                double accuracy = stats.getAccuracy();

                if (accuracy < 60) {
                    subjectIsWeak = true;

                    String severity = accuracy < 40 ? "Critical" : accuracy < 50 ? "High" : "Medium";

                    Map<String, Object> topicInfo = new LinkedHashMap<>();
                    topicInfo.put("subject", subject);
                    topicInfo.put("topic", topic);
                    topicInfo.put("accuracy", Math.round(accuracy * 10.0) / 10.0);
                    topicInfo.put("correctCount", stats.correctCount);
                    topicInfo.put("totalCount", stats.totalCount);
                    topicInfo.put("severity", severity);
                    weakTopicsSummary.add(topicInfo);
                }
            }

            if (subjectIsWeak && !weakSubjects.contains(subject)) {
                weakSubjects.add(subject);
            }
        }

        // Sort weak topics: Critical → High → Medium, then by accuracy ascending
        weakTopicsSummary.sort(Comparator
                .comparingInt((Map<String, Object> m) -> severityOrder((String) m.get("severity")))
                .thenComparingDouble(m -> (Double) m.get("accuracy")));

        log.info("User {} weak subjects: {}", email, weakSubjects);
        log.info("User {} weak topics count: {}", email, weakTopicsSummary.size());

        // Extract detailed topics for the ML service
        List<Map<String, String>> weakTopicsDetailed = new ArrayList<>();
        for (Map<String, Object> t : weakTopicsSummary) {
            Map<String, String> dt = new HashMap<>();
            dt.put("subject", (String) t.get("subject")); // e.g. Data Structures
            dt.put("subtopic", (String) t.get("topic"));  // e.g. Stacks
            weakTopicsDetailed.add(dt);
        }

        // Build ML service payload
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("weak_subjects", weakSubjects.isEmpty()
                ? List.of("General Aptitude")
                : weakSubjects);
        payload.put("weak_topics_detailed", weakTopicsDetailed);
        payload.put("days_to_exam", daysToExam);
        payload.put("hours_per_day", hoursPerDay);
        payload.put("total_attempts", 1);

        // Call ML service
        Map<String, Object> mlResult = mlServiceClient.callStudyPlan(payload);

        // Enrich result with weak topic summary for the frontend
        Map<String, Object> enriched = new LinkedHashMap<>(mlResult);
        enriched.put("weakTopicsSummary", weakTopicsSummary);
        enriched.put("weakSubjectsUsed", weakSubjects);
        enriched.put("personalised", !weakTopicsSummary.isEmpty());

        return enriched;
    }

    private int severityOrder(String severity) {
        if ("Critical".equals(severity)) return 0;
        if ("High".equals(severity))     return 1;
        return 2;
    }
}
