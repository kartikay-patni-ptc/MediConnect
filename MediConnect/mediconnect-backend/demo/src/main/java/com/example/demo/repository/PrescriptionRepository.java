package com.example.demo.repository;

import com.example.demo.model.Prescription;
import com.example.demo.model.Doctor;
import com.example.demo.model.Patient;
import com.example.demo.model.Prescription.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatientOrderByCreatedAtDesc(Patient patient);
    
    List<Prescription> findByDoctorOrderByCreatedAtDesc(Doctor doctor);
    
    List<Prescription> findByPatientAndStatusOrderByCreatedAtDesc(Patient patient, PrescriptionStatus status);
    
    List<Prescription> findByDoctorAndStatusOrderByCreatedAtDesc(Doctor doctor, PrescriptionStatus status);
    
    Optional<Prescription> findByPrescriptionNumber(String prescriptionNumber);
    
    @Query("SELECT p FROM Prescription p WHERE p.patient = :patient AND p.status = :status AND p.validUntil > :currentDate")
    List<Prescription> findActiveValidPrescriptions(@Param("patient") Patient patient, 
                                                   @Param("status") PrescriptionStatus status, 
                                                   @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT p FROM Prescription p WHERE p.doctor = :doctor AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Prescription> findByDoctorAndDateRange(@Param("doctor") Doctor doctor, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.doctor = :doctor AND p.status = :status")
    Long countByDoctorAndStatus(@Param("doctor") Doctor doctor, @Param("status") PrescriptionStatus status);
    
    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.patient = :patient AND p.status = :status")
    Long countByPatientAndStatus(@Param("patient") Patient patient, @Param("status") PrescriptionStatus status);
    
    List<Prescription> findByValidUntilBeforeAndStatus(LocalDateTime validUntil, PrescriptionStatus status);
}
