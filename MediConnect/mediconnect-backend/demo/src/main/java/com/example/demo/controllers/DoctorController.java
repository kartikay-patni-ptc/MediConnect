package com.example.demo.controllers;

import com.example.demo.model.Doctor;
import com.example.demo.model.DoctorDto;
import com.example.demo.model.DoctorVerificationRequest;
import com.example.demo.model.Patient;
import com.example.demo.service.DoctorService;
import com.example.demo.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @PostMapping("/create-profile")
    public ResponseEntity<?> createProfile(@RequestBody DoctorDto doctorDto) {
        try {
            Doctor doctor = doctorService.createDoctorProfile(doctorDto);
            return ResponseEntity.ok(Map.of(
                    "message", "Doctor profile created successfully",
                    "doctor", doctor
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        try {
            var doctorOpt = doctorService.getDoctorByUserId(userId);
            if (doctorOpt.isPresent()) {
                return ResponseEntity.ok(doctorOpt.get());
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
            var doctorOpt = doctorService.getDoctorByUserId(userId);
            if (doctorOpt.isPresent()) {
                Doctor doctor = doctorOpt.get();

                // Return empty data - no sample data
                return ResponseEntity.ok(Map.of(
                        "doctor", doctor,
                        "patients", List.of(),
                        "appointments", List.of(),
                        "totalAppointments", 0,
                        "pendingAppointments", 0,
                        "totalPatients", 0
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody DoctorDto doctorDto) {
        try {
            Doctor doctor = doctorService.updateDoctor(id, doctorDto);
            return ResponseEntity.ok(Map.of(
                    "message", "Doctor profile updated successfully",
                    "doctor", doctor
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyDoctor(@RequestBody DoctorVerificationRequest request) {
        try {
            boolean isVerified = doctorService.verifyDoctor(request);
            if (isVerified) {
                return ResponseEntity.ok(Map.of(
                        "message", "Doctor verification successful",
                        "verified", true
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Doctor verification failed",
                        "verified", false
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Doctor>> searchDoctors(@RequestParam(required = false) String specialization) {
        System.out.println("=== DOCTOR CONTROLLER: SEARCH REQUEST ===");
        System.out.println("Specialization parameter: " + specialization);

        List<Doctor> doctors = doctorService.searchDoctorsBySpecialization(specialization);
        System.out.println("Returning " + doctors.size() + " doctors");

        return ResponseEntity.ok(doctors);
    }
}