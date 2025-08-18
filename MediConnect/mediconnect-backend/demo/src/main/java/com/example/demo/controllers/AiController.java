package com.example.demo.controllers;

import com.example.demo.model.AiConsultation;
import com.example.demo.service.AiConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

	@Autowired
	private AiConsultationService aiConsultationService;

	@PostMapping("/consult")
	public ResponseEntity<?> consult(@RequestBody Map<String, Object> body) {
		try {
			Long patientId = Long.valueOf(body.get("patientId").toString());
			String question = body.get("question").toString();
			
			// Extract conversation context
			String conversationId = (String) body.get("conversationId");
			String sessionId = (String) body.get("sessionId");
			Integer messageOrder = body.get("messageOrder") != null ? 
				Integer.valueOf(body.get("messageOrder").toString()) : null;
			
			return ResponseEntity.ok(aiConsultationService.consultWithContext(
				patientId, question, conversationId, sessionId, messageOrder));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
	
	// Test endpoint removed for simplicity

	@GetMapping("/history/{patientId}")
	public ResponseEntity<?> history(@PathVariable Long patientId) {
		try {
			return ResponseEntity.ok(aiConsultationService.getHistory(patientId));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
	
	@GetMapping("/test-context/{patientId}")
	public ResponseEntity<?> testConversationContext(@PathVariable Long patientId) {
		try {
			// Check if there are recent conversations
			List<AiConsultation> recentHistory = aiConsultationService.getHistory(patientId);
			if (!recentHistory.isEmpty()) {
				AiConsultation latest = recentHistory.get(0);
				return ResponseEntity.ok(Map.of(
					"hasRecentConversations", true,
					"latestConversationId", latest.getConversationId(),
					"latestQuestion", latest.getQuestion(),
					"totalMessages", recentHistory.size()
				));
			}
			return ResponseEntity.ok(Map.of("hasRecentConversations", false));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}



