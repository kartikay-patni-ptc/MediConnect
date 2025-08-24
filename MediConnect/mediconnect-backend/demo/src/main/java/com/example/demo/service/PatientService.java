package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;


    public Patient createPatientProfile(PatientDto dto) {
        System.out.println("=== CREATING PATIENT PROFILE ===");
        System.out.println("User ID: " + dto.getId());

        Optional<User> userOpt = userRepository.findById(dto.getId());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with id: " + dto.getId());
        }

        User user = userOpt.get();
        System.out.println("User found: " + user.getUsername());

        Optional<Patient> existingProfile = patientRepository.findByUserId(dto.getId());
        if (existingProfile.isPresent()) {
            throw new RuntimeException("Patient profile already exists for this user");
        }

        System.out.println("No existing profile found, creating new one...");

        Patient patient = new Patient();
        patient.setFirstName(dto.getFirstName());
        patient.setLastName(dto.getLastName());
        patient.setPhoneNumber(dto.getPhoneNumber());
        patient.setEmail(dto.getEmail());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setAddress(dto.getAddress());
        patient.setEmergencyContact(dto.getEmergencyContact());
        patient.setMedicalHistory(dto.getMedicalHistory());
        patient.setUser(user);

        System.out.println("Saving patient profile...");
        Patient savedPatient = patientRepository.save(patient);
        System.out.println("Patient profile saved with ID: " + savedPatient.getId());

        return savedPatient;
    }


    public Optional<Patient> getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId);
    }

    public Patient updatePatient(Long id, PatientDto patientDto) {
        Optional<Patient> patientOpt = patientRepository.findById(id);
        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Patient not found");
        }

        Patient patient = patientOpt.get();
        patient.setFirstName(patientDto.getFirstName());
        patient.setLastName(patientDto.getLastName());
        patient.setPhoneNumber(patientDto.getPhoneNumber());
        patient.setEmail(patientDto.getEmail());
        patient.setDateOfBirth(patientDto.getDateOfBirth());
        patient.setGender(patientDto.getGender());
        patient.setAddress(patientDto.getAddress());
        patient.setEmergencyContact(patientDto.getEmergencyContact());
        patient.setMedicalHistory(patientDto.getMedicalHistory());

        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
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