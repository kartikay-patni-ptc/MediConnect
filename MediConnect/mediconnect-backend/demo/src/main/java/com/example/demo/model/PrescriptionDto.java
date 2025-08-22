package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDto {
    private Long id;
    private String prescriptionNumber;
    private LocalDateTime prescribedDate;
    private String diagnosis;
    private String notes;
    private String status; // String representation of PrescriptionStatus enum
    private List<PrescriptionMedicineDto> medicines;
    // Note: Patient and Doctor objects are intentionally excluded to prevent circular references
}
