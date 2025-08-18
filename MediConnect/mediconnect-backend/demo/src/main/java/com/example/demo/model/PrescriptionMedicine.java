package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescription_medicines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PrescriptionMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    @JsonIgnore
    private Prescription prescription;

    @Column(nullable = false)
    private String medicineName;

    @Column(nullable = false)
    private String genericName; // Generic name of the medicine

    @Column(nullable = false)
    private String dosage; // e.g., "500mg", "10ml"

    @Column(nullable = false)
    private String frequency; // e.g., "Twice daily", "Every 8 hours"

    @Column(nullable = false)
    private String duration; // e.g., "7 days", "2 weeks"

    @Column(nullable = false)
    private Integer quantity; // Number of tablets/bottles/etc.

    @Column(nullable = false)
    private String instructions; // e.g., "Take after meals", "Before bedtime"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedicineType medicineType = MedicineType.PRESCRIPTION;

    @Column(nullable = true)
    private String specialInstructions; // Additional instructions

    @Column(nullable = true)
    private String sideEffects; // Known side effects

    @Column(nullable = true)
    private String contraindications; // When not to use

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum MedicineType {
        PRESCRIPTION, // Requires prescription
        OTC, // Over the counter
        CONTROLLED // Controlled substance
    }
}
