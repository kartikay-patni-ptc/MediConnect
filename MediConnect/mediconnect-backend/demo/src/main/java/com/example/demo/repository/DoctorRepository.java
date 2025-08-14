package com.example.demo.repository;


import com.example.demo.model.Doctor;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByUser(User user);
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);
    List<Doctor> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
}