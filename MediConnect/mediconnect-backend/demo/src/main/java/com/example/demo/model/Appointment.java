package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DoctorSlot slot;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(columnDefinition = "TEXT")
    private String aiSummary;
    
    @Column(columnDefinition = "TEXT")
    private String doctorSummary;
    
    @Column(columnDefinition = "TEXT")
    private String patientAdvice;
    
    @Column(columnDefinition = "TEXT")
    private String prescribedMedicines;
    
    private String riskLevel;
    
    @Column(columnDefinition = "TEXT")
    private String redFlags;
    
    @Column(columnDefinition = "TEXT")
    private String homeRemedies;
    
    private String specializationHint;
    
    private LocalDateTime createdAt;

    public enum Status {
        Pending, Confirmed, Cancelled, Completed
    }
}
