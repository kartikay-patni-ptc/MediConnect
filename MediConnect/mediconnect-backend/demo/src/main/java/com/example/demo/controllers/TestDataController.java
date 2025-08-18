package com.example.demo.controllers;

import com.example.demo.model.Doctor;
import com.example.demo.model.DoctorSlot;
import com.example.demo.model.User;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.repository.DoctorSlotRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestDataController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorSlotRepository doctorSlotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/init-data")
    public ResponseEntity<String> initializeTestData() {
        try {
            // Create test doctors
            createTestDoctors();
            return ResponseEntity.ok("Test data initialized successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error initializing test data: " + e.getMessage());
        }
    }

    @PostMapping("/init-slots")
    public ResponseEntity<String> initializeSlots() {
        try {
            // Get existing doctors
            List<Doctor> doctors = doctorRepository.findAll();
            
            if (doctors.isEmpty()) {
                return ResponseEntity.badRequest().body("No doctors found. Please create doctors first.");
            }
            
            // Create slots for the next 7 days for all existing doctors
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 7; i++) {
                LocalDate slotDate = today.plusDays(i);
                
                // Morning slots (9 AM - 12 PM)
                for (int hour = 9; hour < 12; hour++) {
                    for (Doctor doctor : doctors) {
                        createSlot(doctor, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                    }
                }
                
                // Afternoon slots (2 PM - 5 PM)
                for (int hour = 14; hour < 17; hour++) {
                    for (Doctor doctor : doctors) {
                        createSlot(doctor, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                    }
                }
            }
            
            return ResponseEntity.ok("Slots initialized successfully for " + doctors.size() + " doctors!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error initializing slots: " + e.getMessage());
        }
    }

    private void createTestDoctors() {
        // Create test users for doctors (only if they don't exist)
        User doctorUser1 = userRepository.findByUsername("dr.smith").orElseGet(() -> {
            User user = new User();
            user.setUsername("dr.smith");
            user.setEmail("dr.smith@mediconnect.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.DOCTOR);
            return userRepository.save(user);
        });

        User doctorUser2 = userRepository.findByUsername("dr.johnson").orElseGet(() -> {
            User user = new User();
            user.setUsername("dr.johnson");
            user.setEmail("dr.johnson@mediconnect.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.DOCTOR);
            return userRepository.save(user);
        });

        User doctorUser3 = userRepository.findByUsername("dr.williams").orElseGet(() -> {
            User user = new User();
            user.setUsername("dr.williams");
            user.setEmail("dr.williams@mediconnect.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.DOCTOR);
            return userRepository.save(user);
        });

        // Create doctors
        Doctor doctor1 = new Doctor();
        doctor1.setFirstName("Dr. Sarah");
        doctor1.setLastName("Smith");
        doctor1.setSpecialization("Cardiology");
        doctor1.setLicenseNumber("CARD001");
        doctor1.setPhoneNumber("+1-555-0101");
        doctor1.setEmail("dr.smith@mediconnect.com");
        doctor1.setExperience(15);
        doctor1.setEducation("MD, Cardiology");
        doctor1.setHospital("City General Hospital");
        doctor1.setAddress("123 Medical Center Dr, City");
        doctor1.setDescription("Experienced cardiologist specializing in heart disease prevention and treatment.");
        doctor1.setIsVerified(true);
        doctor1.setRegistrationNumber("REG001");
        doctor1.setUser(doctorUser1);
        doctorRepository.save(doctor1);

        Doctor doctor2 = new Doctor();
        doctor2.setFirstName("Dr. Michael");
        doctor2.setLastName("Johnson");
        doctor2.setSpecialization("Dermatology");
        doctor2.setLicenseNumber("DERM001");
        doctor2.setPhoneNumber("+1-555-0102");
        doctor2.setEmail("dr.johnson@mediconnect.com");
        doctor2.setExperience(12);
        doctor2.setEducation("MD, Dermatology");
        doctor2.setHospital("Skin Care Clinic");
        doctor2.setAddress("456 Health Plaza, City");
        doctor2.setDescription("Board-certified dermatologist with expertise in skin conditions and cosmetic procedures.");
        doctor2.setIsVerified(true);
        doctor2.setRegistrationNumber("REG002");
        doctor2.setUser(doctorUser2);
        doctorRepository.save(doctor2);

        Doctor doctor3 = new Doctor();
        doctor3.setFirstName("Dr. Emily");
        doctor3.setLastName("Williams");
        doctor3.setSpecialization("Pediatrics");
        doctor3.setLicenseNumber("PED001");
        doctor3.setPhoneNumber("+1-555-0103");
        doctor3.setEmail("dr.williams@mediconnect.com");
        doctor3.setExperience(8);
        doctor3.setEducation("MD, Pediatrics");
        doctor3.setHospital("Children's Medical Center");
        doctor3.setAddress("789 Family Care Ave, City");
        doctor3.setDescription("Caring pediatrician dedicated to children's health and development.");
        doctor3.setIsVerified(true);
        doctor3.setRegistrationNumber("REG003");
        doctor3.setUser(doctorUser3);
        doctorRepository.save(doctor3);

        // Create slots for the next 7 days
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate slotDate = today.plusDays(i);
            
            // Morning slots (9 AM - 12 PM)
            for (int hour = 9; hour < 12; hour++) {
                createSlot(doctor1, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                createSlot(doctor2, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                createSlot(doctor3, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
            }
            
            // Afternoon slots (2 PM - 5 PM)
            for (int hour = 14; hour < 17; hour++) {
                createSlot(doctor1, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                createSlot(doctor2, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                createSlot(doctor3, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
            }
        }
    }

    private void createSlot(Doctor doctor, LocalDate date, LocalTime startTime, LocalTime endTime) {
        DoctorSlot slot = new DoctorSlot();
        slot.setDoctor(doctor);
        slot.setStartTime(date.atTime(startTime));
        slot.setEndTime(date.atTime(endTime));
        slot.setAvailable(true);
        doctorSlotRepository.save(slot);
    }
}
