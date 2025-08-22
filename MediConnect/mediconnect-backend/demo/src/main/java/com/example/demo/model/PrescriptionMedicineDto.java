package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionMedicineDto {
    private Long id;
    private String medicineName;
    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;
    private String contraindications;
}

