package com.example.demo.service;
import com.example.demo.model.Doctor;
import com.example.demo.model.DoctorDto;
import com.example.demo.model.User;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository repository;

    @Autowired
    private UserService userService;

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

        System.out.println("Saving doctor profile...");
        Doctor savedDoctor = repository.save(doctor);
        System.out.println("Doctor profile saved with ID: " + savedDoctor.getId());
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
}
