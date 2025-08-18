package com.example.demo.service;

import com.example.demo.model.AiConsultation;
import com.example.demo.model.Patient;
import com.example.demo.repository.AiConsultationRepository;
import com.example.demo.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AiConsultationService {

	@Autowired
	private AiConsultationRepository aiConsultationRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DoctorService doctorService;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${ai.gemini.api.key:}")
	private String geminiApiKey;

	@Value("${ai.gemini.model:gemini-1.5-flash}")
	private String geminiModel;

	public Map<String, Object> consult(Long patientId, String question) {
		return consultWithContext(patientId, question, null, null, null);
	}
	
	public Map<String, Object> consultWithContext(Long patientId, String question, String conversationId, String sessionId, Integer messageOrder) {
		Patient patient = patientRepository.findById(patientId).orElse(null);
		
		// Debug logging
		System.out.println("=== AI CONSULTATION REQUEST ===");
		System.out.println("Patient ID: " + patientId);
		System.out.println("Question: " + question);
		System.out.println("Conversation ID: " + conversationId);
		System.out.println("Session ID: " + sessionId);
		System.out.println("Message Order: " + messageOrder);
		
		// Get conversation history for context - try to continue recent conversations within 30 minutes
		List<AiConsultation> conversationHistory = new ArrayList<>();
		boolean continuedExisting = false;
		if (conversationId != null) {
			// Use provided conversation ID
			conversationHistory = getConversationHistory(patientId, conversationId);
			System.out.println("Found " + conversationHistory.size() + " previous messages in conversation: " + conversationId);
			continuedExisting = true;
		} else {
			// Check for recent conversations and continue if last message is within 30 minutes
			System.out.println("🔍 Checking for recent conversations for patient " + patientId);
			List<AiConsultation> recentHistory = aiConsultationRepository.findByPatientIdAndConversationIdIsNotNullOrderByCreatedAtDesc(patientId);
			System.out.println("🔍 Found " + recentHistory.size() + " recent conversations");
			if (!recentHistory.isEmpty()) {
				String recentConversationId = recentHistory.get(0).getConversationId();
				LocalDateTime lastMessageTime = recentHistory.get(0).getCreatedAt();
				Duration sinceLast = Duration.between(lastMessageTime, LocalDateTime.now());
				System.out.println("🔍 Most recent conversation ID: " + recentConversationId + ", last message was " + sinceLast.toMinutes() + " minutes ago");
				if (recentConversationId != null && sinceLast.toMinutes() <= 30) {
					conversationHistory = getConversationHistory(patientId, recentConversationId);
					conversationId = recentConversationId; // Continue the recent conversation
					continuedExisting = true;
					System.out.println("🔄 CONTINUING recent conversation: " + recentConversationId + " with " + conversationHistory.size() + " messages (last message " + sinceLast.toMinutes() + " min ago)");
				} else {
					System.out.println("🆕 Last conversation is too old (" + sinceLast.toMinutes() + " min), starting new conversation");
				}
			} else {
				System.out.println("🆕 No recent conversations found - starting completely fresh");
			}
		}

		// Generate new IDs ONLY if we don't have a conversation ID yet
		if (conversationId == null) {
			conversationId = generateConversationId();
			System.out.println("🆕 Generated new conversation ID: " + conversationId);
		}
		if (sessionId == null) {
			sessionId = generateSessionId();
			System.out.println("Generated new session ID: " + sessionId);
		}
		if (messageOrder == null) {
			messageOrder = conversationHistory.size() + 1;
		}
		
		// Final conversation context summary
		System.out.println("🎯 FINAL CONVERSATION CONTEXT:");
		System.out.println("🎯 Conversation ID: " + conversationId);
		System.out.println("🎯 Session ID: " + sessionId);
		System.out.println("🎯 Message Order: " + messageOrder);
		System.out.println("🎯 History Messages: " + conversationHistory.size());
		
		// Debug: Print conversation history being sent to AI
		System.out.println("=== CONVERSATION HISTORY FOR AI ===");
		for (int i = 0; i < conversationHistory.size(); i++) {
			AiConsultation msg = conversationHistory.get(i);
			System.out.println("Message " + (i+1) + ":");
			System.out.println("  Question: " + msg.getQuestion());
			System.out.println("  Answer: " + msg.getAnswer());
		}
		System.out.println("=== END CONVERSATION HISTORY ===");
		
		Map<String, Object> aiResult = generateAdviceWithGemini(question, conversationHistory);
		String patientAdvice = aiResult.getOrDefault("patientAdvice", aiResult.getOrDefault("answer", "I could not generate advice right now. Please try again.")).toString();
		String doctorSummary = aiResult.getOrDefault("doctorSummary", "").toString();

		if (patient != null) {
			AiConsultation log = new AiConsultation();
			log.setPatient(patient);
			log.setQuestion(question);
			log.setAnswer(patientAdvice);
			log.setConversationId(conversationId);
			log.setSessionId(sessionId);
			log.setMessageOrder(messageOrder);
			log.setDoctorSummary(doctorSummary);
			// TODO: setDoctorSummary (after entity update)
			aiConsultationRepository.save(log);
		}

		Map<String, Object> response = new HashMap<>();
		response.put("answer", patientAdvice);
		response.put("patientAdvice", patientAdvice);
		response.put("doctorSummary", doctorSummary);
		response.put("prescribedMedicines", aiResult.get("prescribedMedicines"));
		response.put("riskLevel", aiResult.get("riskLevel"));
		response.put("redFlags", aiResult.get("redFlags"));
		response.put("homeRemedies", aiResult.get("homeRemedies"));
		String specializationHint = aiResult.getOrDefault("specializationHint", extractSpecialization(question)).toString();
		response.put("specializationHint", specializationHint);
		response.put("specialists", doctorService.searchDoctorsBySpecialization(specializationHint));
		response.put("conversationId", conversationId);
		response.put("sessionId", sessionId);
		response.put("messageOrder", messageOrder + 1);
		response.put("aiUsed", aiResult.get("aiUsed"));
		return response;
	}

	public List<AiConsultation> getHistory(Long patientId) {
		return aiConsultationRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
	}
	
	public List<AiConsultation> getConversationHistory(Long patientId, String conversationId) {
		return aiConsultationRepository.findByPatientIdAndConversationIdOrderByMessageOrderAsc(patientId, conversationId);
	}
	
	private String generateConversationId() {
		return "conv_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
	}
	
	private String generateSessionId() {
		return "session_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
	}

	private Map<String, Object> generateAdviceWithGemini(String question) {
		return generateAdviceWithGemini(question, new ArrayList<>());
	}
	
	private Map<String, Object> generateAdviceWithGemini(String question, List<AiConsultation> conversationHistory) {
		// Fallback to heuristic if API key is missing
		if (geminiApiKey == null || geminiApiKey.isBlank()) {
			Map<String, Object> fallback = new HashMap<>();
			String answer = heuristicAdvice(question);
			fallback.put("answer", answer);
			fallback.put("riskLevel", "LOW");
			fallback.put("redFlags", List.of());
			fallback.put("homeRemedies", List.of("Rest", "Hydration"));
			fallback.put("specializationHint", extractSpecialization(question));
			return fallback;
		}

		String endpoint = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s", geminiModel, geminiApiKey);

		String systemPrompt = "You are MediConnect AI, a medical assistant."
				+ " For every consultation, return BOTH a detailed, actionable patientAdvice and a SOAP-format doctorSummary."
				+ " doctorSummary must be a JSON object with: chiefComplaint, historyOfPresentIllness, medicalHistory, assessment, plan, prescribedMedicines (array of objects: name, dose, frequency, duration, otcOrPrescription), redFlags, specialistRecommendation."
				+ " patientAdvice must be clear, actionable, and safe."
				+ " prescribedMedicines: array of objects (name, dose, frequency, duration, otcOrPrescription). Only suggest OTC medicines unless enough info is available for prescription."
				+ " If the user's input is vague or missing key details (age, duration, severity, history), ask clarifying questions before giving advice."
				+ " If the question is not about health, respond: 'Sorry, I can only assist with medical and health-related questions.' and set all other fields to empty/null."
				+ " Respond STRICTLY as compact JSON with these keys: patientAdvice (string), doctorSummary (object), prescribedMedicines (array), riskLevel (LOW|MEDIUM|HIGH), specializationHint (string), redFlags (array), homeRemedies (array). No other text."
				+ "\n\nGOOD EXAMPLE:"
				+ "\n{\"patientAdvice\":\"You should rest, hydrate, and monitor your fever. If you develop chest pain, shortness of breath, or your fever persists, see a doctor immediately.\"," 
				+ " \"doctorSummary\": { \"chiefComplaint\": \"Fever and chest pain\", \"historyOfPresentIllness\": \"Fever for 2 days, new onset chest pain today, no prior cardiac history.\", \"medicalHistory\": \"No known chronic illnesses.\", \"assessment\": \"Possible viral infection, but chest pain raises concern for myocarditis or other cardiac involvement. Risk: Moderate.\", \"plan\": \"Recommend ECG, cardiac enzymes, and referral to cardiology.\", \"prescribedMedicines\": [{\"name\":\"Paracetamol\",\"dose\":\"500mg\",\"frequency\":\"every 6 hours\",\"duration\":\"3 days\",\"otcOrPrescription\":\"OTC\"}], \"redFlags\":[\"Chest pain with fever\"], \"specialistRecommendation\":\"Cardiology\" },"
				+ " \"prescribedMedicines\": [{\"name\":\"Paracetamol\",\"dose\":\"500mg\",\"frequency\":\"every 6 hours\",\"duration\":\"3 days\",\"otcOrPrescription\":\"OTC\"}], \"riskLevel\":\"MEDIUM\", \"specializationHint\":\"cardio\", \"redFlags\":[\"chest pain\",\"persistent fever\"], \"homeRemedies\":[\"Rest\",\"Hydration\"] }"
				+ "\nBAD EXAMPLE:"
				+ "\n{\"patientAdvice\":\"You should rest and drink water.\", ... }  // (This is too generic and not acceptable)"
				+ "\nBAD EXAMPLE (non-medical):"
				+ "\n{\"patientAdvice\":\"Sorry, I can only assist with medical and health-related questions.\", ... }"
				;

		JSONObject requestBody = new JSONObject();
		// Generation config to coerce JSON
		JSONObject generationConfig = new JSONObject();
		generationConfig.put("responseMimeType", "application/json");
		generationConfig.put("temperature", 0.4);
		requestBody.put("generationConfig", generationConfig);

		// System instruction
		JSONObject systemInstruction = new JSONObject();
		systemInstruction.put("role", "system");
		systemInstruction.put("parts", new JSONArray().put(new JSONObject().put("text", systemPrompt)));
		requestBody.put("systemInstruction", systemInstruction);

		// Build conversation context with summary
		JSONArray contents = new JSONArray();
		
		// Create conversation summary if there's history
		if (conversationHistory != null && !conversationHistory.isEmpty()) {
			System.out.println("📝 Creating conversation summary from " + conversationHistory.size() + " previous messages");
			
			// Use the new structured summary method
			String conversationSummary = buildConversationSummary(conversationHistory, question);
			System.out.println("📋 Conversation Summary: " + conversationSummary);
			
			// Add the conversation summary as a single user message
			JSONObject summaryMessage = new JSONObject();
			summaryMessage.put("role", "user");
			summaryMessage.put("parts", new JSONArray().put(new JSONObject().put("text", conversationSummary)));
			contents.put(summaryMessage);
		} else {
			System.out.println("📝 No conversation history - starting fresh conversation");
			// Add current user message for new conversations
			JSONObject userMessage = new JSONObject();
			userMessage.put("role", "user");
			userMessage.put("parts", new JSONArray().put(new JSONObject().put("text", question)));
			contents.put(userMessage);
		}
		
		requestBody.put("contents", contents);
		
		// Debug: Print the final request being sent to AI
		System.out.println("🚀 Final AI request contents count: " + contents.length());
		System.out.println("🚀 Request body: " + requestBody.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

		try {
			ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
			if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
				return fallbackWithMessage(question);
			}
			JSONObject resp = new JSONObject(response.getBody());
			JSONArray candidates = resp.optJSONArray("candidates");
			if (candidates == null || candidates.isEmpty()) {
				return fallbackWithMessage(question);
			}
			JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
			if (content == null) {
				return fallbackWithMessage(question);
			}
			JSONArray respParts = content.optJSONArray("parts");
			if (respParts == null || respParts.isEmpty()) {
				return fallbackWithMessage(question);
			}
			String text = respParts.getJSONObject(0).optString("text", "");
			if (text.isBlank()) {
				return fallbackWithMessage(question);
			}

			// Attempt to extract JSON if extra tokens are present
			String jsonCandidate = extractJson(text);
			JSONObject json;
			try {
				json = new JSONObject(jsonCandidate);
			} catch (JSONException e) {
				return fallbackWithMessage(question);
			}

			// Parse the response
			String patientAdvice = json.optString("patientAdvice", heuristicAdvice(question));
			String doctorSummaryJson = json.optJSONObject("doctorSummary") != null ? json.optJSONObject("doctorSummary").toString() : "";
			List<Map<String, String>> prescribedMedicines = toMapList(json.optJSONArray("prescribedMedicines"));
			String riskLevel = json.optString("riskLevel", "MEDIUM");
			List<String> redFlags = toStringList(json.optJSONArray("redFlags"));
			List<String> homeRemedies = toStringList(json.optJSONArray("homeRemedies"));
			String specializationHint = json.optString("specializationHint", "");
			boolean aiUsed = true;

			        // Build result map with all fields
        Map<String, Object> result = new HashMap<>();
        result.put("answer", patientAdvice); // Keep for backward compatibility
        result.put("patientAdvice", patientAdvice);
        result.put("doctorSummary", doctorSummaryJson);
        result.put("prescribedMedicines", prescribedMedicines);
        result.put("riskLevel", riskLevel);
        result.put("redFlags", redFlags);
        result.put("homeRemedies", homeRemedies);
        result.put("specializationHint", specializationHint);
        result.put("aiUsed", aiUsed);

			return result;
		} catch (RestClientException ex) {
			return fallbackWithMessage(question);
		}
	}

	private Map<String, Object> fallbackWithMessage(String question) {
		Map<String, Object> fallback = new HashMap<>();
		String advice = heuristicAdvice(question);
		fallback.put("answer", buildAnswerText(advice, List.of("Rest", "Hydration"), List.of()));
		fallback.put("patientAdvice", advice);
		fallback.put("doctorSummary", "Fallback response due to AI service unavailability");
		fallback.put("prescribedMedicines", List.of());
		fallback.put("homeRemedies", List.of("Rest", "Hydration"));
		fallback.put("riskLevel", "LOW");
		fallback.put("redFlags", List.of());
		fallback.put("specializationHint", extractSpecialization(question));
		fallback.put("aiUsed", false);
		return fallback;
	}

	private String extractJson(String text) {
		int start = text.indexOf('{');
		int end = text.lastIndexOf('}');
		if (start >= 0 && end > start) {
			return text.substring(start, end + 1);
		}
		return text;
	}

	private String buildAnswerText(String advice, List<String> remedies, List<String> redFlags) {
		StringBuilder sb = new StringBuilder();
		sb.append(advice);
		if (remedies != null && !remedies.isEmpty()) {
			sb.append("\n\nHome Remedies: ").append(String.join(", ", remedies));
		}
		if (redFlags != null && !redFlags.isEmpty()) {
			sb.append("\n\nWhen to seek care: ").append(String.join(", ", redFlags));
		}
		return sb.toString();
	}

	private String buildDoctorSummary(String patientAdvice, JSONObject json) {
		StringBuilder summary = new StringBuilder();
		summary.append("Summary: ");

		// Extract symptoms
		List<String> symptoms = new ArrayList<>();
		if (json.has("redFlags") && json.optJSONArray("redFlags") != null) {
			for (int i = 0; i < json.optJSONArray("redFlags").length(); i++) {
				String symptom = json.optJSONArray("redFlags").optString(i);
				if (!symptoms.contains(symptom)) {
					symptoms.add(symptom);
				}
			}
		}
		if (!symptoms.isEmpty()) {
			summary.append(String.join(", ", symptoms)).append(".");
		}

		// Extract timeline
		String timeline = json.optString("riskLevel", "immediate");
		if (timeline.equals("MEDIUM")) {
			summary.append(" Timeline: 2-3 days.");
		} else if (timeline.equals("HIGH")) {
			summary.append(" Timeline: 1-2 days.");
		}

		// Extract risk
		String risk = json.optString("riskLevel", "LOW");
		if (risk.equals("MEDIUM")) {
			summary.append(" Risk: moderate.");
		} else if (risk.equals("HIGH")) {
			summary.append(" Risk: high.");
		}

		// Extract red flags
		List<String> redFlags = new ArrayList<>();
		if (json.has("redFlags") && json.optJSONArray("redFlags") != null) {
			for (int i = 0; i < json.optJSONArray("redFlags").length(); i++) {
				String symptom = json.optJSONArray("redFlags").optString(i);
				if (!redFlags.contains(symptom)) {
					redFlags.add(symptom);
				}
			}
		}
		if (!redFlags.isEmpty()) {
			summary.append(" Red flags: ").append(String.join(", ", redFlags)).append(".");
		}

		// Append patient advice
		summary.append("\nRecommendation: ").append(patientAdvice);

		return summary.toString();
	}

	private List<String> toStringList(JSONArray array) {
		List<String> list = new ArrayList<>();
		if (array == null) return list;
		for (int i = 0; i < array.length(); i++) {
			list.add(array.optString(i));
		}
		return list;
	}

	private List<Map<String, String>> toMapList(JSONArray array) {
		List<Map<String, String>> list = new ArrayList<>();
		if (array == null) return list;
		for (int i = 0; i < array.length(); i++) {
			JSONObject item = array.optJSONObject(i);
			if (item != null) {
				Map<String, String> map = new HashMap<>();
				map.put("name", item.optString("name"));
				map.put("dose", item.optString("dose"));
				map.put("frequency", item.optString("frequency"));
				map.put("duration", item.optString("duration"));
				map.put("otcOrPrescription", item.optString("otcOrPrescription"));
				list.add(map);
			}
		}
		return list;
	}

	private String heuristicAdvice(String question) {
		String q = question.toLowerCase();
		if (q.contains("fever") || q.contains("cold")) {
			return "For mild fever or cold, rest, hydrate well, and consider acetaminophen as directed. If symptoms persist beyond 3 days or worsen, consult a doctor.";
		}
		if (q.contains("headache")) {
			return "For a mild headache, rest in a quiet room, hydrate, and consider over-the-counter pain relief. If severe or accompanied by other symptoms, see a neurologist.";
		}
		if (q.contains("stomach") || q.contains("acidity")) {
			return "For mild stomach discomfort, try a bland diet, avoid spicy food, and hydrate. If persistent, consider consulting a gastroenterologist.";
		}
		return "Based on your symptoms, consider rest and hydration. If symptoms persist or worsen, consult a specialist. I can suggest nearby doctors if you tell me your concern (e.g., cardiology, dermatology).";
	}

	private String extractSpecialization(String question) {
		String q = question.toLowerCase();
		if (q.contains("heart") || q.contains("chest") || q.contains("cardio") || q.contains("blood pressure") || q.contains("pulse")) return "cardiology";
		if (q.contains("skin") || q.contains("derma") || q.contains("rash") || q.contains("acne") || q.contains("eczema")) return "dermatology";
		if (q.contains("neuro") || q.contains("headache") || q.contains("migraine") || q.contains("brain") || q.contains("nerve")) return "neurology";
		if (q.contains("ortho") || q.contains("bone") || q.contains("joint") || q.contains("fracture") || q.contains("arthritis")) return "orthopedics";
		if (q.contains("gastro") || q.contains("stomach") || q.contains("digestive") || q.contains("intestine") || q.contains("liver")) return "gastroenterology";
		if (q.contains("ent") || q.contains("ear") || q.contains("nose") || q.contains("throat") || q.contains("sinus")) return "ent";
		if (q.contains("psych") || q.contains("mental") || q.contains("anxiety") || q.contains("depression") || q.contains("stress")) return "psychiatry";
		if (q.contains("uro") || q.contains("urinary") || q.contains("bladder") || q.contains("kidney stone")) return "urology";
		if (q.contains("nephro") || q.contains("kidney") || q.contains("dialysis")) return "nephrology";
		if (q.contains("onco") || q.contains("cancer") || q.contains("tumor") || q.contains("chemotherapy")) return "oncology";
		if (q.contains("lung") || q.contains("breathing") || q.contains("respiratory") || q.contains("asthma") || q.contains("cough")) return "pulmonology";
		if (q.contains("eye") || q.contains("vision") || q.contains("sight") || q.contains("ophthalmology")) return "ophthalmology";
		if (q.contains("pediatric") || q.contains("child") || q.contains("baby") || q.contains("infant")) return "pediatrics";
		if (q.contains("gyneco") || q.contains("pregnancy") || q.contains("menstrual") || q.contains("women")) return "gynecology";
		if (q.contains("dental") || q.contains("tooth") || q.contains("teeth") || q.contains("gum")) return "dentistry";
		return "";
	}
	
	private String buildConversationSummary(List<AiConsultation> conversationHistory, String currentQuestion) {
		if (conversationHistory == null || conversationHistory.isEmpty()) {
			return currentQuestion;
		}
		
		StringBuilder summary = new StringBuilder();
		summary.append("CONVERSATION CONTEXT:\n");
		summary.append("Patient has been discussing the following concerns:\n");
		
		// Extract key symptoms and concerns
		Set<String> symptoms = new HashSet<>();
		Set<String> concerns = new HashSet<>();
		
		for (AiConsultation msg : conversationHistory) {
			String question = msg.getQuestion().toLowerCase();
			// Extract common symptoms
			if (question.contains("fever")) symptoms.add("fever");
			if (question.contains("headache")) symptoms.add("headache");
			if (question.contains("chest") || question.contains("heart")) symptoms.add("chest/heart issues");
			if (question.contains("stomach") || question.contains("pain")) symptoms.add("stomach pain");
			if (question.contains("cough") || question.contains("cold")) symptoms.add("respiratory symptoms");
			if (question.contains("dizzy") || question.contains("weakness")) symptoms.add("dizziness/weakness");
			if (question.contains("skin") || question.contains("rash")) symptoms.add("skin issues");
			if (question.contains("joint") || question.contains("bone")) symptoms.add("musculoskeletal issues");
			
			// Add the actual question context
			concerns.add(msg.getQuestion());
		}
		
		// Build structured summary
		if (!symptoms.isEmpty()) {
			summary.append("Symptoms mentioned: ").append(String.join(", ", symptoms)).append("\n");
		}
		summary.append("\nDetailed conversation:\n");
		
		for (int i = 0; i < conversationHistory.size(); i++) {
			AiConsultation msg = conversationHistory.get(i);
			summary.append(i + 1).append(". Patient: ").append(msg.getQuestion()).append("\n");
			summary.append("   AI Response: ").append(msg.getAnswer()).append("\n\n");
		}
		
		summary.append("CURRENT QUESTION: ").append(currentQuestion);
		summary.append("\n\nINSTRUCTIONS: Analyze the ENTIRE conversation above and provide comprehensive medical advice. ");
		summary.append("Consider ALL symptoms, concerns, and previous advice when formulating your response. ");
		summary.append("If new symptoms are mentioned, combine them with previous symptoms for a complete assessment. ");
		summary.append("Provide detailed, specific, and actionable medical advice based on the complete conversation context.");
		
		return summary.toString();
	}
}



