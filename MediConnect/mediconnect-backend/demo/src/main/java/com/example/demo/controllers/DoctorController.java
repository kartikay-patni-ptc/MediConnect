package com.example.demo.controllers;

import com.example.demo.model.*;
import com.example.demo.service.DoctorService;
import com.example.demo.service.DoctorSlotService;
import com.example.demo.service.PatientService;
import com.example.demo.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

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

                // Get doctor's appointments
                List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(doctor.getId());

                // Transform appointments to match frontend expectations
                List<Map<String, Object>> appointmentData = appointments.stream()
                        .map(apt -> {
                            Map<String, Object> appointmentMap = new HashMap<>();
                            appointmentMap.put("id", apt.getId().toString());
                            appointmentMap.put("patientName", apt.getPatient().getFirstName() + " " + apt.getPatient().getLastName());
                            appointmentMap.put("time", apt.getSlot().getStartTime().toString());
                            appointmentMap.put("type", "Consultation");
                            appointmentMap.put("status", apt.getStatus().toString());
                            appointmentMap.put("notes", apt.getNotes());
                            appointmentMap.put("aiSummary", apt.getAiSummary());
                            appointmentMap.put("doctorSummary", apt.getDoctorSummary());

                            // Debug logging to verify data
                            System.out.println("=== APPOINTMENT DATA ===");
                            System.out.println("ID: " + apt.getId());
                            System.out.println("Notes: " + apt.getNotes());
                            System.out.println("AI Summary: " + apt.getAiSummary());

                            return appointmentMap;
                        })
                        .collect(Collectors.toList());

                // Count pending appointments
                long pendingAppointments = appointments.stream()
                        .filter(apt -> apt.getStatus() == Appointment.Status.Pending)
                        .count();

                Map<String, Object> response = new HashMap<>();
                response.put("doctor", doctor);
                response.put("patients", List.of());
                response.put("appointments", appointmentData);
                response.put("totalAppointments", appointments.size());
                response.put("pendingAppointments", (int) pendingAppointments);
                response.put("totalPatients", 0);

                return ResponseEntity.ok(response);
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

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<?> getDoctorByUserId(@PathVariable Long userId) {
        Optional<Doctor> doctorOpt = doctorService.getDoctorByUserId(userId);
        if (doctorOpt.isPresent()) {
            return ResponseEntity.ok(doctorOpt.get());
        } else {
            return ResponseEntity.status(404).body("Doctor not found for user ID: " + userId);
        }
    }
    @Autowired
    private DoctorSlotService doctorSlotService;

    @PostMapping("/{doctorId}/slots")
    public ResponseEntity<?> createSlot(@PathVariable Long doctorId, @RequestBody Map<String, String> payload) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(payload.get("startTime"));
            LocalDateTime endTime = LocalDateTime.parse(payload.get("endTime"));

            DoctorSlot slot = doctorSlotService.createSlot(doctorId, startTime, endTime);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Slot created successfully");
            response.put("slot", slot);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create slot: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{doctorId}/slots")
    public ResponseEntity<?> getSlotsByDoctor(@PathVariable Long doctorId) {
        try {
            List<DoctorSlot> slots = doctorSlotService.getSlotsByDoctor(doctorId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("slots", slots);
            response.put("count", slots.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get slots: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/slots/{slotId}/availability")
    public ResponseEntity<?> updateAvailability(@PathVariable Long slotId, @RequestBody Map<String, Boolean> payload) {
        try {
            boolean available = payload.get("available");
            DoctorSlot slot = doctorSlotService.updateAvailability(slotId, available);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Slot availability updated successfully");
            response.put("slot", slot);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update slot availability: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<?> deleteSlot(@PathVariable Long slotId) {
        try {
            doctorSlotService.deleteSlot(slotId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Slot deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete slot: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

