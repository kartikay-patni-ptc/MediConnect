package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Data
public class DoctorSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Doctor doctor;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean available;

    // Getters and setters omitted for brevity
}
