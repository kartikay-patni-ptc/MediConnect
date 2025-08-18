package com.example.demo.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicationRequest {
    private String medicationName;
    private String dosage;
    private String frequency; // optional
    private LocalDate startDate;
    private LocalDate endDate; // optional
    private String notes; // optional
}


