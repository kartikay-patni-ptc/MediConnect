package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_consultation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiConsultation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String question;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String answer;

	@Column(nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();
	
	// New fields for conversation memory
	@Column(name = "conversation_id", nullable = true)
	private String conversationId;
	
	@Column(name = "message_order", nullable = true)
	private Integer messageOrder;
	
	@Column(name = "session_id", nullable = true)
	private String sessionId;

    @Column(name = "doctor_summary", columnDefinition = "TEXT")
    private String doctorSummary;
}



