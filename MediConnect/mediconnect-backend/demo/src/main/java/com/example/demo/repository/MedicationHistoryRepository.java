package com.example.demo.repository;

import com.example.demo.model.MedicationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationHistoryRepository extends JpaRepository<MedicationHistory, Long> {
	List<MedicationHistory> findByPatientId(Long patientId);
}



