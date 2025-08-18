package com.example.demo.service;

import com.example.demo.model.Allergy;
import com.example.demo.model.MedicalReport;
import com.example.demo.model.MedicationHistory;
import com.example.demo.repository.AllergyRepository;
import com.example.demo.repository.MedicalReportRepository;
import com.example.demo.repository.MedicationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientHistoryService {

	@Autowired
	private MedicationHistoryRepository medicationHistoryRepository;

	@Autowired
	private MedicalReportRepository medicalReportRepository;

	@Autowired
	private AllergyRepository allergyRepository;

	public Map<String, Object> getPatientHistory(Long patientId) {
		List<MedicationHistory> medications = medicationHistoryRepository.findByPatientId(patientId);
		List<Allergy> allergies = allergyRepository.findByPatientId(patientId);
		List<MedicalReport> reports = medicalReportRepository.findByPatientId(patientId);

		Map<String, Object> result = new HashMap<>();
		result.put("medications", medications);
		result.put("allergies", allergies);
		result.put("reports", reports);
		return result;
	}
}



