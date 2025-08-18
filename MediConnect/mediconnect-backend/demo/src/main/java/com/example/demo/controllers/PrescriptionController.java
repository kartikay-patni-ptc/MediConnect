package com.example.demo.controllers;

import com.example.demo.model.*;
import com.example.demo.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @PostMapping("/create")
    public ResponseEntity<?> createPrescription(@RequestBody CreatePrescriptionRequest request) {
        try {
            Prescription prescription = prescriptionService.createPrescription(
                request.getAppointmentId(),
                request.getDiagnosis(),
                request.getSymptoms(),
                request.getDoctorNotes(),
                request.getMedicines()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Prescription created successfully");
            response.put("prescriptionId", prescription.getId());
            response.put("prescriptionNumber", prescription.getPrescriptionNumber());
            response.put("status", prescription.getStatus());
            response.put("issuedDate", prescription.getIssuedDate());
            response.put("validUntil", prescription.getValidUntil());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create prescription: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{prescriptionId}/upload")
    public ResponseEntity<?> uploadPrescriptionFile(
            @PathVariable Long prescriptionId,
            @RequestParam("file") MultipartFile file) {
        try {
            Prescription prescription = prescriptionService.uploadPrescriptionFile(prescriptionId, file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Prescription file uploaded successfully");
            response.put("prescriptionId", prescription.getId());
            response.put("fileUrl", prescription.getPrescriptionImageUrl());
            response.put("originalFileName", prescription.getOriginalFileName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to upload prescription file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientPrescriptions(@PathVariable Long patientId) {
        try {
            List<Prescription> prescriptions = prescriptionService.getPatientPrescriptions(patientId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prescriptions", prescriptions);
            response.put("count", prescriptions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get patient prescriptions: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getDoctorPrescriptions(@PathVariable Long doctorId) {
        try {
            List<Prescription> prescriptions = prescriptionService.getDoctorPrescriptions(doctorId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prescriptions", prescriptions);
            response.put("count", prescriptions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get doctor prescriptions: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/patient/{patientId}/active")
    public ResponseEntity<?> getActivePatientPrescriptions(@PathVariable Long patientId) {
        try {
            List<Prescription> prescriptions = prescriptionService.getActivePrescriptions(patientId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prescriptions", prescriptions);
            response.put("count", prescriptions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get active prescriptions: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{prescriptionId}")
    public ResponseEntity<?> getPrescriptionById(@PathVariable Long prescriptionId) {
        try {
            Optional<Prescription> prescriptionOpt = prescriptionService.getPrescriptionById(prescriptionId);
            
            if (!prescriptionOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Prescription not found");
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prescription", prescriptionOpt.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get prescription: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/number/{prescriptionNumber}")
    public ResponseEntity<?> getPrescriptionByNumber(@PathVariable String prescriptionNumber) {
        try {
            Optional<Prescription> prescriptionOpt = prescriptionService.getPrescriptionByNumber(prescriptionNumber);
            
            if (!prescriptionOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Prescription not found");
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prescription", prescriptionOpt.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get prescription: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{prescriptionId}/status")
    public ResponseEntity<?> updatePrescriptionStatus(
            @PathVariable Long prescriptionId,
            @RequestBody UpdateStatusRequest request) {
        try {
            Prescription prescription = prescriptionService.updatePrescriptionStatus(
                prescriptionId, 
                request.getStatus()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Prescription status updated successfully");
            response.put("prescription", prescription);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update prescription status: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // DTOs
    public static class CreatePrescriptionRequest {
        private Long appointmentId;
        private String diagnosis;
        private String symptoms;
        private String doctorNotes;
        private List<PrescriptionMedicine> medicines;

        // Getters and setters
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
        
        public String getDiagnosis() { return diagnosis; }
        public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
        
        public String getSymptoms() { return symptoms; }
        public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
        
        public String getDoctorNotes() { return doctorNotes; }
        public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }
        
        public List<PrescriptionMedicine> getMedicines() { return medicines; }
        public void setMedicines(List<PrescriptionMedicine> medicines) { this.medicines = medicines; }
    }

    public static class UpdateStatusRequest {
        private Prescription.PrescriptionStatus status;

        public Prescription.PrescriptionStatus getStatus() { return status; }
        public void setStatus(Prescription.PrescriptionStatus status) { this.status = status; }
    }
}
