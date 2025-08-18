package com.example.demo.model;

import lombok.Data;

@Data
public class ConversationRequest {
    private String question;
    private Long patientId;
    private String conversationId;  // To continue existing conversation
    private String sessionId;       // To identify the chat session
    private Integer messageOrder;   // Order of message in conversation
}
