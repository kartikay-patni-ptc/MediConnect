package com.example.demo.controllers;

import com.example.demo.model.Doctor;
import com.example.demo.model.Patient;
import com.example.demo.model.PatientDto;
import com.example.demo.service.DoctorService;
import com.example.demo.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @PostMapping("/create-profile")
    public ResponseEntity<?> createProfile(@RequestBody PatientDto patientDto) {
        try {
            Patient patient = patientService.createPatientProfile(patientDto);
            return ResponseEntity.ok(Map.of(
                    "message", "Patient profile created successfully",
                    "patient", patient
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        try {
            var patientOpt = patientService.getPatientByUserId(userId);
            if (patientOpt.isPresent()) {
                return ResponseEntity.ok(patientOpt.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<?> getDashboard(@PathVariable Long userId) {
        try {
            var patientOpt = patientService.getPatientByUserId(userId);
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();



                return ResponseEntity.ok(Map.of(
                        "patient", patient,
                        "doctors", List.of(),
                        "appointments", List.of(),
                        "prescriptions", List.of()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody PatientDto patientDto) {
        try {
            Patient patient = patientService.updatePatient(id, patientDto);
            return ResponseEntity.ok(Map.of(
                    "message", "Patient profile updated successfully",
                    "patient", patient
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}