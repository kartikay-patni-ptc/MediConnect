package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Patient patient;

    @Column(nullable = false)
    private String prescriptionNumber; // Unique prescription identifier

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT")
    private String doctorNotes;

    @Column(nullable = true)
    private String prescriptionImageUrl; // URL to uploaded prescription image/PDF

    @Column(nullable = true)
    private String originalFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionStatus status = PrescriptionStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime issuedDate;

    @Column(nullable = true)
    private LocalDateTime validUntil; // Prescription expiry date

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PrescriptionMedicine> medicines;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MedicineOrder> orders;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        issuedDate = LocalDateTime.now();
        if (validUntil == null) {
            validUntil = LocalDateTime.now().plusMonths(6); // Default 6 months validity
        }
        if (prescriptionNumber == null) {
            prescriptionNumber = generatePrescriptionNumber();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generatePrescriptionNumber() {
        return "RX" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    public enum PrescriptionStatus {
        ACTIVE,
        EXPIRED,
        CANCELLED,
        COMPLETED
    }
}
