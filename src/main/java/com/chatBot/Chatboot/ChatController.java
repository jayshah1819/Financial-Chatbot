package com.chatBot.Chatboot;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.HashMap;

@RestController
public class ChatController {
    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.getOrDefault("message", "");
        String reply;
        try {
            reply = AIChatbot.getAIResponse(userMessage);
        } catch (Exception e) {
            reply = "Sorry, there was an error: " + e.getMessage();
        }
        Map<String, String> response = new HashMap<>();
        response.put("reply", reply);
        return ResponseEntity.ok(response);
    }
}
