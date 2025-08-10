package com.example.demo.controllers;

import com.example.demo.model.Doctor;
import com.example.demo.model.DoctorDto;
import com.example.demo.service.DoctorService;
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
                return ResponseEntity.ok(Map.of(
                        "doctor", doctor,
                        "appointments", List.of(), // Empty for now
                        "totalAppointments", 0,
                        "pendingAppointments", 0
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
}