package com.example.demo.model;

import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
public class ConversationResponse {
    private String answer;
    private String homeRemedies;
    private String riskLevel;
    private String redFlags;
    private String specializationHint;
    private List<String> specialists;
    private String conversationId;  // To continue the conversation
    private String sessionId;       // Session identifier
    private Integer messageOrder;   // Next message order
    private boolean aiUsed;        // Whether Gemini AI was used
    private List<ConversationMessage> conversationHistory; // Previous messages for context
}

@Data
class ConversationMessage {
    private String question;
    private String answer;
    private LocalDateTime timestamp;
    private Integer messageOrder;
}
