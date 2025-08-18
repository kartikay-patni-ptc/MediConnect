package com.example.demo.service;
import com.example.demo.model.Doctor;
import com.example.demo.model.DoctorDto;
import com.example.demo.model.DoctorVerificationRequest;
import com.example.demo.model.User;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository repository;

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorVerificationService verificationService;

    public Doctor createDoctorProfile(DoctorDto dto) {
        System.out.println("=== CREATING DOCTOR PROFILE ===");
        System.out.println("User ID: " + dto.getUserId());

        User user = userService.findById(dto.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found with id: " + dto.getUserId());
        }
        System.out.println("User found: " + user.getUsername());

        Optional<Doctor> existingProfile = repository.findByUser(user);
        if (existingProfile.isPresent()) {
            throw new RuntimeException("Doctor profile already exists for this user");
        }
        System.out.println("No existing profile found, creating new one...");

        Doctor doctor = new Doctor();
        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setPhoneNumber(dto.getPhoneNumber());
        doctor.setEmail(dto.getEmail());
        doctor.setExperience(dto.getExperience());
        doctor.setEducation(dto.getEducation());
        doctor.setHospital(dto.getHospital());
        doctor.setAddress(dto.getAddress());
        doctor.setDescription(dto.getDescription());
        doctor.setUser(user);

        // Check if this doctor was previously verified during signup
        if (dto.getWasVerified() != null && dto.getWasVerified()) {
            doctor.setIsVerified(true);
            doctor.setRegistrationNumber(dto.getLicenseNumber());
            System.out.println("Doctor was verified during signup, setting verified status to true");
        } else {
            // Fallback: check with NMC API if verification status not provided
            String fullName = dto.getFirstName() + " " + dto.getLastName();
            if (isDoctorVerified(fullName, dto.getLicenseNumber())) {
                doctor.setIsVerified(true);
                doctor.setRegistrationNumber(dto.getLicenseNumber());
                System.out.println("Doctor verified via NMC API, setting verified status to true");
            } else {
                doctor.setIsVerified(false);
                System.out.println("Doctor not verified, setting verified status to false");
            }
        }

        System.out.println("Saving doctor profile...");
        Doctor savedDoctor = repository.save(doctor);
        System.out.println("Doctor profile saved with ID: " + savedDoctor.getId());
        System.out.println("Verification status: " + savedDoctor.getIsVerified());
        return savedDoctor;
    }

    public List<Doctor> getAllDoctors() {
        return repository.findAll();
    }

    public Optional<Doctor> getDoctorByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    public Optional<Doctor> getDoctorByUser(User user) {
        return repository.findByUser(user);
    }

    public Optional<Doctor> getDoctorById(Long id) {
        return repository.findById(id);
    }

    public Doctor updateDoctor(Long id, DoctorDto dto) {
        Optional<Doctor> existingDoctor = repository.findById(id);
        if (existingDoctor.isPresent()) {
            Doctor doctor = existingDoctor.get();

            doctor.setFirstName(dto.getFirstName());
            doctor.setLastName(dto.getLastName());
            doctor.setSpecialization(dto.getSpecialization());
            doctor.setLicenseNumber(dto.getLicenseNumber());
            doctor.setPhoneNumber(dto.getPhoneNumber());
            doctor.setEmail(dto.getEmail());
            doctor.setExperience(dto.getExperience());
            doctor.setEducation(dto.getEducation());
            doctor.setHospital(dto.getHospital());
            doctor.setAddress(dto.getAddress());
            doctor.setDescription(dto.getDescription());

            return repository.save(doctor);
        }
        throw new RuntimeException("Doctor profile not found with id: " + id);
    }

    public boolean verifyDoctor(DoctorVerificationRequest request) {
        System.out.println("=== DOCTOR SERVICE: VERIFYING DOCTOR ===");
        System.out.println("User ID: " + request.getUserId());
        System.out.println("Full Name: " + request.getFullName());
        System.out.println("Registration Number: " + request.getRegistrationNumber());

        boolean isVerified = verificationService.verifyDoctor(request);
        System.out.println("Verification result: " + isVerified);

        // Note: Verification status will be applied when profile is created
        // This method is now just for checking verification status

        return isVerified;
    }

    public List<Doctor> searchDoctorsBySpecialization(String searchTerm) {
        System.out.println("=== SEARCHING DOCTORS FROM DATABASE ===");
        System.out.println("Search term: " + searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            System.out.println("No search term provided, returning all doctors");
            List<Doctor> allDoctors = repository.findAll();
            System.out.println("Found " + allDoctors.size() + " doctors");
            return allDoctors;
        }

        // Combine results and remove duplicates
        Set<Doctor> uniqueDoctors = new HashSet<>();

        // Handle multiple specializations separated by commas or other delimiters
        String[] searchTerms = searchTerm.split("[,;|&+]");
        
        for (String term : searchTerms) {
            String cleanTerm = term.trim();
            if (!cleanTerm.isEmpty()) {
                System.out.println("Searching for specialization: " + cleanTerm);
                
                // Search by specialization
                List<Doctor> specializationResults = repository.findBySpecializationContainingIgnoreCase(cleanTerm);
                System.out.println("Found " + specializationResults.size() + " doctors by specialization '" + cleanTerm + "'");
                uniqueDoctors.addAll(specializationResults);

                // Search by name (first name or last name) for this term
                List<Doctor> nameResults = repository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(cleanTerm, cleanTerm);
                System.out.println("Found " + nameResults.size() + " doctors by name '" + cleanTerm + "'");
                uniqueDoctors.addAll(nameResults);
            }
        }

        List<Doctor> combinedResults = new ArrayList<>(uniqueDoctors);

        // Sort by experience (descending) as requested
        combinedResults.sort((d1, d2) -> {
            if (d1.getExperience() == null) return 1;
            if (d2.getExperience() == null) return -1;
            return d2.getExperience().compareTo(d1.getExperience());
        });

        System.out.println("Total unique doctors found: " + combinedResults.size());

        // Log found doctors for debugging
        for (Doctor doctor : combinedResults) {
            System.out.println("Doctor: " + doctor.getFirstName() + " " + doctor.getLastName() +
                    " - " + doctor.getSpecialization() +
                    " - " + doctor.getExperience() + " years" +
                    " (Verified: " + doctor.getIsVerified() + ")");
        }

        return combinedResults;
    }

    private boolean isDoctorVerified(String fullName, String licenseNumber) {
        try {
            System.out.println("=== CHECKING IF DOCTOR WAS PREVIOUSLY VERIFIED ===");
            System.out.println("Full Name: " + fullName);
            System.out.println("License Number: " + licenseNumber);

            // Create a verification request to check with NMC API
            DoctorVerificationRequest request = new DoctorVerificationRequest();
            request.setFullName(fullName);
            request.setRegistrationNumber(licenseNumber);
            request.setUserId(0L); // Not needed for verification check

            boolean isVerified = verificationService.verifyDoctor(request);
            System.out.println("Verification check result: " + isVerified);
            return isVerified;

        } catch (Exception e) {
            System.err.println("Error checking doctor verification: " + e.getMessage());
            return false;
        }
    }
}