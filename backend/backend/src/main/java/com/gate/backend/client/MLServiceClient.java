package com.gate.backend.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MLServiceClient {

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> predict(Map<String, Object> payload) {
        try {
            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mlServiceUrl + "/predict"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(
                        response.body(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            } else {
                log.error("ML service returned status: {}",
                        response.statusCode());
                return getDefaultPrediction();
            }

        } catch (Exception e) {
            log.error("Failed to call ML service: {}", e.getMessage());
            return getDefaultPrediction();
        }
    }

    private Map<String, Object> getDefaultPrediction() {
        return Map.of(
            "predicted_score_min",  0.0,
            "predicted_score_max",  0.0,
            "predicted_score",      0.0,
            "cutoff_probability",   0.0,
            "weak_subjects",        java.util.List.of(),
            "recommendation",       "ML service unavailable. Try again later."
        );
    }

    public Map<String, Object> callStudyPlan(Map<String, Object> payload) {
    try {
        String requestBody = objectMapper.writeValueAsString(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mlServiceUrl + "/study-plan"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(15))
                .build();
        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        }
        return Map.of("error", "Study plan service unavailable");
    } catch (Exception e) {
        log.error("Study plan error: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }
}




}