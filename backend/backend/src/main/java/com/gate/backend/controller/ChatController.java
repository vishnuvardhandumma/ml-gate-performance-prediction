package com.gate.backend.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gate.backend.service.GeminiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@org.springframework.web.bind.annotation.CrossOrigin(origins = "*")
public class ChatController {

    private final GeminiService geminiService;

    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody Map<String, String> request) {
        String userQuery = request.get("query");
        if (userQuery == null || userQuery.isEmpty()) {
            return Collections.singletonMap("reply", "I didn't hear anything! What's on your mind?");
        }

        String aiReply = geminiService.getAiResponse(userQuery);
        return Collections.singletonMap("reply", aiReply);
    }
}
