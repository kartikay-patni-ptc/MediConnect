package com.example.demo.repository;

import com.example.demo.model.AiConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiConsultationRepository extends JpaRepository<AiConsultation, Long> {
	List<AiConsultation> findByPatientIdOrderByCreatedAtDesc(Long patientId);
	List<AiConsultation> findByPatientIdAndConversationIdOrderByMessageOrderAsc(Long patientId, String conversationId);
	List<AiConsultation> findByPatientIdAndConversationIdIsNotNullOrderByCreatedAtDesc(Long patientId);
}



