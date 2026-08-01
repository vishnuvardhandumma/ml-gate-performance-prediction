package com.gate.backend.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent?key={apiKey}";

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAiResponse(String userPrompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "Gemini API key is not configured. Please add gemini.api.key to application.properties.";
        }

        try {
            String systemInstruction = "You are a helpful and expert GATE (Graduate Aptitude Test in Engineering) preparation assistant. " +
                "You provide accurate technical explanations, study tips, and motivation. " +
                "If the user asks a question not related to GATE, answer it generally like a professional AI assistant (ChatGPT/Gemini). " +
                "Keep responses concise but thorough. Use clear formatting.";

            // Use Map for request (still flexible)
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            contents.put("role", "user");
            Map<String, String> parts = new HashMap<>();
            parts.put("text", systemInstruction + "\n\nUser Question: " + userPrompt);
            contents.put("parts", Collections.singletonList(parts));
            requestBody.put("contents", Collections.singletonList(contents));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Fetch as a type-safe DTO
            GeminiResponse response = restTemplate.postForObject(GEMINI_API_URL, entity, GeminiResponse.class, apiKey);

            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                Candidate firstCandidate = response.getCandidates().get(0);
                if (firstCandidate.getContent() != null && firstCandidate.getContent().getParts() != null) {
                    List<Part> resParts = firstCandidate.getContent().getParts();
                    if (!resParts.isEmpty()) {
                        return resParts.get(0).getText();
                    }
                }
            }

            return "I connected to the AI, but it didn't provide a valid answer. Please try a different question.";

        } catch (org.springframework.web.client.RestClientResponseException e) {
            log.error("Gemini API Error: {} - {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return "AI Error (" + e.getStatusCode().value() + "): " + e.getMessage();
        } catch (Exception e) {
            log.error("Gemini Connection Error: {}", e.getMessage());
            return "Connection Error: I couldn't reach the AI brain. Check your internet or API key.";
        }
    }

    // Type-safe DTOs for Gemini Response
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiResponse {
        private List<Candidate> candidates;
        public List<Candidate> getCandidates() { return candidates; }
        public void setCandidates(List<Candidate> candidates) { this.candidates = candidates; }
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;
        public Content getContent() { return content; }
        public void setContent(Content content) { this.content = content; }
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private List<Part> parts;
        public List<Part> getParts() { return parts; }
        public void setParts(List<Part> parts) { this.parts = parts; }
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private String text;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
