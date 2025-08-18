package com.example.demo.service;

import com.example.demo.model.MedicationHistory;
import com.example.demo.model.MedicationRequest;
import com.example.demo.model.Patient;
import com.example.demo.repository.MedicationHistoryRepository;
import com.example.demo.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationHistoryService {

    @Autowired
    private MedicationHistoryRepository medicationRepo;

    @Autowired
    private PatientRepository patientRepository;

    public List<MedicationHistory> listByPatient(Long patientId) {
        return medicationRepo.findByPatientId(patientId);
    }

    public MedicationHistory add(Long patientId, MedicationRequest req) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        MedicationHistory mh = new MedicationHistory();
        mh.setPatient(patient);
        mh.setMedicationName(req.getMedicationName());
        mh.setDosage(req.getDosage());
        mh.setFrequency(req.getFrequency());
        mh.setStartDate(req.getStartDate());
        mh.setEndDate(req.getEndDate());
        mh.setNotes(req.getNotes());
        return medicationRepo.save(mh);
    }

    public MedicationHistory update(Long id, MedicationRequest req) {
        MedicationHistory mh = medicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication history not found"));
        mh.setMedicationName(req.getMedicationName());
        mh.setDosage(req.getDosage());
        mh.setFrequency(req.getFrequency());
        mh.setStartDate(req.getStartDate());
        mh.setEndDate(req.getEndDate());
        mh.setNotes(req.getNotes());
        return medicationRepo.save(mh);
    }

    public void delete(Long id) {
        if (!medicationRepo.existsById(id)) {
            throw new RuntimeException("Medication history not found");
        }
        medicationRepo.deleteById(id);
    }
}


